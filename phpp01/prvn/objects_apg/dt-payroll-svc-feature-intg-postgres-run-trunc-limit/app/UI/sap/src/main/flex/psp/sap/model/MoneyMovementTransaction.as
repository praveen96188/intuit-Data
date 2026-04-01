package psp.sap.model
{
	import mx.utils.ObjectUtil;
		
	[Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPMoneyMovementTransaction")]
	public class MoneyMovementTransaction
	{		
		public var spcfId:String;
		public var achReason:String;
	    public var checkDate:Date;
	    public var settlementDate:Date;
	    public var creationDate:Date;
	    public var achAmount:Number;
	    public var bankAccount:CompanyBankAccount;
	    public var showDetail:Boolean;
	    public var HPDE:Boolean;	
	    
	    [Transient]
	    public function compare(value:MoneyMovementTransaction):Boolean {
	    	return value != null && this.spcfId == value.spcfId;
	    }   
	     	   
	}
}