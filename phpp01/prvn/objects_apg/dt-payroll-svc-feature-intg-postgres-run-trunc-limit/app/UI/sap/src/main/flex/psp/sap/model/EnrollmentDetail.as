package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEnrollmentDetail")]
    public class EnrollmentDetail {
        public var companyName:String;
        public var companyKey:CompanyKey;
        public var ein:String;
        public var status:String;
        public var rejectionDate:Date;
        public var enrollmentId:String;

       
        public function get companyId():String
        {
            return companyKey.companyId;
        }
        
    }
}

