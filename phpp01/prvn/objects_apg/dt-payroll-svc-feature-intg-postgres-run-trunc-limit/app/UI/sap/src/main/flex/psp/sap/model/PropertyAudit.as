package psp.sap.model
{
	import psp.sap.application.SAP;

	[Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPropertyAudit")]
    [CopyTestCase(nullOk="oldPropertyValue")]           
    public class PropertyAudit
	{
		public function PropertyAudit()
		{
			super();
		}		

		public var createdDate:Date;
        public var propertyName:String;
        public var oldPropertyValue:String;
		public var newPropertyValue:String;
		public var userId:String;
		public var auditDate:Date;
		public var category:String;
		
		[Transient]
		public function get propertyLabel():String {			
			if(propertyName == "StatusCd"){
				return "Status"
			}
			else if(propertyName == "SourceBankAccountName"){
				return "QB Bank Account Name";
			}
			
			return propertyName;
		}
		
		[Transient]
		public function get auditDateTime():Number {
			return auditDate.getTime();
		}
		
	}
}