images gnerated at: http://gmaps-utility-library.googlecode.com/svn/trunk/mapiconmaker/1.1/examples/markericonoptions-wizard.html

 source is here:
 
 <html> 
<head> 
<title>MarkerIconOptions Wizard</title> 
<link rel="stylesheet" type="text/css" href="http://code.google.com/css/dev_docs.css"> 
 <style type="text/css"> 
  table {
    margin: 0px;
  }
  td {
    margin: 1px;
    padding: 0px;
  }
  .colorDiv {
    margin: 10px;
    font-weight: bold;
  }
  .inputDiv {
    margin: 10px;
    font-weight: bold;
  }
  .colorSwatch {
    border: 1px solid #000000; 
    font-family: Verdana; 
    font-size: 10px; 
    text-decoration: none;
  }
  pre {
    margin-left: 10px;
  }
 </style> 
 <script type="text/javascript"  src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAjU0EJWnWPMv7oQ-jjS7dYxQGj0PqsCtxKvarsoS-iqLdqZSKfxRdmoPmGl7Y9335WLC36wIGYa6o5Q"> 
 </script> 
 <script src="../src/mapiconmaker.js" type="text/javascript"></script> 
 <script src="colorselector/fooble.js" type="text/javascript"></script> 
<script type="text/javascript"> 
var iconOptions = {
  width: "32",
  height: "32",
  primaryColor: "ff0000",
  cornerColor: "ffffff",
  strokeColor: "000000"
};
var map = null;
function changeColor(colorHex) {
  iconOptions.primaryColor = colorHex;
  updateImage();
}
 
function changeCornerColor(colorHex) {
  iconOptions.cornerColor = colorHex;
  updateImage();
}
 
function load() {
  if (GBrowserIsCompatible()) {
    updateVisibleOptions();
    map = new GMap2(document.getElementById("map"));
    map.addControl(new GSmallMapControl());
    map.addControl(new GMapTypeControl());
    map.setCenter(new GLatLng(37.4419, -122.1419), 13);
    updateImage();
  }
}
 
function updateVisibleOptions() {
  var iconType = document.getElementById("iconTypeSelect").value;
  if (iconType == "marker") {
    showDiv("primaryColorDiv");
    showDiv("cornerColorDiv");
    showDiv("strokeColorDiv");
    hideDiv("labelColorDiv");
    showDiv("iconSizeDiv");
    hideDiv("labelDiv");
    hideDiv("labelSizeDiv");
    hideDiv("starPrimaryColorDiv");
    hideDiv("starStrokeColorDiv");
    hideDiv("flatIconShapeDiv");
    hideDiv("addStarDiv");
  } else if (iconType == "labeledmarker") {
    showDiv("primaryColorDiv");
    hideDiv("cornerColorDiv");
    showDiv("strokeColorDiv");
    showDiv("labelColorDiv");
    hideDiv("iconSizeDiv");
    showDiv("labelDiv");
    hideDiv("labelSizeDiv");
    showDiv("starPrimaryColorDiv");
    showDiv("starStrokeColorDiv");
    hideDiv("flatIconShapeDiv");
    showDiv("addStarDiv");
  } else if (iconType == "flat") {
    showDiv("primaryColorDiv");
    hideDiv("cornerColorDiv");
    hideDiv("strokeColorDiv");
    showDiv("labelColorDiv");
    showDiv("iconSizeDiv");
    showDiv("labelSizeDiv");
    showDiv("labelDiv");
    hideDiv("starPrimaryColorDiv");
    hideDiv("starStrokeColorDiv");
    showDiv("flatIconShapeDiv");
    hideDiv("addStarDiv");
  }
}
 
function showDiv(divId) {
  document.getElementById(divId).style.display = "block";
}
 
function hideDiv(divId) {
  document.getElementById(divId).style.display = "none";
}
 
function createOptionsCode(optionNames) {
  var codeString = "";
  for (var i = 0; i < optionNames.length; i++) {
    var optionName = optionNames[i];
    var chars = "\"";
    if (optionName == "width" || optionName == "height" || optionName == "labelSize" || optionName == "addStar") chars = "";
    codeString += "iconOptions." + optionName + " = " + chars + iconOptions[optionName] + chars + ";\n";
  }
  return codeString;
}
 
