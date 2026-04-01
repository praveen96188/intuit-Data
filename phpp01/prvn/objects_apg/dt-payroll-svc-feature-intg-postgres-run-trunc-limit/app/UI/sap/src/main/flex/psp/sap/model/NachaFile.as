package psp.sap.model
{
	
	import intuit.sbd.flex.framework.model.EntityObject;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPNachaFile")]
	public class NachaFile extends EntityObject
	{
		public var fileId:String;    	
		public var fileName:String;
		public var totalCredits:Number;
		public var totalDebits:Number;
		public var confirmationCode:String;
		public var finalizedTime:Date;
		public var transmissionTime:Date;
		
		[Transient]
		public var index:int; //this is for preserving the original ordering on equal dates.  Thanks, Flex!


        public function NachaFile(fileId:String=null, fileName:String=null, totalCredits:Number=0, totalDebits:Number=0, confirmationCode:String=null, finalizedTime:Date=null, transmissionTime:Date=null) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.totalCredits = totalCredits;
            this.totalDebits = totalDebits;
            this.confirmationCode = confirmationCode;
            this.finalizedTime = finalizedTime;
            this.transmissionTime = transmissionTime;
        }
    }
	
}
