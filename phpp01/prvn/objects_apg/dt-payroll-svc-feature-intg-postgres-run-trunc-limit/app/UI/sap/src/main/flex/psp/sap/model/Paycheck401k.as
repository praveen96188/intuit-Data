package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaycheck401k")]
    public class Paycheck401k {
        public var voidedAfterTOKOffload:Boolean;
        public var deletedAfterTOKOffload:Boolean;
        public var tokStatus:String;
        public var dateSentToTOK:Date;

    }
}