package com.intuit.sbd.payroll.psp.gateways.accountservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskLimits {
    private String ownerLimit;
    private String payeeLimit;
}
