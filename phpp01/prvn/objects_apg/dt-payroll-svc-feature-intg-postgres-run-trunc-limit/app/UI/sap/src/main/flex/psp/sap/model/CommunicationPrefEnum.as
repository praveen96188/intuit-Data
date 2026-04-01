package psp.sap.model
{
	public class CommunicationPrefEnum
	{
		public static const EMAIL:CommunicationPrefEnum = new CommunicationPrefEnum("Email", "E-mail");
		public static const PHONE:CommunicationPrefEnum = new CommunicationPrefEnum("Phone", "Phone");
		public static const values:Array = [EMAIL, PHONE];
		
		[Bindable] public var code:String;
		[Bindable] public var name:String;
		
		public function CommunicationPrefEnum(code:String, name:String)
		{
			this.code = code;
			this.name = name;
		}
		
		public function toString():String {
			return code;
		}

        public static function getEnumForCode(code:String):CommunicationPrefEnum {
            for each(var value:CommunicationPrefEnum in values){
                if(value.code == code){
                    return value;
                }
            }

            return null;
        }
	}
}