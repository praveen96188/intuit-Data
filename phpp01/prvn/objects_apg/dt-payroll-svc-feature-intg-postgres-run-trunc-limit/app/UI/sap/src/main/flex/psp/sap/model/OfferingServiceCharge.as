package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOfferingServiceCharge")]
	public class OfferingServiceCharge
	{
		public var SKU:String;
	    public var tier:Boolean;
	    public var tierNumber:int;
	    public var tierUnits:int;
	    public var SKUType:String;
	    public var groupDescription:String;
	    public var groupAppliesTo:String;
	    public var price:Number;
        public var unitPrice:Number;
	}
}