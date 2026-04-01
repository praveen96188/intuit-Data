package psp.sap.model {
    public class PayrollTypeEnum {
        public static const REGULAR:PayrollTypeEnum = new PayrollTypeEnum("Regular");
        public static const ADJUSTMENT:PayrollTypeEnum = new PayrollTypeEnum("Adjustment");
        public static const BILL_PAYMENT:PayrollTypeEnum = new PayrollTypeEnum("BillPayment");

        private var mCode:String;

		public function PayrollTypeEnum(code:String)
		{
			mCode = code;
		}

		public function get code():String {
			return mCode;
		}

        public function toString():String {
			return code;
		}
    }
}