package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.junit.*;

import static org.junit.Assert.assertFalse;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import static junit.framework.Assert.assertEquals;

import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: May 16, 2008
 * Time: 1:39:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class InitializeSourceSystemTransmissionSecondaryCoreTests {
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
        ProcessResult processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(null, "123272727", null, null);
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
        ProcessResult processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE, null, null, null);
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
        ProcessResult processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "112233", null, null);
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:112233 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test  Transmission is successfully initialized
     */

    @Test
    public void testInitializeTransmissionSuccessful() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        String requestDoc = "TEST OFX";
        sourceSystemTransmissionDTO.setRequestDocument(requestDoc);
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        String requestDocClob = savedSourceSystemTransmission.getRequestDocument();

        assertEquals("Request Document: ", requestDoc, requestDocClob);

        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

    }

    /**
     * Test  Transmission is successfully initialized - no company associated
     */

    @Test
    public void testInitializeTransmissionWithNoCompanySuccessful
    () {
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        String requestDoc = "TEST OFX";
        sourceSystemTransmissionDTO.setRequestDocument(requestDoc);
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(null,
                null, SpcfUniqueId.createInstance(true).toString(), sourceSystemTransmissionDTO);

        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        String requestDocClob = savedSourceSystemTransmission.getRequestDocument();

        assertEquals("Request Document: ", requestDoc, requestDocClob);

        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        PayrollServices.commitUnitOfWorkWithSecondary();

    }

    /**
     * Test  Transmission is successfully initialized
     */

    @Test
    public void testInitializeTransmissionSuccessfulSetIPAddress() {

        // Load Company
        PayrollServices.beginUnitOfWork();
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);
        sourceSystemTransmissionDTO.setIPAddress("123.123.123.1");

        String requestDoc = "TEST OFX";
        sourceSystemTransmissionDTO.setRequestDocument(requestDoc);
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                "123456", transmissionId, sourceSystemTransmissionDTO);

        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", processResult);
        SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
        PayrollServices.beginUnitOfWorkWithSecondary();
        SourceSystemTransmission savedSourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(sourceSystemTransmission.getId().toString());
        String requestDocClob = savedSourceSystemTransmission.getRequestDocument();
        assertEquals("Request Document: ", requestDoc, requestDocClob);

        assertEquals("Request Token: ", 1L, savedSourceSystemTransmission.getRequestToken());
        assertEquals("Transmission Type: ", TransmissionType.Sync, savedSourceSystemTransmission.getType());
        assertEquals("IP Address: ","123.123.123.1" , savedSourceSystemTransmission.getIPAddress());
        PayrollServices.commitUnitOfWorkWithSecondary();

    }

    @Test
    public void testRemoveNonAsciiCharacters() {
        ApplicationSecondary.beginUnitOfWork();
        SourceSystemTransmission sourceSystemTransmission = new SourceSystemTransmission();
        sourceSystemTransmission.setTransmissionIdentifier("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sourceSystemTransmission.setHost("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sourceSystemTransmission.setDescription("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sourceSystemTransmission.setApplicationId("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sourceSystemTransmission.setTaxTableId("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");

        sourceSystemTransmission = ApplicationSecondary.save(sourceSystemTransmission);
        ApplicationSecondary.commitUnitOfWork();

        sourceSystemTransmission = ApplicationSecondary.findById(com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission.class, sourceSystemTransmission.getId());


        Assert.assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sourceSystemTransmission.getTransmissionIdentifier());
        Assert.assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sourceSystemTransmission.getHost());
        Assert.assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sourceSystemTransmission.getDescription());
        Assert.assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sourceSystemTransmission.getApplicationId());
        Assert.assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sourceSystemTransmission.getTaxTableId());
    }
}
