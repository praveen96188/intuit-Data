package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventQueryReturn")]
	public class CompanyEventQueryReturn
	{

		[ArrayElementType("psp.sap.model.CompanyEventItem")]
		public var events:ArrayCollection;

        public var moreEventsExistForQuery:Boolean;
    	

	}
}