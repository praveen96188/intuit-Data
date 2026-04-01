package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHEnrollmentHistoryItem")]
    public class ACHEnrollmentHistoryItem extends BaseEnrollmentHistoryItem {
        public function ACHEnrollmentHistoryItem() {
            super();
        }

        public var canDelete:Boolean;
        public var rejectedReason:String;
        public var agencyId:String;
    }
}