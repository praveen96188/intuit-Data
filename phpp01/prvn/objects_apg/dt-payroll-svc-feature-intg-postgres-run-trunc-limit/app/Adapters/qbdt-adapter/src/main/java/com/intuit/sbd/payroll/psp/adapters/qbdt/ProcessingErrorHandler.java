package com.intuit.sbd.payroll.psp.adapters.qbdt;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;

import java.util.Date;

/**
 * @version 1.0
 * @created 27-Jan-2008 10:27:26
 */
public class ProcessingErrorHandler {

    private static com.intuit.sbd.payroll.psp.common.ofx.response.ObjectFactory objFactory = new com.intuit.sbd.payroll.psp.common.ofx.response.ObjectFactory();
    
    public ProcessingErrorHandler(){
	}

    public static class SPCFErrorCodes {
        public static String OFXError = "QBDT OFX ERROR";
    }
    
	public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX handleUpdateError(String pPSID, String pTRNUID, ErrorMessage processingError){
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOfx = objFactory.createOFX();

        SIGNONMSGSRSV1 signOnResponseMsg = objFactory.createSIGNONMSGSRSV1();
        SONRS signOnResponse = objFactory.createSONRS();
        signOnResponse.setLANGUAGE(QBOFX.LANGUAGE);
        String curDateOFXStr = QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        signOnResponse.setDTSERVER(curDateOFXStr);
        signOnResponseMsg.setSONRS(signOnResponse);

        STATUS signOnStatus = objFactory.createSTATUS();
        signOnStatus.setCODE(QBOFX.SUCCESS_STATUS_CODE);
        signOnStatus.setSEVERITY(QBOFX.MESSAGE_SEVERITY.INFO);
        signOnResponse.setSTATUS(signOnStatus);

        rtnOfx.setSIGNONMSGSRSV1(signOnResponseMsg);
        IPAYROLLMSGSRSV1 payrollUpdateMsgResponseObj = objFactory.createIPAYROLLMSGSRSV1();
        IPAYROLLUPDATERS payrollUpdateResponseObj = objFactory.createIPAYROLLUPDATERS();                         
        
        Company company = Company.findCompany(pPSID, SourceSystemCode.QBDT);
        payrollUpdateResponseObj.setTOKEN(company.getCurrentToken() + "");
        payrollUpdateResponseObj.setIEMPNEXTID(company.getNextEmployeeId());
        payrollUpdateResponseObj.setIPAYCHKNEXTID(company.getNextPaycheckId());
        payrollUpdateResponseObj.setIPAYROLLTXNEXTID(company.getNextPayrollTransactionId());
        payrollUpdateResponseObj.setIPITEMNEXTID(company.getNextPayrollItemId());
        
        IPAYROLLTRNRS payrollTxResponseObj = objFactory.createIPAYROLLTRNRS();
        payrollTxResponseObj.setTRNUID(pTRNUID);

        STATUS statusObj = objFactory.createSTATUS();
        statusObj.setCODE(processingError.getOfxErrorCode());
        statusObj.setMESSAGE(processingError.getErrorDescription());
        statusObj.setSEVERITY(QBOFX.MESSAGE_SEVERITY.ERROR);
        payrollTxResponseObj.setSTATUS(statusObj);

        payrollUpdateResponseObj.setIPAYROLLTRNRS(payrollTxResponseObj);
        payrollUpdateMsgResponseObj.setIPAYROLLUPDATERS(payrollUpdateResponseObj);
        rtnOfx.setIPAYROLLMSGSRSV1(payrollUpdateMsgResponseObj);

        return rtnOfx;
	}

