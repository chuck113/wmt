<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" style="height:100%;margin:0">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>WheresMyTube.com</title>
    <script type="text/javascript"
            src="http://www.google.com/jsapi?key=ABQIAAAAANyf_1x1i_h8KT1GEqKZvxQOY00Tn5-R_voQhXk7sS84qtWqXBQQNZtoyxsSXaRFj9M6lbqv7gw51Q"></script>
    <script type="text/javascript">
        google.load("jquery", '1.3');
        google.load("maps", "2.x");
    </script>
    <!--<script type="text/javascript" src="javascript/main.js"></script>-->
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css"/>
</head>
<body onunload="GUnload();">

<div id="navigation">
    <div id="navigation-wrapper">
        <h1>Where's My Tube?</h1>

        <p><b>A realtime view of the London Underground.</b></p>
        <br/>

        <div>
            <p><img src="/images/vn.png" alt="up"/>
                Markers represent Underground trains and and their direction of travel.
            </p>

            <p><img src="/images/station.png" alt="(St)"/>
                's represent Underground stations, click to see the live arrival boards for that station</p>

            <p>Each line is automatically updated once per minute.</p>
        </div>

        <div id="liveInfoInternal" style="margin:48px;">
        </div>

        <div id="footer">
            <a href="about.html">About</a> &bull;
            <a href="mailto:tube@charleskubicek.com">Contact</a>
            <br/>
            By <a href="http://charleskubicek.com">Charles Kubicek</a>
        </div>
    </div>
</div>
<div id="content" style="height: 100%;">
</div>

<script type="text/javascript">

var imagesFolder =
<%=getServletConfig().getServletContext().getInitParameter("com.web.imagesFolder")%>
var appContext = <%=getServletConfig().getServletContext().getInitParameter("com.web.appContext")%>

$(document).ready(function() {
    new WMT().loadMap(appContext + "/rest/")
});

WMT = function() {
    this.useLocalServerData = this.getURLParam("local")
    this.useJsonp = this.getURLParam("jsonp")
    this.directAjax = this.getURLParam("directAjax")

    this.isDirectAjax = function() {
        return this.directAjax;
    }

    this.useJsonp = function() {
        return this.useJsonp;
    }

    this.useLocalServerData = function() {
        return this.useLocalServerData;
    }

    this.urlParameters = function() {
        if (!this.useLocalServerData && this.directAjax)return ""
        else if (!this.useLocalServerData && !this.directAjax)return "?jsonp=true&jsoncallback=?";
        else if (this.useLocalServerData && this.directAjax)return "?local=true"
            else return "?local=true&jsonp=true&jsoncallback=?";
    }
}

// taken from http://mattwhite.me/11tmr.nsf/D6Plinks/MWHE-695L9Z
// assumes no '?'s in
WMT.prototype.getURLParam = function(strParamName) {
    var strReturn = "";
    var strHref = window.location.href;
    if (strHref.indexOf("?") > -1) {
        var strQueryString = strHref.substr(strHref.indexOf("?")).toLowerCase();
        var aQueryString = strQueryString.split("&");
        for (var iParam = 0; iParam < aQueryString.length; iParam++) {
            if (aQueryString[iParam].indexOf(strParamName.toLowerCase() + "=") > -1) {
                var aParam = aQueryString[iParam].split("=");
                strReturn = aParam[1];
                break;
            }
        }
    }
    return unescape(strReturn);
}

WMT.prototype.loadMap = function(rootResourceUrl) {
    if (GBrowserIsCompatible()) {
        var map = new GMap2(document.getElementById("content"));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
        map.setCenter(new GLatLng(51.5232, -0.1836), 12);

        var urlParameters = this.urlParameters();
        var wmt = this;

        GDownloadUrl(rootResourceUrl, function(data, responseCode) {
            if (responseCode == 200) {
                var linesObj = eval('(' + data + ')');
                var linesToGet = [];

                $.each(linesObj.lines.linesArray, function(index, value) {
                    linesToGet.push(value.name);
                });

                var mapContext = new MapContext(linesToGet, appContext, map, wmt);
                mapContext.drawStations();

                $.each(linesObj.lines.linesArray, function(index, lineData) {
                    var url = "http://" + lineData.url + urlParameters;
                    var loader = new TrainDataLoader(url, lineData, mapContext)
                    new Poller(((3 * index) * 1000), mapContext, loader).startPolling();
                })
            } else {
                var message = "Could not retreive data from server, please try again later";
                document.getElementById('liveInfoInternal').innerHTML = message;
            }
        })
    }
}

