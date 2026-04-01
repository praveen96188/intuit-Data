package com.intuit.sbd.payroll.psp.domain;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 *
 * User: mvillani
 * Date: Sep 19, 2007
 * Time: 1:12:40 PM

 */
public class PostingRuleBETests {

    private PostingRule postingRule;
    private ProcessResult validateResult;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        postingRule = new PostingRule();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testEligibleGemsTransactionTypes(){
        DomainEntitySet<PostingRule> postingRules;
        PayrollServices.beginUnitOfWork();
        postingRules = PostingRule.findEligibleGemsTransactionTypes(ReportingFrequency.Daily);
        PayrollServices.commitUnitOfWork();
        assertTrue(postingRules.size()!=0);
    }
}
