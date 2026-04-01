package psp.sap.model {
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;

	import psp.sap.application.SAP;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBookTransferTransaction")]
	public class BookTransferTransaction {

		public var fromAccount:String;
		public var toAccount:String;
		public var amount:Number;
		public var settlementDate:Date;
		public var createdBy:String;
		public var transactionId:String;
		public var status:String;
		public var createdDate:Date;

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

				mActionCollection.addItem(new ActionEvent(ActionEventCode.TX_STATE_HISTORY, "View History"));

				for each (var actionEvent:ActionEvent in temp) {
					if (actionEvent.code != ActionEventCode.TX_STATE_HISTORY)
						mActionCollection.addItem(actionEvent);
				}

				mActionCollection.refresh();
			}
		}
	}
}