function MapContext(lines, appContext, gMap, wmt) {
    this.lines = lines;
    this.appContext = appContext;
    this.wmt = wmt;
    this.gMap = gMap;
    this.clusterMap = new Clusterer(gMap);
    this.trainsOnMap = {};
    this.parsingState = {};
    this.markerUtils = new MarkerUtils();
    this.mapModel = new MapModel(lines);

    for (var i = 0; i < lines.length; i++) {
        this.trainsOnMap[lines[i]] = [];
    }

    this.isDirectAjax = function() {
        return this.wmt.isDirectAjax();
    }

    /**
     * TODO optimize by downloading muliple/all lines
     * @param line
     */
    this.drawStations = function drawStations() {
        for (var i = 0; i < this.lines.length; i++) {
            this.drawStation(this.lines[i]);
        }
    };

    this.removeTrainsFromMap = function(line) {
        var trainsOnLine = this.mapModel.getTrainsOnMap(line);
        for (i = 0; i < trainsOnLine.length; i++) {
            //this.gMap.removeOverlay(trainsOnLine[i])
            this.clusterMap.removeOverlay(trainsOnLine[i])
        }
    }

    this.updateLine = function(line, preMapTuples) {
        this.mapModel.addTrainTuples(line, preMapTuples)
    }

    this.applyModelToGMap = function() {
        this.mapModel.applyModelToGMap(gMap)
    }

    this.drawStation = function(line) {
        var url = this.appContext + "/static/stations/" + line + ".json";
        var icon = this.markerUtils.stationIcon();
        var markerUtils = this.markerUtils;

        GDownloadUrl(url, function(data, responseCode) {
            var stationsObj = eval('(' + data + ')');
            //alert("data is: "+data)

            for(j=0; j<stationsObj.stations.stationsArray.length; j++){
                var branchArray = stationsObj.stations.stationsArray[j]
                var points = [];
                for(i=0; i<branchArray.length; i++){
                    var stationObj = branchArray[i]
                    var point = new GLatLng(stationObj.lat, stationObj.lng);
                    points.push(point);
                    //clusterMap.addOverlay(markerUtils.makeStationMarker(point, stationObj, line, icon));
                    gMap.addOverlay(markerUtils.makeStationMarker(point, stationObj, line, icon));
                }
                gMap.addOverlay(new GPolyline(points, markerUtils.getLineColour(line), 4, 1));
            }
        })
    };
}

MapModel = function(lines) {
    this.trainsOnLines = {};
    this.trainsOnMap = {};
    //this.trainsOnMapToggle = {};

    for (var i = 0; i < lines.length; i++) {
        this.trainsOnLines[lines[i]] = [];
    }

    for (var i = 0; i < lines.length; i++) {
        this.trainsOnMap[lines[i]] = [];
    }

    this.getTrainsOnMap = function(line) {
        return this.trainsOnMap[line];
    }

    this.addTrainTuples = function(line, preMapTuples) {
        this.trainsOnLines[line] = preMapTuples;
    }
    
    this.applyModelToGMap = function(gMap) {
        var allTuples = {};
        var allPointsForMap = {};
        var markerUtils = new MarkerUtils();

        for (var line in this.trainsOnLines) {
            allPointsForMap[line] = [];
            $.each(this.trainsOnLines[line], function(index, tuple){
                if (allTuples[tuple.lat] == null) {
                    allPointsForMap[line].push(markerUtils.makeTrainMarkerFromTuple(tuple, 0));
                    allTuples[tuple.lat] = 1;
                } else {
                    allPointsForMap[line].push(markerUtils.makeTrainMarkerFromTuple(tuple, allTuples[tuple.lat]));
                    allTuples[tuple.lat]++;
                }
            })
        }

        for (var line in this.trainsOnMap) {
            line = [];
        }

        for (var line in allPointsForMap) {
            for (i = 0; i < allPointsForMap[line].length; i++) {
                this.trainsOnMap[line].push(allPointsForMap[line][i])
                clusterMap.addOverlay(allPointsForMap[line][i], "Zoom in to see trains.");
                //gMap.addOverlay(allPointsForMap[line][i]);
            }
        }
    }
}

MapModel.prototype.removeTrains = function(line) {
    this.trainsOnLines[line] = []
}

var Poller = function(waitTime, myState, loader) {
    this.loader = loader;
    this.initialWaitTime = waitTime;
    this.myState = myState;
}

