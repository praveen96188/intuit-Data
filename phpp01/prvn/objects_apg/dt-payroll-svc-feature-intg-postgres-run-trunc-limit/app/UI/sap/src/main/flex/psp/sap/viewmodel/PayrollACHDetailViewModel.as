package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;
	import psp.sap.model.MoneyMovementTransaction;
	import psp.sap.model.PayrollACHDetailSet;		
	
	public class PayrollACHDetailViewModel extends AbstractPartViewModel
	{					
		private var mFeesSort:Sort;
        private var mTaxesSort:Sort;       
        private var mDDSort:Sort;

        [Bindable] public var feeTransactions:ArrayCollection = new ArrayCollection();
		[Bindable] public var feeTransactionsTotal:Number;
		[Bindable] public var taxTransactions:ArrayCollection = new ArrayCollection();
		[Bindable] public var taxTransactionsTotal:Number;
		[Bindable] public var taxCreditTransactions:ArrayCollection = new ArrayCollection();
		[Bindable] public var taxCreditTransactionsTotal:Number;
		[Bindable] public var allTaxTransactionsTotal:Number;
		[Bindable] public var ddTransactions:ArrayCollection = new ArrayCollection();
		[Bindable] public var ddTransactionsTotal:Number;
		[Bindable] public var compare:Boolean;

		public function PayrollACHDetailViewModel()
		{
			mFeesSort = new Sort();
            mFeesSort.fields = [new SortField("description", true, false)];
            
            mTaxesSort = new Sort();
            mTaxesSort.fields = [new SortField("agencyAbbreviation", true, false), new SortField("taxDescription", true, false)];
                                    
            mDDSort = new Sort();
            mDDSort.fields = [new SortField("employeeName", true, false)];            
		}

        private var originalSelectedTransaction:MoneyMovementTransaction;

		private var mSelectedTransaction:MoneyMovementTransaction;		
		[Bindable]
		public function get selectedTransaction():MoneyMovementTransaction {
			return mSelectedTransaction;
		}
		
		public function set selectedTransaction(value:MoneyMovementTransaction):void {								
			mSelectedTransaction = value;
			if(showDetail){
				refresh();
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
		}				
		
		private var mShowDetail:Boolean = false;
		[Bindable]
		public function get showDetail():Boolean {
			return mShowDetail;
		}
		
		public function set showDetail(value:Boolean):void {
			mShowDetail = value;
			if(value){
				activate();
				if(transactions != null && transactions.length > 0 && selectedTransaction == null){
					selectedTransaction = transactions.getItemAt(0) as MoneyMovementTransaction;
				}
			}
			else{
				deactivate();
			}
		}
				
		override protected function loadModelData():void {
			feeTransactions = new ArrayCollection();
			taxTransactions = new ArrayCollection();
			taxCreditTransactions = new ArrayCollection();
			ddTransactions = new ArrayCollection();
									
            if(selectedTransaction != null){
                originalSelectedTransaction = selectedTransaction;
                SAP.instance.payrollRunService.findAchDetailTransactions(                        
                        selectedTransaction.spcfId,
                        createLoadModelDataResponder(onTransactionsResult),companyKey.companyId);
            }
            else{
            	modelDataLoaded();
            }
        }                

        public function onTransactionsResult(e:ResultEvent):void {
        	var achDetails:PayrollACHDetailSet = PayrollACHDetailSet(e.result);
        	
            achDetails.feeTransactions.sort = mFeesSort;
            achDetails.feeTransactions.refresh();
            feeTransactions = achDetails.feeTransactions;
            feeTransactionsTotal = achDetails.feeTransactionsTotal;            
        
            achDetails.taxTransactions.sort = mTaxesSort;
            achDetails.taxTransactions.refresh();
            taxTransactions = achDetails.taxTransactions;
            taxTransactionsTotal = achDetails.taxTransactionsTotal;            
        
            achDetails.taxCreditTransactions.sort = mTaxesSort;
            achDetails.taxCreditTransactions.refresh();
            taxCreditTransactions = achDetails.taxCreditTransactions;
            taxCreditTransactionsTotal = achDetails.taxCreditTransactionsTotal;
            
            allTaxTransactionsTotal = taxCreditTransactionsTotal + taxTransactionsTotal;            
        
            achDetails.ddTransactions.sort = mDDSort;
            achDetails.ddTransactions.refresh();
            ddTransactions = achDetails.ddTransactions;            
            ddTransactionsTotal = achDetails.ddTransactionsTotal;

            selectedTransaction=originalSelectedTransaction;
        }
        
        public function viewHPDEReview():void {
        	showDetail = false;
//        	inspector.topics.getTopic(CompanyInspectorTopicEnum.ACTIVATION).activate(); //tax
        }            
	}
}
