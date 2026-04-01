package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by charithah418 on 6/2/15.
 */
public class JPMCEventMessageBuilder {
    private final JPMCEventMessage mJPMCEventMessage;

    private JPMCEventMessageBuilder() {
        mJPMCEventMessage = new JPMCEventMessage();
    }

    public static JPMCEventMessageBuilder JPMCEventMessage() {
        return new JPMCEventMessageBuilder();
    }

    public JPMCEventMessageBuilder withFirstName(String firstName) {
        mJPMCEventMessage.setFirstName(firstName);
        return this;
    }

    public JPMCEventMessageBuilder withMiddleName(String middleName) {
        mJPMCEventMessage.setMiddleName(middleName);
        return this;
    }

    public JPMCEventMessageBuilder withLastName(String lastName) {
        mJPMCEventMessage.setLastName(lastName);
        return this;
    }

    public JPMCEventMessageBuilder withAddressLine1(String addressLine1) {
        mJPMCEventMessage.setAddressLine1(addressLine1);
        return this;
    }

    public JPMCEventMessageBuilder withAddressLine2(String addressLine2) {
        mJPMCEventMessage.setAddressLine2(addressLine2);
        return this;
    }

    public JPMCEventMessageBuilder withCity(String city) {
        mJPMCEventMessage.setCity(city);
        return this;
    }

    public JPMCEventMessageBuilder withState(String state) {
        mJPMCEventMessage.setState(state);
        return this;
    }

    public JPMCEventMessageBuilder withCountry(String country) {
        mJPMCEventMessage.setCountry(country);
        return this;
    }

    public JPMCEventMessageBuilder withZipCode(String zipCode) {
        mJPMCEventMessage.setZipCode(zipCode);
        return this;
    }

    public JPMCEventMessageBuilder withSsn(String ssn) {
        mJPMCEventMessage.setSsn(ssn);
        return this;
    }

    public JPMCEventMessageBuilder withSourceCompanyId(String sourceCompanyId) {
        mJPMCEventMessage.setSourceCompanyId(sourceCompanyId);
        return this;
    }

    public JPMCEventMessageBuilder withDateOfBirth(Calendar dateOfBirth) {
        mJPMCEventMessage.setDateOfBirth(dateOfBirth);
        return this;
    }

    public  JPMCEventMessageBuilder withEmail(String email){
        mJPMCEventMessage.setEmail(email);
        return this;
    }

    public  JPMCEventMessageBuilder withPhoneNumber(String phoneNumber){
        mJPMCEventMessage.setPhoneNumber(phoneNumber);
        return this;
    }

    public  JPMCEventMessageBuilder withLegalName(String legalName){
        mJPMCEventMessage.setLegalName(legalName);
        return this;
    }

    public  JPMCEventMessageBuilder withDba(String dba){
        mJPMCEventMessage.setDba(dba);
        return this;
    }

    public  JPMCEventMessageBuilder withIndustrySicCode(String industrySicCode){
        mJPMCEventMessage.setIndustrySicCode(industrySicCode);
        return this;
    }

    public  JPMCEventMessageBuilder withFedTaxId(String fedTaxId){
        mJPMCEventMessage.setFedTaxId(fedTaxId);
        return this;
    }

    public  JPMCEventMessageBuilder withRealmId(String realmId){
        mJPMCEventMessage.setRealmId(realmId);
        return this;
    }
    
     public  JPMCEventMessageBuilder withRecordStatus(String recordStatus){
        mJPMCEventMessage.setRecordStatus(recordStatus);
        return this;
    }

    public  JPMCEventMessageBuilder withUniqueId(String uniqueId){
        mJPMCEventMessage.setUniqueID(uniqueId);
        return this;
    }

    public JPMCEventMessage build()  {
        //validate();
        return mJPMCEventMessage;
    }
    
    

    private void validate(){
        // TODO: If some validations are required it is done here.
    }
}
