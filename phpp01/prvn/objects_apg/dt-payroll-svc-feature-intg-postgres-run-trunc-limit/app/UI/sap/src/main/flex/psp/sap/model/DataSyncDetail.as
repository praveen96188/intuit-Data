package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncDetail")]
    public class DataSyncDetail {

        [Transient]
        public var selected:Boolean;

        public var detailId:String; //  to hold item GUID
        public var token:int;
        //these fields are used only in data grid data field
        //noinspection JSUnusedGlobalSymbols
        public var memo:String;
        //noinspection JSUnusedGlobalSymbols
        public var classString:String;
        //noinspection JSUnusedGlobalSymbols
        public var isDeleted:Boolean;

        public var isQBOnly:Boolean;

        public function createNewDataSyncSelectedItem():DataSyncSelectedItem {
            var dataSyncSelectedItem:DataSyncSelectedItem = new DataSyncSelectedItem();
            dataSyncSelectedItem.dataSyncDetail = this;
            dataSyncSelectedItem.token = this.token;
            return dataSyncSelectedItem;
        }

        protected function separate(...rest):String {
            var string:String = separate_internal(rest);
            if (string == "" || string.charAt(string.length -1) != " ") {
              return string
            } else {
                return string.substr(0, string.length - 1);
            }
        }

        private function separate_internal(rest:Array):String {
            if (rest.length == 0) {
                return "";
            }

            var text:String = rest.shift();
            if (text == null || text == "") {
                return separate_internal(rest);
            } else {
                return text + " " + separate_internal(rest);
            }
        }

    }
}