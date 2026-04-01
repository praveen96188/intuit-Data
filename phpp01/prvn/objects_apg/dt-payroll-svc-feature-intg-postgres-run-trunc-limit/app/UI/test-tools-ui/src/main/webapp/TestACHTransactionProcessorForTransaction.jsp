<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String payrollRunId = request.getParameter("payrollRunId");
    String transactionId = request.getParameter("transactionId");
    String errorMsg = null;
    boolean payrollUpdated = false;
    try {
        TestAdapterAPI.getTransactionsWS().processACHTransaction(transactionId);
        response.sendRedirect("TestPayrollDetails.jsp?payrollRunId=" + payrollRunId);
        errorMsg = "Transaction was offloaded.";
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
             <h3>Failed to process ach transactions for transaction with id: <%=transactionId%></h3>
               </c:if>
             <span style='color:red'><%=errorMsg%></span><br/><br/>
             <button onClick='javascript:history.go(-1);'>Back</button>
           </div>
        </td>
      </tr>
    </table>
  </body>
</html>