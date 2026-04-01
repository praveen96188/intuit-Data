package psp.sap.view
{
	import mx.controls.LinkButton;

    import psp.sap.model.ActionEvent;
    import psp.sap.model.CompanyLedgerAccount;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.PayrollTransaction;
	
	public class PayrollActionEventButton extends LinkButton
	{
		public function PayrollActionEventButton()
		{
			super();
		}
		
		[Bindable]
		public var payroll:PayrollRun;
		
		[Bindable]
		public var payrollLedgerAccount:CompanyLedgerAccount;
		
		[Bindable]
		public var payrollTransaction:PayrollTransaction;

        [Bindable] public var action:ActionEvent;
	}
}