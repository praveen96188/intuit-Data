<%@ page import="java.util.*" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.EdiPaymentResponseWSDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="org.owasp.encoder.Encode"%>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String payrollRunId = request.getParameter("payrollRunId");
    String transactionId = request.getParameter("transactionId");
    String actionCode = request.getParameter("actionCode");
    String companyId = request.getParameter("companyId");
    String companyCode = request.getParameter("companyCode");
    String dueDate = request.getParameter("dueDate");
    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dueDate.substring(0, 10));
    String dueDtStr = null;
    if(date != null) {
        dueDtStr = new SimpleDateFormat("MM/dd/yyyy").format(date);
    }

    String errorMsg;
    boolean payrollUpdated = false;
    try {
        if(actionCode != null) {
            String paymentTemplateCd = TestAdapterAPI.getTransactionsWS().getPaymentTemplateCd(transactionId);
            if(actionCode.equals("Submit")) {
                if(paymentTemplateCd.equals("IRS-941-PAYMENT") || paymentTemplateCd.equals("IRS-940-PAYMENT")) {
                    TestAdapterAPI.getTransactionsWS().submitEFTPSPayment(transactionId);
                } else {
                    TestAdapterAPI.getTransactionsWS().offloadTransactionACHPayment(transactionId);
                }
            } else if (actionCode.equals("Reject") || actionCode.equals("Return")) {
                com.intuit.sbd.payroll.psp.webservices.client.EdiPaymentResponseWSDTO ediPaymentResponseWSDTO = new com.intuit.sbd.payroll.psp.webservices.client.EdiPaymentResponseWSDTO();
                ediPaymentResponseWSDTO.setPaymentDueDate(dueDtStr);
                ediPaymentResponseWSDTO.setPaymentTemplateCd(paymentTemplateCd);
                ediPaymentResponseWSDTO.setPspCompanyID(companyId);
                ediPaymentResponseWSDTO.setSourceSystemCD(companyCode);
                if (actionCode.equals("Return")) {
                    ediPaymentResponseWSDTO.setErrorCd("5001");
                    ediPaymentResponseWSDTO.setErrorMessage("Payment returned in Test tools UI");
                    TestAdapterAPI.getEdiPaymentsWS().processEDIPaymentsGenerate151(false, false, null, ediPaymentResponseWSDTO, true);
                } else {
                    ediPaymentResponseWSDTO.setErrorCd("2001");
                    ediPaymentResponseWSDTO.setErrorMessage("Payment rejected in Test tools UI");
                    TestAdapterAPI.getEdiPaymentsWS().processEDIPaymentsGenerate151(false, false, ediPaymentResponseWSDTO, null, true);                    
                }
            } else if (actionCode.equals("Complete")) {
                if(paymentTemplateCd.equals("IRS-941-PAYMENT") || paymentTemplateCd.equals("IRS-940-PAYMENT")) {
                    TestAdapterAPI.getTransactionsWS().completeEFTPSPayment(transactionId);
                } else {
                    TestAdapterAPI.getTransactionsWS().processACHTransaction(transactionId);
                }
            }

            response.sendRedirect("TestPayrollDetails.jsp?payrollRunId=" + payrollRunId);
            errorMsg = "Request processed successfully";
        } else {
            errorMsg = "Could not process your request";
        }

    } catch (Exception ex) {
        errorMsg = ex.getMessage();
    }

    request.setAttribute("payrollUpdated", payrollUpdated);
%>
<html>
  <head>
    <title>Process ACH Transaction</title>
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
               <c:if test="${not payrollUpdated}">
             <h3>Failed to process payments for transaction with id:  <%=Encode.forHtml(transactionId)%></h3>
               </c:if>
             <span style='color:red'><%=errorMsg%></span><br/><br/>
             <button onClick='javascript:history.go(-1);'>Back</button>
           </div>
        </td>
      </tr>
    </table>
  </body>
</html>