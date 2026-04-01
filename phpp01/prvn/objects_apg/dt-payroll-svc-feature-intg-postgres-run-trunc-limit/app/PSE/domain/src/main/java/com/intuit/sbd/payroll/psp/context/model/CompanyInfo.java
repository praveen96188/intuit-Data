package com.intuit.sbd.payroll.psp.context.model;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CompanyInfo {
    private String psid;
    private String realmId;
    private SpcfUniqueId companySequence;

}
