package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBillPaymentServiceInformation")]
    public class BillPaymentServiceInformation {
        public var totalLimitVoilationCount:Number;
        public var consecutiveLimitVoilationCount:Number;
    }
}