package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.ServiceCode;

public class CloudV3ServiceInfoDTO extends ServiceInfoDTO{
    public CloudV3ServiceInfoDTO() {
        super.setServiceCode(ServiceCode.CloudV3);
    }
}
