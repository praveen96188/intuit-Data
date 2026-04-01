package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAssetInfo")]
    public class AssetInfo {

        public var primary:Boolean;
        public var assisted:Boolean;
        public var assistedSubType:String;
        public var assetCode:String; 


    }
}