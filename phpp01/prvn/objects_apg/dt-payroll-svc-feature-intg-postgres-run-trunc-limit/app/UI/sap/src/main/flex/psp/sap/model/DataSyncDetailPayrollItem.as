package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncDetailPayrollItem")]
    public class DataSyncDetailPayrollItem extends DataSyncDetail {
        public var payrollItemId:int;
        public var payrollItemName:String;
        public var payrollItemType:String;
        public var EE:Boolean;
        public var inactive:Boolean;

        override public function createNewDataSyncSelectedItem():DataSyncSelectedItem {
            var item:DataSyncSelectedItem = super.createNewDataSyncSelectedItem();
            item.dataSyncDetail = this;
            item.description = separate(
                    payrollItemName,
                    payrollItemType,
                    this.EE ? "EE" : "",
                    inactive ? "Inactive" : "");

            item.itemId = new String(this.payrollItemId);
            item.itemType = "Payroll Item";
            return item;
        }
    }
}