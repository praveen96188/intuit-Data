package psp.sap.model {
    import psp.sap.application.enums.CheckPrintBatchStatusEnum;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCheckPrintingBatch")]
    public class CheckPrintingBatch {
        public var printBatchId:String;
        public var ein:String;
        public var psid:String;
        public var legalName:String;
        public var paycheckDate:Date;
        public var sentToPrinterDate:Date;
        public var paycheckCount:Number;
        public var printStatus:String;
        public var printMessage:String;
        public var minPaycheckId:String;
        public var maxPaycheckId:String;
        public var companyKey:CompanyKey;

        [Transient]
        public function get printStatusEnum():CheckPrintBatchStatusEnum {
            return CheckPrintBatchStatusEnum.valueOf(printStatus);
        }
    }
}
