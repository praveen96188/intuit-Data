function getTodaysDate() {
	var date = new Date();
	var month = date.getMonth()+1;
	if (month.toString().length == 1) month = "0" + month;
	var day = date.getDate();
	if (day.toString().length == 1) day = "0" + day;
	return today = month + "/" +  day + "/" + date.getFullYear();
}

function filterInvalidChars(regExpStr)
{
	var regExpr = new RegExp(regExpStr);
	var ch = String.fromCharCode(window.event.keyCode);

	if (!regExpr.test(ch))
	{
		window.event.cancelBubble = true;
		window.event.returnValue = false;
		window.event.keyCode = 0;
	}
}

function toUpperCase()
{
	var ch = String.fromCharCode(window.event.keyCode);
	upCh = ch.toUpperCase();

	if (ch != upCh)
	{
		window.event.keyCode = upCh.charCodeAt(0);
	}
}
