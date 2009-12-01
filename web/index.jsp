<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" style="height:100%;margin:0">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>WheresMyTube.com</title>
    <%
        String mapsKey = null;
        if(request.getRequestURL().toString().contains("beta")){
            mapsKey = getServletConfig().getServletContext().getInitParameter("GoogleMapsKeyBeta");
        } else{
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
    <br/><p>View the real-time location of tube trains</p>
    <br/><br/>

    <div>
        <p><img src="/images/up4.png" alt="up"/><br/>
            Red markers represent Underground trains and and their direction of travel.
        </p>

        <p><img src="/images/station.png" alt="(St)"/><br/>
            's represent Underground stations, click to see the live arrival boards for that station</p>
    </div>
    <br/><br/>

    <div id="liveInfo" style="display:none; width:240px; height:60px; background:#00CC00; border:1px solid #333;">
        loading trains..
    </div>

    <div id="footer">
        <p>This site is a mash-up between Google Maps and the TFL Live Arriaval Boards site.</p>
        <table>
            <tr>
                <td><a href="http://maps.google.uk">Google Maps</a></td>
            </tr>
            <tr>
                <td><a href="http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/">TFL Departure Boards</a></td>
            </tr>
        </table>
        <p style="font-size:7">For educational purposes only; this site is in no way connected to TFL.
            <a href="mailto:tube@charleskubicek.com">contact</a></p>
        <img src="http://code.google.com/appengine/images/appengine-silver-120x30.gif"
             alt="Powered by Google App Engine"/>
    </div>
</div>
<div id="content" style="height: 100%;">
</div>

<script type="text/javascript">

function state(branches) {
    this.trainsOnMap = {}
    this.trainsOnMapToggle = {}

    for (var i = 0; i < branches.length; i++) {
        this.trainsOnMap[branches[i]] = []
    }
}

function infoViewerState(branches) {
    this.branchesWaitingFor = {}

    for (var i = 0; i < branches.length; i++) {
        this.branchesWaitingFor[branches[i]] = false
    }
}

var imagesFolder =
<%=getServletConfig().getServletContext().getInitParameter("com.web.imagesFolder")%>
var appContext =
<%=getServletConfig().getServletContext().getInitParameter("com.web.appContext")%>
var rootResourceUrl = appContext+"/rest/"

var myInfoViewerState = null
var myState = null
var map = null

var directonImageDict = {};
//directonImageDict["Southbound"] = imagesFolder + "/down4.png";
//directonImageDict["Northbound"] = imagesFolder + "/up4.png";
//directonImageDict["Westbound"] = imagesFolder + "/left5.png";
//directonImageDict["Eastbound"] = imagesFolder + "/right5.png";

directonImageDict["S"] = imagesFolder + "/down4.png";
directonImageDict["N"] = imagesFolder + "/up4.png";
directonImageDict["W"] = imagesFolder + "/left5.png";
directonImageDict["E"] = imagesFolder + "/right5.png";
directonImageDict["I"] = imagesFolder + "/left5.png";
directonImageDict["O"] = imagesFolder + "/right5.png";

lineColourDict = {}
lineColourDict["northern"] = '#000000'
lineColourDict["victoria"] = '#009FE0'
lineColourDict["jubilee"] = '#8F989E'
lineColourDict["bakerloo"] = '#AE6118'
lineColourDict["metropolitan"] = '#893267'
lineColourDict["central"] = '#ff0000'
lineColourDict["piccadilly"] = '#0000c8'

var useLocalServerData = getURLParam("local")

function loadMap() {
    if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("content"));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
        map.setCenter(new GLatLng(51.5232, -0.1836), 12);

        GDownloadUrl(rootResourceUrl, function(data, responseCode) {
            var linesObj = eval('(' + data + ')');
            var linesToGet = [];

            linesObj.lines.linesArray.each(function(line) {
                linesToGet.push(line.name)
            })

            myInfoViewerState = new infoViewerState(linesToGet)
            myState = new state(linesToGet)

            linesToGet.each(function(line, index) {
                drawStations(line)
                loadTrains(line)

                /**
                 * Introduce a delay so when lines are reloaded it's clear to see
                 * what has changed
                 */
                startPolling(((5 * (index + 1)) * 1000), line)
            })
        })
    }
}

