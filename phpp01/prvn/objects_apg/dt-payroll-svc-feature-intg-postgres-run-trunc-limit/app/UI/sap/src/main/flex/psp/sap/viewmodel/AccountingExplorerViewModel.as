package psp.sap.viewmodel
{
	import psp.sap.application.enums.ExplorerEnum;
	
	public class AccountingExplorerViewModel extends AbstractExplorer
	{
		public function AccountingExplorerViewModel()
		{
			super(ExplorerEnum.ACCOUNTING);

			inspectors.addItem(new AccountingInspectorViewModel(this));
		}

	}
}