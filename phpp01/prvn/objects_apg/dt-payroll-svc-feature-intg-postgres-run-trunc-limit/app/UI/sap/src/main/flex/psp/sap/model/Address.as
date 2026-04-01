package psp.sap.model
{
    import flash.events.EventDispatcher;

    import mx.utils.StringUtil;

    import org.as3commons.reflect.Field;
    import org.as3commons.reflect.Type;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAddress")]
	[CopyTestCase(nullOk="addressLine2",nullOk="addressLine3",nullOk="country",nullOk="zipCodeExtension")]
	public class Address extends EventDispatcher
	{
        public var description:String;

		public var addressLine1 : String;	

        private var mAddressLine2 : String;
        public function get addressLine2():String {
            return isEmptyString(mAddressLine2) ? null : mAddressLine2;
        }

        public function set addressLine2(value:String):void {
            mAddressLine2 = value;
        }

		public var addressLine3 : String;	
		public var city : String;	
		public var state : String;	
		public var zipCode : String;

        private var mZipCodeExtension : String;
        public function get zipCodeExtension():String {
            return isEmptyString(mZipCodeExtension) ? null : mZipCodeExtension;
        }

        public function set zipCodeExtension(value:String):void {
            mZipCodeExtension = value;
        }
        
		public var country : String;
		
		override public function toString():String {
			var address:String = "";
			
			address += addressLine1;
			if (addressLine2 != null) address += "\n" + addressLine2;
			if (addressLine3 != null) address += "\n" + addressLine3;
			address += "\n" + city + ", " + state + " " + zipCode;
			if (zipCodeExtension != null) address += "-" + zipCodeExtension;
			if (country != null) address += "\n" + country;
			
			return address;
		}
		
 

        private function isEmptyString(value:String):Boolean {
            return (value == null || StringUtil.trim(value).length == 0);
        }

        public function replaceEmptyWithNull():void {
            //speed over type safety today =/
            for each (var field:Field in Type.forInstance(this).fields) {
                if (field.name in this && this[field.name] == "") {
                    this[field.name] = null;
                }                
            }
        }
	}
}