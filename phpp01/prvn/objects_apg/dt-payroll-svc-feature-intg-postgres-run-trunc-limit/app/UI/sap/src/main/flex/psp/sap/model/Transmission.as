package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTransmission")]
	public class Transmission
	{
		public var requestToken:Number;
    	public var responseToken:Number;
    	public var initializeDateTime:Date;
    	public var description:String;
    	public var transmissionIdentifier:String;
    	public var connectionTime:Number;
    	public var requestDocument:String;
    	public var responseDocument:String;
    	public var ipAddress:String;
        public var companyName:String;
        public var companyKey:CompanyKey;
        public var psid:String;
        public var loginTime:String;
        public function get companyId():String
        {
            return companyKey.companyId;
        }
        public function get sourceSystemCd():String
        {
            return (companyKey.sourceSystemCd != null ? companyKey.sourceSystemCd : "") ;
        }

        public var largerLog:Boolean;
	}
}