package psp.sap.viewmodel
{
import mx.rpc.Responder;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import psp.sap.application.CompanyInspectorLinkHandler;
import psp.sap.application.SAP;
import psp.sap.application.enums.BankReturnsSearchTypeEnum;
import psp.sap.application.enums.ExplorerEnum;
import psp.sap.application.enums.RiskInspectorPageEnum;
import psp.sap.model.BankReturn;
import psp.sap.model.Company;
import psp.sap.model.PayrollRun;

public class BankReturnsSearchViewModel
									extends AbstractPartViewModel	
	{

		//All search types
		public static const SEARCH_TYPES:Array = [BankReturnsSearchTypeEnum.ALL_RETURNS, BankReturnsSearchTypeEnum.FINANCIAL_RESOLUTIONS, BankReturnsSearchTypeEnum.RISK_COLLECTIONS, BankReturnsSearchTypeEnum.RISK_ASSESSMENT];		

		private var mAllReturnsViewModel:BankReturnsSearchAllReturnsViewModel;
		private var mFinancialResolutionsViewModel:BankReturnsSearchFinancialResolutionsViewModel;
		private var mRiskCollectionsViewModel:BankReturnsSearchRiskCollectionsViewModel;
		private var mRiskAssessmentViewModel:BankReturnsSearchRiskAssessmentViewModel;
		
		private var mSelelectedSearchIndex:int = 0;		

		private const DEFAULT_ERROR:String = "";
		private const DEFAULT_SELECTED_SEARCH_INDEX:int = 0;
		private const DEFAULT_START_RECORD:int = 0;
		
		private var mCompany:Company;
		private var mPayrollRun:PayrollRun;
	
		private var mAllTransactionsVisible:Boolean = false;

		private var mErrorString:String;
		private var mIsError:Boolean;

        

		public function BankReturnsSearchViewModel()
		{
			//super(ExplorerEnum.BANK_RETURNS);
			
			this.label = RiskInspectorPageEnum.BANK_RETURNS;
			this.reloadOnActivate = false;
			
			//Construct view Models
			allReturnsViewModel = new BankReturnsSearchAllReturnsViewModel();
			financialResolutionsViewModel = new BankReturnsSearchFinancialResolutionsViewModel();
			riskCollectionsViewModel = new BankReturnsSearchRiskCollectionsViewModel();
			riskAssessmentViewModel = new BankReturnsSearchRiskAssessmentViewModel();
			
			//Defaults
			selectedSearchIndex = DEFAULT_SELECTED_SEARCH_INDEX;
		}
		
		override protected function onActivating():void {
			if(selectedSearchViewModel.firstTimeLoad){
				// do default search once
				resetToDefaults();				
				selectedSearchViewModel.firstTimeLoad = false;
			}			

		}

		/*override public function activate(inspectorToActivate:AbstractInspectorViewModel = null):void {
			
			super.activate(inspectorToActivate);
		}*/

		public function resetToDefaults():void {
			selectedSearchViewModel.resetToDefaults();			
		}


		[Bindable]
		public function get selectedSearchIndex():int {
			return mSelelectedSearchIndex;
		}
		
		public function set selectedSearchIndex(value:int):void {
			if(value < 0) {
				return; // selectedSearchIndex = 0;
			}
			mSelelectedSearchIndex = value;
			selectedSearchViewModel = null; //Force a change
			selectedSearchType = null; //Force a change
			activate();
		}
		
		[Bindable]
		public function get selectedSearchType():String {
			return SEARCH_TYPES[selectedSearchIndex];
		}
		
		public function set selectedSearchType(value:String):void {
			//Do nothing. Don't kill me for the event hack.
		}

		public function findBankReturns():void {
			selectedSearchViewModel.searchResults.reset();
			selectedSearchViewModel.coreSearchBankReturns();
		}

		[Bindable]
		public function get selectedSearchViewModel():BankReturnsSearchBaseViewModel {
			switch(selectedSearchType) {
				case BankReturnsSearchTypeEnum.ALL_RETURNS:
					return allReturnsViewModel;
				case BankReturnsSearchTypeEnum.FINANCIAL_RESOLUTIONS:
					return financialResolutionsViewModel;
				case BankReturnsSearchTypeEnum.RISK_COLLECTIONS:
					return riskCollectionsViewModel;
				case BankReturnsSearchTypeEnum.RISK_ASSESSMENT:
					return riskAssessmentViewModel;
				default:
					return null;
			}
		}
		
		public function set selectedSearchViewModel(value:BankReturnsSearchBaseViewModel):void {
			//Do nothing. Don't kill me for the event hack.
		}			

		[Bindable]
		public function get allReturnsViewModel():BankReturnsSearchAllReturnsViewModel {
			return mAllReturnsViewModel;
		}
		
		public function set allReturnsViewModel(value:BankReturnsSearchAllReturnsViewModel):void {
			mAllReturnsViewModel = value;
		}
		
		[Bindable]
		public function get financialResolutionsViewModel():BankReturnsSearchFinancialResolutionsViewModel {
			return mFinancialResolutionsViewModel;
		}
		
		public function set financialResolutionsViewModel(value:BankReturnsSearchFinancialResolutionsViewModel):void {
			mFinancialResolutionsViewModel = value;
		}
		
		[Bindable]
		public function get riskCollectionsViewModel():BankReturnsSearchRiskCollectionsViewModel {
			return mRiskCollectionsViewModel;
		}
		
		public function set riskCollectionsViewModel(value:BankReturnsSearchRiskCollectionsViewModel):void {
			mRiskCollectionsViewModel = value;
		}
		
		[Bindable]
		public function get riskAssessmentViewModel():BankReturnsSearchRiskAssessmentViewModel {
			return mRiskAssessmentViewModel;
		}
		
		public function set riskAssessmentViewModel(value:BankReturnsSearchRiskAssessmentViewModel):void {
			mRiskAssessmentViewModel = value;
		}								

		public function goToPayrollTransactions(sourceSystemCd:String, companyId:String, sourcePayrollRunId:String):void {
			SAP.instance.companyService.findCompany(sourceSystemCd, companyId, new Responder( function(e:ResultEvent):void {
               (e.result as Company).display();
                var explorer:CompanyExplorerViewModel =
				    SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY)
				    as CompanyExplorerViewModel;
                var inspector:CompanyInspectorViewModel =  explorer.inspectors.findByApplicationItem(e.result as Company) as CompanyInspectorViewModel;
                new CompanyInspectorLinkHandler(inspector).goToSourcePayrollRun(sourcePayrollRunId);
            }, onLoadFault));            
		}

       private function onLoadFault(e:FaultEvent):void {
			if (e.fault != null) {
				if (e.fault.faultDetail != null)
					errorString = e.fault.faultDetail;
				else if (e.fault.faultString != null)
					errorString = e.fault.faultString;
			}
		}
		
		//Bank Returns
		public function goToBankReturnsUpdate(bankReturn:BankReturn):void{
			// reload the search when the page is reactivated
			selectedSearchViewModel.searchOnLoad = true;

			topic.findPage(RiskInspectorPageEnum.BANK_RETURN_STATUS_UPDATE).activatePage(BankReturnsUpdateStatusViewModel.createActivator(bankReturn));			
		}

		public function goToCompanyPayrolls(sourceSystemCd:String, companyId:String):void {
			SAP.instance.companyService.findCompany(sourceSystemCd, companyId, new Responder( function(e:ResultEvent):void {
               (e.result as Company).display();
                var explorer:CompanyExplorerViewModel =
				    SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY)
				    as CompanyExplorerViewModel;
                var inspector:CompanyInspectorViewModel =  explorer.inspectors.findByApplicationItem(e.result as Company) as CompanyInspectorViewModel;
                new CompanyInspectorLinkHandler(inspector).goToPayrolls();
            }, onLoadFault));
		}


		[Bindable]
		public function get errorString():String {
			return mErrorString;
		}

		protected function set errorString(value:String):void {
			mErrorString = value;
		}

		[Bindable]
		public function get isError():Boolean {
			return mIsError;
		}

		protected function set isError(value:Boolean):void {
			mIsError = value;
		}


	}
}
