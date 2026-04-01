package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.ServiceCode;

/**
 * Author: Sriram Nutakki
 * Date created: 1/25/13
 */
public class WorkersCompServiceInfoDTO extends ServiceInfoDTO {

    public WorkersCompServiceInfoDTO() {
        super.setServiceCode(ServiceCode.WorkersComp);
    }
}
