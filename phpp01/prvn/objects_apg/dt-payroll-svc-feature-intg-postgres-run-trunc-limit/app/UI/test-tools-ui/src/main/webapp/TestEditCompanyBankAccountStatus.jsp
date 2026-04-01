<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyBankAccountWSDTO" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="org.owasp.encoder.Encode"%>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    CompanyWSDTO selectedCompany=null;

    TreeMap statusValues = new TreeMap();
    statusValues.put("Active", "Active");
    statusValues.put("Inactive", "Inactive");
    statusValues.put("PendingVerification", "Pending Verification");

    String selectedCompanyGseq = getCompanyGseq(request);
    String companyBankAccGseq = request.getParameter("compBankAccountGseq");
    try{
        selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        CompanyBankAccountWSDTO companyBankAccount = TestAdapterAPI.getCompanyWS().queryCompanyBankAccount(companyBankAccGseq);
        request.setAttribute("companyBankAccount", companyBankAccount);
        request.setAttribute("currCompanyBankAccountStatusName", statusValues.get(companyBankAccount.getStatusCode()));
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    boolean isStatusUpdatable = selectedCompany.getDdStatus() != null && selectedCompany.getDdStatus().equals("ActiveCurrent");
    
    if (errorMsg != null && !isStatusUpdatable) {
        errorMsg = "You can't change the company bank status when company status is \"Inactive\"";
    }

    request.setAttribute("isStatusUpdatable",isStatusUpdatable);
    request.setAttribute("statusValues", statusValues);
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Edit Company Bank Account Status</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>
  <form action="TestEditCompanyBankAccountStatus_Submit.jsp" method="POST">
    <input type="hidden" name="compBankAccountGseq" value="<%=Encode.forHtml(companyBankAccGseq)%>">
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3>Edit Company Bank Account Status</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <table border=0 cellspacing="0" cellpadding="4">
              <tr>
                <td>Source ID:</td>
                <td><c:out value="${companyBankAccount.sourceBankAccountID}"/> </td>
              </tr>
              <tr>
                <td>Bank Name:</td>
                <td><c:out value="${companyBankAccount.bankAccount.bankName}"/></td>
              </tr>
              <tr>
                <td>Routing Number:</td>
                <td><c:out value="${companyBankAccount.bankAccount.routingNumber}"/></td>
              </tr>
              <tr>
                <td>Account Number:</td>
                <td><c:out value="${companyBankAccount.bankAccount.accountNumber}"/></td>
              </tr>
              <tr>
                <td>Verify Retry Counter:</td>
                <td><c:out value="${companyBankAccount.verfyRetryCount}"/></td>
              </tr>
              <tr>
                <td>Status:</td>
                <td>
                <c:choose>
                  <c:when test="${isStatusUpdatable}">
                    <select id="newCompanyBankAccountStatus" name="newCompanyBankAccountStatus">
                      <c:forEach var="status" items="${statusValues}" >
                        <option value="<c:out value="${status.key}"/>"
                        <c:if test="${status.key == companyBankAccount.statusCode}">selected</c:if> >
                        <c:out value="${status.value}"/>
                        </option>
                      </c:forEach>
                    </select>
                  </c:when>
                  <c:otherwise>
                    <c:out value="${currCompanyBankAccountStatusName}"/>
                  </c:otherwise>
                </c:choose>
                </td>                  
              </tr>
              <tr>
                <td></td>
                <td><button type="submit" <c:if test="${not isStatusUpdatable}">disabled</c:if>>Submit</button> </td>
              </tr>
            </table>
        </td>
      </tr>
    </table>
  </form>
  </body>
</html>