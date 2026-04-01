<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyBankAccountWSDTO" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    
    try{
        List<CompanyBankAccountWSDTO> companyBankAccounts = TestAdapterAPI.getCompanyWS().queryCompanyBankAccounts(selectedCompanyGseq);
        request.setAttribute("bankAccountsResult", companyBankAccounts);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Company Bank Account List</title>
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
            <h3>Select Company Bank Account</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>            
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th>Source ID</th>
                <th>Status</th>
                <th>Verify Retry Cnt</th>
                <th>Bank Name</th>
                <th>Routing Number</th>
                <th>Account Number</th>
              </tr>
                <c:forEach var="companyBankAccount" items="${bankAccountsResult}" varStatus="status">
                    <c:choose>
                        <c:when test="${(status.count % 2) == 0}">
                            <c:set var="color" value="#eeeeee" />
                        </c:when>
                        <c:otherwise>
                             <c:set var="color" value="#ffffff" />
                        </c:otherwise>
                    </c:choose>
                    <tr bgcolor='<c:out value="${color}"/>' >
                      <td><c:out value="${companyBankAccount.sourceBankAccountID}"/></td>
                      <td>
                        <a href='TestEditCompanyBankAccountStatus.jsp?compBankAccountGseq=<c:out value="${companyBankAccount.id}"/>'>
                            <c:out value="${companyBankAccount.statusCode}"/>
                        </a>
                      </td>
                      <td><c:out value="${companyBankAccount.verfyRetryCount}"/></td>
                      <td><c:out value="${companyBankAccount.bankAccount.bankName}"/></td>
                      <td><c:out value="${companyBankAccount.bankAccount.routingNumber}"/></td>
                      <td><c:out value="${companyBankAccount.bankAccount.accountNumber}"/></td>
                    </tr>
                </c:forEach>
            </table><br/>
            <span style='color:red'>Note: PSP allows only one Company Bank Account</span><br/>
        </td>
      </tr>
    </table>  
  </body>
</html>