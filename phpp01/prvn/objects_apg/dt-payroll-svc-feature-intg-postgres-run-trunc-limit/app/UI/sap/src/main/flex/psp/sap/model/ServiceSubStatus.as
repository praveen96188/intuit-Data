package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPServiceSubStatus")]
	public class ServiceSubStatus
	{		
		public var subStatusName: String;
		public var subStatusDescription: String;			
		public var subStatusCd: String;
		public var subStatusType:String;
		public var manuallyUpdatable:Boolean;
		
		private var mSelected:Boolean = false;
		
		[Transient]
		public function get selected():Boolean {
			return mSelected;
		}
		public function set selected(value:Boolean):void {
			mSelected = value;
		}

	}
}