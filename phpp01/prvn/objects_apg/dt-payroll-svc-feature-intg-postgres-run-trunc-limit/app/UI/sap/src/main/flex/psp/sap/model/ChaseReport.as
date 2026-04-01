package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPChaseReport")]
	public class ChaseReport
	{
		public var connectionDate:Date;
    	public var postingDate:Date;
	    public var companyName:String;
	    public var sourceSystem:String;
	    public var bankName:String;
	    
	    [ArrayElementType("psp.sap.model.ChaseReportTransaction")]
	    public var transactions:ArrayCollection;
	    
	    [Transient]
	    public function get debitTotal():Number {
	    	var total:Number = 0;
	    	for each(var transaction:ChaseReportTransaction in transactions){
	    		total += transaction.debitAmount;
	    	}
	    	return total;
	    }
	    
	    [Transient]
	    public function get creditTotal():Number {
	    	var total:Number = 0;
	    	for each(var transaction:ChaseReportTransaction in transactions){
	    		total += transaction.creditAmount;
	    	}
	    	return total;
	    }

	}
}