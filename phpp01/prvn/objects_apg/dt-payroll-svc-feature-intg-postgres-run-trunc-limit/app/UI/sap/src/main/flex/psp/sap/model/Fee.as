package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFee")]	
	public class Fee
	{
		public var name:String;
		public var description:String;
		public var feeCd:String;
		public var amount:Number;
		
		public function Fee()
		{
		}

	}
}