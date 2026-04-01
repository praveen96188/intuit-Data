package test.mock
{
    import mx.collections.ArrayCollection;
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

    import psp.sap.model.DirectDepositLimitSettings;
    import psp.sap.model.FraudSettings;
    import psp.sap.model.Quarter;
    import psp.sap.service.interfaces.IAdministrationService;

    public class MockAdministrationService extends MockAsyncService implements IAdministrationService
	{
		private var mDirectDepositLimitSettingsMockData:DirectDepositLimitSettings;
		
		public function MockAdministrationService()
		{
		}

		public function expectsPostToGems():Mock {
            return expects("postToGems").withArgs();
        }
		public function postToGems(responder:IResponder):void {
            record("postToGems");
			sendAsyncResult(responder,"postToGems");
        }

		public function expectsGetGemsMonthlyBalances():Mock {
            return expects("getGemsMonthlyBalances").withArgs();
        }
		public function getGemsMonthlyBalances(responder:IResponder):void {
            record("getGemsMonthlyBalances");
			sendAsyncResult(responder,"getGemsMonthlyBalances");
        }

        public function expectsGetAllSystemParameters():Mock {
            return expects("getAllSystemParameters").withArgs();
        }

        public function getAllSystemParameters(responder:IResponder):void {
            record("getAllSystemParameters");
 			sendAsyncResult(responder,"getAllSystemParameters");
        }

		public function expectsSaveSystemParameters(systemParameters:ArrayCollection):Mock {
            return expects("saveSystemParameters").withArgs(systemParameters);
        }

        public function saveSystemParameters(systemParameters:ArrayCollection, responder:IResponder):void {
            record("saveSystemParameters", systemParameters);
 			sendAsyncResult(responder,"saveSystemParameters");
        }

		public function expectsGetDirectDepositLimitSettings(pSourceSystemCd:String):Mock {
            return expects("getDirectDepositLimitSettings").withArgs(pSourceSystemCd);
        }
		public function getDirectDepositLimitSettings(pSourceSystemCd:String, responder:IResponder):void {
            record("getDirectDepositLimitSettings", pSourceSystemCd);
			sendAsyncResult(responder,"getDirectDepositLimitSettings");
        }

		public function expectsGetFraudSettings(pFraudLimitId:String):Mock {
            return expects("getFraudSettings").withArgs(pFraudLimitId);
        }
		public function getFraudSettings(pFraudLimitId:String, responder:IResponder):void {
            record("getFraudSettings", pFraudLimitId);
			sendAsyncResult(responder,"getFraudSettings");
        }

        public function expectsGetLimitRules():Mock {
            return expects("getLimitRules").withArgs();
        }
        public function getLimitRules(responder:IResponder):void {
            record("getLimitRules");
            sendAsyncResult(responder,"getLimitRules");
        }

		public function expectsGetFraudRules():Mock {
            return expects("getFraudRules").withArgs();
        }
		public function getFraudRules(responder:IResponder):void {
            record("getFraudRules");
			sendAsyncResult(responder,"getFraudRules");
        }

		public function expectsSaveDirectDepositLimitSettings(sapDirectDepositLimitSettings:DirectDepositLimitSettings, pSourceSystemCd:String):Mock {
            return expects("saveDirectDepositLimitSettings").withArgs(sapDirectDepositLimitSettings, pSourceSystemCd);
        }
		public function saveDirectDepositLimitSettings(sapDirectDepositLimitSettings:DirectDepositLimitSettings, pLimitRuleId:String, responder:IResponder):void {
            record("saveDirectDepositLimitSettings", sapDirectDepositLimitSettings, pLimitRuleId);
			sendAsyncResult(responder,"saveDirectDepositLimitSettings");
        }

		public function expectsSaveFraudSettings(sapFraudSettings:FraudSettings, pFraudRuleId:String):Mock {
            return expects("saveFraudSettings").withArgs(sapFraudSettings, pFraudRuleId);
        }
		public function saveFraudSettings(sapFraudSettings:FraudSettings, pFraudRuleId:String, responder:IResponder):void {
            record("saveFraudSettings", sapFraudSettings, pFraudRuleId);
			sendAsyncResult(responder,"saveFraudSettings");
        }

		public function expectsGetNachaFilesForOffload():Mock {
            return expects("getNachaFilesForOffload").withArgs();
        }
		public function getNachaFilesForOffload(responder:IResponder):void {
            record("getNachaFilesForOffload");
			sendAsyncResult(responder,"getNachaFilesForOffload");
        }

		public function expectsConfirmOffloadFiles(nachaFiles:ArrayCollection):Mock {
            return expects("confirmOffloadFiles").withArgs(nachaFiles);
        }
		public function confirmOffloadFiles(nachaFiles:ArrayCollection, responder:IResponder):void {
            record("confirmOffloadFiles", nachaFiles);
			sendAsyncResult(responder,"confirmOffloadFiles");
        }

		public function expectsScheduleSecondaryOffload():Mock {
            return expects("scheduleSecondaryOffload").withArgs();
        }
		public function scheduleSecondaryOffload(responder:IResponder):void {
            record("scheduleSecondaryOffload");
			sendAsyncResult(responder,"scheduleSecondaryOffload");
        }

		public function expectsIsSecondOffloadScheduled():Mock {
            return expects("isSecondOffloadScheduled").withArgs();
        }
		public function isSecondOffloadScheduled(responder:IResponder):void {
            record("isSecondOffloadScheduled");
			sendAsyncResult(responder,"isSecondOffloadScheduled");
        }

		public function expectsGetOffloadStatus(statusDate:Date):Mock {
            return expects("getOffloadStatus").withArgs(statusDate);
        }
		public function getOffloadStatus(statusDate:Date, responder:IResponder):void {
            record("getOffloadStatus", statusDate);
			sendAsyncResult(responder,"getOffloadStatus");
        }

        public function expectsExecuteSql(sqlStatement:String, reason:String):Mock {
            return expects("executeSql").withArgs(sqlStatement, reason);
        }

        public function expectsGetAllTransmissions(pSourceSystemCd:String, pFromDate:Date, pToDate:Date, pFromSourceSystemCode:String, pFirstIndex:uint, pMaxResults:uint):Mock {
            return expects("getAllTransmissions").withArgs(pSourceSystemCd, pFromDate, pToDate, pFromSourceSystemCode, pFirstIndex, pMaxResults);
        }

        public function getAllTransmissions(pFromSourceSystemCode:String, pFromDate:Date, pToDate:Date, pFirstIndex:uint, pMaxResults:uint, responder:IResponder):void {
            record("getAllTransmissions", pFromSourceSystemCode, pFromDate, pToDate, pFirstIndex, pMaxResults);
            sendAsyncResult(responder, "getAllTransmissions");
        }

        public function executeSql(sqlStatement:String, reason:String, expectedRowCount:int, responder:IResponder):void {
            record("executeSql", sqlStatement, reason);
			sendAsyncResult(responder,"executeSql");
        }

		public function expectsScheduleATFExtract(year:String, quarter:int, scheduleDate:Date):Mock {
            return expects("scheduleATFExtract").withArgs(year, quarter, scheduleDate);
        }
		public function scheduleATFExtract(year:String, quarter:int, scheduleDate:Date, responder:IResponder):void {
            record("scheduleATFExtract", year, quarter, scheduleDate);
			sendAsyncResult(responder,"scheduleATFExtract");
        }

		public function expectsGetLedgerOperationJobs():Mock {
            return expects("getLedgerOperationJobs").withArgs();
        }
		public function getLedgerOperationJobs(responder:IResponder):void {
            record("getLedgerOperationJobs");
			sendAsyncResult(responder,"getLedgerOperationJobs");
        }

		public function expectsQueueLedgerOperationJob(jobId:String):Mock {
            return expects("queueLedgerOperationJob").withArgs(jobId);
        }
		public function queueLedgerOperationJob(jobId:String, responder:IResponder):void {
            record("queueLedgerOperationJob", jobId);
			sendAsyncResult(responder,"queueLedgerOperationJob");
        }

		public function expectsCreateTORLedgerOperationJob(paymentTemplateCd:String, quarterDate:Date):Mock {
            return expects("createTORLedgerOperationJob").withArgs(paymentTemplateCd, quarterDate);
        }
		public function createTORLedgerOperationJob(paymentTemplateCd:String, quarterDate:Date, responder:IResponder):void {
            record("createTORLedgerOperationJob", paymentTemplateCd, quarterDate);
			sendAsyncResult(responder,"createTORLedgerOperationJob");
        }

		public function expectsGetMinSupportedQuickbooksVersion(sourceSystemCd:String):Mock {
            return expects("getMinSupportedQuickbooksVersion").withArgs(sourceSystemCd);
        }
		public function getMinSupportedQuickbooksVersion(sourceSystemCd:String, responder:IResponder):void {
            record("getMinSupportedQuickbooksVersion", sourceSystemCd);
			sendAsyncResult(responder,"getMinSupportedQuickbooksVersion");
        }

		public function expectsGetAvailableSUICreditTemplates():Mock {
            return expects("getAvailableSUICreditTemplates").withArgs();
        }
		public function getAvailableSUICreditTemplates(responder:IResponder):void {
            record("getAvailableSUICreditTemplates");
			sendAsyncResult(responder,"getAvailableSUICreditTemplates");
        }

		public function expectsGetSUICreditsJobList():Mock {
            return expects("getSUICreditsJobList").withArgs();
        }
		public function getSUICreditsJobList(responder:IResponder):void {
            record("getSUICreditsJobList");
			sendAsyncResult(responder,"getSUICreditsJobList");
        }

		public function expectsCreateSUICreditsJob(quarter:Quarter, paymentTemplateCd:String):Mock {
            return expects("createSUICreditsJob").withArgs(quarter, paymentTemplateCd);
        }
		public function createSUICreditsJob(quarter:Quarter, paymentTemplateCd:String, responder:IResponder):void {
            record("createSUICreditsJob", quarter, paymentTemplateCd);
			sendAsyncResult(responder,"createSUICreditsJob");
        }

		public function expectsProcessBulkDDLimitUpdates(fileContents:String):Mock {
            return expects("processBulkDDLimitUpdates").withArgs(fileContents);
        }
		public function processBulkDDLimitUpdates(fileContents:String, responder:IResponder):void {
            record("processBulkDDLimitUpdates", fileContents);
			sendAsyncResult(responder,"processBulkDDLimitUpdates");
        }

    }
}
