package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.List;

/**
 * Hand-written business logic
 */
public class Contact extends BaseContact {
    private boolean ssnDecrypted = false;
    public static String SSOKeyName="Contact_SSN";
    public static String DOBKeyName="Contact_DoB";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Contact()
    {
        super();
    }

    /**
     * Validates Contact data entity.
     *
     * @return ProcessResult
     */
    public ProcessResult validateContact() {
        ProcessResult processResult = new ProcessResult();

        if (this == null) {
            return processResult;
        }

        String contactFullName = getFirstName() + ' ' + getLastName();
        processResult.merge(validateIndividual(EntityName.Contact, contactFullName));

        if ((getContactRoleCd() == null) ||
                (getContactRoleCd() != ContactRole.PayrollAdmin &&
                        getContactRoleCd() != ContactRole.PrimaryPrincipal &&
                        getContactRoleCd() != ContactRole.SecondaryPrincipal &&
                        getContactRoleCd() != ContactRole.Other)) {
            processResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "ContactRoleCd");
        }

        return processResult;
    }

    public List<FraudContact> findFraudContactsLike() {
        HqlBuilder hql = new HqlBuilder(" select fraudContact");

        hql.append(" from com.intuit.sbd.payroll.psp.domain.FraudContact as fraudContact ");
        hql.append(" join fetch fraudContact.Company ");
        hql.append(" where (lower(COALESCE(cast(fraudContact.FirstName as java.lang.String),'')) =lower(COALESCE(cast(:firstName as java.lang.String),'')) and lower(COALESCE(cast(fraudContact.LastName as java.lang.String),''))=lower(COALESCE(cast(:lastName as java.lang.String),''))");

        hql.setParameter("firstName", getFirstName());
        hql.setParameter("lastName", getLastName());


        if (getPhone() != null) {
            hql.append("   or lower(COALESCE(cast(fraudContact.Phone as java.lang.String),''))= lower(COALESCE(cast(:phoneNumber as java.lang.String),''))");
            hql.setParameter("phoneNumber", getPhone());
        }

        if (getEmail() != null) {
            hql.append("   or lower(COALESCE(cast(fraudContact.Email as java.lang.String),'')) = lower(COALESCE(cast(:email as java.lang.String),''))");
            hql.setParameter("email", getEmail());
        }

        hql.append(")");

        return hql.list(0, 25);
    }

    /*
       The social security Number is encrypted and persisted in the Database
    */
    public void setSocialSecurityNumberPlainText(String pSocialSecurityNumber){
        setSocialSecurityNumber(pSocialSecurityNumber);
    }

    /*
        The social security Number is encrypted and persisted in the Database.
        When we retrieve the Social security number we can decrypt it and get it as plain text
     */
    public String getSocialSecurityNumberPlainText(){
        return getSocialSecurityNumber();
    }


    public void setSocialSecurityNumber(String pSocialSecurityNumber) {
        super.setSocialSecurityNumberEnc(EncryptionUtils.deterministicEncrypt(SSOKeyName,pSocialSecurityNumber));
    }


    public String getSocialSecurityNumber() {
        return EncryptionUtils.deterministicDecrypt(SSOKeyName, getSocialSecurityNumberEnc());
    }


    public void setDateOfBirth(SpcfCalendar pDateOfBirth) {
        super.setDateOfBirthEnc(EncryptionUtils.probabilisticEncryptDate(DOBKeyName, pDateOfBirth, getId().toString()));
    }


    public SpcfCalendar getDateOfBirth() {
        return EncryptionUtils.probabilisticDecryptDate(DOBKeyName, getDateOfBirthEnc());
    }
}