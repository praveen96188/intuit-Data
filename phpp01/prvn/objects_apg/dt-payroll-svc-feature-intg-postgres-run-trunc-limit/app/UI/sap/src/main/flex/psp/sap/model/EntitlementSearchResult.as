package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementSearchResult")]
    public class EntitlementSearchResult {

        public var id:String;

        public var licenseNumber:String;
        public var eoc:String;

        public var key:CompanyKey;
        public var legalName:String;
        public var fein:String;
        public var PSID:String;

        public var entitlementUnitStatus:String;

        public var entitlementStatus:String;

        public var serviceKey:String;

        public var assetInfo:AssetInfo;

        public var companyServiceState:CompanyServiceState;

        public var subtypeDescription:String;
        
        [Transient]
        public var actionCollection:ArrayCollection = new ArrayCollection();

        public function get isActivated():Boolean {
            return entitlementUnitStatus == "Activated" || entitlementUnitStatus == "PendingActivation" || entitlementUnitStatus == "PendingReactivation" || entitlementUnitStatus == "ActivationHold";
        }

        public function get isDeactivated():Boolean {
            return entitlementUnitStatus == "Deactivated" || entitlementUnitStatus == "PendingDeactivation";
        }

        public function get isHistoric():Boolean {
            return entitlementUnitStatus == "Historic";
        }

        public function get companyServiceStateCd():String {
            if (companyServiceState == null) {
                return null;
            }
            return companyServiceState.code;
        }

        public function set companyServiceStateCd(value:String):void {
            if (value == null) {
                companyServiceState = null;
            }
            companyServiceState = CompanyServiceState.valueOf(value);
        }


    }
}