package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPManualLedgerLimit")]
    public class ManualLedgerLimit {
        public var warningLimit:Number;
        public var blockLimit:Number;
        public var limitEnabled:Boolean;
    }
}