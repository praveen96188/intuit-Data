package psp.sap.model
{		
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturnExtendedInfo")]	
	public class BankReturnExtendedInfo
	{	
		public var expectedResolutionDate:Date;
		public var payrollBalanceDue:Number;					
	}
}