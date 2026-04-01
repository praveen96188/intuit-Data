package psp.sap.viewmodel
{
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	
	public class PayrollAddNonACHRedebitViewModel extends PayrollAddRedebitViewModel
	{
		public function PayrollAddNonACHRedebitViewModel()
		{
			super();
			this.label = CompanyInspectorPageEnum.PAYROLL_NONACH_ADD_REDEBIT;
		}

	}
}