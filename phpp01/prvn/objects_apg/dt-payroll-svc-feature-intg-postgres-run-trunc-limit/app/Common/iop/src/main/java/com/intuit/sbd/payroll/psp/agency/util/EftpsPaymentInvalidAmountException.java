package com.intuit.sbd.payroll.psp.agency.util;

import com.paycycle.eftpsBp.EftpsBpRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Feb 11, 2011
 * Time: 2:02:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsPaymentInvalidAmountException extends EftpsBpRuntimeException {
    public EftpsPaymentInvalidAmountException(String message) {
        super(message);
    }

    public EftpsPaymentInvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
