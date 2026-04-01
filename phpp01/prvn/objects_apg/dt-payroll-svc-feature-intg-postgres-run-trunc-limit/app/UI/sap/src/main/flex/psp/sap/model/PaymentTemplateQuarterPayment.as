package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentTemplateQuarterPayment")]
	public class PaymentTemplateQuarterPayment
	{
		public var quarter:String;
		public var year:String;
    	public var paymentTemplateName:String;
    	public var paymentTemplateCd:String;
	    public var pendingPaymentsTotal:Number;
	    public var paymentsMadeTotal:Number;
	    public var quarterPaymentsTotal:Number;
	    [ArrayElementType("psp.sap.model.Payment")]
	    public var pendingPayments:ArrayCollection;
	    [ArrayElementType("psp.sap.model.Payment")]
	    public var paymentsMade:ArrayCollection;
	    public var notStarted:Boolean;
	}
}