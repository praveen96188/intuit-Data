package com.intuit.sbd.payroll.psp.agency.util;

import com.paycycle.eftpsBp.EftpsBpRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 23, 2011
 * Time: 9:13:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsPaymentBusinessException extends EftpsBpRuntimeException {
    public EftpsPaymentBusinessException(String message) {
        super(message);
    }

    public EftpsPaymentBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
