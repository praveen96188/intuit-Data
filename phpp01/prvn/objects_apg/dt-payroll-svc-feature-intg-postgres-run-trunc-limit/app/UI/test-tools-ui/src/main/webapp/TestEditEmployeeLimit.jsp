<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    String employeeLimit = request.getParameter("employeeLimit");

    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        if (employeeLimit != null && employeeLimit.length() > 0) {
            errorMsg = TestAdapterAPI.getCompanyWS().updateDDLimits(selectedCompany.getSourceSystemCD(),
                    selectedCompany.getSourceCompanyID(), null, employeeLimit);
        } else {
            employeeLimit = selectedCompany.getOverrideEmployeeLimitAmount().toString();
        }
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("employeeLimit", employeeLimit);
    request.setAttribute("errorMsg", errorMsg);
%>
<html>
  <head>
    <title>Edit Employee Limit</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>
  <form action="TestEditEmployeeLimit.jsp" method="POST">
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3 class="pageTitle">Edit Employee Limit</h3>
          <br>
          DD Employee Limit:
          &nbsp;&nbsp;
          <input type="text" style="text-align:right" name="employeeLimit" maxlength="10" size="10"
                 value="<c:out value="${employeeLimit}"/>">
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