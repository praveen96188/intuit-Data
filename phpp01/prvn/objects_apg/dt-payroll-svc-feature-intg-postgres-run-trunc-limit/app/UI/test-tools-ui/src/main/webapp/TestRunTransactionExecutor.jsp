<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadBatchWSDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String sysTime = null;
    String runTxnExec = request.getParameter("runTxnExec");
    String offloadGroupCd = request.getParameter("offloadGroupCd");
    String currentOffloadGroupCd = "";
    String selectedCompanyGseq = getCompanyGseq(request);

    try {
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        List<OffloadGroupWSDTO> offloadGroups = TestAdapterAPI.getOffloadGroupWS().queryOffloadGroups();
        List<OffloadBatchWSDTO> offloadBatches = TestAdapterAPI.getOffloadGroupWS().queryOffloadBatches(true);
        request.setAttribute("offloadGroupResult", offloadGroups);
        request.setAttribute("offloadBatchResult", offloadBatches);
        currentOffloadGroupCd = selectedCompany.getOffloadGroup();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        sysTime = df.format(TestAdapterAPI.getPSPDateWS().get().toGregorianCalendar().getTime());
        System.out.println("Date " + df.format(TestAdapterAPI.getPSPDateWS().get().toGregorianCalendar().getTime()));
        if (runTxnExec != null && runTxnExec.equals("true")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String pspDate = dateFormat.format(TestAdapterAPI.getPSPDateWS().get().toGregorianCalendar().getTime());
            TestAdapterAPI.getBatchJobsWS().generateNACHAFiles(offloadGroupCd, pspDate, true);
        }
    } catch (Exception ex) {
        errorMsg = ex.getMessage();
    }
    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("runTxnExec", runTxnExec);
    request.setAttribute("currentOffloadGroupCd", currentOffloadGroupCd);
    request.setAttribute("offloadGroupCd", offloadGroupCd);
    request.setAttribute("sysTime", sysTime);
%>

<html>
  <head><title>Run Transaction Executor</title>
  <link rel="stylesheet" href="css/Styles.css" type="text/css">
  <script type="text/javascript">
    var cutoffTimes = new Array();

    function displayCutoffTimeWarning() {
      if(document.getElementById('offloadGroupCd') != null){
        var offloadGroupCd = document.getElementById('offloadGroupCd').value;
        var cutoffTime = cutoffTimes[offloadGroupCd];
        var sysTime = '<c:out value="${sysTime}"/>';

        var cutoffTimeWarnPanel = document.getElementById('cutoffTimeWarning');
        if (sysTime < cutoffTime) {
          cutoffTimeWarnPanel.style.display = "block";
          cutoffTimeWarnPanel.innerHTML = "Current database time " + sysTime + " is before Offload Cutoff Time " + cutoffTime + ".<br/>Nothing will be offloaded.";
        }
        else{
          cutoffTimeWarnPanel.style.display = "none";
        }
      }
    }
  </script>
  </head>
  <body onload="displayCutoffTimeWarning();">
  <form action="TestRunTransactionExecutor.jsp" method="POST">
   <input type="hidden" name="runTxnExec" value="true"/>
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
                <c:if test="${runTxnExec!= null}">
                <h3>Transactions Offloaded for offload group '<c:out value="${offloadGroupCd}"/>'</h3>
                </c:if>
                <c:if test="${runTxnExec == null}">
                <h3>Select the offload group to run Transaction Executor</h3>
                <br/><br/>
                    Offload Group: &nbsp;&nbsp;
                    <select name="offloadGroupCd" onChange="displayCutoffTimeWarning();">
                        <c:forEach var="offloadGroup" items="${offloadGroupResult}">
                            <option <c:if test="${offloadGroup.offloadGroupCd == currentOffloadGroupCd}">selected</c:if> value="<c:out value="${offloadGroup.offloadGroupCd}"/>"><c:out value="${offloadGroup.offloadGroupCd}"/> : <c:out value="${offloadGroup.name}"/></option>
                            <script language="javascript">cutoffTimes['<c:out value="${offloadGroup.offloadGroupCd}"/>'] = '<c:out value="${offloadGroup.cutoffTime}"/>';</script>
                        </c:forEach>
                    </select>
                    &nbsp;&nbsp;&nbsp;
                    <button type="submit">Run Executor</button>
                <br/>
                <span id="cutoffTimeWarning" style='color:red;display:none;margin:5px;'>
                </span>
            </c:if>
            <br/><br/><br/>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            (displaying offload batches from last 3 days)
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Offload<br/>Batch Gseq</b></th>
                <th><b>Offload<br/>Group CD</b></th>
                <th><b>Status</b></th>
                <th><b>Insert Date</b></th>
                <th><b>Status Change<br/>Date</b></th>
              </tr>
                 <c:forEach var="offloadBatch" items="${offloadBatchResult}" varStatus="status">
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
							<c:out value="${offloadBatch.id}"/>
						</td>
                        <td align="center">
							&nbsp;<c:out value="${offloadBatch.offloadGroupCd}"/>
                        </td>
						<td align="right">
							&nbsp;<c:out value="${offloadBatch.status}"/>
						</td>
						<td align="center">
                            &nbsp;<c:out value="${offloadBatch.insertDate}"/>
                           <%-- <fmt:formatDate type="both" value="${offloadBatch.insertDate}" pattern="MM/dd/yyyy hh:mm a"/> --%>
                        </td>
						<td align="center">
                            &nbsp;<c:out value="${offloadBatch.statusChangeDate}"/>
                            <%--<fmt:formatDate type="both" value="${offloadBatch.statusChangeDate}" pattern="MM/dd/yyyy hh:mm a"/>--%>
                        </td>
                    </tr>
                  </c:forEach>                
            </table>
            <br/><br/><br/>
            <span style='color:red'>Note: This simulates the offload process, it does not execute the end-to-end process.</span><br/>
        </td>
      </tr>
    </table>

  </form>
  </body>
</html>