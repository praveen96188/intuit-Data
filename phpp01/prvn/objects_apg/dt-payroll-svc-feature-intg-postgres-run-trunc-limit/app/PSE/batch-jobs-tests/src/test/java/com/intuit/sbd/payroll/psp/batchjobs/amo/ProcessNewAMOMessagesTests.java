package com.intuit.sbd.payroll.psp.batchjobs.amo;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.ERSMockGateway;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementUnitInfoDTO;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetChangeReasonType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetStatusType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AMOMessageProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.amo.*;
import com.intuit.sbd.payroll.psp.gateways.amo.Entitlement;
import com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 9, 2010
 * Time: 1:52:31 PM
 */
public class ProcessNewAMOMessagesTests {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        AMOMockGateway.clearMessages();
        deleteMessageFiles();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
        AMOGatewayFactory.setInstanceClass(AMOGateway.class);
    }

    private void deleteMessageFiles() {
        File messageDir = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir"));
        File[] files = messageDir.listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Test
    public void testProcessNewMessages_ContactBillingEntitlementUnitUpdates() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);        

        List<Company> companies = new ArrayList<Company>(5);
        for (int i = 0; i<5; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
            companies.add(company);
        }

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.cancellationReason = "Cancel yo";
        entitlement.addContactUpdate("Zack", "B", "Norcross", "zack@intuit.com");
        entitlement.addBillingUpdate("08", "2010", "1234567891111", "VISA");
        // basic unlimited subtype
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        entitlement.entitlementState = Entitlement.ENABLED;
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        //assertEquals("cc exp", "08/2010", domainEntitlement.getCreditCardExpiration());
        //assertEquals("cc num", "1111", domainEntitlement.getCreditCardNumber());
        //assertEquals("cc type", "VISA", domainEntitlement.getCreditCardType());
        //assertEquals("payment type", EntitlementPaymentMethodType.CC, domainEntitlement.getPaymentMethodType());
        assertEquals("entitlement code", 11, domainEntitlement.getEntitlementCode().getQuickBooksSubtype());
        assertEquals("email", "zack@intuit.com", domainEntitlement.getContactEmail());
        assertEquals("contact name", "Zack B Norcross", domainEntitlement.getContactName());
        assertEquals("entitlement state", Entitlement.ENABLED, domainEntitlement.getEntitlementState().toString());
        assertEquals("order number", "12345", domainEntitlement.getOrderNumber());
        assertEquals("next charge date", "2010/10/20 07:00:00.0", domainEntitlement.getNextChargeDate().toString());
        assertEquals("customer id", "Customer1", domainEntitlement.getCustomerId());
        assertEquals("billing zip", "89511", domainEntitlement.getBillingZipCode());
        assertEquals("cancel reason", "Cancel yo", domainEntitlement.getCancellationReason());

        for (Company company : companies) {
            Application.refresh(company);
            assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_BlankCAN() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        List<Company> companies = new ArrayList<Company>(5);
        for (int i = 0; i<5; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
            companies.add(company);
        }

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "", "89511");
        entitlement.cancellationReason = "Cancel yo";
        entitlement.addContactUpdate("Zack", "B", "Norcross", "zack@intuit.com");
        entitlement.addBillingUpdate("08", "2010", "1234567891111", "VISA");
        // basic unlimited subtype
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        entitlement.entitlementState = Entitlement.ENABLED;
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        //assertEquals("cc exp", "08/2010", domainEntitlement.getCreditCardExpiration());
        //assertEquals("cc num", "1111", domainEntitlement.getCreditCardNumber());
        //assertEquals("cc type", "VISA", domainEntitlement.getCreditCardType());
        //assertEquals("payment type", EntitlementPaymentMethodType.CC, domainEntitlement.getPaymentMethodType());
        assertEquals("entitlement code", 11, domainEntitlement.getEntitlementCode().getQuickBooksSubtype());
        assertEquals("email", "zack@intuit.com", domainEntitlement.getContactEmail());
        assertEquals("contact name", "Zack B Norcross", domainEntitlement.getContactName());
        assertEquals("entitlement state", Entitlement.ENABLED, domainEntitlement.getEntitlementState().toString());
        assertEquals("order number", "12345", domainEntitlement.getOrderNumber());
        assertEquals("next charge date", "2010/10/20 07:00:00.0", domainEntitlement.getNextChargeDate().toString());
        assertEquals("customer id", "CustomerId", domainEntitlement.getCustomerId());
        assertEquals("billing zip", "89511", domainEntitlement.getBillingZipCode());
        assertEquals("cancel reason", "Cancel yo", domainEntitlement.getCancellationReason());

        for (Company company : companies) {
            Application.refresh(company);
            assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_EntitlementUnitStatusUpdates() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.DEACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending activation -> deactivated not an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        // update first company to activated
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.ACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 1, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 2, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending activation -> activated is an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // manually update entitlement unit to error activating
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        Application.save(company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0));
        PayrollServices.commitUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.DEACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 2, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 3, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // error activating -> deactivated not an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorActivating, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        // update first company to activated
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.ACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 3, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 4, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending activation -> activated is an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // manually update entitlement unit to pending reactivation
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);
        Application.save(company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0));
        PayrollServices.commitUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.DEACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 4, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 5, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending reactivation -> deactivated not an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingReactivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        // update first company to activated
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.ACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 5, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 6, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending activation -> activated is an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Activated, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // manually update entitlement unit to pending deactivation
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        Application.save(company.getEntitlementUnitCollection().get(0));
        PayrollServices.commitUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.ACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 6, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 7, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending deactivation -> activated not an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        // update first company to activated
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.DEACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 7, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 8, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // pending deactivation -> deactivated is an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // manually update entitlement unit to error activating
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        Application.save(company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0));
        PayrollServices.commitUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.ACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 8, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 9, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // error deactivating -> activated not an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.ErrorDeactivating, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        // update update company to deactivated
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit(company.getFedTaxId(), EntitlementUnit.DEACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 9, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 10, 0, 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // error deactivating -> deactivated is an allowed transition
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_ContactUpdates() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate(null, null, null, "zack@intuit.com");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("cc exp", domainEntitlement.getCreditCardExpiration());
        assertNull("cc num", domainEntitlement.getCreditCardNumber());
        assertNull("cc type", domainEntitlement.getCreditCardType());
        assertNull("payment type", domainEntitlement.getPaymentMethodType());
        assertNotNull("next charge date", domainEntitlement.getNextChargeDate());

        assertEquals("email", "zack@intuit.com", domainEntitlement.getContactEmail());

        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_ContactUpdates_InvalidEmail() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate(null, null, null, "invalidemail@intuit.com@intuit.com");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("cc exp", domainEntitlement.getCreditCardExpiration());
        assertNull("cc num", domainEntitlement.getCreditCardNumber());
        assertNull("cc type", domainEntitlement.getCreditCardType());
        assertNull("payment type", domainEntitlement.getPaymentMethodType());
        assertNotNull("next charge date", domainEntitlement.getNextChargeDate());

        assertEquals("email", "invalidemail@intuit.com@intuit.com", domainEntitlement.getContactEmail());

        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_BillingUpdates() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addBillingUpdate("08", "2010", "1234567891111", "VISA");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        //assertEquals("cc exp", "08/2010", domainEntitlement.getCreditCardExpiration());
        //assertEquals("cc num", "1111", domainEntitlement.getCreditCardNumber());
        //assertEquals("cc type", "VISA", domainEntitlement.getCreditCardType());
        //assertEquals("payment type", EntitlementPaymentMethodType.CC, domainEntitlement.getPaymentMethodType());
        assertEquals("order number", "12345", domainEntitlement.getOrderNumber());

        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_BillingUpdatesWithEmptyCCAccountNumber() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addBillingUpdate("06", "2014", "", "VISA");
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        //assertEquals("cc exp", "06/2014", domainEntitlement.getCreditCardExpiration());
        assertEquals("cc num", null, domainEntitlement.getCreditCardNumber());
        //assertEquals("cc type", "VISA", domainEntitlement.getCreditCardType());
        //assertEquals("payment type", EntitlementPaymentMethodType.CC, domainEntitlement.getPaymentMethodType());
        assertEquals("order number", "12345", domainEntitlement.getOrderNumber());

        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testProcessNewMessages_EntitlementUnitUpdates() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        List<Company> companies = new ArrayList<Company>(5);
        for (int i = 0; i<5; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
            companies.add(company);
        }

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        // basic unlimited subtype
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        // update first company to deactivated
        entitlement.addEntitlementUnitUpdates(new EntitlementUnit("000000001", EntitlementUnit.DEACTIVATED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertEquals("entitlement code", 11, domainEntitlement.getEntitlementCode().getQuickBooksSubtype());

        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
            if(company.getSourceCompanyId().equals("1")) {
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, company.getEntitlementUnitCollection().get(0).getEntitlementUnitStatus());
            } else {
                assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0).getEntitlementUnitStatus());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_NextBillingDateUpdate() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertEquals("next charge date", "2010/10/20 07:00:00.0", domainEntitlement.getNextChargeDate().toString());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_SaveUnmatchedMessage() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
    }

    @Test
    public void testProcessNewMessages_ExceptionWhileProcessingUpdateMessage() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), "Does not exist"),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
    }

    @Test
    public void testProcessNewMessages_SaveInvalidEntitlementUpdateMessage() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.ASSISTED, "Customer1", "89511");
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
    }

    /**
     * Test whether email from valid role asset is picked
     * by entitlement update and the one with deleted role is ignored
     */
    @Test
    public void testInvalidRoleEmailEntitlementUpdateMessage() {
        String licenseNumber = "411267075338416";
        String eoc = "087689";
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <ns2:syncCustomerAssetDataAreaType xmlns:ns2=\"http://www.intuit.com/iep/CustomerAsset/IntuitCustomerAssetABO/V1\" xmlns:ns4=\"http://www.intuit.com/iep/entitlement/EntitlementService/xsd\" xmlns:ns3=\"http://www.intuit.com/iep/BillingProfile/BillingProfileABO/V1\"> <ns2:TransactionInfo> <ns2:TransactionId></ns2:TransactionId> <ns2:TransactionDatetime>2018-12-05T01:47:32-08:00</ns2:TransactionDatetime> <ns2:SalesOrganization></ns2:SalesOrganization> </ns2:TransactionInfo> <ns2:SyncCustomerAsset> <ns2:CustomerAccount> <CustomerAccount> <BusinessCustomerAccount> <CustomerAccountNumber>139470266</CustomerAccountNumber> <CustomerAccountName>TestSiebelEncPDS2</CustomerAccountName> <AccountSiteName>TestSiebelEncPDS2</AccountSiteName> <CustomerAccountClass>Customer</CustomerAccountClass> <Business> <BusinessName>TestSiebelEncPDS2</BusinessName> <Address> <PrimaryAddress> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </PrimaryAddress> <PrimaryShipToAddress> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </PrimaryShipToAddress> <PrimaryBillToAddress> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </PrimaryBillToAddress> </Address> <Telephone> <MainPhoneNumber>4563473481</MainPhoneNumber> </Telephone> </Business> <PrimaryCustomerAccountContactId SchemeName=\"MDM\">18135844</PrimaryCustomerAccountContactId> </BusinessCustomerAccount> </CustomerAccount> <CustomerAccountContact> <CustomerAccountContactId SchemeName=\"MDM\">18135843</CustomerAccountContactId> <ContactPerson> <Person> <GivenName>Gene</GivenName> <FamilyName>Fleming</FamilyName> <Address> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </Address> <Telephone> <HomeNumber>5639845385</HomeNumber> </Telephone> <EmailAddress> <MainEmailAddress>contact1@email.com</MainEmailAddress> </EmailAddress> </Person> </ContactPerson> <ContactRole> <RoleName>ACCOUNT ADMIN</RoleName> </ContactRole> <ContactRole> <RoleName>DELETED</RoleName> </ContactRole> <CommunicationPreferences> <Email> <Language>eng</Language> </Email> </CommunicationPreferences> </CustomerAccountContact> <CustomerAccountContact> <CustomerAccountContactId SchemeName=\"MDM\">18135844</CustomerAccountContactId> <ContactPerson> <Person> <GivenName>Amy</GivenName> <FamilyName>Datson</FamilyName> <Address> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </Address> <Telephone> <HomeNumber>5639815385</HomeNumber> </Telephone> <EmailAddress> <MainEmailAddress>contact2@email.com</MainEmailAddress> </EmailAddress> </Person> </ContactPerson> <ContactRole> <RoleName>ACCOUNT ADMIN</RoleName> </ContactRole> <CommunicationPreferences> <Email> <Language>eng</Language> </Email> </CommunicationPreferences> </CustomerAccountContact> </ns2:CustomerAccount> <ns2:BillingProfile> <ns3:BillingAccountNumber>139470266</ns3:BillingAccountNumber> <ns3:BillingProfileId SchemeName=\"Siebel\">PDS-GZVN7KC</ns3:BillingProfileId> <ns3:BillingProfileContactId SchemeName=\"MDM\">18135843</ns3:BillingProfileContactId> <ns3:BillingProfileAddressId SchemeName=\"MDM\">16313323</ns3:BillingProfileAddressId> <ns3:PaymentMethod> <CCInfo> <AccountType>Visa</AccountType> <AccountNumber>************4448</AccountNumber> <AccountName>Gene Fleming</AccountName> <ExpirationMonth>5</ExpirationMonth> <ExpirationYear>2027</ExpirationYear> </CCInfo> </ns3:PaymentMethod> <ns3:BillingDayOfMonth>8</ns3:BillingDayOfMonth> </ns2:BillingProfile> <ns2:BillingProfile> <ns3:BillingAccountNumber>139470266</ns3:BillingAccountNumber> <ns3:BillingProfileId SchemeName=\"Siebel\">PDS-IEGOSYM</ns3:BillingProfileId> <ns3:BillingProfileContactId SchemeName=\"MDM\">18135843</ns3:BillingProfileContactId> <ns3:BillingProfileAddressId SchemeName=\"MDM\">16313323</ns3:BillingProfileAddressId> <ns3:PaymentMethod> <CCInfo> <AccountType>Visa</AccountType> <AccountNumber>************4448</AccountNumber> <AccountName>Gene Fleming</AccountName> <ExpirationMonth>4</ExpirationMonth> <ExpirationYear>2031</ExpirationYear> </CCInfo> </ns3:PaymentMethod> <ns3:BillingDayOfMonth>25</ns3:BillingDayOfMonth> </ns2:BillingProfile> <ns2:BillingProfile> <ns3:BillingAccountNumber>139470266</ns3:BillingAccountNumber> <ns3:BillingProfileId SchemeName=\"Siebel\">PDS-H34FQ66</ns3:BillingProfileId> <ns3:BillingProfileContactId SchemeName=\"MDM\">18135843</ns3:BillingProfileContactId> <ns3:BillingProfileAddressId SchemeName=\"MDM\">16313323</ns3:BillingProfileAddressId> <ns3:PaymentMethod> <CCInfo> <AccountType>Visa</AccountType> <AccountNumber>************4448</AccountNumber> <AccountName>Gene Fleming</AccountName> <ExpirationMonth>4</ExpirationMonth> <ExpirationYear>2030</ExpirationYear> </CCInfo> </ns3:PaymentMethod> <ns3:BillingDayOfMonth>25</ns3:BillingDayOfMonth> </ns2:BillingProfile> <ns2:Asset> <ns2:ServiceAccountNumber>139470266</ns2:ServiceAccountNumber> <ns2:AssetContactId SchemeName=\"MDM\">18135843</ns2:AssetContactId> <ns2:BillingProfileId SchemeName=\"Siebel\">PDS-GZVN7KC</ns2:BillingProfileId> <ns2:Item> <Id schemeName=\"PIM\">1099581</Id> </ns2:Item> <ns2:TransactionAttributes> <Name>PURCHASED UNITS</Name> <Value>1</Value> </ns2:TransactionAttributes> <ns2:TransactionAttributes> <Name>SECONDARY ENABLED TIME MINUTES</Name> <Value>1</Value> </ns2:TransactionAttributes> <ns2:TransactionAttributes> <Name>GRACE UNITS</Name> <Value>9999</Value> </ns2:TransactionAttributes> <ns2:TransactionAttributes> <Name>Intuit</Name> <Value>Intuit</Value> </ns2:TransactionAttributes> <ns2:Status>Active</ns2:Status> <ns2:EntitlementOfferingGroup> <ns4:Name>AssistedPay</ns4:Name> </ns2:EntitlementOfferingGroup> <ns2:Entitlement> <ns2:LicenseId SchemeName=\"ERS\">411267075338416</ns2:LicenseId> <ns2:EntitlementId SchemeName=\"ERS\">087689</ns2:EntitlementId> <ns2:EntitlementStartDate>2018-12-05</ns2:EntitlementStartDate> </ns2:Entitlement> <ns2:OrderInfo> <ns2:OrderNumber>2000020767968</ns2:OrderNumber> <ns2:OrderSource>SIEBEL</ns2:OrderSource> <ns2:LineItem> <ns2:LineNumber>1</ns2:LineNumber> <ns2:OrderQuantity>1</ns2:OrderQuantity> <ns2:ActionCode>ADD</ns2:ActionCode> </ns2:LineItem> <ns2:OrderDate>2018-12-05T01:47:27.211-08:00</ns2:OrderDate> </ns2:OrderInfo> <ns2:Registration> <ns2:RegistrationStatus>Registered</ns2:RegistrationStatus> <ns2:RegistrationDate>2018-12-05T01:47:27.175-08:00</ns2:RegistrationDate> </ns2:Registration> </ns2:Asset> </ns2:SyncCustomerAsset> <ns2:Event> <ns2:EventReason>EntitlementCreation</ns2:EventReason> </ns2:Event> </ns2:syncCustomerAssetDataAreaType>";
        String outputDir = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir");
        try {
            FileWriter fileWriter = new FileWriter(outputDir + File.separator + UUID.randomUUID().toString() + ".txt");
            fileWriter.write(message);
            fileWriter.close();
        } catch (IOException e) {

        }
        //Process New Message
        runAMOProcessor();
        //Process Saved Message
        runAMOProcessor();
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement sourceEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        boolean isOOPFixEnabled=FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_OOP_FIX_ENABLED,false);
        if(isOOPFixEnabled){
            assertEquals("Email Id Matches- OOP Fix Enabled",sourceEntitlement.getContactEmail(),"contact@email.com");
        }else{
            assertEquals("Email Id Matches- OOP Fix Disabled",sourceEntitlement.getContactEmail(),"contact1@email.com");
        }
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Test whether email from valid role asset is picked by entitlement update
     */
    @Test
    public void testValidRoleEmailEntitlementUpdateMessage() {
        String licenseNumber = "411267075338416";
        String eoc = "087689";
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        String message="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <ns2:syncCustomerAssetDataAreaType xmlns:ns2=\"http://www.intuit.com/iep/CustomerAsset/IntuitCustomerAssetABO/V1\" xmlns:ns4=\"http://www.intuit.com/iep/entitlement/EntitlementService/xsd\" xmlns:ns3=\"http://www.intuit.com/iep/BillingProfile/BillingProfileABO/V1\"> <ns2:TransactionInfo> <ns2:TransactionId></ns2:TransactionId> <ns2:TransactionDatetime>2018-12-05T01:47:32-08:00</ns2:TransactionDatetime> <ns2:SalesOrganization></ns2:SalesOrganization> </ns2:TransactionInfo> <ns2:SyncCustomerAsset> <ns2:CustomerAccount> <CustomerAccount> <BusinessCustomerAccount> <CustomerAccountNumber>139470266</CustomerAccountNumber> <CustomerAccountName>TestSiebelEncPDS2</CustomerAccountName> <AccountSiteName>TestSiebelEncPDS2</AccountSiteName> <CustomerAccountClass>Customer</CustomerAccountClass> <Business> <BusinessName>TestSiebelEncPDS2</BusinessName> <Address> <PrimaryAddress> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </PrimaryAddress> <PrimaryShipToAddress> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </PrimaryShipToAddress> <PrimaryBillToAddress> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </PrimaryBillToAddress> </Address> <Telephone> <MainPhoneNumber>4563473481</MainPhoneNumber> </Telephone> </Business> <PrimaryCustomerAccountContactId SchemeName=\"MDM\">18135844</PrimaryCustomerAccountContactId> </BusinessCustomerAccount> </CustomerAccount> <CustomerAccountContact> <CustomerAccountContactId SchemeName=\"MDM\">18135843</CustomerAccountContactId> <ContactPerson> <Person> <GivenName>Gene</GivenName> <FamilyName>Fleming</FamilyName> <Address> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </Address> <Telephone> <HomeNumber>5639845385</HomeNumber> </Telephone> <EmailAddress> <MainEmailAddress>contact1@email.com</MainEmailAddress> </EmailAddress> </Person> </ContactPerson> <ContactRole> <RoleName>ACCOUNT ADMIN</RoleName> </ContactRole> <CommunicationPreferences> <Email> <Language>eng</Language> </Email> </CommunicationPreferences> </CustomerAccountContact> <CustomerAccountContact> <CustomerAccountContactId SchemeName=\"MDM\">18135844</CustomerAccountContactId> <ContactPerson> <Person> <GivenName>Amy</GivenName> <FamilyName>Datson</FamilyName> <Address> <AddressId SchemeName=\"MDM\">16313323</AddressId> <PostalAddress> <AddressLine>3341 Yorkie Lane</AddressLine> <City>VIDALIA</City> <StateOrProvince>GA</StateOrProvince> <Country>US</Country> <PostalCode>30474</PostalCode> </PostalAddress> </Address> <Telephone> <HomeNumber>5639815385</HomeNumber> </Telephone> <EmailAddress> <MainEmailAddress>contact2@email.com</MainEmailAddress> </EmailAddress> </Person> </ContactPerson> <ContactRole> <RoleName>ACCOUNT ADMIN</RoleName> </ContactRole> <CommunicationPreferences> <Email> <Language>eng</Language> </Email> </CommunicationPreferences> </CustomerAccountContact> </ns2:CustomerAccount> <ns2:BillingProfile> <ns3:BillingAccountNumber>139470266</ns3:BillingAccountNumber> <ns3:BillingProfileId SchemeName=\"Siebel\">PDS-GZVN7KC</ns3:BillingProfileId> <ns3:BillingProfileContactId SchemeName=\"MDM\">18135843</ns3:BillingProfileContactId> <ns3:BillingProfileAddressId SchemeName=\"MDM\">16313323</ns3:BillingProfileAddressId> <ns3:PaymentMethod> <CCInfo> <AccountType>Visa</AccountType> <AccountNumber>************4448</AccountNumber> <AccountName>Gene Fleming</AccountName> <ExpirationMonth>5</ExpirationMonth> <ExpirationYear>2027</ExpirationYear> </CCInfo> </ns3:PaymentMethod> <ns3:BillingDayOfMonth>8</ns3:BillingDayOfMonth> </ns2:BillingProfile> <ns2:BillingProfile> <ns3:BillingAccountNumber>139470266</ns3:BillingAccountNumber> <ns3:BillingProfileId SchemeName=\"Siebel\">PDS-IEGOSYM</ns3:BillingProfileId> <ns3:BillingProfileContactId SchemeName=\"MDM\">18135843</ns3:BillingProfileContactId> <ns3:BillingProfileAddressId SchemeName=\"MDM\">16313323</ns3:BillingProfileAddressId> <ns3:PaymentMethod> <CCInfo> <AccountType>Visa</AccountType> <AccountNumber>************4448</AccountNumber> <AccountName>Gene Fleming</AccountName> <ExpirationMonth>4</ExpirationMonth> <ExpirationYear>2031</ExpirationYear> </CCInfo> </ns3:PaymentMethod> <ns3:BillingDayOfMonth>25</ns3:BillingDayOfMonth> </ns2:BillingProfile> <ns2:BillingProfile> <ns3:BillingAccountNumber>139470266</ns3:BillingAccountNumber> <ns3:BillingProfileId SchemeName=\"Siebel\">PDS-H34FQ66</ns3:BillingProfileId> <ns3:BillingProfileContactId SchemeName=\"MDM\">18135843</ns3:BillingProfileContactId> <ns3:BillingProfileAddressId SchemeName=\"MDM\">16313323</ns3:BillingProfileAddressId> <ns3:PaymentMethod> <CCInfo> <AccountType>Visa</AccountType> <AccountNumber>************4448</AccountNumber> <AccountName>Gene Fleming</AccountName> <ExpirationMonth>4</ExpirationMonth> <ExpirationYear>2030</ExpirationYear> </CCInfo> </ns3:PaymentMethod> <ns3:BillingDayOfMonth>25</ns3:BillingDayOfMonth> </ns2:BillingProfile> <ns2:Asset> <ns2:ServiceAccountNumber>139470266</ns2:ServiceAccountNumber> <ns2:AssetContactId SchemeName=\"MDM\">18135843</ns2:AssetContactId> <ns2:BillingProfileId SchemeName=\"Siebel\">PDS-GZVN7KC</ns2:BillingProfileId> <ns2:Item> <Id schemeName=\"PIM\">1099581</Id> </ns2:Item> <ns2:TransactionAttributes> <Name>PURCHASED UNITS</Name> <Value>1</Value> </ns2:TransactionAttributes> <ns2:TransactionAttributes> <Name>SECONDARY ENABLED TIME MINUTES</Name> <Value>1</Value> </ns2:TransactionAttributes> <ns2:TransactionAttributes> <Name>GRACE UNITS</Name> <Value>9999</Value> </ns2:TransactionAttributes> <ns2:TransactionAttributes> <Name>Intuit</Name> <Value>Intuit</Value> </ns2:TransactionAttributes> <ns2:Status>Active</ns2:Status> <ns2:EntitlementOfferingGroup> <ns4:Name>AssistedPay</ns4:Name> </ns2:EntitlementOfferingGroup> <ns2:Entitlement> <ns2:LicenseId SchemeName=\"ERS\">411267075338416</ns2:LicenseId> <ns2:EntitlementId SchemeName=\"ERS\">087689</ns2:EntitlementId> <ns2:EntitlementStartDate>2018-12-05</ns2:EntitlementStartDate> </ns2:Entitlement> <ns2:OrderInfo> <ns2:OrderNumber>2000020767968</ns2:OrderNumber> <ns2:OrderSource>SIEBEL</ns2:OrderSource> <ns2:LineItem> <ns2:LineNumber>1</ns2:LineNumber> <ns2:OrderQuantity>1</ns2:OrderQuantity> <ns2:ActionCode>ADD</ns2:ActionCode> </ns2:LineItem> <ns2:OrderDate>2018-12-05T01:47:27.211-08:00</ns2:OrderDate> </ns2:OrderInfo> <ns2:Registration> <ns2:RegistrationStatus>Registered</ns2:RegistrationStatus> <ns2:RegistrationDate>2018-12-05T01:47:27.175-08:00</ns2:RegistrationDate> </ns2:Registration> </ns2:Asset> </ns2:SyncCustomerAsset> <ns2:Event> <ns2:EventReason>EntitlementCreation</ns2:EventReason> </ns2:Event> </ns2:syncCustomerAssetDataAreaType>";
        String outputDir = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir");
        try {
            FileWriter fileWriter = new FileWriter(outputDir + File.separator + UUID.randomUUID().toString() + ".txt");
            fileWriter.write(message);
            fileWriter.close();
        } catch (IOException e) {

        }
        //Process New Message
        runAMOProcessor();
        //Process Saved Message
        runAMOProcessor();
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement sourceEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        boolean isOOPFixEnabled=FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_OOP_FIX_ENABLED,false);
        if(isOOPFixEnabled){
            assertEquals("Email Id Matches- OOP Fix Enabled",sourceEntitlement.getContactEmail(),"contact@email.com");
        }else{
            assertEquals("Email Id Matches- OOP Fix Disabled",sourceEntitlement.getContactEmail(),"contact1@email.com");
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_EntitlementTransfer() {
        String sourceLicenseNumber = "12345678901234567890";
        String targetLicenseNumber = "789417";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString(), sourceLicenseNumber, targetLicenseNumber);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, sourceLicenseNumber, eoc);

        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
        entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EntitlementProcessor ersActivateEntitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, "1234", "");
        ersActivateEntitlementProcessor.execute();

        runAMOProcessor();
        assertEntitlementMessages(sourceLicenseNumber, null, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(sourceLicenseNumber, null, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement sourceEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(sourceLicenseNumber, eoc);
        assertEquals("source entitlement state", EntitlementStateCode.Disabled, sourceEntitlement.getEntitlementState());
        for (com.intuit.sbd.payroll.psp.domain.EntitlementUnit entitlementUnit : sourceEntitlement.getEntitlementUnitCollection()) {
            assertEquals("source entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
        }

        com.intuit.sbd.payroll.psp.domain.Entitlement targetEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(targetLicenseNumber, eoc);
        assertEquals("target entitlement state", EntitlementStateCode.Enabled, targetEntitlement.getEntitlementState());
        for (com.intuit.sbd.payroll.psp.domain.EntitlementUnit entitlementUnit : targetEntitlement.getEntitlementUnitCollection()) {
            assertEquals("target entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_SubscriptionEndDate() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_ENDING_NOTICE;
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("subscription end date", domainEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_transactionDates() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        message.transactionDate = SpcfCalendar.createInstance(2010, 10, 20);
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_ENDING_NOTICE;
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("subscription end date", domainEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();

        // date before last update ignore it
        message = new Message();
        message.transactionDate = SpcfCalendar.createInstance(2010, 9, 20);
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 11, 20);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 1, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 1, 0);

        PayrollServices.beginUnitOfWork();
        domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("subscription end date",domainEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();

        // date after update; update fields
        message = new Message();
        message.transactionDate = SpcfCalendar.createInstance(2010, 11, 20);
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 11, 20);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 1, 1, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 2, 1, 0);

        PayrollServices.beginUnitOfWork();
        domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("subscription end date", domainEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_AssetCanceled() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.assetStatus = AssetStatusType.CANCELLED;
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertEquals("entitlement state", EntitlementStateCode.Disabled, domainEntitlement.getEntitlementState());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessNewMessages_SaveUnmarshallable() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_ENDING_NOTICE;
        AMOMockGateway.getMessages().add(message);

        AMOMockGateway.setReturnUnmarshallableDTO(true);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("subscription end date", domainEntitlement.getSubscriptionEndDate());

        Assert.assertEquals("Unmarshallable saved", 1, Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Error)).size());
        PayrollServices.rollbackUnitOfWork();

        runAMOProcessor();
    }

    @Test
    public void testProcessNewMessages_SavedMessages() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_ENDING_NOTICE;
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_ENDING_NOTICE;
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 1, 1, 0, 0);
        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 0, 2, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertNull("subscription end date", domainEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();

        assertEntitlementMessages(licenseNumber, eoc, 0, 2, 0, 0);
    }

    @Test
    public void testProcessNewMessages_SaveMessagesWithDifferentOrderNumbers() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_ENDING_NOTICE;
        AMOMockGateway.getMessages().add(message);

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "54321", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionEndDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        runAMOProcessor();
        assertEntitlementMessages(licenseNumber, eoc, 2, 0, 0, 0);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals(2, entitlementMessages.size());
        boolean found12345OrderNumber = false;
        boolean found54321OrderNumber = false;
        for (EntitlementMessage entitlementMessage : entitlementMessages) {
            if(entitlementMessage.getOrderNumber().equals("12345")) {
                found12345OrderNumber = true;
            } else if(entitlementMessage.getOrderNumber().equals("54321")) {
                found54321OrderNumber = true;
            }
        }
        assertTrue(found12345OrderNumber);
        assertTrue(found54321OrderNumber);
        PayrollServices.rollbackUnitOfWork();
    }

    public static void assertEntitlementMessages(String pLicenseNumber, String pEOC, int pNewMessages, int pProcessedMessages, int pSkippedMessages, int pErrorMessages) {
        Criterion<EntitlementMessage> baseCriterion = EntitlementMessage.LicenseNumber().equalTo(pLicenseNumber);
        String message = "Lic: " + pLicenseNumber;
        if(pEOC != null) {
            baseCriterion = baseCriterion.And(EntitlementMessage.EntitlementOfferingCode().equalTo(pEOC));
            message += "EOC: " + pEOC;
        }

        PayrollServices.beginUnitOfWork();
        assertEquals(message + " 'New' entitlement messages", pNewMessages,
                     Application.find(EntitlementMessage.class, baseCriterion.And(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.New))).size());
        assertEquals(message + " 'Processed' entitlement messages", pProcessedMessages,
                     Application.find(EntitlementMessage.class, baseCriterion.And(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed))).size());
        assertEquals(message + " 'Skipped' entitlement messages", pSkippedMessages,
                     Application.find(EntitlementMessage.class, baseCriterion.And(EntitlementMessage.Status().in(EntitlementMessageStatusCode.SkippedOldTimestamp,
                                                                                                                 EntitlementMessageStatusCode.SkippedEntitlementNotFound))).size());
        assertEquals(message + " 'Error' entitlement messages", pErrorMessages,
                     Application.find(EntitlementMessage.class, baseCriterion.And(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Error))).size());
        PayrollServices.rollbackUnitOfWork();
    }

    public static void runAMOProcessor() {
        AMOMessageProcessor amoMessageProcessor = new AMOMessageProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AMOMessageProcessor, UUID.randomUUID().toString(), "");
        amoMessageProcessor.execute();
    }
}
