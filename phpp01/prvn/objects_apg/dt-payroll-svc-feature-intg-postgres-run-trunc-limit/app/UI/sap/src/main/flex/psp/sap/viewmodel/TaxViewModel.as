package psp.sap.viewmodel
{
	import mx.binding.utils.BindingUtils;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;

    public class TaxViewModel extends CompositePartViewModel
	{
		public var taxPaymentsDetail:TaxPaymentsDetailViewModel;
							
		public function TaxViewModel()
		{
			super();
			
			bindSaveMessageWithChildren = true;			
			
			this.label = CompanyInspectorPageEnum.TAXES;			

            var taxPtn:PartsTabNavigatorViewModel = addPartsTabNavigator(CompanyInspectorPageEnum.TAX_PAYMENTS_SUMMARY);

            taxPtn.addNewPart(AgencyInfoViewModel, CompanyInspectorPageEnum.AGENCY_INFO);
            taxPtn.addNewPart(CompanyLawsViewModel, CompanyInspectorPageEnum.TAX_LAWS);
            var taxPaymentsSummary:AbstractPartViewModel = taxPtn.addNewPart(TaxPaymentsSummaryViewModel, CompanyInspectorPageEnum.TAX_PAYMENTS_SUMMARY);
			taxPaymentsDetail = taxPtn.addNewPart(TaxPaymentsDetailViewModel, CompanyInspectorPageEnum.TAX_PAYMENTS_DETAIL) as TaxPaymentsDetailViewModel;
			taxPtn.addNewPart(TaxLedgerViewModel, CompanyInspectorPageEnum.TAX_LEDGER);
            taxPtn.addNewPart(ManualLedgerViewModel, CompanyInspectorPageEnum.MANUAL_LEDGER);

            taxPtn.partViewModels.filterFunction = function(e:Object):Boolean {
                switch (AbstractPartViewModel(e).label) {
                    case CompanyInspectorPageEnum.TAX_PAYMENTS_SUMMARY:
                        return SAP.canPerformOperation(OperationsEnum.VIEW_COMPANY_TAX_PAYMENTS);
                    case CompanyInspectorPageEnum.TAX_PAYMENTS_DETAIL:
                        return SAP.canPerformOperation(OperationsEnum.VIEW_COMPANY_TAX_PAYMENTS);
                    case CompanyInspectorPageEnum.TAX_LEDGER:
                        return SAP.canPerformOperation(OperationsEnum.VIEW_TAX_LEDGER);
                    case CompanyInspectorPageEnum.MANUAL_LEDGER:
                        return SAP.canPerformOperation(OperationsEnum.CREATE_MANUAL_LEDGER_ENTRY);
                    case CompanyInspectorPageEnum.AGENCY_INFO:
                        return SAP.canPerformOperation(OperationsEnum.VIEW_AGENCY_INFO);
                    default:
                        return true;
                }
            };
            taxPtn.partViewModels.refresh();
            taxPtn.defaultSinglePart = AbstractPartViewModel(taxPtn.partViewModels.getItemAt(0));
						
			BindingUtils.bindProperty(taxPaymentsDetail, "selectedQuarter", taxPaymentsSummary, "selectedQuarter");
			taxPtn.defaultSinglePart = taxPaymentsSummary;
		}
		
		override protected function onRefresh():void {			
			if(activeSinglePart == taxPaymentsDetail){
				// clear the current selections
				taxPaymentsDetail.selectedQuarter = null;
				taxPaymentsDetail.selectedPayment = null;
			}

			super.onRefresh();
		}				

	}
}