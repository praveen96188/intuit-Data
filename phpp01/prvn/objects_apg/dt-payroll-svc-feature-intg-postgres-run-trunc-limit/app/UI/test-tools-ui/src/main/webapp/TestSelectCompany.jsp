<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="Cookies.jsp" %>

<%
    String selectedCompanyGseq = request.getParameter("companyGseq");
    if (selectedCompanyGseq != null) {
        setCompanyGseq(selectedCompanyGseq, response);
        response.sendRedirect("TestSelectCompany.jsp");
    }
%>

<html>
  <head>
    <title>Select company</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>

    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
           <div>
            <h3>Company Selected</h3>
           </div>
        </td>
      </tr>
    </table>

  </body>
</html>