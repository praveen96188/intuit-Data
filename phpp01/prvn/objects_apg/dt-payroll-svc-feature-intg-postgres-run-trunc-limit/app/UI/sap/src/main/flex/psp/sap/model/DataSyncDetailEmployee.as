package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncDetailEmployee")]
    public class DataSyncDetailEmployee extends DataSyncDetail {
        public var employeeId:String;
        public var employeeName:String;


        override public function createNewDataSyncSelectedItem():DataSyncSelectedItem {
            var item:DataSyncSelectedItem = super.createNewDataSyncSelectedItem();
            item.dataSyncDetail = this;
            item.itemType = "Employee";
            item.description = this.employeeName;
            item.itemId = this.employeeId;
            return item;
        }
    }
}
