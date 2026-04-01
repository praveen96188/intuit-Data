<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    String companyLimit = request.getParameter("companyLimit");

    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        if (companyLimit != null && companyLimit.length() > 0) {
            errorMsg = TestAdapterAPI.getCompanyWS().updateDDLimits(selectedCompany.getSourceSystemCD(),
                    selectedCompany.getSourceCompanyID(), companyLimit, null);
        } else {
            companyLimit = selectedCompany.getOverrideCompanyLimitAmount().toString();
        }
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("companyLimit", companyLimit);
    request.setAttribute("errorMsg", errorMsg);    
%>

<html>
  <head>
    <title>Edit Company Limit</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>
  <form action="TestEditCompanyLimits.jsp" method="POST">
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3 class="pageTitle">Edit Company Limit</h3>
          <br>
          DD Company Limit:
          &nbsp;&nbsp;
          <input type="text" style="text-align:right" name="companyLimit" maxlength="10" size="10"
                 value="<c:out value="${companyLimit}"/>">  
          &nbsp;&nbsp;&nbsp;
          <button type="submit">Update</button>
          <br/><br/>
          <c:if test="${errorMsg != null}">
            <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
          </c:if>
          <br/>
        </td>
      </tr>
    </table>
    </form>  
  </body>
</html>