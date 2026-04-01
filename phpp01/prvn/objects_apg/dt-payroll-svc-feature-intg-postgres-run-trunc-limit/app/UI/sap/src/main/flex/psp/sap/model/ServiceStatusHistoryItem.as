package psp.sap.model
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	
	import psp.sap.application.SAP;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyServiceStatusHistoryItem")]
	public class ServiceStatusHistoryItem
	{
        public var oldServiceStatus:String;
		public var newServiceStatus:String;
		public var changedBy:String;
		public var changeDate:Date;
        public var serviceCd:String;
		
		public var newSubStatuses:ArrayCollection = new ArrayCollection();		
		public var oldSubStatuses:ArrayCollection = new ArrayCollection();
		
		[Bindable("propertyChange")]
		[Transient]
		public function get wasOnHold():Boolean {
			return oldSubStatuses.length > 0;
		}
		
		[Bindable("propertyChange")]
		[Transient]
		public function get isOnHold():Boolean {
			return newSubStatuses.length > 0;
		}
		
		[Bindable("propertyChange")]
		[Transient]
		public function get oldOnHoldString():String {
			var retString:String = "";
			if(oldSubStatuses != null)
			{
				 var sort:Sort = new Sort();
				 sort.fields = [new SortField(null, true)];
				 oldSubStatuses.sort = sort;
				 oldSubStatuses.refresh();
			}
			
			for each(var onHoldReason:String in oldSubStatuses) {
				//var tempString:String = retString + onHoldReason + ", ";
				//if(tempString.length > 40) retString+= "\n";
				retString+= onHoldReason + ", ";
			}			
			return (retString.length > 2) ? retString.substr(0, retString.length - 2) : retString;
		}
		
		[Bindable("propertyChange")]
		[Transient]
		public function get newOnHoldString():String {
			var retString:String = "";
			if(newSubStatuses != null)
			{
				var sort:Sort = new Sort();
				sort.fields = [new SortField(null, true)];
				newSubStatuses.sort = sort;
				newSubStatuses.refresh();
			}
			for each(var onHoldReason:String in newSubStatuses) {
				//var tempString:String = retString + onHoldReason + ", ";
				//if(tempString.length > 40) retString+= "\n";
				retString+= onHoldReason + ", ";
			}			
			return (retString.length > 2) ? retString.substr(0, retString.length - 2) : retString;
		}

        [Transient]
		public function get serviceCodeEnum():ServiceCodeEnum {
			return ServiceCodeEnum.valueOf(serviceCd);
		}

	}
}