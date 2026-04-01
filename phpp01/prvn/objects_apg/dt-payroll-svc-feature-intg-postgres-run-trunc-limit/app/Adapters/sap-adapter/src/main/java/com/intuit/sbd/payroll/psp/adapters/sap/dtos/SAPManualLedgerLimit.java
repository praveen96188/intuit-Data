package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * User: rnorian
 * Date: Apr 28, 2010
 * Time: 4:42:26 PM
 */
@Getter
@Setter
public class SAPManualLedgerLimit {
    private boolean limitEnabled = true;
    private int warningLimit = 10000;
    private int blockLimit = 100000;
}