    /**
     * Create an OFX response with the sign on error information.
     * @param errorSignOnMsg
     * @return
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX handleSignOnError(ErrorMessage errorSignOnMsg){
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOfx = objFactory.createOFX();

        SIGNONMSGSRSV1 signOnResponseMsg = objFactory.createSIGNONMSGSRSV1();
        SONRS signOnResponse = objFactory.createSONRS();
        signOnResponse.setLANGUAGE(QBOFX.LANGUAGE);
        String curDateOFXStr = QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        signOnResponse.setDTSERVER(curDateOFXStr);

        STATUS statusObj = objFactory.createSTATUS();
        statusObj.setCODE(errorSignOnMsg.getOfxErrorCode());
        statusObj.setMESSAGE(errorSignOnMsg.getErrorDescription());
        statusObj.setSEVERITY(QBOFX.MESSAGE_SEVERITY.ERROR);

        signOnResponse.setSTATUS(statusObj);
        signOnResponseMsg.setSONRS(signOnResponse);
        rtnOfx.setSIGNONMSGSRSV1(signOnResponseMsg);

        return rtnOfx;
	}

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX handleOfxParsingError(ErrorMessage errorSignOnMsg, String requestStr, TransmissionType pTransmissionType){
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOfx = objFactory.createOFX();

        SIGNONMSGSRSV1 signOnResponseMsg = objFactory.createSIGNONMSGSRSV1();
        SONRS signOnResponse = objFactory.createSONRS();
        signOnResponse.setLANGUAGE(QBOFX.LANGUAGE);
        String curDateOFXStr = QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        signOnResponse.setDTSERVER(curDateOFXStr);

        STATUS signOnStatus = objFactory.createSTATUS();
        if(pTransmissionType == null || pTransmissionType.in(TransmissionType.Sync, TransmissionType.UsageSync, TransmissionType.Unknown)) {
            signOnStatus.setCODE(errorSignOnMsg.getOfxErrorCode());
            signOnStatus.setSEVERITY(QBOFX.MESSAGE_SEVERITY.ERROR);
            signOnStatus.setMESSAGE(errorSignOnMsg.getErrorDescription());
        } else {
            signOnStatus.setCODE(QBOFX.SUCCESS_STATUS_CODE);
            signOnStatus.setSEVERITY(QBOFX.MESSAGE_SEVERITY.INFO);
        }

        signOnResponse.setSTATUS(signOnStatus);
        signOnResponseMsg.setSONRS(signOnResponse);
        rtnOfx.setSIGNONMSGSRSV1(signOnResponseMsg);

        IPAYROLLMSGSRSV1 iPAYROLLMSGSRSV1 = objFactory.createIPAYROLLMSGSRSV1();

        IPAYROLLUPDATERS iPAYROLLUPDATERS = objFactory.createIPAYROLLUPDATERS();

        QBDTProcessResult<String> stringPR = QBDTRequestProcessor.retrieveStringFromRequestString(requestStr, "<TOKEN>");
        iPAYROLLUPDATERS.setTOKEN(stringPR.isSuccess() ? stringPR.getResult() : "");

        stringPR = QBDTRequestProcessor.retrieveCompanyPSIDFromRequestString(requestStr);
        if (stringPR.isSuccess()) {
            Company co = Company.findCompany(stringPR.getResult(), SourceSystemCode.QBDT);
            if (co != null) {
                iPAYROLLUPDATERS.setIEMPNEXTID(co.getNextEmployeeId());
                iPAYROLLUPDATERS.setIPAYCHKNEXTID(co.getNextPaycheckId());
                iPAYROLLUPDATERS.setIPAYROLLTXNEXTID(co.getNextPayrollTransactionId());
                iPAYROLLUPDATERS.setIPITEMNEXTID(co.getNextPayrollItemId());
            }

        }

        if(pTransmissionType == null || pTransmissionType.in(TransmissionType.PayrollSubmission, TransmissionType.UsageSend, TransmissionType.BalanceFile)) {
            IPAYROLLTRNRS iPAYROLLTRNRS = objFactory.createIPAYROLLTRNRS();
            stringPR = QBDTRequestProcessor.retrieveStringFromRequestString(requestStr, "<TRNUID>");
            iPAYROLLTRNRS.setTRNUID(stringPR.isSuccess() ? stringPR.getResult() : "");

            STATUS payrollStatus = objFactory.createSTATUS();
            payrollStatus.setCODE(errorSignOnMsg.getOfxErrorCode());
            payrollStatus.setSEVERITY(QBOFX.MESSAGE_SEVERITY.ERROR);
            payrollStatus.setMESSAGE(errorSignOnMsg.getErrorDescription());
            iPAYROLLTRNRS.setSTATUS(payrollStatus);

            iPAYROLLUPDATERS.setIPAYROLLTRNRS(iPAYROLLTRNRS);
        }

        iPAYROLLMSGSRSV1.setIPAYROLLUPDATERS(iPAYROLLUPDATERS);
        rtnOfx.setIPAYROLLMSGSRSV1(iPAYROLLMSGSRSV1);

        return rtnOfx;
	}

    /**
     * Handle undercoerable error
     *
     * @return
     */
    public static String getUnrecoverableProcessingErrorString() {
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX resposneOFXObj = handleSignOnError(ErrorMessages.UnexpectedError(null));
        String responseStr = OFXManager.javaResponseToOFX(resposneOFXObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        return responseStr;
    }

    public static String getUnsupportedCompressionProcessingErrorString() {
        String unsupportedString = "Any Quickbooks version older than 2010";
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX resposneOFXObj = handleSignOnError(ErrorMessages.QBVersionSunsetted(unsupportedString, unsupportedString, "", ""));
        return OFXManager.javaResponseToOFX(resposneOFXObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
    }
}