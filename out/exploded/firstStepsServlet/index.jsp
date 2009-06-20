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
      margin: 0;
    }

    #header {

    }

    #navigation {
      position: absolute;
      left: 0;
      top: 85px;
      width: 150px;
    }

    #content {
      margin-left: 200px;
    }


  </style>
</head>
<!--<body onunload="GUnload()" onload="loadTrains()" style="height:100%;margin:0">-->
<!--<body onunload="GUnload()" onload="loadMap()" style="height:100%;margin:0">-->
<body onunload="GUnload()" onload="loadMap()" style="height:100%;margin:0">
<div id="header">
  <!--<H1 align="center"> Realtime Underground </H1><br>
     View the position of trains on the London undergournd in real time-->
  <img src="/images/banner.png" align="center">
</div>
<!--<hr NOSHADE size="1" COLOR="blue"> -->
<div id="navigation">
  <!-- <a href="/tubemap/show_map?branch=test">Mordern to Clapham Common</a><br>
  <a href="/tubemap/show_map?branch=bankBarnet">Bank and high barnet threaded</a><br>
  <a href="/tubemap/show_map?branch=bank">Bank only</a><br>
  <a href="/tubemap/show_map?cache=true">cache</a><br>
  <a href="/tubemap/show_map?none=true">empty</a><br>
  <a href="javascript: removeAllPoints()">remove all</a><br>
  <a href="javascript: addAllPoints()">add all</a><br>
  <a href="javascript: loadTrains()">test via ajax</a><br>     -->
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
    <!--<tr>
          <td>All</td>
          <td><input type="CHECKBOX" checked="true" onclick="toggleLineDisplay('')"/></td>
        </tr>-->

  </table>
</div>
<div id="content" style="width: 80%; height: 92%;">
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


//* uses googles download url to load xml into map
function loadTrains() {

//  GDownloadUrl("/points/current?branch=all", function(data, responseCode) {
//    var xml = GXml.parse(data);
//    var trains = xml.documentElement.getElementsByTagName("train");
//    var trainMarkers = []
//    for (var i = 0; i < trains.length; i++) {
//      var point = new GLatLng(parseFloat(trains[i].getAttribute("y")),
//              parseFloat(trains[i].getAttribute("x")));
//      var marker = new createMarker(point, trains[i].getAttribute("name") + " (" + trains[i].getAttribute("dir") + ")", trains[i].getAttribute("dir"), trains[i].getAttribute("multi"));
//      trainMarkers.push(marker)
//      myState.trainsOnMap['northern'] = trainMarkers
//      //myState.trainsOnMapToggle['northern'] = true
//      map.addOverlay(marker)
//    }
//  });
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

function getTrainDataFromServer() {
  //if (!this.working) {
  var http = getHTTPObject()

  http.open("GET", "http://localhost:3000/points/current?cache=true", true);
  http.onreadystatechange = function() {
    if (http.readyState == 4) {
      alert("got some xml");
    }
  }
  http.send(null);
  //}
}


function upTrainMarker(multiple) {
  var icon = new GIcon();
  icon.image = "/images/up4.png";
  icon.iconSize = new GSize(20, 34);
  icon.shadow = "";
  if (multiple == "true")
    icon.iconAnchor = new GPoint(5, 34);
  else
    icon.iconAnchor = new GPoint(10, 34);

  icon.infoWindowAnchor = new GPoint(6, 10);

  return icon;
}

// the train marksers should have an icon anchor of 8, 20, but to avoid clashes we split them up.
function downTrainMarker(multiple) {
  var icon = new GIcon();
  icon.image = "/images/down4.png";
  icon.iconSize = new GSize(20, 34);
  icon.shadow = "";

  if (multiple == "true")
    icon.iconAnchor = new GPoint(15, 34);
  else
    icon.iconAnchor = new GPoint(10, 34);

  icon.infoWindowAnchor = new GPoint(6, 10);

  return icon;
}

function leftTrainMarker(multiple) {
  var icon = new GIcon();
  icon.image = "/images/left5.png";
  icon.iconSize = new GSize(20, 34);
  icon.shadow = "";

  if (multiple)
    icon.iconAnchor = new GPoint(5, 34);
  else
    icon.iconAnchor = new GPoint(10, 34);

  icon.infoWindowAnchor = new GPoint(6, 10);

  return icon;
}

function rightTrainMarker(multiple) {
  var icon = new GIcon();
  icon.image = "/images/right5.png";
  icon.iconSize = new GSize(20, 34);
  icon.shadow = "";

  if (multiple)
    icon.iconAnchor = new GPoint(15, 34);
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
  var marker = null;

  if (direction == "Southbound") {
    marker = new GMarker(point, downTrainMarker(multiple));
  } else if (direction == "Northbound") {
    marker = new GMarker(point, upTrainMarker(multiple));
  } else if (direction == "Westbound") {
    marker = new GMarker(point, leftTrainMarker(multiple));
  } else { //if(direction == "Eastbound"){
    marker = new GMarker(point, rightTrainMarker(multiple));
  }

  GEvent.addListener(marker, "click", function() {
    marker.openInfoWindowHtml(text);
  });
  return marker;
}

function getColourForLine(line) {
  if (line == "northern") {
    return '#000000'
  } else if (line == "victoria") {
    return "#009FE0"
  } else if (line == "jubilee") {
    return "#8F989E"
  } else if (line == "bakerloo") {
    return "#AE6118"
  } else if (line == "metropolitan") {
    return "#893267"
  }

  return '#000000'
}

function loadMap() {
  if (GBrowserIsCompatible()) {
      map = new GMap2(document.getElementById("content"));
      map.addControl(new GLargeMapControl());
      map.addControl(new GMapTypeControl());

      //var trainIcon = upTrainMarker();
      //var stationIcon = stationIcon();

      map.setCenter(new GLatLng(51.5183, -0.1246), 12);
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
