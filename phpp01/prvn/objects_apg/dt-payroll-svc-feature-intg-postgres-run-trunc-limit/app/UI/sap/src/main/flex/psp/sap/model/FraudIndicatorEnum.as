package psp.sap.model
{
	public class FraudIndicatorEnum
	{
		public static const ALL:FraudIndicatorEnum = new FraudIndicatorEnum("All", "All Indicators");
		public static const SIGN_UP:FraudIndicatorEnum = new FraudIndicatorEnum("SignUp", "Sign Up");
		public static const PAYROLL:FraudIndicatorEnum = new FraudIndicatorEnum("Payroll");
		
		public static const LIST:Array = [ALL, SIGN_UP, PAYROLL];				
		
		private var mCode:String;
		private var mLabel:String;
		
		public function FraudIndicatorEnum(code:String, label:String = null)
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
		
		public static function getLabelFromCode(code:String):String {
			for each (var enum:FraudIndicatorEnum in LIST) {
				if (enum.code == code){
					return enum.label;
				}						
			}
			
			return code;
		}			

	}
}