package psp.sap.model
{
      public class CompanyAgencyStatusEnum
      {
            
            public static const ACTIVE:CompanyAgencyStatusEnum = new CompanyAgencyStatusEnum("Active");
            public static const HOLD:CompanyAgencyStatusEnum = new CompanyAgencyStatusEnum("Hold");
            public static const INACTIVE:CompanyAgencyStatusEnum = new CompanyAgencyStatusEnum("Inactive");
            
            public static const values:Array = [ACTIVE, HOLD, INACTIVE];
            
            private var mCode:String;
            private var mLabel:String;
            
            public function CompanyAgencyStatusEnum(code:String, label:String = null)
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
            
            public static function fromLabel(label:String):CompanyAgencyStatusEnum {
                  for each (var enum:CompanyAgencyStatusEnum in values) {
                        if (enum.label == label)
                              return enum;      
                  }
                  
                  return null;
            }
            
			public static function valueOf(value:String):CompanyAgencyStatusEnum {
				for each (var enum:CompanyAgencyStatusEnum in values) {
					if (enum.code == value)
						return enum;
				}
	
				return null;
			}

      }
}
