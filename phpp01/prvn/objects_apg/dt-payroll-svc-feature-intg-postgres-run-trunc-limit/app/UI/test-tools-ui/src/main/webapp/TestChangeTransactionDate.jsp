<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="org.owasp.encoder.Encode"%>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String payrollRunId = request.getParameter("payrollRunId");
    int days = Integer.parseInt(request.getParameter("days"));
    String txnId = request.getParameter("txnId");
    String errorMsg = null;
    boolean txnUpdated = false;
    try {
        TestAdapterAPI.getTransactionsWS().updateTransactionDate(txnId, days);
        errorMsg = "Txn Settlement date was updated";
        response.sendRedirect("TestPayrollDetails.jsp?payrollRunId=" + payrollRunId);
    } catch (Exception ex) {
        errorMsg = ex.getMessage();
    }
    request.setAttribute("txnUpdated", txnUpdated);
%>
<html>
  <head>
    <title>Change Transaction Settlement Date</title>
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
             <c:if test="${not txnUpdated}">
               <h3>Failed to change transaction settlement date for txn id: <%=Encode.forHtml(txnId)%></h3>
             </c:if>
             <span style='color:red'><%=errorMsg%></span><br/><br/>
             <button onClick='javascript:history.go(-1);'>Back</button>
           </div>            
        </td>
      </tr>
    </table>  
  </body>
</html>