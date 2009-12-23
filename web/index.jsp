<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" style="height:100%;margin:0">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>WheresMyTube.com</title>
    <%
        String mapsKey = null;
        if (request.getRequestURL().toString().contains("beta")) {
            mapsKey = getServletConfig().getServletContext().getInitParameter("GoogleMapsKeyBeta");
        } else {
            mapsKey = getServletConfig().getServletContext().getInitParameter("GoogleMapsKey");
        }

    %>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=mapsKey%>" type="text/javascript"></script>
    <script src="javascripts/prototype.js" type="text/javascript"></script>
    <script src="javascripts/scriptaculous.js" type="text/javascript"></script>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css"/>
</head>
<body onunload="GUnload();" onload="loadMap();" style="height:100%;margin:0">

<div id="navigation" style="width: 300px; height: 100%;">
    <h1>Where's My Tube?</h1>

    <p><b>A realtime view of the London Underground.</b></p>
    <br/>

    <div>
        <p><img src="/images/vn.png" alt="up"/>
            Markers represent Underground trains and and their direction of travel.
        </p>

        <p><img src="/images/station.png" alt="(St)"/>
            's represent Underground stations, click to see the live arrival boards for that station</p>
    </div>

    <!--<div id="liveInfoTEST" style="width:240px; height:100px;">-->
    <!--<table border="1px solid black;border-collapse:collapse;">-->
    <!--<table>
           <tr><td>piccadilly</td><td><img src='/images/loader.gif' alt='.'/></td></tr>
            <tr><td>central</td><td><img src='/images/loader.gif' alt='.'/></td></tr>
            <tr><td>northern</td><td><img src='/images/tick.png' alt='.'/></td></tr>
            <tr><td>victoria</td><td><img src='/images/warn.png' alt='.'/></td></tr>
        </table>-->
    <!--</div>-->

    <!--
    <div id="liveInfo">
        <a href="#" onclick="showUpdateStatusBox();return false;">Show update status</a>
    </div>-->


    <div id="liveInfoInternal" style="margin:48px;">
    </div>

    <div id="footer">
        <!--<p>This site is a mash-up between Google Maps and the TFL Live Arriaval Boards site.</p>
        <table>
            <tr>
                <td><a href="http://maps.google.uk">Google Maps</a></td>
            </tr>
            <tr>
                <td><a href="http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/">TFL Departure Boards</a></td>
            </tr>
        </table>
        -->

        <table>
            <tr>
                <td><p>For educational purposes only; this site is in no way connected to TFL.
                    <a href="mailto:tube@charleskubicek.com">contact</a></p></td>
                <td><img src="http://code.google.com/appengine/images/appengine-silver-120x30.gif"
                         alt="Powered by Google App Engine"/></td>
            </tr>
        </table>
    </div>
</div>
<div id="content" style="height: 100%;">
</div>

<script type="text/javascript">

function state(branches) {
    this.trainsOnMap = {};
    this.preMap = {};
    this.trainsOnMapToggle = {};
    this.tainsAtPoints = {};//only use x
    this.parsingState = {};

    for (var i = 0; i < branches.length; i++) {
        this.trainsOnMap[branches[i]] = [];
    }

    for (var i = 0; i < branches.length; i++) {
        this.preMap[branches[i]] = [];
    }
}

function showUpdateStatusBox() {
    $('liveInfoInternal').appear();
    $('liveInfo').hide();
}

var imagesFolder =
<%=getServletConfig().getServletContext().getInitParameter("com.web.imagesFolder")%>
var appContext =
<%=getServletConfig().getServletContext().getInitParameter("com.web.appContext")%>
var rootResourceUrl = appContext + "/rest/"

var myState = null
var map = null

var directonImageDict = {};
directonImageDict["N"] = imagesFolder + "/jubilee-square.png"
directonImageDict["S"] = imagesFolder + "/jubilee-square.png"

directonImageDict["W"] = imagesFolder + "/left5.png";
directonImageDict["E"] = imagesFolder + "/right5.png";
directonImageDict["I"] = imagesFolder + "/left5.png";
directonImageDict["O"] = imagesFolder + "/right5.png";

lineColourDict = {};
lineColourDict["northern"] = '#000000';
lineColourDict["victoria"] = '#009FE0';
lineColourDict["jubilee"] = '#8F989E';
lineColourDict["bakerloo"] = '#AE6118';
lineColourDict["metropolitan"] = '#893267';
lineColourDict["central"] = '#ff0000';
lineColourDict["piccadilly"] = '#0000c8';

var useLocalServerData = getURLParam("local")

