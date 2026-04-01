package com.intuit.sbd.payroll.psp.adapters.taxcredits.dto;

import com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter.TaxCreditsTranslator;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Jan 22, 2010
 * Time: 12:55:24 PM
 * values from questionnaire
 * assumption is that all unanswered questions are logically equivalent to No (false)
 *
 */
public class EligibilityInfo {
    private boolean snapLast6Months;
    private boolean snapLast3of5MonthsNoLongerEligible;
    private String snapPrimaryRecipient;
    private String snapCityStateBenefitsReceived;

    private boolean tanfLast18Months;
    private boolean tanfStopLaw2Years;
    private boolean tanf9of18Months;
    private String tanfPrimaryRecipient;
    private String tanfCityStateBenefitsReceived;

    private boolean referralVocationalRehabilitationAgency;
    private boolean referralEmploymentNetwork;
    private boolean referralDepartmentVeteranAffairs;

    private boolean veteran;
    private boolean veteranSnap3of15Months;
    private String veteranSnapPrimaryRecipient;
    private String veteranSnapCityStateBenefitsReceived;
    private boolean veteranDisability;
    private boolean veteranDischargedBeforeHired;
    private boolean veteranUnemployed6MonthsBeforeHired;
    private boolean veteranServed180Days;
    private boolean veteranDischargedServiceRelatedDisability;
    private boolean veteranDischargedPast5Years;
    private boolean veteranUnemploymentCompensation4weeksOfLastYear;

    private boolean designatedEZorRC;    
    
    private boolean disconnectedSchoolLessThan10Hours;
    private boolean disconnectedNotRegularlyEmployedLast6Months;
    private boolean disconnectedGraduateGED;
    private boolean disconnectedAdmittedSinceCertificate;

    private boolean felonLastYear;
    private String felonConvictionDateString;
    private String felonReleaseDateString;
    private boolean felonFederal;    

    private boolean ssiWithin60Days;
    
    private boolean summerYouthEmployedBetween1Mayand15September;

    private boolean conditionalCertification;

    private boolean unemployed60;


    public boolean getSnapLast6Months() {
        return snapLast6Months;
    }

    public void setSnapLast6Months(boolean snapLast6Months) {
        this.snapLast6Months = snapLast6Months;
    }

    public boolean getSnapLast3of5MonthsNoLongerEligible() {
        return snapLast3of5MonthsNoLongerEligible;
    }

    public void setSnapLast3of5MonthsNoLongerEligible(boolean snapLast3of5MonthsNoLongerEligible) {
        this.snapLast3of5MonthsNoLongerEligible = snapLast3of5MonthsNoLongerEligible;
    }

    public String getSnapPrimaryRecipient() {
        return snapPrimaryRecipient;
    }

    public void setSnapPrimaryRecipient(String snapPrimaryRecipient) {
        this.snapPrimaryRecipient = snapPrimaryRecipient;
    }

    public String getSnapCityStateBenefitsReceived() {
        return snapCityStateBenefitsReceived;
    }

    public void setSnapCityStateBenefitsReceived(String snapCityStateBenefitsReceived) {
        this.snapCityStateBenefitsReceived = snapCityStateBenefitsReceived;
    }

    public boolean getTanfLast18Months() {
        return tanfLast18Months;
    }

    public void setTanfLast18Months(boolean tanfLast18Months) {
        this.tanfLast18Months = tanfLast18Months;
    }

    public boolean getTanfStopLaw2Years() {
        return tanfStopLaw2Years;
    }

    public void setTanfStopLaw2Years(boolean tanfStopLaw2Years) {
        this.tanfStopLaw2Years = tanfStopLaw2Years;
    }

    public boolean getTanf9of18Months() {
        return tanf9of18Months;
    }

    public void setTanf9of18Months(boolean tanf9of18Months) {
        this.tanf9of18Months = tanf9of18Months;
    }

    public String getTanfPrimaryRecipient() {
        return tanfPrimaryRecipient;
    }

    public void setTanfPrimaryRecipient(String tanfPrimaryRecipient) {
        this.tanfPrimaryRecipient = tanfPrimaryRecipient;
    }

