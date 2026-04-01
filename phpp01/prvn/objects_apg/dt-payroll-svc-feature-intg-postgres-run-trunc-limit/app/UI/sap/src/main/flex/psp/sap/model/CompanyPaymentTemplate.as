package psp.sap.model
{
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyPaymentTemplate")]
	public class CompanyPaymentTemplate extends EventDispatcher
	{
		public var paymentTemplate:PaymentTemplate;
        public var agencyTaxpayerId:String;

        public var canChangeDepositFrequency:Boolean;
		public var currentDepositFrequency:DepositFrequency;
		public var futureDepositFrequency:DepositFrequency;

        [ArrayElementType("psp.sap.model.PaymentMethod")]
		public var paymentMethods:ArrayCollection;

        [ArrayElementType("psp.sap.model.CompanyAgencyPaymentTemplateAgencyId")]
        public var additionalIds:ArrayCollection;

        [ArrayElementType("psp.sap.model.CompanyLawRateDetail")]
		public var lawRates:ArrayCollection;

        public var hasSUIERRates:Boolean;

        [ArrayElementType("psp.sap.model.CompanyFilingAmount")]
        public var activeFilingAmounts:ArrayCollection;
		
		public var is944Filer:Boolean;

        public var filerType:String;
        public var filerTypeFutureEffectiveQuarter:Quarter;

        public var registeredForACH:Boolean;

        public function get isNYMetro():Boolean {
            return paymentTemplate.paymentTemplateCd == "NY-MTA305-PAYMENT";
        }
	}
}