Poller.prototype.startPolling = function() {
    log("waiting for " + this.initialWaitTime);
    var pollerInstance = this;

    var onTimeout_ = function() {
        pollerInstance.loader.load(new TrainDataHandler(pollerInstance.myState));
        pollerInstance.reloadBranchAfterTimeout();
    }

    setTimeout(function() {   // initial pause
        onTimeout_();
        log("executing after timeout...")
        setTimeout(function() {
            try {
                onTimeout_();
            } catch(e) {
                log("caught exception " + e);
            }
            return;
        }, (60 * 1000));
    }, this.initialWaitTime);
}

Poller.prototype.reloadBranchAfterTimeout = function() {
    var pollerInstance = this;

    setTimeout(function() {
        try {
            pollerInstance.loader.load(new TrainDataHandler(pollerInstance.myState));
            pollerInstance.reloadBranchAfterTimeout();
        } catch(e) {
            log("caught exception " + e)
        }
        return;
    }, (60 * 1000));
}

var TrainDataLoader = function(url, lineData, myState) {
    this.markerUtils = new MarkerUtils();
    this.url = url;
    this.lineData = lineData;
    this.myState = myState;
}

TrainDataLoader.prototype.load = function(trainDataHandler) {
    var line = this.lineData.name;

    //this.addBranchWaitingFor(line)
    //trainDataHandler.preCall(line)
    //    var resultCallback = function(pointsObj, line) {
    //        trainDataHandler.onResult(pointsObj, line);
    //    };
    trainDataHandler.preCall(line);

    if (this.myState.isDirectAjax()) {
        GDownloadUrl(this.url, function(data, responseCode) {
            if (responseCode == 200) {
                var pointsObj = eval('(' + data + ')');
                //successCallback(pointsObj, line)
                //trainDataHandler.onResult(pointsObj, line)
                trainDataHandler.onResult(pointsObj, line);
            } else {
                log("response code was " + responseCode);
                //this.myState.preMap[line] = [];
                trainDataHandler.onError(line);
            }
        })
    } else {
        $.getJSON(this.url, function (data) {
            //successCallback(data, line);
            trainDataHandler.onResult(data, line)
            //resultCallback(data, line);
        });
    }
}

var TrainDataHandler = function(myState) {
    this.trainDrawer = new TrainDrawing(myState);
}

TrainDataHandler.prototype.onResult = function(pointsObj, line) {
    this.trainDrawer.loadTrainsResultHandler(pointsObj, line);
}

TrainDataHandler.prototype.onError = function(line) {
    this.trainDrawer.removeTrainsAfterError(line)
}

TrainDataHandler.prototype.preCall = function(line) {
    this.trainDrawer.addBranchWaitingFor(line)
}


/**
 * Deals with drawing Trains on the map
 * @param myState
 */
function TrainDrawing(myState) {
    this.markerUtils = new MarkerUtils();
    this.myState = myState;
}

TrainDrawing.prototype.preMapTuple = function(pointObj, line) {
    var tuple = {};
    tuple.lat = pointObj.t;
    tuple.long = pointObj.g;
    tuple.info = pointObj.i;
    tuple.direction = pointObj.d;
    tuple.line = line;
    return tuple;
}

TrainDrawing.prototype.loadTrainsResultHandler = function(pointsObj, line) {
    this.myState.removeTrainsFromMap(line);
    var preMapTuples = [];

    if (pointsObj.error) {
        log("error was: " + pointsObj.error);
        this.removeTrainsAfterError(line);
    } else {
        for (i = 0; i < pointsObj.p.a.length; i++) {
            preMapTuples.push(this.preMapTuple(pointsObj.p.a[i], line));
        }

        this.myState.updateLine(line, preMapTuples);
        this.pauseBeforeAddingTrainsBackToMap(line);
    }
}

TrainDrawing.prototype.pauseBeforeAddingTrainsBackToMap = function(line) {
    var trainDrawingInstance = this;

    setTimeout(function() {
        trainDrawingInstance.redrawAll();
        trainDrawingInstance.removeBranchWaitingFor(line);
    }, 1000);
}

TrainDrawing.prototype.removeTrainsAfterError = function(line) {
    this.redrawAll();
    this.makeErrorParseStatus(line);
    this.updateInfoBox2(line);
}

TrainDrawing.prototype.redrawAll = function() {
    this.myState.applyModelToGMap();
}

TrainDrawing.prototype.addBranchWaitingFor = function(line) {
    this.makeWaitingForResultParseStatus(line)
    this.updateInfoBox2(line);
}

