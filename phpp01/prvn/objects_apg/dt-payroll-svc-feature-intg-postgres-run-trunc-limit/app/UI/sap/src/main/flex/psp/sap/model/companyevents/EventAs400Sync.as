package psp.sap.model.companyevents {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEventAs400Sync")]
    public class EventAs400Sync {
        public var id:String;
        public var status:String;
        public var retryCount:int;

        [Transient]
        public function get summaryText():String {
            var text:String = "AS/400 Sync Status - ";
            text += status;
            if (retryCount > 0) {
                text += " (" + retryCount + " retries)";
            }
            text += "\n";
            return text;

        }
    }
}