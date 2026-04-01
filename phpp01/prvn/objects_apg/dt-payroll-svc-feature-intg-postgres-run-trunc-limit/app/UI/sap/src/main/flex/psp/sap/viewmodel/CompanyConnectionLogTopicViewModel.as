package psp.sap.viewmodel
{
import psp.sap.application.enums.CompanyInspectorPageEnum;
import psp.sap.application.enums.CompanyInspectorTopicEnum;
    public class CompanyConnectionLogTopicViewModel extends CompanyInspectorTopicViewModel
	{
		public function CompanyConnectionLogTopicViewModel(companyInspector:CompanyInspectorViewModel)
		{
			super(companyInspector, CompanyInspectorTopicEnum.CONNECTION_LOG);	
			
			addSinglePart(CompanyInspectorPageEnum.COMPANY_CONNECTIONS, CompanyConnectionsViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_CONNECTION_OFX, PayrollConnectionOFXViewModel);
			addSinglePart(CompanyInspectorPageEnum.DATA_SYNC_SEARCH_VIEW, DataSyncSearchViewModel);
            addSinglePart(CompanyInspectorPageEnum.DATA_SYNC_MANAGE_VIEW, DataSyncManageViewModel);

		
		}
	}
}
