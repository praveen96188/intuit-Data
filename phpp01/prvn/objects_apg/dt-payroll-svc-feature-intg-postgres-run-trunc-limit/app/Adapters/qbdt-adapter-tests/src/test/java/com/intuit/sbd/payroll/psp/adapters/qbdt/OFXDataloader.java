package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Date;
import java.util.List;

/**
 * OFX Dataloader for use with the QBDT Adapter Tests
 */
public class OFXDataloader {
    ObjectFactory objFactory = null;
    public static String dmEmpName = "Donovan McNabb";
    public static String alEmpName = "Abe Lincoln";
    public static final String companyPSID = "8574536";
    public static final String companyPassword = "test1234";

    public static final String DM_BANK_NAME = "Bank of Money";
    public static final String DM_BANK_ID = "113003842";
    public static final String DM_ACCT_ID = "0011992288";
    public static final String DM_ACCTTYPE = "SAVINGS";
    public static final BankAccountType DM_PSP_ACCTTYPE = BankAccountType.Savings;


    public static final String AL_PAYCHECK1_BANK_NAME = "Abe's Bank";
    public static final String AL_PAYCHECK1_BANK_ID = "113003842";
    public static final String AL_PAYCHECK1_ACCT_ID = "11122221111";
    public static final String AL_PAYCHECK1_ACCTTYPE = "SAVINGS";

    public static final String AL_PAYCHECK2_BANK_NAME = "Abe's Bank 2";
    public static final String AL_PAYCHECK2_BANK_ID = "113003842";
    public static final String AL_PAYCHECK2_ACCT_ID = "333322222233";
    public static final String AL_PAYCHECK2_ACCTTYPE = "CHECKING";

    public static final String OFX_NULL_STRING = "^@~*";

    public OFXDataloader() {
        objFactory = new ObjectFactory();
    }

