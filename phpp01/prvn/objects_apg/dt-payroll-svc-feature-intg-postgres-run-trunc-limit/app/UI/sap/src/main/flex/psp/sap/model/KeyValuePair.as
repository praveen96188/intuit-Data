package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPKeyValuePair")]
    public class KeyValuePair {

        public function KeyValuePair(key:String=null, value:String=null) {
            this.key = key;
            this.value = value;
        }

        public var key:String;
        public var value:String;

    }
}