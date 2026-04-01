package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class TaxPaymentMethodEnum
	{
        public static const EFTPS:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("EFTPS", "EFTPS");
        public static const EDI:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("EDI", "EDI");
        public static const EFTPS_DIRECT_DEBIT:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("EFTPSDirectDebit", "EFTPS Dir");
        public static const RECORDED:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("Recorded", "Recorded");
		public static const ACH:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("ACHCredit", "ACH Credit");
		public static const CHECK:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("CheckPayment", "Check");
		public static const WIRE:TaxPaymentMethodEnum = new TaxPaymentMethodEnum("WirePayment", "Wire");
		public static const BLANK:TaxPaymentMethodEnum = new TaxPaymentMethodEnum(null, "");		
		
		public static const values:ArrayCollection = new ArrayCollection([ACH, CHECK, WIRE, EFTPS, EFTPS_DIRECT_DEBIT, EDI]);
		public static const values_with_blank:ArrayCollection = new ArrayCollection([BLANK, ACH, CHECK, WIRE, EFTPS, EFTPS_DIRECT_DEBIT, EDI]);
		public static const non_ach_values:ArrayCollection = new ArrayCollection([CHECK, WIRE]);		
		
		private var mCode:String;
		private var mLabel:String;
		
		public function TaxPaymentMethodEnum(code:String, label:String = null)
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
		
		public static function fromCode(code:String):TaxPaymentMethodEnum {
			for each (var enum:TaxPaymentMethodEnum in values) {
				if (enum.code == code)
					return enum;	
			}
			
			return null;
		}

	}
}