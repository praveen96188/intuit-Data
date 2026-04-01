package psp.sap.service
{
    import mx.controls.Alert;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.model.Company;
    import psp.sap.viewmodel.CompanyExplorerViewModel;

    public class AbstractCompanyService extends PSPService
	{
		public function AbstractCompanyService()
		{
			super();
		}

		public function display(company:Company):void {
			var explorer:CompanyExplorerViewModel = 
				SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY)
				as CompanyExplorerViewModel;

			if(explorer != null && explorer.inspectors != null 
				&& (explorer.inspectors.length < (SAP.MAX_OPEN_COMPANIES +1) || explorer.inspectors.findByApplicationItem(company) != null)){													
				explorer.display(company);
			}
			else{
				Alert.show("Tab limit reached. Please close a tab before opening a new one.");				
			}			
		}
	}
}
