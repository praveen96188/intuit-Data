package psp.sap.service {

    import mx.collections.ArrayCollection;
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.remoting.RemoteObject;

    import psp.sap.model.DirectDepositLimitSettings;
    import psp.sap.model.FraudSettings;
    import psp.sap.model.Quarter;
    import psp.sap.service.interfaces.IAdministrationService;

    public class AdministrationService extends PSPService implements IAdministrationService {
        public function AdministrationService():void {
            remoteObjectPool = new RemoteObjectPool("administrationservice", 3);
        }

        public function get administrationRemoteService():RemoteObject {
            return mRemoteObjectPool.nextAvailable();
        }

        public function postToGems(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.postToGems());
            remoteToken.addResponder(responder);
        }

        public function getGemsMonthlyBalances(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getGemsMonthlyBalances());
            remoteToken.addResponder(responder);
        }

        public function getAllSystemParameters(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getAllSystemParameters());
            remoteToken.addResponder(responder);
        }

        public function saveSystemParameters(systemParameters:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.saveSystemParameters(systemParameters));
            remoteToken.addResponder(responder);
        }

        public function getDirectDepositLimitSettings(pSourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getDirectDepositLimitSettings(pSourceSystemCd));
            remoteToken.addResponder(responder);
        }

        public function getFraudSettings(pFraudLimitId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getFraudSettings(pFraudLimitId));
            remoteToken.addResponder(responder);
        }

        public function getLimitRules(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getLimitRules());
            remoteToken.addResponder(responder);
        }

        public function getFraudRules(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getFraudRules());
            remoteToken.addResponder(responder);
        }

        public function saveDirectDepositLimitSettings(sapDirectDepositLimitSettings:DirectDepositLimitSettings, pLimitRuleId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.saveDirectDepositLimitSettings(sapDirectDepositLimitSettings, pLimitRuleId));
            remoteToken.addResponder(responder);
        }

        public function saveFraudSettings(sapFraudSettings:FraudSettings, pFraudRuleId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.saveFraudSettings(sapFraudSettings, pFraudRuleId));
            remoteToken.addResponder(responder);
        }

        public function getNachaFilesForOffload(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getNachaFilesForOffload());
            remoteToken.addResponder(responder);
        }

        public function confirmOffloadFiles(nachaFiles:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.confirmOffloadFiles(nachaFiles));
            remoteToken.addResponder(responder);
        }

        public function scheduleSecondaryOffload(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.scheduleSecondaryOffload());
            remoteToken.addResponder(responder);
        }

        public function isSecondOffloadScheduled(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.isSecondOffloadScheduled());
            remoteToken.addResponder(responder);
        }

        public function getOffloadStatus(statusDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getOffloadStatus(statusDate));
            remoteToken.addResponder(responder);
        }

        public function executeSql(sqlStatement:String, reason:String, expectedRowCount:int, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.executeSql(sqlStatement, reason, expectedRowCount));
            remoteToken.addResponder(responder);
        }

        public function getAllTransmissions(pFromSourceSystemCode:String, pFromDate:Date, pToDate:Date, pStartIndex:uint, pMaxResults:uint, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getAllTransmissions(pFromSourceSystemCode, pFromDate, pToDate, pStartIndex, pMaxResults));
            remoteToken.addResponder(responder);
        }

        public function scheduleATFExtract(year:String, quarter:int, scheduleDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.scheduleATFExtract(year, quarter, scheduleDate));
            remoteToken.addResponder(responder);
        }

        public function getLedgerOperationJobs(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getLedgerOperationJobs());
            remoteToken.addResponder(responder);
        }

        public function queueLedgerOperationJob(jobId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.queueLedgerOperationJob(jobId));
            remoteToken.addResponder(responder);
        }

        public function createTORLedgerOperationJob(paymentTemplateCd:String, quarterDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.createTORLedgerOperationJob(paymentTemplateCd, quarterDate));
            remoteToken.addResponder(responder);
        }

        public function getMinSupportedQuickbooksVersion(sourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getMinSupportedQuickbooksVersion(sourceSystemCd));
            remoteToken.addResponder(responder);
        }

        public function getAvailableSUICreditTemplates(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getAvailableSUICreditTemplates());
            remoteToken.addResponder(responder);
        }

        public function getSUICreditsJobList(responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.getSUICreditsJobList());
            remoteToken.addResponder(responder);
        }

        public function createSUICreditsJob(quarter:Quarter, paymentTemplateCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.createSUICreditsJob(quarter, paymentTemplateCd));
            remoteToken.addResponder(responder);
        }

        public function processBulkDDLimitUpdates(fileContents:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.processBulkDDLimitUpdates(fileContents));
            remoteToken.addResponder(responder);
        }

        public function deleteLedgerOperationJob(jobId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(administrationRemoteService.deleteLedgerOperationJob(jobId));
            remoteToken.addResponder(responder);
        }

    }
}
 


