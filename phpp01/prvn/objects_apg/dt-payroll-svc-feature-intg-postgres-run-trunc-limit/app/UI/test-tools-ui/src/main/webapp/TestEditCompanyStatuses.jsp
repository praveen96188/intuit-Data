<%@ page import="java.util.*" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OnHoldReasonWSDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String selectedCompanyGseq = getCompanyGseq(request);
    CompanyWSDTO selectedCompany = null;

    TreeMap onHoldReasonCodes = new TreeMap();
    onHoldReasonCodes.put("AchRejectOther", "AchRejectOther");
    onHoldReasonCodes.put("AchRejectR1R9", "AchRejectR1R9");
    onHoldReasonCodes.put("ActiveCurrent", "ActiveCurrent");
    onHoldReasonCodes.put("ActiveSeasonal", "ActiveSeasonal");
    onHoldReasonCodes.put("AuditCorrections", "AuditCorrections");
    onHoldReasonCodes.put("Cancelled", "Cancelled");
    onHoldReasonCodes.put("DirectDepositLimit", "DirectDepositLimit");
    onHoldReasonCodes.put("Fraud", "Fraud");
    onHoldReasonCodes.put("FraudReview", "FraudReview");
    onHoldReasonCodes.put("IntuitCollections", "IntuitCollections");
    onHoldReasonCodes.put("MissingPaperwork", "MissingPaperwork");
    onHoldReasonCodes.put("NoticeOfChange", "NoticeOfChange");
    onHoldReasonCodes.put("PendingBalanceFile", "PendingBalanceFile");
    onHoldReasonCodes.put("PendingBankVerification", "PendingBankVerification");
    onHoldReasonCodes.put("PendingFirstPayroll", "PendingFirstPayroll");
    onHoldReasonCodes.put("PendingPinCreation", "PendingPinCreation");
    onHoldReasonCodes.put("PendingTermination", "PendingTermination");
    onHoldReasonCodes.put("RiskAssessment", "RiskAssessment");
    onHoldReasonCodes.put("RiskCollections", "RiskCollections");
    onHoldReasonCodes.put("SuspendedDirectDeposit", "SuspendedDirectDeposit");
    onHoldReasonCodes.put("Terminated", "Terminated");

    TreeMap ddStatuses = new TreeMap();
    ddStatuses.put("PendingPinCreation", "PendingPinCreation");
    ddStatuses.put("PendingBankVerification", "PendingBankVerification");
    ddStatuses.put("PendingFirstPayroll", "PendingFirstPayroll");
    ddStatuses.put("ActiveCurrent", "ActiveCurrent");
    ddStatuses.put("Cancelled", "Cancelled");
    ddStatuses.put("Terminated", "Terminated");


    String errorMsg = null;

    try {
        selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        List<OnHoldReasonWSDTO> onHoldReasons = TestAdapterAPI.getCompanyWS().queryOnHoldReasons(selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID());
        String currentServiceStatus = TestAdapterAPI.getCompanyWS().queryServiceStatus(selectedCompany.getSourceSystemCD(),
                selectedCompany.getSourceCompanyID(), "DirectDeposit");

        System.out.println("currentServiceStatus" + currentServiceStatus);
        request.setAttribute("onHoldReasons", onHoldReasons);
        request.setAttribute("selectedCompany", selectedCompany);
        request.setAttribute("currentServiceStatus", currentServiceStatus);
    } catch (Exception ex) {
        errorMsg = ex.getMessage();
    }

    request.setAttribute("ddStatuses", ddStatuses);
    request.setAttribute("onHoldReasonCodes", onHoldReasonCodes);
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Edit Service Statuses</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
    <script language="javascript" src="scripts/Scripts.js"></script>
    <script type="text/javascript">
        function updateServiceStatus(){
            document.getElementById('action').value='updateServiceStatus';
        }
    </script>
  </head>
  <body>
  <form action="TestEditCompanyStatuses_Submit.jsp" method="POST">
   <input type="hidden" name="action" value="add"/>
  <table border="0">
    <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
        <h3 class="pageTitle">Edit Service Statuses</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
        <h4>On Hold Reasons</h4>
        <table border="1">
          <tr valign="top" bgcolor="#dddddd">
            <th><b>OnHold Reason<br/>Gseq</b></th>
            <th><b>OnHold Reason</b></th>
            <th><b>Effective Date</b></th>
            <th><b>Expiry Date</b></th>
            <th><b>Action</b></th>
          </tr>
          <c:forEach var="onHoldReason" items="${onHoldReasons}" varStatus="status">
              <c:choose>
                  <c:when test="${(status.count % 2) == 0}">
                      <c:set var="color" value="#eeeeee" />
                   </c:when>
                   <c:otherwise>
                         <c:set var="color" value="#ffffff" />
                   </c:otherwise>
              </c:choose>
              <tr bgcolor='<c:out value="${color}"/>' >
                  <td align="center">
                    <c:out value="${onHoldReason.id}"/>
                  </td>
                  <td align="left">
                    &nbsp;<c:out value="${onHoldReason.onHoldReasonNane}"/>
                  </td>
                  <td align="left">
                    &nbsp;<c:out value="${onHoldReason.effectiveDate}"/>
                  </td>
                  <td align="center">
                    &nbsp;<c:out value="${onHoldReason.expirationDate}"/>
                  </td>
                  <td align="center">
                    <a href='TestEditCompanyStatuses_Submit.jsp?action=delete&onHoldReasonCd=<c:out value="${onHoldReason.onHoldReasonCd}"/>'>Delete</a>&nbsp;
                  </td>
              </tr>
            </c:forEach>
            <tr bgcolor='<c:out value="${color}"/>' >
                <td align="center">
                </td>
                <td align="left">
                    <select name="onHoldReasonCd">
                      <c:forEach var="status" items="${onHoldReasonCodes}" >
                        <option value="<c:out value="${status.key}"/>">
                        <c:out value="${status.value}"/>
                        </option>
                      </c:forEach>
                    </select>
                </td>
                <td align="left">
                </td>
                <td align="center">
                </td>
                <td align="center">
                  <button type="submit">Add</button>
                </td>
            </tr>
        </table>
        <br/><br/>
        <table border="0" cellspacing="0" cellpadding="4">
          <tr>
            <td align="right" valign="top"><b>DD Status:</b>&nbsp;</td>
            <td>
              <select name="serviceStatusCD">
                <c:forEach var="status" items="${ddStatuses}" >
                  <option value="<c:out value="${status.key}"/>"
                          <c:if test="${status.key == currentServiceStatus}">selected</c:if> >
                  <c:out value="${status.value}"/>
                  </option>
                </c:forEach>
              </select>
            </td>
          </tr>
		  <tr>
		  	<td colspan="2">&nbsp;</td>
		  </tr>
          <tr>
            <td>&nbsp;</td>
            <td><button style="width:180pt" type="submit" onclick="updateServiceStatus();">Submit new PSE and DD
              Statuses</button> </td>
          </tr>
        </table>            
      </td>
    </tr>
  </table>
    </form>
  </body>
</html>