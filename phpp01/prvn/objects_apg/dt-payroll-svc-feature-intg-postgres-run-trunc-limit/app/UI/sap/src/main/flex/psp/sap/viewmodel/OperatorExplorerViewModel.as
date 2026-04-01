package psp.sap.viewmodel
{
	import psp.sap.application.SAP;
	import psp.sap.application.enums.ExplorerEnum;
	import psp.sap.application.enums.OperationsEnum;
	
	/**
	 * This explorer is used only by operations personnel.
	 * 
	 * The explorer is not availabe for selection by users; operations personnel are
	 * auto-navigated to this explorer on login.  See SAP.initialize()
	 */
	public class OperatorExplorerViewModel
	extends AbstractExplorer
	{
		public function OperatorExplorerViewModel()
		{
			super(ExplorerEnum.OPERATOR, ExplorerEnum.OPERATOR, true );
			
			var mOperatorInspector:OperatorInspectorViewModel = new OperatorInspectorViewModel(this);
			inspectors.addItem(mOperatorInspector);
						
		}
		
		override public function permissionGranted():Boolean {
			return SAP.canPerformOperation(OperationsEnum.VIEW_OPERATOR_TAB);
		}
		
	}
}
