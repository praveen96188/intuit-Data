package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEftpsEnrollmentItem")]
    public class EftpsEnrollmentItem extends BaseEnrollmentHistoryItem {

        public var secondaryEnrollment:Boolean;

        public function EftpsEnrollmentItem():void {

        }
    }
}