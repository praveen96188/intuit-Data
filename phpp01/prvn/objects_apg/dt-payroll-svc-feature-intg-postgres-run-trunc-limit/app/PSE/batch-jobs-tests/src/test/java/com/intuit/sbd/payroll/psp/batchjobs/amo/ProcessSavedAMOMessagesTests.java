package com.intuit.sbd.payroll.psp.batchjobs.amo;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.ERSMockGateway;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementUnitInfoDTO;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetChangeReasonType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.ItemType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AMOMessageProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.gateways.amo.*;
import com.intuit.sbd.payroll.psp.gateways.amo.Entitlement;
import com.intuit.sbd.payroll.psp.processes.AddEntitlementCore;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 17, 2010
 * Time: 4:57:00 PM
 */
public class ProcessSavedAMOMessagesTests {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        AMOMockGateway.getMessages().clear();
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
    public void testProcessSavedMessages_ProcessUnmatchedMessage() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);

        SpcfCalendar currentDate = PSPDate.getPSPTime().copy();
        CalendarUtils.addBusinessDays(currentDate, 1);
        DataLoadServices.setPSPDate(currentDate);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc, 0, 0, 1, 0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertEquals("next charge date", "2010/10/20 07:00:00.0", domainEntitlement.getNextChargeDate().toString());
        PayrollServices.rollbackUnitOfWork();

        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);
    }

    @Test
    public void testProcessSavedMessages_ExceptionWhileProcessingUpdateMessage() {
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

        AMOMessageProcessor amoMessageProcessing =
                new AMOMessageProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AMOMessageProcessor, "1234", "");

        amoMessageProcessing.execute();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 1, entitlementMessages.size());
        entitlementMessages = Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                       .Where(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed)));
        assertEquals("processed saved entitlement messages", 0, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_InvalidEntitlementUpdateMessage() {
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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 1, entitlementMessages.size());
        assertEquals("retry count", 1, entitlementMessages.get(0).getFailureCount());
        assertNotNull("failure message", entitlementMessages.get(0).getLastFailureMessage());

        entitlementMessages = Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                       .Where(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed)));
        assertEquals("processed saved entitlement messages", 0, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        long maxRetryCount = SystemParameter.findLongValue(SystemParameter.Code.AMO_MAX_MESSAGE_FAILURE_COUNT, 5);
        PayrollServices.rollbackUnitOfWork();

        for(int i = 1; i < maxRetryCount; i++) {
            ProcessNewAMOMessagesTests.runAMOProcessor();
        }

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Error));
        assertEquals("error entitlement messages", 1, entitlementMessages.size());
        assertEquals("retry count", maxRetryCount, entitlementMessages.get(0).getFailureCount());
        assertNotNull("failure message", entitlementMessages.get(0).getLastFailureMessage());

        entitlementMessages = Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed));
        assertEquals("processed saved entitlement messages", 0, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_InvalidEntitlementUpdateMessage_NewMessage() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        Message message = new Message();
        // invlid message item number does not match
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.ASSISTED, "Customer1", "89511");
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        // add a new valid message
        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0).sort(EntitlementMessage.FailureCount().Descending());
        assertEquals("new saved entitlement messages", 2, entitlementMessages.size());

        // failed message
        assertEquals("retry count", 1, entitlementMessages.get(0).getFailureCount());
        assertNotNull("failure message", entitlementMessages.get(0).getLastFailureMessage());

        // new message
        assertEquals("retry count", 0, entitlementMessages.get(1).getFailureCount());
        assertNull("failure message", entitlementMessages.get(1).getLastFailureMessage());

        // the first message was already saved, so the new message processor could not process the second
        entitlementMessages = Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                .Where(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed)));
        assertEquals("processed saved entitlement messages", 0, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed));
        assertEquals("processed saved entitlement messages", 2, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_UnmatchedMessage() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2010, 10, 20);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 1, entitlementMessages.size());
        entitlementMessages = Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed));
        assertEquals("processed saved entitlement messages", 0, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        int expirationPeriod = SystemParameter.findIntValue(SystemParameter.Code.AMO_MESSAGE_EXPIRATION_WAIT_PERIOD, 20);
        PayrollServices.rollbackUnitOfWork();
        SpcfCalendar date = PSPDate.getPSPTime();
        date.addMinutes(expirationPeriod + 1);
        DataLoadServices.setPSPDate(date);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        entitlementMessages = Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.SkippedEntitlementNotFound));
        assertEquals("skipped entitlement messages", 1, entitlementMessages.size());

        entitlementMessages = Application.find(EntitlementMessage.class, EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed));
        assertEquals("processed saved entitlement messages", 0, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();
    }
    
    @Test
    public void testProcessSavedMessages_EntitlementTransfer() {
        String sourceLicenseNumber = "12345678901234567890";
        String targetLicenseNumber = "789417";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString(), sourceLicenseNumber, targetLicenseNumber);
        message.entitlements.add(entitlement);
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement sourceEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(sourceLicenseNumber, eoc);
        assertEquals("source entitlement state", EntitlementStateCode.Disabled, sourceEntitlement.getEntitlementState());
        for (com.intuit.sbd.payroll.psp.domain.EntitlementUnit entitlementUnit : sourceEntitlement.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(sourceLicenseNumber))) {
            assertEquals("source entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());
        }

        com.intuit.sbd.payroll.psp.domain.Entitlement targetEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(targetLicenseNumber, eoc);
        assertEquals("target entitlement state", EntitlementStateCode.Enabled, targetEntitlement.getEntitlementState());
        for (com.intuit.sbd.payroll.psp.domain.EntitlementUnit entitlementUnit : targetEntitlement.getEntitlementUnitCollection()) {
            assertEquals("target entitlement unit status", EntitlementUnitStatusCode.Activated, entitlementUnit.getEntitlementUnitStatus());
        }

        entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("new saved entitlement messages", 0, entitlementMessages.size());
        entitlementMessages = Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                       .Where(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.Processed)));
        assertEquals("processed saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEntitlementDisablementAsstWVmpAndWC() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.CloudV2, ServiceCode.Tax, ServiceCode.WorkersComp, ServiceCode.ViewMyPaycheck);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, entitlementOfferingCode, "1", DataLoadServices.AssetItemNumber.ASSISTED, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyService service = company.getCompanyService(ServiceCode.Tax);
        assertEquals("Assisted service Should be active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.ViewMyPaycheck);
        assertEquals("VMP service Should be active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.CloudV2);
        assertEquals("CloudV2 service Should be active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);


        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        service = company.getCompanyService(ServiceCode.Tax);
        assertEquals("Assisted service Should be Cancelled", ServiceSubStatusCode.Cancelled, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.DirectDeposit);
        assertEquals("DD service Should be Cancelled", ServiceSubStatusCode.Cancelled, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.ViewMyPaycheck);
        assertEquals("VMP service Should be Cancelled", ServiceSubStatusCode.Cancelled, service.getStatusCd());

        //Workers COmp is active on this company. Cloudv2 will be active
        service = company.getCompanyService(ServiceCode.CloudV2);
        assertEquals("CloudV2 service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testEntitlementDisablemenDIYWVmpWC() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.CloudV2, ServiceCode.DirectDeposit, ServiceCode.WorkersComp, ServiceCode.ViewMyPaycheck);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, entitlementOfferingCode, "1", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        CompanyService service = company.getCompanyService(ServiceCode.DirectDeposit);
        assertEquals("DD service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.ViewMyPaycheck);
        assertEquals("VMP service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.WorkersComp);
        assertEquals("WC service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        PayrollServices.rollbackUnitOfWork();


        message = new Message();
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);


        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        service = company.getCompanyService(ServiceCode.DirectDeposit);
        assertEquals("DD service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.ViewMyPaycheck);
        assertEquals("VMP service Should be Cancelled", ServiceSubStatusCode.Cancelled, service.getStatusCd());

        //Workers COmp is active on this company. Cloudv2 will be active
        service = company.getCompanyService(ServiceCode.WorkersComp);
        assertEquals("WC service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());
        service = company.getCompanyService(ServiceCode.CloudV2);
        assertEquals("CloudV2 service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testEntitlementDisablemenDIYWVmpNoWC() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.CloudV2, ServiceCode.DirectDeposit, ServiceCode.ViewMyPaycheck);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, entitlementOfferingCode, "1", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        CompanyService service = company.getCompanyService(ServiceCode.DirectDeposit);
        assertEquals("DD service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.ViewMyPaycheck);
        assertEquals("VMP service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.WorkersComp);
        assertNull(service);

        PayrollServices.rollbackUnitOfWork();


        message = new Message();
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);


        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        service = company.getCompanyService(ServiceCode.DirectDeposit);
        assertEquals("DD service Should be Active", ServiceSubStatusCode.ActiveCurrent, service.getStatusCd());

        service = company.getCompanyService(ServiceCode.ViewMyPaycheck);
        assertEquals("VMP service Should be Cancelled", ServiceSubStatusCode.Cancelled, service.getStatusCd());

        //Workers COmp is inactive on this company. Cloudv2 will be deactivated
        service = company.getCompanyService(ServiceCode.CloudV2);
        assertEquals("CloudV2 service Should be Cancelled", ServiceSubStatusCode.Cancelled, service.getStatusCd());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testProcessSavedMessages_MoveEntitlementBasicToEnhanced() {
        String licenseNumber = "12345678901234567890";
        String oldEOC = "1";
        String newEOC = "2";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, oldEOC, "1", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, oldEOC);

        message = new Message();
        entitlement = new Entitlement(licenseNumber, oldEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, newEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        entitlement.entitlementState = "Enabled";
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit pspEU : company.getEntitlementUnitCollection()) {
            com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit amoEU = new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(pspEU.getFedTaxId(),"Activated");
            entitlement.addEntitlementUnitUpdates(amoEU);
        }
        PayrollServices.rollbackUnitOfWork();

        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement oldEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, oldEOC);
        assertNotNull(oldEntitlement);
        assertEquals(EntitlementStateCode.Disabled, oldEntitlement.getEntitlementState());
        assertEquals(0, oldEntitlement.getEntitlementUnitCollection().size());

        com.intuit.sbd.payroll.psp.domain.Entitlement newEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, newEOC);
        assertNotNull(newEntitlement);
        assertEquals(EntitlementStateCode.Enabled, newEntitlement.getEntitlementState());
        assertEquals(1, newEntitlement.getEntitlementUnitCollection().size());

        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals(3, entitlementMessages.size());
        for (EntitlementMessage entitlementMessage: entitlementMessages) {
            assertEquals(EntitlementMessageStatusCode.Processed, entitlementMessage.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_MoveEntitlementDiskToDIY() {
        String licenseNumber = "12345678901234567890";
        String oldEOC = "1";
        String newEOC = "2";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, oldEOC, "1", DataLoadServices.AssetItemNumber.DIY_DISK_DELIVERY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addDiskDeliveryEntitlementUnit(company, licenseNumber, oldEOC);

        message = new Message();
        entitlement = new Entitlement(licenseNumber, oldEOC, "2", DataLoadServices.AssetItemNumber.DIY_DISK_DELIVERY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, newEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        entitlement.entitlementState = "Enabled";
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.STANDARD),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        //Make sure the service key is a 8000 key and the extension key is null
        assertEquals("8", company.getActivePrimaryEntitlementUnit().getServiceKey().substring(0,1));

        for (EntitlementUnit pspEU : company.getEntitlementUnitCollection()) {
            com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit amoEU = new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(pspEU.getFedTaxId(),"Activated");
            entitlement.addEntitlementUnitUpdates(amoEU);
        }
        PayrollServices.rollbackUnitOfWork();

        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement oldEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, oldEOC);
        assertNotNull(oldEntitlement);
        assertEquals(EntitlementStateCode.Disabled, oldEntitlement.getEntitlementState());
        assertEquals(0, oldEntitlement.getEntitlementUnitCollection().size());

        com.intuit.sbd.payroll.psp.domain.Entitlement newEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, newEOC);
        assertNotNull(newEntitlement);
        assertEquals(EntitlementStateCode.Enabled, newEntitlement.getEntitlementState());
        assertEquals(1, newEntitlement.getEntitlementUnitCollection().size());

        //Make sure the service key is a 4000 key and the extension key is null
        assertEquals("4", newEntitlement.getEntitlementUnitCollection().getFirst().getServiceKey().substring(0,1));
        assertNull(newEntitlement.getEntitlementUnitCollection().getFirst().getExtensionKey());

        entitlementMessages = Application.find(EntitlementMessage.class);
        assertEquals(3, entitlementMessages.size());
        for (EntitlementMessage entitlementMessage: entitlementMessages) {
            assertEquals(EntitlementMessageStatusCode.Processed, entitlementMessage.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_MoveEntitlementWithTwoActiveEntitlementUnits() {
        String licenseNumber = "12345678901234567890";
        String oldEOC = "1";
        String newEOC = "2";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, oldEOC, "1", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, oldEOC);

        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, oldEOC);

        message = new Message();
        entitlement = new Entitlement(licenseNumber, oldEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, newEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        entitlement.entitlementState = "Enabled";
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        com.intuit.sbd.payroll.psp.domain.Entitlement oldEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, oldEOC);
        for (EntitlementUnit pspEU : oldEntitlement.getEntitlementUnitCollection()) {
            com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit amoEU = new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(pspEU.getFedTaxId(),"Activated");
            entitlement.addEntitlementUnitUpdates(amoEU);
        }
        PayrollServices.rollbackUnitOfWork();

        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        oldEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, oldEOC);
        assertNotNull(oldEntitlement);
        assertEquals(EntitlementStateCode.Disabled, oldEntitlement.getEntitlementState());
        assertEquals(0, oldEntitlement.getEntitlementUnitCollection().size());

        com.intuit.sbd.payroll.psp.domain.Entitlement newEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, newEOC);
        assertNotNull(newEntitlement);
        assertEquals(EntitlementStateCode.Enabled, newEntitlement.getEntitlementState());
        assertEquals(2, newEntitlement.getEntitlementUnitCollection().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_MoveEntitlementWithOneActiveAndOneInactiveEntitlementUnits() {
        String licenseNumber = "12345678901234567890";
        String oldEOC = "1";
        String newEOC = "2";

        String oldSubNum;

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, oldEOC, "1", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, oldEOC);

        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, oldEOC);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu = company.getEntitlementUnitCollection().find(com.intuit.sbd.payroll.psp.domain.EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)).get(0);
        eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        oldSubNum = eu.getEntitlement().getSubscriptionNumber();
        Application.save(eu);
        PayrollServices.commitUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, oldEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, newEOC, "2", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.addContactUpdate("FirstName", "MiddleName", "LastName", "test@intuit.com");
        entitlement.setEntitlementDisabled();
        entitlement.entitlementState = "Enabled";
        entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), TransactionAttribute.BASIC),
                                             new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), TransactionAttribute.UNLIMITED));

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement oldEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, oldEOC);
        for (EntitlementUnit pspEU : oldEntitlement.getEntitlementUnitCollection()) {
            com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit amoEU;
            if (pspEU.getFedTaxId().equals("000000001")) {
                amoEU = new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(pspEU.getFedTaxId(),"Activated");
            } else {
                amoEU = new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(pspEU.getFedTaxId(),"Deactivated");
            }

            entitlement.addEntitlementUnitUpdates(amoEU);
        }
        PayrollServices.rollbackUnitOfWork();

        message.entitlements.add(entitlement);
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        oldEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, oldEOC);
        assertNotNull(oldEntitlement);
        assertEquals(EntitlementStateCode.Disabled, oldEntitlement.getEntitlementState());
        assertEquals(0, oldEntitlement.getEntitlementUnitCollection().size());
        assertNotNull(oldEntitlement.getSubscriptionNumber());

        com.intuit.sbd.payroll.psp.domain.Entitlement newEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, newEOC);
        assertNotNull(newEntitlement);
        assertEquals(EntitlementStateCode.Enabled, newEntitlement.getEntitlementState());
        assertEquals(2, newEntitlement.getEntitlementUnitCollection().size());
        assertEquals(oldSubNum, newEntitlement.getSubscriptionNumber());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_AssistedNullNextChargeDate() {
        String licenseNumber = DataLoadServices.LIC_PREFIX  + DataLoadServices.getNextPSID() +  "Y";
        String oldEOC = DataLoadServices.EOC_PREFIX  + DataLoadServices.getNextPSID();

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, oldEOC, "1", DataLoadServices.AssetItemNumber.ASSISTED, "Customer1", "89511");
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        com.intuit.sbd.payroll.psp.domain.Entitlement ent = company.getActivePrimaryEntitlementUnit().getEntitlement();
        ent.setNextChargeDate(PSPDate.getPSPTime());
        Application.save(ent);
        PayrollServices.commitUnitOfWork();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNull(entitlementUnit.getEntitlement().getNextChargeDate());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEntitlementMigration_withChargeDateBeforeCreation() {
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        String orderNumber1 = "1";
        String orderNumber2 = "2";
        String licenseNumber = "12345678901234567890";
        String eoc1 = "1";
        String eoc2 = "2";
        String eoc3 = "3";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc1);

        // disable message for eoc1
        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc1, orderNumber1, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc1, 1, 0, 0, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc1, 0, 1, 0, 0);

        // process entitlement creation with same order and license number (should create entitlement 2 and move the eu's from entitlement 1)
        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc2, orderNumber1, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementEnabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc2, 1, 0, 0, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc1, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement entitlement1 = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc1);
        assertNotNull(entitlement1);
        assertEquals("Entitlement units not moved", 0, entitlement1.getEntitlementUnitCollection().size());
        assertEquals("Entitlement enabled", EntitlementStateCode.Disabled, entitlement1.getEntitlementState());
        com.intuit.sbd.payroll.psp.domain.Entitlement entitlement2 = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc2);
        assertNotNull(entitlement2);
        assertEquals("Entitlement units not moved", 1, entitlement2.getEntitlementUnitCollection().size());
        assertEquals("Entitlement disabled", EntitlementStateCode.Enabled, entitlement2.getEntitlementState());
        PayrollServices.rollbackUnitOfWork();

        // disable message for eoc2
        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc2, orderNumber2, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc2, 1, 1, 0, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc2, 0, 2, 0, 0);

        // add next charge date message
        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc3, null, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementEnabled();
        entitlement.nextChargeDate = SpcfCalendar.createInstance(2012, 7, 5, SpcfTimeZone.getLocalTimeZone());
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.SUBSCRIPTION_CHARGE_DATE_NOTICE;
        AMOMockGateway.getMessages().add(message);

        // skip next charge date
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc3, 1, 0, 0, 0);

        SpcfCalendar current = PSPDate.getPSPTime().copy();
        current.addHours(1);
        DataLoadServices.setPSPDate(current);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc3, 0, 0, 1, 0);

        // creation message for eoc3
        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc3, orderNumber2, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementEnabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc3, 1, 0, 1, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc3, 2, 0, 0, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc3, 0, 2, 0, 0);

        PayrollServices.beginUnitOfWork();
        entitlement2 = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc2);
        assertNotNull(entitlement2);
        assertEquals("Entitlement units not moved", 0, entitlement2.getEntitlementUnitCollection().size());
        assertEquals("Entitlement enabled", EntitlementStateCode.Disabled, entitlement2.getEntitlementState());
        com.intuit.sbd.payroll.psp.domain.Entitlement entitlement3 = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc3);
        assertNotNull(entitlement3);
        assertEquals("Entitlement units not moved", 1, entitlement3.getEntitlementUnitCollection().size());
        assertEquals("Entitlement disabled", EntitlementStateCode.Enabled, entitlement3.getEntitlementState());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEntitlementMigration_withNoEntitlementUnits() {
        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        String orderNumber1 = "1";
        String licenseNumber = "12345678901234567890";
        String eoc1 = "1";
        String eoc2 = "2";

        // this can only be created by the sql migration of entitlements
        PayrollServices.beginUnitOfWork();
        EntitlementDTO entitlementDTO = new EntitlementDTO();
        entitlementDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        entitlementDTO.setEntitlementOfferingCode(eoc1);
        entitlementDTO.setLicenseNumber(licenseNumber);
        AddEntitlementCore addEntitlementCore = new AddEntitlementCore(entitlementDTO);
        assertSuccess("add entitlement", addEntitlementCore.execute());
        PayrollServices.commitUnitOfWork();

        // disable message for eoc1
        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc1, orderNumber1, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementDisabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_DISABLEMENT;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc1, 1, 0, 0, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc1, 0, 1, 0, 0);

        // process entitlement creation with same order and license number (should create entitlement 2 and move the eu's from entitlement 1)
        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc2, orderNumber1, DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.setEntitlementEnabled();
        message.entitlements.add(entitlement);
        message.transactionDate = PSPDate.getPSPTime();
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_CREATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc2, 1, 0, 0, 0);
        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc1, 0, 1, 0, 0);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement entitlement1 = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc1);
        assertNotNull(entitlement1);
        assertEquals("Entitlement enabled", EntitlementStateCode.Disabled, entitlement1.getEntitlementState());
        com.intuit.sbd.payroll.psp.domain.Entitlement entitlement2 = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc2);
        assertNotNull(entitlement2);
        assertEquals("Entitlement disabled", EntitlementStateCode.Enabled, entitlement2.getEntitlementState());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testProcessSavedMessages_UsagePaymentFailureNotice() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, "Customer1", "89511");
        entitlement.setItemType(ItemType.USAGE);
        message.entitlements.add(entitlement);
        message.includeUsageAsset = true;
        message.eventReason = AssetChangeReasonType.PAYMENT_FAILURE_NOTICE;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance();
        nextChargeDate.addDays(365);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, EditionType.Basic, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, nextChargeDate);

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement pspEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);

        assertNotNull(pspEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, "Customer1", "89511");
        entitlement.setItemType(ItemType.USAGE);
        message.entitlements.add(entitlement);
        message.includeUsageAsset = true;
        message.eventReason = AssetChangeReasonType.PAYMENT_SUCCESS_NOTICE;
        AMOMockGateway.getMessages().add(message);

        //Save the message
        ProcessNewAMOMessagesTests.runAMOProcessor();

        //Process the message
        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        pspEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);

        assertNull(pspEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_UsageOnlyPaymentFailureNotice() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, "Customer1", "89511");
        entitlement.setItemType(ItemType.USAGE);
        message.entitlements.add(entitlement);
        message.includeUsageAsset = true;
        message.includePayrollAsset = false;
        message.eventReason = AssetChangeReasonType.PAYMENT_FAILURE_NOTICE;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.rollbackUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance();
        nextChargeDate.addDays(365);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, EditionType.Basic, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, nextChargeDate);

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement pspEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);

        assertNotNull(pspEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();

        message = new Message();
        entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, "Customer1", "89511");
        entitlement.setItemType(ItemType.USAGE);
        message.entitlements.add(entitlement);
        message.includeUsageAsset = true;
        message.includePayrollAsset = false;
        message.eventReason = AssetChangeReasonType.PAYMENT_SUCCESS_NOTICE;
        AMOMockGateway.getMessages().add(message);

        //Save the message
        ProcessNewAMOMessagesTests.runAMOProcessor();

        //Process the message
        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        pspEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);

        assertNull(pspEntitlement.getSubscriptionEndDate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_WelcomeEventNoTrial() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);



        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, "Customer1", "89511");
        entitlement.subscriptionStartDate = SpcfCalendar.createInstance();
        message.entitlements.add(entitlement);
        message.isIncludeTrialAsset = true;
        message.includePayrollAsset = true;
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_UNIT_ACTIVATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance();
        nextChargeDate.addDays(365);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, EditionType.Basic, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, nextChargeDate);

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.getEntitlement().setSubscriptionStartDate(PSPDate.getPSPTime());
        Application.save(entitlementUnit.getEntitlement());
        PayrollServices.commitUnitOfWork();

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEventEmail> welcomeEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.SymphonyWelcomeNoTrial);
        assertEquals(1, welcomeEmails.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_WelcomeEventTrial() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, "Customer1", "89511");
        entitlement.subscriptionStartDate = SpcfCalendar.createInstance();
        message.entitlements.add(entitlement);
        message.isIncludeTrialAsset = true;
        message.includePayrollAsset = true;
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_UNIT_ACTIVATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance();
        nextChargeDate.addDays(365);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, EditionType.Basic, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, nextChargeDate);

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);

        SpcfCalendar futureStartDate = PSPDate.getPSPTime();
        futureStartDate.addDays(30);
        entitlementUnit.getEntitlement().setSubscriptionStartDate(futureStartDate);

        Application.save(entitlementUnit.getEntitlement());
        PayrollServices.commitUnitOfWork();

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEventEmail> welcomeEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.SymphonyWelcomeFreeTrial);
        assertEquals(1, welcomeEmails.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_WelcomeEventOneMonth() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_DD_YEAREND, "Customer1", "89511");
        entitlement.subscriptionStartDate = SpcfCalendar.createInstance();
        message.entitlements.add(entitlement);
        message.isIncludeTrialAsset = true;
        message.includePayrollAsset = true;
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_UNIT_ACTIVATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance();
        nextChargeDate.addDays(365);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_DD_YEAREND, nextChargeDate);

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.getEntitlement().setSubscriptionStartDate(PSPDate.getPSPTime());
        Application.save(entitlementUnit.getEntitlement());
        PayrollServices.commitUnitOfWork();

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEventEmail> welcomeEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.SymphonyWelcomeOneMonthReactivation);
        assertEquals(1, welcomeEmails.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testProcessSavedMessages_NoWelcomeEvent() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance();
        nextChargeDate.addDays(365);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_DD_YEAREND, nextChargeDate);

        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        spcfCalendar.addDays(8);
        DataLoadServices.setPSPDate(spcfCalendar);

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_DD_YEAREND, "Customer1", "89511");
        entitlement.subscriptionStartDate = SpcfCalendar.createInstance();
        message.entitlements.add(entitlement);
        message.isIncludeTrialAsset = true;
        message.includePayrollAsset = true;
        message.eventReason = AssetChangeReasonType.ENTITLEMENT_UNIT_ACTIVATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(0, 0);
        assertEquals("saved entitlement messages", 1, entitlementMessages.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.getEntitlement().setSubscriptionStartDate(PSPDate.getPSPTime());
        Application.save(entitlementUnit.getEntitlement());
        PayrollServices.commitUnitOfWork();

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

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEventEmail> welcomeEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.SymphonyWelcomeOneMonthReactivation);
        assertEquals(0, welcomeEmails.size());

        PayrollServices.rollbackUnitOfWork();
    }

    //Uncomment to run locally
    @Test @Ignore
    public void test() {
        ProcessNewAMOMessagesTests.runAMOProcessor();
    }

    // Ignore or remove when the AMO sends the retail field as part of the message.
    @Ignore
    @Test
    public void testEntitlementRegistration() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);

        Message message = new Message();
        Entitlement entitlement = new Entitlement(licenseNumber, eoc, "12345", DataLoadServices.AssetItemNumber.DIY_YEARLY, "Customer1", "89511");
        entitlement.subscriptionStartDate = SpcfCalendar.createInstance(2012, 10, 20);
        message.entitlements.add(entitlement);

        message.eventReason = AssetChangeReasonType.ENTITLEMENT_UNIT_ACTIVATION;
        AMOMockGateway.getMessages().add(message);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc, 1, 0, 0, 0);

        SpcfCalendar currentDate = PSPDate.getPSPTime().copy();
        CalendarUtils.addBusinessDays(currentDate, 1);
        DataLoadServices.setPSPDate(currentDate);

        ProcessNewAMOMessagesTests.runAMOProcessor();
        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc, 0, 0, 1, 0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        EntitlementUnit eu = DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement pspEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        pspEntitlement.setSubscriptionStartDate(SpcfCalendar.createInstance(2012, 12, 12));
        Application.save(pspEntitlement);
        PayrollServices.commitUnitOfWork();

        ProcessNewAMOMessagesTests.runAMOProcessor();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Entitlement domainEntitlement = com.intuit.sbd.payroll.psp.domain.Entitlement.findEntitlement(licenseNumber, eoc);
        assertTrue(domainEntitlement.getRetail());
        PayrollServices.rollbackUnitOfWork();

        ProcessNewAMOMessagesTests.assertEntitlementMessages(licenseNumber, eoc, 0, 1, 0, 0);
    }

}
