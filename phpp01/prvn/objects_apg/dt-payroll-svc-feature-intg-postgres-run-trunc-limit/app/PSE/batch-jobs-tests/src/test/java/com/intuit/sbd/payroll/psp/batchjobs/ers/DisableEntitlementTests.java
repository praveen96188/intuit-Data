package com.intuit.sbd.payroll.psp.batchjobs.ers;

import com.intuit.ems.payroll.psp.gateways.ers.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 20, 2010
 * Time: 12:26:10 PM
 */
public class DisableEntitlementTests {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
        ERSGatewayFactory.setInstanceClass(ERSGateway.class);
    }

    @Test
    public void testDisableEntitlements_HappyPath() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Cloud, ServiceCode.Tax);
            DataLoadServices.activateCloudService(company);
            companies.add(company);
        }

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            for (EntitlementUnit entitlementUnit : company.getPrimaryEntitlementUnits()) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
                Application.save(entitlementUnit);
            }
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(121);
        PayrollServices.commitUnitOfWork();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, "1234", "");
        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                assertEquals("entitlement status", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDisableEntitlements_ExceptionForOneCompany() {
        ERSGatewayFactory.setInstanceClass(ERSExceptionGateway.class);

        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Cloud, ServiceCode.Tax);
            DataLoadServices.activateCloudService(company);
            companies.add(company);
        }

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            for (EntitlementUnit entitlementUnit : company.getPrimaryEntitlementUnits()) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
                Application.save(entitlementUnit);
            }
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(121);
        PayrollServices.commitUnitOfWork();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, "1234", "");
        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        companies = companies.sort(Company.SourceCompanyId());
        List<EntitlementUnit> activatedEuList = new ArrayList<EntitlementUnit>();
        List<EntitlementUnit> deactivatedEuList = new ArrayList<EntitlementUnit>();
        int companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                if (entitlementUnit.isActivated()) {
                    activatedEuList.add(entitlementUnit);
                    assertEquals(1, entitlementUnit.getErrorCount());
                } else {
                    deactivatedEuList.add(entitlementUnit);
                }
            }
        }

        assertEquals(1, activatedEuList.size());
        assertEquals(9, deactivatedEuList.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDisableEntitlements_ConnectionException() {
        ERSGatewayFactory.setInstanceClass(ERSConnectionExceptionGateway.class);

        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Cloud, ServiceCode.Tax);
            DataLoadServices.activateCloudService(company);
            companies.add(company);
        }

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            for (EntitlementUnit entitlementUnit : company.getPrimaryEntitlementUnits()) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
                Application.save(entitlementUnit);
            }
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(121);
        PayrollServices.commitUnitOfWork();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSConnectionExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, "1234", "");
        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        companies = companies.sort(Company.SourceCompanyId());
        List<EntitlementUnit> activatedEuList = new ArrayList<EntitlementUnit>();
        List<EntitlementUnit> deactivatedEuList = new ArrayList<EntitlementUnit>();
        int companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                if (entitlementUnit.isActivated()) {
                    activatedEuList.add(entitlementUnit);
                } else {
                    deactivatedEuList.add(entitlementUnit);
                }
            }
        }

        assertEquals(6, activatedEuList.size());
        assertEquals(4, deactivatedEuList.size());

        PayrollServices.rollbackUnitOfWork();
    }
}
