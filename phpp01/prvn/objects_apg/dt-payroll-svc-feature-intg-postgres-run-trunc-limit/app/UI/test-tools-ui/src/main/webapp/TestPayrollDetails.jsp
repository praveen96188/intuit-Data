<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.PayrollRunWSDTO" %>
<%@ page import="java.util.*" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.PaycheckWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.TransactionWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.BankReturnWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyBankAccountWSDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="org.owasp.encoder.Encode"%>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    String payrollRunGseq = request.getParameter("payrollRunId");
    String paycheckDepositDate = null;
    String payrollRunDate = null;
    List<PaycheckWSDTO> paychecks = null;
    PayrollRunWSDTO payrollRun = null;
    List<TransactionWSDTO> transactions = null;
    List<BankReturnWSDTO> bankReturns = null;
    List<CompanyBankAccountWSDTO> companyBankAccounts = null;
    String reversalFeeAmount = null;
    String returnFeeAmount = null;
    CompanyWSDTO selectedCompany = null;
    try {
        String selectedCompanyGseq = getCompanyGseq(request);
        selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        companyBankAccounts = TestAdapterAPI.getCompanyWS().queryCompanyBankAccounts(selectedCompanyGseq);
        payrollRun = TestAdapterAPI.getTransactionsWS().getPayrollRun(payrollRunGseq);

        SimpleDateFormat df = new SimpleDateFormat("M/d/y");
        paycheckDepositDate = df.format(payrollRun.getPaycheckDepositDate().toGregorianCalendar().getTime());
        payrollRunDate = df.format(payrollRun.getPayrollRunDate().toGregorianCalendar().getTime());

        paychecks = TestAdapterAPI.getTransactionsWS().getPaychecks(
                selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID(), payrollRun.getSourceBatchId(), true, false, false);
        
        transactions = TestAdapterAPI.getTransactionsWS().getFinancialTransactions(
                selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID(), payrollRun.getSourceBatchId(), false, true, true);

        bankReturns = TestAdapterAPI.getTransactionsWS().getBankReturns(selectedCompany.getSourceSystemCD(),
                selectedCompany.getSourceCompanyID(), payrollRun.getSourceBatchId(), null);

        reversalFeeAmount = TestAdapterAPI.getTransactionsWS().getFeeAmount("ReverseFee");
        returnFeeAmount = TestAdapterAPI.getTransactionsWS().getFeeAmount("NSFFee");
    } catch (Exception ex) {
        errorMsg = ex.getMessage();
    }
    request.setAttribute("payrollRun", payrollRun);
    request.setAttribute("paycheckDepositDate", paycheckDepositDate);
    request.setAttribute("payrollRunDate", payrollRunDate);
    request.setAttribute("paychecks", paychecks);
    request.setAttribute("transactions", transactions);
    request.setAttribute("bankReturns", bankReturns);
    request.setAttribute("companyBankAccounts", companyBankAccounts);
    request.setAttribute("reversalFeeAmount", reversalFeeAmount);
    request.setAttribute("returnFeeAmount", returnFeeAmount);
    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("selectedCompany", selectedCompany);
%>

