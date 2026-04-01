package psp.sap.model
{
	import intuit.sbd.flex.framework.model.EntityObject;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturn")]
	[CopyTestCase(nullOk="bankReturnExtendedInfo")]
	public class BankReturn
	{
		public var statusCd:String;
		public var payrollStatus:String
	    public var txnType:String;	    
	    public var companyName:String;
	    public var employeeName:String;
	    public var companyId:String;
	    public var companySourceSystemCd:String;
	    public var sourcePayRunId:String;
	    public var fein:String;
	    public var bankAccountNumber:String;
	    public var bankRoutingNumber:String;
	    public var amount:Number;
	    public var returnCd:String;
	    public var checkDate:Date;
	    public var returnDate:Date;
	    public var txnId:String;
	    public var bankReturnExtendedInfo:BankReturnExtendedInfo;	    
	    
	   
	    // convert txn type code to readable string
	    [Transient]
	    public function get transactionType():String {
	    	return TransactionType(SAP.instance.lookupService.transactionTypes.getItemByKey(txnType)).name;
	    }
	    
	    [Transient]
	    public function canUpdateReturns():Boolean {
	    	return SAP.canPerformOperation(OperationsEnum.BANK_RETURN_UPDATE);
	    }
	    
	    [Transient]
	    public function canReopenReturn():Boolean {
	    	return statusCd == "Resolved";
	    }
	    
	    [Transient]
	    public function get payrollStatusName():String {
	    	var status:Object = SAP.instance.lookupService.payrollStatus.getItemByKey(payrollStatus);
	    	if(status != null){
	    		return status.label;
	    	}	    		    	
	    	return payrollStatus;
	    }
	    
	    [Transient]
	    public function get returnDateTime():Number {
			return returnDate.getTime();
	    }
	   
	
	    
	}
}
