package com.intuit.sbd.payroll.psp.agency.util;

import com.paycycle.eftpsBp.EftpsBpRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 23, 2011
 * Time: 7:15:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsFileBoundaryException extends EftpsBpRuntimeException {
    public EftpsFileBoundaryException(String message) {
        super(message);
    }

    public EftpsFileBoundaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
