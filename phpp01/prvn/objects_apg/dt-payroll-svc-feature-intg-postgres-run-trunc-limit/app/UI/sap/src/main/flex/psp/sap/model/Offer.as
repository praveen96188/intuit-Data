package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOffer")]
	public class Offer
	{
		public var offerCd:String;
	    public var name:String;
	    public var description:String;
	    public var offerEndEvent:String;
	    public var effectiveDate:Date;
	    public var expirationDate:Date;
        public var openToUser:Boolean;


        public function Offer(offerCd:String=null, description:String=null) {
            this.offerCd = offerCd;
            this.description = description;
        }
    }
}