package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentMethodNote")]
	public class PaymentMethodNote
	{
		public var notes: String;
        public var createdDate: Date;

	}
}