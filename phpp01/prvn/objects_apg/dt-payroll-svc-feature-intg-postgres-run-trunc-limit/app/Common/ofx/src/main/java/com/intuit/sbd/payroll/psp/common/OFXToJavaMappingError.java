package com.intuit.sbd.payroll.psp.common;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jan 25, 2008
 * Time: 10:14:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class OFXToJavaMappingError extends Exception {
    public OFXToJavaMappingError(String errorStr, Exception e) {
        super(errorStr, e);
    }
}
