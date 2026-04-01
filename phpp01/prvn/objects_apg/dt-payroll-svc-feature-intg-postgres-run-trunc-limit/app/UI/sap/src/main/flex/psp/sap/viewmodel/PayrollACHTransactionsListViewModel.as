package psp.sap.viewmodel
{
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
			
	public class PayrollACHTransactionsListViewModel
		extends CompositePartViewModel
	{
		private var achDetailPupVm:PopUpPartViewModel;
		private var mSort:Sort = new Sort();

        [Bindable] public var payrollACHDetailPopUpViewModel:PayrollACHDetailPopUpViewModel;
		
		public function PayrollACHTransactionsListViewModel()
		{	
			super();
			mSort.fields = [new SortField("settlementDate", false, true)];
			reloadOnActivate = false;
			
			achDetailPupVm = addPopUpPart(CompanyInspectorPageEnum.PAYROLL_ACH_DETAIL_POPUP); 
			
			payrollACHDetailPopUpViewModel = achDetailPupVm.addNewPart(PayrollACHDetailPopUpViewModel,CompanyInspectorPageEnum.PAYROLL_ACH_DETAIL_POPUP) as PayrollACHDetailPopUpViewModel;					
			
			BindingUtils.bindProperty(payrollACHDetailPopUpViewModel, "transactions", this, "transactions");			
		}

        [ArrayElementType ("psp.sap.model.MoneyMovementTransaction")]
        private var mTransactions:ArrayCollection =  new ArrayCollection();
		[Bindable]
		public function get transactions():ArrayCollection {
			return mTransactions;
		}
		
		public function set transactions(value:ArrayCollection):void {
			mTransactions = value;	
			mTransactions.sort = mSort;
			mTransactions.refresh();
		}

        private var mShowEntireHistory:Boolean = false;
		[Bindable]
		public function get showEntireHistory():Boolean {
			return mShowEntireHistory;
		}
		
		public function set showEntireHistory(value:Boolean):void {
			mShowEntireHistory = value;
			refresh();
		}
		
		private var mHasTransactions:Boolean = false;
		[Bindable]
		public function get hasTransactions():Boolean {
			return mHasTransactions;
		}
		public function set hasTransactions(value:Boolean):void {
			mHasTransactions = value;
		}

		override protected function loadModelData():void {
			var today:Date = SAP.instance.PSPDate;
			
			var oneYearAgo:Date = SAP.instance.PSPDate;
			oneYearAgo.setFullYear(	today.fullYear - 1, 
									today.month, 
									today.month == 1 && today.date == 29 ? 28 : today.date);
			
			SAP.instance.payrollRunService.findMoneyMovementTransactions(				
				this.company.sourceSystemCd, 
				this.company.companyId, 
				mShowEntireHistory ? null : oneYearAgo,	
				createLoadModelDataResponder(onTransactionsResults));
		}
		
		private function onTransactionsResults(e:ResultEvent):void {
			transactions = e.result as ArrayCollection;
			hasTransactions = transactions.length > 0;						
		}				
		
		public function showAchDetails():void {
			achDetailPupVm.displayPopUp();
		}
	
	}
}
