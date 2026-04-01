package psp.sap.service
{
	import psp.sap.model.PayrollRun;

    public class AbstractPayrollRunService extends PSPService 
	{
		public function AbstractPayrollRunService()
		{
			super();
		}

		public function display(payrollRun:PayrollRun):void {
            // TODO:  Make a PayrollRunExplorer!
            /*var explorer:ICompanyExplorer =
				SAP.instance.explorers.getExplorer(ExplorerEnum.SAP_COMPANY)
				as ICompanyExplorer;

			explorer.display(company);*/
            trace("NOT IMPLEMENTED: PayrollRunService.display()");
        }
	}
}