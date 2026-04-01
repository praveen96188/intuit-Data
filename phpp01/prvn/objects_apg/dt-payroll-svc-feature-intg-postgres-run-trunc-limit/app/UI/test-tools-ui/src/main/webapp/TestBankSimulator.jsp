<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.EntryDetailRecordWSDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    TreeMap bankReturnCodes = new TreeMap();
    bankReturnCodes.put("R01", "Insufficient Funds");
    bankReturnCodes.put("R02", "Account Closed");
    bankReturnCodes.put("R03", "No Account/Unable to Locate Account");
    bankReturnCodes.put("R04", "Invalid Account Number");
    bankReturnCodes.put("R05", "Unauthorized Debit to Consumer Account Using Corporate SEC Code");
    bankReturnCodes.put("R06", "Returned per ODFIs Request");
    bankReturnCodes.put("R07", "Authorization Revoked by Customer");
    bankReturnCodes.put("R08", "Payment Stopped");
    bankReturnCodes.put("R09", "Uncollected Funds");
    bankReturnCodes.put("R09", "Uncollected Funds");
    bankReturnCodes.put("R10", "Customer Advises Not Authorized, Notice Not Provided, Improper Source Document, or Amount of Entry Not Accurately Obtained from Source Document");
    bankReturnCodes.put("R11", "Check Truncation Entry Return");
    bankReturnCodes.put("R12", "Account Sold to Another DFI");
    bankReturnCodes.put("R13", "RDFI Not Qualified to Participate");
    bankReturnCodes.put("R14", "Representative Payee Deceased or Unable to Continue in that Capacity");
    bankReturnCodes.put("R15", "Beneficiary or Account Holder (Other Than a Representative Payee) Deceased");
    bankReturnCodes.put("R16", "Account Frozen");
    bankReturnCodes.put("R17", "File Record Edit Criteria");
    bankReturnCodes.put("R18", "Improper Effective Entry Date");
    bankReturnCodes.put("R19", "Amount Field Error");
    bankReturnCodes.put("R20", "Non-Transaction Account");
    bankReturnCodes.put("R21", "Invalid Company Identification");
    bankReturnCodes.put("R22", "Invalid Individual ID Number");
    bankReturnCodes.put("R23", "Credit Entry Refused by Receiver");
    bankReturnCodes.put("R24", "Duplicate Entry");
    bankReturnCodes.put("R25", "Addenda Error");
    bankReturnCodes.put("R26", "Mandatory Field Error");
    bankReturnCodes.put("R27", "Trace Number Error");
    bankReturnCodes.put("R28", "Routing Number Check Digit Error");
    bankReturnCodes.put("R29", "Corporate Customer Advises Not Authorized");
    bankReturnCodes.put("R30", "RDFI Not Participant in Check Truncation Program");
    bankReturnCodes.put("R31", "Permissible Return Entry");
    bankReturnCodes.put("R32", "RDFI Non-Settlement R34 Limited Participation DFI");
    bankReturnCodes.put("R33", "Return of XCK Entry");
    bankReturnCodes.put("R35", "Return of Improper Debit Entry");
    bankReturnCodes.put("R36", "Return of Improper Credit Entry");
    bankReturnCodes.put("R37", "Source Document Presented for Payment");
    bankReturnCodes.put("R38", "Stop Payment on Source Document");
    bankReturnCodes.put("R39", "Improper Source Document");
    bankReturnCodes.put("R40", "Return of ENR Entry by Federal Government Agency");
    bankReturnCodes.put("R41", "Invalid Transaction Code");
    bankReturnCodes.put("R42", "Routing Number/Check Digit Error");
    bankReturnCodes.put("R43", "Invalid DFI Account Number");
    bankReturnCodes.put("R44", "Invalid Individual ID Number/Identification Number");
    bankReturnCodes.put("R45", "Invalid Individual Name/Company Name");
    bankReturnCodes.put("R46", "Invalid Representative Payee Indicator");
    bankReturnCodes.put("R47", "Duplicate Enrollment");
    bankReturnCodes.put("R50", "State Law Affecting RCK Acceptance");
    bankReturnCodes.put("R51", "Item is Ineligible, Notice Not Provided, Signature Not Genuine, Item Altered, or Amount of Entry Not Accurately Obtained from Item");
    bankReturnCodes.put("R52", "Stop Payment on Item");
    bankReturnCodes.put("R53", "Item and ACH Entry Presented for Payment");
    bankReturnCodes.put("R61", "Misrouted Return");
    bankReturnCodes.put("R62", "Incorrect Trace Number");
    bankReturnCodes.put("R63", "Incorrect Dollar Amount");
    bankReturnCodes.put("R64", "Incorrect Individual Identification");
    bankReturnCodes.put("R65", "Incorrect Transaction Code");
    bankReturnCodes.put("R66", "Incorrect Company Identification");
    bankReturnCodes.put("R67", "Duplicate Return");
    bankReturnCodes.put("R68", "Untimely Return");
    bankReturnCodes.put("R69", "Multiple Errors [Field Errors(s)]");
    bankReturnCodes.put("R70", "Permissible Return Entry Not Accepted");
    bankReturnCodes.put("R71", "Misrouted Dishonored Return");
    bankReturnCodes.put("R72", "Untimely Dishonored Return");
    bankReturnCodes.put("R73", "Timely Original Return");
    bankReturnCodes.put("R74", "Corrected Return");
    bankReturnCodes.put("R75", "Original Return Not a Duplicate");
    bankReturnCodes.put("R76", "No Errors Found");
    bankReturnCodes.put("R80", "Cross-Border Payment Coding Error");
    bankReturnCodes.put("R81", "Non-Participant in Cross-Border Program");
    bankReturnCodes.put("R82", "Invalid Foreign Receiving DFI Identification");
    bankReturnCodes.put("R83", "Foreign Receiving DFI Unable to Settle");
    bankReturnCodes.put("R84", "Entry Not Processed by OGO");

    bankReturnCodes.put("C01", "Incorrect DFI Account Number");
    bankReturnCodes.put("C02", "Incorrect Routing Number");
    bankReturnCodes.put("C03", "Incorrect Routing Number and Incorrect DFI Account Number");
    bankReturnCodes.put("C04", "Incorrect Individual Name/Receiving Company Name");
    bankReturnCodes.put("C05", "Incorrect Transaction Code");
    bankReturnCodes.put("C06", "Incorrect DFI Account Number and Incorrect Transaction Code");
    bankReturnCodes.put("C07", "Incorrect Routing Number, Incorrect DFI Account Number, and Incorrect Transaction Code");
    bankReturnCodes.put("C08", "Incorrect Foreign Receiving DFI Identification");
    bankReturnCodes.put("C09", "Incorrect Individual Identification Number");
    bankReturnCodes.put("C13", "Addenda Format Error");
    bankReturnCodes.put("C61", "Misrouted Notification of Change");
    bankReturnCodes.put("C62", "Incorrect Trace Number");
    bankReturnCodes.put("C63", "Incorrect Company Identification Number");
    bankReturnCodes.put("C64", "Incorrect Individual Identification Number/Identification Number");
    bankReturnCodes.put("C65", "Incorrectly Formatted Corrected Data");
    bankReturnCodes.put("C66", "Incorrect Discretionary Data");
    bankReturnCodes.put("C67", "Routing Number Not From Original Entry Detail Record");
    bankReturnCodes.put("C68", "DFI Account Number Not From Original Entry Detail Record");
    bankReturnCodes.put("C69", "Incorrect Transaction Code");
    
    StringBuffer dropdownOptions = new StringBuffer();
    Set returnCodes = bankReturnCodes.keySet();

    for (Object returnCode : returnCodes) {
        String key = (String) returnCode;
        String value = (String) bankReturnCodes.get(key);
        dropdownOptions.append("<option value='");
        dropdownOptions.append(key);
        dropdownOptions.append("'>");
        dropdownOptions.append(key);
        dropdownOptions.append(" - ");
        dropdownOptions.append(value);
        dropdownOptions.append("</option>\n");
    }

    String errorMsg = null;
    String startDate = request.getParameter("startDate");
    String endDate = request.getParameter("endDate");
    String selectedCompanyGseq = getCompanyGseq(request);
    String currentOffloadGroupCd = request.getParameter("offloadGroupCd");
    int startIndex = 1;
    int pageSize = 100;
    int totalRecords = 0;
    boolean hasMoreData = false;

    if (request.getParameter("startIndex") != null) {
        startIndex = Integer.parseInt(request.getParameter("startIndex"));
    }
    
    if ((startDate == null || startDate.equals("")) && (endDate == null || endDate.equals(""))) {
        Date dateToday = new Date();
        SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy");
        startDate = mmddyyyy.format(dateToday);
        endDate = mmddyyyy.format(dateToday);
    }    
    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        currentOffloadGroupCd = selectedCompany.getOffloadGroup();
        List<OffloadGroupWSDTO> offloadGroups = TestAdapterAPI.getOffloadGroupWS().queryOffloadGroups();

        totalRecords = TestAdapterAPI.getTransactionsWS().getEntryDetailRecords(startDate,
                endDate, currentOffloadGroupCd, startIndex, 0).size();

        List<EntryDetailRecordWSDTO> detailRecords = TestAdapterAPI.getTransactionsWS().getEntryDetailRecords(startDate,
                endDate, currentOffloadGroupCd, startIndex-1, pageSize);
        request.setAttribute("offloadGroupResult", offloadGroups);
        request.setAttribute("detailRecords", detailRecords);
        hasMoreData = totalRecords >= startIndex+pageSize;
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    
    request.setAttribute("hasMoreData", hasMoreData);
    request.setAttribute("pageSize", pageSize);
    request.setAttribute("startIndex", startIndex);
    request.setAttribute("startDate", startDate);
    request.setAttribute("endDate", endDate);    
    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("currentOffloadGroupCd", currentOffloadGroupCd);
    request.setAttribute("bankReturnCodes", bankReturnCodes);

