package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventGroupItem")]
	public class CompanyEventGroupItem
	{
		public var eventTypeName:String;
		public var eventTypeCd:String;
		
		[Transient]
		public var checked:Boolean = false;

		public function toString(): String {
			return eventTypeName;
		}

	}
}