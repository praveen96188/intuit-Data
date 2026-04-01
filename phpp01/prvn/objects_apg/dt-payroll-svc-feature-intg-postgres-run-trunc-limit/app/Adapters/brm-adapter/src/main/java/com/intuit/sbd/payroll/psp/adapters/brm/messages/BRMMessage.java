package com.intuit.sbd.payroll.psp.adapters.brm.messages;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.text.MessageFormat;

/**
 * Response messages for BRM
 */

public class BRMMessage {
    private static SpcfLogger logger = PayrollServices.getLogger(BRMMessage.class);
    private String code;
    private String message;

    /**
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * @param pCode
     */
    public void setCode(String pCode) {
        this.code = pCode;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param pMessage
     */
    public void setMessage(String pMessage) {
        this.message = pMessage;
    }

    /**
     * @return
     */
    public static BRMMessage companyNotFound(String ein) {
        return createBRMMessage(30002,ein);
    }

    /**
     * @return
     */
    public static BRMMessage missingInputs(String ein) {
        return createBRMMessage(30001,ein);
    }

    /**
     * @return
     */
    public static BRMMessage entitlementNotFound(String ein) {
        return createBRMMessage(30003,ein);
    }

    /**
     * @return
     */
    public static BRMMessage companyUsageNotFound(String ein) {
        return createBRMMessage(30004,ein);
    }

    /**
     * @return
     */
    public static BRMMessage billNotFound(String ein) {
        return createBRMMessage(30005,ein);
    }
    /**
     * @return
     */
    public static BRMMessage billDetailsNotFound(String ein) {
        return createBRMMessage(30006,ein);
    }

    /**
     * Create a BRM message with the specified definition and the applicable parameter for the
     * specified message definition.  The messages contain {0} and {1} to replace these
     * arguments in the message.
     *
     * @param pCode
     * @param pArgs
     * @return
     */
    public static BRMMessage createBRMMessage(int pCode, Object... pArgs) {
        BRMMessageDefinition definition = BRMMessageDefinition.getMessageDefinition(pCode);
        return createBRMMessage(definition, pArgs);
    }

    /**
     * Create a BRM message with the specified definition and the applicable parameter for the
     * specified message definition.  The messages contain {0} and {1} to replace these
     * arguments in the message.
     *
     * @param messageDefinition
     * @param args
     * @return
     */
    private static BRMMessage createBRMMessage(BRMMessageDefinition messageDefinition, Object... args) {
        BRMMessage message = new BRMMessage();
        message.setCode(String.valueOf(messageDefinition.getNumber()));
        MessageFormat format = new MessageFormat(messageDefinition.getMessageFormat());
        message.setMessage(format.format(args));
        return message;
    }


}
