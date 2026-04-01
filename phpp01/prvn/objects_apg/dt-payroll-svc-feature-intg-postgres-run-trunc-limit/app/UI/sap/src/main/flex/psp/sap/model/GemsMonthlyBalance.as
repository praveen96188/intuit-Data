package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPGemsMonthlyBalance")]
	public class GemsMonthlyBalance
	{
		public var account:String;
	    public var company:String;
	    public var department:String;
	    public var groupCode:String;
	    public var interCompany:String;
	    public var reportedBalance:Number;
	    public var uploadStatus:String;
	}
}