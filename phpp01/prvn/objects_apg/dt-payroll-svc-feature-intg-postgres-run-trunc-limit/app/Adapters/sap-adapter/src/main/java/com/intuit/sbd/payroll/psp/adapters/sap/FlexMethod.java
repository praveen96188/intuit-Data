package com.intuit.sbd.payroll.psp.adapters.sap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: dweinberg
 * Date: 10/20/11
 * Time: 2:05 PM
 * This (at least at present) has no purpose but to configure the entry point manager in IDEA so it does not flag them as unused
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface FlexMethod {
}
