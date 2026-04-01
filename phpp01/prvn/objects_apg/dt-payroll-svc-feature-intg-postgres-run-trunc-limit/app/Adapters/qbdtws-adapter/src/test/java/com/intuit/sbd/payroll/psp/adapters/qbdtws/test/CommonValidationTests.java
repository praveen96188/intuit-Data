package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.CommonValidations;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 23, 2009
 * Time: 3:38:28 PM
 */
public class CommonValidationTests {
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
    public void testCompanyValidation() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20091223000000");
        Company3Dataloader companyDataloader = new Company3Dataloader();
        companyDataloader.persistCompany3();
        companyDataloader.persistCompanyPIN();
        PayrollServices.commitUnitOfWork();

        String psid = "8574536";
        String pin = "1234567a";

        PayrollServices.beginUnitOfWork();
        Request validRequest = new Request();
        validRequest.setPSID(psid);
        validRequest.setPIN(pin);
        QBProcessingMessages qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.validateCompanyPin(validRequest, qbProcessingMessages);
        assertEquals("Processing Errors Count", 0, qbProcessingMessages.getProcessingMessagesList().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Request invalidPsidRequest = new Request();
        invalidPsidRequest.setPSID("123");
        invalidPsidRequest.setPIN(pin);
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.validateCompanyPin(invalidPsidRequest, qbProcessingMessages);
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 169, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Request invalidPinRequest = new Request();
        invalidPinRequest.setPSID(psid);
        invalidPinRequest.setPIN("123");
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.validateCompanyPin(invalidPinRequest, qbProcessingMessages);
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 292, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testQbVersionValidation() {
        PayrollServices.beginUnitOfWork();
        QBProcessingMessages qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.isQBVersionActive("", qbProcessingMessages);
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 6, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        qbProcessingMessages = new QBProcessingMessages();
        OFXAPPVERObject ofxappverObject = CommonValidations.isQBVersionActive("15.01.R.10/14586#pro", qbProcessingMessages);
        assertEquals("QB Major Version", 15, ofxappverObject.getIntQBVersion().intValue());
        assertEquals("R Number", 10, ofxappverObject.getIntRNumber().intValue());
        assertEquals("QB Full Version", "15.01.R.10", ofxappverObject.getQBVersionStr());
        assertEquals("Tax Table", "14586", ofxappverObject.getTaxTableId());
        assertEquals("QB Flavor", "pro", ofxappverObject.getFlavorId());
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 7, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.isQBVersionActive("17.01.R.10/#pro", qbProcessingMessages);
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 6, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.isQBVersionActive("17/#pro", qbProcessingMessages);
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 6, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.isQBVersionActive("14586#pro", qbProcessingMessages);
        assertEquals("Processing Errors Count", 1, qbProcessingMessages.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 6, qbProcessingMessages.getProcessingMessagesList().get(0).getCode());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.isQBVersionActive("20.01.R.10/14586", qbProcessingMessages);
        assertEquals("Processing Errors Count", 0, qbProcessingMessages.getProcessingMessagesList().size());        
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        qbProcessingMessages = new QBProcessingMessages();
        CommonValidations.isQBVersionActive("20.01.R.10/14586#pro", qbProcessingMessages);
        assertEquals("Processing Errors Count", 0, qbProcessingMessages.getProcessingMessagesList().size());
        PayrollServices.rollbackUnitOfWork();
    }
}
