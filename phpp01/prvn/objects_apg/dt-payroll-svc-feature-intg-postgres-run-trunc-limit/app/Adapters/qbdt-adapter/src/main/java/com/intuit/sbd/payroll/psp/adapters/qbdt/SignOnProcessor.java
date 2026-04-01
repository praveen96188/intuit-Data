package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.request.SIGNONMSGSRQV1;
import com.intuit.sbd.payroll.psp.common.ofx.response.SIGNONMSGSRSV1;
import com.intuit.sbd.payroll.psp.common.ofx.response.SONRS;
import com.intuit.sbd.payroll.psp.common.ofx.response.STATUS;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class handles the tasks related to the sign on OFX request.
 * <p/>
 * 27-Jan-2008 10:27:28
 */
public class SignOnProcessor {

    private String mTransmissionId;
    private Company mCompany = null;

    // Constants
    public static class PSP_ERROR_MESSAGE_IDS {
        public static final String INVALID_PIN = "292";
        public static final String ACCOUNT_LOCKED = "293";
        public static final String INVALID_EIN = "12006";
        public static final String INVALID_SubscriptionNumber = "12007";
    }

    /*
    * SignOnProcessor
    */
    public SignOnProcessor(String pTransmissionId, Company pCompany) {
        mTransmissionId = pTransmissionId;
        mCompany = pCompany;
    }

    /*
     * Process the sign on request OFX.
     *
     * @param pSignOnRequestObj - Sign on request OFX JAXB object.
     * @param pOfxRequest - now the whole OFX is needed to sign on
     * @return - QBDTProcessResult<SIGNONMSGSRSV1>
     */
    public QBDTProcessResult<SIGNONMSGSRSV1> processSignOnRequest(SIGNONMSGSRQV1 pSignOnRequestObj, OFX pOfxRequest) {
        QBDTProcessResult<SIGNONMSGSRSV1> processResult = new QBDTProcessResult<SIGNONMSGSRSV1>();
        OFXAPPVERObject ofxAPPVERObject = new OFXAPPVERObject(pSignOnRequestObj.getSONRQ().getAPPVER());

        if (!ofxAPPVERObject.isQBVersionActive()) {
            processResult.setSuccess(false);
            ErrorMessage errMsg = getQuickBooksDiscoMessage(ofxAPPVERObject.getQBVersionStr());
            processResult.setMessage(errMsg);
        } else if (!ofxAPPVERObject.isQBBuildSupported()) {
            processResult.setSuccess(false);
            ErrorMessage errMsg = ErrorMessages.QBReleaseNotSupported(ofxAPPVERObject.getQBVersionStr());
            processResult.setMessage(errMsg);
        }  else if (!ofxAPPVERObject.isTaxTableSupported()) {
            processResult.setSuccess(false);
            ErrorMessage errMsg = ErrorMessages.QBTaxTableNotSupported(ofxAPPVERObject.getTaxTableId());
            processResult.setMessage(errMsg);
        } else {
            QBDTProcessResult verifyLoginInfoPR = verifyLoginInfo(pSignOnRequestObj, pOfxRequest, ofxAPPVERObject);
            if (!verifyLoginInfoPR.isSuccess()) {
                processResult.setSuccess(false);
                processResult.setMessage(verifyLoginInfoPR.getMessage());
            } else {
                processResult.setCredentialType((CredentialType) verifyLoginInfoPR.getResult());
            }

        }

        if (!processResult.isSuccess()) {
            QBDTCompanyEventDTO eventDTO = new QBDTCompanyEventDTO();
            eventDTO.setCompany(mCompany);
            eventDTO.setEventTypeCode(EventTypeCode.PayrollRejected);
            eventDTO.setTransmissionId(mTransmissionId);
            eventDTO.setErrMsg(processResult.getMessage().getErrorDescription());
            processResult.addCompanyEvent(eventDTO);
            return processResult;
        }

        boolean validateQuickBooksFileId = SystemParameter.findBooleanValue(SystemParameter.Code.QBDT_VALIDATE_FILE_ID, false);
        if (validateQuickBooksFileId) {
            String clientFileId = QBOFX.getQBFileId(pSignOnRequestObj.getSONRQ().getIQBFILEID());
            String serverFileId = mCompany.getQuickbooksInfo().getFileId();
            if (serverFileId != null) {
                serverFileId = serverFileId.trim();
                if (!ObjectUtils.equals(clientFileId, serverFileId)) {
                    processResult.setSuccess(false);
                    ErrorMessage errMsg = ErrorMessages.QBFileIdChanged(serverFileId, clientFileId);
                    processResult.setMessage(errMsg);
                    QBDTCompanyEventDTO eventDTO = new QBDTCompanyEventDTO();
                    eventDTO.setCompany(mCompany);
                    eventDTO.setEventTypeCode(EventTypeCode.PayrollRejected);
                    eventDTO.setTransmissionId(mTransmissionId);
                    eventDTO.setErrMsg(errMsg.getErrorDescription());
                    processResult.addCompanyEvent(eventDTO);
                    return processResult;
                }
            }
        }

        return createSuccessfulSignOnMsg(processResult.getCredentialType());
    }

