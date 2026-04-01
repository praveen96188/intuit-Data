<%@ page import="com.intuit.sbd.payroll.psp.testtools.TestAdapterAPI" %>
<%@ page import="javax.xml.datatype.XMLGregorianCalendar" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String companyGseq = getCompanyGseq(request);
    boolean isCompanySelected = (companyGseq != null && !companyGseq.equals(""));
%>

<script>
    function allowNavigate() {
        if (!<%=isCompanySelected%>) {
            alert("You must select company first");
        }
        return <%=isCompanySelected%>;
    }
</script>


<table style="background-color:#eeeeee;height:100%" cellpadding="10">
	<tr>
		<td valign="top">
			<b>Test Tools Menu</b><br><br/>
		
			<a href="index.jsp" class="activeLink">Select Company</a>
            <br><br>
            <a href="TestBankSimulator.jsp" class="activeLink">Bank Simulator</a>
            <%--
            <br><br>
			<a href="TestAchBankFileSelector.jsp" class="activeLink">ACH Bank Simulator</a>
			--%>
			<br><br>
			<a href="TestOffloadGroups.jsp" class="activeLink">Offload Groups</a>
			<br><br>
			<a href="TestEditOffloadCutoffTime.jsp" class="activeLink">Edit Offload Cutoff Time</a>
			<br><br>
			<a href="TestRunTransactionExecutor.jsp" class="activeLink">Run Transaction Executor</a>
			<br><br><br/>
			<a href="TestEditCompanyStatuses.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Company Status</a>
			<br><br>
			<a href="TestCompanyBankAccounts.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Company Bank Accounts</a>
            <%--
            <br><br>
			<a href="TestCompanySystemEvents.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Company System Events</a>
			<br><br>
			<a href="TestCreateSystemEvent.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Create System Event</a>
			--%>
			<br><br>
			<a href="TestEditFundingModel.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Funding Model</a>
			<br><br><br/>
            <%--
            <a href="TestStrikes.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Strike Events</a>
			<br><br>
			<a href="TestLimitViolationEvents.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Limit Violation Events</a>
			<br><br>
			<a href="TestAutoLimitIncreaseEvents.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Auto Limit Increase Events</a>
			<br><br>
			--%>
			<a href="TestEditCompanyLimits.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Edit Company Limit</a>
			<br><br>
			<a href="TestEditEmployeeLimit.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Edit Employee Limit</a>
			<br><br><br/>
			<a href="TestPayrollList.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();" >Payroll Runs (Batches)</a>
			<br><br>
			<a href="TestChangeOffloadGroup.jsp" class="<%=isCompanySelected?"activeLink":"inactiveLink"%>" onclick="return allowNavigate();">Change Offload Group</a>
			<br><br>
			<a href="TestBatchJobs.jsp" class="activeLink">Batch Jobs</a>
            <br/><br/>
            <a href="TestScheduleSecondOffload.jsp" class="activeLink">Schedule Second Offload</a>
            <br/><br/>
            <a href="TestCreateDefaultCompany.jsp" class="activeLink">Create Company</a>
            <br/><br/>
            <a href="achFileAnalyser.jsp" class="activeLink">Analyse ACH file</a>
            <br/><br/>
        </td>
	</tr>
</table>

