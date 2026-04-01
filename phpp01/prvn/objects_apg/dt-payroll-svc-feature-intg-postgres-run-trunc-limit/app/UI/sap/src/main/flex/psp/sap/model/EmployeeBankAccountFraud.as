package psp.sap.model
{
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.events.PropertyChangeEvent;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeBankAccountFraud")]
	
	public class EmployeeBankAccountFraud extends EventDispatcher
	{
		public var bankName:String;
		public var bankAccountNumber:String;
		
		[ArrayElementType ("psp.sap.model.EmployeeInfo")]
		public var employeeInfo:ArrayCollection;
	}
}