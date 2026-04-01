package psp.sap.viewmodel
{
	import flash.events.Event;
	
	import psp.sap.application.enums.ViewModelActivationStateEnum;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	public class DisplayEditHistoryViewModel extends CompositePartViewModel
	{
		
			
		private var mDisplayViewModel:AbstractPartViewModel=null;
		
		private var mEditViewModel:AbstractPartViewModel=null;
		
		[Bindable]
		public var historyViewModel:AbstractPartViewModel=null;		

		[Bindable]
		public var currentState:String;
		
		[Bindable]
		public var skipEdit:Boolean = false;

		//todo
		[Bindable]
		public var isEditingAnySubPart:Boolean = false;   
		
		public function DisplayEditHistoryViewModel() {
			super();
			this.subpartStrategy = PartAdditionStrategy.LAZY_COMPOSITE;
		}

		[Bindable]
		public function get editViewModel():AbstractPartViewModel {
			return mEditViewModel;
		}
		
		public function set editViewModel(value:AbstractPartViewModel):void {
			mEditViewModel = value;
			
			if (value != null) {
				value.addEventListener(ViewModelEvent.CLOSE,onEditClose,false,0,true);
				value.addEventListener(ViewModelEvent.SAVE_SUCCEEDED,onSaveSucceeded,false,0,true);
			}
		}
		
		[Bindable]
		public function get displayViewModel():AbstractPartViewModel {
			return mDisplayViewModel;
		}
		
		public function set displayViewModel(value:AbstractPartViewModel):void {
			mDisplayViewModel = value;
						
			if (value != null) {
				value.addEventListener(ViewModelEvent.SAVE_SUCCEEDED,onDisplaySaveSucceeded,false,0,true);	
			}
		}
		

		[Bindable]
		public var displayActive:Boolean = true;
				
		public function get historyActive():Boolean {
			return currentState == "historyOpen";
		} 
		
		private function onEditClose(e:Event):void {
			display();
			isEditingAnySubPart = false;
		}
		
		private function onSaveSucceeded(e:Event):void {
			display();
			isEditingAnySubPart = false;
			
			//we also want to refresh the history since it has probably changed now
			if (historyActive) {							
				historyViewModel.refresh();				
			}
		}
		
		
		private function onDisplaySaveSucceeded(e:Event):void {
			if (historyActive) {							
				historyViewModel.refresh();
				
			}			
		}
		
		
		override protected function onActivated():void {							
			if (displayActive) {
				if (displayViewModel != null && displayViewModel.activationState == ViewModelActivationStateEnum.NEW) {
					displayViewModel.activate();
				} else {
					displayViewModel.refresh();	
				}				
			} else {
				editViewModel.refresh();
			}
			
			if (historyActive) {
				historyViewModel.refresh();
			}
		}
		
		private function activateDisplay():void {
			if (displayViewModel != null) {
				displayViewModel.activate();
			}
			displayActive = true;
		}
		
		private function activateEdit():void {
			if (editViewModel != null) {
				editViewModel.activate();
			}						
		}
		
		private function deactivateDisplay():void {
			if (displayViewModel != null) {
				displayViewModel.deactivate();
			}
			displayActive = false;			
		}
		
		private function deactivateEdit():void {
			if (editViewModel != null) {
				editViewModel.deactivate();
			}						
		}
		
		public function display():void {
			if (host is ExpanderViewModel) {
                ExpanderViewModel(host).canCollapse = true;
            }
            if (! displayActive){
				deactivateEdit();
			}
			activateDisplay();						
		}
		
		public function edit():void {
			if (host is ExpanderViewModel) {
                ExpanderViewModel(host).canCollapse = false;
            }
            if (displayActive) {
				deactivateDisplay();
			}
			activateEdit();
			isEditingAnySubPart = true;
		}
		
		
		private var mDisplayEditSelectedIndex:int;
		
		[Bindable]
		public function get displayEditSelectedIndex():int {
			return mDisplayEditSelectedIndex;
		}
		
		public function set displayEditSelectedIndex(value:int):void{
			mDisplayEditSelectedIndex = value;
			if (value == 0) {
				display();
			} else if (value == 1) {
				edit();
			} //otherwise -1 because flex sucks						
		}
		
		
		//todo sugar
		private var clientPartCount:int = 0;
		override public function addNewPart(partClass:Class, label:String=null, strategy:String="manual"):AbstractPartViewModel {
			var part:AbstractPartViewModel = super.addNewPart(partClass, label, strategy);			
			if (clientPartCount == 0) {
				displayViewModel = part;
			} else if (clientPartCount == 1 && !skipEdit) {
				editViewModel = part;
			} else {
				historyViewModel = part;
			}
			clientPartCount++;
			return part;
		}
		
		public function openHistory():void {
			currentState = "historyOpen";
			
			if (historyViewModel != null) {
				historyViewModel.activate();
			}	

		}
		
		public function closeHistory():void {
			currentState = "historyClosed";
			
			if (historyViewModel != null && historyActive) {
				historyViewModel.deactivate();		
			}		
			
		}   
		
		public function toggleHistory():void {
			currentState == "historyOpen" ? closeHistory() : openHistory();
		}
				
		
		//convenience
		public function addDisplay(displayClass:Class, baseLabel:String=null):AbstractPartViewModel {
			return this.addNewPart(displayClass, getLabel(baseLabel, "display"), PartAdditionStrategy.LAZY_COMPOSITE);
		}	
		
		public function addEdit(editClass:Class, baseLabel:String=null):AbstractPartViewModel {
			return this.addNewPart(editClass, getLabel(baseLabel, "edit"), PartAdditionStrategy.LAZY_COMPOSITE);
		}	
		
		public function addHistory(historyClass:Class, baseLabel:String=null):AbstractPartViewModel {
			if (this.editViewModel == null) {
				this.skipEdit = true;
			}
			return this.addNewPart(historyClass, getLabel(baseLabel, "history"), PartAdditionStrategy.LAZY_COMPOSITE);
		}	
		
		

	}
}