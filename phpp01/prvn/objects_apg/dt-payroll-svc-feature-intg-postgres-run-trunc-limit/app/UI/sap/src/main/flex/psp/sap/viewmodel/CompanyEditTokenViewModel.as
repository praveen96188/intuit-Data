package psp.sap.viewmodel
{
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	import mx.validators.NumberValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.Company;

    import psp.sap.model.QBDTTokens;
    import psp.sap.validators.SAPValidators;

	public class CompanyEditTokenViewModel
	extends AbstractPartViewModel
	{

        [Bindable] [BackingProperty]
		public var syncTokens:QBDTTokens;

		public function CompanyEditTokenViewModel()
		{
			this.label = CompanyInspectorPageEnum.COMPANY_TOKEN;
			this.reloadOnSave = true;
		}
		
		override protected function initializeBackingProperties():void {
            validators.push(SAPValidators.createNumberValidator(syncTokens, "highToken", true, syncTokens.highToken, null, false, 0));
            validators.push(SAPValidators.createNumberValidator(syncTokens, "payrollTxNextId", true, syncTokens.payrollTxNextId, null, false, 0));
            validators.push(SAPValidators.createNumberValidator(syncTokens, "paycheckNextId", true, syncTokens.paycheckNextId, null, false, 0));
            validators.push(SAPValidators.createNumberValidator(syncTokens, "employeeNextId", true, syncTokens.employeeNextId, null, false, 0));
            validators.push(SAPValidators.createNumberValidator(syncTokens, "payrollItemNextId", true, syncTokens.payrollItemNextId, null, false, 0));
		}

		override protected function loadModelData():void {
			SAP.instance.taxService.getQBDTTokens(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onTokenDataReturned));
		}

		private function onTokenDataReturned(e:ResultEvent):void {
            syncTokens = e.result as QBDTTokens;
		}

        [Bindable("propertyChange")]
        public function get showAssistedTokens():Boolean {
            return company.isAssisted || company.isAssistedServiceCancelled;
        }

        override protected function executeSave():void {
            SAP.instance.companyService.adjustCompanyTokens(company.sourceSystemCd, company.companyId, syncTokens, createSaveResponder());
        }


	}
}