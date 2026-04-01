package psp.sap.viewmodel
{
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	
	public class PayrollViewModel extends CompositePartViewModel
	{					
		public function PayrollViewModel()
		{
			super();
			
			bindSaveMessageWithChildren = true;
			
			this.label = CompanyInspectorPageEnum.PAYROLL;			
			
			var tabNavigatorVM:PartsTabNavigatorViewModel = addPartsTabNavigator(CompanyInspectorPageEnum.PAYROLLS_LIST);			
			 			
			var payrollsListViewModel:PayrollsListViewModel = tabNavigatorVM.addNewPart(PayrollsListViewModel, CompanyInspectorPageEnum.PAYROLLS_LIST) as PayrollsListViewModel;
			tabNavigatorVM.addNewPart(PayrollVendorPaymentsListViewModel, CompanyInspectorPageEnum.VENDOR_PAYMENT_LIST);
            tabNavigatorVM.addNewPart(PayrollACHTransactionsListViewModel, CompanyInspectorPageEnum.PAYROLL_ACH_TRANSACTIONS);
            tabNavigatorVM.addNewPart(PayrollPItemsViewModel, CompanyInspectorPageEnum.PAYROLLS_PITEMS);

			// commenting out tax stuff			
			// tabNavigatorVM.addNewPart(PayrollAdjustmentsSummaryViewModel, CompanyInspectorPageEnum.PAYROLL_ADJUSTMENTS);

			tabNavigatorVM.defaultSinglePart = payrollsListViewModel;
		}

	}
}
