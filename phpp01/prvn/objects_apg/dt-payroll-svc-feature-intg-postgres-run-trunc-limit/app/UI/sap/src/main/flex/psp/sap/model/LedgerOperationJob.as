/**
 * User: dweinberg
 * Date: 11/13/12
 * Time: 9:50 AM
 */
package psp.sap.model {
    import mx.formatters.NumberFormatter;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLedgerOperationJob")]
    public class LedgerOperationJob {
        public var id:String;
        public var uploadTime:Date;
        public var startTime:Date;
        public var finishTime:Date;
        public var status:String;
        public var type:String;
        public var totalRecords:int;
        public var processedRecords:int;
        public var description:String;

        public function get canQueue():Boolean {
            return status == "Created";
        }

        public function get isRunning():Boolean {
            return status == "InProgress";
        }

        public function get isProcessed():Boolean {
            return status == "Complete";
        }

        public function get isDeleted():Boolean {
            return status == "Deleted";
        }

        public function get percentDoneString():String {
            var numberFormatter:NumberFormatter = new NumberFormatter();
            numberFormatter.precision = 1;

            return " - " + numberFormatter.format(processedRecords / totalRecords * 100) + "%";
        }
    }
}
