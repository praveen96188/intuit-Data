<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <title>Payroll Services Platform</title>
    <style id="antiClickjack">body{display:none !important;}</style>
    <script type="text/javascript" src="swfobject.js"></script>
    <script type="text/javascript">
        swfobject.registerObject("myId", "10.0.0", "expressInstall.swf");
    </script>

    <script type="text/javascript">
        if (self === top) {
            var antiClickjack = document.getElementById("antiClickjack");
            antiClickjack.parentNode.removeChild(antiClickjack);
        } else {
            top.location = self.location;
        }
    </script>

    <script src="swfaddress/swfaddress.js" language="javascript"></script>
    <style>
        body { margin: 0px; overflow:hidden }
    </style>

    <script language="JavaScript" type="text/javascript">
        function showPrintWindow(query){
            try{
                var newWindow = window.open("Print" + query, "_blank");
                if(!newWindow){
                    alert("Error launching print browser. Please disable your pop-up blocker.");
                }
            } catch (e) {
                alert("Error launching print browser");
            }
        }

        function logout(){
            var url="https://"+window.location.host.toString().concat("/SAP/ssoLogout");
            window.location.replace(url);
            window.location.reload();
            window.location.replace(url);
        }

    </script>
</head>

<body scroll="no">
<object id="sap" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%">
    <param name="movie" value="SAPApp.swf" />
    <param name="flashvars" value="corpId=<%=request.getAttribute("corpId")%>&authToken=<%=request.getAttribute("authToken")%>&authId=<%=request.getAttribute("authId")%>&ticket=<%=request.getAttribute("ticket")%>&realmId=<%=request.getAttribute("realmId")%>&emailAddress=<%=request.getAttribute("emailAddress")%>">
    <!--[if !IE]>-->
    <object type="application/x-shockwave-flash" data="SAPApp.swf" width="100%" height="100%">
        <param name="flashvars" value="corpId=<%=request.getAttribute("corpId")%>&authToken=<%=request.getAttribute("authToken")%>&authId=<%=request.getAttribute("authId")%>&ticket=<%=request.getAttribute("ticket")%>&realmId=<%=request.getAttribute("realmId")%>&emailAddress=<%=request.getAttribute("emailAddress")%>">
        <!--<![endif]-->
        <div>
            <h1>Adobe Flash Is Required</h1>
            <p><a href="https://www.adobe.com/go/getflashplayer"><img src="https://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash player" /></a></p>
        </div>
        <!--[if !IE]>-->
    </object>
    <!--<![endif]-->
</object>
</body>