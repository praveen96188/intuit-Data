package psp.sap.viewmodel
{
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;

    public class CompanyFundingModelHistoryViewModel
		extends CompanyPropertyAuditHistory
	{	
		public function CompanyFundingModelHistoryViewModel()
		{
			super();
			this.label = CompanyInspectorPageEnum.FUNDING_MODEL_HISTORY;
			
		}

		override protected function loadModelData():void {
			SAP.instance.propertyAuditService.getFundingModelHistory(
											company.companyId, company.sourceSystemCd,
											null,
											createLoadModelDataResponder(onDataLoadCompleted));
		}
	}
}