package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFraudEvent")]
	public class FraudEvent
	{
		
		public var fraudIndicator:String;
	    public var companyName:String;
	    public var companyId:String;
	    public var sourceSystemCd:String;
	    public var companyEin:String;
	    public var employeeName:String;
	    public var payrollAmount:Number;
	    public var eventTimeStamp:Date;
	    public var details:String;
	    public var fraudFlagSet:Boolean;
	    
	    // Fraud Detail
	    public var sourcePayRunId:String;
	    public var payrollCheckDate:Date;
	    public var payrollRunDate:Date;
	    public var payrollRunStatus:String;  
	    
	    [Transient]	    
	    public function fraudIndicatorLabel():String {
	    	return FraudIndicatorEnum.getLabelFromCode(fraudIndicator);
	    }
	}
}