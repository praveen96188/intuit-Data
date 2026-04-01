package psp.sap.viewmodel
{
    import flash.events.Event;

    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.viewmodel.events.ViewModelEvent;

	public class PopUpPartViewModel extends CompositePartViewModel
	{
	
		
		[Bindable]
		public var closeOnSave:Boolean = false;
				
						
		[Bindable]
		public var modalViewModel:AbstractPartViewModel=null;
		
		public function PopUpPartViewModel() {
			super();
			this.subpartStrategy = PartAdditionStrategy.LAZY_COMPOSITE;
		}
		
		public function displayPopUp():void {
			modalViewModel.addEventListener(ViewModelEvent.CLOSE,onModalClosed,false,0,true);	
			if (closeOnSave) {
				modalViewModel.addEventListener(ViewModelEvent.SAVE_SUCCEEDED,onModalClosed,false,0,true);
			}
			if (modalViewModel.activationState != ViewModelActivationStateEnum.ACTIVATED) {
                modalViewModel.activate();
            } else {
                modalViewModel.refresh();
            }


			dispatchEvent(new Event("displayPopUp"));			
		}
		
		public function hidePopUp():void {													
			modalViewModel.deactivate();
			
			dispatchEvent(new Event("hidePopUp"));
		}
		
		
		//when the modal part has determined to close it (e.g. user clicks cancel from modal part)
		protected function onModalClosed(event:ViewModelEvent):void {
			hidePopUp();
		}
		
		override public function addNewPart(partClass:Class, label:String=null, strategy:String="manual"):AbstractPartViewModel {
			var partVm:AbstractPartViewModel = super.addNewPart(partClass, label, strategy);
			modalViewModel = partVm;
			return partVm;
		}
		
		override protected function onDeactivated():void {
			hidePopUp();
		}

        public static function refreshIfSaved(part:AbstractPartViewModel, host:AbstractPartViewModel):void {
            if (part.hasSaved) {
                host.refresh();
            }
        }

	}
}