<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    String currentOffloadGroupCd = request.getParameter("currentOffloadGroupCd");
    String offloadGroupCd = null;

    try{
        List<OffloadGroupWSDTO> offloadGroups = TestAdapterAPI.getOffloadGroupWS().queryOffloadGroups();
        request.setAttribute("offloadGroupResult", offloadGroups);
        if(currentOffloadGroupCd != null && currentOffloadGroupCd.length() > 0){
            TestAdapterAPI.getOffloadGroupWS().scheduleSecondOffload(currentOffloadGroupCd);
        }else{
            CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
            offloadGroupCd = selectedCompany.getOffloadGroup();
            request.setAttribute("offloadGroupCd", offloadGroupCd);
        }
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }    
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Schedule Second Offload</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">      
  </head>
  <body>
  <form action="TestScheduleSecondOffload.jsp" method="POST">
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
          <h3 class="pageTitle">Schedule Second Offload</h3>
          <br>
          Offload Group:
          &nbsp;&nbsp;
          <select name="currentOffloadGroupCd">
            <c:forEach var="offloadGroup" items="${offloadGroupResult}">
              <option <c:if test="${offloadGroup.offloadGroupCd == offloadGroupCd}">selected</c:if> value="<c:out value="${offloadGroup.offloadGroupCd}"/>"><c:out value="${offloadGroup.offloadGroupCd}"/> : <c:out value="${offloadGroup.name}"/></option>
            </c:forEach>
          </select>
          &nbsp;&nbsp;&nbsp;
          <button type="submit">Submit</button>
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