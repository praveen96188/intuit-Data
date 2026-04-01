/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/16/12
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTransaction")]
    public class Transaction {

        public var amount:Number;
        public var settlementDate:Date;
        public var createdBy:String;
        public var transactionId:String;
        public var status:String;
        public var createdDate:Date;
        public var transactionType:String;
        public var settlementType:String;
        public var returnCd:String;

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

                if (SAP.canPerformOperation(OperationsEnum.VIEW_TRANSACTION_HISTORY))
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
