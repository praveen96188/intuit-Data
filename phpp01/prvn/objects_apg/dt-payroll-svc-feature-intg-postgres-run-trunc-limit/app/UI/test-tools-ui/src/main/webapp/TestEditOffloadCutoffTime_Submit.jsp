<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyBankAccountWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.PayrollRunWSDTO" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String offloadGroupCd = request.getParameter("offloadGroupCd");
    String newOffLoadCutoffTime = request.getParameter("newOffLoadCutoffTime");

    if (newOffLoadCutoffTime.equals("")) {
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);

        int newOffLoadCutoffTime_Hour = calendar.get(Calendar.HOUR_OF_DAY);
        int newOffLoadCutoffTime_Minute = calendar.get(Calendar.MINUTE);
        int newOffLoadCutoffTime_Second = calendar.get(Calendar.SECOND);

        String newOffLoadCutoffTime_Formatted = newOffLoadCutoffTime_Hour + ":" +
                newOffLoadCutoffTime_Minute + ":" +
                newOffLoadCutoffTime_Second;

        newOffLoadCutoffTime = newOffLoadCutoffTime_Formatted;
    }
    try{
        TestAdapterAPI.getOffloadGroupWS().update(offloadGroupCd, newOffLoadCutoffTime);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }

    request.setAttribute("newOffLoadCutoffTime", newOffLoadCutoffTime);
    request.setAttribute("offloadGroupCd", offloadGroupCd);
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Edit Offload Cutoff Time</title>
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
            <h3>Edit Offload Cutoff Time</h3>
            <c:if test="${errorMsg == null}">
                Offload group: <c:out value="${offloadGroupCd}"/> <br/>
                New Cutoff time: <c:out value="${newOffLoadCutoffTime}"/> <br/>
            </c:if>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <br/>
        </td>
      </tr>
    </table>  
  </body>
</html>