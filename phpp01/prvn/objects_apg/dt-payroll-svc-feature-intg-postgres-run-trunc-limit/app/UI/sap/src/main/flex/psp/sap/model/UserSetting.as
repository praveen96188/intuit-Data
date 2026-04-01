package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUserSetting")]
    public class UserSetting {

        public function UserSetting(key:String=null, value:String=null) {
            this.key = key;
            this.value = value;
        }

        public var key:String;
        public var value:String;
        public var isDefault:Boolean;

        [Transient]
        public function get booleanValue():Boolean {
            return value == "true";
        }

        public function set booleanValue(value:Boolean):void {
            this.value = value ? "true" : "false";
        }
    }
}