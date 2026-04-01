package psp.taxcredits.dto {
    import psp.taxcredits.model.ModelUtils;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.EmployeeInfo")]
    public class EmployeeInfo {
        public var firstName:String;
        public var lastName:String;
        public var middleInitial:String;
        public var ssn:String;
        public var liveAddress:Address;
        public var telephoneNumber:String;
        public var telephoneExtension:String;
        public var email:String;
        public var dateOfBirthString:String;
        public var jobOfferDateString:String;
        public var hireDateString:String;
        public var startDateString:String;
        public var startingWage:String;
        public var position:String;
        public var workState:String;

        public function set dateOfBirth(value:Date):void {
            dateOfBirthString = ModelUtils.dateToString(value);
        }

        public function set jobOfferDate(value:Date):void {
            jobOfferDateString = ModelUtils.dateToString(value);
        }

        public function set hireDate(value:Date):void {
            hireDateString = ModelUtils.dateToString(value);
        }

        public function set startDate(value:Date):void {
            startDateString = ModelUtils.dateToString(value);            
        }
      
    }
}