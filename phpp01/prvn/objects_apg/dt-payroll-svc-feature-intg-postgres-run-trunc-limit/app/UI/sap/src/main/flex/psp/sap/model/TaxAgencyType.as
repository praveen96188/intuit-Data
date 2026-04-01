package psp.sap.model
{

	public class TaxAgencyType
	{
		public static const IRS:TaxAgencyType = new TaxAgencyType("IRS", "IRS");
		public static const CA_EDD:TaxAgencyType = new TaxAgencyType("CAEDD", "CA-EDD");

		public static const values:Array = [IRS, CA_EDD];

		private var mCode:String;
		private var mLabel:String;

		public function TaxAgencyType(code:String = null, label:String = null)
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

		public static function fromLabel(label:String):TaxAgencyType {
			for each (var enum:TaxAgencyType in values) {
				if (enum.label == label)
					return enum;
			}

			return null;
		}

		public static function valueOf(value:String):TaxAgencyType {
			for each (var enum:TaxAgencyType in values) {
				if (enum.code == value)
					return enum;
			}

			return null;
		}

	}
}