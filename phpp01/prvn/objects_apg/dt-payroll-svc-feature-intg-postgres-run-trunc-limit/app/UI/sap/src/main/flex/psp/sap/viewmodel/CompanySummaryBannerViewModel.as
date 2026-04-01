package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.CompanyBalance;
    import psp.sap.model.CompanyNote;
    import psp.sap.model.CompanyStatus;

    /**
	 * The CompanySummaryBanner view displays a set of 'at a glance' values for a company
	 * that are typically important for an agent to see for context.
	 * 
	 * These values represent the set of values that are currently in the database; they are not
	 * to represent the company's current edits (that have not been saved.)  This is by fiat from the
	 * XD team.
	 * 
	 * Therefore, the values display in the banner should not change
	 * as a user is entering information; the banner valeus should auto-update when a company
	 * is successfully saved.
	 * 
	 * DWeinberg 7/17/2009 - Still some work and refactoring left to do when banner is redesigned.
	 */
	public class CompanySummaryBannerViewModel extends CompositePartViewModel
	{
		[Bindable] public var companyStatus:CompanyStatus;
        [Bindable] public var legalName:String = "";
        [Bindable] public var fein:String = "";
        [Bindable] public var companyBalance:Number;
        [Bindable] public var payrollRunCount:int;
        [Bindable] public var bankReturnCount:int;
        [Bindable] public var alertNote:CompanyNote;

        [Bindable]
        [ArrayElementType("psp.sap.model.EntityChange")]
        public var entityChangeHistory:ArrayCollection = new ArrayCollection();

        [ArrayElementType("psp.sap.model.CompanyStrike")]
        [Bindable]
        public var strikes:ArrayCollection = new ArrayCollection();

        private var mEntityChangeHistoryPopUp:PopUpPartViewModel;
        private var mEntityChangeHistoryPopUpViewModel:EntityChangeHistoryPopUpViewModel;

		public function CompanySummaryBannerViewModel()
		{
            mEntityChangeHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.COMPANY_ENTITY_CHANGE);
            mEntityChangeHistoryPopUp.closeOnSave = true;
            mEntityChangeHistoryPopUpViewModel = mEntityChangeHistoryPopUp.addNewPart(EntityChangeHistoryPopUpViewModel, CompanyInspectorPageEnum.COMPANY_ENTITY_CHANGE) as EntityChangeHistoryPopUpViewModel;
        }
        
		/**
		 * Copy the current model values to the banner holder variables;
		 * holder values are used so the company information does not auto-update
		 * as the user changes values on the screen before saving.
		 */
		protected function copyValues():void {
			if (company == null)
				return;
				
			this.legalName = company.legalName;
			this.fein = company.fein;
		}


		public function canPerformOperation(operation:String):Boolean {
			return SAP.canPerformOperation(operation);
		}
		
		override protected function loadModelData():void {
            this.loadCount = 7;
            SAP.instance.payrollRunService.findCompanyBalance(company.sourceSystemCd,
                    company.companyId,
                    createLoadModelDataResponder(onBalanceResults, onResultFault));
            SAP.instance.companyService.getCompanyStatus(companyKey.sourceSystemCd, companyKey.companyId, false, true,
                    createLoadModelDataResponder(onStatusLoaded));
            SAP.instance.companyService.getEntityChangeHistory(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onEntityChangeResults));
            SAP.instance.companyService.getStrikeInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onStrikeInfoLoaded));
            SAP.instance.companyService.getPayrollRunCount(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onPayrollRunCountLoaded));
            SAP.instance.companyService.getBankReturnTransactionCount(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onBankReturnCountLoaded));
            SAP.instance.companyService.getMostRecentAlertNote(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onAlertNotesLoaded));
        }
				
		private function onBalanceResults(e:ResultEvent):void {
			var balance:CompanyBalance = e.result as CompanyBalance;
			companyBalance = balance.balanceDue;
		}
		
		private function onResultFault(e:FaultEvent):void {
			companyBalance = NaN;
		}

        private function onEntityChangeResults(e:ResultEvent):void {
			entityChangeHistory= e.result as ArrayCollection;
		}
		
		private function onStatusLoaded(e:ResultEvent):void {
			companyStatus = e.result as CompanyStatus;            
		}

        private function onStrikeInfoLoaded(e:ResultEvent):void {
            strikes = ArrayCollection(e.result);
        }

        private function onPayrollRunCountLoaded(e:ResultEvent):void {
            payrollRunCount = int(e.result);
        }

        private function onBankReturnCountLoaded(e:ResultEvent):void {
            bankReturnCount = int(e.result);
        }

        private function onAlertNotesLoaded(e:ResultEvent): void {
            alertNote = CompanyNote(e.result);
        }

		override protected function initializeBackingProperties():void {
			copyValues();
		}

        public function showEntityChangeHistory():void {
            mEntityChangeHistoryPopUp.displayPopUp();
        }
    }
}