function updateImage() {
  map.clearOverlays();
  iconOptions.width = document.getElementById("iconWidth").value;
  iconOptions.height = document.getElementById("iconHeight").value;
  iconOptions.primaryColor = document.getElementById("primaryColorPickerField").value;
  iconOptions.cornerColor = document.getElementById("cornerColorPickerField").value;
  iconOptions.strokeColor = document.getElementById("strokeColorPickerField").value;
  iconOptions.starPrimaryColor = document.getElementById("starPrimaryColorPickerField").value;
  iconOptions.starStrokeColor = document.getElementById("starStrokeColorPickerField").value;
  iconOptions.label = document.getElementById("label").value;
  iconOptions.labelColor = document.getElementById("labelColorPickerField").value;
  iconOptions.labelSize = document.getElementById("labelSize").value;
  iconOptions.addStar = document.getElementById("iconAddStar").checked;
  iconOptions.shape = document.getElementById("flatIconShapeSelect").value;
 
  var iconType = document.getElementById("iconTypeSelect").value;
  if (iconType == "marker") {
    var markerOptionNames = ["width", "height", "primaryColor", "cornerColor", "strokeColor"];
    var icon = MapIconMaker.createMarkerIcon(iconOptions);
    var codeString = "var iconOptions = {};\n";     
    codeString += createOptionsCode(markerOptionNames);
    codeString += "var icon = MapIconMaker.createMarkerIcon(iconOptions);";
    document.getElementById("iconCode").innerHTML = codeString;
  } else if (iconType == "labeledmarker") {
    var markerOptionNames = ["primaryColor", "strokeColor", "label", "labelColor", "addStar", "starPrimaryColor", "starStrokeColor"];
    var icon = MapIconMaker.createLabeledMarkerIcon(iconOptions);
    var codeString = "var iconOptions = {};\n";     
    codeString += createOptionsCode(markerOptionNames);
    codeString += "var icon = MapIconMaker.createLabeledMarkerIcon(iconOptions);";
    document.getElementById("iconCode").innerHTML = codeString;
  } else if (iconType == "flat") {
    var markerOptionNames = ["width", "height", "primaryColor", "label", "labelSize", "labelColor", "shape"];
    var icon = MapIconMaker.createFlatIcon(iconOptions);
    var codeString = "var iconOptions = {};\n";     
    codeString += createOptionsCode(markerOptionNames);
    codeString += "var icon = MapIconMaker.createFlatIcon(iconOptions);";
    document.getElementById("iconCode").innerHTML = codeString;
  }
 
  // Make an image from the foreground of the marker
  document.getElementById("marker").innerHTML = "";
  var img = document.createElement("img");
  img.setAttribute("src", icon.image);
  document.getElementById("marker").appendChild(img);
 
  // Add 10 markers to the map at random locations
  var bounds = map.getBounds();
  var southWest = bounds.getSouthWest();
  var northEast = bounds.getNorthEast();
  var lngSpan = northEast.lng() - southWest.lng();
  var latSpan = northEast.lat() - southWest.lat();
  for (var i = 0; i < 10; i++) {
    var point = new GLatLng(southWest.lat() + latSpan * Math.random(),
                            southWest.lng() + lngSpan * Math.random());
    map.addOverlay(createMarker(point, icon));
  }
}
 
function createMarker(point, icon) {
  var marker = new GMarker(point, {icon: icon, draggable:true});
  GEvent.addListener(marker, "click", function() {
    marker.openInfoWindowHtml("Test");
  });
  return marker;
}
</script> 
</head> 
<body style="font-family: Arial, sans serif" onload="load()"> 
 
<h2>MarkerIconOptions Wizard</h2> 
 
<h3><img src="http://www.google.com/uds/solutions/wizards/img/1b.gif" style="vertical-align: middle;"> Specify size and color options</h3> 
 
<div class="inputDiv"> 
 Select type of icon to create: 
 <select id="iconTypeSelect" onchange="updateVisibleOptions(); updateImage();"> 
  <option value="marker">Marker</option> 
  <option value="labeledmarker">Labeled Marker</option> 
  <option value="flat">Flat</option> 
 </select> 
</div> 
 
<div style="clear:both;"></div> 
 
<div class="inputDiv" id="iconSizeDiv"> 
 Width: <input type="text" size="3" id="iconWidth" value="32"/> 
 &nbsp;  &nbsp;
 Height: <input type="text" size="3" id="iconHeight" value="32"/> 
</div> 
 
<br/> 
 
