package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPServiceStatus")]
	public class ServiceStatus
	{
		public var serviceStatusCd: String;
		public var serviceStatusName: String;		
		public var serviceStatusDescription: String;
		
		[ArrayElementType("psp.sap.model.ServiceSubStatus")]
		public var serviceSubStatusList: ArrayCollection;						
	}
}