function loadMap() {
    if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("content"));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
        map.setCenter(new GLatLng(51.5232, -0.1836), 12);

        GDownloadUrl(rootResourceUrl, function(data, responseCode) {
            if (responseCode == 200) {
                var linesObj = eval('(' + data + ')');
                var linesToGet = [];

                linesObj.lines.linesArray.each(function(line) {
                    linesToGet.push(line.name)
                })

                myState = new state(linesToGet)

                linesToGet.each(function(line, index) {
                    drawStations(line)

                    /**
                     * Introduce a delay so when lines are reloaded it's clear to see
                     * what has changed
                     */
                    registerPoller(((3 * index) * 1000), line)
                })
            }else{
                var message = "Could not retreive data from server, please try again later";
                document.getElementById('liveInfoInternal').innerHTML = message;
            }
        })
    }
}

function registerPoller(waitTime, line) {
    log("waiting for " + waitTime)
    setTimeout(function() {
        loadTrains(line)
        reloadBranchAfterTimeout(line)
    }, waitTime);
}

function log(msg) {
    if (window.console) {
        console.log(msg);
    }
}

function reloadBranchAfterTimeout(line) {
    setTimeout(function() {
        try {
            loadTrains(line)
            reloadBranchAfterTimeout(line)
        } catch(e) {
            log("caught exception " + e)
        }
        return;
    }, (60 * 1000));
}

/**
 * TODO optimize by downloading muliple/all lines
 * @param line
 */
function drawStations(line) {
    var url = appContext + "/static/stations/" + line + ".json";
    var icon = stationIcon();

    GDownloadUrl(url, function(data, responseCode) {
        var stationsObj = eval('(' + data + ')');
        stationsObj.stations.stationsArray.each(function(branchArray) {
            var points = [];
            branchArray.each(function(stationObj) {
                var point = new GLatLng(stationObj.lat, stationObj.lng);
                points.push(point);
                map.addOverlay(makeStationMarker(point, stationObj, line, icon));
            })
            map.addOverlay(new GPolyline(points, lineColourDict[line], 4, 1));
        })
    })
}


//* uses googles download url to load json into map
//
// the jons that comes back uses these values
//        LONG("points", "pointsArray", "lat", "lng", "direction", "description"),
//        SHORT("p", "a", "t", "g", "d", "i");
function loadTrains(line) {
    var url = appContext + "/rest/lines/" + line
    url = useLocalServerData ? url + "?local=true" : url
    addBranchWaitingFor(line)

    GDownloadUrl(url, function(data, responseCode) {
        if (responseCode == 200) {
            log("data was "+data);
            removeTrainsFromMap(line);
            var pointsObj = eval('(' + data + ')');
            var preMapTuples = [];

            if (pointsObj.error) {
                log("error was: " + pointsObj.error);
                myState.preMap[line] = preMapTuples;
                removeTrainsAfterError(line);
            } else {
                pointsObj.p.a.each(function(pointObj) {
                    preMapTuples.push(preMapTuple(pointObj, line));
                });

                myState.preMap[line] = preMapTuples;
                myState.trainsOnMapToggle[line] = true;
                pauseBeforeAddingTrainsBackToMap(line);
            }
        } else {
            log("response code was "+responseCode);
            myState.preMap[line] = [];
            removeTrainsAfterError(line);
        }
    })
}

function preMapTuple(pointObj, line) {
    var tuple = {};
    tuple.lat = pointObj.t;
    tuple.long = pointObj.g;
    tuple.info = pointObj.i;
    tuple.direction = pointObj.d;
    tuple.line = line;
    return tuple;
}

function now() {
    var d = new Date();
    return d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + ":" + d.getMilliseconds();
}

function redrawAll() {
    var allTuples = {};
    var allPointsForMap = {};

    for (var line in myState.preMap) {
        allPointsForMap[line] = [];
        myState.preMap[line].each(function(tuple) {
            if (allTuples[tuple.lat] == null) {
                allPointsForMap[line].push(makeTrainMarkerFromTuple(tuple, 0));
                allTuples[tuple.lat] = 1;
            } else {
                allPointsForMap[line].push(makeTrainMarkerFromTuple(tuple, allTuples[tuple.lat]));
                allTuples[tuple.lat]++;
            }
        })
    }

    for (var line in myState.trainsOnMap) {
        line = [];
    }

    for (var line in allPointsForMap) {
        allPointsForMap[line].each(function(trainMarker) {
            myState.trainsOnMap[line].push(trainMarker)
            map.addOverlay(trainMarker);
        });
    }
}

function pauseBeforeAddingTrainsBackToMap(line) {
    setTimeout(function() {
        redrawAll();
        removeBranchWaitingFor(line);
    }, 1000);
}

function removeTrainsAfterError(line) {
    redrawAll();
    makeErrorParseStatus(line);
    updateInfoBox2(line);
}

