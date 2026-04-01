package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll;

/**
 * User: rnorian
 * Date: Feb 1, 2010
 * Time: 11:36:47 PM
 */
public enum QBDTTaxTrackingType {
    K401("11"),
    ROTH401K("57");

    /**
     * This 'raw value' is defined as an integer constant
     * in QBDT C/C++ header files
     */
    private String qbdtRawValue;
    QBDTTaxTrackingType(String pRawValue) {
        qbdtRawValue = pRawValue;
    }

    public String getRawValue() {
        return qbdtRawValue;
    }
}
