package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.SAP;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxPaymentsQueueItem")]
	public class TaxPaymentsQueueItem
	{
		public var id:String;
		public var status:String;
		public var companyLegalName:String;
		public var agencyName:String;
		public var agencyId:String;
		public var paymentType:String;
		public var paymentDate:Date;
		public var dueDate:Date;
		public var amount:Number;
		public var paymentMethod:String;
		public var sourceSystemCd:String;
		public var companyId:String;
		public var agentCorpId:String;
		public var paymentTemplateCd:String;
		
		//For exceptions queue
		public var agencyRejections:ArrayCollection;
		public var agencyHolds:ArrayCollection;
		public var companyHolds:ArrayCollection;
		
		public var quarterId:String;
		
		[Transient]
		public function get hasAgencyRejections():Boolean {
			return (agencyRejections != null && agencyRejections.length > 0);
		}
		
		[Transient]
		public function get hasAgencyHolds():Boolean {
			return (agencyHolds != null && agencyHolds.length > 0)
		}
		
		[Transient]
		public function get hasCompanyHolds():Boolean {
			return (companyHolds != null && companyHolds.length > 0);
		}
		
		[Transient]
		public function get canResolve():Boolean {
			return (agentCorpId != null && agentCorpId == SAP.instance.session.user.corpId && (agencyRejections != null && agencyRejections.length > 0));
		}
		
		[Transient]
		public function get canAssign():Boolean {
			return (agentCorpId == null);
		}
		
		[Transient]
		public function get canUnAssign():Boolean {
			return (agentCorpId != null && agentCorpId == SAP.instance.session.user.corpId);
		}
		
		[Transient]
		public function get actionsList():ArrayCollection {
			var retCollection:ArrayCollection = new ArrayCollection();
			if(canAssign) retCollection.addItem("Assign to Me");
			if(canResolve) retCollection.addItem("Resolve");
			if(canUnAssign) retCollection.addItem("Unassign");
			return retCollection;
		}
		
		[Transient]
		public function get isFullyLoaded():Boolean {
			return (status != null) || 
			(
				(agencyRejections != null && agencyRejections.length > 0) ||
				(agencyHolds != null && agencyHolds.length > 0) ||
				(companyHolds != null && companyHolds.length > 0)
			);
		}
		
		[Transient]
		public function get statusName():String {
			return (status == null) ? 'Loading....' : status;
		}
		
		[Transient]
		public function get taxPaymentYear():Number {
			return (paymentDate == null) ? 0 : (paymentDate.fullYear);
		}
	}
}