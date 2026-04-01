package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.PayrollTransaction;

    public class PayrollTransactionHistoryViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty(context=true)] public var payrollTransaction:PayrollTransaction;

        [Bindable]
        [ArrayElementType("psp.sap.model.PropertyAudit")]
        public var propertyAudit:ArrayCollection = new ArrayCollection();


        public function PayrollTransactionHistoryViewModel() {
            this.shallowCopyFields = ["id", "createdDate", "txnType"];
        }

        public static function createActivator(payrollTransaction:PayrollTransaction):Object {
            return {"payrollTransaction":payrollTransaction};
        }

        [Bindable("propertyChange")]
        public function get companyName():String {
            return company.DBA;
        }

        override protected function loadModelData():void {            
            propertyAudit.removeAll();

            SAP.instance.payrollRunService.getTransactionHistory(	company.sourceSystemCd,
                    company.companyId,
                    payrollTransaction.id,
                    createLoadModelDataResponder(onDataLoadCompleted));
        }

        protected virtual function onDataLoadCompleted(e:ResultEvent):void {
            propertyAudit = e.result as ArrayCollection;
        }

    }
}