package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyInfoDTO")]
    public class AgencyInfoDTO {
        public var agency:Agency;
        public var currentEFTPSStatus:String;
        public var currentRAFStatus:String;
        public var currentACHEnrollmentStatus:String;
        public var nameControl:String;
        public var erFicaDeferralEnabled:Boolean;

        [ArrayElementType("psp.sap.model.CompanyPaymentTemplate")]
        public var companyPaymentTemplates:ArrayCollection;

        public function isIRS():Boolean {
            return (agency.agencyId != null && agency.agencyId.toUpperCase() === 'IRS');
        }
    }
}