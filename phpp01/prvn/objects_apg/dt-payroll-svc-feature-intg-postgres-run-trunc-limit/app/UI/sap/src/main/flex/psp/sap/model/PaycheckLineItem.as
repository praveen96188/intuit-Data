package psp.sap.model
{
    import mx.collections.ArrayCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaycheckLineItem")]
	public class PaycheckLineItem
	{
		public var lineItemGseq:String;
		public var payrollItemCategory:String;
    	public var payrollItemType:String;
    	public var sourcePayrollItemName:String;
        public var amount:Number;

	}
}