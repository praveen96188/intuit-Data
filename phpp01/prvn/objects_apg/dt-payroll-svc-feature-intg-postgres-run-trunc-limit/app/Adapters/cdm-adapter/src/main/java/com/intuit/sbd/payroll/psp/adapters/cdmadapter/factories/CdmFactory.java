package com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployerPreference;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.schema.ems.v3.*;
import com.intuit.schema.ems.v3.Gender;
import com.intuit.schema.ems.v3.Paystub;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

//Class for creating CDM objects from PSP domain objects
public class CdmFactory {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(CdmFactory.class);
    }

    public static PayrollCompany createPayrollCompany(Company company, DomainEntitySet<Employee> employees) {
        PayrollCompany payrollCompany = null;
        if(company != null) {
            payrollCompany = new PayrollCompany();
            payrollCompany.setId(company.getSourceCompanyId());
            payrollCompany.setEIN(company.getFedTaxId());
            payrollCompany.setCompanyLegalName(company.getLegalName());
            payrollCompany.setCompanyName(company.getDbaName());
            payrollCompany.setPhone(createTelephoneNumber(company.getPhone()));
            payrollCompany.setEmail(createEmailAddress(company.getNotificationEmail()));
            payrollCompany.setAddress(createPhysicalAddress(company.getLegalAddress()));
            payrollCompany.setShipAddr(createPhysicalAddress(company.getMailingAddress()));
            for(Contact contact : company.getContactCollection()) {
                payrollCompany.getContact().add(createPayrollContact(contact));
            }
            for(Employee employee : employees) {
                payrollCompany.getEmployee().add(createPayrollEmployee(employee));
            }

            payrollCompany.setPayrollSubscription(new PayrollSubscription());

            // Entitlements
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                com.intuit.schema.ems.v3.Entitlement entitlement = createEntitlement(entitlementUnit);
                if (entitlement != null) {
                    payrollCompany.getPayrollSubscription().getEntitlement().add(entitlement);
                }
            }
            // Services
            for (CompanyService companyService : company.getCompanyServiceCollection()) {
                AddOnService service = createAddOnService(companyService);
                if (service != null) {
                    payrollCompany.getPayrollSubscription().getAddOnService().add(service);
                }
            }
        }
        return payrollCompany;
    }

    public static PhysicalAddress createPhysicalAddress(Address address) {
        PhysicalAddress physicalAddress = null;
        if(address != null) {
            physicalAddress = new PhysicalAddress();
            physicalAddress.setLine1(address.getAddressLine1());
            physicalAddress.setLine2(address.getAddressLine2());
            physicalAddress.setLine3(address.getAddressLine3());
            physicalAddress.setCity(address.getCity());
            physicalAddress.setPostalCode(address.getZipCode());
            physicalAddress.setPostalCodeSuffix(address.getZipCodeExtension());
            physicalAddress.setCountrySubDivisionCode(address.getState());
            physicalAddress.setCountry(address.getCountry());
        }
        return physicalAddress;
    }

    public static TelephoneNumber createTelephoneNumber(String phoneNumber) {
        TelephoneNumber telephoneNumber = null;
        if(phoneNumber != null) {
            telephoneNumber = new TelephoneNumber();
            telephoneNumber.setFreeFormNumber(phoneNumber);
        }
        return telephoneNumber;
    }

    public static EmailAddress createEmailAddress(String emailAddressString) {
        EmailAddress emailAddress = null;
        if(emailAddressString != null) {
            emailAddress = new EmailAddress();
            emailAddress.setAddress(emailAddressString);
        }
        return emailAddress;
    }

    public static StateTaxFilingInformation createWorkStateTaxFilingInformation(Employee employee) {
        StateTaxFilingInformation stateTaxFilingInformation = null;
        if(employee != null) {
            stateTaxFilingInformation = new StateTaxFilingInformation();
            stateTaxFilingInformation.setState(employee.getWorkState());
            //TODO where is other state tax filing information stored?
        }
        return stateTaxFilingInformation;
    }

    public static PayrollContact createPayrollContact(Contact contact) {
        PayrollContact payrollContact = null;
        if(contact != null) {
            payrollContact = new PayrollContact();
            payrollContact.setId(contact.getId().toString());
            if(contact.getContactRoleCd() != null) {
                payrollContact.setContactType(contact.getContactRoleCd().toString());
            }
            payrollContact.setGivenName(contact.getFirstName());
            payrollContact.setMiddleName(contact.getMiddleName());
            payrollContact.setFamilyName(contact.getLastName());
            payrollContact.setFullName(contact.getFirstMiddleLastName());
            payrollContact.setPrimaryPhone(createTelephoneNumber(contact.getPhone()));
            payrollContact.setPrimaryEmailAddress(createEmailAddress(contact.getEmail()));
        }
        return payrollContact;
    }

    public static PayrollEmployee createPayrollEmployee(Employee employee) {
        PayrollEmployee payrollEmployee = null;
        if (employee != null) {
            try {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(employee.getCompany());
                payrollEmployee = new PayrollEmployee();
                payrollEmployee.setId(employee.getId().toString());
                //Name fields
                payrollEmployee.setGivenName(employee.getFirstName());
                payrollEmployee.setMiddleName(employee.getMiddleName());
                payrollEmployee.setFamilyName(employee.getLastName());
                payrollEmployee.setFullName(employee.getFirstMiddleLastName());
                //TODO hardcoding this, should refactor and remove in the future if there is not another type than this one
                payrollEmployee.setEmployeeType("Employee");
                //We don't have any employee / badge number available so it is being ignored
                if (employee.getCompany() != null) {
                    payrollEmployee.setCompanyName(employee.getCompany().getDbaName());
                    payrollEmployee.setCompanyTaxId(employee.getCompany().getFedTaxId());
                }
                payrollEmployee.setSSN(CdmHelper.formatAndMask(employee.getTaxId()));
                payrollEmployee.setUnmaskedSSN(employee.getTaxId());
                payrollEmployee.setGender(createGender(employee.getGenderCd()));
                payrollEmployee.setPrimaryAddress(createPhysicalAddress(employee.getMailingAddress()));
                //Date fields
                payrollEmployee.setBirthDate(createXmlGregorianCalendar(employee.getBirthDate()));
                payrollEmployee.setHiredDate(createXmlGregorianCalendar(employee.getHireDate()));
                payrollEmployee.setReleasedDate(createXmlGregorianCalendar(employee.getTerminationDate()));

                payrollEmployee.setActive(isActive(employee));
                payrollEmployee.setWorkStateTaxFilingInfo(createWorkStateTaxFilingInformation(employee));
                payrollEmployee.setPayPeriodHistory(createPayPeriodHistory(employee));
                payrollEmployee.setViewingPaystubDisabled(employee.getIsViewingPaystubDisabled());

            } finally {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            }
        }
        return payrollEmployee;
    }

    public static PayrollEmployee createPayrollEmployee(VmpEmployeeInfo vmpEmployeeInfo){
        PayrollEmployee payrollEmployee = null;
        if(vmpEmployeeInfo != null){
            payrollEmployee = new PayrollEmployee();
            payrollEmployee.setId(vmpEmployeeInfo.getId().toString());
            //For employees in VmpEmployeeInfo, Name fields are not stored
            payrollEmployee.setEmployeeType("Employee");
            //We don't have any employee / badge number available so it is being ignored
            if(vmpEmployeeInfo.getCompany() != null) {
                payrollEmployee.setCompanyName(vmpEmployeeInfo.getCompany().getDbaName());
                payrollEmployee.setCompanyTaxId(vmpEmployeeInfo.getCompany().getFedTaxId());
            }
        }
        return payrollEmployee;
    }

    private static boolean isActive(Employee employee) {
        return EmployeeStatus.Active.equals(employee.getStatusCd());
    }

    public static Gender createGender(com.intuit.sbd.payroll.psp.domain.Gender gender) {
        Gender cdmGender = null;
        if(gender != null) {
            cdmGender = Gender.fromValue(gender.toString());
        }
        return cdmGender;
    }

    public static PayFrequency createPayFrequency(PayrollFrequencyCode payrollFrequencyCode) {
        PayFrequency payFrequency = PayFrequency.ANNUALLY;
        if(payrollFrequencyCode == PayrollFrequencyCode.Annually) {
            payFrequency = PayFrequency.ANNUALLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.SemiAnnually) {
            payFrequency = PayFrequency.SEMI_ANNUALLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.Quarterly) {
            payFrequency = PayFrequency.QUARTERLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.Monthly) {
            payFrequency = PayFrequency.MONTHLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.SemiMonthly) {
            payFrequency = PayFrequency.SEMI_MONTHLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.BiWeekly) {
            payFrequency = PayFrequency.BI_WEEKLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.Weekly) {
            payFrequency = PayFrequency.WEEKLY;
        } else if(payrollFrequencyCode == PayrollFrequencyCode.Daily) {
            payFrequency = PayFrequency.DAILY;
        }
        return payFrequency;
    }

    public static PayrollEmployee.PayPeriodHistory createPayPeriodHistory(Employee employee) {
        PayrollEmployee.PayPeriodHistory payPeriodHistory = null;
        if(employee != null) {
            payPeriodHistory = new PayrollEmployee.PayPeriodHistory();
            payPeriodHistory.setPayFrequency(createPayFrequency(employee.getPayPeriod()));
            com.intuit.sbd.payroll.psp.domain.Paystub firstPaystub = PaystubFinder.findFirstPaystub(employee);
            if(firstPaystub != null) {
                payPeriodHistory.setStartDate(createXmlGregorianCalendar(firstPaystub.getPaycheckDate()));
            }
            com.intuit.sbd.payroll.psp.domain.Paystub lastPaystub = PaystubFinder.findLastPaystub(employee);
            if(lastPaystub != null) {
                payPeriodHistory.setEndDate(createXmlGregorianCalendar(lastPaystub.getPaycheckDate()));
            }
        }
        return payPeriodHistory;
    }

    public static XMLGregorianCalendar createXmlGregorianCalendar(SpcfCalendar spcfCalendar) {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        if(spcfCalendar != null) {
            try {
                xmlGregorianCalendar = SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(spcfCalendar);
            } catch (Exception e) {
                logger.error("Error creating XMLGregorianCalendar", e);
            }
        }
        return xmlGregorianCalendar;
    }

    public static Paystub createPaystub(com.intuit.sbd.payroll.psp.domain.Paystub domainPaystub) {
        Paystub paystub = null;
        if (domainPaystub != null) {
            try {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(domainPaystub.getCompany());
                paystub = new Paystub();
                paystub.setId(domainPaystub.getId().toString());
                paystub.setEmployeeInfo(createPaystubEmployeeInfo(domainPaystub.getPstubEmployeeInfo()));
                paystub.setEmployerInfo(createPaystubEmployerInfo(domainPaystub.getPstubEmployerInfo()));
                paystub.setGrossPay(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getGrossPay()));
                paystub.setYTDGrossPay(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getYTDGrossPay()));
                paystub.setNetPay(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getNetPay()));
                paystub.setYTDNetPay(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getYTDNetPay()));
                paystub.setTaxes(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getTaxes()));
                paystub.setYTDTaxes(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getYTDTaxes()));
                paystub.setPreTaxDeductions(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getPreTaxDeductions()));
                paystub.setYTDPreTaxDeductions(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getYTDPreTaxDeductions()));
                paystub.setNetAdjustments(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getAdjNetPay()));
                paystub.setYTDNetAdjustments(CdmHelper.convertToFormattedBigDecimal(domainPaystub.getYTDAdjNetPay()));
                //Total other pay is only used in IOP so we don't need to populate those fields

                DomainEntitySet<PstubMsg> messages = domainPaystub.getPstubMsgCollection();
                if (messages != null && messages.isNotEmpty()) {
                    for (PstubMsg message : messages) {
                        //Doing mapping based on https://spaces.iopdev.intuit.com/display/PD/Answers+from+investigations
                        if (PstubMsgType.Company == message.getType()) {
                            paystub.setMessage(message.getText());
                        } else if (PstubMsgType.User == message.getType()) {
                            paystub.setMemo(message.getText());
                        }
                    }
                }
                //This needs to start as null for CdmHelper.addHoursMinutes to work properly
                String totalTime = null;
                for (PstubPayItem payItem : domainPaystub.getPstubPayItemCollection()) {
                    if (PstubItemType.Earnings == payItem.getType()) {
                        paystub.getEarningItem().add(createPaystubEarningItem(payItem));
                        //Add any time quantity amounts to total hours on the paystub
                        String quantityTime = payItem.getQtyTime();
                        if (CdmHelper.notBlank(quantityTime)) {
                            totalTime = CdmHelper.addHoursMinutes(totalTime, quantityTime);
                        }
                    } else if (PstubItemType.Tax == payItem.getType()) {
                        paystub.getTaxItem().add(createTaxPaystubLineItem(payItem));
                    } else if (PstubItemType.TaxCompContri == payItem.getType()) {
                        paystub.getTaxCompanyItem().add(createTaxPaystubLineItem(payItem));
                    } else if (PstubItemType.AdjNetPay == payItem.getType()) {
                        paystub.getNetPayAdjustmentItem().add(createPaystubLineItem(payItem));
                    } else if (PstubItemType.NonTaxCompContri == payItem.getType()) {
                        paystub.getNonTaxCompanyItem().add(createPaystubLineItem(payItem));
                    } else if (PstubItemType.PreTaxDeduct == payItem.getType()) {
                        paystub.getPreTaxItem().add(createPaystubLineItem(payItem));
                    }
                }
                for (PstubDDItem ddItem : domainPaystub.getPstubDDItemCollection()) {
                    paystub.getDDItem().add(createPaystubDdItem(ddItem));
                }
                for (PstubPaidTimeoffItem ptoItem : domainPaystub.getPstubPaidTimeoffItemCollection()) {
                    paystub.getPaidTimeOffItem().add(createPaidTimeOffItem(ptoItem));
                }
                paystub.setPayPeriod(createPaystubPayPeriod(domainPaystub));

                if (CdmHelper.notBlank(totalTime)) {
                    paystub.setTotalHours(totalTime);
                }

                //Only populate check items if there are no DD items, a check should never be split between DD / physical check
                if (domainPaystub.getPstubDDItemCollection().size() == 0) {
                    paystub.getCheckItem().add(createCheckItem(domainPaystub));
                }
            } finally {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            }
        }
        return paystub;
    }

    public static Paystub.CheckItem createCheckItem(com.intuit.sbd.payroll.psp.domain.Paystub paystub) {
        Paystub.CheckItem checkItem = null;
        if(paystub != null && paystub.getCheckNumber() != null) {
            checkItem = new Paystub.CheckItem();
            checkItem.setCheckNumber(paystub.getCheckNumber());
            checkItem.setCurrent(CdmHelper.convertToFormattedBigDecimal(paystub.getNetPay()));
        }
        return checkItem;
    }

    public static void setPaystubLineItem(PstubPayItem payItem, PaystubLineItem lineItem) {
        if(payItem != null && lineItem != null) {
            lineItem.setCurrent(CdmHelper.convertToFormattedBigDecimal(payItem.getCurAmt()));
            lineItem.setName(payItem.getName());
            lineItem.setYTD(CdmHelper.convertToFormattedBigDecimal(payItem.getYTD()));
        }
    }

    public static PaystubLineItem createPaystubLineItem(PstubPayItem payItem) {
        PaystubLineItem lineItem = null;
        if(payItem != null) {
            lineItem = new PaystubLineItem();
            setPaystubLineItem(payItem, lineItem);
        }
        return lineItem;
    }

    public static Paystub.EarningItem createPaystubEarningItem(PstubPayItem payItem) {
        Paystub.EarningItem earningItem = null;
        if(payItem != null) {
            earningItem = new Paystub.EarningItem();
            setPaystubLineItem(payItem, earningItem);
            if(CdmHelper.notBlank(payItem.getQtyAmt())) {
                earningItem.setQty(payItem.getQtyAmt());
            } else if(CdmHelper.notBlank(payItem.getQtyTime())) {
                earningItem.setQty(payItem.getQtyTime());
            }

            String rateString = payItem.getRate();
            earningItem.setRate(CdmHelper.parseDecimal(rateString));
            if(rateString != null) {
                if(rateString.contains("%")) {
                    earningItem.setRateType(RateType.PERCENTAGE);
                } else {
                    earningItem.setRateType(RateType.CURRENCY);
                }
            }
        }
        return earningItem;
    }

    public static TaxPaystubLineItem createTaxPaystubLineItem(PstubPayItem payItem) {
        TaxPaystubLineItem taxPaystubLineItem = null;
        if(payItem != null) {
            taxPaystubLineItem = new TaxPaystubLineItem();
            setPaystubLineItem(payItem, taxPaystubLineItem);
            taxPaystubLineItem.setIncomeSubjectToTax(CdmHelper.convertToFormattedBigDecimal(payItem.getIncomeSubjectToTax()));
            taxPaystubLineItem.setWageBase(CdmHelper.convertToFormattedBigDecimal(payItem.getWageBase()));
        }
        return taxPaystubLineItem;
    }

    public static Paystub.DDItem createPaystubDdItem(PstubDDItem ddItem) {
        Paystub.DDItem cdmDDItem = null;
        if(ddItem != null) {
            cdmDDItem = new Paystub.DDItem();
            cdmDDItem.setName(ddItem.getAcctType());
            cdmDDItem.setAccount(CdmHelper.formatAndMask(ddItem.getAcctNumber()));
            cdmDDItem.setCurrent(CdmHelper.convertToFormattedBigDecimal(ddItem.getCurAmt()));
        }
        return cdmDDItem;
    }

    public static Paystub.PaidTimeOffItem createPaidTimeOffItem(PstubPaidTimeoffItem paidTimeOffItem) {
        Paystub.PaidTimeOffItem cdmPtoItem = null;
        if(paidTimeOffItem != null) {
            //If both available and ytdused are zero then there is no PTO policy in place, so return nothing
            if(CdmHelper.notBlank(paidTimeOffItem.getAvailable()) || CdmHelper.notBlank(paidTimeOffItem.getYTDUsed())) {
                cdmPtoItem = new Paystub.PaidTimeOffItem();
                cdmPtoItem.setName(paidTimeOffItem.getName());
                cdmPtoItem.setAvailable(String.valueOf(paidTimeOffItem.getAvailable()));
                cdmPtoItem.setYTDUsed(String.valueOf(paidTimeOffItem.getYTDUsed()));
            }
        }
        return cdmPtoItem;
    }

    public static Paystub.PayPeriod createPaystubPayPeriod(com.intuit.sbd.payroll.psp.domain.Paystub domainPaystub) {
        Paystub.PayPeriod payPeriod = null;
        if(domainPaystub != null) {
            payPeriod = new Paystub.PayPeriod();
            payPeriod.setStartDate(createXmlGregorianCalendar(domainPaystub.getPayBeginDate()));
            payPeriod.setEndDate(createXmlGregorianCalendar(domainPaystub.getPayEndDate()));
            payPeriod.setPayDate(createXmlGregorianCalendar(domainPaystub.getPaycheckDate()));
        }
        return payPeriod;
    }

    public static Paystub.EmployeeInfo createPaystubEmployeeInfo(PstubEmployeeInfo domainEmployeeInfo) {
        Paystub.EmployeeInfo employeeInfo = null;
        if(domainEmployeeInfo != null) {
            employeeInfo = new Paystub.EmployeeInfo();
            employeeInfo.setAddress(createPhysicalAddress(domainEmployeeInfo.getPstubAddress()));
            employeeInfo.setGivenName(domainEmployeeInfo.getFirstName());
            employeeInfo.setMiddleName(domainEmployeeInfo.getMiddleName());
            employeeInfo.setFamilyName(domainEmployeeInfo.getLastName());
            employeeInfo.setSSN(domainEmployeeInfo.getSSN());
            employeeInfo.setFederalTaxFilingInfo(createTaxFilingInformation(domainEmployeeInfo));
            employeeInfo.setWorkStateTaxFilingInfo(createWorkStateTaxFilingInformation(domainEmployeeInfo));
        }
        return employeeInfo;
    }

    public static Paystub.EmployerInfo createPaystubEmployerInfo(PstubEmployerInfo domainEmployerInfo) {
        Paystub.EmployerInfo employerInfo = null;
        if(domainEmployerInfo != null) {
            employerInfo = new Paystub.EmployerInfo();
            employerInfo.setAddress(createPhysicalAddress(domainEmployerInfo.getPstubAddress()));
            for (PstubStateTaxInfo pstubStateTaxInfo : domainEmployerInfo.getPstubStateTaxInfoCollection()) {
                employerInfo.getStateTaxId().add(createStateTaxInfo(pstubStateTaxInfo));
            }
            employerInfo.setName(domainEmployerInfo.getName());

        }
        return employerInfo;
    }

    public static PhysicalAddress createPhysicalAddress(PstubAddress address) {
        PhysicalAddress physicalAddress = null;
        if(address != null && !isAddressEmpty(address)) {
            physicalAddress = new PhysicalAddress();
            physicalAddress.setLine1(address.getLine1());
            physicalAddress.setLine2(address.getLine2());
            physicalAddress.setLine3(address.getLine3());
            physicalAddress.setLine4(address.getLine4());
            //TODO Is line 5 used? can we delete?
        }
        return physicalAddress;
    }

    public static StateTaxId createStateTaxInfo(PstubStateTaxInfo stateTaxInfo) {
        StateTaxId  stateTaxId = null;
        if(stateTaxInfo != null ) {
            stateTaxId = new StateTaxId();
            stateTaxId.setAgencyName(stateTaxInfo.getAgencyName());
            stateTaxId.setAgencyId(stateTaxInfo.getAgencyId());
        }
        return stateTaxId;
    }

    private static boolean isAddressEmpty(PstubAddress address) {
        return address.getLine1() == null && address.getLine2() == null && address.getLine3() == null && address.getLine4() == null;
    }

    public static TaxFilingInformation createTaxFilingInformation(PstubEmployeeInfo employeeInfo) {
        TaxFilingInformation taxFilingInformation = null;
        if(employeeInfo != null) {
            taxFilingInformation = new TaxFilingInformation();
            taxFilingInformation.setFilingStatus(employeeInfo.getFedTaxFilingStatus());
            taxFilingInformation.setAllowances(BigInteger.valueOf(employeeInfo.getFedAllowances()));
            taxFilingInformation.setExtras(CdmHelper.convertToFormattedBigDecimal(employeeInfo.getFedExtra(), SpcfDecimal.createInstance(0.0)));
        }
        return taxFilingInformation;
    }

    public static StateTaxFilingInformation createWorkStateTaxFilingInformation(PstubEmployeeInfo employeeInfo) {
        StateTaxFilingInformation stateTaxFilingInformation = null;
        if(employeeInfo != null) {
            stateTaxFilingInformation = new StateTaxFilingInformation();
            stateTaxFilingInformation.setState(employeeInfo.getTaxFilingState());
            stateTaxFilingInformation.setFilingStatus(employeeInfo.getStateTaxFilingStatus());
            stateTaxFilingInformation.setAllowances(BigInteger.valueOf(employeeInfo.getStateAllowances()));
            stateTaxFilingInformation.setExtras(CdmHelper.convertToFormattedBigDecimal(employeeInfo.getStateExtra(), SpcfDecimal.createInstance(0.0)));
        }
        return stateTaxFilingInformation;
    }

    public static EmployeePreference createEmployeePreference(PstubEmployeePreference pstubPEmployeePreference) {
        EmployeePreference eePref = new EmployeePreference();
        eePref.setAppName(pstubPEmployeePreference.getAppName());
        eePref.setEmployeeId(pstubPEmployeePreference.getEmployee().getId().toString());
        eePref.setPreferenceName(pstubPEmployeePreference.getPreferenceName());
        eePref.setPreferenceValue(pstubPEmployeePreference.getPreferenceValue());
        return eePref;
    }

    public static com.intuit.schema.ems.v3.EmployerPreference createEmployerPreference(EmployerPreference employerPreference) {
        com.intuit.schema.ems.v3.EmployerPreference cdmEmployerPreference = null;
        if(employerPreference != null) {
            cdmEmployerPreference = new com.intuit.schema.ems.v3.EmployerPreference();
            cdmEmployerPreference.setAppName(employerPreference.getAppName());
            cdmEmployerPreference.setPreferenceName(employerPreference.getPreferenceName());
            cdmEmployerPreference.setPreferenceValue(employerPreference.getPreferenceValue());
        }
        return cdmEmployerPreference;
    }

    public static com.intuit.schema.ems.v3.Entitlement createEntitlement(EntitlementUnit entitlementUnit) {
        com.intuit.schema.ems.v3.Entitlement entitlement = null;
        if (entitlementUnit != null
                && entitlementUnit.getEntitlement() != null
                && entitlementUnit.getEntitlement().getEntitlementCode() != null) {
            EntitlementCode entitlementCode = entitlementUnit.getEntitlement().getEntitlementCode();
            entitlement = new com.intuit.schema.ems.v3.Entitlement();
            if (entitlementUnit.getEntitlementUnitStatus() != null) {
                entitlement.setActive(
                        entitlementUnit.getEntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES));
            }
            entitlement.setPrimary(
                    entitlementCode.getIsPrimary());
            entitlement.setAssetItemCode(
                    entitlementCode.getAssetItemCd() != null ? entitlementCode.getAssetItemCd().name() : "");
            entitlement.setEditionType(
                    entitlementCode.getEditionType() != null ? entitlementCode.getEditionType().name() : "");
        }
        return entitlement;
    }

    public static com.intuit.schema.ems.v3.AddOnService createAddOnService(CompanyService companyService) {
        com.intuit.schema.ems.v3.AddOnService service = null;
        if (companyService.getService() != null
                && companyService.getService().getServiceCd() != null
                && companyService.getStatusCd() != null) {
            service = new AddOnService();
            service.setName(companyService.getService().getServiceCd().name());
            service.setStatus(companyService.getStatusCd().name());
        }
        return service;
    }
}
