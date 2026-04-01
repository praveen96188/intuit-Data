package psp.sap.model
{
	import psp.sap.application.SAP;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyNote")]
	public class CompanyNote
	{
        public var id:String;
		public var insertUserId: String;
		public var notes: String;
        public var createdDate: Date;
        public var alert:Boolean;
        public var eventId:String;

		public static function notesCompareFunction(obj1:Object, obj2:Object, fields:Array = null):int {
			var aDate:Date = SAP.instance.PSPDate;
			var bDate:Date = SAP.instance.PSPDate;
			
			if(obj1 == null || obj2 == null) {
				return 0;
			}
			
			if(obj1 is CompanyNote) {
				aDate = (obj1 as CompanyNote).createdDate;	
	  		} else {
	  			aDate = obj1["eventDate"] as Date;
	  		}
	  		
	  		if(obj2 is CompanyNote) {
				bDate = (obj2 as CompanyNote).createdDate;	
	  		} else {
	  			bDate = obj2["eventDate"] as Date;
	  		}
	  		
	  		if(aDate == null) aDate = SAP.instance.PSPDate;
	  		if(bDate == null) bDate = SAP.instance.PSPDate;
	  		
	  		//Force descending for now
	  		if (aDate.time < bDate.time) {
				//return -1; 
				return 1;
			} else if (aDate.time > bDate.time) {
				//return 1;
				return -1;
			} else {
				return 0;
			}
		}
        
    }
}