%>
<html>
  <head>
      <title>Bank Simulator</title>
      <link rel="stylesheet" href="css/Styles.css" type="text/css">
      <script language="javascript" src="scripts/Scripts.js"></script>
      <script language="JavaScript">
			function filterTxnsByDateRange() {
				var startDate = document.getElementById("startDate").value;
				var endDate = document.getElementById("endDate").value;

				if (startDate == "" || endDate == "") {
					alert("Transaction Date Range Error:\nFrom and To dates are required")
				} else if (Date.parse(startDate) > Date.parse(endDate) ){
					alert("Invalid Date Range!\nFrom date cannot be after To date!");
				} else {
					document.forms[0].action='TestBankSimulator.jsp';
					document.forms[0].submit();
				}
			}
		</script>
  </head>
  <body>
  <iframe id="calendar" style="z-index:2;left:0px;visibility:hidden;position:absolute;top:0px" src="html/calendar/calendar.html"></iframe>
  <form action="TestBankSimulator_Submit.jsp" method="POST">
  <table border=0>
    <tr>
      <td valign="top">
         <%@ include file="TestToolsMenu.jsp" %>
      </td>
      <td valign="top">
         <%@ include file="TestCompanyHeader.jsp" %>
          <h3>Bank Simulator</h3>
          <c:if test="${errorMsg != null}">
              <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
          </c:if>
        <table width="350" border="0" cellspacing="0" cellpadding="6" bgcolor="#EBEBEB">
			<tr>
				<td>
                    Transactions inserted into MME database within these dates are displayed below.
                    Transactions that already have a bank return show return code and return insertion date.
                </td>
			</tr>
			<tr>
				<td valign="bottom">
                  From:
				  <input type="text" id="startDate" name="startDate" maxlength="10" size="8" onFocus="calendar.setCalendar(event, this);" value="<c:out value="${startDate}"/>">
				  <img alt= "" src="images/calendar_icon.gif" align="absmiddle" onClick="calendar.show(event, getTodaysDate());" width="16" height="18">
				  To:
				  <input type="text" id="endDate" name="endDate" maxlength="10" size="8" onFocus="calendar.setCalendar(event, this);" value="<c:out value="${endDate}"/>">
				  <img alt= "" src="images/calendar_icon.gif" align="absmiddle" onClick="calendar.show(event, getTodaysDate());" width="16" height="18">
                </td>
            </tr>
            <tr>
                <td>
                    Offload Group:
                    <select name="offloadGroupCd" style="width:200px">
                            <option value="">[All offload groups]</option>
                        <c:forEach var="offloadGroup" items="${offloadGroupResult}">
                            <option <c:if test="${offloadGroup.offloadGroupCd == currentOffloadGroupCd}">selected</c:if> value="<c:out value="${offloadGroup.offloadGroupCd}"/>"><c:out value="${offloadGroup.name}"/></option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" value="Refresh" onclick="filterTxnsByDateRange()">
                </td>
			</tr>
		</table>
        <br>

        <c:if test="${!empty detailRecords}">

            	<button type="submit">Create Returns</button>
                &nbsp;&nbsp;&nbsp;
                <c:if test="${hasMoreData or startIndex > 1}">
                    Displaying results <c:out value="${startIndex}"/> - <c:out value="${startIndex + pageSize - 1}"/>
                    &nbsp;&nbsp;
                </c:if>
                <c:if test="${startIndex > 1}">
                    <a href="TestBankSimulator.jsp?startIndex=<c:out value="${startIndex - pageSize}"/>&startDate=<c:out value="${startDate}"/>&endDate=<c:out value="${endDate}"/>&offloadGroupCd=<c:out value="${currentOffloadGroupCd}"/>">previous page</a>
                </c:if>
                &nbsp;&nbsp;
                <c:if test="${hasMoreData}">
                    <a href="TestBankSimulator.jsp?startIndex=<c:out value="${startIndex + pageSize}"/>&startDate=<c:out value="${startDate}"/>&endDate=<c:out value="${endDate}"/>&offloadGroupCd=<c:out value="${currentOffloadGroupCd}"/>">next page</a>
                </c:if>
                <br><br>

				<table border="1" cellspacing="0" cellpadding="3">
					<tr valign="top" bgcolor="#dddddd">
						<td>
							<b>Company<br>ID</b>
						</td>
						<td>
							<b>Individual<br>Name</b>
						</td>
						<td>
							<b>Settlement<br>Date</b>
						</td>
						<td>
							<b>CR<br/>/DB</b>
						</td>
						<td>
							<b>Routing<br>Number</b>
						</td>
						<td>
							<b>Account<br>Number</b>
						</td>
						<td>
							<b>Acct<br>Type</b>
						</td>
						<td>
							<b>Trace<br>Number</b>
						</td>
						<td>
							<b>Amount</b>
						</td>
						<td>
							<b>Bank Return<br/><a href="TestReturnCodes.jsp" target="_blank">Reference</a></b>
						</td>
					</tr>

                   <c:forEach var="txn" items="${detailRecords}" varStatus="status">
                      <c:choose>
                        <c:when test="${(status.count % 2) == 0}">
                            <c:set var="color" value="#eeeeee" />
                        </c:when>
                        <c:otherwise>
                             <c:set var="color" value="#ffffff" />
                      </c:otherwise>
                    </c:choose>

					<tr bgcolor='<c:out value="${color}"/>' >
						<td>
							<span style="width:80px;white-space:nowrap;overflow: hidden;text-overflow: ellipsis;" title="<c:out value="${txn.companyId}"/>"><c:out value="${txn.companyId}"/></span>
						</td>
						<td>
							<span style="width:150px;white-space:nowrap;overflow: hidden;text-overflow: ellipsis;" title="<c:out value="${txn.individualName}"/>"><c:out value="${txn.individualName}"/></span> 
						</td>
					  	<td align="center">
							<%-- <fmt:formatDate value="${txn.CHECK_DATE}" pattern="MM/dd/yyyy" /> --%>
                              <c:out value="${txn.settlementDate}" />                              
                        </td>
					  	<td align="center">
							<c:out value="${txn.creditDebitIndicator}"/>
						</td>
					  	<td>
                        <c:choose>
                            <c:when test="${txn.individualName == 'INTUIT FEE' or txn.individualName == 'INTUIT EE RETURN' or txn.individualName == 'INTUIT ER RETURN' or txn.individualName == 'INTUIT DD'}">
                                <c:out value="${txn.bankAccount.routingNumber}"/>
                            </c:when>
                            <c:otherwise>
                                <input type="text" name="achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.routingNumber" id="achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.routingNumber" value="<c:out value="${txn.bankAccount.routingNumber}"/>" maxlength="9" size="6"/>
                            </c:otherwise>
                        </c:choose>
                        </td>
					  	<td>
                        <c:choose>
                            <c:when test="${txn.individualName == 'INTUIT FEE' or txn.individualName == 'INTUIT EE RETURN' or txn.individualName == 'INTUIT ER RETURN' or txn.individualName == 'INTUIT DD'}">
                                <c:out value="${txn.bankAccount.accountNumber}"/>
                            </c:when>
                            <c:otherwise>
                                <input type="text" name="achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.accountNumber" id="achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.accountNumber" value="<c:out value="${txn.bankAccount.accountNumber}"/>" maxlength="17" size="9"/>
                            </c:otherwise>
                        </c:choose>
                        </td>
					  	<td align="center">
                          <input type="text" name="achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.accountType" id="achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.accountType"
                                 value="<c:out value="${txn.bankAccount.bankAccountType}" />" maxlength="1" size="1" style="width:20px" title="C=Checking, S=Savings, G=General Ledger, L=Loan"/>
                        </td>
					  	<td>
							&nbsp;<c:out value="${txn.traceNumber}"/>
						</td>
					  	<td align="right">
							<fmt:formatNumber type="currency" value="${txn.amount}"/>
						</td>
					    <td>

                        <c:choose>
                            <c:when test="${txn.individualName == 'INTUIT FEE' or txn.individualName == 'INTUIT EE RETURN' or txn.individualName == 'INTUIT ER RETURN' or txn.individualName == 'INTUIT DD'}">
                                &nbsp;
                            </c:when>
                            <c:otherwise>
                            
                            <c:if test="${txn.isBankReturnsExists == true}">
                                <c:forEach var="bankReturn" items="${txn.bankReturns}" varStatus="status">
                                    <c:out value="${bankReturn.bankReturnCd}"/> 
                                    <c:out value="${bankReturn.createdDate}"/> <br/>
                                </c:forEach>
                            </c:if>
                            <c:if test="${txn.isBankReturnsExists == false}">
                            <select name='achtxnseq.<c:out value="${txn.mmTransactionId}"/>.<c:out value="${txn.creditDebitIndicator}"/>.returnCode' style='width:150px;font-size:8pt'>
                                <option style="color:blue" value=""></option>
                                <%=dropdownOptions%>
                            </select>
                            </c:if>
                            </c:otherwise>
                        </c:choose>

					    </td>
					</tr>
   	     	</c:forEach>
				  </table>
              	<br>
            </c:if>

   	     	<c:if test="${empty detailRecords}">
   	     	     <br><br>
		     <table width="700" border="0" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
		          <tr>
		               <td colspan="5" style="padding-left:4pt">
		               		<span style="color:red">There were no transactions inserted within the specified dates.</span>
		               </td>
		          </tr>
		     </table>
   		</c:if>

		<c:if test="${!empty detailRecords}">
            		<button type="submit">Create Returns</button>
            	</c:if>
            	<br><br>

      </td>
    </tr>
  </table>
  </form>
  </body>
</html>