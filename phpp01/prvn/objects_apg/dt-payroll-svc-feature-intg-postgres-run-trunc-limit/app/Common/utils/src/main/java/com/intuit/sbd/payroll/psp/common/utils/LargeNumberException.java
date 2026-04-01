package com.intuit.sbd.payroll.psp.common.utils;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 11/22/11
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class LargeNumberException extends RuntimeException {
    public LargeNumberException(String message) {
        super(message);
    }
}
