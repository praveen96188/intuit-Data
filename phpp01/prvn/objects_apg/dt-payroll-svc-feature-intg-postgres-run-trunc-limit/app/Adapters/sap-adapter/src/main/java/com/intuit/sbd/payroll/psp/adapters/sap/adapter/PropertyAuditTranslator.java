/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/PropertyAuditTranslator.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPropertyAudit;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPServicePropertyAudit;
import com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.PropertyAudit;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.StringUtils;

/**
 * PropertyAuditTranslator - SAP tranlator for retrieving SAP DTOs from PSP core domain entities.
 *
 * @author Joe Warmelink
 */
public class PropertyAuditTranslator {

    public static SAPPropertyAudit getSAPPropertyAuditBasicsFromDomainEntity(PropertyAudit pPropertyAudit) {
        SAPPropertyAudit sapPropertyAudit = new SAPPropertyAudit();
        sapPropertyAudit.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(pPropertyAudit.getCreatedDate()));
        sapPropertyAudit.setAuditDate(SAPTranslator.getDateFromSpcfCalendar(pPropertyAudit.getAuditDate()));
        sapPropertyAudit.setUserId(SAPTranslator.getUserNameFromUserID(pPropertyAudit.getUserId()));
        return sapPropertyAudit;
    }

    public static SAPPropertyAudit getSAPPropertyAuditFromDomainEntity(PropertyAudit pPropertyAudit) {
        SAPPropertyAudit sapPropertyAudit = getSAPPropertyAuditBasicsFromDomainEntity(pPropertyAudit);
        //get rid of the time portion for these dates
        //todo use relfection instead
        if (pPropertyAudit.getPropertyName() != null && pPropertyAudit.getPropertyName().equals(Employee.TerminationDate().getPropertyName())) {
            sapPropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue() != null ? pPropertyAudit.getNewPropertyValue().substring(0, 9) : pPropertyAudit.getNewPropertyValue());
            sapPropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue() != null ? pPropertyAudit.getOldPropertyValue().substring(0, 9) : pPropertyAudit.getOldPropertyValue());
            sapPropertyAudit.setPropertyName("Termination Date");
        } else if (pPropertyAudit.getPropertyName() != null && pPropertyAudit.getPropertyName().equals(Employee.HireDate().getPropertyName())) {
            sapPropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue() != null ? pPropertyAudit.getNewPropertyValue().substring(0, 9) : pPropertyAudit.getNewPropertyValue());
            sapPropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue() != null ? pPropertyAudit.getOldPropertyValue().substring(0, 9) : pPropertyAudit.getOldPropertyValue());
            sapPropertyAudit.setPropertyName("Hire Date");
        } else if (pPropertyAudit.getPropertyName() != null && pPropertyAudit.getPropertyName().equals(Employee.BirthDateEnc().getPropertyName())) {
            SpcfCalendar newValue = EncryptionUtils.probabilisticDecryptDate(Employee.BirthDateKeyName, pPropertyAudit.getNewPropertyValue());
            SpcfCalendar oldValue = EncryptionUtils.probabilisticDecryptDate(Employee.BirthDateKeyName, pPropertyAudit.getOldPropertyValue());
            sapPropertyAudit.setNewPropertyValue(String.valueOf(newValue));
            sapPropertyAudit.setOldPropertyValue(String.valueOf(oldValue));
            sapPropertyAudit.setPropertyName("Birth Date");
        } else if (pPropertyAudit.getPropertyName() != null && pPropertyAudit.getPropertyName().equals(Employee.TaxIdEnc().getPropertyName())) {
            sapPropertyAudit.setNewPropertyValue(EncryptionUtils.deterministicDecrypt(Employee.TaxIdKeyName, pPropertyAudit.getNewPropertyValue()));
            sapPropertyAudit.setOldPropertyValue(EncryptionUtils.deterministicDecrypt(Employee.TaxIdKeyName, pPropertyAudit.getOldPropertyValue()));
            sapPropertyAudit.setPropertyName("SSN");
        } else if (pPropertyAudit.getPropertyName() != null && pPropertyAudit.getPropertyName().equals(Employee.SourceEmployeeId().getPropertyName())) {
            sapPropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue());
            sapPropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue());
            sapPropertyAudit.setPropertyName("Source Employee Id");
        } else if (pPropertyAudit.getPropertyName() != null && pPropertyAudit.getPropertyName().equals(CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().getPropertyName())) {
            sapPropertyAudit.setNewPropertyValue(EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName, pPropertyAudit.getNewPropertyValue()));
            sapPropertyAudit.setOldPropertyValue(EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName, pPropertyAudit.getOldPropertyValue()));
            sapPropertyAudit.setPropertyName("AgencyTaxPayerId");
        } else {
            sapPropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue());
            sapPropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue());
            sapPropertyAudit.setPropertyName(pPropertyAudit.getPropertyName());
        }
        return sapPropertyAudit;
    }

    public static SAPPropertyAudit convertBooleanPropertyAudit(PropertyAudit propertyAudit, String trueString, String falseString) {
        SAPPropertyAudit sapPropertyAudit = getSAPPropertyAuditBasicsFromDomainEntity(propertyAudit);
        sapPropertyAudit.setNewPropertyValue(convertBoolean(propertyAudit.getNewPropertyValue(), trueString, falseString));
        sapPropertyAudit.setOldPropertyValue(convertBoolean(propertyAudit.getOldPropertyValue(), trueString, falseString));
        sapPropertyAudit.setPropertyName(propertyAudit.getPropertyName());
        return sapPropertyAudit;
    }

    private static String convertBoolean(String value, String trueString, String falseString) {
        switch(StringUtils.trimToEmpty(value)) {
            case "1":
                return trueString;
            case "0":
                return falseString;
            default:
                return falseString;
        }
    }

    public static SAPServicePropertyAudit getSAPServicePropertyAuditFromDomainEntity(PropertyAudit pPropertyAudit, String pServiceCode) {
        SAPServicePropertyAudit sapServicePropertyAudit = new SAPServicePropertyAudit();
        sapServicePropertyAudit.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(pPropertyAudit.getCreatedDate()));
        sapServicePropertyAudit.setAuditDate(SAPTranslator.getDateFromSpcfCalendar(pPropertyAudit.getAuditDate()));
        sapServicePropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue());
        sapServicePropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue());
        sapServicePropertyAudit.setPropertyName(pPropertyAudit.getPropertyName());
        sapServicePropertyAudit.setUserId(SAPTranslator.getUserNameFromUserID(pPropertyAudit.getUserId()));
        sapServicePropertyAudit.setServiceCd(pServiceCode);
        return sapServicePropertyAudit;
    }

    public static SAPPropertyAudit getSAPPropertyAuditFromTaxPaymentHistory(PropertyAudit pPropertyAudit) {
        SAPPropertyAudit sapPropertyAudit = getSAPPropertyAuditBasicsFromDomainEntity(pPropertyAudit);

        if (pPropertyAudit.getPropertyName().equals(MoneyMovementTransaction.MoneyMovementPaymentMethod().getPropertyName())) {
            sapPropertyAudit.setNewPropertyValue(getPaymentMethod(pPropertyAudit.getNewPropertyValue()));
            sapPropertyAudit.setOldPropertyValue(getPaymentMethod(pPropertyAudit.getOldPropertyValue()));
        } else if (pPropertyAudit.getPropertyName().equals(MoneyMovementTransaction.InitiationDate().getPropertyName())) {
            // cut off the time
            sapPropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue() != null ? pPropertyAudit.getNewPropertyValue().substring(0, 9) : pPropertyAudit.getNewPropertyValue());
            sapPropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue() != null ? pPropertyAudit.getOldPropertyValue().substring(0, 9) : pPropertyAudit.getOldPropertyValue());
        } else {
            sapPropertyAudit.setNewPropertyValue(pPropertyAudit.getNewPropertyValue());
            sapPropertyAudit.setOldPropertyValue(pPropertyAudit.getOldPropertyValue());
        }

        if (pPropertyAudit.getPropertyName().equals("ManualPaymentStatus")) {
            sapPropertyAudit.setPropertyName("Manual Payment Status");
        } else if (pPropertyAudit.getPropertyName().equals("MoneyMovementPaymentMethod")) {
            sapPropertyAudit.setPropertyName("Payment Method");
        } else if (pPropertyAudit.getPropertyName().equals("ReferenceNumber")) {
            sapPropertyAudit.setPropertyName("Reference Number");
        } else if (pPropertyAudit.getPropertyName().equals("InitiationDate")) {
            sapPropertyAudit.setPropertyName("Initiation Date");
        }

        return sapPropertyAudit;
    }

    private static String getPaymentMethod(String paymentMethod) {
        if (paymentMethod != null) {
            if (PaymentMethod.valueOf(paymentMethod) == PaymentMethod.EFE) {
                return "ACH";
            } else if (PaymentMethod.valueOf(paymentMethod) == PaymentMethod.CheckPayment) {
                return "Check";
            } else if (PaymentMethod.valueOf(paymentMethod) == PaymentMethod.WirePayment) {
                return "Wire";
            } else if (PaymentMethod.valueOf(paymentMethod) == PaymentMethod.EDI) {
                return "EDI";
            }
        }
        return paymentMethod;
    }
}
