package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxPaymentCheckDateSet")]
	public class TaxPaymentCheckDateSet
	{
		public var checkDateTotal:Number;
		[ArrayElementType("psp.sap.model.PaymentDetails")]
    	public var agencyTransactions:ArrayCollection;
	}
}