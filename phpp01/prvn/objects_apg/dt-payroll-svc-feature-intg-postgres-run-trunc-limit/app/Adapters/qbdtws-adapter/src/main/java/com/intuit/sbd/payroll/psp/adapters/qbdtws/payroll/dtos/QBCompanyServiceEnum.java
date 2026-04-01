package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos;

import com.intuit.sbd.payroll.psp.domain.ServiceCode;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "QBCompanyServiceEnum")
@XmlEnum(String.class)
public enum QBCompanyServiceEnum {
    Assisted,
    BillPayment,
    CheckDistribution,
    Cloud,
    DirectDeposit,
    Tax,
    ThirdParty401k;

    QBCompanyServiceEnum() {
    }

    public static QBCompanyServiceEnum fromValue(String v) {
        for (QBCompanyServiceEnum c: QBCompanyServiceEnum.values()) {
            if (c.name().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static QBCompanyServiceEnum fromServiceCode(ServiceCode pServiceCode) {
        String v = (pServiceCode != null ? pServiceCode.name() : "null");
        if (pServiceCode == ServiceCode.Tax) {
            v = "Assisted";
        }
        return fromValue(v);
    }
}