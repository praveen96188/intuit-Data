package psp.sap.model
{
	public class SettlementTypeEnum
	{
		public static const ACH:SettlementTypeEnum = new SettlementTypeEnum("ACH");
		public static const WIRE:SettlementTypeEnum = new SettlementTypeEnum("Wire");
		public static const CASH:SettlementTypeEnum = new SettlementTypeEnum("Cash");
		public static const CHECK:SettlementTypeEnum = new SettlementTypeEnum("CheckType", "Check");
		public static const OTHER:SettlementTypeEnum = new SettlementTypeEnum("Other");
		
		public static const values:Array = [ACH, WIRE, CHECK, CASH, OTHER];		
		public static const non_ach_values:Array = [WIRE, CHECK, CASH, OTHER];
		public static const ach_wire:Array = [ACH, WIRE];
		public static const payment_type:Array = [CHECK, WIRE];
        public static const ach_check_wire:Array = [ACH, CHECK, WIRE];


		private var mCode:String;
		private var mLabel:String;
		
		public function SettlementTypeEnum(code:String, label:String = null)
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
		
		public static function fromLabel(label:String):SettlementTypeEnum {
			for each (var enum:SettlementTypeEnum in values) {
				if (enum.label == label)
					return enum;	
			}
			
			return null;
		}

	}
}