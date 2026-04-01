package psp.sap.viewmodel
{
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.events.PropertyChangeEvent;
	
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.events.SAPEvent;
	import psp.sap.model.MoneyMovementTransaction;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	public class PayrollACHDetailPopUpViewModel extends CompositePartViewModel
	{			
		private var mUpdatingFilters:Boolean;
		
		[Bindable] public var payrollAchDetail1:PayrollACHDetailViewModel;		
		[Bindable] public var payrollAchDetail2:PayrollACHDetailViewModel;
		[Bindable] public var payrollAchDetail3:PayrollACHDetailViewModel;
        [Bindable] public var supportNavigateBack:Boolean = false;
        [Bindable] public var selectedTransaction:MoneyMovementTransaction;
		
		public function PayrollACHDetailPopUpViewModel()
		{
			super();
			
			this.label = CompanyInspectorPageEnum.PAYROLL_ACH_DETAIL_POPUP;
			
			payrollAchDetail1 = this.addNewPart(PayrollACHDetailViewModel, CompanyInspectorPageEnum.PAYROLL_ACH_DETAIL_1, PartAdditionStrategy.MANUAL) as PayrollACHDetailViewModel;
			payrollAchDetail2 = this.addNewPart(PayrollACHDetailViewModel, CompanyInspectorPageEnum.PAYROLL_ACH_DETAIL_2, PartAdditionStrategy.MANUAL) as PayrollACHDetailViewModel;
			payrollAchDetail3 = this.addNewPart(PayrollACHDetailViewModel, CompanyInspectorPageEnum.PAYROLL_ACH_DETAIL_3, PartAdditionStrategy.MANUAL) as PayrollACHDetailViewModel;
			
			addEventListener(ViewModelEvent.ACTIVATED, onPopUpActivated, false, 0, true);
			addEventListener(ViewModelEvent.DEACTIVATED, onPopUpDeactivated, false, 0, true);
			
			BindingUtils.bindProperty(payrollAchDetail1, "saveMsg", this, "saveMsg");
			BindingUtils.bindProperty(payrollAchDetail1, "saveFaulted", this, "saveFaulted");
			BindingUtils.bindProperty(this, "saveMsg", payrollAchDetail1, "saveMsg");
			BindingUtils.bindProperty(this, "saveFaulted", payrollAchDetail1, "saveFaulted");			
			
			BindingUtils.bindProperty(payrollAchDetail2, "saveMsg", this, "saveMsg");
			BindingUtils.bindProperty(payrollAchDetail2, "saveFaulted", this, "saveFaulted");
			BindingUtils.bindProperty(this, "saveMsg", payrollAchDetail2, "saveMsg");
			BindingUtils.bindProperty(this, "saveFaulted", payrollAchDetail2, "saveFaulted");
			
			BindingUtils.bindProperty(payrollAchDetail3, "saveMsg", this, "saveMsg");
			BindingUtils.bindProperty(payrollAchDetail3, "saveFaulted", this, "saveFaulted");
			BindingUtils.bindProperty(this, "saveMsg", payrollAchDetail3, "saveMsg");
			BindingUtils.bindProperty(this, "saveFaulted", payrollAchDetail3, "saveFaulted");
			
			payrollAchDetail1.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onSelectedTransactionChanged, false, 0, true);
			payrollAchDetail2.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onSelectedTransactionChanged, false, 0, true);
			payrollAchDetail3.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onSelectedTransactionChanged, false, 0, true);
								
		}
		
		public function onSelectedTransactionChanged(e:PropertyChangeEvent):void {
			if(e.property == "selectedTransaction" || e.property == "showDetail"){							
				if(!mUpdatingFilters){
					mUpdatingFilters = true;
					detail1Transactions.refresh();
					detail2Transactions.refresh();
					detail3Transactions.refresh();
					mUpdatingFilters = false;
				}
			}						
		}		

        [ArrayElementType ("psp.sap.model.MoneyMovementTransaction")]
		private var mTransactions:ArrayCollection;
		[Bindable]
		public function get transactions():ArrayCollection {
			return mTransactions;
		}
		
		public function set transactions(value:ArrayCollection):void {
			mTransactions = value;
			detail1Transactions = value;
			detail2Transactions = value;
			detail3Transactions = value;
		}
		
		private function onPopUpActivated(e:ViewModelEvent):void {			
			payrollAchDetail1.showDetail = true;
		}
		
		private function onPopUpDeactivated(e:ViewModelEvent):void {
			if(payrollAchDetail1.showDetail){
				payrollAchDetail1.showDetail = false;
			}
			if(payrollAchDetail2.showDetail){
				payrollAchDetail2.showDetail = false;
			}
			if(payrollAchDetail3.showDetail){
				payrollAchDetail3.showDetail = false;
			}
		}
		
		public function navigateBack():void {
			dispatchEvent(new SAPEvent("navigateBack"));
		}
		
		private var mDetail1Transactions:ArrayCollection;		
		[Bindable]
		public function get detail1Transactions():ArrayCollection {
			return mDetail1Transactions;
		}
		
		public function set detail1Transactions(value:ArrayCollection):void {			
			mDetail1Transactions = new ArrayCollection(value.toArray());
			mDetail1Transactions.filterFunction = detail1Filter;
			mDetail1Transactions.refresh();
		}
		
		private function detail1Filter(transaction:MoneyMovementTransaction):Boolean {
			var showTransaction:Boolean = transaction.showDetail; 			
			if(payrollAchDetail2.showDetail){
				showTransaction = showTransaction && !transaction.compare(payrollAchDetail2.selectedTransaction);
			}			
			if(payrollAchDetail3.showDetail){
				showTransaction = showTransaction && !transaction.compare(payrollAchDetail3.selectedTransaction);
			}							
			return showTransaction;        	
        }
        
        private var mDetail2Transactions:ArrayCollection;		
		[Bindable]
		public function get detail2Transactions():ArrayCollection {
			return mDetail2Transactions;
		}
		
		public function set detail2Transactions(value:ArrayCollection):void {			
			mDetail2Transactions = new ArrayCollection(value.toArray());
			mDetail2Transactions.filterFunction = detail2Filter;
			mDetail2Transactions.refresh();
		}
		
		private function detail2Filter(transaction:MoneyMovementTransaction):Boolean {
        	var showTransaction:Boolean = transaction.showDetail; 			
			if(payrollAchDetail1.showDetail){
				showTransaction = showTransaction && !transaction.compare(payrollAchDetail1.selectedTransaction);
			}			
			if(payrollAchDetail3.showDetail){
				showTransaction = showTransaction && !transaction.compare(payrollAchDetail3.selectedTransaction);
			}							
			return showTransaction;
        }
        
        private var mDetail3Transactions:ArrayCollection;		
		[Bindable]
		public function get detail3Transactions():ArrayCollection {
			return mDetail3Transactions;
		}
		
		public function set detail3Transactions(value:ArrayCollection):void {			
			mDetail3Transactions = new ArrayCollection(value.toArray());
			mDetail3Transactions.filterFunction = detail3Filter;
			mDetail3Transactions.refresh();
		}
		
		private function detail3Filter(transaction:MoneyMovementTransaction):Boolean {
        	var showTransaction:Boolean = transaction.showDetail; 			
			if(payrollAchDetail1.showDetail){
				showTransaction = showTransaction && !transaction.compare(payrollAchDetail1.selectedTransaction);
			}			
			if(payrollAchDetail2.showDetail){
				showTransaction = showTransaction && !transaction.compare(payrollAchDetail2.selectedTransaction);
			}							
			return showTransaction;
        }

	}
}