package psp.sap.model
{
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.SAP;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawItem")]
	public class LawItem extends EventDispatcher
	{
		public var name:String;
		public var lawId:String;
		public var description:String;
        public var paymentTemplateCd:String;

        public var negativeLiability:Boolean;

	}
}