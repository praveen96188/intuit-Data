package psp.sap.model
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction")]
	public class PayrollTransaction
	{
		public var createdDate:Date;
        public var txnDate:Date;
        public var txnType:String;
        public var settlementType:String;
        public var status:String;
        public var amount:Number;
        public var sourcePayRunId:String;
        public var transactionId:String;
        public var id:String;
        public var credit:Boolean;
        public var returnCd:String;
        public var description:String;
        
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
				
				
				if(SAP.canPerformOperation(OperationsEnum.VIEW_TRANSACTION_HISTORY))
					mActionCollection.addItem(new ActionEvent(ActionEventCode.TX_STATE_HISTORY, "View History"));
				
				for each (var actionEvent:ActionEvent in temp)
				{
					if(actionEvent.code != ActionEventCode.TX_STATE_HISTORY)
						mActionCollection.addItem(actionEvent);
				}
						
				mActionCollection.filterFunction = filterOutUnavailable;
				mActionCollection.refresh();
			}
			
											                                                                	
        }
        
        private function filterOutUnavailable(actionEvent:ActionEvent):Boolean {
        	return actionEvent.canPreformAction();
        }      
        
        
        
        
        [Transient]
	    public function get accountType():String {
	    	return credit ? "Credit" : "Debit";
	    }
	}
}
