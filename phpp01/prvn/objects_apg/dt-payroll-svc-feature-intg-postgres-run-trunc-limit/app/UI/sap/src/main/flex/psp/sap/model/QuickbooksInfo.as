package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuickbooksInfo")]
	public class QuickbooksInfo
	{
		public var licenseNumber:String;
	    public var applicationVersion:String;
	    public var taxTable:String;
	    public var coaFeeAccountName:String;
	    public var coaSalesTaxAccountName:String;
        public var processTransmissions:Boolean;
        public var allowTransmissions:Boolean;
        public var fileId:String;
	}
}