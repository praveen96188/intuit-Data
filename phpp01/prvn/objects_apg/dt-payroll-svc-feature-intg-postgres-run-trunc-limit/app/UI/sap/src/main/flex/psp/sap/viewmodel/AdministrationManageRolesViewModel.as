package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.Responder;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.AdministrationInspectorPageEnum;
	import psp.sap.model.UserRole;
	
	public class AdministrationManageRolesViewModel
	extends AbstractPartViewModel
	{		
		protected static const DEFAULT_mRolesCollection:ArrayCollection = new ArrayCollection();
		protected static const DEFAULT_mOperationsCollection:ArrayCollection = new ArrayCollection();				
		
		private var mRolesCollection:ArrayCollection = DEFAULT_mRolesCollection;
		private var mOperationsCollection:ArrayCollection = DEFAULT_mOperationsCollection;						

		[Bindable]
		public function get roles():ArrayCollection {
			return mRolesCollection;
		}
		
		public function set roles(value:ArrayCollection):void {
			mRolesCollection = value;
		}

		[Bindable]
		public function get operations():ArrayCollection {
			return mOperationsCollection;
		}
		
		public function set operations(value:ArrayCollection): void {
			mOperationsCollection = value;
		}	

		override protected function loadModelData():void {					
			SAP.instance.userService.getAllOperations(
				new Responder(operationsResultHandler, onLoadModelDataFaulted));			
		}
		
		public function operationsResultHandler(event:ResultEvent):void {
			operations = event.result as ArrayCollection;
			
			var operationsSort:Sort = new Sort();
		    operationsSort.fields = [new SortField("name", false, false)];
		    operations.sort = operationsSort;
		    operations.refresh();				
			
			SAP.instance.userService.getAllRoleObjects(
				createLoadModelDataResponder(rolesResultHandler));
		}
		
		public function rolesResultHandler(event:ResultEvent):void {
			var temp:ArrayCollection = event.result as ArrayCollection; 			
			
			var rolesSort:Sort = new Sort();
		    rolesSort.fields = [new SortField("name", false, false)];
		    temp.sort = rolesSort;	
		    temp.refresh();
		    roles = temp;									
		}
		
		/** these two functions are the exact same but I could not 
		 *	come up with a better name for one function
		 */
		public function expandAll():void {				
			for each(var role:UserRole in roles){
				role.selected = true;
			}			
		}
			
		public function collapseAll():void {				
			for each(var role:UserRole in roles){
				role.selected = false;
			}				
		}
		
	}
}