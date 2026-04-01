package psp.sap.model.companyevents
{

    import mx.collections.ArrayCollection;
    
    import psp.sap.application.SAP;
    import psp.sap.model.User;

    public class CompanyEvent {
         //public var createdDate: Date;
        
        [Transient]         
        public var groupEventType: String = "";
        
        public var id:String;
        public var creatorId: String;	
        public var lastNoteDate: Date;     

        public var eventDate:Date;
        public var eventTypeCd:String;
        public var eventTypeName:String;
        public var eventTypeDescription:String;
        public var eventGroupCode:String;
        public var statusCd:String;
        public var statusEffectiveDate:Date;

        [ArrayElementType("psp.sap.model.companyevents.EventAs400Sync")]
        public var companyEventAs400Syncs:ArrayCollection;

        [Transient]
        public var details:Object = new Object();
        
        [Transient]
        [ArrayElementType("psp.sap.model.companyevents.CompanyEventDetail")]
        private var mEventDetails:ArrayCollection = new ArrayCollection();
        
            
        public function get companyEventDetails():ArrayCollection {
        	return mEventDetails;
        }
        
        public function set companyEventDetails(value:ArrayCollection):void {
        	mEventDetails = value;
        	 		
        	for each (var eventDetail:CompanyEventDetail in mEventDetails) {
        		details[eventDetail.eventDetailTypeCd] = eventDetail.value;
			}
			
        }
        
        [Transient]
        public function get summaryText():String {
        	var noteString:String = "";
        	
        	if(eventTypeName != null && eventTypeDescription != null)
        		noteString = eventTypeName + " - " + eventTypeDescription + "\n";

			if(companyEventDetails != null)
			{
				for each (var eventDetail:CompanyEventDetail in companyEventDetails) {
	        		noteString+= "\t" + eventDetail.name + " - " + eventDetail.value + "\n";
				}
			}            
			
			return noteString;
        }          
            
      
        
        private function lowerCaseFirstChar(value:String):String {
        	if(value == null || value.length == 0) return "";
        	if(value.length == 1) return value.charAt(0).toLocaleLowerCase();
        	return value.charAt(0).toLocaleLowerCase() + value.substr(1, value.length - 1);	
        }      
              
              
              
              
        [Transient]
        public function get children():ArrayCollection {
        	return new ArrayCollection([summaryText]);
        }
        
              
        [Transient]
        public function get eventTypeString():String {
        	return groupEventType + " : " + eventTypeName;
        }
        
        
        [Transient]
        public function get noteInserted():Boolean {
        	return (lastNoteDate != null);
        }

         /*   
        public var eventDate:Date;
        public var eventTypeCd:String;
        public var eventTypeName:String;
        public var eventTypeDescription:String;
        public var statusCd:String;
        public var statusEffectiveDate:Date;
        public var lastNoteDate:Date;
        public var creatorId:String;
        
        [Transient]
        public var details:Object = new Object();
        
        [Transient]
        [ArrayElementType("psp.sap.model.companyevents.CompanyEventDetail")]
        private var mEventDetails:ArrayCollection = new ArrayCollection();
        
            
        public function get companyEventDetails():ArrayCollection {
        	return mEventDetails;
        }
        
        public function set companyEventDetails(value:ArrayCollection):void {
        	mEventDetails = value;
        	 		
        	for each (var eventDetail:CompanyEventDetail in mEventDetails) {
        		details[eventDetail.eventDetailTypeCd] = eventDetail.value;
			}
			
        }
        
        private function lowerCaseFirstChar(value:String):String {
        	if(value == null || value.length == 0) return "";
        	if(value.length == 1) return value.charAt(0).toLocaleLowerCase();
        	return value.charAt(0).toLocaleLowerCase() + value.substr(1, value.length - 1);	
        }
    }*/
     
       
    }
}
