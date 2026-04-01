package com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowDigestHelperTest {

    private WorkflowDigestHelper workflowDigestHelper;

    @Before
    public void init() {
        workflowDigestHelper = new WorkflowDigestHelper();
    }

    @Test
    public void testPreprocessSingleClass() {

        //Input
        String line1 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter,addNewUserData";
        String line2 = ",getAllOperations";
        String line3 = ",getUsersInDomain";
        List<String> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);

        //Expected
        String digestLine1 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.addNewUserData";
        String digestLine2 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.getAllOperations";
        String digestLine3 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.getUsersInDomain";

        Map<Integer, String> digestMap = workflowDigestHelper.preProcess(lines);
        Assert.assertEquals(3, digestMap.size());
        Assert.assertTrue(digestLine1.equals(digestMap.get(digestLine1.hashCode())));
        Assert.assertTrue(digestLine2.equals(digestMap.get(digestLine2.hashCode())));
        Assert.assertTrue(digestLine3.equals(digestMap.get(digestLine3.hashCode())));
    }

    @Test
    public void testPreprocessMultiClass() {

        //Input
        String line1 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter,addNewUserData";
        String line2 = ",getAllOperations";
        String line3 = ",getUsersInDomain";
        String line4 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter,findBookTransferTransactions";
        String line5 = ",cancelBookTransfer";
        String line6 = ",createBookTransfer";
        String line7 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.ViewMyPaycheckAdapter,getEmployeesInfo";
        String line8 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.ReportAdapter,getReportList";
        List<String> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);
        lines.add(line5);
        lines.add(line6);
        lines.add(line7);
        lines.add(line8);

        //Expected
        String digestLine1 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.addNewUserData";
        String digestLine2 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.getAllOperations";
        String digestLine3 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter.getUsersInDomain";
        String digestLine4 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter.findBookTransferTransactions";
        String digestLine5 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter.cancelBookTransfer";
        String digestLine6 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.AccountingAdapter.createBookTransfer";
        String digestLine7 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.ViewMyPaycheckAdapter.getEmployeesInfo";
        String digestLine8 = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.ReportAdapter.getReportList";

        Map<Integer, String> digestMap = workflowDigestHelper.preProcess(lines);
        Assert.assertEquals(8, digestMap.size());
        Assert.assertTrue(digestLine1.equals(digestMap.get(digestLine1.hashCode())));
        Assert.assertTrue(digestLine2.equals(digestMap.get(digestLine2.hashCode())));
        Assert.assertTrue(digestLine3.equals(digestMap.get(digestLine3.hashCode())));
        Assert.assertTrue(digestLine4.equals(digestMap.get(digestLine4.hashCode())));
        Assert.assertTrue(digestLine5.equals(digestMap.get(digestLine5.hashCode())));
        Assert.assertTrue(digestLine6.equals(digestMap.get(digestLine6.hashCode())));
        Assert.assertTrue(digestLine7.equals(digestMap.get(digestLine7.hashCode())));
        Assert.assertTrue(digestLine8.equals(digestMap.get(digestLine8.hashCode())));
        
    }
}
