package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.mail.Message;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 8, 2011
 * Time: 8:19:11 PM
 */
public class MailSenderHolder {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(MailSenderHolder.class);
    private static Message mMessage;
    private static Boolean failMailSenderHolderFlag = Boolean.FALSE;
    public static Boolean getFailMailSenderHolderFlag() {
        return failMailSenderHolderFlag;
    }

    public static void setFailMailSenderHolderFlag(Boolean pFailMailSenderHolderFlag) {
        failMailSenderHolderFlag = pFailMailSenderHolderFlag;
    }


    private final static String FAIL_MAIL_SENDER_HOLDER_EXCEPTION_MESSAGE = "MailSenderHolder RuntimeException Generated";

    public static void setMessage(Message pMessage)
    {
        if(failMailSenderHolderFlag.equals(Boolean.TRUE))
        {
            throw new RuntimeException(FAIL_MAIL_SENDER_HOLDER_EXCEPTION_MESSAGE);
        }

        mMessage = pMessage;
    }

    public static Message getMessage() {
        return mMessage;
    }
}
