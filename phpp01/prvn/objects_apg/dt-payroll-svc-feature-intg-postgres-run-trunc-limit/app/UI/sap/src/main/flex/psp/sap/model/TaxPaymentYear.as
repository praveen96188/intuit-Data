package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxPaymentYear")]
	public class TaxPaymentYear
	{
		public var year:String;
		[ArrayElementType("psp.sap.model.PaymentTemplate")]
    	public var paymentTemplates:ArrayCollection;
    	
    	[Transient]
    	public var isYearShowing:Boolean = false;
	}
}