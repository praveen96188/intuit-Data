/**
 * User: dweinberg
 * Date: 9/27/13
 * Time: 12:34 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSUICreditsJob")]
    public class SUICreditsJob {
        public var id:String;
        public var quarter:Quarter;
        public var paymentTemplate:String;
        public var status:String;
        public var createdDate:Date;
        public var modifiedDate:Date;
        public var processedFileExists:Boolean;

        public function isProcessed():Boolean {
            return status == "Complete";
        }

        public function isDownloadEnabled():Boolean {
            return processedFileExists;
        }
        
    }
}
