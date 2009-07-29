<%--
  Created by IntelliJ IDEA.
  User: Charles
  Date: 24-Aug-2008
  Time: 14:18:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" style="height:100%;margin:0">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
  <title>RealtimeUndergound.co.uk</title>
  <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=getServletConfig().getServletContext().getInitParameter("GoogleMapsKey") %>"
          type="text/javascript"></script>
  <style type="text/css" media="all">
    body {
      font: 80% arial, helvetica, sans-serif;
      margin: 0;
    }

    h1, h2 {
      margin: 5;
    }

    #navigation{
        float: right;
        width: 300px;
        margin: -10px;
    }

    #content {
        margin-right: 300px;
    }

    #footer {
        clear: both;
    }


  </style>
</head>
<!--<body onunload="GUnload()" onload="loadTrains()" style="height:100%;margin:0">-->
<!--<body onunload="GUnload()" onload="loadMap()" style="height:100%;margin:0">-->
<body onunload="GUnload()" onload="loadMap()" style="height:100%;margin:0">
<!--<hr NOSHADE size="1" COLOR="blue"> -->
<div id="navigation"  style="width: 300px; height: 100%;">
  <!-- <a href="/tubemap/show_map?branch=test">Mordern to Clapham Common</a><br>
  <a href="/tubemap/show_map?branch=bankBarnet">Bank and high barnet threaded</a><br>
  <a href="/tubemap/show_map?branch=bank">Bank only</a><br>
  <a href="/tubemap/show_map?cache=true">cache</a><br>
  <a href="/tubemap/show_map?none=true">empty</a><br>
  <a href="javascript: removeAllPoints()">remove all</a><br>
  <a href="javascript: addAllPoints()">add all</a><br>  -->
  <h1>Where's My Tube?</h1>
    <p>A realtime view of the London Underground.</p>

  <a href="javascript: loadTrains('test', true)">test points</a><br>
    <a href="javascript: loadTrains('victoria', true)">Victoria line - mock</a><br>
    <a href="javascript: loadTrains('victoria', false)">Victoria line</a><br>
    <a href="javascript: loadTrains('jubilee', false)">jubile line</a><br>
    <a href="javascript: loadTrains('jubilee', true)">jubile line - mock</a><br>
  last parse at
  <br>
  <table border="1">
    <tr>
      <td>line</td>
      <td>on/off</td>
    </tr>
    <tr>
      <td>Northern</td>
      <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('northern')"/></td>
    </tr>
    <tr>
      <td>Jubilee</td>
      <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('jubilee')"/></td>
    </tr>
    <tr>
      <td>Victoria</td>
      <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('victoria')"/></td>
    </tr>
    <tr>
      <td>Bakerloo</td>
      <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('bakerloo')"/></td>
    </tr>
      <tr>
      <td>Test</td>
      <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('test')"/></td>
    </tr>
    <!--<tr>
          <td>All</td>
          <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('')"/></td>
        </tr>-->

  </table>
</div>
<div id="content" style="height: 100%;">
</div>

<script type="text/javascript">
//<![CDATA[
//keeps a map of arrays, where each entry is the name of a line mapped to an array of markers that has been
// overlayed on the GMap

function state() {
  this.trainsOnMap = {}
  this.trainsOnMapToggle = {}
}

var myState = new state()
var map = null

var directonImageDict = {};
directonImageDict["Southbound"] = "/images/down4.png";
directonImageDict["Northbound"] = "/images/up4.png";
directonImageDict["Westbound"] = "/images/left5.png";
directonImageDict["Eastbound"] = "/images/right5.png";

lineColourDict = {}
lineColourDict["northern"] = '#000000'
lineColourDict["victoria"] = '#009FE0'
lineColourDict["jubilee"] = '#8F989E'
lineColourDict["bakerloo"] = '#AE6118'
lineColourDict["metropolitan"] = '#893267'


function stationIcon(){
      var icon = new GIcon();
      icon.image = "/images/station.png";
      icon.iconSize = new GSize(14, 14);
      icon.shadow = "";
      icon.iconAnchor = new GPoint(7, 7);
      icon.infoWindowAnchor = new GPoint(6, 10);

      return icon;
}

