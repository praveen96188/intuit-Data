package psp.sap.model
{

import mx.collections.ArrayCollection;

[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeInfo")]
	public class EmployeeInfo
	{
		public var employeeGseq:String;
		public var employeeId:String;
	    public var firstName:String;
	    public var lastName:String;
	    public var middleName:String;
	    public var socialSecurityNumber:String;
	    public var stateLive:String;
	    public var stateWork:String;
	    public var dd:Boolean;
	    public var firstPayDate:Date;
	    public var lastPayDate:Date;
        public var hireDate:Date;
        public var birthDate:Date;
        public var termDate:Date;
        public var rehireDate:Date;
		public var status:String;
        public var mailingAddress:Address;
        public var enforceSubjectTo:Boolean;
		public var isSeasonal:String;

		[Transient]
		public function get statusName():String {
			return (status == "Inactive") ? "Terminated" : status;
		}
		
		[Transient]
        [Bindable("propertyChange")]
		public function get fullName():String {
			var retName:String = "";

            var retFirstName:String = firstName == null ? "" : firstName;
            var retLastName:String = lastName == null ? "" : lastName;

			retName = retLastName + ", " + retFirstName + ((middleName != null && middleName != "") ? (" " + middleName) : "");

			return retName;
		}
		
		[Transient]
		public function get fullNameForward():String {
			var retName:String = "";

            var retFirstName:String = firstName == null ? "" : firstName;
            var retLastName:String = lastName == null ? "" : lastName;

            retName = retFirstName + ((middleName != null && middleName != "") ? (" " + middleName) : "") + " " + retLastName;

			
			return retName;
		}

        [Transient]
		public function get actionCollection():ArrayCollection {
            var actionCollection:ArrayCollection = new ArrayCollection();
            actionCollection.addItem(new ActionEvent(ActionEventCode.VIEW_PAYROLLS, "View Paychecks"));
            actionCollection.addItem(new ActionEvent(ActionEventCode.VIEW_EE_HISTORY, "View History"));
            return actionCollection;
		}

		

	}
}