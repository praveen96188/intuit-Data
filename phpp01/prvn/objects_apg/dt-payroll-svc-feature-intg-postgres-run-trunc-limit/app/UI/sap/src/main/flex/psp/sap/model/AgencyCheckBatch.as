package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyCheckBatch")]
    public class AgencyCheckBatch extends CheckPrintingBatch {
        public var templateNameLine1:String;
        public var templateNameLine2:String;
        public var initiationDate:Date;
        public var isSuperCheck:Boolean;
    }
}