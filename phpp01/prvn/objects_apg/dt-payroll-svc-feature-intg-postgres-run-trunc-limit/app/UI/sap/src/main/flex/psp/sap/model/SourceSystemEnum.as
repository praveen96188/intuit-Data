package psp.sap.model
{

	public class SourceSystemEnum
	{
		public static const CRIS:SourceSystemEnum = new SourceSystemEnum("CRIS");
		public static const PSP:SourceSystemEnum = new SourceSystemEnum("PSP");
		public static const EWS:SourceSystemEnum = new SourceSystemEnum("EWS");
		public static const QBOE:SourceSystemEnum = new SourceSystemEnum("QBOE");
		public static const QBDT:SourceSystemEnum = new SourceSystemEnum("QBDT");
		public static const AS_400:SourceSystemEnum = new SourceSystemEnum("AS400");
		public static const GEMINI:SourceSystemEnum = new SourceSystemEnum("GEMINI");
        public static const IOP:SourceSystemEnum = new SourceSystemEnum("IOP");
        public static const ERS:SourceSystemEnum = new SourceSystemEnum("ERS");
        public static const AMO:SourceSystemEnum = new SourceSystemEnum("AMO");

		public static const connectionLogList:Array = [AMO, EWS, PSP, QBDT];

		public static const values:Array = [AMO, CRIS, PSP, EWS, QBOE, QBDT, AS_400, GEMINI, IOP, ERS];

		private var mCode:String;
		private var mLabel:String;

		public function SourceSystemEnum(code:String = null, label:String = null)
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

		public static function fromLabel(label:String):SourceSystemEnum {
			for each (var enum:SourceSystemEnum in values) {
				if (enum.label == label)
					return enum;
			}

			return null;
		}

		public static function valueOf(value:String):SourceSystemEnum {
			for each (var enum:SourceSystemEnum in values) {
				if (enum.code == value)
					return enum;
			}

			return null;
		}

	}
}
