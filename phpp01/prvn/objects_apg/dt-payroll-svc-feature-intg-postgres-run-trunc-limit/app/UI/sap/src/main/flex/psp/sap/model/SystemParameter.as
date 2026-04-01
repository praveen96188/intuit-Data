package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSystemParameter")]
    public class SystemParameter {
        public var code:String;
        public var value:String;
        public var description:String;
        public var org:String;

        [Transient]
        public var changed:Boolean;
    }
}