    public OFX loadVoidCompany3Payroll(List<String> paycheckIdVoidList,String paycheckDate) {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadVoidCompany3PayrollMessage(paycheckIdVoidList,paycheckDate);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadCompany3WithCOINFOMODChangingAll() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCoInfoModCompany3MessageChangeAll();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadCompany3WithCOINFOMODChangeOnlyLegalNameAndAddress() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCoInfoModCompany3MessageChangeOnlyLegalNameAndAddress();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadZeroPayroll() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);
        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0088");
        ofxObj.setIPAYROLLMSGSRQV1(rtnPayrollUpdateMessage);
        IPAYROLLRQ payrollReq = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollReq);
        return ofxObj;
    }

    public IPAYROLLMSGSRQV1 loadVoidCompany3PayrollMessage(List<String> paycheckIdVoidList,String paycheckDate) {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0088");
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);
        IPAYROLLRUN payrollRun = objFactory.createIPAYROLLRUN();
        payrollRun.setIDTPAYCHKS(paycheckDate);
        payrollRequest.getIPAYROLLRUN().add(payrollRun);
        for (String paycheckVoidId : paycheckIdVoidList) {
            String empName;
            if (paycheckVoidId.compareTo("1")==0){
                empName = dmEmpName;
            } else {
                empName = alEmpName;
            }
            IPAYCHK paycheckMod = createPaycheckModVoid(paycheckVoidId,empName);
            payrollRun.getIPAYCHKMOD().add(paycheckMod);
        }
        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 loadCoInfoModCompany3MessageChangeOnlyLegalNameAndAddress() {

        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0099");
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);
        ICOINFOMOD coInfoModRequest = objFactory.createICOINFOMOD();
        payrollRequest.setICOINFOMOD(coInfoModRequest);

        String newLegalName = "New Legal Name";
        coInfoModRequest.setILEGALNAME(newLegalName);
        String newAddrLine1= "New Address Line 1";
        coInfoModRequest.setIADDR1(newAddrLine1);
        String newAddrLine2= "New Address Line 1";
        coInfoModRequest.setIADDR2(newAddrLine2);
        String newAddrCity= "New City";
        coInfoModRequest.setICITY(newAddrCity);
        String newAddrState= "NJ";
        coInfoModRequest.setISTATE(newAddrState);
        String newAddrZip= "23123";
        coInfoModRequest.setIPOSTALCODE(newAddrZip);

        return rtnPayrollUpdateMessage;
    }


    public IPAYROLLMSGSRQV1 loadCoInfoModCompany3MessageChangeAll() {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0099");
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);
        ICOINFOMOD coInfoModRequest = objFactory.createICOINFOMOD();
        payrollRequest.setICOINFOMOD(coInfoModRequest);

        BANKACCT bankAcctFromRequest = objFactory.createBANKACCT();
        String newBankAcctFromId = "3453453444";
        bankAcctFromRequest.setACCTID(newBankAcctFromId);
        String newBankAcctFromAcctType = "SAVINGS";
        String newBankAcctFromBankId = "9083459034";
        bankAcctFromRequest.setACCTTYPE(newBankAcctFromAcctType);
        bankAcctFromRequest.setBANKID(newBankAcctFromBankId);
        coInfoModRequest.setBANKACCTFROM(bankAcctFromRequest);

        String newBankAcctName = "Updated Bank Acct Name";
        coInfoModRequest.setIACCTNAME(newBankAcctName);
        String newAddressLine1 = "New Address Line 1";
        coInfoModRequest.setIADDR1(newAddressLine1);
        String newAddressLine2 = "New Address Line 2";
        coInfoModRequest.setIADDR2(newAddressLine2);
        String newBankName = "New Bank Name";
        coInfoModRequest.setIBANKNAME(newBankName);
        String newCity = "New City";
        coInfoModRequest.setICITY(newCity);

        ICONTACT contactRequest = objFactory.createICONTACT();
        String newContactFirstName = "NewFirst";
        contactRequest.setICONFIRSTNAME(newContactFirstName);
        String newContactLastName = "NewLast";
        contactRequest.setICONLASTNAME(newContactLastName);
        String newContactMI = "X";
        contactRequest.setICONMIDINI(newContactMI);
        String newContactTitle = "Mx";
        contactRequest.setICONTITLE(newContactTitle);
        String newContactEmail = "new_email@here.com";
        contactRequest.setIEMAIL(newContactEmail);
        String newContactFax = "NewFax";
        contactRequest.setIFAX(newContactFax);
        String newContactPhone = "NewPhone";
        contactRequest.setIPHONE(newContactPhone);
        coInfoModRequest.setICONTACT(contactRequest);

        String newFEIN = "NEW FEIN";
        coInfoModRequest.setIFEIN(newFEIN);
        String newLegalName = "NEW LEGAL NAME";
        coInfoModRequest.setILEGALNAME(newLegalName);
        String newMailAddrLine1 = "MA Line 1";
        coInfoModRequest.setIMAILADDR1(newMailAddrLine1);
        String newMailAddrLine2 = "MA Line 2";
        coInfoModRequest.setIMAILADDR2(newMailAddrLine2);
        String newMailAddrCity = "MA City";
        coInfoModRequest.setIMAILCITY(newMailAddrCity);
        String newMailPostalCode = "11223";
        coInfoModRequest.setIMAILPCODE(newMailPostalCode);
        String newMailState = "NM";
        coInfoModRequest.setIMAILSTATE(newMailState);
        String newPostalCode= "22334";
        coInfoModRequest.setIPOSTALCODE(newPostalCode);
        String newPrincFirstName = "NewPrinFirst";
        coInfoModRequest.setIPRINCFIRSTNAME(newPrincFirstName);
        String newPrincFirstLast = "NewPrinLast";
        coInfoModRequest.setIPRINCLASTNAME(newPrincFirstLast);
        String newPrincMI = "NewPrinMI";
        coInfoModRequest.setIPRINCMIDINI(newPrincMI);
        String newPrincTitle = "NewPrinTitle";
        coInfoModRequest.setIPRINCTITLE(newPrincTitle);
        String newQ3LiabAmt = "$123.12";
        coInfoModRequest.setIQ3TAXLIABAMT(newQ3LiabAmt);
        String newQ4LiabAmt = "$321.21";
        coInfoModRequest.setIQ4TAXLIABAMT(newQ4LiabAmt);
        String newRegNum = "22332123-123";
        coInfoModRequest.setIREGNUM(newRegNum);
        String newState = "VT";
        coInfoModRequest.setISTATE(newState);
        String newShipToAddrLine1 = "New Addr Line 1";
        coInfoModRequest.setISTRTADDR1(newShipToAddrLine1);
        String newShipToAddrLine2 = "New Addr Line 2";
        coInfoModRequest.setISTRTADDR2(newShipToAddrLine2);
        String newShipToAddrCity = "New ACity";
        coInfoModRequest.setISTRTCITY(newShipToAddrCity);
        String newShipToAddrZip = "12322";
        coInfoModRequest.setISTRTPCODE(newShipToAddrZip);
        String newShipToAddrState = "WV";
        coInfoModRequest.setISTRTSTATE(newShipToAddrState);

        

        return rtnPayrollUpdateMessage;
    }

    public OFX loadHappyPathOFXPayroll1() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCompany3PayrollMessagePayrollRun1();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCompany3PayrollMessagePayrollRun1();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long nextPaycheckId = Long.parseLong(company.getNextPaycheckId());

        for (IPAYROLLRUN payrollRun : payrollObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {


            SpcfCalendar sevenBusinessDaysOut = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(sevenBusinessDaysOut,7);
            String paycheckYYYYMMDD = sevenBusinessDaysOut.format("yyyyMMdd");
            payrollRun.setIDTPAYCHKS(paycheckYYYYMMDD);
            for (IPAYCHK paycheck: payrollRun.getIPAYCHK()) {
                paycheck.setIPAYCHKID(nextPaycheckId+++"");
            }
        }

        return ofxObj;
    }

    public OFX loadHappyPathOFXPayroll2() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCompany3PayrollMessagePayrollRun2();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadHappyPathSyncRequest() {
        return loadHappyPathSyncRequest("1");
    }
    public OFX loadHappyPathSyncRequest(String token) {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadSyncPayrollUpdateMessage(token);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadBalanceFile() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadBalanceFilePayrollUpdateMessage();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    private IPAYROLLMSGSRQV1 loadBalanceFilePayrollUpdateMessage() {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");

        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0099");
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        ICOINFOMOD coInfoMod = objFactory.createICOINFOMOD();
        coInfoMod.setITAXREADY("Y");
        coInfoMod.setIDTFILEQTRSTART("20080707");
        payrollRequest.setICOINFOMOD(coInfoMod);

        return rtnPayrollUpdateMessage;
    }

    public OFX loadHappyPathOFX() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCompany3PayrollMessage();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadHappyPathWithZeroDollarCheckOFX() {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCompany3PayrollWithZeroDollarCheckMessage();
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX loadHappyPathOFXWithSpecifiedToken(String token) {
        OFX ofxObj = objFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = loadHappyPathSignOnMessage();
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = loadCompany3PayrollMessage();
        payrollObj.getIPAYROLLUPDATERQ().setTOKEN(token);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }


    public SIGNONMSGSRQV1 loadHappyPathSignOnMessage() {
        return createSignOnMessage(companyPSID);
    }

    public SIGNONMSGSRQV1 createSignOnMessage(String username) {

        SIGNONMSGSRQV1 signOnRequestMessage = objFactory.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFactory.createSONRQ();

        signOnRequest.setAPPID(QBOFX.QB_APP_ID_VERSIONS.QBWPRO);
        signOnRequest.setAPPVER("50.00.R.3/20804#pro");
        String curDateOFXStr = QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        signOnRequest.setDTCLIENT(curDateOFXStr);
        signOnRequest.setIIPADDRESS("FileInfo:QB_data_engine_18:172.17.214.180#10180");
        signOnRequest.setIQBFILEID("c8e251053a984b3b9e107e8daa9bb640");
        signOnRequest.setIQBFILENAME("C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW");
        signOnRequest.setIQBUSERNAME("Admin");
        signOnRequest.setLANGUAGE(QBOFX.LANGUAGE);
        // This userid is the one loaded in the Core company dataloader
        signOnRequest.setUSERID(username);
        signOnRequest.setUSERPASS(companyPassword);

        signOnRequestMessage.setSONRQ(signOnRequest);
        return signOnRequestMessage;
    }

    public IPAYROLLMSGSRQV1 loadCompany3PayrollMessagePayrollRun1() {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0027");
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        IPAYROLLRUN payrollRun1 = loadCompany3PayrollRun1();
        payrollRequest.getIPAYROLLRUN().add(payrollRun1);
        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 loadCompany3PayrollMessagePayrollRun2() {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0028");
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        IPAYROLLRUN payrollRun2 = loadCompany3PayrollRun2();
        payrollRequest.getIPAYROLLRUN().add(payrollRun2);
        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 loadSyncPayrollUpdateMessage(String token) {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        rtnPayrollUpdateRequest.setTOKEN(token);
        rtnPayrollUpdateRequest.setREJECTIFMISSING("N");
        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 loadCompany3PayrollMessage() {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0026");
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        IPAYROLLRUN payrollRun1 = loadCompany3PayrollRun1();
        payrollRequest.getIPAYROLLRUN().add(payrollRun1);
        IPAYROLLRUN payrollRun2 = loadCompany3PayrollRun2();
        payrollRequest.getIPAYROLLRUN().add(payrollRun2);
        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 loadCompany3PayrollWithZeroDollarCheckMessage() {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = objFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = objFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = objFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0026");
        IPAYROLLRQ payrollRequest = objFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        IPAYROLLRUN payrollRun1 = loadCompany3PayrollRun1WithZeroDollarCheck();
        payrollRequest.getIPAYROLLRUN().add(payrollRun1);
        IPAYROLLRUN payrollRun2 = loadCompany3PayrollRun2();
        payrollRequest.getIPAYROLLRUN().add(payrollRun2);
        return rtnPayrollUpdateMessage;
    }






    public IPAYROLLRUN loadCompany3PayrollRun1WithZeroDollarCheck() {
        SpcfDecimal dmPaycheckAmt = new SpcfMoney("0.00");
        SpcfDecimal alPaycheck1Amt = new SpcfMoney("40.00");
        SpcfDecimal alPaycheck2Amt = new SpcfMoney("153.11");
        return loadCompany3PayrollRun1(dmPaycheckAmt,alPaycheck1Amt,alPaycheck2Amt);
    }

    public IPAYROLLRUN loadCompany3PayrollRun1() {
        SpcfDecimal dmPaycheckAmt = new SpcfMoney("927.69");
        SpcfDecimal alPaycheck1Amt = new SpcfMoney("40.00");
        SpcfDecimal alPaycheck2Amt = new SpcfMoney("153.11");
        return loadCompany3PayrollRun1(dmPaycheckAmt,alPaycheck1Amt,alPaycheck2Amt);
    }

    public IPAYROLLRUN loadCompany3PayrollRun1(SpcfDecimal dmPaycheckAmt
                                               ,SpcfDecimal alPaycheck1Amt
                                               ,SpcfDecimal alPaycheck2Amt) {
        IPAYROLLRUN payrollRun = objFactory.createIPAYROLLRUN();

        SpcfCalendar sixBusinessDaysOutSpcfCal = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(sixBusinessDaysOutSpcfCal,6);
        String sixBusinessDaysOutSpcfStr = sixBusinessDaysOutSpcfCal.format("yyyyMMdd");
        payrollRun.setIDTPAYCHKS(sixBusinessDaysOutSpcfStr);

        IPAYCHK dmPaycheck = loadDMPaycheck(sixBusinessDaysOutSpcfStr,dmPaycheckAmt,"1");
        payrollRun.getIPAYCHK().add(dmPaycheck);

        IPAYCHK alPaycheck = loadALPaycheck(sixBusinessDaysOutSpcfStr,alPaycheck1Amt,alPaycheck2Amt,"2");
        payrollRun.getIPAYCHK().add(alPaycheck);

        SpcfDecimal totalPayrollAmt = new SpcfMoney("0.00");
        totalPayrollAmt.add(dmPaycheckAmt);
        totalPayrollAmt.add(alPaycheck1Amt);
        totalPayrollAmt.add(alPaycheck2Amt);

        IDDADVICE ddAdvice = objFactory.createIDDADVICE();
        ddAdvice.setIDDAMT("$-"+totalPayrollAmt.toString());
        ddAdvice.getIDD().add(getDDTx(dmPaycheckAmt, dmEmpName, DM_BANK_ID,DM_ACCT_ID));
        ddAdvice.getIDD().add(getDDTx(alPaycheck1Amt, alEmpName, AL_PAYCHECK1_BANK_ID,AL_PAYCHECK1_ACCT_ID));
        ddAdvice.getIDD().add(getDDTx(alPaycheck2Amt, alEmpName, AL_PAYCHECK2_BANK_ID,AL_PAYCHECK2_ACCT_ID));

        payrollRun.setIDDADVICE(ddAdvice);
        return payrollRun;
    }

    public IPAYROLLRUN loadCompany3PayrollRun2() {
        IPAYROLLRUN payrollRun = objFactory.createIPAYROLLRUN();

        SpcfCalendar sixBusinessDaysOutSpcfCal = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(sixBusinessDaysOutSpcfCal,10);
        String tenBusinessDaysOutSpcfStr = sixBusinessDaysOutSpcfCal.format("yyyyMMdd");
        payrollRun.setIDTPAYCHKS(tenBusinessDaysOutSpcfStr);

        SpcfDecimal dmPaycheckAmt = new SpcfMoney("8091.11");
        IPAYCHK dmPaycheck = loadDMPaycheck(tenBusinessDaysOutSpcfStr,dmPaycheckAmt,"3");
        payrollRun.getIPAYCHK().add(dmPaycheck);

        SpcfDecimal alPaycheck1Amt = new SpcfMoney("100.00");
        SpcfDecimal alPaycheck2Amt = new SpcfMoney("2012.44");
        IPAYCHK alPaycheck = loadALPaycheck(tenBusinessDaysOutSpcfStr,alPaycheck1Amt,alPaycheck2Amt,"4");
        payrollRun.getIPAYCHK().add(alPaycheck);

        SpcfDecimal totalPayrollAmt = new SpcfMoney("0.00");
        totalPayrollAmt.add(dmPaycheckAmt);
        totalPayrollAmt.add(alPaycheck1Amt);
        totalPayrollAmt.add(alPaycheck2Amt);

        IDDADVICE ddAdvice = objFactory.createIDDADVICE();
        ddAdvice.setIDDAMT("$-"+totalPayrollAmt.toString());
        ddAdvice.getIDD().add(getDDTx(dmPaycheckAmt, dmEmpName,DM_BANK_ID,DM_ACCT_ID));
        ddAdvice.getIDD().add(getDDTx(alPaycheck1Amt, alEmpName,AL_PAYCHECK1_BANK_ID,AL_PAYCHECK1_ACCT_ID));
        ddAdvice.getIDD().add(getDDTx(alPaycheck2Amt, alEmpName,AL_PAYCHECK2_BANK_ID,AL_PAYCHECK2_ACCT_ID));

        payrollRun.setIDDADVICE(ddAdvice);
        return payrollRun;
    }

    public IDDADVICE loadDDAdvice(SpcfDecimal dmPaycheckAmt, String empName,String bankId,String routingNumber) {
        IDDADVICE ddAdvice = objFactory.createIDDADVICE();
        ddAdvice.setIDDAMT("$-"+dmPaycheckAmt.toString());
        ddAdvice.getIDD().add(getDDTx(dmPaycheckAmt,empName,bankId,routingNumber));
        return ddAdvice;
    }

    public IPAYCHK loadDMPaycheck(String paycheckDateStr,SpcfDecimal paycheckDollarAmt,String paycheckId) {
        IPAYCHK paycheck = objFactory.createIPAYCHK();

        paycheck.setIPAYCHKID(paycheckId);
        paycheck.setIEMPID("0");
        paycheck.setIDTTX(paycheckDateStr);
        paycheck.setIPAYCHKTYPE("PAYCHK");
        String empName;
        empName = dmEmpName;
        paycheck.setIEMPNAME(empName);
        paycheck.setICLASS(OFX_NULL_STRING);
        paycheck.setIACCTNAME("BofA1");
        paycheck.setIAMT("$0.00");
        IPAYCHKINFO paycheckInfo = objFactory.createIPAYCHKINFO();
        paycheck.setIPAYCHKINFO(paycheckInfo);

        paycheckInfo.setICHKNUM("TOPRINT");
        paycheckInfo.setIPRORATE("N");
        paycheckInfo.setISICKACCRUED(OFX_NULL_STRING);
        paycheckInfo.setIVACACCRUED(OFX_NULL_STRING);

        paycheck.setIVOID("N");
        paycheck.setIONSERVICE("Y");
        paycheck.setIDTPAYPDBEGIN("20071117");
        paycheck.setIDTPAYPDEND("20071130");
        paycheck.setIMEMO("Direct Deposit");
        paycheck.setICLEARED("2");

        IDDLINE ddLine = getDMDDLine(paycheckDollarAmt);
        paycheck.getIDDLINE().add(ddLine);

        return paycheck;
    }

    public IPAYCHK loadALPaycheck(String paycheckDateStr,SpcfDecimal splitAmt1,SpcfDecimal splitAmt2,String paycheckId) {
        IPAYCHK paycheck = objFactory.createIPAYCHK();

        paycheck.setIPAYCHKID(paycheckId);
        paycheck.setIEMPID("0");
        paycheck.setIDTTX(paycheckDateStr);
        paycheck.setIPAYCHKTYPE("PAYCHK");
        paycheck.setIEMPNAME(alEmpName);
        paycheck.setICLASS(OFX_NULL_STRING);
        paycheck.setIACCTNAME("Abe's Acct");
        paycheck.setIAMT("$0.00");
        IPAYCHKINFO paycheckInfo = objFactory.createIPAYCHKINFO();
        paycheck.setIPAYCHKINFO(paycheckInfo);

        paycheckInfo.setICHKNUM("TOPRINT");
        paycheckInfo.setIPRORATE("N");
        paycheckInfo.setISICKACCRUED(OFX_NULL_STRING);
        paycheckInfo.setIVACACCRUED(OFX_NULL_STRING);

        paycheck.setIVOID("N");
        paycheck.setIONSERVICE("Y");
        paycheck.setIDTPAYPDBEGIN("20071117");
        paycheck.setIDTPAYPDEND("20071130");
        paycheck.setIMEMO("Direct Deposit");
        paycheck.setICLEARED("0");

        IDDLINE alDDLine1 = getALDDLine1(splitAmt1);
        paycheck.getIDDLINE().add(alDDLine1);
        IDDLINE alDDLine2 = getALDDLine2(splitAmt2);
        paycheck.getIDDLINE().add(alDDLine2);

        return paycheck;
    }

    public IDD getDDTx(SpcfDecimal paycheckAmtStr,String empName,String bankId,String bankRoutingNumber) {
        IDD dd = objFactory.createIDD();

        BANKACCT ddBankAcctTo = objFactory.createBANKACCT();
        dd.setBANKACCTTO(ddBankAcctTo);

        ddBankAcctTo.setBANKID(bankId);
        ddBankAcctTo.setACCTID(bankRoutingNumber);
        ddBankAcctTo.setACCTTYPE("SAVINGS");

        dd.setIEMPID("0");
        dd.setIAMT("$-"+paycheckAmtStr.toString());
        dd.setIEMPNAME(empName);
        dd.setISSN("567-12-3456");
        return dd;
    }

    public IPAYCHK createPaycheckModVoid(String paycheckIdToVoid,String empName) {

        IPAYCHK paycheckMod = objFactory.createIPAYCHK();
        paycheckMod.setIACCTNAME("BofA2");
        paycheckMod.setICLASS(QBOFX.EMPTY_STR);
        paycheckMod.setICLEARED("2");
        paycheckMod.setIDTPAYPDBEGIN("20071117");
        paycheckMod.setIDTPAYPDEND("20071130");
        paycheckMod.setIEMPID("0");
        paycheckMod.setIEMPNAME(empName);
        paycheckMod.setIMEMO("VOID: Direct Deposit");
        paycheckMod.setIPAYCHKID(paycheckIdToVoid);
        IPAYCHKINFO paycheckInfo = objFactory.createIPAYCHKINFO();

        paycheckInfo.setICHKNUM(QBOFX.EMPTY_STR);
        paycheckInfo.setIPRORATE(QBOFX.OFX_YN.N);
        paycheckInfo.setISICKACCRUED(QBOFX.EMPTY_STR);
        paycheckInfo.setIVACACCRUED(QBOFX.EMPTY_STR);

        paycheckMod.setIPAYCHKINFO(paycheckInfo);
        paycheckMod.setIPAYCHKTYPE("PAYCHK");
        paycheckMod.setIVOID(QBOFX.OFX_YN.Y);

        return paycheckMod;
    }

    public IDDLINE getDMDDLine(SpcfDecimal paycheckDollarAmt) {
        IDDLINE ddLine = objFactory.createIDDLINE();

        IDDACCT ddLineDDAcct = objFactory.createIDDACCT();
        ddLine.setIDDACCT(ddLineDDAcct);

        ddLineDDAcct.setIACCTNAME(DM_BANK_NAME);
        ddLineDDAcct.setIAMT(OFX_NULL_STRING);

        BANKACCT ddLineDDAcctBankAcctTo = objFactory.createBANKACCT();
        ddLineDDAcct.setBANKACCTTO(ddLineDDAcctBankAcctTo);

        ddLineDDAcctBankAcctTo.setBANKID(DM_BANK_ID);
        ddLineDDAcctBankAcctTo.setACCTID(DM_ACCT_ID);
        ddLineDDAcctBankAcctTo.setACCTTYPE(DM_ACCTTYPE);

        ddLine.setIPITEMID("0");
        ddLine.setIAMT("$-"+paycheckDollarAmt.toString());
        return ddLine;
    }
    public IDDLINE getALDDLine1(SpcfDecimal netSplitAmt) {
        IDDLINE ddLine1 = objFactory.createIDDLINE();

        IDDACCT ddLine1DDAcct = objFactory.createIDDACCT();
        ddLine1.setIDDACCT(ddLine1DDAcct);

        ddLine1DDAcct.setIACCTNAME(AL_PAYCHECK1_BANK_NAME);
        ddLine1DDAcct.setIAMT("$"+netSplitAmt.toString());

        BANKACCT ddLine1DDAcctBankAcctTo = objFactory.createBANKACCT();
        ddLine1DDAcct.setBANKACCTTO(ddLine1DDAcctBankAcctTo);

        ddLine1DDAcctBankAcctTo.setBANKID(AL_PAYCHECK1_BANK_ID);
        ddLine1DDAcctBankAcctTo.setACCTID(AL_PAYCHECK1_ACCT_ID);
        ddLine1DDAcctBankAcctTo.setACCTTYPE(AL_PAYCHECK1_ACCTTYPE);

        ddLine1.setIPITEMID("0");
        ddLine1.setIAMT("$-"+netSplitAmt);
        return ddLine1;
    }

    public IDDLINE getALDDLine2(SpcfDecimal netSplitAmt) {

        IDDLINE ddLine2 = objFactory.createIDDLINE();

        IDDACCT ddLine2DDAcct = objFactory.createIDDACCT();
        ddLine2.setIDDACCT(ddLine2DDAcct);

        ddLine2DDAcct.setIACCTNAME(AL_PAYCHECK2_BANK_NAME);
        ddLine2DDAcct.setIAMT(OFX_NULL_STRING);

        BANKACCT ddLine2DDAcctBankAcctTo = objFactory.createBANKACCT();
        ddLine2DDAcct.setBANKACCTTO(ddLine2DDAcctBankAcctTo);

        ddLine2DDAcctBankAcctTo.setBANKID(AL_PAYCHECK2_BANK_ID);
        ddLine2DDAcctBankAcctTo.setACCTID(AL_PAYCHECK2_ACCT_ID);
        ddLine2DDAcctBankAcctTo.setACCTTYPE(AL_PAYCHECK2_ACCTTYPE);

        ddLine2.setIPITEMID("0");
        ddLine2.setIAMT("$-"+netSplitAmt.toString());

        return ddLine2;
    }



}
