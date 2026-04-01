package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.inputFileModel;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LegacyToSymphonyInputFileModel {

    @CsvBindByName(column = "LICENSE_NUMBER", required = true)
    private String licenseNumber;

    @CsvBindByName(column = "ENTITLEMENT_OFFERING_CODE", required = true)
    private String entitlementOfferingCode;

    @CsvBindByName(column = "EMAIL_ADDR", required = true)
    private String email;

    @CsvBindByName(column = "EDITION", required = true)
    private String edition;

    @CsvBindByName(column = "BILLING_FREQUENCY", required = true)
    private String billingFrequencyType;

    @CsvBindByName(column = "DAYS_TILL_RENEWAL")
    private String daysTillRenewal;

    @CsvBindByName(column = "NEXT_CHARGE_DATE", required = true)
    private String nextChargeDate;

    @CsvBindByName(column = "BASE_PRICE", required = true)
    private String baseRate;
}
