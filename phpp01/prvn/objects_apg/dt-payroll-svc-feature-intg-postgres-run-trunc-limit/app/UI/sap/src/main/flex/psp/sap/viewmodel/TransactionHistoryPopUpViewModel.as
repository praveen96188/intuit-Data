package psp.sap.viewmodel {
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	import psp.sap.application.SAP;
	import psp.sap.model.BookTransferTransaction;
    import psp.sap.model.Transaction;

    public class TransactionHistoryPopUpViewModel extends AbstractPartViewModel {

		[Bindable]
		public var bookTransferTxn:BookTransferTransaction = null;

        [Bindable]
        public var transaction:Transaction = null;

		[Bindable]
		[ArrayElementType("psp.sap.model.PropertyAudit")]
		public var propertyAudit:ArrayCollection = new ArrayCollection();

        [Bindable]
        public var amount:Number = NaN;

        [Bindable]
        public var settlementDate:Date;

		override protected function loadModelData():void {
			propertyAudit.removeAll();
            var transactionId:String = null;
            settlementDate = null;
            amount = NaN;
            if(bookTransferTxn != null) {
                transactionId = bookTransferTxn.transactionId;
                amount = bookTransferTxn.amount;
                settlementDate = bookTransferTxn.settlementDate;
            } else if (transaction != null){
                transactionId = transaction.transactionId;
                amount = transaction.amount;
                settlementDate = transaction.settlementDate;
            }
            
			SAP.instance.payrollRunService.getTransactionHistory(null, null, transactionId, createLoadModelDataResponder(onDataLoadCompleted));
		}

		protected virtual function onDataLoadCompleted(e:ResultEvent):void {
			propertyAudit = e.result as ArrayCollection;
		}

    }
}
