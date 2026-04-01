package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.io.Reader;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: May 16, 2008
 * Time: 2:08:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class FinalizeSourceSystemTransmissionSecondaryCoreTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        ProcessResult processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(null, "123272727", null, null);
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        ProcessResult processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE, null, null, null);

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }


    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testCompanyDoesNotExist() {
        ProcessResult processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "112233", null, null);

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:112233 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 295 - Invalid Source System Transmission
     */
    @Test
    public void testInvalidSourceSystemTransmission() {
        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission DTO with a null Transmission ID

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);

        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "123456", null, sourceSystemTransmissionDTO);
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "295", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid Source System Transmission Id.",
                errorMessage.getMessage());
    }

    /**
     * Test message 296 - Source System Transmission Does Not Exist
     */

    @Test
    public void testSourceSystemTransmissionDoesNotExist() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);
        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

        // Try to Finalize the Source System Transmission with invalid ID
        sourceSystemTransmissionDTO.setResponseToken(5L);

        sourceSystemTransmissionDTO.setResponseDocument("RESPONSE OFX");
        processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "123456", "99870baa-47ae-4d04-8bd8-01b0f3d72a8f", sourceSystemTransmissionDTO);

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "296", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Transmission 99870baa-47ae-4d04-8bd8-01b0f3d72a8f for company QBOE:123456 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 297 - Source System Transmission Does Belong to the Company
     */

    @Test
    public void testSourceSystemTransmissionDoesNotBelongToCompany() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);
        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();

        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

        // Load Another Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        result = DataLoader.addCompany(company2);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company2.getSourceSystemCd().toString()), company2.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Try to Finalize the Source System Transmission with invalid Company
        sourceSystemTransmissionDTO.setResponseToken(5L);

        sourceSystemTransmissionDTO.setResponseDocument("RESPONSE OFX");
        processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "1234562", transmissionId, sourceSystemTransmissionDTO);

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "296", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Transmission " + transmissionId + " for company QBOE:1234562 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Verify that the response token can be less than the request token.
     */

    @Test
    public void testResponseTokenGreaterThanRequestToken() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(6L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 6L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

        // Try to Finalize the Source System Transmission with invalid Response Token
        sourceSystemTransmissionDTO.setResponseToken(5L);

        sourceSystemTransmissionDTO.setResponseDocument("RESPONSE OFX");
        processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        assertTrue(processResult.isSuccess());

        assertEquals("Messages size", 0, processResult.getMessages().size());
    }

    /**
     * Test  Transmission is successfully finalized
     */

    @Test
    public void testFinalizeTransmissionSuccessful() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId,
                sourceSystemTransmissionDTO);

        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

        // Finalize the Source System Transmission
        sourceSystemTransmissionDTO.setResponseToken(5L);

        sourceSystemTransmissionDTO.setResponseDocument("RESPONSE OFX");
        processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        // Check that transmission was successfully finalized
        assertSuccess("finalizeSourceSystemTransmission", processResult);
        sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        compareOFX( savedSourceSystemTransmission.getResponseDocument(), "RESPONSE OFX");
        assertEquals("Response Token: ", 5L, savedSourceSystemTransmission.getResponseToken());
        PayrollServices.commitUnitOfWorkWithSecondary();
    }

    /**
     * Test Sucessful Finalize with null Response values
     */

    @Test
    public void testNullResponseTokenAndDocument() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(6L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 6L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

        // Finalize the Source System Transmission with null Response Token and null Response Document
        sourceSystemTransmissionDTO.setResponseToken(null);
        sourceSystemTransmissionDTO.setResponseDocument(null);
        processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        // Check that transmission was successfully finalized
        assertSuccess("finalizeSourceSystemTransmission", processResult);
        sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        compareOFX( savedSourceSystemTransmission.getRequestDocument(), "REQUEST OFX");
        assertEquals("Request Token: ", 6L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        assertTrue("Response Document: ", savedSourceSystemTransmission.getResponseDocument() == null);
        assertTrue("Response Token: ", savedSourceSystemTransmission.getResponseToken() == 0L);
        PayrollServices.commitUnitOfWorkWithSecondary();
    }

    private void compareOFX(String actual, String expected) {
            assertEquals("Request Document: ", actual, expected);
    }
}
