package psp.sap.viewmodel
{
	import flash.events.Event;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	
	import psp.sap.application.enums.FraudInspectorPageEnum;
	
	public class FraudViewModel extends CompositePartViewModel
	{					
		private var mFraudSearchViewModel:FraudSearchViewModel;
		private var mFraudDetailViewModel:FraudDetailViewModel;
		
		private var mSearchResults:ArrayCollection = new ArrayCollection();
				
		private var tabNav:PartsTabNavigatorViewModel;
				
		public function FraudViewModel()
		{
			super();
			
			this.label = FraudInspectorPageEnum.FRAUD_VIEW;
			
			tabNav = addPartsTabNavigator(FraudInspectorPageEnum.FRAUD_SEARCH);
			
			mFraudSearchViewModel = 
				tabNav.addNewPart(FraudSearchViewModel, FraudInspectorPageEnum.FRAUD_SEARCH) as FraudSearchViewModel;
				
			mFraudSearchViewModel.addEventListener(FraudSearchViewModel.FRAUD_FLAG_REMOVED_EVENT, onFraudFlagRemoved, false, 0, true);
			
			mFraudDetailViewModel = 
				tabNav.addNewPart(FraudDetailViewModel, FraudInspectorPageEnum.FRAUD_DETAIL) as FraudDetailViewModel;
		
			

			BindingUtils.bindProperty(mFraudDetailViewModel, "fraudSearchResults", mFraudSearchViewModel, "searchResults");
			BindingUtils.bindProperty(mFraudDetailViewModel, "fraudEvent", mFraudSearchViewModel, "selectedFraudEvent");
			
			mFraudDetailViewModel.addEventListener(FraudDetailViewModel.FRAUD_FLAG_REMOVED_EVENT, onFraudFlagRemoved, false, 0, true);
			mFraudDetailViewModel.addEventListener(FraudDetailViewModel.ALL_FRAUD_FLAGS_REMOVED_EVENT, onAllFraudFlagsRemoved, false, 0, true);			
			
			tabNav.defaultSinglePart = mFraudSearchViewModel;
		}
		
		
		private function onFraudFlagRemoved(e:Event):void {
			mFraudSearchViewModel.searchResults.dispatchEvent(new CollectionEvent(CollectionEventKind.UPDATE));						
		}
		
		private function onAllFraudFlagsRemoved(e:Event):void {
			tabNav.activateSinglePart(FraudInspectorPageEnum.FRAUD_SEARCH);
		}

		public function goToDetails():void {
			tabNav.activateSinglePart(FraudInspectorPageEnum.FRAUD_DETAIL);
		}
		
	}
}