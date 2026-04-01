package com.intuit.sbd.payroll.psp.common.utils.workflowfinder;

import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess.WorkflowPreProcessor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowFinderTest {

	private WorkflowFinderImpl workflowFinder;
	private WorkflowPreProcessor workflowPreProcessor;

	@Before
	public void setup() {
		workflowPreProcessor = Mockito.mock(WorkflowPreProcessor.class);
		this.workflowFinder = new WorkflowFinderImpl(workflowPreProcessor);
	}

	@Test
	public void getWorkflowHappyPath() {

		Map<Integer, String> digestMap = prepareDigestMap();

		//Fourth line is the matching stacktrace.
		String stackTrace = "	at com.intuit.sbd.payroll.psp.api.PayrollServicesTest.beforeEachTest(PayrollServicesTest.java:106)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper.typicalRunBeforeEachTest(QBDTTestHelper.java:391)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.qbdt.PayrollSubmitTest.runBeforeEachTest(PayrollSubmitTest.java:55)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.sap.adapter.ReportAdapter.getReportList(ReportAdapter.java:99)\n"
				+ "	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
				+ "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)";

		Mockito.when(workflowPreProcessor.getWorkflowDigestMap()).thenReturn(digestMap);
		workflowFinder.init();

		String workflow = workflowFinder.getWorkflowName(stackTrace);
		Assert.assertTrue("com.intuit.sbd.payroll.psp.adapters.sap.adapter.ReportAdapter.getReportList".equals(workflow));

	}

	@Test
	public void getWorkflowEmptyDigest() {

		//No workflows. Empty Digest Map
		Map<Integer, String> digestMap = new HashMap<Integer, String>();

		String stackTrace = "	at com.intuit.sbd.payroll.psp.api.PayrollServicesTest.beforeEachTest(PayrollServicesTest.java:106)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper.typicalRunBeforeEachTest(QBDTTestHelper.java:391)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.qbdt.PayrollSubmitTest.runBeforeEachTest(PayrollSubmitTest.java:55)\n"
				+ "	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
				+ "	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
				+ "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)";

		Mockito.when(workflowPreProcessor.getWorkflowDigestMap()).thenReturn(digestMap);
		workflowFinder.init();

		String workflow = workflowFinder.getWorkflowName(stackTrace);
		Assert.assertTrue(StringUtils.EMPTY.equals(workflow));

	}

	@Test
	public void getWorkflowNoWorkflowFound() {

		//No workflows. Empty Digest Map
		Map<Integer, String> digestMap = new HashMap<Integer, String>();

		//Fourth line is the matching stacktrace.
		String stackTrace = "	at com.intuit.sbd.payroll.psp.api.PayrollServicesTest.beforeEachTest(PayrollServicesTest.java:106)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper.typicalRunBeforeEachTest(QBDTTestHelper.java:391)\n"
				+ "	at com.intuit.sbd.payroll.psp.adapters.qbdt.PayrollSubmitTest.runBeforeEachTest(PayrollSubmitTest.java:55)\n"
				+ "	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
				+ "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)";

		Mockito.when(workflowPreProcessor.getWorkflowDigestMap()).thenReturn(digestMap);
		workflowFinder.init();

		String workflow = workflowFinder.getWorkflowName(stackTrace);
		Assert.assertTrue(StringUtils.EMPTY.equals(workflow));

	}

	private Map<Integer, String> prepareDigestMap() {
		String digestLine1 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.addNewUserData";
		String digestLine2 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.getAllOperations";
		String digestLine3 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.getUsersInDomain";
		String digestLine4 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter.findBookTransferTransactions";
		String digestLine5 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter.cancelBookTransfer";
		String digestLine6 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter.createBookTransfer";
		String digestLine7 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.ViewMyPaycheckAdapter.getEmployeesInfo";
		String digestLine8 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.ReportAdapter.getReportList";

		Map<Integer, String> digestMap = new HashMap<Integer, String>();
		digestMap.put(digestLine1.hashCode(), digestLine1);
		digestMap.put(digestLine2.hashCode(), digestLine2);
		digestMap.put(digestLine3.hashCode(), digestLine3);
		digestMap.put(digestLine4.hashCode(), digestLine4);
		digestMap.put(digestLine5.hashCode(), digestLine5);
		digestMap.put(digestLine6.hashCode(), digestLine6);
		digestMap.put(digestLine7.hashCode(), digestLine7);
		digestMap.put(digestLine8.hashCode(), digestLine8);
		return digestMap;
	}
}
