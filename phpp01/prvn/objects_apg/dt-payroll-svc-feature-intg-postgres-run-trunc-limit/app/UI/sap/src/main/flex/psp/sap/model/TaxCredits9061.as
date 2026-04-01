package psp.sap.model {

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxCredits9061")]
    public class TaxCredits9061 {
        public var ein:String;
        public var ssn:String;
        public var formId:String;
        public var createdDate:Date;

        public var applicationPassword:String;
        public var employerEmail:String;
        public var employeeEmail:String;
        public var signersRemaining:String;
        public var applicationId:String;

        [Transient]
        [Bindable("propertyChange")]
        public function get hasUnsignedApplication():Boolean {
            return applicationId != null;
        }

        [Transient]
        [Bindable("propertyChange")]
        public function get hasSignedApplication():Boolean {
            return applicationId != null && signersRemaining != "ER, EE";
        }


    }
}