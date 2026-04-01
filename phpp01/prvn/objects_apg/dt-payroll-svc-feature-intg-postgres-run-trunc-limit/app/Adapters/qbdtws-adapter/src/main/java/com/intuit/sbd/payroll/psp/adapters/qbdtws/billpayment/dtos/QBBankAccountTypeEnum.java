package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "QBBankAccountTypeEnum")
@XmlEnum
public enum QBBankAccountTypeEnum {

    @XmlEnumValue("Checking")
    CHECKING("Checking"),
    @XmlEnumValue("Savings")
    SAVINGS("Savings");
    private final String value;

    private QBBankAccountTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QBBankAccountTypeEnum fromValue(String v) {
        for (QBBankAccountTypeEnum c: QBBankAccountTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
