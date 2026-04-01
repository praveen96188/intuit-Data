package com.intuit.sbd.payroll.psp.mapper.guideline401k.company;

public enum EcosystemOwnershipTypeEnum {
    SOLE_PROPRIETOR("Sole proprietor (Form 1040)"),
    PARTNERSHIP_OR_LTD_LIABILITY("Partnership or limited liability company (Form 1065)"),
    SMALL_BUSINESS_CORP("Small business corporation, two or more owners (Form 1120S)"),
    CORPORATION("Corporation, one or more shareholders (Form 1120)"),
    NONPROFIT("Nonprofit organization (Form 990)"),
    NOT_SURE("Not sure"),
    LTD_LIABILITY("Limited liability");

    private String description;

    EcosystemOwnershipTypeEnum(String description){
        this.description = description;
    }
}