function startPolling(waitTime, line) {
    setTimeout(function() {
        loadTrains(line)
        reloadBranchAfterTimeout(line)
        return;
    }, waitTime);
}

function reloadBranchAfterTimeout(line) {
    setTimeout(function() {
        loadTrains(line)
        reloadBranchAfterTimeout(line)
        return;
    }, (60 * 1000));
}

/**
 * TODO optimize by downloading muliple/all lines
 * @param line
 */
function drawStations(line) {
    var url = appContext + "/static/stations/" + line +".json";
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
        removeBranchPointsFromMap(line)
        var pointsObj = eval('(' + data + ')');
        var trainMarkers = []

        //for (var i = 0; i < pointsObj.p.a.length; i++) {
        pointsObj.p.a.each(function(pointObj) {
            //pointObj = pointsObj.p.a[i]
            var point = new GLatLng(pointObj.t, pointObj.g);

            var marker = new makeTrainMarker(point, pointObj.i, pointObj.d, "false");
            trainMarkers.push(marker)
        })

        myState.trainsOnMap[line] = trainMarkers
        myState.trainsOnMapToggle[line] = true
        pauseBeforeAddingTrainsBackToMap(line)
    })
}

function pauseBeforeAddingTrainsBackToMap(line) {
    setTimeout(function() {
        addBranchPointsToMap(line)
        removeBranchWaitingFor(line)
    }, 1000);
}

function makeBranchesWaitingString() {
    var st = "";
    for (var i in myInfoViewerState.branchesWaitingFor) {
        if (myInfoViewerState.branchesWaitingFor[i]) {
            st += " " + i + "<br/>\n";
        }
    }

    if (st == "")return "";
    else return "getting data for:<br/>\n" + st;
}

// would be synchronized!
function addBranchWaitingFor(line) {
    myInfoViewerState.branchesWaitingFor[line] = true;
    updateInfoBox();
}

function removeBranchWaitingFor(line) {
    myInfoViewerState.branchesWaitingFor[line] = false;
    updateInfoBox();
}

function updateInfoBox() {
    var infoString = makeBranchesWaitingString();
    if (infoString.length > 0) {
        document.getElementById('liveInfo').innerHTML = infoString;
        $('liveInfo').appear();
    } else {
        $('liveInfo').hide();
    }
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

//* gets the XMLHttpRequest browser neutrally
function getHTTPObject() {
    if (typeof XMLHttpRequest != 'undefined') {
        return new XMLHttpRequest();
    }
    try {
        return new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
        try {
            return new ActiveXObject("Microsoft.XMLHTTP");
        } catch (e) {
        }
    }
    return false;
}

function makeStationMarker(point, stationObj, line, icon) {
    var marker = new GMarker(point, icon)

    GEvent.addListener(marker, "click", function() {
        marker.openInfoWindowHtml(stationObj.name + "<br/><p><a href='http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=" + line + "&StationCode=" + stationObj.code + "'>Go to live departure board</a></p>");
    });

    return marker
}


function makeTrainMarker(point, text, direction, multiple) {
    var marker = new GMarker(point, makeTrainIcon(direction, multiple));

    GEvent.addListener(marker, "click", function() {
        marker.openInfoWindowHtml(text);
    });
    return marker;
}

function makeTrainIcon(direction, multiple) {
    var icon = new GIcon();
    icon.image = directonImageDict[direction];
    icon.iconSize = new GSize(20, 34);
    icon.shadow = "";
    //if (multiple == "true")
    //  icon.iconAnchor = new GPoint(5, 34);
    //else
    icon.iconAnchor = new GPoint(10, 34);

    icon.infoWindowAnchor = new GPoint(6, 10);

    return icon;
}

function addBranchPointsToMap(key) {
    myState.trainsOnMap[key].each(function(item, index) {
        map.addOverlay(item);
    });
}

function removeBranchPointsFromMap(key) {
    myState.trainsOnMap[key].each(function(item, index) {
        map.removeOverlay(item);
    });
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
