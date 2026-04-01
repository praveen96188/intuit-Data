package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.AgencyInfoDTO;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class AgencyInfoViewModel extends CompositePartViewModel {

        private var mEFTPSEnrollmentHistoryPopUp:PopUpPartViewModel;
        private var mEFTPSEnrollmentHistoryViewModel:EFTPSEnrollmentsHistoryViewModel;

        private var mRAFHistoryPopUp:PopUpPartViewModel;
        private var mRAFHistoryPopUpViewModel:RAFHistoryPopUpViewModel;

        private var achHistoryPopUp:PopUpPartViewModel;
        private var achHistoryPopUpViewModel:ACHEnrollmentHistoryViewModel;

        private var mDFHistoryPopUp:PopUpPartViewModel;
        private var mDFHistoryPopUpViewModel:DepositFrequencyHistoryPopUpViewModel;

        private var mFTHistoryPopUp:PopUpPartViewModel;
        private var mFTHistoryPopUpViewModel:FilerTypeHistoryPopUpViewModel;

        private var mLawRatesHistoryPopUp:PopUpPartViewModel;
        private var mLawRatesHistoryPopUpViewModel:LawRatesHistoryPopUpViewModel;

        private var mEntityChangeHistoryPopUp:PopUpPartViewModel;
        private var mEntityChangeHistoryPopUpViewModel:EntityChangeHistoryPopUpViewModel;

        private var mAgencyIdHistoryPopUp:PopUpPartViewModel;
        private var mAgencyIdHistoryPopUpViewModel:AgencyIDHistoryPopUpViewModel;

        private var mCompanyAgencyHistoryPopUp:PopUpPartViewModel;
        private var mCompanyAgencyHistoryPopUpViewModel:CompanyAgencyHistoryPopUpViewModel;

        private var mPTSDPopUp:PopUpPartViewModel;
        private var mPTSDPopUpViewModel:PaymentTemplateSupportDatePopUpViewModel;

        private var mPaymentMethodsPopUp:PopUpPartViewModel;
        private var mPaymentMethodsPopUpViewModel:PaymentMethodPopUpViewModel;

        private var mACHRegistrationHistoryPopUp:PopUpPartViewModel;
        private var mACHRegistrationHistoryPopUpViewModel:ACHRegistrationHistoryPopUpViewModel;

        private var mAdditionalAgencyIdsHistoryPopUp:PopUpPartViewModel;
        private var mAdditionalAgencyIdsHistoryPopUpViewModel:AdditionalIdHistoryPopUpViewModel;

        private var mAdditionalFilingAmountsHistoryPopUp:PopUpPartViewModel;
        private var mAdditionalFilingAmountHistoryPopUpViewModel:CompanyFilingAmountHistoryViewModel;

        private var mEditRatesPopUp:PopUpPartViewModel;
        private var mEditRatesViewModel:EditRatesViewModel;

        private var mEditRatesMultiplePopUp:PopUpPartViewModel;
        private var mEditRatesMultipleViewModel:EditRatesMultipleViewModel;

        private var mEditAgencyIdsPopUp:PopUpPartViewModel;
        private var mEditAgencyIdsViewModel:EditAgencyIdViewModel;

        private var mEditDepositFrequencyPopUp:PopUpPartViewModel;
        private var mEditDepositFrequencyViewModel:EditDepositFrequencyViewModel;

        private var mEditAdditionalFilingAmountsPopUp:PopUpPartViewModel;
        private var mEditAdditionalFilingAmountsViewModel:EditAdditionalFilingAmountsViewModel;

        private var mEditLawFlagsPopUp:PopUpPartViewModel;
        private var mEditLawFlagsViewModel:EditLawFlagsViewModel;

        private var mEditFilerTypePopUp:PopUpPartViewModel;
        private var mEditFilerTypeViewModel:EditFilerTypeViewModel;

        private var mLawFlagHistoryPopUp:PopUpPartViewModel;
        private var mLawFlagHistoryViewModel:LawFlagHistoryViewModel;

        public static const EDIT_MULTIPLE_RATES:String = "editMultipleRates";
        public static const EDIT_SINGLE_RATE:String = "editSingleRate";

        public function AgencyInfoViewModel() {
            super();
            reloadOnActivate = false;
            reloadOnSave = true;

            mEFTPSEnrollmentHistoryPopUp = addPopUpPart(EnrollmentsPageEnum.EFTPS_HISTORY);
            mEFTPSEnrollmentHistoryViewModel = mEFTPSEnrollmentHistoryPopUp.addNewPart(EFTPSEnrollmentsHistoryViewModel, EnrollmentsPageEnum.EFTPS_HISTORY) as EFTPSEnrollmentsHistoryViewModel;
            mEFTPSEnrollmentHistoryViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mRAFHistoryPopUp = addPopUpPart(EnrollmentsPageEnum.RAF_HISTORY);
            mRAFHistoryPopUpViewModel = mRAFHistoryPopUp.addNewPart(RAFHistoryPopUpViewModel, EnrollmentsPageEnum.RAF_HISTORY) as RAFHistoryPopUpViewModel;
            mRAFHistoryPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            achHistoryPopUp = addPopUpPart(EnrollmentsPageEnum.ACH_HISTORY);
            achHistoryPopUpViewModel = achHistoryPopUp.addNewPart(ACHEnrollmentHistoryViewModel, EnrollmentsPageEnum.ACH_HISTORY) as ACHEnrollmentHistoryViewModel;
            achHistoryPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mDFHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.DEPOSIT_FREQUENCY_HISTORY);
            mDFHistoryPopUpViewModel = mDFHistoryPopUp.addNewPart(DepositFrequencyHistoryPopUpViewModel, CompanyInspectorPageEnum.DEPOSIT_FREQUENCY_HISTORY) as DepositFrequencyHistoryPopUpViewModel;

            mAgencyIdHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.AGENCY_ID_HISTORY);
            mAgencyIdHistoryPopUpViewModel = mAgencyIdHistoryPopUp.addNewPart(AgencyIDHistoryPopUpViewModel, CompanyInspectorPageEnum.AGENCY_ID_HISTORY) as AgencyIDHistoryPopUpViewModel;

            mCompanyAgencyHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.COMPANY_AGENCY_HISTORY);
            mCompanyAgencyHistoryPopUpViewModel = mCompanyAgencyHistoryPopUp.addNewPart(CompanyAgencyHistoryPopUpViewModel, CompanyInspectorPageEnum.COMPANY_AGENCY_HISTORY) as CompanyAgencyHistoryPopUpViewModel;

            mFTHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.FILER_TYPE_HISTORY);
            mFTHistoryPopUpViewModel = mFTHistoryPopUp.addNewPart(FilerTypeHistoryPopUpViewModel, CompanyInspectorPageEnum.FILER_TYPE_HISTORY) as FilerTypeHistoryPopUpViewModel;

            mLawRatesHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.LAW_RATES_HISTORY);
            mLawRatesHistoryPopUpViewModel = mLawRatesHistoryPopUp.addNewPart(LawRatesHistoryPopUpViewModel, CompanyInspectorPageEnum.LAW_RATES_HISTORY) as LawRatesHistoryPopUpViewModel;

            mEntityChangeHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.COMPANY_ENTITY_CHANGE);
            mEntityChangeHistoryPopUp.closeOnSave = true;
            mEntityChangeHistoryPopUpViewModel = mEntityChangeHistoryPopUp.addNewPart(EntityChangeHistoryPopUpViewModel, CompanyInspectorPageEnum.COMPANY_ENTITY_CHANGE) as EntityChangeHistoryPopUpViewModel;

            mPTSDPopUp = addPopUpPart(CompanyInspectorPageEnum.PSP_PAYMENT_TEMPLATE_SUPPORT_DATES);
            mPTSDPopUpViewModel = mPTSDPopUp.addNewPart(PaymentTemplateSupportDatePopUpViewModel, CompanyInspectorPageEnum.PSP_PAYMENT_TEMPLATE_SUPPORT_DATES) as PaymentTemplateSupportDatePopUpViewModel;

            mPaymentMethodsPopUp = addPopUpPart(CompanyInspectorPageEnum.PSP_PAYMENT_METHOD);
            mPaymentMethodsPopUp.closeOnSave = true;
            mPaymentMethodsPopUpViewModel = mPaymentMethodsPopUp.addNewPart(PaymentMethodPopUpViewModel, CompanyInspectorPageEnum.PSP_PAYMENT_METHOD) as PaymentMethodPopUpViewModel;

            mACHRegistrationHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.ACH_REGISTRATION_HISTORY);
            mACHRegistrationHistoryPopUpViewModel = mACHRegistrationHistoryPopUp.addNewPart(ACHRegistrationHistoryPopUpViewModel, CompanyInspectorPageEnum.ACH_REGISTRATION_HISTORY) as ACHRegistrationHistoryPopUpViewModel;

            mAdditionalAgencyIdsHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.PSP_ADDITIONAL_AGENCY_IDS);
            mAdditionalAgencyIdsHistoryPopUpViewModel = mAdditionalAgencyIdsHistoryPopUp.addNewPart(AdditionalIdHistoryPopUpViewModel, CompanyInspectorPageEnum.PSP_ADDITIONAL_AGENCY_IDS) as AdditionalIdHistoryPopUpViewModel;

            mAdditionalFilingAmountsHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_ADDITIONAL_FILING_AMOUNT_HISTORY);
            mAdditionalFilingAmountHistoryPopUpViewModel = mAdditionalFilingAmountsHistoryPopUp.addNewPart(CompanyFilingAmountHistoryViewModel, CompanyInspectorPageEnum.TAX_ADDITIONAL_FILING_AMOUNT_HISTORY) as CompanyFilingAmountHistoryViewModel;

            mEditRatesPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_RATES);
            mEditRatesPopUp.closeOnSave = true;
            mEditRatesViewModel = mEditRatesPopUp.addNewPart(EditRatesViewModel, CompanyInspectorPageEnum.TAX_EDIT_RATES) as EditRatesViewModel;
            mEditRatesViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
            mEditRatesViewModel.addEventListener(AgencyInfoViewModel.EDIT_MULTIPLE_RATES, function(e:Event):void {
                editRatesMultiple(mEditRatesViewModel.paymentTemplate);
            }, false, 0, false );

            mEditRatesMultiplePopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_RATES_MULTIPLE);
            mEditRatesMultipleViewModel = mEditRatesMultiplePopUp.addNewPart(EditRatesMultipleViewModel, CompanyInspectorPageEnum.TAX_EDIT_RATES_MULTIPLE) as EditRatesMultipleViewModel;
            mEditRatesMultipleViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
            mEditRatesMultipleViewModel.addEventListener(AgencyInfoViewModel.EDIT_SINGLE_RATE, function(e:Event):void {
                editRates(mEditRatesMultipleViewModel.paymentTemplate);
            }, false, 0, false );

            mEditAgencyIdsPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_AGENCY_IDS);
            mEditAgencyIdsPopUp.closeOnSave = true;
            mEditAgencyIdsViewModel = mEditAgencyIdsPopUp.addNewPart(EditAgencyIdViewModel, CompanyInspectorPageEnum.TAX_EDIT_AGENCY_IDS) as EditAgencyIdViewModel;
            mEditAgencyIdsViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mEditDepositFrequencyPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_DEPOSIT_FREQUENCY);
            mEditDepositFrequencyPopUp.closeOnSave = true;
            mEditDepositFrequencyViewModel = mEditDepositFrequencyPopUp.addNewPart(EditDepositFrequencyViewModel, CompanyInspectorPageEnum.TAX_EDIT_DEPOSIT_FREQUENCY) as EditDepositFrequencyViewModel;
            mEditDepositFrequencyViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mEditAdditionalFilingAmountsPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_ADDITIONAL_FILING_AMOUNTS);
            mEditAdditionalFilingAmountsPopUp.closeOnSave = true;
            mEditAdditionalFilingAmountsViewModel = mEditAdditionalFilingAmountsPopUp.addNewPart(EditAdditionalFilingAmountsViewModel, CompanyInspectorPageEnum.TAX_EDIT_ADDITIONAL_FILING_AMOUNTS) as EditAdditionalFilingAmountsViewModel;
            mEditAdditionalFilingAmountsViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mEditLawFlagsPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_LAW_FLAGS);
            mEditLawFlagsPopUp.closeOnSave = true;
            mEditLawFlagsViewModel = mEditLawFlagsPopUp.addNewPart(EditLawFlagsViewModel, CompanyInspectorPageEnum.TAX_EDIT_LAW_FLAGS) as EditLawFlagsViewModel;
            mEditLawFlagsViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mEditFilerTypePopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EDIT_FILER_TYPE);
            mEditFilerTypePopUp.closeOnSave = true;
            mEditFilerTypeViewModel = mEditFilerTypePopUp.addNewPart(EditFilerTypeViewModel, CompanyInspectorPageEnum.TAX_EDIT_FILER_TYPE) as EditFilerTypeViewModel;
            mEditFilerTypeViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mLawFlagHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_LAW_FLAG_HISTORY);
            mLawFlagHistoryViewModel = mLawFlagHistoryPopUp.addNewPart(LawFlagHistoryViewModel, CompanyInspectorPageEnum.TAX_LAW_FLAG_HISTORY) as LawFlagHistoryViewModel;

            mAgencyInfoTabViewModelCollection = new ArrayCollection();
        }

        [Bindable]
        public var agencyInfo:AgencyInfoDTO;

        [ArrayElementType("psp.sap.model.AgencyInfoDTO")]
		[Bindable] public var agencyInfoArray:ArrayCollection;

        [ArrayElementType("psp.sap.viewmodel.AgencyInfoTabViewModel")]
        private var mAgencyInfoTabViewModelCollection:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getAgencyInfoArray(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onLoadInfoArrayCompleted));
        }

        private function onLoadInfoArrayCompleted(e:ResultEvent):void {
            agencyInfoArray = e.result as ArrayCollection;
            /* Sort agencies (IRS first, then alphabetical)    */
            var agencySort:Sort = new Sort();
            var agencySortField:SortField = new SortField("agency");
            agencySortField.compareFunction = CompareUtils.compareAgencyInfos;
            agencySort.fields = [agencySortField];
            agencyInfoArray.sort = agencySort;
            agencyInfoArray.refresh();
            mAgencyInfoTabViewModelCollection.removeAll();
            for each (var agencyInfoDTO:AgencyInfoDTO in agencyInfoArray) {
                var agencyInfoViewModel:AgencyInfoTabViewModel = new AgencyInfoTabViewModel();
                agencyInfoViewModel.agencyInfo = agencyInfoDTO;
                agencyInfoViewModel.label = agencyInfoDTO.agency.agencyAbbrev;
                agencyInfoViewModel.agencyInfoViewModel = this;
                agencyInfoTabViewModelCollection.addItem(agencyInfoViewModel);
            }
            agencyInfoTabViewModelCollection.refresh();
        }

        [Bindable ("propertyChange")]
        public function get agencyInfoTabViewModelCollection():ArrayCollection {
            return mAgencyInfoTabViewModelCollection;
        }

        public function viewEFTPSHistory():void {
            mEFTPSEnrollmentHistoryViewModel.targetCompanyKey = companyKey;
            mEFTPSEnrollmentHistoryPopUp.displayPopUp();
        }

        public function viewRAFHistory():void {
            mRAFHistoryPopUpViewModel.targetCompanyKey = companyKey;
            mRAFHistoryPopUp.displayPopUp();
        }

        public function viewACHEnrollmentHistory():void {
            achHistoryPopUpViewModel.targetCompanyKey = companyKey;
            achHistoryPopUp.displayPopUp();
        }

        public function viewLawRatesHistory(pTemplate:PaymentTemplate):void {
            mLawRatesHistoryPopUpViewModel.setActivator(LawRatesHistoryPopUpViewModel.createActivator(pTemplate));
            mLawRatesHistoryPopUp.displayPopUp();
        }

        public function viewDepositFrequencyHistory(template:PaymentTemplate):void {
            mDFHistoryPopUpViewModel.setActivator(DepositFrequencyHistoryPopUpViewModel.createActivator(template));
            mDFHistoryPopUp.displayPopUp();
        }

         public function viewAgencyIdHistory(template:PaymentTemplate):void {
            mAgencyIdHistoryPopUpViewModel.paymentTemplate = template;
            mAgencyIdHistoryPopUp.displayPopUp();
        }

        public function viewErFicaDeferralHistory():void {
            mCompanyAgencyHistoryPopUp.displayPopUp();
        }

        public function viewFilerTypeHistory(templateName:String):void {
            mFTHistoryPopUp.displayPopUp();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

        public function viewEntityChangeHistory():void {
            mEntityChangeHistoryPopUp.displayPopUp();
        }

        public function showPaymentMethodsHistory(paymentTemplateCode:String):void {
            mPaymentMethodsPopUpViewModel.setActivator(PaymentMethodPopUpViewModel.createActivator(paymentTemplateCode));
            mPaymentMethodsPopUp.displayPopUp();
        }
        public function showAdditionalIdHistory(paymentTemplateCode:String):void {
            mAdditionalAgencyIdsHistoryPopUpViewModel.targetCompanyKey = companyKey;
            mAdditionalAgencyIdsHistoryPopUpViewModel.templateCode = paymentTemplateCode;
            mAdditionalAgencyIdsHistoryPopUp.displayPopUp();
        }

        public function viewACHRegistrationHistory(paymentTemplate:PaymentTemplate):void {
            mACHRegistrationHistoryPopUpViewModel.setActivator(ACHRegistrationHistoryPopUpViewModel.createActivator(paymentTemplate));
            mACHRegistrationHistoryPopUp.displayPopUp();
        }

        public function viewFilingAmountHistory(paymentTemplate:PaymentTemplate):void {
            mAdditionalFilingAmountHistoryPopUpViewModel.paymentTemplate = paymentTemplate;
            mAdditionalFilingAmountsHistoryPopUp.displayPopUp();
        }

        public function editRates(paymentTemplate:PaymentTemplate):void {
            mEditRatesViewModel.setActivator(EditRatesViewModel.createActivator(paymentTemplate));

            mEditRatesMultiplePopUp.hidePopUp();
            mEditRatesPopUp.displayPopUp();
        }

        public function editRatesMultiple(paymentTemplate:PaymentTemplate):void {
            mEditRatesMultipleViewModel.setActivator(EditRatesMultipleViewModel.createActivator(paymentTemplate));

            mEditRatesPopUp.hidePopUp();
            mEditRatesMultiplePopUp.displayPopUp();
        }

        public function editAgencyIds(paymentTemplate:PaymentTemplate):void {
            mEditAgencyIdsViewModel.setActivator(EditAgencyIdViewModel.createActivator(paymentTemplate));
            mEditAgencyIdsPopUp.displayPopUp();
        }

        public function editDepositFrequencies(paymentTemplate:PaymentTemplate):void {
            mEditDepositFrequencyViewModel.setActivator(EditDepositFrequencyViewModel.createActivator(paymentTemplate));
            mEditDepositFrequencyPopUp.displayPopUp();
        }

        public function editAdditionalFilingAmounts(paymentTemplate:PaymentTemplate):void {
            mEditAdditionalFilingAmountsViewModel.setActivator(EditAdditionalFilingAmountsViewModel.createActivator(paymentTemplate));
            mEditAdditionalFilingAmountsPopUp.displayPopUp();
        }

        public function editLawFlags(paymentTemplate:PaymentTemplate):void {
            mEditLawFlagsViewModel.setActivator(EditLawFlagsViewModel.createActivator(paymentTemplate));
            mEditLawFlagsPopUp.displayPopUp();
        }

        public function editFilerType():void {
            mEditFilerTypePopUp.displayPopUp();
        }

        public function viewLawFlagHistory(paymentTemplate:PaymentTemplate):void {
            mLawFlagHistoryViewModel.setActivator(LawFlagHistoryViewModel.createActivator(paymentTemplate));
            mLawFlagHistoryPopUp.displayPopUp();
        }

        private var selectedPaymentTemplate:PaymentTemplate;
        private var agentEnabled:Boolean;

        public function updateAgentEnabled(paymentTemplate:PaymentTemplate, agentEnabled:Boolean):void {
            this.selectedPaymentTemplate = paymentTemplate;
            this.agentEnabled = agentEnabled;
            forceSave();
        }

        override protected function executeSave():void {
            SAP.instance.taxService.updateAgentEnabled(companyKey.sourceSystemCd, companyKey.companyId, selectedPaymentTemplate.paymentTemplateCd, agentEnabled, createSaveResponder());
        }

        public function updateErFicaDeferral(newErFicaDeferral:Boolean):void {
            SAP.instance.showProgress((newErFicaDeferral ? "Enabling" : "Disabling") + " ER FICA Deferral");
            SAP.instance.taxService.updateErFicaDeferral(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    newErFicaDeferral,
                    createSaveResponder(onCompanyAgencySaved))
        }

        protected function onCompanyAgencySaved(e:ResultEvent=null):void {
            if(SAP.instance.isShowingProgress){
                SAP.instance.hideProgress();
            }
            refresh();
        }
    }
}