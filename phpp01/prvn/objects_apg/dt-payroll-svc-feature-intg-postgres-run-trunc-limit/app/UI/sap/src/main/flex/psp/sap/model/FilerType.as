package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFilerType")]
    public class FilerType {
        public var filerType:String;
        public var effectiveQuarter:Quarter;
        public var modifiedDate:Date;
        public var invalidDate:Date;
        public var modifierId:String;
    }
}