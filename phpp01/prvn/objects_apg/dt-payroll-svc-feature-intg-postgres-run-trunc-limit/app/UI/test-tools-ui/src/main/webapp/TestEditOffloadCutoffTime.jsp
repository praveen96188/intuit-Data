<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    String currentOffloadGroupCd = null;
    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        currentOffloadGroupCd = selectedCompany.getOffloadGroup();
        List<OffloadGroupWSDTO> offloadGroups = TestAdapterAPI.getOffloadGroupWS().queryOffloadGroups();
        String currOffLoadCutoffTime = TestAdapterAPI.getOffloadGroupWS().getOffloadCutoffTime(currentOffloadGroupCd);
        request.setAttribute("offloadGroupResult", offloadGroups);
        request.setAttribute("currentOffloadGroupCd", currentOffloadGroupCd);
        request.setAttribute("currOffLoadCutoffTime", currOffLoadCutoffTime);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Edit Offload Cutoff Time</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
    <script language="javascript" src="scripts/Scripts.js"></script>
    <script language="javascript">var offloadGroupTime = new Array();</script>
  </head>
  <body onLoad="document.getElementById('newOffLoadCutoffTime').focus()">
  <form action="TestEditOffloadCutoffTime_Submit.jsp" method="POST">
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3>Edit Offload Cutoff Time</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <br/>
          <table border="0">
            <tr>
              <td>Offload Group:</td>
              <td>
                <select name="offloadGroupCd" onChange="document.getElementById('newOffLoadCutoffTime').value = offloadGroupTime[this.value];">
                  <c:forEach var="offloadGroup" items="${offloadGroupResult}">
                    <option <c:if test="${offloadGroup.offloadGroupCd == currentOffloadGroupCd}">selected</c:if> value="<c:out value="${offloadGroup.offloadGroupCd}"/>"><c:out value="${offloadGroup.offloadGroupCd}"/> : <c:out value="${offloadGroup.name}"/></option>
                    <script language="javascript">offloadGroupTime['<c:out value="${offloadGroup.offloadGroupCd}"/>'] = '<c:out value="${offloadGroup.cutoffTime}" />';</script>
                  </c:forEach>
                </select>
              </td>
            </tr>
            <tr>
              <td>Cutoff Time:</td>
              <td>
                <input type="text" id="newOffLoadCutoffTime" name="newOffLoadCutoffTime" maxlength="8" size="8"
                       value="<c:out value="${currOffLoadCutoffTime}"/>" onkeypress="filterInvalidChars('[0-9:]')">
                      (ex. 15:34:00, leave empty to set to time now)
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