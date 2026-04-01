package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.PItem;

    public class PayrollPItemsTaxabilityViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty(context=true)]
        public var pitem:PItem
        private var mCompanyLaws:ArrayCollection;
        [Bindable]
        public var hasCompanyLaws:Boolean;

        public function PayrollPItemsTaxabilityViewModel() {
            super();
        }

        public static function createActivator(value:PItem, laws:ArrayCollection):Object {
            return {"pitem":value, "companyLaws":laws};
        }

        [Bindable]
        [BackingProperty(context=true)]
        public function get companyLaws():ArrayCollection {
            return mCompanyLaws;
        }

        public function set companyLaws(value:ArrayCollection):void {
            mCompanyLaws = value;
            hasCompanyLaws = (mCompanyLaws!=null) && (mCompanyLaws.length>0);
        }

    }
}