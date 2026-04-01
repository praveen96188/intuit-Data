package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/DISMessages.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * DIS Message handler that centralizes the loading and exposing of the DIS messages
 *     which are from a properties file.
 *
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.text.MessageFormat;

/**
    @author Jeff Jones
 */
public class DISMessages {

    private static SpcfLogger logger = PayrollServices.getLogger(DISMessages.class);

    /***
     * @return
     */
    public static DISMessage systemError(String pErrorMsg) {
        return createDISMessage(30001,pErrorMsg);
    }

    /***
     * @return
     */
    public static DISMessage companyDoesNotExist(String sourceCompanyId) {
        return createDISMessage(30002,sourceCompanyId);
    }

    /***
     * @return
     */
    public static DISMessage einOrSourceCompanyIdRequiredForCoSearch() {
        return createDISMessage(30003);
    }

    /***
     * Create a DIS error message with the PSP error message specified
     * @param pPSPMessage
     * @return
     */
    public static DISMessage pspErrorMessage(String pPSPMessage) {
        return createDISMessage(30004,pPSPMessage);
    }

    /***
     * Create a DIS warning message with the PSP error message specified
     * @param pPSPMessage
     * @return
     */
    public static DISMessage pspWarningMessage(String pPSPMessage) {
        return createDISMessage(30004,pPSPMessage);
    }

    /***
     * Create a DIS message with the specified definition and the applicable parameter for the
     *     specified message definition.  The messages contain {0} and {1} to replace these
     *     arguments in the message.
     *
     * @param pCode
     * @param pArgs
     * @return
     */
    public static DISMessage createDISMessage(int pCode, Object... pArgs) {
        DISMessageDefinition definition = DISMessageDefinition.getMessageDefinition(pCode);
        return createDISMessage(definition, pArgs);
    }

    /**
     *
     * @param pIDName
     * @param pIDValue
     * @return
     */
    public static DISMessage objectNotFound(String pIDName, String pIDValue) {
        return createDISMessage(30006,pIDName,pIDValue);
    }

    public static DISMessage payrollNotFound(String pSourcePayrollId, String pPSID) {
        return createDISMessage(30007,pSourcePayrollId,pPSID);
    }

    public static DISMessage requiredFieldNotPassed(String pFieldName) {
        return createDISMessage(30008,pFieldName);
    }

    public static DISMessage invalidUsernameOrPassword() {
        return createDISMessage(30009);
    }

    public static DISMessage userDoesNotHavePermissions() {
        return createDISMessage(30010);
    }

    public static DISMessage financialTransactionIdNotFound(String pFnTxId) {
        return createDISMessage(30011,pFnTxId);
    }

    public static DISMessage existingAIDMatchesUpdateAID(String pPaymentTemplateCd,String pAID) {
        return createDISMessage(30012,pPaymentTemplateCd,pAID);
    }

    public static DISMessage couldNotFindDefaultPaymentTemplateForCompany(String pPaymentTemplateCd,String pSourceCompanyId) {
        return createDISMessage(30013,pPaymentTemplateCd,pSourceCompanyId);
    }

    /***
     * Create a DIS message with the specified definition and the applicable parameter for the
     *     specified message definition.  The messages contain {0} and {1} to replace these
     *     arguments in the message.
     *
     * @param messageDefinition
     * @param args
     * @return
     */
    private static DISMessage createDISMessage(DISMessageDefinition messageDefinition, Object... args) {
        DISMessage message = new DISMessage();
        message.setCode(messageDefinition.getNumber());

        MessageFormat format = new MessageFormat(messageDefinition.getMessageFormat());
        message.setMessage(format.format(args));

        return message;
    }

}