/**
 * TODO optimize by downloading muliple/all lines
 * @param line
 */
function drawStations(line) {
    var url = "/rest/stations/" + line
    var icon = stationIcon();


    GDownloadUrl(url, function(data, responseCode) {
        var stationsObj = eval('(' + data + ')');
        lines = [];
        for (var i = 0; i < stationsObj.stations.stationsArray.length; i++) {
            stationObj = stationsObj.stations.stationsArray[i];
            var point = new GLatLng(stationObj.lat, stationObj.lng);
            lines.push(new GLatLng(stationObj.lat, stationObj.lng));

            map.addOverlay(makeStationMarker(point, stationObj.name, icon));
        }

        var polyLine = new GPolyline(lines, lineColourDict[line], 4, 1)
        map.addOverlay(polyLine);
    })
}


//* uses googles download url to load json into map
function loadTrains(branch, test) {
    var url = "/rest/branches/" + branch
    url = test ? url + "?testMode=1" : url
    //url = replay ? url + "?replay=true" : url

    GDownloadUrl(url, function(data, responseCode) {
        var pointsObj = eval('(' + data + ')');
        var trainMarkers = []

        for (var i = 0; i < pointsObj.points.pointsArray.length; i++) {
            pointObj = pointsObj.points.pointsArray[i]
            var point = new GLatLng(pointObj.lat, pointObj.lng);

            var marker = new createMarker(point, pointObj.description, pointObj.direction, "false");
            trainMarkers.push(marker)
            map.addOverlay(marker)
        }

        myState.trainsOnMap[branch] = trainMarkers
        myState.trainsOnMapToggle[branch] = true
    })

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

function createTrainMarker(direction, multiple){
  var icon = new GIcon();
  icon.image = directonImageDict[direction];
  icon.iconSize = new GSize(20, 34);
  icon.shadow = "";
  if (multiple == "true")
    icon.iconAnchor = new GPoint(5, 34);
  else
    icon.iconAnchor = new GPoint(10, 34);

  icon.infoWindowAnchor = new GPoint(6, 10);

  return icon;
}


function stationIcon() {
  var icon = new GIcon();
  icon.image = "/images/station.png";
  icon.iconSize = new GSize(14, 14);
  icon.shadow = "";
  icon.iconAnchor = new GPoint(7, 7);
  icon.infoWindowAnchor = new GPoint(6, 10);

  return icon;
}

function makeStationMarker(point, text, icon) {
  var marker = new GMarker(point, icon)

  GEvent.addListener(marker, "click", function() {
    marker.openInfoWindowHtml(text);
  });

  return marker
}

function createMarker(point, text, direction, multiple) {
  var marker = new GMarker(point, createTrainMarker(direction, multiple));

  GEvent.addListener(marker, "click", function() {
    marker.openInfoWindowHtml(text);
  });
  return marker;
}



function loadMap() {
  if (GBrowserIsCompatible()) {
      map = new GMap2(document.getElementById("content"));
      map.addControl(new GLargeMapControl());
      map.addControl(new GMapTypeControl());

      map.setCenter(new GLatLng(51.5183, -0.1246), 12);
      branch = 'victoria'
      drawStations(branch)
      drawStations('jubilee')
      loadTrains(branch, true)
  }
  
}
function toggleLineDisplay(line) {
  if (line == '') {

  }
  if (myState.trainsOnMapToggle[line]) {
    removeAllPoints(line)
    myState.trainsOnMapToggle[line] = false
  } else {
    addAllPoints(line)
    myState.trainsOnMapToggle[line] = true
  }
}

function addAllPoints(key) {
  var markers = myState.trainsOnMap[key]

  for (m in markers) {
    map.addOverlay(markers[m])
  }
}

function removeAllPoints(key) {
  var markers = myState.trainsOnMap[key]
  for (m in markers) {
    map.removeOverlay(markers[m])
  }
}

//]]>
</script>
</body>
</html>
