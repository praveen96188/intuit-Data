package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.binding.utils.BindingUtils;

    import mx.collections.ArrayCollection;

    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.EINManagementInspectorPageEnum;
    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.model.AddCompany;
    import psp.sap.model.Address;
    import psp.sap.model.CompanyContacts;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.CompanyLegalInfo;
    import psp.sap.model.Contact;
    import psp.sap.view.SAPDateField;

    public class EINAddCompanyViewModel extends CompositePartViewModel {

        [Bindable]
        [BackingProperty(context=true)]
        public var licenseNumber:String;
        [Bindable]
        [BackingProperty(context=true)]
        public var eoc:String;
        [Bindable]
        [BackingProperty(context=true)]
        public var itemNumber:String;
        [Bindable]
        [BackingProperty(context=true)]
        public var serviceAccountId:String;
        [Bindable]
        [BackingProperty(context=true)]
        public var isAssisted:Boolean;
        [Bindable]
        [BackingProperty(context=true, required=false)]
        public var copyFrom:CompanyKey;

        [Bindable]
        public var legalInfoViewModel:CompanyEditLegalInfoViewModel;
        [Bindable]
        public var contactInfoViewModel:CompanyEditContactInfoViewModel;
        [Bindable]
        public var priceTypeViewModel:CompanyEditPriceTypeViewModel;
        [Bindable]
        public var editSuccessorEntityChangeViewModel:EditSuccessorEntityChangeViewModel;

        private var mLegalInfoCanSave:Boolean;
        private var mContactInfoCanSave:Boolean;
        private var mPriceTypeInfoCanSave:Boolean;
        private var mSuccessorEntityChangeCanSave:Boolean;
        private var mCanEditSuccessorEntityChange:Boolean;

        public function EINAddCompanyViewModel() {
            super();

            shallowCopyFields = ["sourceSystemCd", "companyId"];

            legalInfoViewModel = CompanyEditLegalInfoViewModel(addNewPart(CompanyEditLegalInfoViewModel, CompanyInspectorPageEnum.COMPANY_LEGAL_INFO));
            legalInfoViewModel.showSaveButtons = false;
            legalInfoViewModel.includeEffectiveDate = false;

            contactInfoViewModel = CompanyEditContactInfoViewModel(addNewPart(CompanyEditContactInfoViewModel, CompanyInspectorPageEnum.COMPANY_CONTACT_INFO));
            contactInfoViewModel.showSaveButtons = false;

            priceTypeViewModel = CompanyEditPriceTypeViewModel(addNewPart(CompanyEditPriceTypeViewModel, CompanyInspectorPageEnum.PRICE_TYPE));
            priceTypeViewModel.showSaveButtons = false;
            BindingUtils.bindProperty(priceTypeViewModel, "isAssisted", this, "isAssisted");
            BindingUtils.bindProperty(priceTypeViewModel, "itemNumber", this, "itemNumber");

            editSuccessorEntityChangeViewModel = EditSuccessorEntityChangeViewModel(addNewPart(EditSuccessorEntityChangeViewModel, CompanyInspectorPageEnum.SUCCESSOR_ENTITY_CHANGE_INFO));


            BindingUtils.bindProperty(this, "legalInfoCanSave", legalInfoViewModel, "canSave");
            BindingUtils.bindProperty(this, "contactInfoCanSave", contactInfoViewModel, "canSave");
            BindingUtils.bindProperty(this, "priceTypeInfoCanSave", priceTypeViewModel, "canSave");
            BindingUtils.bindProperty(this, "successorEntityChangeCanSave", editSuccessorEntityChangeViewModel, "canSave");
        }

        override protected function initializeBackingProperties():void {
            legalInfoViewModel.includeEffectiveDate = false;
            canEditSuccessorEntityChange = SAP.canPerformOperation(OperationsEnum.EDIT_ENTITY_CHANGE_INFO);
        }

        public static function createActivator(licenseNumber:String, eoc:String, itemNumber:String, serviceAccountId:String, isAssisted:Boolean, copyFrom:CompanyKey):Object {
            return {"licenseNumber":licenseNumber, "eoc":eoc, "itemNumber":itemNumber, "serviceAccountId":serviceAccountId, "isAssisted":isAssisted, "copyFrom":copyFrom};
        }

        override protected function loadModelData():void {
            if (copyFrom != null) {
                loadCount = 4;
                SAP.instance.companyService.getCompanyLegalInfo(copyFrom.sourceSystemCd, copyFrom.companyId, createLoadModelDataResponder(onLegalInfoResult));
                SAP.instance.companyService.getCompanyContacts(copyFrom.sourceSystemCd, copyFrom.companyId, createLoadModelDataResponder(onContactsResult));
                SAP.instance.companyService.getAdditionalContacts(licenseNumber, eoc, createLoadModelDataResponder(onAdditionalContactsResult));
                SAP.instance.companyService.getAdditionalAddresses(licenseNumber, eoc, createLoadModelDataResponder(onAdditionalAddressesResult));
            } else {
                legalInfoViewModel.clearValues = true;
                contactInfoViewModel.clearValues = true;
                loadCount = 2;
                SAP.instance.companyService.getAdditionalContacts(licenseNumber, eoc, createLoadModelDataResponder(onAdditionalContactsResult));
                SAP.instance.companyService.getAdditionalAddresses(licenseNumber, eoc, createLoadModelDataResponder(onAdditionalAddressesResult));
            }
        }

        private function onLegalInfoResult(e:ResultEvent):void {
            legalInfoViewModel.clearValues = false;
            var legalInfo:CompanyLegalInfo = CompanyLegalInfo(e.result);
            legalInfo.ein = "";
            legalInfoViewModel.legalInfo = legalInfo;
        }

        private function onContactsResult(e:ResultEvent):void {
            contactInfoViewModel.clearValues = false;
            var contacts:ArrayCollection = CompanyContacts(e.result).contacts;
            for each (var contact:Contact in contacts) {
                if (contact.address == null) {
                    contact.address = new Address();
                }
            }
            contactInfoViewModel.contacts = contacts;
        }

        private function onAdditionalContactsResult(e:ResultEvent):void {
            contactInfoViewModel.additionalContacts = ArrayCollection(e.result);
        }

        private function onAdditionalAddressesResult(e:ResultEvent):void {
            legalInfoViewModel.additionalAddresses = ArrayCollection(e.result);
        }

        [Bindable]
        public function get legalInfoCanSave():Boolean {
            return mLegalInfoCanSave;
        }

        public function set legalInfoCanSave(value:Boolean):void {
            mLegalInfoCanSave = value;
            updateCanSave();
        }

        [Bindable]
        public function get contactInfoCanSave():Boolean {
            return mContactInfoCanSave;
        }

        public function set contactInfoCanSave(value:Boolean):void {
            mContactInfoCanSave = value;
            updateCanSave();
        }

        public function get priceTypeInfoCanSave():Boolean {
            return mPriceTypeInfoCanSave;
        }

        public function set priceTypeInfoCanSave(value:Boolean):void {
            mPriceTypeInfoCanSave = value;
            updateCanSave();
        }

        [Bindable]
        public function get successorEntityChangeCanSave():Boolean {
            return mSuccessorEntityChangeCanSave;
        }

        public function set successorEntityChangeCanSave(value:Boolean):void {
            mSuccessorEntityChangeCanSave = value;
            updateCanSave();
        }

        [Bindable]
        [BackingProperty(context=true)]
        public function get canEditSuccessorEntityChange():Boolean {
            return mCanEditSuccessorEntityChange;
        }

        public function set canEditSuccessorEntityChange(value:Boolean):void {
            mCanEditSuccessorEntityChange = value;
        }

        override protected function evaluateCanSave():Boolean {
            //may not have changed vs. snapshot when copying
            var legalInfoValid:Boolean = legalInfoViewModel.activationState == ViewModelActivationStateEnum.ACTIVATED && legalInfoViewModel.isValid;
            var contactInfoValid:Boolean = contactInfoViewModel.activationState == ViewModelActivationStateEnum.ACTIVATED && contactInfoViewModel.isValid;
            var priceTypeValid:Boolean = priceTypeViewModel.activationState == ViewModelActivationStateEnum.ACTIVATED && priceTypeViewModel.isValid;
            var saveReady:Boolean = legalInfoValid && contactInfoValid && priceTypeValid;
            if (canEditSuccessorEntityChange) {
                var successorEntityChangeValid:Boolean = editSuccessorEntityChangeViewModel.activationState == ViewModelActivationStateEnum.ACTIVATED && editSuccessorEntityChangeViewModel.isValid;
                saveReady = saveReady && successorEntityChangeValid;
            }
            return saveReady;
        }

        override protected function executeSave():void {
            var legalInfo:CompanyLegalInfo = legalInfoViewModel.legalInfo;
            var contacts:ArrayCollection = contactInfoViewModel.contacts;

            legalInfo.ein = legalInfoViewModel.ein;
            legalInfo.ein = legalInfo.ein.replace(/-/g, "");
            //null out blank addresses
            for each (var contact:Contact in contacts) {
                contact.address.replaceEmptyWithNull();
            }

            var addCompany:AddCompany = new AddCompany();
            addCompany.legalInfo = legalInfo;
            addCompany.contacts = contacts;
            addCompany.licenseNumber = licenseNumber;
            addCompany.eoc = eoc;
            addCompany.itemNumber = itemNumber;
            addCompany.serviceAccountId = serviceAccountId;

            if (isAssisted) {
                addCompany.priceType = priceTypeViewModel.priceType;
                addCompany.offerCode = priceTypeViewModel.offer.offerCd;
            }
            var successorEntityChangeInfo:Boolean = (editSuccessorEntityChangeViewModel.successor == EditSuccessorEntityChangeViewModel.SUCCESSOR);
            if (successorEntityChangeInfo) {
                addCompany.isSuccessorEntityChange = successorEntityChangeInfo;
                addCompany.oldEIN = editSuccessorEntityChangeViewModel.oldEIN;
                addCompany.einEffectiveDate = SAPDateField.stringToDate(editSuccessorEntityChangeViewModel.effectiveDate, SAP.instance.configuration.dateFormatShort);
            }

            SAP.instance.companyService.addCompany(addCompany, createSaveResponder(onSaveSucceeded));
        }

        private function onSaveSucceeded(e:Event):void {
            topic.findPage(EINManagementInspectorPageEnum.EINS).activatePage(EINSearchViewModel.createActivator(licenseNumber, eoc, serviceAccountId, itemNumber));
        }

    }
}