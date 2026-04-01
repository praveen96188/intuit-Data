package psp.taxcredits.dto {
    import psp.taxcredits.model.ModelUtils;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.EligibilityInfo")]
    public class EligibilityInfo {
        public var snapLast6Months:Boolean;
        public var snapLast3of5MonthsNoLongerEligible:Boolean;
        public var snapPrimaryRecipient:String;
        public var snapCityStateBenefitsReceived:String;

        public var tanfLast18Months:Boolean;
        public var tanfStopLaw2Years:Boolean;
        public var tanf9of18Months:Boolean;
        public var tanfPrimaryRecipient:String;
        public var tanfCityStateBenefitsReceived:String;

        public var referralVocationalRehabilitationAgency:Boolean;
        public var referralEmploymentNetwork:Boolean;
        public var referralDepartmentVeteranAffairs:Boolean;

        public var veteran:Boolean;
        public var veteranSnap3of15Months:Boolean;
        public var veteranSnapPrimaryRecipient:String;
        public var veteranSnapCityStateBenefitsReceived:String;
        public var veteranDisability:Boolean;
        public var veteranDischargedBeforeHired:Boolean;
        public var veteranUnemployed6MonthsBeforeHired:Boolean;
        public var veteranServed180Days:Boolean;
        public var veteranDischargedServiceRelatedDisability:Boolean;
        public var veteranDischargedPast5Years:Boolean;
        public var veteranUnemploymentCompensation4weeksOfLastYear:Boolean;

        public var designatedEZorRC:Boolean;        
        
        public var disconnectedSchoolLessThan10Hours:Boolean;
        public var disconnectedNotRegularlyEmployedLast6Months:Boolean;
        public var disconnectedGraduateGED:Boolean;
        public var disconnectedAdmittedSinceCertificate:Boolean;

        public var felonLastYear:Boolean;
        public var felonConvictionDateString:String;
        public var felonReleaseDateString:String;
        public var felonFederal:Boolean;

        public var ssiWithin60Days:Boolean;

        public var summerYouthBetween16and18:Boolean;
        public var summerYouthEmployedBetween1Mayand15September:Boolean;

        public var conditionalCertification:Boolean;

        public var unemployed60:Boolean;

        public function set felonConvictionDate(value:Date):void {
            felonConvictionDateString = ModelUtils.dateToString(value);
        }

        public function set felonReleaseDate(value:Date):void {
            felonReleaseDateString = ModelUtils.dateToString(value);
        }
    }
}