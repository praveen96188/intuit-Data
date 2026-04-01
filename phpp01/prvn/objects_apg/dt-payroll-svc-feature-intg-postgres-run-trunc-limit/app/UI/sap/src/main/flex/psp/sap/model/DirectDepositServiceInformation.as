package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDirectDepositServiceInformation")]
    public class DirectDepositServiceInformation {
        public var totalLimitVoilationCount:Number;
        public var consecutiveLimitVoilationCount:Number;
        public var underwritingPlatform:String;
    }
}