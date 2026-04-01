package psp.sap.model
{
      public class LawItemStatusEnum
      {
            
            public static const ACTIVE:LawItemStatusEnum = new LawItemStatusEnum("Active");
            public static const EXEMPT:LawItemStatusEnum = new LawItemStatusEnum("Exempt");
            public static const INACTIVE:LawItemStatusEnum = new LawItemStatusEnum("Inactive");
            
            public static const values:Array = [ACTIVE, EXEMPT, INACTIVE];
            
            [Bindable]
            public var code:String;
            [Bindable]
            public var label:String;
            
            public function LawItemStatusEnum(code:String=null, label:String = null)
            {
                  this.code = code;
                  this.label = (label != null ? label : code);
            }
            
            public function toString():String {
                  return label;
            }
            
            public static function fromLabel(label:String):LawItemStatusEnum {
                  for each (var enum:LawItemStatusEnum in values) {
                        if (enum.label == label)
                              return enum;      
                  }
                  
                  return null;
            }
            
			public static function valueOf(value:String):LawItemStatusEnum {
				for each (var enum:LawItemStatusEnum in values) {
					if (enum.code == value)
						return enum;
				}
	
				return null;
			}

      }
}