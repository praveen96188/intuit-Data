package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentTemplateYearPayment")]
	public class PaymentTemplateYearPayment
	{
		[ArrayElementType("psp.sap.model.PaymentTemplateQuarterPayment")]
		public var templateQuarterPayments:ArrayCollection;
    	public var pendingPaymentsTotal:Number;
    	public var paymentsMadeTotal:Number;
    	public var yearPaymentsTotal:Number;
    	public var paymentTemplateCd:String;
    	public var taxYear:String;
	}
}