package com.intuit.sbd.payroll.psp.batchjobs.ers;

import com.intuit.ems.payroll.psp.gateways.ers.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 20, 2010
 * Time: 12:25:53 PM
 */
public class ActivateEntitlementTests {
    @BeforeClass
    public static void beforeClass() {
        SocketManagerFactory.setInstanceClass(MockSocketManager.class);
    }

    @AfterClass
    public static void afterClass() {
        SocketManagerFactory.setInstanceClass(null);
    }

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.reinitialize();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
        ERSGatewayFactory.setInstanceClass(ERSGateway.class);
    }

    @Test
    public void testActivateEntitlements_HappyPath() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);
        }

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlements_ExceptionForOneCompany() {
        ERSGatewayFactory.setInstanceClass(ERSExceptionGateway.class);

        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);
        }

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        int companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if (companyCount == 5) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                }
            } else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlementUnitsWithEntitlementDisabled_ExceptionForOneCompany() {
        ERSGatewayFactory.setInstanceClass(ERSExceptionGateway.class);

        String psid = "";
        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);

            if (company.getFedTaxId().equals("000000005")) {
                psid = company.getSourceCompanyId();
            }
        }


        PayrollServices.beginUnitOfWork();
        Company comp = Company.findCompany(psid, SourceSystemCode.QBDT);
        Entitlement entitlement = comp.getActiveEntitlementUnits().getFirst().getEntitlement();
        for (EntitlementUnit entitlementUnit : entitlement.getEntitlementUnitCollection()) {
            if (!entitlementUnit.getFedTaxId().equals("000000005")) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
            }
        }
        entitlement.setEntitlementState(EntitlementStateCode.Disabled);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        int companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if (companyCount == 5) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
                    assertEquals("entitlement error count", 1, entitlementUnit.getErrorCount());
                }
            } else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if (companyCount == 5) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
                    assertEquals("entitlement error count", 2, entitlementUnit.getErrorCount());
                }
            } else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if (companyCount == 5) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
                    assertEquals("entitlement error count", 3, entitlementUnit.getErrorCount());
                }
            } else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if (companyCount == 5) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
                    assertEquals("entitlement error count", 4, entitlementUnit.getErrorCount());
                }
            } else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if (companyCount == 5) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorActivating, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
                    assertEquals("entitlement error count", 0, entitlementUnit.getErrorCount());
                }
            } else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlements_ConnectionException() {
        ERSGatewayFactory.setInstanceClass(ERSConnectionExceptionGateway.class);

        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);
        }

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSConnectionExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        int companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if(companyCount < 5){
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
            else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlements_RepeatExceptions() {
        ERSGatewayFactory.setInstanceClass(ERSExceptionGateway.class);

        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);
        }

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        int companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if(companyCount != 5){
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
            else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("error count", 1, entitlementUnit.getErrorCount());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        // run the activation job 4 more times
        for(int i = 0; i < 4; i++) {
            entitlementProcessor =
                    new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");
            entitlementProcessor.execute();
        }

        PayrollServices.beginUnitOfWork();
        companyCount = 0;
        for (Company company : companies) {
            companyCount++;
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            if(companyCount != 5){
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
            else {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorActivating, entitlementUnit.getEntitlementUnitStatus());
                    assertEquals("error count", 0, entitlementUnit.getErrorCount()); //resets to 0 when changing status
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

     @Test
     public void testActivateEntitlements_AssistedHappyPath() {
         ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

         String licenseNumber = "12345678901234567890";
         Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Cloud, ServiceCode.Tax);
         DataLoadServices.activateTaxServiceExceptBalanceFile(company);
         DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321", null, null, DataLoadServices.AssetItemNumber.ASSISTED, null);
         QBDTTestHelper.submitBalanceFile(company, true);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

         EntitlementProcessor ersActivateEntitlementProcessor =
                 new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

         ersActivateEntitlementProcessor.execute();

         PayrollServices.beginUnitOfWork();
         company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
         for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
             assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
         }
         PayrollServices.rollbackUnitOfWork();
     }

    @Test
    public void testActivateEntitlements_DisabledEntitlement() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        String licenseNumber = "12345678901234567890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Disabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorActivating, entitlementUnit.getEntitlementUnitStatus());
            assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlements_EmptyEntitlement() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        String licenseNumber = "12345678901234567890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
            assertEquals("entitlement unit error count", 1, entitlementUnit.getErrorCount());
            assertEquals("entitlement state", EntitlementStateCode.Enabled, entitlementUnit.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlements_EntitlementUnitExistsDeactivated() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        String licenseNumber = "12345678901234567890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
        entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
            assertEquals("entitlement state", EntitlementStateCode.Enabled, entitlementUnit.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActivateEntitlements_DisabledEntitlementInPsp() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        String licenseNumber = "12345678901234567890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        Entitlement entitlement = company.getActiveEntitlementUnits().getFirst().getEntitlement();
        entitlement.setEntitlementState(EntitlementStateCode.Disabled);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Disabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorActivating, entitlementUnit.getEntitlementUnitStatus());
            assertEquals("entitlement state", EntitlementStateCode.Disabled, entitlementUnit.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEntitlementSkippedIfPendingMessage() {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        String licenseNumber = "12345678901234567890";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        EntitlementUnit eu = DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        addMessage(eu.getEntitlement().getLicenseNumber(), eu.getEntitlement().getEntitlementOfferingCode());
        runProcessor();
        assertEntitlementUnitStatus(eu, EntitlementUnitStatusCode.PendingActivation);

        Application.beginUnitOfWork();
        EntitlementMessage entitlementMessage = assertOne(Application.find(EntitlementMessage.class));
        entitlementMessage.setStatus(EntitlementMessageStatusCode.Processed);
        Application.commitUnitOfWork();

        runProcessor();
        assertEntitlementUnitStatus(eu, EntitlementUnitStatusCode.PendingActivation);

        SpcfCalendar pspTime = PSPDate.getPSPTime();
        pspTime.addSeconds(65);
        DataLoadServices.setPSPDate(pspTime);

        runProcessor();
        assertEntitlementUnitStatus(eu, EntitlementUnitStatusCode.Activated);

        PayrollServices.rollbackUnitOfWork();
    }

    private void runProcessor() {
        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");
        ersActivateEntitlementProcessor.execute();
    }

    //verify that the job did or did not process
    private void assertEntitlementUnitStatus(EntitlementUnit pEntitlementUnit, EntitlementUnitStatusCode status) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(pEntitlementUnit);
        assertEquals(status, pEntitlementUnit.getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    private void addMessage(String licenseNumber, String eoc) {
        EntitlementMessageDTO entitlementMessageDTO = new EntitlementMessageDTO();
        entitlementMessageDTO.setLicenseNumber(licenseNumber);
        entitlementMessageDTO.setEntitlementOfferingCode(eoc);
        entitlementMessageDTO.setOrderNumber("12345");
        entitlementMessageDTO.setMessage("This is a message received from AMO.");

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO));
        PayrollServices.commitUnitOfWork();
    }

    @Test @Ignore
    public void test() {
        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        ersActivateEntitlementProcessor.execute();
    }

}
