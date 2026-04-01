package psp.sap.viewmodel
{
	import psp.sap.application.enums.TestToolsExplorerEnum;
	
	/**
	 * This explorer is used only by operations personnel.
	 * 
	 * The explorer is not availabe for selection by users; operations personnel are
	 * auto-navigated to this explorer on login.  See SAP.initialize()
	 */
	public class TestToolsExplorerViewModel
		extends AbstractExplorer
	{
		private var mTestToolsInspector:TestToolsInspectorViewModel;		
		public function TestToolsExplorerViewModel()
		{
			super(TestToolsExplorerEnum.TEST_TOOLS, TestToolsExplorerEnum.TEST_TOOLS, true );
			
			mTestToolsInspector = new TestToolsInspectorViewModel();
			inspectors.addItem(mTestToolsInspector);
			defaultInspector = mTestToolsInspector;
			mActiveInspector = mTestToolsInspector;
		}

		override public function set activeInspector(value:AbstractInspectorViewModel):void {
			return;
		}
		
		override public function deactivate():void
		{		
			mTestToolsInspector.deactivate();
		}
		
		override public function activate(inspectorToActivate:AbstractInspectorViewModel=null):void {
			if (inspectorToActivate == null)
				inspectorToActivate = mTestToolsInspector;
			
			super.activate(inspectorToActivate);		
			
			inspectorToActivate.activeTopic = inspectorToActivate.defaultTopic;
			inspectorToActivate.activeTopic.activePage = inspectorToActivate.activeTopic.defaultPage;
		}
		
		override public function get showInMenu():Boolean {
			return permissionGranted(); 
		}
		
		override public function permissionGranted():Boolean {
			return true;
		}
		
	}
}