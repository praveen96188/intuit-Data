package psp.sap.model
{
	[ValueEquals(exclude="sourceBankAccountName")]
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyBankAccount")]
	public class CompanyBankAccount
	{
		public var accountId:String;
    	public var accountNumber:String;
    	public var routingNumber:String;
    	public var bankName:String;
    	public var accountType:String;
    	public var bankAccountStatusCd:String;
    	public var verifyRetryCount:Number;
    	public var sourceBankAccountName:String;
    	public var sourceBankAccountId:String;


    	public function toString():String {
    		return bankName + " - " + accountNumber;
    	}
    	
    	[Transient]
    	public var verifyAmount1:Number = 0.00;
    	
    	[Transient]
    	public var verifyAmount2:Number = 0.00;
    	
    	
    	[Transient]
    	[Bindable ("propertyChange")]
    	public function get bankAccountStatus():String {
    		if(bankAccountStatusCd == "PendingVerification") {
    			return "Pending Verification";
    		} else {
    			return bankAccountStatusCd;
    		}
    	}
    	
    	[Transient]
    	[Bindable ("propertyChange")]
    	public function get isPendingVerification():Boolean {
    		return (bankAccountStatusCd == "PendingVerification");
    	}
    	
    	[Transient]
    	[Bindable ("propertyChange")]
    	public function get isActive():Boolean {
    		return (bankAccountStatusCd == "Active");
    	}
    	
    	[Transient]
    	public function get isInactive():Boolean {
    		return (bankAccountStatusCd == "Inactive");
    	}
    	
	}
}