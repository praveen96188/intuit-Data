package com.intuit.ems.payroll.psp.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Address {

    @CsvBindByName(column = "SOURCE_COMPANY_ID")
    private String sourceCompanyId;

    @CsvBindByName(column = "I_A_M_REALM_ID")
    private String realmId;

    @CsvBindByName(column = "ADDRESS_LINE1")
    private String addressLine1;

    @CsvBindByName(column = "ADDRESS_LINE2")
    private String addressLine2;

    @CsvBindByName(column = "ADDRESS_LINE3")
    private String addressLine3;

    @CsvBindByName(column = "CITY")
    private String city;

    @CsvBindByName(column = "STATE")
    private String state;

    @CsvBindByName(column = "COUNTRY")
    private String country;

    @CsvBindByName(column = "ZIP_CODE")
    private String zipCode;

    @CsvBindByName(column = "ZIP_CODE_EXTENSION")
    private String zipCodeExtension;

}
