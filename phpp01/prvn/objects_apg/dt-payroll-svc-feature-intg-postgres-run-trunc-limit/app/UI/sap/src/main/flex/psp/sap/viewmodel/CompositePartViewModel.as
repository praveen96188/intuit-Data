package psp.sap.viewmodel
{
    import flash.events.EventDispatcher;
    import flash.utils.Dictionary;
	
	import mx.events.PropertyChangeEvent;
	
	import psp.sap.application.collections.PartViewModelCollection;
	import psp.sap.application.enums.ViewModelActivationStateEnum;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	/**
	 * This class represents a part that has other parts and perhaps is
	 * logically a page.
	 */
	public class CompositePartViewModel extends AbstractPartViewModel
	{		
		//todo protected				
		public var partStrategyMap:Dictionary = new Dictionary(true);		
		
		//this should be set (ideally by the subclass) to set the default on its children
		public var subpartStrategy:String = PartAdditionStrategy.COMPOSITE;		
		
		public var defaultSinglePart:AbstractPartViewModel = null;
		public var maintainCrossTopicViewHistory:Boolean = false;							
		
		public function CompositePartViewModel(){
			this.addEventListener(ViewModelEvent.ACTIVATED,onThisActivated,false,0,true);
			this.addEventListener(ViewModelEvent.DEACTIVATED,onThisDeactivated,false,0,true);	
		}
		
		public function addNewPart(partClass:Class, label:String=null, strategy:String=null):AbstractPartViewModel {
			if (strategy == null) {
				strategy = subpartStrategy;
			}
			var part:AbstractPartViewModel = this.addNewChildPartViewModel(partClass,label);			
			partStrategyMap[part] = strategy;											
			
			return part;
			
		}				

		public function createNewPart(partClass:Class, label:String=null, strategy:String=null):AbstractPartViewModel {
			if (strategy == null) {
				strategy = subpartStrategy;
			}
			var part:AbstractPartViewModel = this.createNewChildPartViewModel(partClass,label);			
			partStrategyMap[part] = strategy;														
			return part;			
		}	
		
		//using an event instead of overriding this so subclasses do not have to call super
		private function onThisActivated(e:ViewModelEvent):void {
			propagateActivation();		
		}
		
		private function onThisDeactivated(e:ViewModelEvent):void {
			for (var key:Object in partStrategyMap) {
				var part:AbstractPartViewModel = AbstractPartViewModel(key);
				if (partStrategyMap[part] == PartAdditionStrategy.COMPOSITE || partStrategyMap[part] == PartAdditionStrategy.LAZY_COMPOSITE){
					part.deactivate();
				}															
			}
			
			if(activeSinglePart != null){
				activeSinglePart.deactivate();
			} 			
		}
		
		override protected function onRefresh():void {
			for (var key:Object in partStrategyMap) {
				var part:AbstractPartViewModel = AbstractPartViewModel(key);
				if (partStrategyMap[part] == PartAdditionStrategy.COMPOSITE || partStrategyMap[part] == PartAdditionStrategy.LAZY_COMPOSITE){
					part.refresh();
				}				
			}
			if (activeSinglePart != null) {
				activeSinglePart.refresh();
			}
		}
		
		public function activateSinglePart(partLabel:String = null):void {			
			if(partLabel != null){
				activeSinglePart = findPartByLabel(partLabel);
			}
			else{
				activeSinglePart = defaultSinglePart;
			}									 
		}
		
		private var mActiveSinglePart:AbstractPartViewModel = null;
		[Bindable]
		public function get activeSinglePart():AbstractPartViewModel {
			return mActiveSinglePart;
		}
		
		public function set activeSinglePart(value:AbstractPartViewModel):void {
			var oldActiveSinglePart:AbstractPartViewModel = activeSinglePart;
			
			if(oldActiveSinglePart != null){
				oldActiveSinglePart.deactivate();
			}
			
			mActiveSinglePart = value;
			
			if(mActiveSinglePart != null && mActiveSinglePart.activationState != ViewModelActivationStateEnum.ACTIVATED){
				mActiveSinglePart.activate();
			}
			
			dispatchActiveSinglePartIndexChangedEvent((oldActiveSinglePart != null) ? partViewModels.getItemIndex(oldActiveSinglePart) : -1,
												      activeSinglePartIndex);
												      
			dispatchActiveSinglePartLabelChangedEvent((oldActiveSinglePart != null) ? oldActiveSinglePart.label : null,
													  (activeSinglePart != null) ? activeSinglePart.label : null);
		}
		
		[Bindable ("propertyChange")]
		public function get activeSinglePartIndex():int {
			return partViewModels.getItemIndex(activeSinglePart);
		}
		
		[Bindable ("propertyChange")]
		public function get activeSinglePartLabel():String {
			return activeSinglePart.label;
		}

		private function dispatchActiveSinglePartIndexChangedEvent(oldValue:Object, newValue:Object):void {						
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "activeSinglePartIndex", oldValue, newValue));			
		}
		
		private function dispatchActiveSinglePartLabelChangedEvent(oldValue:Object, newValue:Object):void {						
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "activeSinglePartLabel", oldValue, newValue));			
		}

        private var subPartsToActivate:int=0;
		private function propagateActivation():void {
            if (subPartsToActivate > 0) {
                //don't activate sub-parts if they are currently being activated
                return;
            }
            for (var key:Object in partStrategyMap) {
				var part:AbstractPartViewModel = AbstractPartViewModel(key);
				if (partStrategyMap[part] == PartAdditionStrategy.COMPOSITE){
					if (! part.guardActivate) {
                        subPartsToActivate++;
                        part.addEventListener(ViewModelEvent.ACTIVATED, onSubPartActivated);
                        part.activate();
                    }

				}											
			} 	
			
			if(activeSinglePart == null && defaultSinglePart != null){					
				activeSinglePart = defaultSinglePart;					
			}
			else if(activeSinglePart != null){
                if (!activeSinglePart.guardActivate) {
				    subPartsToActivate++;
                    activeSinglePart.addEventListener(ViewModelEvent.ACTIVATED, onSubPartActivated);
                    activeSinglePart.activate();
                }
			}			
		}

        private function onSubPartActivated(event:ViewModelEvent):void {
            subPartsToActivate--;
            (event.target as EventDispatcher).removeEventListener(ViewModelEvent.ACTIVATED, onSubPartActivated);
            if (subPartsToActivate == 0) {
                onSubPartsActivated();
            }
        }

        protected function onSubPartsActivated():void {

        }
		
		//when the parts are configured dynamically and a temp
		//collection is assigned, we need to make the subparts active
		//if the old ones were
		override public function set partViewModels(value:PartViewModelCollection):void {
			super.partViewModels = value;
			if (this.activationState == ViewModelActivationStateEnum.ACTIVATED) {
				propagateActivation();
			}	
		}
		

		protected function getLabel(baseLabel:String, suffix:String):String {
			var label:String;
			if (baseLabel == null) {
				label = this.label + "_" + suffix;
			} else {
				label = baseLabel + "_" + suffix;
			}
			return label;			
		}
		
		//convenience functions
		//these are kind of like a palette of view models you can use for your pages
		//you don't have to use the palette, you can pick any color, but it's a lot easier to use the palette.
		//I am like the Bob Ross of parts.
		public function addExpander(baseLabel:String=null):ExpanderViewModel {
			var vm:ExpanderViewModel = this.addNewPart(ExpanderViewModel, getLabel(baseLabel, "expander")) as ExpanderViewModel;
            vm.preferenceKey="expand_on_activate_" + baseLabel;
            return vm;
		}				
		
		public function addDisplayEditHistory(baseLabel:String=null):DisplayEditHistoryViewModel {
			return this.addNewPart(DisplayEditHistoryViewModel, getLabel(baseLabel, "deh")) as DisplayEditHistoryViewModel;				
		}
		
		public function addPopUpPart(baseLabel:String=null):PopUpPartViewModel {
			return this.addNewPart(PopUpPartViewModel, getLabel(baseLabel, "pup")) as PopUpPartViewModel;
		}
		
		public function addPartsTabNavigator(baseLabel:String=null):PartsTabNavigatorViewModel {
			return this.addNewPart(PartsTabNavigatorViewModel, getLabel(baseLabel, "ptn")) as PartsTabNavigatorViewModel;
		}
	}
}