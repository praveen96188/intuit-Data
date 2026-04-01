package com.intuit.sbd.payroll.psp.adapters.qbdt;

import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Aug 5, 2008
 * Time: 12:08:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class QBDTProcessResultTest {

    @Test
    public void validateIsSuccess() {
        QBDTProcessResult processResult = new QBDTProcessResult();
        // True by default
        assertTrue(processResult.isSuccess());

        // False once error message set
        processResult.setMessage(ErrorMessages.AuthenticationFailedError());
        assertFalse(processResult.isSuccess());

        // False when isSuccess set manually.
        processResult = new QBDTProcessResult();
        processResult.setSuccess(false);
        assertFalse(processResult.isSuccess());

    }

    @Test
    public void validateToString() {
        QBDTProcessResult processResult = new QBDTProcessResult();
        // True by default
        assertTrue(processResult.isSuccess());

        ErrorMessage errMsg = ErrorMessages.AuthenticationFailedError();
        // False once error message set
        processResult.setMessage(errMsg);

        String toStringResult = processResult.toString();
        assertTrue(toStringResult.contains(errMsg.getErrorDescription()));
        assertTrue(toStringResult.contains("false"));
    }

}
