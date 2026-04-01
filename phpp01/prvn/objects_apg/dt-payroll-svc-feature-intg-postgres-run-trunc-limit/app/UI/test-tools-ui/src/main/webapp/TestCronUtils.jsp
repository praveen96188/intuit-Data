<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String message = null;
    String action = request.getParameter("action");
    try{
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String pspDate = df.format(TestAdapterAPI.getPSPDateWS().get().toGregorianCalendar().getTime());
        System.out.println("PSP Date " + pspDate);
        if ("achtxprocessor".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runAchTransactionProcessor(pspDate);
            message ="ACH Transactions Processed.";
        } else if ("payrollprocessor".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runMissedPayrollProcessor(pspDate);
            message ="Missed Payrolls Processed.";
        } else if ("missedtxprocessor".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runMissedTransactionProcessor(pspDate);
            message ="Missed Transactions Processed.";
        } else if ("fraudulentPayrollProcessor".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runFraudulentPayrollProcessor();
            message ="Fraudulent Payrolls Processed.";
        } else if ("eftpsEnrollmentProcessor".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runEftpsEnrollmentBatchProcessor();
            message ="Eftps Enrollments Processed.";
        } else if ("atfDataExtractAll".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runATFExtractE2E("QuarterlyData");
            message ="Scheduled ATF Extract for ALL (current quarter)";
        } else if ("atfDataExtractUpdate".equals(action)) {
            TestAdapterAPI.getBatchJobsWS().runATFExtractE2E("UpdatedData");
            message ="Scheduled ATF Extract for UPDATE";
        } else {
            message = "Unknown action: \""+action+"\"";
        }

    }catch(Exception ex){
        message = ex.getMessage();
    }
    request.setAttribute("message", message);
%>
<html>
  <head>
    <title>Test Cron Utils</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
            <br/><br/>
            <h4><c:out value="${message}"/></h4>
        </td>
      </tr>
    </table>  
  </body>
</html>