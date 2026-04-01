package psp.sap.view.controls
{
    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;
    import mx.controls.Menu;
    import mx.events.MenuEvent;
    import mx.events.PropertyChangeEvent;

    import psp.sap.application.SAP;

    import psp.sap.application.enums.CompanySearchEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.events.UniversalSearchEvent;

    [Event(name="universalSearch", type="psp.sap.application.events.UniversalSearchEvent")]
	public class UniversalSearchFieldViewModel extends EventDispatcher
	{

		[Bindable]
		public var searchTypeMenu:Menu;

		public const DEFAULT_SEARCH_INDEX:int = 0;
		
		
		private var mSearchText:String;
		
		[Bindable]
		public var searchButtonLabel:String;
		
		[Bindable]
		public var canSearch:Boolean=false;

        [Bindable]
        public var searchMenuOptions:ArrayCollection = new ArrayCollection();

        private const vmpEmpSearchSSN:Object = {label: "VMP Employee SSN"};
        private const vmpEmpSearchEmail:Object = {label: "VMP Employee Email"};
        private const vmpEmpSearchCFR:Object = {label: "VMP Employee CFR"};
        // private const vmpEmpSearchName:Object = {label: "VMP Employee Name"};
		
		public function UniversalSearchFieldViewModel()
		{			
			searchTypeMenu = new Menu();
            searchMenuOptions.addItem({label: "Smart Search"});
            searchMenuOptions.addItem({label: "EIN Search"});
            searchMenuOptions.addItem({label: "PSID Search"});
            searchMenuOptions.addItem({label: "Name Search"});
            searchMenuOptions.addItem({label: "License Number Search"});
            searchMenuOptions.addItem({label: "CAN Search"});
            searchMenuOptions.addItem({label: "Service Key Search"});
            searchMenuOptions.addItem({label: "Registration Number Search"});
            searchMenuOptions.addItem({label: "RealmId Search"});
            searchTypeMenu.addEventListener(MenuEvent.ITEM_CLICK, onItemClick,false,0,true);			

			searchTypeMenu.dataProvider = searchMenuOptions;
			searchTypeMenu.selectedIndex = DEFAULT_SEARCH_INDEX;			
			searchButtonLabel = searchTypeMenu.dataProvider[DEFAULT_SEARCH_INDEX].label;
		}
				
		private function set searchTypeSelectedIndex(value:int):void {
			//Oh sure, you ask, "why don't you do this in an event and not require"
			//the programmer to call this method every time and have it very confusing and everything.
			//Yes, a valid point, and one that has a good answer: flex sucks.
			//viz. there are 6 events for monitoring that a USER set the index, and 0 for monitoring
			//that the programmer did.
			//But you didn't try the List events!  Yes I did.  They don't work because the Menu extends List
			//but doesn't use the collection property which means it won't fire the event			
			//Stupid.
			searchTypeMenu.selectedIndex = value;
			searchButtonLabel = searchTypeMenu.dataProvider[searchTypeMenu.selectedIndex].label;
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this,"searchType",null,searchType));
		}
		
		private function onItemClick(event:MenuEvent):void {
	        searchTypeSelectedIndex = event.index;
	        
	        search();
	    }

	    public function search():void {
			dispatchEvent(UniversalSearchEvent.createUniversalSearchEvent(searchText,searchType));				    	
	    }

	    [Bindable]
	    public function get searchType():String {
            if (searchTypeMenu.selectedIndex == -1) {
                //should be impossible, yet isn't
                var i:int = 0;
                for each (var o:Object in searchTypeMenu.dataProvider) {
                    if (o["label"] == searchButtonLabel) {
                        searchTypeSelectedIndex = i;
                        break;
                    }
                    i++;
                }                
            }

            switch (searchTypeMenu.selectedIndex) {
                case 0:
                    return CompanySearchEnum.SEARCH_SMART;
                case 1:
                    return CompanySearchEnum.SEARCH_BY_EIN;
                case 2:
                    return CompanySearchEnum.SEARCH_BY_PSID;
                case 3:
                    return CompanySearchEnum.SEARCH_BY_LEGAL_NAME;
                case 4:
                    return CompanySearchEnum.SEARCH_BY_LICENSE_NUMBER;
                case 5:
                    return CompanySearchEnum.SEARCH_BY_CAN;
                case 6:
                    return CompanySearchEnum.SEARCH_BY_SERVICE_KEY;
                case 7:
                    return CompanySearchEnum.SEARCH_BY_REGISTRATION_NUMBER;
                case 8:
                    return CompanySearchEnum.SEARCH_BY_REALMID;
                case 9:
                    return CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_SSN;
                case 10:
                    return CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_EMAIL;
                case 11:
                    return CompanySearchEnum.SEARCH_BY_CFR;
                /*case 9:
                    return CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_NAME;*/
                default:
                    return CompanySearchEnum.SEARCH_SMART;
            }

	    }
	    
	    public function set searchType(value:String):void {
	    	switch (value) {
                case CompanySearchEnum.SEARCH_SMART:
                    searchTypeSelectedIndex = 0;
                    break;
                case CompanySearchEnum.SEARCH_BY_EIN:
                    searchTypeSelectedIndex = 1;
                    break;
                case CompanySearchEnum.SEARCH_BY_PSID:
                    searchTypeSelectedIndex = 2;
                    break;
                case CompanySearchEnum.SEARCH_BY_LEGAL_NAME:
                    searchTypeSelectedIndex = 3;
                    break;
                case CompanySearchEnum.SEARCH_BY_LICENSE_NUMBER:
                    searchTypeSelectedIndex = 4;
                    break;
                case CompanySearchEnum.SEARCH_BY_CAN:
                    searchTypeSelectedIndex = 5;
                    break;
                case CompanySearchEnum.SEARCH_BY_SERVICE_KEY:
                    searchTypeSelectedIndex = 6;
                    break;
                case CompanySearchEnum.SEARCH_BY_REGISTRATION_NUMBER:
                    searchTypeSelectedIndex = 7;
                    break;
                case CompanySearchEnum.SEARCH_BY_REALMID:
                    searchTypeSelectedIndex = 8;
                    break;
                case CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_SSN:
                    searchTypeSelectedIndex = 9;
                    break;
                case CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_EMAIL:
                    searchTypeSelectedIndex = 10;
                    break;
                case CompanySearchEnum.SEARCH_BY_CFR:
                    searchTypeSelectedIndex = 11;
                    break;
                /*case CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_NAME:
                    searchTypeSelectedIndex = 9;*/
                    break;
            }
	    }

        public function nextSearchType():void {
            if (searchTypeMenu.selectedIndex < searchTypeMenu.dataProvider.length - 1) {
                searchTypeSelectedIndex = searchTypeMenu.selectedIndex + 1;
            }
        }

        public function prevSearchType():void {
            if (searchTypeMenu.selectedIndex > 0) {
                searchTypeSelectedIndex = searchTypeMenu.selectedIndex - 1;
            }
        }


	    [Bindable]
	    public function get searchText():String {
	    	return mSearchText;
	    }

	    public function set searchText(value:String):void {
	    	mSearchText = value;

	    	//leaving the space bug in because it's so nice for developers.  Shh.
	    	canSearch = (mSearchText.length >= 3 && mSearchText.replace(/ /g,"").length != 0);
	    }

        public function updateSearchMenuOptions():void {
            /* If a user has SEARCH_BY_SSN permission, then he gains access to search by SSN, Email & Name */
            if(SAP.canPerformOperation(OperationsEnum.SEARCH_BY_SSN)) {
                if (!searchMenuOptions.contains(vmpEmpSearchSSN))
                    searchMenuOptions.addItem(vmpEmpSearchSSN);

                if (!searchMenuOptions.contains(vmpEmpSearchEmail))
                    searchMenuOptions.addItem(vmpEmpSearchEmail);

                if (!searchMenuOptions.contains(vmpEmpSearchCFR))
                    searchMenuOptions.addItem(vmpEmpSearchCFR);
               /* if (!searchMenuOptions.contains(vmpEmpSearchName))
                    searchMenuOptions.addItem(vmpEmpSearchName);*/
            }

        }

	}
}
