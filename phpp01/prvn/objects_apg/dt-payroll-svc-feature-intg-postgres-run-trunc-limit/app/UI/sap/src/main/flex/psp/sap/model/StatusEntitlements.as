package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    public class StatusEntitlements {

        public function StatusEntitlements(status:String) {
            this.status = status;
        }

        public var status:String;
        [ArrayElementType ("psp.sap.model.EntitlementSearchResult")]
        public var entitlements:ArrayCollection = new ArrayCollection();
        
        public function getLabel():String {
            var newLabel:String = status;
            if (entitlements.length > 1) {
                newLabel += " (" + entitlements.length + ")";
            }
            return newLabel;
        }

    }
}