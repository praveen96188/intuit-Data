package psp.sap.model
{
    import intuit.sbd.flex.framework.model.EntityObject;
    import intuit.sbd.flex.framework.service.ServiceUtils;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.logging.ILogger;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollRun")]
	public class PayrollRun extends EntityObject
	{
        private var logger:ILogger = ClientLoggingTarget.getLogger(this);

        public var paycheckDate: Date;
        public var payrollNetAmount: Number;
        public var payrollRunDate: Date;
        public var sourcePayRunId: String;
        public var statusEffectiveDate: Date;
        public var paycheckSettlementDate: Date;
        public var payrollRunStatus: String;
        public var companyId: String;
        public var collectionStage: String;
        public var wireExpectedDate: Date;
        public var sourceSystemId: String;        
        public var bankAccount:CompanyBankAccount;        
        public var id:String;
        public var expectedResolutionDate:Date;
        public var hasVoidedPaycheck:Boolean;
        public var HPDE:Boolean;
        public var payrollType:String;
        public var manualCreator:String;
        public var manualNote:String;
        public var employerDDDebitTxnNumber:String;

        public var isSuperseded:Boolean;
        public var isBackdated:Boolean;
        public var hasDDTransactions:Boolean;
        public var hasTaxTransactions:Boolean;
        public var feeOnlyAmount:Number;
        
        [ArrayElementType("psp.sap.model.ActionEvent")]
        private var mActionCollection:ArrayCollection;
        
        [ArrayElementType("psp.sap.model.ActionEvent")]
        public function get actionCollection():ArrayCollection {
        	return mActionCollection;
        }
        
        public function set actionCollection(value:ArrayCollection):void {
			mActionCollection = value;
			
			if (mActionCollection != null) {
				var temp:ArrayCollection = new ArrayCollection(mActionCollection.source);
				var sort:Sort = new Sort();
				sort.fields = [new SortField("description", true)];
				temp.sort = sort;
				temp.refresh();
				
				mActionCollection.removeAll();
				
				mActionCollection.addItem(new ActionEvent(ActionEventCode.VIEW_TRANSACTION, "View Transactions"));
				
				if (SAP.canPerformOperation(OperationsEnum.VIEW_LEDGER))
					mActionCollection.addItem(new ActionEvent(ActionEventCode.VIEW_LEDGER, "View Ledger"));
					
				for each (var actionEvent:ActionEvent in temp)
					mActionCollection.addItem(actionEvent);
						
				mActionCollection.filterFunction = filterOutUnavailable;
				mActionCollection.refresh();
			}
			
											                                                                	
        }
        
        private function filterOutUnavailable(actionEvent:ActionEvent):Boolean {
        	return actionEvent.canPreformAction();
        }

        [Transient]
        public function get isBillPaymentPayroll():Boolean {
            return payrollType == PayrollTypeEnum.BILL_PAYMENT.toString();
        }

        [Transient] public var balanceDue:Number=NaN;
        [Transient] private var isLoadingBalanceDue:Boolean = false;

        public function loadBalance():void {
            if (! isLoadingBalanceDue) {
                SAP.instance.payrollRunService.findPayrollRunBalanceDue(this.sourceSystemId, this.companyId, this.sourcePayRunId, new Responder(onBalanceLoaded, onBalanceFailure) );
                isLoadingBalanceDue = true;
            }
        }

        public static function loadBalance(pr:PayrollRun):void {
            pr.loadBalance();
        }

        private function onBalanceFailure(e:FaultEvent):void {
            balanceDue = NaN;
            logger.error("Failed to load balance: " + ServiceUtils.getFaultDetails(e));
        }

        private function onBalanceLoaded(e:ResultEvent):void {
            balanceDue = Number(e.result);
        }
    }
}