    public String getTanfCityStateBenefitsReceived() {
        return tanfCityStateBenefitsReceived;
    }

    public void setTanfCityStateBenefitsReceived(String tanfCityStateBenefitsReceived) {
        this.tanfCityStateBenefitsReceived = tanfCityStateBenefitsReceived;
    }

    public boolean getReferralVocationalRehabilitationAgency() {
        return referralVocationalRehabilitationAgency;
    }

    public void setReferralVocationalRehabilitationAgency(boolean referralVocationalRehabilitationAgency) {
        this.referralVocationalRehabilitationAgency = referralVocationalRehabilitationAgency;
    }

    public boolean getReferralEmploymentNetwork() {
        return referralEmploymentNetwork;
    }

    public void setReferralEmploymentNetwork(boolean referralEmploymentNetwork) {
        this.referralEmploymentNetwork = referralEmploymentNetwork;
    }

    public boolean getReferralDepartmentVeteranAffairs() {
        return referralDepartmentVeteranAffairs;
    }

    public void setReferralDepartmentVeteranAffairs(boolean referralDepartmentVeteranAffairs) {
        this.referralDepartmentVeteranAffairs = referralDepartmentVeteranAffairs;
    }

    public boolean getVeteran() {
        return veteran;
    }

    public void setVeteran(boolean veteran) {
        this.veteran = veteran;
    }

    public boolean getVeteranSnap3of15Months() {
        return veteranSnap3of15Months;
    }

    public void setVeteranSnap3of15Months(boolean veteranSnap3of15Months) {
        this.veteranSnap3of15Months = veteranSnap3of15Months;
    }

    public String getVeteranSnapPrimaryRecipient() {
        return veteranSnapPrimaryRecipient;
    }

    public void setVeteranSnapPrimaryRecipient(String veteranSnapPrimaryRecipient) {
        this.veteranSnapPrimaryRecipient = veteranSnapPrimaryRecipient;
    }

    public String getVeteranSnapCityStateBenefitsReceived() {
        return veteranSnapCityStateBenefitsReceived;
    }

    public void setVeteranSnapCityStateBenefitsReceived(String veteranSnapCityStateBenefitsReceived) {
        this.veteranSnapCityStateBenefitsReceived = veteranSnapCityStateBenefitsReceived;
    }

    public boolean getVeteranDisability() {
        return veteranDisability;
    }

    public void setVeteranDisability(boolean veteranDisability) {
        this.veteranDisability = veteranDisability;
    }

    public boolean getVeteranDischargedBeforeHired() {
        return veteranDischargedBeforeHired;
    }

    public void setVeteranDischargedBeforeHired(boolean veteranDischargedBeforeHired) {
        this.veteranDischargedBeforeHired = veteranDischargedBeforeHired;
    }

    public boolean getVeteranUnemployed6MonthsBeforeHired() {
        return veteranUnemployed6MonthsBeforeHired;
    }

    public void setVeteranUnemployed6MonthsBeforeHired(boolean veteranUnemployed6MonthsBeforeHired) {
        this.veteranUnemployed6MonthsBeforeHired = veteranUnemployed6MonthsBeforeHired;
    }

    public boolean getVeteranServed180Days() {
        return veteranServed180Days;
    }

    public void setVeteranServed180Days(boolean veteranServed180Days) {
        this.veteranServed180Days = veteranServed180Days;
    }

    public boolean getVeteranDischargedServiceRelatedDisability() {
        return veteranDischargedServiceRelatedDisability;
    }

    public void setVeteranDischargedServiceRelatedDisability(boolean veteranDischargedServiceRelatedDisability) {
        this.veteranDischargedServiceRelatedDisability = veteranDischargedServiceRelatedDisability;
    }

    public boolean getVeteranDischargedPast5Years() {
        return veteranDischargedPast5Years;
    }

    public void setVeteranDischargedPast5Years(boolean veteranDischargedPast5Years) {
        this.veteranDischargedPast5Years = veteranDischargedPast5Years;
    }

