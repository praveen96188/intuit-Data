package psp.sap.viewmodel
{
    import psp.sap.application.SAP;

    public class CompanyQuickBooksHistoryViewModel
		extends CompanyPropertyAuditHistory
	{
		override protected function loadModelData():void {
            SAP.instance.propertyAuditService.getQuickBooksPropertyAudits(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    createLoadModelDataResponder(onDataLoadCompleted));
		}
	}
}