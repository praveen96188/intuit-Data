<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">

<head>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>SAP error page</title>
    <style >
        body{
            margin: 0;
            padding: 0;
            font-family: 'Helvetica Neue', 'Helvetica', Verdana, 'sans serif';
            font-size: 16px;
            background-color: #eeeff1;}
    </style>
</head>


<body>

<div align="center">
    <h1 align="center"><b><font size="14">HTTP Error 401:Unauthorized Access!</font></b> </h1>
    <p>You have attempted to access a page for which you are not authorized.</p>
    <p>Please use the below link to return to login page.</p>
    <a href=<%request.getAttribute("ssoLoginUrl");%>>Click here</a>



</div>

</body>
</html>