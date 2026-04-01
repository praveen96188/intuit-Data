/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SourcePayrollParameterBETests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateFoundParam()
    {
        String psid = "1234567";
        
        DataLoadServices.setupCompany(psid);
        Application.beginUnitOfWork();
        
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        FraudValue fraudEEPaidXTimes = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudEEPaidXTimes);

        assertEquals("3", fraudEEPaidXTimes.getValue());
        Application.commitUnitOfWork();
    }

}