    public boolean getVeteranUnemploymentCompensation4weeksOfLastYear() {
        return veteranUnemploymentCompensation4weeksOfLastYear;
    }

    public void setVeteranUnemploymentCompensation4weeksOfLastYear(boolean veteranUnemploymentCompensation4weeksOfLastYear) {
        this.veteranUnemploymentCompensation4weeksOfLastYear = veteranUnemploymentCompensation4weeksOfLastYear;
    }

    public boolean getDesignatedEZorRC() {
        return designatedEZorRC;
    }

    public void setDesignatedEZorRC(boolean designatedEZorRC) {
        this.designatedEZorRC = designatedEZorRC;
    }

    public boolean getDisconnectedSchoolLessThan10Hours() {
        return disconnectedSchoolLessThan10Hours;
    }

    public void setDisconnectedSchoolLessThan10Hours(boolean disconnectedSchoolLessThan10Hours) {
        this.disconnectedSchoolLessThan10Hours = disconnectedSchoolLessThan10Hours;
    }

    public boolean getDisconnectedNotRegularlyEmployedLast6Months() {
        return disconnectedNotRegularlyEmployedLast6Months;
    }

    public void setDisconnectedNotRegularlyEmployedLast6Months(boolean disconnectedNotRegularlyEmployedLast6Months) {
        this.disconnectedNotRegularlyEmployedLast6Months = disconnectedNotRegularlyEmployedLast6Months;
    }

    public boolean getDisconnectedGraduateGED() {
        return disconnectedGraduateGED;
    }

    public void setDisconnectedGraduateGED(boolean disconnectedGraduateGED) {
        this.disconnectedGraduateGED = disconnectedGraduateGED;
    }

    public boolean getDisconnectedAdmittedSinceCertificate() {
        return disconnectedAdmittedSinceCertificate;
    }

    public void setDisconnectedAdmittedSinceCertificate(boolean disconnectedAdmittedSinceCertificate) {
        this.disconnectedAdmittedSinceCertificate = disconnectedAdmittedSinceCertificate;
    }

    public boolean getFelonLastYear() {
        return felonLastYear;
    }

    public void setFelonLastYear(boolean felonLastYear) {
        this.felonLastYear = felonLastYear;
    }

    public String getFelonConvictionDateString() {
        return felonConvictionDateString;
    }

    public void setFelonConvictionDateString(String felonConvictionDateString) {
        this.felonConvictionDateString = felonConvictionDateString;
    }

    public String getFelonReleaseDateString() {
        return felonReleaseDateString;
    }

    public void setFelonReleaseDateString(String felonReleaseDateString) {
        this.felonReleaseDateString = felonReleaseDateString;
    }

    public boolean getFelonFederal() {
        return felonFederal;
    }

    public void setFelonFederal(boolean felonFederal) {
        this.felonFederal = felonFederal;
    }

    public boolean getSsiWithin60Days() {
        return ssiWithin60Days;
    }

    public void setSsiWithin60Days(boolean ssiWithin60Days) {
        this.ssiWithin60Days = ssiWithin60Days;
    }

    public boolean getSummerYouthEmployedBetween1Mayand15September() {
        return summerYouthEmployedBetween1Mayand15September;
    }

    public void setSummerYouthEmployedBetween1Mayand15September(boolean summerYouthEmployedBetween1Mayand15September) {
        this.summerYouthEmployedBetween1Mayand15September = summerYouthEmployedBetween1Mayand15September;
    }

    public boolean getConditionalCertification() {
        return conditionalCertification;
    }

    public void setConditionalCertification(boolean conditionalCertification) {
        this.conditionalCertification = conditionalCertification;
    }

    public Date getFelonConvictionDate() {
        return TaxCreditsTranslator.parseDate(getFelonConvictionDateString());
    }

    public Date getFelonReleaseDate() {
        return TaxCreditsTranslator.parseDate(getFelonReleaseDateString());
    }

    public boolean getUnemployed60() {
        return unemployed60;
    }

    public void setUnemployed60(boolean unemployed60) {
        this.unemployed60 = unemployed60;
    }
}
