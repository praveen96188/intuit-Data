package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTRNRQ;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jul 30, 2008
 * Time: 10:27:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceMigrationTests {
    @BeforeClass
    public static void beforeClass() {

    }

    @AfterClass
    public static void afterClass() {

    }

    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void testAS400BalanceFileHappyPath() throws Exception {
        IMocksControl ctrl = createStrictControl();
        ISocketManager mockSocket = ctrl.createMock(ISocketManager.class);
        mockSocket.open((String)anyObject(), anyInt(), anyInt(), anyInt());
        mockSocket.processRequest((String) anyObject());
        String responseOFX = "<OFX>\n" +
                " <SIGNONMSGSRSV1>\n" +
                "  <SONRS>\n" +
                "   <STATUS>\n" +
                "    <CODE>0\n" +
                "    <SEVERITY>INFO\n" +
                "   </STATUS>\n" +
                "   <DTSERVER>20080725002552\n" +
                "   <LANGUAGE>ENG\n" +
                "  </SONRS>\n" +
                " </SIGNONMSGSRSV1>\n" +
                " <I.PAYROLLMSGSRSV1>\n" +
                "  <I.PAYROLLUPDATERS>\n" +
                "   <TOKEN>100\n" +
                "   <I.PAYROLLTXNEXTID>4\n" +
                "   <I.PAYCHKNEXTID>3\n" +
                "   <I.EMPNEXTID>1\n" +
                "   <I.PITEMNEXTID>1\n" +
                "  </I.PAYROLLUPDATERS>\n" +
                " </I.PAYROLLMSGSRSV1>\n" +
                "</OFX>";
        expectLastCall().andReturn(responseOFX);
        mockSocket.close();
        mockSocket.open((String)anyObject(), anyInt(), anyInt(), anyInt());
        mockSocket.processRequest((String) anyObject());

        /* This is not a real balance file, but the
        *      important piece is that the string does not
        *     contain <SEVERITY>ERROR.
        */
        responseOFX = "<OFX>\n" +
                " <SIGNONMSGSRSV1>\n" +
                "  <SONRS>\n" +
                "   <STATUS>\n" +
                "    <CODE>0\n" +
                "    <SEVERITY>INFO\n" +
                "   </STATUS>\n" +
                "   <DTSERVER>20080725002552\n" +
                "   <LANGUAGE>ENG\n" +
                "  </SONRS>\n" +
                " </SIGNONMSGSRSV1>\n" +
                " <I.PAYROLLMSGSRSV1>\n" +
                "  <I.PAYROLLUPDATERS>\n" +
                "   <TOKEN>101\n" +
                "   <I.PAYROLLTXNEXTID>4\n" +
                "   <I.PAYCHKNEXTID>3\n" +
                "   <I.EMPNEXTID>1\n" +
                "   <I.PITEMNEXTID>1\n" +
                "  </I.PAYROLLUPDATERS>\n" +
                " </I.PAYROLLMSGSRSV1>\n" +
                "</OFX>";
        expectLastCall().andReturn(responseOFX);
        mockSocket.close();
        ctrl.replay();


        Company company = new Company();
        company.setSourceSystemCd(SourceSystemCode.QBDT);
        company.setSourceCompanyId("8574536");
        DataLoadServices.addTaxService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX balanceFileOFX = ofxDataloader.loadBalanceFile();
        String ofxResponseStr = QBDTTestHelper.submitQBDTRequestStringResponse(balanceFileOFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String signOnSeverity = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnSeverity);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        assertEquals(ServiceSubStatusCode.PendingFirstPayroll, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        assertEquals(company.getService(ServiceCode.Tax).getStatusCd(), ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAS400BalanceFileProcessingReturnsError() throws Exception {
        IMocksControl ctrl = createStrictControl();
        ISocketManager mockSocket = ctrl.createMock(ISocketManager.class);
        mockSocket.open((String)anyObject(), anyInt(), anyInt(), anyInt());
        mockSocket.processRequest((String) anyObject());
        String responseOFX = "<OFX>\n" +
                " <SIGNONMSGSRSV1>\n" +
                "  <SONRS>\n" +
                "   <STATUS>\n" +
                "    <CODE>0\n" +
                "    <SEVERITY>INFO\n" +
                "   </STATUS>\n" +
                "   <DTSERVER>20080725002552\n" +
                "   <LANGUAGE>ENG\n" +
                "  </SONRS>\n" +
                " </SIGNONMSGSRSV1>\n" +
                " <I.PAYROLLMSGSRSV1>\n" +
                "  <I.PAYROLLUPDATERS>\n" +
                "   <TOKEN>4\n" +
                "   <I.PAYROLLTXNEXTID>4\n" +
                "   <I.PAYCHKNEXTID>3\n" +
                "   <I.EMPNEXTID>1\n" +
                "   <I.PITEMNEXTID>1\n" +
                "  </I.PAYROLLUPDATERS>\n" +
                " </I.PAYROLLMSGSRSV1>\n" +
                "</OFX>";
        expectLastCall().andReturn(responseOFX);
        mockSocket.close();
        mockSocket.open((String)anyObject(), anyInt(), anyInt(), anyInt());
        mockSocket.processRequest((String) anyObject());
        responseOFX = "<OFX>\n" +
                " <SIGNONMSGSRSV1>\n" +
                "  <SONRS>\n" +
                "   <STATUS>\n" +
                "    <CODE>101\n" +
                "    <SEVERITY>ERROR\n" +
                "    <MESSAGE>The ID and/or PIN you entered are invalid. Please check your ID and PIN and try again.\n" +
                "   </STATUS>\n" +
                "   <DTSERVER>20080725002636\n" +
                "   <LANGUAGE>ENG\n" +
                "  </SONRS>\n" +
                " </SIGNONMSGSRSV1>\n" +
                "</OFX>";
        expectLastCall().andReturn(responseOFX);
        mockSocket.close();
        ctrl.replay();

        Company company = new Company();
        company.setSourceSystemCd(SourceSystemCode.QBDT);
        company.setSourceCompanyId("8574536");
        DataLoadServices.addTaxService(company);

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX balanceFileOFX = ofxDataloader.loadBalanceFile();

        String orfRequestStr = OFXManager.javaRequestToOFX(balanceFileOFX,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        QBDTTestHelper.submitQBDTRequestStringResponse(orfRequestStr, false);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        assertEquals(ServiceSubStatusCode.PendingFirstPayroll, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void onHoldCustomerSendsBalanceFile() throws Exception {        
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                              OFXDataloader.companyPSID,
                                                                              ServiceSubStatusCode.Fraud);
        assertTrue(result.isSuccess());
        PayrollServices.commitUnitOfWork();
        OFXDataloader ofxDataLoader = new OFXDataloader();
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balanceFileObj = ofxDataLoader.loadBalanceFile();
        QBDTTestHelper.processRequestPayrollError(balanceFileObj,ErrorMessages.BalanceFileReceivedForOnHoldClient(ServiceSubStatusCode.Fraud.toString()));
    }

    @Test
    public void clientNotOnHoldWithNonResoledNocReturns() throws Exception {
        // Setup an NOC, which will create the return and put the client on hold.
        NoticeOfChangeTests.setupOffloadAndNOCPayroll(false,"C01");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        TransactionReturn transactionReturn = assertOne(ACHReturnsDataLoader.persistTransactionReturns(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerDdDebit), "R01", "NSF"));
        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);
        company.removeOnHoldReason(ServiceSubStatusCode.AchRejectR1R9);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        // Take the client off hold, which a Rep can do in DDM.
        PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBDT,OFXDataloader.companyPSID, ServiceCode.DirectDeposit, ServiceSubStatusCode.ActiveCurrent);


        DomainEntitySet<FinancialTransaction> coFnTxns = FinancialTransaction.findFinancialTransactions(company,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null);

        for (FinancialTransaction fnTx : coFnTxns) {
            if (fnTx.getCurrentTransactionState().getTransactionStateCd()==TransactionStateCode.Executed) {
                fnTx.updateFinancialTransactionState(TransactionStateCode.Completed);
            }
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX balanceFileOFX = ofxDataloader.loadBalanceFile();
        QBDTTestHelper.processRequestPayrollError(balanceFileOFX, ErrorMessages.BalanceFileRejectUnresolvedBankReturns());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        assertFalse(company.isCompanyCancelled());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void balanceFileSentWithPendingPayroll() throws Exception {
        QBDTTestHelper.processOFXPayrollRequestHappyPath();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX balanceFileOFX = ofxDataloader.loadBalanceFile();
        String ofxResponseStr = QBDTTestHelper.submitQBDTRequestStringResponse(balanceFileOFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String signOnSeverity = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnSeverity);
        assertNull(ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());


        PayrollServices.beginUnitOfWork();        
        company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        assertEquals(ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void balanceFileSentWithNonCompletedFnTx() throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();

        QBDTTestHelper.offloadCompanyPayroll(ofxDataloader.loadHappyPathOFX());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX balanceFileOFX = ofxDataloader.loadBalanceFile();
        String ofxResponseStr = QBDTTestHelper.submitQBDTRequestStringResponse(balanceFileOFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String signOnSeverity = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnSeverity);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        assertEquals(ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testTaxServerStatusInITAXSERVSTATUS() throws Exception {
        // Process a sync request to handle the bank events.
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123", false, ServiceCode.Tax);
        CompanyBankAccount companyBankAccount = DataLoadServices.addCompanyBankAccount(company, true);
        PayrollServices.beginUnitOfWork();
        companyBankAccount = Application.refresh(companyBankAccount);
        SpcfCalendar verificationInitiationDate = companyBankAccount.getVerificationTransactions().get(0).getMoneyMovementTransaction().getInitiationDate();
        SpcfCalendar verificationSettlementDate = companyBankAccount.getVerificationTransactions().get(0).getSettlementDate();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, verificationInitiationDate);

        DataLoadServices.setPSPDate(verificationSettlementDate);

        DataLoadServices.verifyCompanyBankAccount(companyBankAccount);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        company = DataLoadServices.refreshCompany(company);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX origSyncResponseOFX = QBDTTestHelper.submitSyncRequest(company, company.getCurrentToken()-1, true);
        assertEquals(QBOFX.TAX_MODES.VERIFIED, origSyncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE());
    }

    @Test
    public void testDDToAssistedMigrationWithCancelledAssistedService() throws Exception {
        String psid = OFXDataloader.companyPSID;
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        // cancel the tax service
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyService companyService = company.getCompanyService(ServiceCode.Tax);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Assert.assertEquals("tax service", ServiceSubStatusCode.Cancelled, company.getService(ServiceCode.Tax).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        ofx.getSIGNONMSGSRQV1().getSONRQ().setUSERPASS(OFXDataloader.companyPassword);

        QBDTTestHelper.processRequestPayrollError(ofx, ErrorMessages.DDCustomerBalanceFileError());
    }

    @Test
    public void testDDToAssistedMigrationWithCancelledAssistedService_VoidPayroll() {
        DataLoadServices.setPSPDate(2011, 1, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.DirectDeposit);

        // cancel the dd service
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        company = DataLoadServices.refreshCompany(company);

        OFX voidOfx = new OFX();
        List<IPAYROLLRUN> voidPayrolls = new ArrayList<IPAYROLLRUN>();
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            IPAYROLLRUN voidIpayrollrun = new IPAYROLLRUN();
            voidIpayrollrun.setIDTPAYCHKS(ipayrollrun.getIDTPAYCHKS());
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                IPAYCHK ipaychkmod = new IPAYCHK();
                ipaychkmod.setIPAYCHKID(ipaychk.getIPAYCHKID());
                ipaychkmod.setIEMPID(ipaychk.getIEMPID());
                ipaychkmod.setIPAYCHKTYPE(ipaychk.getIPAYCHKTYPE());
                ipaychkmod.setIEMPNAME(ipaychk.getIEMPNAME());
                ipaychkmod.setICLASS(ipaychk.getICLASS());
                ipaychkmod.setIACCTNAME(ipaychk.getIACCTNAME());
                ipaychkmod.setIPAYCHKINFO(ipaychk.getIPAYCHKINFO());
                ipaychkmod.getIPAYCHKINFO().setICHKNUM("c" + ipaychkmod.getIPAYCHKINFO().getICHKNUM());
                ipaychkmod.setIVOID("Y");
                ipaychkmod.setIDTPAYPDBEGIN(ipaychk.getIDTPAYPDBEGIN());
                ipaychkmod.setIDTPAYPDEND(ipaychk.getIDTPAYPDEND());
                ipaychkmod.setIMEMO(ipaychk.getIMEMO());
                ipaychkmod.setICLEARED("9");
                ipaychkmod.setIONSERVICE("Y");
                voidIpayrollrun.getIPAYCHKMOD().add(ipaychkmod);
            }
            voidPayrolls.add(voidIpayrollrun);
        }
        voidOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                voidPayrolls);
        voidOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(voidOfx);
    }

    @Test
    public void testBalanceFileRejected_PendingSetup() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyPIN(company, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals(ServiceSubStatusCode.PendingSetup, company.getCompanyService(ServiceCode.Tax).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(ofx, false);
        String payrollSeverity = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
        assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR, payrollSeverity);
        String errorMessage = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE();
        assertEquals(ErrorMessages.BalanceFileRejectPendingSetup().getErrorDescription(), errorMessage);
    }

    @Test
    public void testBalanceFileRejected_PendingSetup2() {
        DataLoadServices.setPSPDate(2012, 10, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyPIN(company, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals(ServiceSubStatusCode.PendingSetup, company.getCompanyService(ServiceCode.Tax).getStatusCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 1));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO, false, false, false);
        String payrollSeverity = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
        assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR, payrollSeverity);
        String errorMessage = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE();
        assertEquals(ErrorMessages.BalanceFileRejectPendingSetup().getErrorDescription(), errorMessage);
    }

}
