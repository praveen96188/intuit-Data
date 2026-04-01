package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.enums.TestToolsPageEnum;
	
	import testTools.model.OffloadGroup;
	import testTools.service.TestService;
	
	public class TTCompanyChangeOffloadGroupViewModel
		extends AbstractPartViewModel
	{
		private var mOffloadGroupList:ArrayCollection = null;
		private var mCurrentOffloadGroup:OffloadGroup = null;
		
		public function TTCompanyChangeOffloadGroupViewModel()
		{
			this.label = TestToolsPageEnum.COMPANY_CHANGE_OFFLOAD_GROUP; 			
		}
		
		[Bindable]
		public function get selectedOffloadGroupIndex():Number {
			for (var i:int = 0; i < mOffloadGroupList.length; i++) {
				if (mOffloadGroupList.getItemAt(i) == mCurrentOffloadGroup) {
					return i;
				}
			}
			return -1;
		}
		
		public function set selectedOffloadGroupIndex(value:Number):void {
			currentOffloadGroup = mOffloadGroupList.getItemAt(value) as OffloadGroup;
		}

		[Bindable]
		public function get currentOffloadGroup():OffloadGroup {
			return mCurrentOffloadGroup;
		}
		
		public function set currentOffloadGroup(value:OffloadGroup):void {			
			mCurrentOffloadGroup = value;
			updateCanSave();
		}
			
		[Bindable]	
		public function get offloadGroupList():ArrayCollection {
			return mOffloadGroupList;
		}
		
		public function set offloadGroupList(value:ArrayCollection):void {
			mOffloadGroupList = value;
		}
		
		override protected function loadModelData():void {
			TestService.instance.findOffloadGroups(createLoadModelDataResponder(onOffloadGroupsResult));
		}
		
		private function onOffloadGroupsResult(e:ResultEvent):void {
			offloadGroupList = e.result as ArrayCollection;
			modelDataLoaded();
			initializeBackingProperties();
		}
	
		override protected function evaluateCanSave():Boolean {
			return (company != null) && super.evaluateCanSave();
		}
		
		override public function get hasChanged():Boolean {	
			return (currentOffloadGroup != null);
		}		
		
		override protected function initializeBackingProperties():void {
			if (company != null) {
				for (var i:int = 0; i < offloadGroupList.length; i++) {
					if (OffloadGroup(offloadGroupList.getItemAt(i)).groupCode == company.offloadGrp) {
						selectedOffloadGroupIndex = i;
					}
				}
			}	
			else {
				currentOffloadGroup = null;
			}
		}
		
		protected function writeModelValues():void {
			company.offloadGrp = currentOffloadGroup.groupCode;
		}
		
		override protected function executeSave():void {
			writeModelValues();
			TestService.instance.changeCompanyOffloadGroup(
					company.sourceSystemCd, 
					company.companyId, 
					currentOffloadGroup.groupCode, 
					createSaveResponder());
		}			
	}
}