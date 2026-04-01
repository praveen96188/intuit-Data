package com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.WorkflowFinderConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FileReaderTest {
    private FileReader fileReader;

    @Before
    public void setup() {
        fileReader = new FileReader();
    }

    @Test
    public void read() {
        String filePath = Application.findFileOnClassPath(WorkflowFinderConstants.TEST_WORKFLOW_REPORT_PATH);
        List<String> lines = fileReader.read(filePath);
        Assert.assertEquals(12, lines.size());
        Assert.assertTrue("com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter,addNewUserData".equals(lines.get(0)));
        Assert.assertTrue(",reCalculateLedgerBalances".equals(lines.get(11)));
    }
}
