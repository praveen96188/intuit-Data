package com.intuit.sbd.payroll.psp.domain;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Mar 25, 2009
 * Time: 1:13:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class IllegalStateTransition extends DomainException {
    IllegalStateTransition(Enum oldval, Enum newval) {
        super("transition from " + oldval.name() + " to " + newval.name() + " is not allowed.");
    }
}
