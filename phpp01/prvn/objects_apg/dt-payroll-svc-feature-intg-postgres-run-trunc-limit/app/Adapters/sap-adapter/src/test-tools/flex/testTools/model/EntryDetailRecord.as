package testTools.model
{
	import intuit.sbd.flex.framework.model.EntityObject;
	
	import mx.collections.ArrayCollection;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTEntryDetailRecord")]
	public class EntryDetailRecord extends EntityObject
	{
	    public var amount:Number;
	    public var traceNumber:String;
	    public var creditDebitIndicator:String;
	    public var settlementDate:Date;
	    public var accountNumber:String;
	    public var routingNumber:String;
	    public var bankName:String;
	    public var bankAccountType:String;
	    public var bankAccountOwnerType:String;
	    public var companyId:String;
	    public var companyLegalName:String;
	    public var individualName:String;
	    public var mmTransactionId:String;
	    
	    [ArrayElementType("testTools.model.BankReturn")]
	    public var bankReturns:ArrayCollection;
	    
	    [Transient]
	    public function get canMakeReturn():Boolean {
	    	return this.individualName.substr(0, 6) != "INTUIT" && !bankReturnsExists;
	    }	    
	    
	    public var bankReturnsExists:Boolean;
	    
	    [Transient]
	    public var bankReturnCd:String = "";				
	}
}