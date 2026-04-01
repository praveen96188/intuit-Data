package psp.sap.application.enums {
    public class CheckPrintBatchStatusEnum {
        public static const All:CheckPrintBatchStatusEnum = new CheckPrintBatchStatusEnum("All");
        public static const PENDING:CheckPrintBatchStatusEnum = new CheckPrintBatchStatusEnum("Pending");
        public static const SENT_TO_PRINTER:CheckPrintBatchStatusEnum = new CheckPrintBatchStatusEnum("SentToPrinter", "Sent To Printer");
        public static const ERROR:CheckPrintBatchStatusEnum = new CheckPrintBatchStatusEnum("Error");

		public static const values:Array = [PENDING, SENT_TO_PRINTER, ERROR];
        public static const filterValues:Array = [All, PENDING, SENT_TO_PRINTER, ERROR];        
        public static const pendingValues:Array = [PENDING];        

        private var mCode:String;
		private var mLabel:String;

		public function CheckPrintBatchStatusEnum(code:String = null, label:String = null)
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

		public static function fromLabel(label:String):CheckPrintBatchStatusEnum {
			for each (var value:CheckPrintBatchStatusEnum in values) {
				if (value.label == label)
					return value;
			}

			return null;
		}

		public static function valueOf(code:String):CheckPrintBatchStatusEnum {
			for each (var value:CheckPrintBatchStatusEnum in values) {
				if (value.code == code)
					return value;
			}

			return null;
		}
    }
}
