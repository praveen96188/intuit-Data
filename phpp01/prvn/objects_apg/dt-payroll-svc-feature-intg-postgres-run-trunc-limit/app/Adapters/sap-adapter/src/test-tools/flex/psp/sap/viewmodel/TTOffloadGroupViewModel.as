package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	import mx.validators.StringValidator;
	
	import psp.sap.validators.SAPValidators;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import testTools.model.OffloadBatch;
	import testTools.model.OffloadGroup;
	import testTools.service.TestService;
		
	public class TTOffloadGroupViewModel extends CompositePartViewModel
	{
		public function TTOffloadGroupViewModel () {
			super();
			
			nameValidator = SAPValidators.createStringValidator(this, "groupName", true);
			validators.push(nameValidator);

			descriptionValidator = SAPValidators.createStringValidator(this, "groupDescription", true);
			validators.push(descriptionValidator);

			timeValidator = SAPValidators.createStringValidator(this, "cuttoffTime", true);
			validators.push(descriptionValidator);
			
			codeValidator = SAPValidators.createStringValidator(this, "groupCode", true);
			validators.push(codeValidator);
			
			this.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, refreshData);
		}
		
		[Bindable]
		public var modifyBoxTitle:String = "Modify offload group";
		
		[Bindable]
		public var modifyMode:Boolean = true;
		
		[Bindable]
		public var editMode:Boolean = false;
		
		[Bindable]
		public var offloadGroupList:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var offloadBatchList:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public function get groupName():String {
			return mGroupName;
		}
		
		public function set groupName(value:String):void {
			mGroupName = value;
			updateCanSave();
		}
		
		private var mGroupName:String;
		
		[Bindable]
		public function get groupDescription():String {
			return mGroupDescription;
		}
		
		public function set groupDescription(value:String):void {
			mGroupDescription = value;
			updateCanSave();
		}
		
		private var mGroupDescription:String;
		
		[Bindable]
		public function get groupCode():String {
			return mGroupCode;
		}
		
		public function set groupCode(value:String):void {
			mGroupCode = value;	
		}
		
		private var mGroupCode:String;
		
		[Bindable]
		public function get cutoffTime():String {
			return mCutoffTime;
		}
		
		public function set cutoffTime(value:String):void {
			mCutoffTime = value;
			updateCanSave();
		}
		
		private var mCutoffTime:String;
		
		[Bindable]
		public var nameValidator:StringValidator;
		
		[Bindable]
		public var descriptionValidator:StringValidator;
		
		[Bindable]
		public var timeValidator:StringValidator;
		
		[Bindable]
		public var codeValidator:StringValidator;
		
		private var mOldOffloadGroup:OffloadGroup = null;
		
		override protected function loadModelData():void{
			TestService.instance.findOffloadGroups(createLoadModelDataResponder(onLoadSucceeded));
		}
		
		public function loadGroupForEdit(group:OffloadGroup):void {
			mOldOffloadGroup = group;
			
			groupName = group.groupName;
			groupDescription = group.groupDescription;
			groupCode = group.groupCode;
			cutoffTime = group.cutoffTime;
		}

        protected function onLoadSucceeded(e:ResultEvent):void{
        	offloadGroupList = e.result as ArrayCollection;
        	
        	mOldOffloadGroup = null;
        	this.groupCode = "";
        	this.groupDescription = "";
        	this.groupName = "";
        	this.cutoffTime = "";	
        	
        	TestService.instance.findOffloadBatches(createLoadModelDataResponder(onBatchesLoadSucceeded));
        }
        
		protected function onBatchesLoadSucceeded(e:ResultEvent):void {
			offloadBatchList = e.result as ArrayCollection;
			if (offloadBatchList.length > 0) {
				var batch:OffloadBatch = offloadBatchList.getItemAt(0) as OffloadBatch;
			}
            modelDataLoaded();
        }
        
        override protected function executeSave():void {
        	if (modifyMode) {
	        	TestService.instance.saveOffloadGroup(
	        		groupCode,
	        		groupName,
	        		groupDescription,
	        		cutoffTime,        	
	                createSaveResponder());
        	} else {
	        	TestService.instance.addOffloadGroup(
	        		groupCode,
	        		groupName,
	        		groupDescription,
	        		cutoffTime,        	
	                createSaveResponder());
        	}
        }
        
        public function executeOffloadOnGroup(offloadGrp:String):void {
        	TestService.instance.generateNACHAFiles(offloadGrp, createSaveResponder());
        }
        
        private function refreshData(e:ViewModelEvent):void {
        	loadModelData();
        }
        
        override public function get hasChanged():Boolean {
        	if (modifyMode) {
        		if (mOldOffloadGroup != null) {
		        	return (groupName != mOldOffloadGroup.groupName) || (groupDescription != mOldOffloadGroup.groupDescription)
		        		|| (cutoffTime != mOldOffloadGroup.cutoffTime);
		        } else return true;
        	} else {
        		return true;
        	}
        }

	}
}