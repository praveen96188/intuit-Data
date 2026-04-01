package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;

    import psp.sap.application.CompanyInspectorLinkHandler;

    import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.formatters.SAPCurrencyFormatter;
	import psp.sap.model.CompanyLedgerAccount;
	

	public class LedgerViewModel extends AbstractPartViewModel
	{
		
		[Bindable]
		public var currencyFormatter:SAPCurrencyFormatter = new SAPCurrencyFormatter();
		
		private var mLedgerAccounts:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public function get ledgerAccounts():ArrayCollection {
			return mLedgerAccounts;
		}
		
		public function set ledgerAccounts(value:ArrayCollection):void {
			mLedgerAccounts = value;		
		}
		
		public function LedgerViewModel()
		{
			this.label = CompanyInspectorPageEnum.COMPANY_LEDGER;
		}
		
		override protected function loadModelData():void {
			SAP.instance.payrollRunService.findLedgerAccounts(
					company.companyId, company.sourceSystemCd,
					createLoadModelDataResponder(onLedgerAccountsLoaded));
		}
		
		
		public function onLedgerAccountsLoaded(e:ResultEvent):void {
			ledgerAccounts = e.result as ArrayCollection;
			
			var sort:Sort = new Sort();
			sort.fields = [new SortField("name", true)];
		    ledgerAccounts.sort = sort;
		    ledgerAccounts.refresh();
		}
	
				
		public function viewLedgerEntry(ledgerAccount:CompanyLedgerAccount):void {
			topic.findPage(CompanyInspectorPageEnum.COMPANY_LEDGER_ACCOUNT_ENTRIES).activatePage(LedgerEntriesViewModel.createActivator(ledgerAccount));
		}

        public function viewFinancialLedgerAdjustment():void {
            new CompanyInspectorLinkHandler(CompanyInspectorViewModel(inspector)).goToFinancialLedgerAdjustment();
        }

        public function viewCreatePAndIRefunds():void {
            new CompanyInspectorLinkHandler(CompanyInspectorViewModel(inspector)).goToPAndIRefunds();
        }

        public function viewCreateCourtesyRefund():void {
            inspector.getPage(CompanyInspectorPageEnum.COURTESY_REFUND).activatePage();
        }
		
		public function recalculateLedgerBalances():void {

            SAP.instance.showProgress("Recalculating...");

			SAP.instance.companyService.reCalculateLedgerBalances(
					company.sourceSystemCd, company.companyId,
					createSaveResponder(assignSuccess));
		}

        public function assignSuccess(event:ResultEvent=null, token:Object=null):void {
			SAP.instance.hideProgress();
			saveFaulted = false;
			saveMsg = "Ledger Balances have been recalculated";
			loadModelData();
		}

	}
}
