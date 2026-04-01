package psp.sap.viewmodel
{
    import psp.sap.application.enums.PartsEnum;
    import psp.sap.application.events.UniversalSearchEvent;
    import psp.sap.view.controls.UniversalSearchFieldViewModel;

    public class UniversalSearchViewModel extends CompositePartViewModel
	{
		
		private var resultsPup:PopUpPartViewModel;
		
		[Bindable]
		public var resultsViewModel:UniversalSearchAdvancedViewModel;

        [Bindable]
        public var searchFieldViewModel:UniversalSearchFieldViewModel;
				
		public function UniversalSearchViewModel()
		{
			resultsPup = addPopUpPart(PartsEnum.ADVANCED_SEARCH); 
			
			resultsViewModel = 
				resultsPup.addNewPart(UniversalSearchAdvancedViewModel,PartsEnum.ADVANCED_SEARCH)
				as UniversalSearchAdvancedViewModel;

            searchFieldViewModel = new UniversalSearchFieldViewModel();
            searchFieldViewModel.addEventListener(UniversalSearchEvent.UNIVERSAL_SEARCH,onSearch, false, 0, true);
				
		}
		
		private function onSearch(event:UniversalSearchEvent):void {
			//set the parameters on the results/advanced search
			resultsViewModel.searchFieldViewModel.searchText = event.searchValue;
			resultsViewModel.searchFieldViewModel.searchType = event.searchType;
			resultsViewModel.doSearch = true;
			resultsViewModel.clearResults = false;
			
			resultsPup.displayPopUp();
		}

        override protected function onActivated():void {
            searchFieldViewModel.updateSearchMenuOptions();
        }

        public function displayResults():void {
			resultsViewModel.doSearch = false;
			resultsViewModel.clearResults = false;			
			resultsPup.displayPopUp();
		}

        public function initialSearch():void {
            resultsViewModel.doSearch = false;
			resultsViewModel.clearResults = true;
			resultsPup.displayPopUp();
        }

	}
}