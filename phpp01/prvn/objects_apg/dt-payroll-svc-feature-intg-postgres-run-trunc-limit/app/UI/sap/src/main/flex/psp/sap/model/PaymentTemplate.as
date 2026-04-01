package psp.sap.model
{
	import mx.collections.ArrayCollection;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentTemplate")]
	public class PaymentTemplate
	{
		public var paymentTemplateCd:String;
    	public var paymentTemplateName:String;
        public var agencyName:String;
        public var supportStartDate:Date;
        public var processingStartDate:Date;
        public var agencyIDs:ArrayCollection;
        public var canBeFinalized:Boolean;
        public var followsFedDepositFrequency:Boolean;

    	[ArrayElementType("psp.sap.model.LawItem")]
    	public var lawItems:ArrayCollection;


    	[ArrayElementType("String")]
    	public var possibleDepositFrequencies:ArrayCollection;

        public var registeredForACH:Boolean = false;

    	[Transient]
		public function toString():String {
    		return paymentTemplateName;
    	}
    	
    	[Transient]
    	public var isTemplateShowing:Boolean = false;
    	
    	[Transient]
    	public var paymentTemplateYearPayment:PaymentTemplateYearPayment;
    	
    	[Transient]
    	public var selectedIndices:Array = [];

        public function getIRS94x():String {
            if (isIRSPaymentTemplate()) {
                return getSimplifiedName();
            }
            else {
                return '';
            }
        }

        public function isIRSPaymentTemplate():Boolean {
            return agencyName.toUpperCase() == 'IRS';
        }

        public function hasACHEnrollment():Boolean {
            return paymentTemplateCd == "FL-UCT6-PAYMENT";
        }

        public function getSimplifiedName():String {
            return paymentTemplateCd.split('-')[1];
        }

        public static function get EMPTY_TEMPLATE():PaymentTemplate {
            var paymentTemplate:PaymentTemplate = new PaymentTemplate();
            paymentTemplate.paymentTemplateName = "";
            paymentTemplate.paymentTemplateCd = "";
            return paymentTemplate;
        }
        
	}
}
