package psp.sap.service.interfaces {
    import mx.collections.ArrayCollection;
    import mx.rpc.IResponder;

    import psp.sap.model.DirectDepositLimitSettings;
    import psp.sap.model.FraudSettings;
    import psp.sap.model.Quarter;

    public interface IAdministrationService extends IPSPService {

        function postToGems(responder:IResponder):void;

        function getGemsMonthlyBalances(responder:IResponder):void;

        function getAllSystemParameters(responder:IResponder):void;

        function saveSystemParameters(systemParameters:ArrayCollection, responder:IResponder):void;

        function getDirectDepositLimitSettings(pSourceSystemCd:String, responder:IResponder):void;

        function getFraudSettings(pFraudLimitId:String, responder:IResponder):void;

        function getLimitRules(responder:IResponder):void;

        function getFraudRules(responder:IResponder):void;

        function saveDirectDepositLimitSettings(sapDirectDepositLimitSettings:DirectDepositLimitSettings, pLimitRuleId:String, responder:IResponder):void;

        function saveFraudSettings(sapFraudSettings:FraudSettings, pFraudRuleId:String, responder:IResponder):void;

        function getNachaFilesForOffload(responder:IResponder):void;

        function confirmOffloadFiles(nachaFiles:ArrayCollection, responder:IResponder):void;

        function scheduleSecondaryOffload(responder:IResponder):void;

        function isSecondOffloadScheduled(responder:IResponder):void;

        function getOffloadStatus(statusDate:Date, responder:IResponder):void;

        function executeSql(sqlStatement:String, reason:String, expectedRowCount:int, responder:IResponder):void;

        function getAllTransmissions(pFromSourceSystemCode:String, pFromDate:Date, pToDate:Date, pFirstIndex:uint, pMaxResults:uint, responder:IResponder):void;

        function scheduleATFExtract(year:String, quarter:int, scheduleDate:Date, responder:IResponder):void;

        function getLedgerOperationJobs(responder:IResponder):void;

        function queueLedgerOperationJob(jobId:String, responder:IResponder):void;

        function createTORLedgerOperationJob(paymentTemplateCd:String, quarterDate:Date, responder:IResponder):void;

        function getMinSupportedQuickbooksVersion(sourceSystemCd:String, responder:IResponder):void;

        function getAvailableSUICreditTemplates(responder:IResponder):void;

        function getSUICreditsJobList(responder:IResponder):void;

        function createSUICreditsJob(quarter:Quarter, paymentTemplateCd:String, responder:IResponder):void;

        function processBulkDDLimitUpdates(fileContents:String, responder:IResponder):void;

        function deleteLedgerOperationJob(jobId:String, responder:IResponder):void;

    }
}
