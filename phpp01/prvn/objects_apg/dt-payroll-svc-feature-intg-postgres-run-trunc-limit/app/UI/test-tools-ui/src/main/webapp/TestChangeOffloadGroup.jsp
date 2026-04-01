<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyBankAccountWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.PayrollRunWSDTO" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    String currentOffloadGroupCd = null;
    String action = (String) request.getParameter("action");
    String offloadGroupCd = (String) request.getParameter("offloadGroupCd");
    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        if (action != null && action.equals("update")) {
            TestAdapterAPI.getCompanyWS().setOffloadGroup(selectedCompany.getSourceSystemCD(),
                    selectedCompany.getSourceCompanyID(), offloadGroupCd);
        }
        selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq, false, false, false);
        currentOffloadGroupCd = selectedCompany.getOffloadGroup();
        List<OffloadGroupWSDTO> offloadGroups = TestAdapterAPI.getOffloadGroupWS().queryOffloadGroups();
        request.setAttribute("offloadGroupResult", offloadGroups);
        request.setAttribute("currentOffloadGroupCd", currentOffloadGroupCd);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("action", action);
%>

<html>
  <head>
    <title>Change Offload Group</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>
  <form action="TestChangeOffloadGroup.jsp" method="POST">
      <input type="hidden" name="action" value="update"/>
    <table border=0>
      <tr>
        <td valign="top">
          <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
          <%@ include file="TestCompanyHeader.jsp" %>
          <h3 class="pageTitle">Change Offload Group for a Company</h3>
          <c:if test="${errorMsg != null}">
              <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
          </c:if>
          <c:if test="${action != null and errorMsg == null}">
            Offload group has been set to '<c:out value="${currentOffloadGroupCd}"/>'<br/>
          </c:if>
          <br/>
          <table border="0">
            <tr>
              <td>Offload Group:</td>
              <td>
                <select name="offloadGroupCd">
                  <c:forEach var="offloadGroup" items="${offloadGroupResult}">
                    <option <c:if test="${offloadGroup.offloadGroupCd == currentOffloadGroupCd}">selected</c:if>
                            value="<c:out value="${offloadGroup.offloadGroupCd}"/>">
                        <c:out value="${offloadGroup.offloadGroupCd}"/> : <c:out value="${offloadGroup.name}"/>
                    </option>
                  </c:forEach>
                </select>
              </td>
            </tr>
          </table>
          <br/>
          <br/>
          <button type="submit">Submit</button>
        </td>
      </tr>
    </table>
    </form>
  </body>
</html>