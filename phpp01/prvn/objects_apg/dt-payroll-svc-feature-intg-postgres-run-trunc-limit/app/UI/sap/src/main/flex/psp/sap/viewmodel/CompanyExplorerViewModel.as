package psp.sap.viewmodel
{
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	
	import psp.sap.application.IApplicationItem;
    import psp.sap.application.SAP;
	import psp.sap.application.enums.ExplorerEnum;
	import psp.sap.application.enums.OperationsEnum;
	import psp.sap.model.Company;

	public class CompanyExplorerViewModel 
		extends AbstractExplorer
	{	
		public function CompanyExplorerViewModel() {
			super(ExplorerEnum.COMPANY, ExplorerEnum.COMPANY, false);
			inspectors.addEventListener(CollectionEvent.COLLECTION_CHANGE, onInspectorsCollectionChanged, false, int.MIN_VALUE, true);
			enabled = permissionGranted() && inspectors.length > 0;
		}
		
		override protected function createInspector(applicationItem:IApplicationItem):AbstractInspectorViewModel {
			var inspector:CompanyInspectorViewModel = new CompanyInspectorViewModel(this);			
            inspector.initialize(applicationItem as Company);
			return inspector;
		}
		
		override protected function addInspector(inspector:AbstractInspectorViewModel):void {
			if(inspectors.length == SAP.MAX_OPEN_COMPANIES){
				inspector.enabled = false;
			}
			super.addInspector(inspector);
		}				
		
		private function onInspectorsCollectionChanged(e:CollectionEvent):void {
			enabled = permissionGranted() && inspectors.length > 0;
			
			if (e.kind == CollectionEventKind.REMOVE){
				if(inspectors.length == SAP.MAX_OPEN_COMPANIES){
					var lastInspector:AbstractInspectorViewModel = inspectors.getItemAt(inspectors.length - 1) as AbstractInspectorViewModel;
					lastInspector.enabled = true;
					activeInspector = lastInspector;
				}
			}			
		}
				
		override public function deactivate():void {
			activeInspector = null;
			dispatchActiveInspectorIndexChangedEvent();
		}
		
		override public function permissionGranted():Boolean {
			return SAP.canPerformOperation(OperationsEnum.ACCESS_APPLICATION);
		}		
	}
	
	
}
