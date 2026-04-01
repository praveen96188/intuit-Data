package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOffering")]
	public class Offering
	{
		public var SKU:String;
	    public var name:String;
	    public var description:String;
	    
	    [ArrayElementType("psp.sap.model.OfferingServiceCharge")]
	    public var serviceCharges:ArrayCollection;
	    
	    [Transient]
	    public var selected:Boolean = false;
	}
}