TrainDrawing.prototype.removeBranchWaitingFor = function(line) {
    this.makeGotResultParseStatus(line)
    this.updateInfoBox2(line);
}

TrainDrawing.prototype.updateInfoBox2 = function(tableEntries) {
    var infoString = "Update Status:\n<table class='status'>";
    for (var line in this.myState.parsingState) {
        infoString += this.myState.parsingState[line].createTableRow(line);
    }
    infoString += "</table>";
    document.getElementById('liveInfoInternal').innerHTML = infoString;
}

TrainDrawing.prototype.makeWaitingForResultParseStatus = function(lineName) {
    this.updateParsingState(lineName, "loader.gif");
}

TrainDrawing.prototype.makeGotResultParseStatus = function(lineName) {
    this.updateParsingState(lineName, "tick.png");
}

TrainDrawing.prototype.makeErrorParseStatus = function(lineName) {
    this.updateParsingState(lineName, "warn.png");
}

TrainDrawing.prototype.updateParsingState = function(lineName, imageFileName) {
    this.myState.parsingState[lineName] = new HtmlParseStatusCell("<img src='/images/" + imageFileName + "' alt='.'/>",
            this.markerUtils.getLineColour(lineName))
}


function HtmlParseStatusCell(cellHtml, colour) {
    this.html = cellHtml;
    this.createTableRow = function(lineName) {
        // cheap horizontal padding with space
        var styleSt = "background-color:" + colour + ";color: white;font-weight: bold;";
        var displayLineName = lineName.charAt(0).toUpperCase() + lineName.substring(1);
        return "<tr><td style='" + styleSt + "'>" + displayLineName + "</td><td width='100' style='" + styleSt + "'></td><td>" + cellHtml + "</td></tr>";
    };
}


function log(msg) {
    if (window.console) {
        console.log(msg);
    }
}


MarkerUtils = function() {
    this.lineColourDict = {};
    this.lineColourDict["northern"] = '#000000';
    this.lineColourDict["victoria"] = '#009FE0';
    this.lineColourDict["jubilee"] = '#8F989E';
    this.lineColourDict["bakerloo"] = '#AE6118';
    this.lineColourDict["metropolitan"] = '#893267';
    this.lineColourDict["central"] = '#ff0000';
    this.lineColourDict["piccadilly"] = '#0000c8';

    this.getLineColour = function(line) {
        return this.lineColourDict[line];
    }
}

MarkerUtils.prototype.stationIcon = function() {
    var icon = new GIcon();
    icon.image = imagesFolder + "/station.png";
    icon.iconSize = new GSize(14, 14);
    icon.shadow = "";
    icon.iconAnchor = new GPoint(7, 7);
    icon.infoWindowAnchor = new GPoint(6, 10);

    return icon;
}

MarkerUtils.prototype.makeStationMarker = function(point, stationObj, line, icon) {
    var marker = new GMarker(point, icon)

    GEvent.addListener(marker, "click", function() {
        marker.openInfoWindowHtml(stationObj.name + "<br/><p><a href='http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=" + line + "&StationCode=" + stationObj.code + "'>Go to live departure board</a></p>");
    });

    return marker
}

MarkerUtils.prototype.makeTrainMarkerFromTuple = function(tupple, markersAlreadyPresent) {
    var marker = new GMarker(new GLatLng(tupple.lat, tupple.long),
            this.makeTrainIcon(tupple.line, tupple.direction, markersAlreadyPresent));

    GEvent.addListener(marker, "click", function() {
        marker.openInfoWindowHtml(tupple.info + " (" + tupple.direction + ")");
    });
    return marker;
}

MarkerUtils.prototype.makeImageName = function(line, direction) {
    return imagesFolder + "/" + line.charAt(0) + direction.toLowerCase() + ".png"
}

MarkerUtils.prototype.makeTrainIcon = function(line, direction, markersAlreadyPresent) {
    var icon = new GIcon();
    icon.image = this.makeImageName(line, direction);
    icon.iconSize = new GSize(18, 32);
    icon.shadow = "";
    icon.iconAnchor = this.makeGPoint(markersAlreadyPresent);
    icon.infoWindowAnchor = new GPoint(6, 10);

    return icon;
}

MarkerUtils.prototype.makeGPoint = function(trainsAtStation) {
    // 9 (18/2) is the distance from the actual spot that the icon is drawn
    var multiplier = 7
    var x = 9
    var result = x + (trainsAtStation * multiplier)
    return new GPoint(result, 32);
}
    //]]>
</script>
</body>
</html>