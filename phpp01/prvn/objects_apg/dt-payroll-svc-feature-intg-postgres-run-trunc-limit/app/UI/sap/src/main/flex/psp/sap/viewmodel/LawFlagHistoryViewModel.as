package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.PropertyAudit;
    import psp.sap.view.UIUtils;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class LawFlagHistoryViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty (context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        [Bindable]
        [ArrayElementType("psp.sap.model.PropertyAudit")]
        public var propertyAudits:ArrayCollection;

        [Bindable]
        [ArrayElementType("String")]
        public var laws:ArrayCollection;

        [Bindable]
        public var flags:ArrayCollection = new ArrayCollection(["", "Inactive", "Exempt", "Reimbursable"]);

        [Bindable]
        [BackingProperty]
        public var selectedLaw:String = "";
        [Bindable]
        [BackingProperty]
        public var selectedFlag:String = "";

        public function LawFlagHistoryViewModel() {
            super();
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getLawFlagHistory(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            propertyAudits = e.result as ArrayCollection;
        }

        override protected function initializeBackingProperties():void {
            laws = new ArrayCollection();
            laws.addItem("");
            for each (var propertyAudit:PropertyAudit in propertyAudits) {
                if (!laws.contains(propertyAudit.category)) {
                    laws.addItem(propertyAudit.category);
                }
            }
            laws.sort = UIUtils.singleSort(new SortField(null));

            propertyAudits.filterFunction = function(o:PropertyAudit):Boolean {
                if (selectedLaw != "" && selectedLaw != o.category) {
                    return false;
                } else if (selectedFlag != "" && selectedFlag != o.propertyName) {
                    return false;
                }
                return true;
            };

            this.addEventListener(ViewModelEvent.BACKING_PROPERTY_CHANGED, function(e:Event):void {
                propertyAudits.refresh();
            });
        }


    }
}