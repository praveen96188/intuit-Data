package com.intuit.sbd.payroll.psp.batchjobs.ers;

import com.intuit.ems.payroll.psp.gateways.ers.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.amo.AMOMessageProcessing;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AMOMessageProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOMockGateway;
import com.intuit.sbd.payroll.psp.gateways.amo.Message;
import com.intuit.sbd.payroll.psp.gateways.amo.TransactionAttribute;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 23, 2010
 * Time: 8:49:54 AM
 */
public class DeactivateEntitlementUnitTests {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.reinitialize();
        AMOMockGateway.getMessages().clear();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
        ERSGatewayFactory.setInstanceClass(ERSGateway.class);
    }

    @Test
    public void testDeactivateEntitlements_EINChange() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        List<Company> companies = new ArrayList<Company>(10);
        for (int i = 0; i<5; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
            companies.add(company);
            if (!entitlementInfoDTO.getEntitlementUnits().containsKey(company.getFedTaxId())) {
                EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
                entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
                entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
            }
        }

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.setFein(String.format("%1$09d", Integer.parseInt(company.getFedTaxId()) + 10));
            ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
            PSP_PRAssert.assertSuccess("update company ProcessResult", processResult);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        Message message = new Message();
        com.intuit.sbd.payroll.psp.gateways.amo.Entitlement entitlement = new com.intuit.sbd.payroll.psp.gateways.amo.Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.cancellationReason = "Cancel yo";
        entitlement.addContactUpdate("Zack", "B", "Norcross", "zack@intuit.com");
        entitlement.addBillingUpdate("08", "2010", "1234567891111", "VISA");
        // basic unlimited subtype
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));


        entitlement.addEntitlementUnitUpdates(new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(companies.get(0).getFedTaxId(), com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit.ACTIVATED));
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        entitlement.entitlementState = com.intuit.sbd.payroll.psp.gateways.amo.Entitlement.ENABLED;
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        AMOMessageProcessor amoMessageProcessing =
                new AMOMessageProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AMOMessageProcessor, "1234", "");
        amoMessageProcessing.execute();
        amoMessageProcessing =
                new AMOMessageProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AMOMessageProcessor, "12345", "");
        amoMessageProcessing.execute();


        PayrollServices.beginUnitOfWork();
        EntitlementUnit eu = EntitlementUnit.findEntitlementUnits(companies.get(0).getFedTaxId(), licenseNumber, eoc).get(0);
        assertEquals(EntitlementUnitStatusCode.PendingDeactivation, eu.getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar pspTime = PSPDate.getPSPTime();
        pspTime.addMinutes(2);
        DataLoadServices.setPSPDate(pspTime);

        entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");
        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.setFein(String.format("%1$09d", Integer.parseInt(company.getFedTaxId()) - 10));
            ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
            PSP_PRAssert.assertSuccess("update company ProcessResult", processResult);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingReactivation, entitlementUnit.getEntitlementUnitStatus());
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();


        entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDeactivateEntitlements_ExceptionForOneCompany() {
        ERSGatewayFactory.setInstanceClass(ERSExceptionGateway.class);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(9);
        for (int i = 0; i<9; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);
            if (!entitlementInfoDTO.getEntitlementUnits().containsKey(company.getFedTaxId())) {
                EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
                entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
                entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
            }
        }

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, "1234", "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 5) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.setFein(String.format("%1$09d", Integer.parseInt(company.getFedTaxId()) + 10));
            ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
            PSP_PRAssert.assertSuccess("update company ProcessResult", processResult);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 5) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                    } else {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
                    }
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();


        entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 6) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
                    } else {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                    }
                } else {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 15) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                        assertEquals("entitlement unit error count", 1, entitlementUnit.getErrorCount());
                    } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDeactivateEntitlements_RepeatedExceptions() {
        ERSGatewayFactory.setInstanceClass(ERSExceptionGateway.class);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        ERSExceptionGateway.setEntitlementDTO(entitlementInfoDTO);

        String licenseNumber = "12345678901234567890";
        List<Company> companies = new ArrayList<Company>(9);
        for (int i = 0; i<9; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, "09876543210987654321");
            companies.add(company);
            if (!entitlementInfoDTO.getEntitlementUnits().containsKey(company.getFedTaxId())) {
                EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
                entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
                entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
            }
        }

        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, "1234", "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 5) {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.setFein(String.format("%1$09d", Integer.parseInt(company.getFedTaxId()) + 10));
            ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
            PSP_PRAssert.assertSuccess("update company ProcessResult", processResult);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 5) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                    } else {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
                    }
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();


        entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        entitlementProcessor.execute();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 6) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
                    } else {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                    }
                } else {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 15) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
                        assertEquals("entitlement unit error count", 1, entitlementUnit.getErrorCount());
                    } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        }
        PayrollServices.rollbackUnitOfWork();

        for(int i = 0; i < 4; i++) {
            entitlementProcessor =
                    new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");
            entitlementProcessor.execute();
        }

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
                if(Integer.parseInt(entitlementUnit.getFedTaxId()) < 10) {
                    if(Integer.parseInt(entitlementUnit.getFedTaxId()) == 6) {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorDeactivating, entitlementUnit.getEntitlementUnitStatus());
                    } else {
                        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
                    }
                } else {
                    assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDeactivateEntitlement() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        String licenseNumber = "lic1";
        String eoc = "eoc1";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
        entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            Assert.assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId()));

        // update entitlement unit status to activated (equivalent of calling a Entitlement batch job)
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);

        ProcessResult processResult=PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Error activating entitlement", processResult);

        new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "").execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            assertEquals("Incorrect entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
        }
        PayrollServices.rollbackUnitOfWork();

        /*  Now for the deactivation    */
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId()));

        // update entitlement unit status to deactivated (equivalent of calling a Entitlement batch job)
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);

        processResult=PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Error deactivating entitlement", processResult);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            Assert.assertEquals("Incorrect entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
        }
        PayrollServices.rollbackUnitOfWork();

        new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "").execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            Assert.assertEquals("Incorrect entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDeactivateEntitlementERSEntitlementDisabled() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        String licenseNumber = "lic1";
        String eoc = "eoc1";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Disabled);
        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
        entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        /*  Now for the deactivation    */
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId());
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        Application.save(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "").execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit eu : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            Assert.assertEquals("Incorrect entitlement unit status", EntitlementUnitStatusCode.ErrorDeactivating, eu.getEntitlementUnitStatus());
            Assert.assertEquals("Incorrect entitlement state", EntitlementStateCode.Disabled, eu.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDeactivateEntitlementERSEntitlementEnabledEUDeactivated() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        String licenseNumber = "lic1";
        String eoc = "eoc1";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
        entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        /*  Now for the deactivation    */
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId());
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        Application.save(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "").execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit eu : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            Assert.assertEquals("Incorrect entitlement unit status", EntitlementUnitStatusCode.Deactivated, eu.getEntitlementUnitStatus());
            Assert.assertEquals("Incorrect entitlement state", EntitlementStateCode.Enabled, eu.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDeactivateEntitlementERSEntitlementEmptyEUDeactivated() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        String licenseNumber = "lic1";
        String eoc = "eoc1";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        //entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
        entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        /*  Now for the deactivation    */
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId());
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        Application.save(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "").execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit eu : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber))) {
            Assert.assertEquals("Incorrect entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, eu.getEntitlementUnitStatus());
            Assert.assertEquals("Incorrect entitlement unit error count", 1, eu.getErrorCount());
            Assert.assertEquals("Incorrect entitlement state", EntitlementStateCode.Enabled, eu.getEntitlement().getEntitlementState());
        }
        PayrollServices.rollbackUnitOfWork();
    }
}