<html>
  <head>
    <title>Payroll Run Details</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
    <script type="text/javascript" src="scripts/CalendarPopup.js"></script>
    <script type="text/javascript" src="scripts/AnchorPosition.js"></script>
    <script type="text/javascript" src="scripts/date.js"></script>
    <script type="text/javascript" src="scripts/PopupWindow.js"></script>
    <script LANGUAGE="JavaScript">document.write(getCalendarStyles());</script>
	<script type="text/javascript">
        function days_between(date1, date2) {
            var ONE_DAY = 1000 * 60 * 60 * 24;
            var date1_ms = date1.getTime();
            var date2_ms = date2.getTime();
            var difference_ms = date2_ms - date1_ms;
            return Math.round(difference_ms/ONE_DAY);
        }

        var calP = new CalendarPopup("calendarDiv");
        calP.setReturnFunction("changePayrollDate");

        var calTx = new CalendarPopup("calendarDiv");
        calTx.setReturnFunction("changeTxDate");

        var txnId = null;
        var txnDate = null;
        var payrollDate = parseDate('<c:out value="${paycheckDepositDate}" />');

        function changePayrollDate(y,m,d) {
            var date = m+'/'+d+'/'+y;
            var selectedDate = parseDate(date);
            var days = days_between(payrollDate, selectedDate);
            if (days != 0) {
                document.body.style.cursor = 'wait';
                document.location = 'TestChangePayrollDate.jsp?payrollRunId=<c:out value="${payrollRun.id}"/>&days=' + days;
            }
        }

        function changeTxDate(y,m,d) {
            var date = m+'/'+d+'/'+y;
            var selectedDate = parseDate(date);
            var days = days_between(txnDate, selectedDate);
            if (days != 0) {
                document.body.style.cursor = 'wait';
                document.location = 'TestChangeTransactionDate.jsp?txnId=' + txnId + '&payrollRunId=<c:out value="${payrollRun.id}"/>&days=' + days;
            }
        }
	</script>
    <script type="text/javascript">
        function showhide(what){
            if (eval("document.getElementById('" + what + "').style.display =='none'")){
                eval("document.getElementById('" + what + "').style.display='block'");
            } else {
                eval("document.getElementById('" + what + "').style.display='none'");
            }
        }
    </script>      
  </head>
  <body>
    <DIV ID="calendarDiv" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3>Payroll Run (Batch) Details</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <table border=0>
              <tr>
                <td>Payroll Run Date:</td>
                <td style="color:green;font-weight:bold">
                  <c:out value="${payrollRunDate}" />
                     <%-- <fmt:formatDate value="${payrollRun.payrollRunDate}" /> --%>
                </td>
              </tr>
              <tr>
                <td>Paycheck Deposit Date:</td>
                <td style="color:green;font-weight:bold">
                  <c:out value="${paycheckDepositDate}" />
                  &nbsp;&nbsp;
                  <%-- <A HREF="#" onClick="calP.showCalendar('anchorP', '<fmt:formatDate value="${payrollRun.PAYCHECK_DEPOSIT_DATE}" />'); return false;" TITLE="Change payroll date" NAME="anchorP" ID="anchorP"><img src="images/calendar.gif" border=0/></A> --%>
                  <A HREF="#" onClick="calP.showCalendar('anchorP', '<c:out value="${paycheckDepositDate}" />'); return false;" TITLE="Change payroll date" NAME="anchorP" ID="anchorP"><img alt="" src="images/calendar.gif" border=0/></A>
                </td>
              </tr>
              <tr>
                <td>Status:</td>
                <td><c:out value="${payrollRun.status}"/></td>
              </tr>
              <tr>
                <td>Source Batch ID:</td>
                <td><c:out value="${payrollRun.sourceBatchId}"/></td>
              </tr>
              <tr>
                <td>Payroll Net Amount:</td>
                <td><fmt:formatNumber type="currency" value="${Encode.forHtml(payrollRun.netAmount)}"/></td>
              </tr>
            </table>
            <br/>
            <a href='TestChangePayrollDate.jsp?payrollRunId=<c:out value="${payrollRun.id}"/>&days=-1'>&lt;&lt; Move back 1 day</a>
            &nbsp;&nbsp;&nbsp;
            <a href='TestChangePayrollDate.jsp?payrollRunId=<c:out value="${payrollRun.id}"/>&days=1'>Move forward 1 day &gt;&gt;</a>

            (The dates in <span style="color:green;font-weight:bold">green</span> will be affected.)
            <br/><br/>
            <h4>Paychecks</h4>
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Paycheck<br/>gseq</b></th>
                <th><b>Source<br/>Paycheck ID</b></th>
                <th><b>Employee ID</b></th>
                <th><b>Employee Name</b></th>
                <th><b>Amount</b></th>
              </tr>
              <c:forEach var="paycheck" items="${paychecks}" varStatus="status">
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
                    &nbsp;<c:out value="${paycheck.id}"/>
                  </td>
                  <td align="right">
                    &nbsp;<c:out value="${paycheck.sourcePaycheckId}"/>
                  </td>
                  <td align="right">
                    &nbsp;<c:out value="${paycheck.sourceEmployeeId}"/>
                  </td>
                  <td align="left">
                    &nbsp;<c:out value="${paycheck.employeeDisplayName}"/>
                  </td>
                  <td align="right">
                    &nbsp;<fmt:formatNumber type="currency" value="${paycheck.paycheckAmount}"/>
                  </td>
                </tr>
              </c:forEach>
            </table>
            <h4>Transactions</h4>
            <a href='#' onclick="showhide('reversalFee');return false;">Issue Reversal Fee</a><br/>
            <div id="reversalFee" style="display:none;background-color:#eeeeee">
               <form action="TestReversalFee.jsp" method="POST">
               <input type="hidden" name="payrollRunId" value="<%=Encode.forHtmlAttribute(payrollRunGseq)%>"/>
               <table border=0>
                   <tr>
                       <td>Settl. Date:</td>
                       <td><input type="text" name="reversalFeeSettlementDate" maxlength="10" size="10"> (ex. 09/23/2006)</td>
                   </tr>
                   <tr>
                       <td>Settl. Type</td>
                       <td>
                           <select name="reversalFeeSettlementType">
                                <option value="ACH">ACH</option>
                                <option value="CheckType">CheckType</option>
                                <option value="Cash">Cash</option>
                                <option value="WIRE">Wire</option>
                                <option value="Other">Other</option>
                            </select>
                       </td>
                   </tr>
                   <tr>
                       <td>Bank Account</td>
                       <td>
                           <select name="reversalFeeBankAccountId">
                               <c:forEach var="bankAccount" items="${companyBankAccounts}" varStatus="status">
                                   <option value="<c:out value="${bankAccount.id}"/>"><c:out value="${bankAccount.bankAccount.bankName}"/> (<c:if test="${bankAccount.statusCode == 'Active'}">Active</c:if><c:if test="${bankAccount.statusCode != 'Active'}">Inactive</c:if>)</option>
                               </c:forEach>
                           </select>
                       </td>
                   </tr>
                   <tr>
                       <td>Amount</td>
                       <td>
                           <input type="text" style="text-align:right" name="reversalFeeAmount" maxlength="10" size="5" value="<c:out value="${reversalFeeAmount}"/>">
                       </td>
                   </tr>
                   <tr>
                       <td colspan="2"><button type="submit">Issue Reversal Fee</button></td>
                   </tr>
               </table>
               </form>
            </div>
            <a href='#' onclick="showhide('returnFee');return false;">Issue Return Fee</a><br/>
            <div id="returnFee" style="display:none;background-color:#eeeeee">
               <form action="TestReturnFee.jsp" method="POST">
                   <input type="hidden" name="payrollRunId" value="<%=Encode.forHtmlAttribute(payrollRunGseq)%>"/>
               <table border=0>
                   <tr>
                       <td>Settl. Date:</td>
                       <td><input type="text" name="returnFeeSettlementDate" maxlength="10" size="10"> (ex. 09/23/2006)</td>
                   </tr>
                   <tr>
                       <td>Settl. Type</td>
                       <td>
                           <select name="returnFeeSettlementType">
                               <option value="ACH">ACH</option>
                               <option value="CheckType">CheckType</option>
                               <option value="Cash">Cash</option>
                               <option value="WIRE">Wire</option>
                               <option value="Other">Other</option>
                            </select>
                       </td>
                   </tr>
                   <tr>
                       <td>Bank Account</td>
                       <td>
                           <select name="returnFeeBankAccountId">
                               <c:forEach var="bankAccount" items="${companyBankAccounts}" varStatus="status">
                                   <option value="<c:out value="${bankAccount.id}"/>"><c:out value="${bankAccount.bankAccount.bankName}"/> (<c:if test="${bankAccount.statusCode == 'Active'}">Active</c:if><c:if test="${bankAccount.statusCode != 'Active'}">Inactive</c:if>)</option>
                               </c:forEach>
                           </select>
                       </td>
                   </tr>
                   <tr>
                       <td>Amount</td>
                       <td>
                           <input type="text" style="text-align:right" name="returnFeeAmount" maxlength="10" size="5" value="<c:out value="${returnFeeAmount}"/>">
                       </td>
                   </tr>
                   <tr>
                       <td colspan="2"><button type="submit">Issue Return Fee</button></td>
                   </tr>
               </table>
               </form>
            </div>
            <br/><br/>
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Txn<br/>gseq</b></th>
                <th><b>Transaction<br/>Type</b></th>
                <th><b>Current<br/>State</b></th>
                <th><b>Amount</b></th>
                <th><b>Settl.<br/>Type</b></th>
                <th><b>Settlement<br/>Date</b></th>
                <th><b>Offload<br/>Status</b></th>
                <th><b>Credited<br/>Bank Account</b></th>
                <th><b>Debited<br/>Bank Account</b></th>
                <th><b>Action</b></th>
              </tr>
              <c:forEach var="transaction" items="${transactions}" varStatus="status">
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
                    &nbsp;<c:out value="${transaction.id}"/>
                </td>
                <td align="center">
                    &nbsp;<c:out value="${transaction.transactionType}"/>
                </td>
                <td align="center">
                    <c:out value="${transaction.currentState}"/>
                    <%--
                    <c:if test="${transaction.TXN_RETURN_COUNT > 1}">
                        <br/><span style="color:red;font-weight:bold;font-size:8pt"><c:out value="${transaction.TXN_RETURN_COUNT}"/> Returns</span><br/>
                    </c:if>
                    <c:if test="${transaction.TXN_RETURN_COUNT == 1}">
					    <br/><span style="color:red;font-weight:bold;font-size:8pt"><c:out value="${transaction.TXN_RETURN_COUNT}"/> Return</span><br/>
                    </c:if>
                    --%>
                </td>
                <td align="right">
                    &nbsp;<fmt:formatNumber type="currency" value="${transaction.transactionAmount}"/>
                </td>
                <td align="center">
                    &nbsp;<c:out value="${transaction.settlementType}"/>
                </td>
                <td style="color:green;font-weight:bold">
                    <a href='TestChangeTransactionDate.jsp?txnId=<c:out value="${transaction.id}"/>&payrollRunId=<c:out value="${payrollRun.id}"/>&days=-1'>&lt;&lt; </a>
                    <%-- <fmt:formatDate value="${transaction.SETTLEMENT_DATE}" /> --%>
                    <c:out value="${transaction.settlementDate}" />
                    <a href='TestChangeTransactionDate.jsp?txnId=<c:out value="${transaction.id}"/>&payrollRunId=<c:out value="${payrollRun.id}"/>&days=1'>&gt;&gt;</a>
                    <%-- Need to remove the comments <A HREF="#" onClick="txnDate=parseDate('<c:out value="${transaction.settlementDate}" />');txnId=<c:out value="${transaction.id}"/>;calTx.showCalendar('anchor<c:out value="${transaction.id}"/>', '<c:out value="${transaction.settlementDate}" />'); return false;" TITLE="Calendar" NAME="anchor<c:out value="${transaction.id}"/>" ID="anchor<c:out value="${transaction.id}"/>"><img alt= "" src="images/calendar.gif" border=0/></A> --%>
                    <br/>
                </td>
                <td align="center">
                    &nbsp;<c:out value="${transaction.offloadStatus}"/>
                </td>
                <td align="left" style="font-size:8pt">
                    <div>
                        <a href="#" onclick="showhide('cd<c:out value="${transaction.id}"/>'); return false;"><c:out value="${transaction.creditBankAccount.accountNumber}"/></a>
                    </div>
                    <div style="display:none" id="cd<c:out value="${transaction.id}"/>">
                    <table border="0">
                        <tr><td>Bank Name:</td><td><c:out value="${transaction.creditBankAccount.bankName}"/></td></tr>
                        <tr><td>Bank Account Type:</td><td><c:out value="${transaction.creditBankAccount.bankAccountOwnerType}"/></td></tr>
                        <tr><td>Checking/Savings:</td><td><c:out value="${transaction.creditBankAccount.bankAccountType}"/></td></tr>
                        <tr><td>Routing Number:</td><td><c:out value="${transaction.creditBankAccount.routingNumber}"/></td></tr>
                        <tr><td>Account_Number:</td><td><c:out value="${transaction.creditBankAccount.accountNumber}"/></td></tr>
                    </table>
                    </div>
                </td>
                <td align="left" style="font-size:8pt">
                    <div>
                        <a href="#" onclick="showhide('dd<c:out value="${transaction.id}"/>'); return false;"><c:out value="${transaction.debitBankAccount.accountNumber}"/></a>
                    </div>
                    <div style="display:none" id="dd<c:out value="${transaction.id}"/>">
                    <table border="0">
                        <tr><td>Bank Name:</td><td><c:out value="${transaction.debitBankAccount.bankName}"/></td></tr>
                        <tr><td>Bank Account Type:</td><td><c:out value="${transaction.debitBankAccount.bankAccountOwnerType}"/></td></tr>
                        <tr><td>Checking/Savings:</td><td><c:out value="${transaction.debitBankAccount.bankAccountType}"/></td></tr>
                        <tr><td>Routing Number:</td><td><c:out value="${transaction.debitBankAccount.routingNumber}"/></td></tr>
                        <tr><td>Account_Number:</td><td><c:out value="${transaction.debitBankAccount.accountNumber}"/></td></tr>
                    </table>
                    </div>
                </td>
                <td>
                    <c:if test="${transaction.action != null}">
                        <c:if test="${transaction.action == 'Run Offload'}">
                            <a href='TestRunOffloadForTransaction.jsp?payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>'><c:out value="${transaction.action}"/></a>
                        </c:if>
                        <c:if test="${transaction.action == 'Complete Transaction'}">
                            <a href='TestACHTransactionProcessorForTransaction.jsp?payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>'><c:out value="${transaction.action}"/></a>
                        </c:if>
                        <c:if test="${transaction.action == 'Submit Payment'}">
                            <a href='TestPaymentsProcessor.jsp?actionCode=Submit&payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>&companyCode=<c:out value="${selectedCompany.sourceSystemCD}"/>&companyId=<c:out value="${selectedCompany.sourceCompanyID}"/>&dueDate=<c:out value="${transaction.moneyMovementTransaction.dueDate}"/>'><c:out value="${transaction.action}"/> <c:out value="${transaction.template}"/></a>
                        </c:if>
                        <c:if test="${transaction.action != null && transaction.action == 'Complete Payment'}">
                             <a href='TestPaymentsProcessor.jsp?actionCode=Complete&payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>&companyCode=<c:out value="${selectedCompany.sourceSystemCD}"/>&companyId=<c:out value="${selectedCompany.sourceCompanyID}"/>&dueDate=<c:out value="${transaction.moneyMovementTransaction.dueDate}"/>'><c:out value="Complete Payment"/> <c:out value="${transaction.template}"/></a>
                        </c:if>
                         <c:if test="${transaction.action != null && transaction.action == 'Payment Submitted'}">
                             <a href='TestPaymentsProcessor.jsp?actionCode=Reject&payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>&companyCode=<c:out value="${selectedCompany.sourceSystemCD}"/>&companyId=<c:out value="${selectedCompany.sourceCompanyID}"/>&dueDate=<c:out value="${transaction.moneyMovementTransaction.dueDate}"/>'><c:out value="Reject Payment"/></a>/
                             <a href='TestPaymentsProcessor.jsp?actionCode=Return&payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>&companyCode=<c:out value="${selectedCompany.sourceSystemCD}"/>&companyId=<c:out value="${selectedCompany.sourceCompanyID}"/>&dueDate=<c:out value="${transaction.moneyMovementTransaction.dueDate}"/>'><c:out value="Return Payment"/></a>/
                             <a href='TestPaymentsProcessor.jsp?actionCode=Complete&payrollRunId=<c:out value="${payrollRun.id}"/>&transactionId=<c:out value="${transaction.id}"/>&companyCode=<c:out value="${selectedCompany.sourceSystemCD}"/>&companyId=<c:out value="${selectedCompany.sourceCompanyID}"/>&dueDate=<c:out value="${transaction.moneyMovementTransaction.dueDate}"/>'><c:out value="Complete Payment"/></a>
                        </c:if>
                    </c:if>
                </td>
              </tr>
              </c:forEach>
          </table>
          <h4>Returns</h4>
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Return<br/>gseq</b></th>
                <th><b>Transaction<br/>gseq</b></th>
                <th><b>Source<br/>Employee ID</b></th>
                <th><b>Employee<br/>Name</b></th>
                <th><b>Trace<br/>Number</b></th>
                <th><b>Bank<br/>Return CD</b></th>
                <th><b>Return<br/>Status</b></th>
                <th><b>Status<br/>Change Date</b></th>
              </tr>
              <c:if test="${empty bankReturns}">
                 <tr>
                     <td colspan="8" align="center">
                         There are no returns for this payroll.
                     </td>
                 </tr>
              </c:if>
              <c:forEach var="bankReturn" items="${bankReturns}" varStatus="status">
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
                    &nbsp;<c:out value="${bankReturn.id}"/>
                </td>
                <td align="right">
                    &nbsp;<c:out value="${bankReturn.transactionId}"/>
                </td>
                <td align="center">
                    &nbsp;<c:out value="${bankReturn.sourceEmployeeId}"/>
                </td>
                <td align="center">
                    &nbsp;<c:out value="${bankReturn.employeeDisplayName}"/>
                </td>
                <td align="right">
                    &nbsp;<c:out value="${bankReturn.traceNumber}"/>
                </td>
                <td align="center">
                    &nbsp;
                    <span title="<c:out value="${bankReturn.description}"/>">
                        <c:out value="${bankReturn.bankReturnCd}"/>
                    </span>
                </td>
                <td align="center">
                    &nbsp;<c:out value="${bankReturn.returnStatus}"/>
                </td>
                <td align="center"style="color:green;font-weight:bold">
                    &nbsp;<%-- <fmt:formatDate value="${return.statusChangeDate}"/> --%>
                    <c:out value="${bankReturn.statusChangeDate}"/>
                </td>
            </tr>
          </c:forEach>
         </table>
        <br/><br/>        
        </td>
      </tr>
    </table>  
  </body>
</html>