    /*
     * Create successful sign-on response.
     *
     * @return - Return Sign on message OFX JAXB response.
     */
    QBDTProcessResult<SIGNONMSGSRSV1> createSuccessfulSignOnMsg(CredentialType pCredentialType) {
        QBDTProcessResult<SIGNONMSGSRSV1> processResult = new QBDTProcessResult<SIGNONMSGSRSV1>();
        processResult.setSuccess(true);

        SIGNONMSGSRSV1 signOnResponseMsg = createSuccessfulSignOn();

        processResult.setSuccess(true);
        processResult.setResult(signOnResponseMsg);
        processResult.setCredentialType(pCredentialType);
        return processResult;
    }

    public static SIGNONMSGSRSV1 createSuccessfulSignOn() {
        com.intuit.sbd.payroll.psp.common.ofx.response.ObjectFactory objFactory = new com.intuit.sbd.payroll.psp.common.ofx.response.ObjectFactory();
        SIGNONMSGSRSV1 signOnResponseMsg = objFactory.createSIGNONMSGSRSV1();

        SONRS signOnResponse = objFactory.createSONRS();

        String serverDTTM = QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));

        signOnResponse.setDTSERVER(serverDTTM);
        signOnResponse.setLANGUAGE(QBOFX.LANGUAGE);

        STATUS statusObj = objFactory.createSTATUS();
        statusObj.setCODE(QBOFX.SUCCESS_STATUS_CODE);
        statusObj.setSEVERITY(QBOFX.MESSAGE_SEVERITY.INFO);

        signOnResponse.setSTATUS(statusObj);

        signOnResponseMsg.setSONRS(signOnResponse);
        return signOnResponseMsg;
    }

    /**
     * This verifies that the userid and password within the sign-on OFX
     * both match what is in PSP.  If the user id does not match OR
     * the password is not correct for that userid, the same generic
     * error is returned in the ProcessResult.
     *
     * @param pSignOnRequestObj
     * @param pOfxRequest
     * @param pOfxAPPVERObject
     * @return - Empty QBDTProcessResult containing only success/failure.
     */
    QBDTProcessResult verifyLoginInfo(SIGNONMSGSRQV1 pSignOnRequestObj, OFX pOfxRequest, OFXAPPVERObject pOfxAPPVERObject) {
        QBDTProcessResult processResult = new QBDTProcessResult();

        String username = pSignOnRequestObj.getSONRQ().getUSERID();
        String password = pSignOnRequestObj.getSONRQ().getUSERPASS();

        CredentialType credentialType = null;

        // deal with ping
        boolean pinProvided = password != null && !password.equals("");
        if (pinProvided) {
            credentialType = CredentialType.Pin;
            ProcessResult<Company> pspAuthPR = PayrollServices.subscriptionManager.verifyCompanyPIN(SourceSystemCode.QBDT, username, password);
            if (!pspAuthPR.isSuccess()) {
                processResult.setSuccess(false);
                MessageList errMsgList = pspAuthPR.getMessages();
                if (errMsgList.size() == 1) {
                    Message errMsgObj = errMsgList.get(0);
                    // 292 = PinNotRecognized
                    if (errMsgObj.getMessageCode().compareTo(PSP_ERROR_MESSAGE_IDS.ACCOUNT_LOCKED) == 0) {
                        ErrorMessage errMsg = ErrorMessages.MaxPinRetryError();
                        processResult.setMessage(errMsg);
                        return processResult;
                    }
                }
                ErrorMessage errMsg = ErrorMessages.AuthenticationFailedError();
                processResult.setMessage(errMsg);
                return processResult;
            }

            //Check if transmissions are disabled
            Company company = pspAuthPR.getResult();
            if (company.getQuickbooksInfo() != null && !company.getQuickbooksInfo().getAllowTransmissions()) {
                ErrorMessage errMsg = ErrorMessages.TransmissionsDisabled();
                processResult.setMessage(errMsg);
                return processResult;
            }
        }

        // deal with secondary credential
        String ein = pSignOnRequestObj.getSONRQ().getIRQEIN();
        String subscriptionNum = pSignOnRequestObj.getSONRQ().getISUBSCRIPTIONNUM();
        boolean secondaryCredProvided = (ein != null && !ein.equals("")) || (subscriptionNum != null && !subscriptionNum.equals(""));
        if (!pinProvided && secondaryCredProvided) {
            if (credentialType == null) {
                credentialType = CredentialType.Secondary;
            }
            ProcessResult pspAuthPR = PayrollServices.subscriptionManager.verifyCompanyEIN(SourceSystemCode.QBDT, username, ein, subscriptionNum);
            if (!pspAuthPR.isSuccess()) {
                processResult.setSuccess(false);
                ErrorMessage errMsg = ErrorMessages.AuthenticationFailedError();
                MessageList errMsgList = pspAuthPR.getMessages();
                if (errMsgList.size() == 1) {
                    Message errMsgObj = errMsgList.get(0);

                    if (errMsgObj.getMessageCode().compareTo(PSP_ERROR_MESSAGE_IDS.INVALID_EIN) == 0) {
                        errMsg = ErrorMessages.InvalidEINError();
                    } else if (errMsgObj.getMessageCode().compareTo(PSP_ERROR_MESSAGE_IDS.INVALID_SubscriptionNumber) == 0) {
                        errMsg = ErrorMessages.InvalidSubscriptionNumberError();
                    }
                }

                processResult.setMessage(errMsg);
                return processResult;
            }
        }

        if (credentialType == null) {
            processResult.setSuccess(false);
            ErrorMessage errMsg = ErrorMessages.AuthenticationFailedError();
            processResult.setMessage(errMsg);
        } else {
            processResult.setResult(credentialType);
            processResult.setSuccess(true);
        }
        return processResult;
    }

    /**
     * Determines if the version of Quickbooks is about to sunset.  If the version is equal to the minimum support
     * version and the date is in March, April, or May, then return true for the warning
     *
     * @param ofxAPPVERObject Application Version Tag of the Quickbooks OFX
     * @return true if its about to sunset, false otherwise
     */
    private String getQuickBooksSunsetMessage(OFXAPPVERObject ofxAPPVERObject) {
        String sunsetMessage = null;
        SpcfCalendar calendar = PSPDate.getPSPTime().toLocal();
        SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar effectiveDate = parameter.getEffectiveDate().toLocal();
        SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
        String quickbooksVersionString = parameter.getParameterValue();

        if (ofxAPPVERObject.isMinQBVersionSupported()) {
            if (calendar.getTimeInMilliseconds() >= effectiveDate.getTimeInMilliseconds() &&
                    calendar.getTimeInMilliseconds() < expirationDate.getTimeInMilliseconds()) {

                // Set the expiration date to one day before for purposes of message
                // This assumes that the
                expirationDate.addDays(-1);
                SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
                String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));

                ErrorMessage errMsg = ErrorMessages.QBReleaseToBeSunset(quickbooksVersionString, formattedDate, formattedDate);
                sunsetMessage = errMsg.getErrorDescription();
            }
        }
        return sunsetMessage;
    }

    private ErrorMessage getQuickBooksDiscoMessage(String ofxAPPVERObject) {
        SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
        String quickbooksVersionString = parameter.getParameterValue();

        SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
        String formattedDiscoDate = formatter.format(CalendarUtils.convertToDate(expirationDate));

        // Set the expiration date to one day before for purposes of message
        expirationDate.addDays(-1);

        String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));

        return ErrorMessages.QBVersionSunsetted(ofxAPPVERObject, quickbooksVersionString, formattedDate, formattedDiscoDate);
    }
}
