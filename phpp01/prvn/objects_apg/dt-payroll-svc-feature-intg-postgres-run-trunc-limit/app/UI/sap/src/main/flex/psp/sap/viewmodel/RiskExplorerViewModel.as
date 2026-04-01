package psp.sap.viewmodel
{
	import psp.sap.application.enums.ExplorerEnum;
	
	public class RiskExplorerViewModel extends AbstractExplorer
	{
		public function RiskExplorerViewModel()
		{
			super(ExplorerEnum.RISK);

			inspectors.addItem(new RiskInspectorViewModel(this));
		}

	}
}