package psp.sap.viewmodel
{
    import psp.sap.application.enums.ExplorerEnum;

    public class AdministrationExplorerViewModel
	extends AbstractExplorer
	{		
		public function AdministrationExplorerViewModel()
		{
			super(ExplorerEnum.ADMINISTRATION);
			
            inspectors.addItem(new AdministrationInspectorViewModel(this));
		}				
		
	}
}