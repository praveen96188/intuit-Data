package psp.sap.model
{

    //most are from the model with the generator, but some are added manually because they exist in the UI only

    public class ActionEventCode
    {
        public static const FINANCIAL_TRANSACTION_VOID_TX:ActionEventCode = new ActionEventCode("FinancialTransactionVoidTx");
		public static const FINANCIAL_TRANSACTION_CANCEL:ActionEventCode = new ActionEventCode("FinancialTransactionCancel");
		public static const ISSUE_REISSUE_REFUND_ER:ActionEventCode = new ActionEventCode("IssueReissueRefundEr");
		public static const TX_STATE_HISTORY:ActionEventCode = new ActionEventCode("TxStateHistory");
		public static const DD_TRANSACTION_CANCEL:ActionEventCode = new ActionEventCode("DDTransactionCancel");
		public static const DD_TRANSACTION_REVERSE:ActionEventCode = new ActionEventCode("DDTransactionReverse");
		public static const DD_REDEBIT_ADD:ActionEventCode = new ActionEventCode("DDRedebitAdd");
		public static const DD_REDEBIT_RECORD:ActionEventCode = new ActionEventCode("DDRedebitRecord");
		public static const ER_FEE_ADD:ActionEventCode = new ActionEventCode("ERFeeAdd");
        public static const ER_FEE_CANCEL:ActionEventCode = new ActionEventCode("ERFeeCancel");
        public static const ER_PAYABLE_CANCEL:ActionEventCode = new ActionEventCode("RefundERPayableCancel");
		public static const BAD_DEBT_WRITE_OFF:ActionEventCode = new ActionEventCode("BadDebtWriteOff");
		public static const BAD_DEBT_RECOVER:ActionEventCode = new ActionEventCode("BadDebtRecover");
		public static const EE_RETURN_TRANSFER:ActionEventCode = new ActionEventCode("EEReturnTransfer");
		public static const FEE_TRANSFER:ActionEventCode = new ActionEventCode("FeeTransfer");
		public static const INTUIT_5_DAY_RETURN_TRANSFER:ActionEventCode = new ActionEventCode("Intuit5DayReturnTransfer");
		public static const DD_REFUND:ActionEventCode = new ActionEventCode("DDRefund");
		public static const ER_RETURN_REFUND:ActionEventCode = new ActionEventCode("ERReturnRefund");
		public static const EE_RETURN_REFUND:ActionEventCode = new ActionEventCode("EEReturnRefund");
		public static const ER_WIRE_EXPECTED:ActionEventCode = new ActionEventCode("ERWireExpected");
		public static const REFUND_REBILL_FEE:ActionEventCode = new ActionEventCode("RefundRebillFee");
		public static const DD_REDEBIT_EDIT:ActionEventCode = new ActionEventCode("DDRedebitEdit");
		public static const ER_FRAUD_OR_ESCALATION_REFUND:ActionEventCode = new ActionEventCode("ERFraudOrEscalationRefund");
		public static const BAD_DEBT_WRITE_OFF_EE_RETURN:ActionEventCode = new ActionEventCode("BadDebtWriteOffEEReturn");
		public static const RECORD_PREFUNDING_WIRE:ActionEventCode = new ActionEventCode("RecordPrefundingWire");
		public static const CANCEL_ADJUSTMENT:ActionEventCode = new ActionEventCode("CancelAdjustment");
		public static const VOID_PAYROLL_TAX_PAYMENT:ActionEventCode = new ActionEventCode("VoidPayrollTaxPayment");
		public static const REISSUE_PAYROLL_TAX_PAYMENT:ActionEventCode = new ActionEventCode("ReissuePayrollTaxPayment");
		public static const APPLY_ER_PAYABLE_TO_BALANCE_DUE:ActionEventCode = new ActionEventCode("ApplyERPayableToBalanceDue");
        public static const REFUND_DEBIT:ActionEventCode = new ActionEventCode("RefundDebit");
        public static const VOID_TOR:ActionEventCode = new ActionEventCode("VoidTORTransaction");


        //manual
        public static const VIEW_LEDGER:ActionEventCode = new ActionEventCode("ViewLedger");
        public static const VIEW_TRANSACTION:ActionEventCode = new ActionEventCode("ViewTransaction");
        public static const VIEW_PAYCHECK_LINE_ITEMS:ActionEventCode = new ActionEventCode("ViewPaycheckLineItems");        
        public static const VIEW_PAYROLLS:ActionEventCode = new ActionEventCode("ViewPayrolls");
        public static const VIEW_EE_HISTORY:ActionEventCode = new ActionEventCode("ViewEEHistory");
        public static const REFUND_ER_PAYABLE:ActionEventCode = new ActionEventCode("RefundERPayable");


		public static const values:Array = [FINANCIAL_TRANSACTION_VOID_TX, VOID_TOR, FINANCIAL_TRANSACTION_CANCEL, ISSUE_REISSUE_REFUND_ER, TX_STATE_HISTORY, DD_TRANSACTION_CANCEL, DD_TRANSACTION_REVERSE, DD_REDEBIT_ADD, DD_REDEBIT_RECORD, ER_FEE_ADD, BAD_DEBT_WRITE_OFF, BAD_DEBT_RECOVER, EE_RETURN_TRANSFER, FEE_TRANSFER, INTUIT_5_DAY_RETURN_TRANSFER, DD_REFUND, ER_RETURN_REFUND, EE_RETURN_REFUND, ER_WIRE_EXPECTED, REFUND_REBILL_FEE, DD_REDEBIT_EDIT, ER_FRAUD_OR_ESCALATION_REFUND, BAD_DEBT_WRITE_OFF_EE_RETURN, RECORD_PREFUNDING_WIRE, CANCEL_ADJUSTMENT, VOID_PAYROLL_TAX_PAYMENT, REISSUE_PAYROLL_TAX_PAYMENT, APPLY_ER_PAYABLE_TO_BALANCE_DUE, REFUND_DEBIT, ER_FEE_CANCEL, ER_PAYABLE_CANCEL]
                .concat(VIEW_LEDGER, VIEW_TRANSACTION, VIEW_PAYCHECK_LINE_ITEMS, VIEW_PAYROLLS, VIEW_EE_HISTORY, REFUND_ER_PAYABLE);



        private var mCode:String;
        private var mLabel:String;

        public function ActionEventCode(code:String = null, label:String = null)
        {
            mCode = code;
            mLabel = (label != null ? label : code);
        }

        public function get code():String {
            return mCode;
        }

        public function get label():String {
            return mLabel;
        }

        public function toString():String {
            return label;
        }

        public static function fromLabel(label:String):ActionEventCode {
            for each (var enumObj:ActionEventCode in values) {
                if (enumObj.label == label)
                    return enumObj;
            }

            return null;
        }

        public static function valueOf(value:String):ActionEventCode {
            for each (var enumObj:ActionEventCode in values) {
                if (enumObj.code == value)
                    return enumObj;
            }

            return null;
        }

    }
}