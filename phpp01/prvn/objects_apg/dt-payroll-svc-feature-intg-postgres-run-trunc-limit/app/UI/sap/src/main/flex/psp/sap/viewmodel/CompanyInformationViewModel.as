package psp.sap.viewmodel
{
    import mx.binding.utils.BindingUtils;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.CompanyServiceState;

    public class CompanyInformationViewModel extends CompositePartViewModel
	{			

		public function CompanyInformationViewModel()
		{
			super();
			
			this.label = CompanyInspectorPageEnum.COMPANY_INFO;
			
			addExpander(CompanyInspectorPageEnum.COMPANY_RECENT_EVENTS).addNewPart(RecentEventsViewModel,CompanyInspectorPageEnum.COMPANY_RECENT_EVENTS);			
			
			var legalDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.COMPANY_LEGAL_INFO).addDisplayEditHistory();
			legalDEH.addDisplay(CompanyEditLegalInfoViewModel);
			legalDEH.addEdit(CompanyEditLegalInfoViewModel);
			legalDEH.addHistory(EntityChangeHistoryPopUpViewModel);

			var contactsDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.COMPANY_CONTACT_INFO).addDisplayEditHistory();								
			var contactDisplay:CompanyDisplayContactInfoViewModel = contactsDEH.addDisplay(CompanyDisplayContactInfoViewModel) as CompanyDisplayContactInfoViewModel; 
			var contactEdit:CompanyEditContactInfoViewModel = contactsDEH.addEdit(CompanyEditContactInfoViewModel) as CompanyEditContactInfoViewModel;
			BindingUtils.bindProperty(contactEdit, "selectedContactIndex", contactDisplay, "selectedContactIndex");
            BindingUtils.bindProperty(contactDisplay, "selectedContactIndex", contactEdit, "selectedContactIndex");
			
			var statusDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.DISPLAY_SUBSCRIPTION_STATUS).addDisplayEditHistory();
			statusDEH.addDisplay(CompanyDisplaySubscriptionStatusViewModel);
			statusDEH.addEdit(CompanyEditSubscriptionStatusViewModel);
			statusDEH.addHistory(CompanySubscriptionStatusHistoryViewModel);

            var cancellationInfoDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.CANCELLATION_INFO).addDisplayEditHistory();
			cancellationInfoDEH.addDisplay(CompanyDisplayCancellationInfoViewModel);
			cancellationInfoDEH.addEdit(CompanyEditCancellationInfoViewModel);

	        var priceTypeDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.PRICE_TYPE).addDisplayEditHistory();
            priceTypeDEH.addDisplay(CompanyEditPriceTypeViewModel);
            CompanyEditPriceTypeViewModel(priceTypeDEH.addEdit(CompanyEditPriceTypeViewModel)).isAssisted = true; //will just hide view otherwise
			
			var fundingModelDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.FUNDING_MODEL).addDisplayEditHistory();
			fundingModelDEH.addDisplay(CompanyEditFundingModelViewModel);
			fundingModelDEH.addEdit(CompanyEditFundingModelViewModel);
			fundingModelDEH.addHistory(CompanyFundingModelHistoryViewModel);
			
			addExpander(CompanyInspectorPageEnum.STRIKES_SUMMARY).addNewPart(CompanyManageStrikesViewModel,CompanyInspectorPageEnum.STRIKES_SUMMARY);
			
			var qbDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.QUICKBOOKS_INFORMATION).addDisplayEditHistory();
			qbDEH.addDisplay(CompanyQuickBooksInfoViewModel);
			qbDEH.addEdit(CompanyQuickBooksInfoEditViewModel);
            qbDEH.addHistory(CompanyQuickBooksHistoryViewModel);
			
			addExpander(CompanyInspectorPageEnum.COMPANY_AGREEMENTS).addNewPart(CompanyAgreementsSummaryViewModel, CompanyInspectorPageEnum.COMPANY_AGREEMENTS);			

            var taxDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.COMPANY_SALES_TAX).addDisplayEditHistory();
            taxDEH.addDisplay(CompanySalesTaxEditViewModel);
            taxDEH.addEdit(CompanySalesTaxEditViewModel);

			addExpander(CompanyInspectorPageEnum.COMPANY_PIN).addNewPart(CompanyPINViewModel, CompanyInspectorPageEnum.COMPANY_PIN);						
			
			var tokenDEH:DisplayEditHistoryViewModel = addExpander(CompanyInspectorPageEnum.COMPANY_TOKEN).addDisplayEditHistory();
			tokenDEH.addDisplay(CompanyEditTokenViewModel);
			tokenDEH.addEdit(CompanyEditTokenViewModel);
			
			addExpander(CompanyInspectorPageEnum.COMPANY_DEBUGGING).addNewPart(CompanyDebugViewModel, CompanyInspectorPageEnum.COMPANY_DEBUGGING);
		}

		public function canPerformOperation(operation:String):Boolean {
			return SAP.canPerformOperation(operation);
		}

        public function mayEditContacts():Boolean {
            if (company.companyServiceState == CompanyServiceState.DIYDD) {
                return canPerformOperation(OperationsEnum.EDIT_COMPANY_CONTACT_INFORMATION)
            } else if (company.companyServiceState == CompanyServiceState.DIYOnly) {
                return canPerformOperation(OperationsEnum.EDIT_PRINCIPAL_CONTACTS_DIY)
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                return canPerformOperation(OperationsEnum.EDIT_ASSISTED_CONTACTS_PENDING) || canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS_PENDING_ACTIVATION)
            } else if (company.companyServiceState == CompanyServiceState.AssistedActive) {
                return canPerformOperation(OperationsEnum.EDIT_ASSISTED_CONTACTS_ACTIVE) || canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS)
            }
            return false;
        }

        public function saveAllPreferences():void {
            for each (var pvm:AbstractPartViewModel in this.partViewModels) {
                if (pvm is ExpanderViewModel) {
                    var evm:ExpanderViewModel = pvm as ExpanderViewModel;
                    if (evm.preferenceEnabled && ! evm.preferenceLocked) {
                        evm.savePreference();
                    }
                }
            }
        }

        public function expandAll():void {
            for each (var pvm:AbstractPartViewModel in this.partViewModels) {
                if (pvm is ExpanderViewModel) {
                    var evm:ExpanderViewModel = pvm as ExpanderViewModel;
                    if (! evm.opened) {
                        evm.expand();
                    }
                }
            }
        }

        public function collapseAll():void {
            for each (var pvm:AbstractPartViewModel in this.partViewModels) {
                if (pvm is ExpanderViewModel) {
                    var evm:ExpanderViewModel = pvm as ExpanderViewModel;
                    if (evm.opened) {
                        evm.collapse();
                    }
                }
            }
        }

        public function mayEditLegalInformation():Boolean {
            if (company == null) {
                return true;
            }

            if(company.companyServiceState == CompanyServiceState.DIYDD || company.companyServiceState == CompanyServiceState.DIYOnly) {
                return SAP.canPerformOperation(OperationsEnum.EDIT_COMPANY_LEGAL_INFORMATION);
            } else if(company.companyServiceState == CompanyServiceState.AssistedPending) {
                return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_LEGAL_INFO_PENDING_ACTIVATION);
            } else {
                return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_LEGAL_INFO);
            }

        }

		
	}
}
