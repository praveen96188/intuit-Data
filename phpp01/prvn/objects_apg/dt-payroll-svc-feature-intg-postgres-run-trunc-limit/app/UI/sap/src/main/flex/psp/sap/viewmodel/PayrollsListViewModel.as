package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
    import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.logging.ILogger;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.ClientLoggingTarget;
	import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.ActionEvent;
    import psp.sap.model.PayrollRun;

    public class PayrollsListViewModel
		extends AbstractPartViewModel
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);
		private var mShowEntireHistory:Boolean = false;
		
		public function PayrollsListViewModel()
		{	
			super();
			mPayrollsSort.fields = [new SortField("paycheckDate", false, true)];
			
			reloadOnActivate = false;
            reloadOnSave = true;
		}				
		
		private var mPayrolls:ArrayCollection =  new ArrayCollection();
		private var mPayrollsSort:Sort = new Sort();

		[Bindable]
		public function get payrolls():ArrayCollection {
			return mPayrolls;
		}
		
		public function set payrolls(value:ArrayCollection):void {
			mPayrolls = value;	
			mPayrolls.sort = mPayrollsSort;
			mPayrolls.refresh();
		}
		
		[Bindable]
		public function get showEntireHistory():Boolean {
			return mShowEntireHistory;
		}
		
		public function set showEntireHistory(value:Boolean):void {
			mShowEntireHistory = value;
			refresh();
		}

        protected function get payrollTypes():ArrayCollection {
            var defaultPayrollTypes:ArrayCollection = new ArrayCollection;
            defaultPayrollTypes.addItem("Regular");
            defaultPayrollTypes.addItem("CloudOnly");
            defaultPayrollTypes.addItem("Adjustment");
            defaultPayrollTypes.addItem("FeeOnly");
            return defaultPayrollTypes;
        }

		override protected function loadModelData():void {
			var today:Date = SAP.instance.PSPDate;
			
			var oneYearAgo:Date = SAP.instance.PSPDate;
			oneYearAgo.setFullYear(	today.fullYear - 1, 
									today.month, 
									today.month == 1 && today.date == 29 ? 28 : today.date);

			SAP.instance.payrollRunService.findPayrollRunsByDate(
				this.company.companyId, 
				this.company.sourceSystemCd,
                payrollTypes,
				mShowEntireHistory ? null : oneYearAgo,
				mShowEntireHistory ? null : today,
				createLoadModelDataResponder(onPayrollResults));
			logger.info(" loadModelData called.");
		}
		
		public function onPayrollResults(e:ResultEvent):void {
			payrolls = e.result as ArrayCollection;
			hasPayrolls = payrolls.length > 0;			
			logger.info(" onPayrollResults completed " + payrolls.length + " payrolls in the collection");
		}

		private var mHasPayrolls:Boolean = false;
		[Bindable]
		public function get hasPayrolls():Boolean {
			return mHasPayrolls;
		}
		public function set hasPayrolls(value:Boolean):void {
			mHasPayrolls = value;
		}


        private var targetPayrollRun:PayrollRun;
        public function cancelAdjustment(payrollRun:PayrollRun):void {
            targetPayrollRun = payrollRun;
            forceSave();
        }

        override protected function executeSave():void {
            SAP.instance.payrollRunService.cancelAdjustment(company.companyId, company.sourceSystemCd, targetPayrollRun.sourcePayRunId, createSaveResponder());
        }

        public function createManualFee():void {
            inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_CREATE_FEE).activatePage(PayrollTransactionCreateFeeViewModel.createActivator(null));
        }

        public function performPayrollAction(action:ActionEvent, payrollRun:PayrollRun):void {
            action.performPayrollAction(inspector, payrollRun);
        }
				
	}
}
