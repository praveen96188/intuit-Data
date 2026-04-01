package psp.sap.model
{
    import mx.collections.ArrayCollection;

    import psp.sap.application.CompanyEventHandler;
    import psp.sap.application.SAP;
    import psp.sap.model.companyevents.CompanyEventDetail;
    import psp.sap.model.companyevents.CompanyEventEmail;
    import psp.sap.model.companyevents.EventAs400Sync;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEvent")]
	public class CompanyEventItem
	{
        //public var createdDate: Date;
        public var creatorId: String;	
        public var lastNoteDate: Date;     
		public var eventGroupCode:String;
		
        [ArrayElementType("psp.sap.model.companyevents.CompanyEventDetail")]
        public var mCompanyEventDetails:ArrayCollection;

        [ArrayElementType("psp.sap.model.companyevents.CompanyEventEmail")]
        public var mCompanyEventEmails:ArrayCollection;

        [ArrayElementType("psp.sap.model.companyevents.EventAs400Sync")]
        public var companyEventAs400Syncs:ArrayCollection;

        public var eventDate:Date;
        public var eventTypeCd:String;
        public var eventTypeName:String;
        public var eventTypeDescription:String;
        public var statusCd:String;
        public var statusEffectiveDate:Date;
        public var id:String;
        public var overrideMessage:String;
        public var transmissionId:String;
        
        [Transient]
        public var details:Object = new Object();
        
        public function get companyEventEmails():ArrayCollection {
        	return mCompanyEventEmails;
        }
        
        public function set companyEventEmails(value:ArrayCollection):void {
        	mCompanyEventEmails = value;
        }
        
        public function get companyEventDetails():ArrayCollection {
        	return mCompanyEventDetails;
        }
        
        public function set companyEventDetails(value:ArrayCollection):void {
        	mCompanyEventDetails = value;
        	 		
        	for each (var eventDetail:CompanyEventDetail in mCompanyEventDetails) {
        		details[eventDetail.eventDetailTypeCd] = eventDetail.value;
			}
        }
        
        [Transient]
        public function get builtDescription():String {
        	return eventHandler.buildDescription(eventTypeDescription, this);
        }
        
        [Transient]
        public function get summaryText():String {
        	var noteString:String = "";

            if(overrideMessage != null){
                noteString = overrideMessage;
            }
            else {
                if(eventTypeName != null && eventTypeDescription != null)
                if (eventHandler != null) {
                    noteString = eventTypeName + " - " + eventHandler.buildDescription(eventTypeDescription, this) + "\n";
                } else {
                    noteString = eventTypeName + " - " + eventTypeDescription + "\n";
                }

                if(companyEventDetails != null)
                {
                    for each (var eventDetail:CompanyEventDetail in companyEventDetails) {
                        if(eventDetail != null)
                        {
                            var eventDetailName:String = (eventDetail.name == null) ? "" : eventDetail.name;
                            var eventDetailValue:String = (eventDetail.value == null) ? "" : eventDetail.value;
                            noteString+= "\t" + eventDetailName + " - " + eventDetailValue + "\n";
                        }
                    }
                }

                if (companyEventEmails != null)
                {
                    for each (var eventEmail:CompanyEventEmail in companyEventEmails) {
                        if (eventEmail != null)
                        {
                            noteString += eventEmail.summaryText;
                        }
                    }
                }


                if(companyEventAs400Syncs != null) {
                    for each (var sync:EventAs400Sync in companyEventAs400Syncs) {
                        noteString += sync.summaryText;
                    }
                }

            }

			noteString += "Created by " + creatorId;

			return noteString;
        }          
            
      
        
        private function lowerCaseFirstChar(value:String):String {
        	if(value == null || value.length == 0) return "";
        	if(value.length == 1) return value.charAt(0).toLocaleLowerCase();
        	return value.charAt(0).toLocaleLowerCase() + value.substr(1, value.length - 1);	
        }      
              
              
              
        [Transient]
        public function get children():ArrayCollection {
        	return new ArrayCollection([new CompanyEventItemChild(this)]);
        }
        
              
        [Transient]
        public function get eventTypeString():String {
        	return eventGroupRename(eventGroupCode) + " : " + eventTypeName;
        }
        
        [Transient]
        private function eventGroupRename(value:String):String {
			var groupName:String = value;

			if(groupName != null && groupName.indexOf(" ") == -1)
			{
				var newGroupName:String = "";
				for(var i:int = 0; i < groupName.length; i++)
				{
					if(groupName.substr(i, 1).toUpperCase() == groupName.substr(i, 1) && i+1 != (groupName.length) && groupName.substr(i+1, 1).toUpperCase() != groupName.substr(i+1, 1))
					{
						newGroupName += " " + groupName.substr(i, 1);
					} else {
						newGroupName += groupName.substr(i, 1)
					}
				}
				return newGroupName;
			} else {
				return groupName;
			}
        }
        
        [Transient]
        [Bindable ("propertyChange")]
        public function get noteInserted():Boolean {
        	return (lastNoteDate != null);
        }
		
		[Transient]
		public function get eventDateTime():Number {
			return eventDate.getTime();
		}
		
		[Transient]
		public var eventHandler:CompanyEventHandler;

	}
}