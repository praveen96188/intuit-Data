package psp.sap.model
{
	import intuit.sbd.flex.framework.model.EntityObject;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUserOperation")]
	public class UserOperation extends EntityObject
	{
		public var name: String;
		public var description: String;
		public var operationId: String;
		public var  domainId: String;
		
		[Transient]
		public var selected:Boolean = false;
	}
}
