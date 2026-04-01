package psp.sap.model
{
	import intuit.sbd.flex.framework.model.EntityObject;
	
	import mx.collections.ArrayCollection;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUserRole")]
	public class UserRole extends EntityObject
	{
		private var mSelected:Boolean;
		
		public var name: String;
		public var description: String;
		public var roleId: String;
		public var systemId: String;
		
		[ArrayElementType("psp.sap.model.UserOperation")]
		public var operations: ArrayCollection = new ArrayCollection();
		
		[Transient]
		public function containsOperation(userOperationId:String):Boolean {
			for each (var userOperation:UserOperation in this.operations) {
				if(userOperation.operationId == userOperationId) {
					return true;
				}
			}
			return false;
		}
		
		[Transient]
		public function get selected():Boolean {
			return mSelected;
		}
		
		public function set selected(value:Boolean):void {
			mSelected = value;
		}
	}
}