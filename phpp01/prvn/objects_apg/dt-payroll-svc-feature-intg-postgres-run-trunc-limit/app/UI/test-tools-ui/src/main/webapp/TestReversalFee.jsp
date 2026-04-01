<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    boolean txnInserted = false;
    String payrollRunGseq = request.getParameter("payrollRunId");
    String reversalFeeBankAccountGseq = request.getParameter("reversalFeeBankAccountId");
    String reversalFeeSettlementDate = request.getParameter("reversalFeeSettlementDate");
    String reversalFeeSettlementType = request.getParameter("reversalFeeSettlementType");
    String reversalFeeAmount = request.getParameter("reversalFeeAmount");
    String selectedCompanyGseq = getCompanyGseq(request);
    try{
        TestAdapterAPI.getTransactionsWS().createFeeTransaction(selectedCompanyGseq, payrollRunGseq, reversalFeeAmount,
                reversalFeeSettlementType,reversalFeeSettlementDate, reversalFeeBankAccountGseq, "REVFEEAMT");
        txnInserted = true;
        response.sendRedirect("TestPayrollDetails.jsp?payrollRunId=" + payrollRunGseq);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }

    request.setAttribute("txnInserted", txnInserted);
%>
<html>
  <head>
    <title>Reversal Fee</title>
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
               <c:if test="${not txnInserted}">
             <h3>Failed to create transaction</h3>
               </c:if>
             <span style='color:red'><%=errorMsg%></span><br/><br/>
             <button onClick='javascript:history.go(-1);'>Back</button>
           </div>
        </td>
      </tr>
    </table>
  </body>
</html>