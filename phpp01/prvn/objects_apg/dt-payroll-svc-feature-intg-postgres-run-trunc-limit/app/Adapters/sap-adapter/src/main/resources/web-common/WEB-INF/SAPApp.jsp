<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Redirect to SAP Desktop Application</title>

    <script language="JavaScript" type="text/javascript">
        var customURLScheme = '<%=request.getAttribute("customURLScheme")%>';

        window.onload = function() {
            window.location.replace(customURLScheme);
        };
    </script>
</head>

<body>
</body>