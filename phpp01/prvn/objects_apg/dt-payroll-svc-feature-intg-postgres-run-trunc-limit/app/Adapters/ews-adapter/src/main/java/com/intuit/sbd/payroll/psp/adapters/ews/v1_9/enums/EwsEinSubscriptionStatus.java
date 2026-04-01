package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums;

/**
 * These values must contain all the values in PSP domain enum: EntitlementUnitStatusCode
 * plus the 'EinNotSubscribed'
 */
public enum EwsEinSubscriptionStatus {
    EinNotSubscribed,
    Activated,
    Deactivated
}
