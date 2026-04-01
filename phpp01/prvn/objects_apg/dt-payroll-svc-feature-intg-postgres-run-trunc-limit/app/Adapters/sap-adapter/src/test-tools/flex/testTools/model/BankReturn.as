package testTools.model
{
	import intuit.sbd.flex.framework.model.EntityObject;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTBankReturn")]
	public class BankReturn extends EntityObject
	{
	    public var bankReturnCd:String;
	    public var creationDate:Date;
	    public var id:String; // GUID
	    public var transactionId:String;  // GUID
	    public var sourceEmployeeId:String;
	    public var employeeDisplayName:String; // First name + last name
	    public var traceNumber:String;
	    public var returnStatus:String;
	    public var statusChangeDate:Date;
	    public var routingNumber:String;
	    public var accountNumber:String;
	    public var accountType:String;
	    public var description:String;
	}
}