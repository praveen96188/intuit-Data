/**
 * User: dweinberg
 * Date: 11/5/11
 * Time: 9:12 PM
 */
package psp.sap.model {
    import psp.sap.formatters.SAPCurrencyFormatters;
    import psp.sap.formatters.SAPDateFormatters;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncDetailPayrollTransaction")]
    public class DataSyncDetailPayrollTransaction extends DataSyncDetail {

        public var payrollTransactionId:int;
        public var payrollTransactionType:String;
        public var employeeId:String;
        public var employeeName:String;
        public var transactionDate:Date;
        public var amount:Number;


        override public function createNewDataSyncSelectedItem():DataSyncSelectedItem {
            var item:DataSyncSelectedItem = super.createNewDataSyncSelectedItem();
            item.dataSyncDetail = this;
            item.itemType = "Payroll Transaction";
            if (employeeName != null) {
                item.description = separate(payrollTransactionType, employeeId, employeeName, formattedTransactionDate, formattedAmount);
            } else {
                item.description = separate(payrollTransactionType, formattedTransactionDate, formattedAmount);
            }

            item.itemId = this.payrollTransactionId.toString();
            return item;
        }

        public function get formattedTransactionDate():String {
            if (transactionDate == null) {
                return "";
            } else {
                return SAPDateFormatters.dateFormatShort.format(transactionDate);
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
