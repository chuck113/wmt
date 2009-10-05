<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" style="height:100%;margin:0">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
  <title>WheresMyTube.com</title>
  <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAANyf_1x1i_h8KT1GEqKZvxRFoOGN9_H1GR_I2S--_TGFnQAVVhSfY7Fai1dAf6J5t_NspbQmL898fg" type="text/javascript"></script>
  <!--<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=getServletConfig().getServletContext().getInitParameter("GoogleMapsKey") %>" type="text/javascript"></script> -->
  <script src="javascripts/prototype.js" type="text/javascript"></script>
  <script src="javascripts/scriptaculous.js" type="text/javascript"></script>
  <style type="text/css" media="all">
    body {
      font: 80% arial, helvetica, sans-serif;
      margin: 0;
    }

    #liveInfo{
       font: 110% arial, helvetica, sans-serif;
       color:white
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
<body onunload="GUnload()" onload="loadMap()" style="height:100%;margin:0">

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

    <br/>
    <div id="liveInfo" style="display:none; width:240px; height:60px; background:#00CC00; border:1px solid #333;">loading trains..</div>
</div>
<div id="content" style="height: 100%;">
</div>

<script type="text/javascript">
//<![CDATA[
//keeps a map of arrays, where each entry is the name of a line mapped to an array of markers that has been
// overlayed on the GMap

var branchesToGet=["victoria", "jubilee"]

function state() {
  this.trainsOnMap = {}
  this.trainsOnMapToggle = {}

  for (var i = 0; i < branchesToGet.length; i++) {
    this.trainsOnMap[branchesToGet[i]] = []
  }
}

//appContext = "/wmt"
appContext = ""

var myState = new state()
var map = null

var directonImageDict = {};
directonImageDict["Southbound"] = appContext+"/images/down4.png";
directonImageDict["Northbound"] = appContext+"/images/up4.png";
directonImageDict["Westbound"] = appContext+"/images/left5.png";
directonImageDict["Eastbound"] = appContext+"/images/right5.png";

lineColourDict = {}
lineColourDict["northern"] = '#000000'
lineColourDict["victoria"] = '#009FE0'
lineColourDict["jubilee"] = '#8F989E'
lineColourDict["bakerloo"] = '#AE6118'
lineColourDict["metropolitan"] = '#893267'

var useLocalServerData = getURLParam("local")

function infoViewerState(){
    this.branchesWaitingFor={}
    this.queueSize = 0;

    this.branchesWaitingFor["victoria"]=false
    this.branchesWaitingFor["jubilee"]=false
}

var myInfoViewerState = new infoViewerState()

function makeBranchesWaitingString(){
   var st = "";
   for (var i in myInfoViewerState.branchesWaitingFor){
       if(myInfoViewerState.branchesWaitingFor[i] == true){
           st += " "+i +"<br/>\n";
       }
   }

   if(st == "")return st;
   else return "getting data for:<br/>\n"+st; 
}

// would be synchronized!
function addBranchWaitingFor(branch){
   myInfoViewerState.branchesWaitingFor[branch] = true;
    document.getElementById('liveInfo').innerHTML = makeBranchesWaitingString();
    if(myInfoViewerState.queueSize == 0){
        $('liveInfo').appear();
    }
    myInfoViewerState.queueSize++;
}

// would be synchronized!
function removeBranchWaitingFor(branch){
    myInfoViewerState.branchesWaitingFor[branch] = false;
    document.getElementById('liveInfo').innerHTML = makeBranchesWaitingString();
    if(myInfoViewerState.queueSize == 1){
        $('liveInfo').hide();
    }
    myInfoViewerState.queueSize--;
}

function stationIcon(){
      var icon = new GIcon();
      icon.image = appContext+"/images/station.png";
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
    var url = appContext+"/rest/stations/" + line
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
function loadTrains(branch) {
    var url = appContext+"/rest/branches/" + branch
    //url = test ? url + "?testMode=1" : url
    url = useLocalServerData ? url + "?local=true" : url
    addBranchWaitingFor(branch)

    GDownloadUrl(url, function(data, responseCode) {
        removeBranchPointsFromMap(branch)
        var pointsObj = eval('(' + data + ')');
        var trainMarkers = []

        for (var i = 0; i < pointsObj.points.pointsArray.length; i++) {
            pointObj = pointsObj.points.pointsArray[i]
            var point = new GLatLng(pointObj.lat, pointObj.lng);

            var marker = new createMarker(point, pointObj.description, pointObj.direction, "false");
            trainMarkers.push(marker)
            //map.addOverlay(marker)
        }

        myState.trainsOnMap[branch] = trainMarkers
        myState.trainsOnMapToggle[branch] = true        
        pauseBeforeAddingTrainsBackToMap(branch)        
    })

}

function pauseBeforeAddingTrainsBackToMap(branch){
    setTimeout(function() {
        addBranchPointsToMap(branch)
        removeBranchWaitingFor(branch)
     }, 1000);
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
  icon.image = appContext+"/images/station.png";
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
      //branch = 'victoria'
      for (var i=0; i<branchesToGet.length; i++){
        drawStations(branchesToGet[i])
        loadTrains(branchesToGet[i])

        /**
        * Get all trains to start with, then start polling every 30 secs for new trains,
        * but stagger the polls by 10 seconds each (assuming 2 branches)
        */
        startPolling(((10 * (i+1)) * 1000), branchesToGet[i])  
      }
  }
}

function startPolling(waitTime, branch){
     setTimeout(function() {
        loadTrains(branch)
        reloadBranchAfterTimeout(branch)
     }, waitTime);
}

function reloadBranchAfterTimeout(branch){
    setTimeout(function() {
        loadTrains(branch)
        reloadBranchAfterTimeout(branch)
        return
    }, (30 * 1000));
}

//function forEachTrainOnMap(key, ){
//
//}

function addBranchPointsToMap(key) {
  var markers = myState.trainsOnMap[key]
  for (var i = 0; i < markers.length; i++) {
    map.addOverlay(markers[i])
  }
}

function removeBranchPointsFromMap(key) {
  var markers = myState.trainsOnMap[key]
  for (var i = 0; i < markers.length; i++) {
    map.removeOverlay(markers[i])
  }
}

// taken from http://mattwhite.me/11tmr.nsf/D6Plinks/MWHE-695L9Z
function getURLParam(strParamName){
  var strReturn = "";
  var strHref = window.location.href;
  if ( strHref.indexOf("?") > -1 ){
    var strQueryString = strHref.substr(strHref.indexOf("?")).toLowerCase();
    var aQueryString = strQueryString.split("&");
    for ( var iParam = 0; iParam < aQueryString.length; iParam++ ){
      if (aQueryString[iParam].indexOf(strParamName.toLowerCase() + "=") > -1 ){
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