function addBranchWaitingFor(line) {
    makeWaitingForResultParseStatus(line)
    updateInfoBox2(line);
}

function removeBranchWaitingFor(line) {
    makeGotResultParseStatus(line)
    updateInfoBox2(line);
}

function updateInfoBox2(tableEntries) {
    var infoString = "Update Status:\n<table class='status'>";
    for (var line in myState.parsingState) {
        infoString += myState.parsingState[line].createTableRow(line);
    }
    infoString += "</table>";
    document.getElementById('liveInfoInternal').innerHTML = infoString;
}

function stationIcon() {
    var icon = new GIcon();
    icon.image = imagesFolder + "/station.png";
    icon.iconSize = new GSize(14, 14);
    icon.shadow = "";
    icon.iconAnchor = new GPoint(7, 7);
    icon.infoWindowAnchor = new GPoint(6, 10);

    return icon;
}

function makeStationMarker(point, stationObj, line, icon) {
    var marker = new GMarker(point, icon)

    GEvent.addListener(marker, "click", function() {
        marker.openInfoWindowHtml(stationObj.name + "<br/><p><a href='http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=" + line + "&StationCode=" + stationObj.code + "'>Go to live departure board</a></p>");
    });

    return marker
}

function makeTrainMarkerFromTuple(tupple, markersAlreadyPresent) {
    var marker = new GMarker(new GLatLng(tupple.lat, tupple.long),
            makeTrainIcon(tupple.line, tupple.direction, markersAlreadyPresent));

    GEvent.addListener(marker, "click", function() {
        marker.openInfoWindowHtml(tupple.info + " (" + tupple.direction + ")");
    });
    return marker;
}

function makeImageName(line, direction) {
    return imagesFolder + "/" + line.charAt(0) + direction.toLowerCase() + ".png"
}

function makeTrainIcon(line, direction, markersAlreadyPresent) {
    var icon = new GIcon();
    icon.image = makeImageName(line, direction);
    icon.iconSize = new GSize(34, 34);
    icon.shadow = "";
    icon.iconAnchor = makeGPoint(markersAlreadyPresent);
    icon.infoWindowAnchor = new GPoint(6, 10);

    return icon;
}

function makeGPoint(trainsAtStation) {
    // 17 (34/2) is the distance from the actual spot that the icon is drawn
    var multiplier = 7
    var x = 17
    var result = x + (trainsAtStation * multiplier)
    return new GPoint(result, 34);
}

function addTrainsToMap(line) {
    myState.trainsOnMap[line].each(function(item) {
        map.addOverlay(item)
    });
}

function removeTrainsFromMap(line) {
    myState.trainsOnMap[line].each(function(item) {
        map.removeOverlay(item)
    });
}

function makeGlobalKeyFromGMarker(gMarker) {
    return gMarker.getLatLng().lat()
}

function makeGlobalKeyFromPoint(point) {
    return point.lat()
}

function addGlobalPoint(gMarker) {
    var key = makeGlobalKeyFromGMarker(gMarker)
    if (myState.tainsAtPoints[key] == null) {
        myState.tainsAtPoints[key] = 0
    } else {
        myState.tainsAtPoints[key]++
    }
}

function removeGlobalPoint(gMarker) {
    var key = makeGlobalKeyFromGMarker(gMarker);
    if ((myState.tainsAtPoints[key] != null) && (myState.tainsAtPoints[key] > 0)) {
        myState.tainsAtPoints[key]--;
    }
}

function makeWaitingForResultParseStatus(lineName) {
    updateParsingState(lineName, "loader.gif");
}

function makeGotResultParseStatus(lineName) {
    updateParsingState(lineName, "tick.png");
}

function makeErrorParseStatus(lineName) {
    updateParsingState(lineName, "warn.png");
}

function updateParsingState(lineName, imageFileName) {
    myState.parsingState[lineName] = new HtmlParseStatusCell("<img src='/images/" + imageFileName + "' alt='.'/>")
}

function HtmlParseStatusCell(cellHtml) {
    this.html = cellHtml;
    this.createTableRow = function(lineName) {
        // cheap horizontal padding with space
        var styleSt = "background-color:" + lineColourDict[lineName] + ";color: white;font-weight: bold;";
        var displayLineName = lineName.charAt(0).toUpperCase() + lineName.substring(1);
        return "<tr><td style='" + styleSt + "'>" + displayLineName + "</td><td width='100' style='" + styleSt + "'></td><td>" + cellHtml + "</td></tr>";
    };
}

// taken from http://mattwhite.me/11tmr.nsf/D6Plinks/MWHE-695L9Z
function getURLParam(strParamName) {
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
//]]>
</script>
</body>
</html>