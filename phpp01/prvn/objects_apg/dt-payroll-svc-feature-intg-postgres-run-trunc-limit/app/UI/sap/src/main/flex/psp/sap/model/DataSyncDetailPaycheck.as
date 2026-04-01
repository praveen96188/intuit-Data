/**
 * User: dweinberg
 * Date: 11/5/11
 * Time: 9:08 PM
 */
package psp.sap.model {
    import psp.sap.formatters.SAPCurrencyFormatters;
    import psp.sap.formatters.SAPDateFormatters;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncDetailPaycheck")]
    public class DataSyncDetailPaycheck extends DataSyncDetail {
        public var paycheckId:int;
        public var employeeId:String;
        public var employeeName:String;
        public var checkDate:Date;
        public var checkNumber:String;
        public var amount:Number;
        public var paycheckType:String;
        override public function createNewDataSyncSelectedItem():DataSyncSelectedItem {
            var item:DataSyncSelectedItem = super.createNewDataSyncSelectedItem();
            item.dataSyncDetail = this;
            item.itemType = "Paycheck";
            item.description = separate(employeeId, employeeName, formattedCheckDate, checkNumber, formattedAmount, paycheckType);
            item.itemId = this.paycheckId.toString();
            return item;
        }

        public function get formattedCheckDate():String {
            if (checkDate == null) {
                return "";
            } else {
                return SAPDateFormatters.dateFormatShort.format(checkDate);
            }
        }

        public function get formattedAmount():String {
            if (isNaN(amount)) {
                return "";
            } else {
                return SAPCurrencyFormatters.defaultFormatter.format(amount);
            }
        }
    }
}
