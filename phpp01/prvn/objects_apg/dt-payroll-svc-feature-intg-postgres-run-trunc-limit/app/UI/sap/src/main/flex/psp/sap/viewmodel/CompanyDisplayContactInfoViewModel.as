package psp.sap.viewmodel
{
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyContacts;
    import psp.sap.model.CompanyServiceState;


    public class CompanyDisplayContactInfoViewModel extends CompositePartViewModel
	{

        private var mSelectedContactIndex:int;         //this one is bound to the other VMs
        [Bindable] public var actualSelectedIndex:int; //this one is bound to the view

        [Bindable] public var companyContacts:CompanyContacts;
		
		public function CompanyDisplayContactInfoViewModel()
		{
            reloadOnSave = true;
			super();
		}

        override protected function loadModelData():void {
			if (companyKey != null) {
				SAP.instance.companyService.getCompanyContacts(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onContactsLoaded));
            } else {
                loadModelData();
            }
		}

		public function onContactsLoaded(e:ResultEvent):void {
			companyContacts = e.result as CompanyContacts;

			if (companyContacts == null || companyContacts.contacts.length == 0) {
				selectedContactIndex = -1;
            }else{
                selectedContactIndex = 0;
            }
		}
				
        [Bindable]
        public function get selectedContactIndex():int {
            return mSelectedContactIndex;
        }

        public function set selectedContactIndex(value:int):void {
            mSelectedContactIndex = value;
            if (value >= 0 && value < companyContacts.contacts.length) {
                actualSelectedIndex = value;    
            }            
        }

        public function removeInvalidFlagOnEmail(email:String):void {
            SAP.instance.companyService.removeInvalidFlagOnEmailAddresses(companyKey.sourceSystemCd, companyKey.companyId, email, createSaveResponder());
            forceSave();
        }

        public function isDirectDepositCustomer():Boolean{
            if (company == null) {
                return false;
            }
            /*Not using !DIYOnly Problem in Migration*/
            if(company.companyServiceState == CompanyServiceState.AssistedPending || company.companyServiceState == CompanyServiceState.AssistedActive || company.companyServiceState == CompanyServiceState.DIYDD){
                return true;
            }
            return false;
        }

    }

}
