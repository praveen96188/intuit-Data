package com.intuit.ems.payroll.psp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AddressReportModel{

    private String cRealm_Id;

    private String cSource_Company_Id;

    private boolean info_address_processed;

    private boolean owner_address_processed;

    private String status;

    private String message;

}
