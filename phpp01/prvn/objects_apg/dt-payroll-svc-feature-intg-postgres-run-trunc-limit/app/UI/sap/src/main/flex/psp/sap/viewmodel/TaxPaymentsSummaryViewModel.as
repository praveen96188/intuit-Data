package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.PaymentTemplateQuarterPayment;
    import psp.sap.model.PaymentTemplateYearPayment;
    import psp.sap.model.TaxPaymentYear;

    public class TaxPaymentsSummaryViewModel extends AbstractPartViewModel
	{
		private var mPaymentYearSort:Sort;
		private var mPaymentTemplateSort:Sort;
		public function TaxPaymentsSummaryViewModel()
		{
			reloadOnActivate = false;
			
			mPaymentYearSort = new Sort();
			var sortField:SortField = new SortField("year", false, true);
			sortField.compareFunction = compareStringYears;
			mPaymentYearSort.fields = [sortField];
			
			mPaymentTemplateSort = new Sort();
			var templateSortField:SortField = new SortField("paymentTemplateName", true);
			templateSortField.compareFunction = CompareUtils.comparePaymentTemplate;
			mPaymentTemplateSort.fields = [templateSortField];
		}
		
		private function compareStringYears(a:Object, b:Object):int {
			var numA:Number = parseInt(String(a.year));
			var numB:Number = parseInt(String(b.year));
			return ObjectUtil.numericCompare(numA, numB);						
		}				
		

        [Bindable] public var taxPaymentYears:ArrayCollection = new ArrayCollection();
        [Bindable] public var selectedQuarter:PaymentTemplateQuarterPayment;

		override protected function onActivated():void {
			for each(var year:TaxPaymentYear in taxPaymentYears){
				if(year.year == SAP.instance.PSPDate.fullYear.toString()){
					for each(var paymentTemplate:PaymentTemplate in year.paymentTemplates){
						// open to the current year IRS 941 payment template
						if(paymentTemplate.paymentTemplateCd.indexOf("IRS") > -1 && paymentTemplate.paymentTemplateCd.indexOf("941") > -1){
							year.isYearShowing = true;
							paymentTemplate.isTemplateShowing = true;
							getTemplateYearPayment(year, paymentTemplate);
						}
					}
				}
			}
		}		
		
		override protected function loadModelData():void {
			SAP.instance.taxService.getPaymentTemplateYears(company.sourceSystemCd, 
															company.companyId,
                                                            SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_POSSIBLE_BACKDATE_YEARS),
															createLoadModelDataResponder(onPaymentYearsLoaded));
		}		
		
		private function onPaymentYearsLoaded(e:ResultEvent):void {
			taxPaymentYears = e.result as ArrayCollection;
			taxPaymentYears.sort = mPaymentYearSort;
			taxPaymentYears.refresh();
			for each(var taxPaymentYear:TaxPaymentYear in taxPaymentYears){
				taxPaymentYear.paymentTemplates.sort = mPaymentTemplateSort;
				taxPaymentYear.paymentTemplates.refresh();
			}			
		}
		
		public function getTemplateYearPayment(taxPaymentYear:TaxPaymentYear, paymentTemplate:PaymentTemplate):void {
			SAP.instance.taxService.getTemplateYearPayment(company.sourceSystemCd, 
															company.companyId, 
															taxPaymentYear.year, 
															paymentTemplate.paymentTemplateCd, 
															new Responder(onPaymentTemplateYearPaymentLoaded, onSaveFaulted_internal));
		}
		
		private function onPaymentTemplateYearPaymentLoaded(e:ResultEvent):void {
			var paymentTemplateYearPayment:PaymentTemplateYearPayment = e.result as PaymentTemplateYearPayment;
			for each(var taxPaymentYear:TaxPaymentYear in taxPaymentYears){
				if(paymentTemplateYearPayment.taxYear == taxPaymentYear.year){
					for each(var paymentTemplate:PaymentTemplate in taxPaymentYear.paymentTemplates){
						if(paymentTemplateYearPayment.paymentTemplateCd == paymentTemplate.paymentTemplateCd){
							paymentTemplate.paymentTemplateYearPayment = paymentTemplateYearPayment;
							return;
						}
					}
				}
			}
			saveFaulted = true;
			saveMsg = "Payment template for tax year: " + paymentTemplateYearPayment.taxYear + " and template: " + paymentTemplateYearPayment.paymentTemplateCd + " not found.";
		}

	}
}