<div class="colorDiv" id="primaryColorDiv"> 
 Primary color:
  <a style="background-color:#FF0000" href="javascript:pickColor('primaryColorPicker');" id="primaryColorPicker" class="colorSwatch">&nbsp;&nbsp;&nbsp;</a> 
  <input id="primaryColorPickerField" size="7" onChange="relateColor('primaryColorPicker', this.value);" name="primaryColor" value="#FF0000"> 
</div> 
 
<div class="colorDiv" id="strokeColorDiv"> 
 Stroke color:
  <a style="background-color:#000000" href="javascript:pickColor('strokeColorPicker');" id="strokeColorPicker" class="colorSwatch">&nbsp;&nbsp;&nbsp;</a> 
  <input id="strokeColorPickerField" size="7" onChange="relateColor('strokeColorPicker', this.value);" name="strokeColor" value="#000000"> 
</div> 
 
<div class="colorDiv" id="cornerColorDiv"> 
 Corner color:
  <a style="background-color:#ffffff" href="javascript:pickColor('cornerColorPicker');" id="cornerColorPicker" class="colorSwatch">&nbsp;&nbsp;&nbsp;</a> 
  <input id="cornerColorPickerField" size="7" onChange="relateColor('cornerColorPicker', this.value);" name="cornerColor" value="#FFFFFF"> 
</div> 
 
<br/> 
 
<div class="inputDiv" id="labelDiv"> 
 Label: <input type="text" size="3" id="label" value="a"/> 
</div> 
 
<div class="inputDiv" id="labelSizeDiv"> 
 Label size: <input type="text" size="3" id="labelSize" value="0"/> 
</div> 
 
<div class="inputDiv" id="labelColorDiv"> 
 Label color: 
  <a style="background-color:#000000" href="javascript:pickColor('labelColorPicker');" id="labelColorPicker" class="colorSwatch">&nbsp;&nbsp;&nbsp;</a> 
  <input id="labelColorPickerField" size="7" onChange="relateColor('labelColorPicker', this.value);" name="labelColor" value="#000000"> 
</div> 
 
<br/> 
 
<div class="inputDiv" id="flatIconShapeDiv"> 
 Shape: 
 &nbsp; &nbsp;
 <select id="flatIconShapeSelect" > 
  <option value="circle">Circle</option> 
  <option value="roundrect">Rounded Rectangle</option> 
 </select> 
</div> 
 
<br/> 
 
<div class="inputDiv" id="addStarDiv"> 
 Add star? <input type="checkbox" id="iconAddStar"/> 
</div> 
 
<div class="colorDiv" id="starPrimaryColorDiv"> 
 Star primary color:
  <a style="background-color:#FFFF00" href="javascript:pickColor('starPrimaryColorPicker');" id="starPrimaryColorPicker" class="colorSwatch">&nbsp;&nbsp;&nbsp;</a> 
  <input id="starPrimaryColorPickerField" size="7" onChange="relateColor('starPrimaryColorPicker', this.value);" name="starPrimaryColor" value="#FFFF00"> 
</div> 
 
<div class="colorDiv" id="starStrokeColorDiv"> 
 Star stroke color:
  <a style="background-color:#0000FF" href="javascript:pickColor('starStrokeColorPicker');" id="starStrokeColorPicker" class="colorSwatch">&nbsp;&nbsp;&nbsp;</a> 
  <input id="starStrokeColorPickerField" size="7" onChange="relateColor('starStrokeColorPicker', this.value);" name="starStrokeColor" value="#0000FF"> 
</div> 
 
<div style="clear:both;"></div> 
 
<div style="margin-left:10px"> 
 <input type="button" onclick="updateImage()" value="Update & Preview Marker"/> 
</div> 
 
<div id="marker" style="margin-left: 10px; height: 70px"></div> 
 
<div id="map" style="margin-left: 10px; width: 500px; height: 300px"></div> 
 
<h3><img src="http://www.google.com/uds/solutions/wizards/img/2b.gif" style="vertical-align: middle;"> Get the Code</h3> 
<pre class="code" style="width: 700px; overflow-x: scroll;" id="iconCode"> 
</pre> 
 
<br/> 
<em> 
All the <a target="_blank" href="http://www.flooble.com/scripts/colorpicker.php">color pickers</a> on this page are by <a target="_blank" href="http://www.flooble.com"><b>flooble</b></a></center> 
</em> 
 
</body> 
</html> 