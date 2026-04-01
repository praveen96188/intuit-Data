/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AddServiceCheckDistributionTests {

    private Company company;

    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void addCheckDistributionServiceSuccess() {
        // Add a company with the right status
        PayrollServices.beginUnitOfWork();

        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> pr = PayrollServices.companyManager.addCompany(companyDTO);
        Company company1 = pr.getResult();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company1);

        // Add CheckDistribution service
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO companyService = dataloader.getTestCheckDistributionCompanyService(1);
        ProcessResult<CompanyService> serviceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), companyService);
        PayrollServices.commitUnitOfWork();

        assertEquals(0, serviceAddProcessResult.getMessages().size());

        // Assert
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.CheckDistribution);

        assertNotNull(serviceInfo);
        assertEquals("Service code", ServiceCode.CheckDistribution, serviceInfo.getService().getServiceCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCheckDistributionServiceForNonAssistedCompany() {
        // Add a company with the wrong MigrationStatus
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        ProcessResult<Company> pr = PayrollServices.companyManager.addCompany(companyDTO);
        Company company1 = pr.getResult();
        PayrollServices.commitUnitOfWork();

        // Add CheckDistribution service
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO companyService = dataloader.getTestCheckDistributionCompanyService(1);
        ProcessResult<CompanyService> serviceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), companyService);
        PayrollServices.commitUnitOfWork();

        assertEquals(1, serviceAddProcessResult.getMessages().size());
        assertEquals("615", serviceAddProcessResult.getMessages().get(0).getMessageCode());
    }

}