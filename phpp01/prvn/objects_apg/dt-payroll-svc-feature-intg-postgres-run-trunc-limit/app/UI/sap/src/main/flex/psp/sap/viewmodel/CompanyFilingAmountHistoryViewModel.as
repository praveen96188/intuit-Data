/**
 * User: dweinberg
 * Date: 8/22/12
 * Time: 5:03 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyFilingAmountHistory;
    import psp.sap.model.PaymentTemplate;

    public class CompanyFilingAmountHistoryViewModel extends AbstractPartViewModel{

        [ArrayElementType("psp.sap.model.CompanyFilingAmountHistory")]
        [Bindable] public var filingAmounts:ArrayCollection;

        [ArrayElementType("String")]
        [Bindable] public var additionalAmountNames:ArrayCollection;

        private var mSelectedName:String;

        [Bindable] [BackingProperty(context="true")]
        public var paymentTemplate:PaymentTemplate;

        public function CompanyFilingAmountHistoryViewModel() {
        }

        [Bindable]
        public function get selectedName():String {
            return mSelectedName;
        }

        public function set selectedName(value:String):void {
            mSelectedName = value;
            filingAmounts.refresh();
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getCompanyFilingAmountHistory(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            filingAmounts = ArrayCollection(e.result);

            additionalAmountNames = new ArrayCollection();
            additionalAmountNames.addItem("");

            for each (var filingAmount:CompanyFilingAmountHistory in filingAmounts) {
                if (!additionalAmountNames.contains(filingAmount.name)) {
                    additionalAmountNames.addItem(filingAmount.name);
                }
            }

            selectedName = String(additionalAmountNames.getItemAt(0));
            filingAmounts.filterFunction = filter;
        }

        private function filter(item:CompanyFilingAmountHistory):Boolean {
            if (item == null || item.name == null || item.name == "") {
                return true;
            }
            return item.name.toUpperCase().indexOf(this.selectedName.toUpperCase()) >= 0;

        }



    }
}
