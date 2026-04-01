package com.intuit.sbd.payroll.psp.batchjobs.amo;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 9, 2010
 * Time: 8:33:13 AM
 */
public class DatabaseFailureException extends RuntimeException {

    public DatabaseFailureException(String errorMessage) {
        super(errorMessage);
    }

}
