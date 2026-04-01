package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Contact;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

public class MtlTransactionRecordBuilder {

    private static final String NOT_AVAILABLE = "N/A";

    private FinancialTransaction financialTransaction;

    public MtlTransactionRecordBuilder financialTransaction(FinancialTransaction financialTransaction) {
        this.financialTransaction = financialTransaction;
        return this;
    }

    public MtlTransactionRecord build() {
        Company company = financialTransaction.getCompany();

        MtlTransactionRecord mtlTransactionRecord = new MtlTransactionRecord();
        setTransactionInfo(mtlTransactionRecord);
        setCustomerInfo(mtlTransactionRecord, company);
        setPrimaryPrincipalInfo(mtlTransactionRecord, company);
        setBeneficiaryInfo(mtlTransactionRecord);
        setSenderInfo(mtlTransactionRecord);
        setMiscellaneousInfo(mtlTransactionRecord);

        Validate.isTrue(mtlTransactionRecord.isValid(), "MtlTransactionRecord is not valid");

        return mtlTransactionRecord;
    }

    private void setTransactionInfo(MtlTransactionRecord mtlTransactionRecord) {
        mtlTransactionRecord.setTransactionNumber(financialTransaction.getId().toString());
        mtlTransactionRecord.setTransactionDate(financialTransaction.getSettlementDate().toString());
        mtlTransactionRecord.setTransactionAmount(financialTransaction.getFinancialTransactionAmount().toString());
        mtlTransactionRecord.setAmountAndCurrencyType(NOT_AVAILABLE);
        mtlTransactionRecord.setExchangeRate(NOT_AVAILABLE);
        mtlTransactionRecord.setTransactionFee(NOT_AVAILABLE);
    }

    private void setCustomerInfo(MtlTransactionRecord mtlTransactionRecord, Company company) {
        Address address = company.getLegalAddress();
        mtlTransactionRecord.setCustomerCompanyId(company.getSourceCompanyId());
        mtlTransactionRecord.setCustomerName(company.getLegalName());
        mtlTransactionRecord.setCustomerAddress(address.getAddressLine1());
        mtlTransactionRecord.setCustomerAddress2(address.getAddressLine2());
        mtlTransactionRecord.setCustomerCity(address.getCity());
        mtlTransactionRecord.setCustomerZipCode(address.getZipCode());
        mtlTransactionRecord.setCustomerState(address.getState());
        mtlTransactionRecord.setCustomerCountry(address.getCountry());
        mtlTransactionRecord.setCustomerTelephone(company.getPhone());
        mtlTransactionRecord.setCustomerPassportNum(NOT_AVAILABLE);
        mtlTransactionRecord.setCustomerPassportCountry(NOT_AVAILABLE);
        mtlTransactionRecord.setCustomerPhotoIdNum(NOT_AVAILABLE);
        mtlTransactionRecord.setCustomerPhotoIdType(NOT_AVAILABLE);
    }

    private void setPrimaryPrincipalInfo(MtlTransactionRecord mtlTransactionRecord, Company company) {
        Contact contact = getPrimaryPrincipalOfficer(company);
        mtlTransactionRecord.setPrimaryPrincipalOfficerName(contact.getFullName());
        mtlTransactionRecord.setPrimaryPrincipalOfficerAddress(contact.getMailingAddress().getFullAddress());
    }

    private void setBeneficiaryInfo(MtlTransactionRecord mtlTransactionRecord) {
        mtlTransactionRecord.setBeneficiaryCompanyId(null);
        mtlTransactionRecord.setBeneficiaryName(null);
        mtlTransactionRecord.setBeneficiaryCountry("US");
        mtlTransactionRecord.setBeneficiaryAddress(null);
        mtlTransactionRecord.setBeneficiaryPhone(null);

        mtlTransactionRecord.setBeneficiaryType(MtlTransactionRecordEnricher.BeneficiaryType.Employee.name());
    }

    private void setSenderInfo(MtlTransactionRecord mtlTransactionRecord) {
        mtlTransactionRecord.setSenderName(NOT_AVAILABLE);
        mtlTransactionRecord.setSenderAddress(NOT_AVAILABLE);
        mtlTransactionRecord.setSenderPhone(NOT_AVAILABLE);
    }

    private void setMiscellaneousInfo(MtlTransactionRecord mtlTransactionRecord) {
        mtlTransactionRecord.setOfficeLocation(NOT_AVAILABLE);
        mtlTransactionRecord.setPaymentMethod("ACH");
        mtlTransactionRecord.setEmployeeInitials(NOT_AVAILABLE);
        mtlTransactionRecord.setComments(NOT_AVAILABLE);
        mtlTransactionRecord.setProduct("Payroll");
        mtlTransactionRecord.setRails("PSP");
    }

    private Contact getPrimaryPrincipalOfficer(Company company) {
        DomainEntitySet<Contact> contacts = company.getContactCollection();
        Optional<Contact> contactOptional = contacts
                .stream()
                .filter(contact -> contact.getContactRoleCd() == ContactRole.PrimaryPrincipal)
                .findFirst();
        return contactOptional.get();
    }

}
