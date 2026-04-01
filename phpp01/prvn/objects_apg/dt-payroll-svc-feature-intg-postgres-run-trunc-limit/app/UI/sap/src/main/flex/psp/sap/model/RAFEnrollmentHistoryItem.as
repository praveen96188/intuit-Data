package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRAFEnrollmentHistoryItem")]
    public class RAFEnrollmentHistoryItem extends BaseEnrollmentHistoryItem {
        public function RAFEnrollmentHistoryItem() {
            super();
        }

        public var filerType:String;
        public var firstFilingQuarter:String;
        public var canDelete:Boolean;
        public var rejectedReason:String;
    }
}