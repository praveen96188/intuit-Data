package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxTransaction")]
	public class TaxTransaction
	{
		public var txnDescription:String;
		public var paymentStatus:String;
		public var submissionDate:Date;
		public var checkPaymentDate:Date;
		public var paymentMethod:String;
		public var currentTaxes:Number;
		public var currentWages:Number;
		public var QTDTaxes:Number;
		public var QTDWages:Number;
		public var YTDTaxes:Number;
		public var YTDWages:Number;
		public var quarter:int;
        public var isLastLineInQuarter:Boolean;
        public var manualLedgerCreator:String;
        public var manualLedgerMemo:String;

		public var isSummary:Boolean;
        public var moneyMovementTransactionId:String;
        public var payrollRunId:String;
        public var voidId:String;
        public var lawId:String;
        public var templateCd:String;
        public var isReconcilingAdjustment:Boolean;
        public var payment:Payment;

        [Transient]
        public function get showCurrentWages():Boolean {
            return affectsTaxes && !isSummary;
        }
        [Transient]
        public function get showQTDYTDTaxes():Boolean {
            return affectsTaxes || isLastLineInQuarter;
        }
        [Transient]
        public function get showQTDYTDWages():Boolean {
            return (affectsTaxes || isLastLineInQuarter) && !isSummary;
        }
        [Transient]
        public function get affectsTaxes():Boolean {
            return txnDescription == "Payroll" || txnDescription == "Adjustment";
        }
        [Transient]
        public function get showEmployeeDetailLink():Boolean {
            return affectsTaxes;
        }


	}
}
