<%@ page import="java.util.*" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.PayrollRunWSDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%      
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        List<PayrollRunWSDTO> payrollRuns = TestAdapterAPI.getTransactionsWS().getPayrollRuns(selectedCompany.getSourceSystemCD(),selectedCompany.getSourceCompanyID(), null);
        request.setAttribute("payrollRunsResult", payrollRuns);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Payroll Runs</title>
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
            <h3>Payroll Runs (Batches)</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Payroll<br/>Run Gseq</b></th>
                <th><b>Payroll<br/>Run Date</b></th>
                <th><b>Paycheck<br/>Deposit Date</b></th>
                <th><b>Status</b></th>
                <th><b>Source<br/>Batch ID</b></th>
                <th><b>Net Amount</b></th>
                <th><b>Paycheck<br/>Count</b></th>
                <th><b>Txn<br/>Count</b></th>
                <th><b>Offload<br/>Executed</b></th>
                <th><b>Offload<br/>Pending</b></th>
              </tr>
                <c:forEach var="payrollRun" items="${payrollRunsResult}" varStatus="status">
                    <c:choose>
                        <c:when test="${(status.count % 2) == 0}">
                            <c:set var="color" value="#eeeeee" />
                        </c:when>
                        <c:otherwise>
                             <c:set var="color" value="#ffffff" />
                        </c:otherwise>
                    </c:choose>
                    <tr bgcolor='<c:out value="${color}"/>' >
						<td align="right">
							<c:out value="${payrollRun.id}"/>
						</td>
						<td align="center">
                            <a href='TestPayrollDetails.jsp?payrollRunId=<c:out value="${payrollRun.id}"/>'>
                                <c:out value="${payrollRun.payrollRunDate}"/>
                                <%-- <fmt:formatDate value="${payrollRun.payrollRunDate}" dateStyle="short"/> --%></a>
                        </td>
						<td align="center">
                            &nbsp;<%--<fmt:formatDate value="${payrollRun.paycheckDepositDate}" dateStyle="short"/>--%>
                            <c:out value="${payrollRun.paycheckDepositDate}"/>
                        </td>
                        <td align="center">
							&nbsp;<c:out value="${payrollRun.status}"/>
                        </td>
						<td align="right">
							&nbsp;<c:out value="${payrollRun.sourceBatchId}"/>
						</td>
                        <td align="right">
                            &nbsp;<fmt:formatNumber type="currency" value="${payrollRun.netAmount}"/>
                        </td>
                        <td align="right">
                            &nbsp;<fmt:formatNumber value="${payrollRun.paycheckCount}"/>
                        </td>
                        <td align="right">
                            &nbsp;<fmt:formatNumber value="${payrollRun.txnCount}"/>
                        </td>
                        <td align="right">
                            &nbsp;<fmt:formatNumber value="${payrollRun.offloadExecutedCount}"/>
                        </td>
                        <td align="right">
                            &nbsp;<fmt:formatNumber value="${payrollRun.offloadExecutedCount}"/>
                        </td>
                    </tr>                    
                </c:forEach>
            </table>
        </td>
      </tr>
    </table>
  </body>
</html>