//function to launch eta pop up board
function fnPop(LineCode,StationCode,PlatformCode,PlatformName){
	window.open('departure-board.asp?LineCode=' + LineCode + '&StationCode=' + StationCode + '&PlatformCode=' + PlatformCode + '&PlatformName=' + PlatformName,'ETA','width=500,height=200');
}
//function to close the eta pop up board
function fnCloseWindow(){
	window.close();
}
