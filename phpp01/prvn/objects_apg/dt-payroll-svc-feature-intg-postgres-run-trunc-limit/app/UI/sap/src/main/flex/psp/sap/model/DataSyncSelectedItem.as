package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncSelectedItem")]
    public class DataSyncSelectedItem {
        public var dataSyncDetail:DataSyncDetail;
        
        public var itemId:String;
        public var itemType:String;
        public var token:int;
        public var description:String;
    }
}