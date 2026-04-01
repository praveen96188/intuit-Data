package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyStatus")]
	public class CompanyStatus {
		public var sourceSystemCd:String;
		public var companyId:String;
		public var flaggedForFraud:Boolean;

        [ArrayElementType ("String")]
        public var availableServices:ArrayCollection;
		
		[ArrayElementType("psp.sap.model.CompanyServiceStatus")]
		public var serviceStatusCollection:ArrayCollection;
	}
}