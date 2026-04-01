package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventGroup")]
	public class CompanyEventGroup
	{
		public var name:String;
		public var eventGroupCode:String;
		
		[ArrayElementType("psp.sap.model.CompanyEventGroupItem")]
		public var children:ArrayCollection;
		
		[Transient]
		public var checked:Boolean = false;
		
	}
}