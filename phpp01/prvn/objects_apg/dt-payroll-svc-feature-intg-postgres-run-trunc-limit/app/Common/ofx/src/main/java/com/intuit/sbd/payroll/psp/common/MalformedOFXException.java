package com.intuit.sbd.payroll.psp.common;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jan 25, 2008
 * Time: 10:24:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class MalformedOFXException extends Exception {
    public MalformedOFXException(String errorStr) {
        super(errorStr);
    }
}
