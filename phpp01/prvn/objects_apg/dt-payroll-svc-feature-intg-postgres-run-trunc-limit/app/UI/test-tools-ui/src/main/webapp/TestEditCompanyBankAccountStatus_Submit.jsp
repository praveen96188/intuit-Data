<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
  	String companyBankAccountGseq = request.getParameter("compBankAccountGseq");
	String newCompanyBankAccountStatus = request.getParameter("newCompanyBankAccountStatus");
    
    if (newCompanyBankAccountStatus == null || newCompanyBankAccountStatus.equals("") ) {
		errorMsg = "New Company Bank Account Status is null or empty.";
	}

    try{
        TestAdapterAPI.getCompanyWS().updateBankAccountStatus(companyBankAccountGseq, newCompanyBankAccountStatus);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }

    request.setAttribute("errorMsg", errorMsg);
%>
<html>
  <head>
    <title>Edit Company Bank Account Status Submit</title>
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
          <c:choose>
          <c:when test="${errorMsg != null}">
            <h3>Company Bank Account Status Not Updated</h3>
            <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
          </c:when>
          <c:otherwise>
            <h3>Company Bank Account Status updated</h3>
          </c:otherwise>
          </c:choose>
      </td>
    </tr>
  </table>

  </body>
</html>