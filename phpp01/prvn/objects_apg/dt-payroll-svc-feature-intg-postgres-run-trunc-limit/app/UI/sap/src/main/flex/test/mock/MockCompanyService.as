package test.mock
{
    import flash.utils.ByteArray;

    import mx.collections.ArrayCollection;
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

    import psp.sap.model.AddCompany;
    import psp.sap.model.CompanyDdLimits;
    import psp.sap.model.CompanyLegalInfo;
    import psp.sap.model.CompanyNote;
    import psp.sap.model.EntityChange;
    import psp.sap.model.QBDTTokens;
    import psp.sap.model.TaxCompanyServiceInfo;
    import psp.sap.model.TaxExemptInfo;
    import psp.sap.service.interfaces.ICompanyService;

    public class MockCompanyService extends MockAsyncService implements ICompanyService
	{
		public function MockCompanyService()
		{
		}

		public function expectsSearch(searchMethod:String, searchInput:String):Mock {
            return expects("search").withArgs(searchMethod, searchInput);
        }
		public function search(searchMethod:String, searchInput:String, responder:IResponder):void {
            record("search", searchMethod, searchInput);
			sendAsyncResult(responder,"search");
        }

		public function expectsFindCompany(source:String, id:String):Mock {
            return expects("findCompany").withArgs(source, id);
        }
		public function findCompany(source:String, id:String, responder:IResponder):void {
            record("findCompany", source, id);
			sendAsyncResult(responder,"findCompany");
        }

		public function expectsGetCompanyCancellationInfo(source:String, id:String):Mock {
            return expects("getCompanyCancellationInfo").withArgs(source, id);
        }
		public function getCompanyCancellationInfo(source:String, id:String, responder:IResponder):void {
            record("getCompanyCancellationInfo", source, id);
			sendAsyncResult(responder,"getCompanyCancellationInfo");
        }

		public function expectsUpdateCompanyCancellationInfo(pSource:String, pId:String, pSapTaxCompanyServiceInfo:TaxCompanyServiceInfo):Mock {
            return expects("updateCompanyCancellationInfo").withArgs(pSource, pId, pSapTaxCompanyServiceInfo);
        }
		public function updateCompanyCancellationInfo(pSource:String, pId:String, pSapTaxCompanyServiceInfo:TaxCompanyServiceInfo, responder:IResponder):void {
            record("updateCompanyCancellationInfo", pSource, pId, pSapTaxCompanyServiceInfo);
			sendAsyncResult(responder,"updateCompanyCancellationInfo");
        }

		public function expectsGetStrikeInfo(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getStrikeInfo").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getStrikeInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getStrikeInfo", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getStrikeInfo");
        }

		public function expectsGetPayrollRunCount(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getPayrollRunCount").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getPayrollRunCount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getPayrollRunCount", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getPayrollRunCount");
        }

		public function expectsGetBankReturnTransactionCount(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getBankReturnTransactionCount").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getBankReturnTransactionCount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getBankReturnTransactionCount", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getBankReturnTransactionCount");
        }

		public function expectsGetPINInfo(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getPINInfo").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getPINInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getPINInfo", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getPINInfo");
        }

		public function expectsGetFundingModelCd(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getFundingModelCd").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getFundingModelCd(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getFundingModelCd", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getFundingModelCd");
        }

		public function expectsGetActiveBankAccount(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getActiveBankAccount").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getActiveBankAccount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getActiveBankAccount", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getActiveBankAccount");
        }

		public function expectsIsDebugLogging(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("isDebugLogging").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function isDebugLogging(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("isDebugLogging", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"isDebugLogging");
        }

		public function expectsUpdateCompanyFundingModel(pSourceSystemCd:String, pCompanyId:String, pFundingModelCd:String):Mock {
            return expects("updateCompanyFundingModel").withArgs(pSourceSystemCd, pCompanyId, pFundingModelCd);
        }
		public function updateCompanyFundingModel(pSourceSystemCd:String, pCompanyId:String, pFundingModelCd:String, responder:IResponder):void {
            record("updateCompanyFundingModel", pSourceSystemCd, pCompanyId, pFundingModelCd);
			sendAsyncResult(responder,"updateCompanyFundingModel");
        }

		public function expectsAddCompanyNote(pSourceSystemCd:String, pCompanyId:String, companyEventId:String, companyEventTransmissionId:String, sapCompanyNote:CompanyNote):Mock {
            return expects("addCompanyNote").withArgs(pSourceSystemCd, pCompanyId, companyEventId, companyEventTransmissionId, sapCompanyNote);
        }
		public function addCompanyNote(pSourceSystemCd:String, pCompanyId:String, companyEventId:String, companyEventTransmissionId:String, sapCompanyNote:CompanyNote, responder:IResponder):void {
            record("addCompanyNote", pSourceSystemCd, pCompanyId, companyEventId, companyEventTransmissionId, sapCompanyNote);
			sendAsyncResult(responder,"addCompanyNote");
        }

		public function expectsRemoveCompanyNoteAlert(noteId:String):Mock {
            return expects("removeCompanyNoteAlert").withArgs(noteId);
        }
		public function removeCompanyNoteAlert(noteId:String, responder:IResponder):void {
            record("removeCompanyNoteAlert", noteId);
			sendAsyncResult(responder,"removeCompanyNoteAlert");
        }

		public function expectsGetMostRecentAlertNote(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getMostRecentAlertNote").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getMostRecentAlertNote(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getMostRecentAlertNote", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getMostRecentAlertNote");
        }

		public function expectsAddEntitlementUnitToCompany(sourceSystemCd:String, sourceCompanyId:String, licenseNumber:String, eoc:String, itemNumber:String):Mock {
            return expects("addEntitlementUnitToCompany").withArgs(sourceSystemCd, sourceCompanyId, licenseNumber, eoc, itemNumber);
        }
		public function addEntitlementUnitToCompany(sourceSystemCd:String, sourceCompanyId:String, licenseNumber:String, eoc:String, itemNumber:String, responder:IResponder):void {
            record("addEntitlementUnitToCompany", sourceSystemCd, sourceCompanyId, licenseNumber, eoc, itemNumber);
			sendAsyncResult(responder,"addEntitlementUnitToCompany");
        }

		public function expectsGetPriceType(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("getPriceType").withArgs(sourceSystemCd, sourceCompanyId);
        }
		public function getPriceType(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("getPriceType", sourceSystemCd, sourceCompanyId);
			sendAsyncResult(responder,"getPriceType");
        }

        public function expectsAvailablePriceTypesByCompanyKey(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("getAvailablePriceTypes").withArgs(sourceSystemCd, sourceCompanyId);
        }
        public function getAvailablePriceTypesByCompanyKey(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("getAvailablePriceTypes", sourceSystemCd, sourceCompanyId);
            sendAsyncResult(responder,"getAvailablePriceTypes");
        }

        public function expectsAvailablePriceTypesByItemNumber(itemNumber:String):Mock {
            return expects("getAvailablePriceTypes").withArgs(itemNumber);
        }
        public function getAvailablePriceTypesByItemNumber(itemNumber:String, responder:IResponder):void {
            record("getAvailablePriceTypes", itemNumber);
            sendAsyncResult(responder,"getAvailablePriceTypes");
        }

        public function expectsSetAssistedPriceTypeAndOffer(sourceSystemCd:String, sourceCompanyId:String, priceType:String, offerCode:String):Mock {
            return expects("setAssistedPriceTypeAndOffer").withArgs(sourceSystemCd, sourceCompanyId, priceType, offerCode);
        }
		public function setAssistedPriceTypeAndOffer(sourceSystemCd:String, sourceCompanyId:String, priceType:String, offerCode:String, responder:IResponder):void {
            record("setAssistedPriceTypeAndOffer", sourceSystemCd, sourceCompanyId, priceType, offerCode);
			sendAsyncResult(responder,"setAssistedPriceTypeAndOffer");
        }

		public function expectsGetAssistedOffer(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("getAssistedOffer").withArgs(sourceSystemCd, sourceCompanyId);
        }
		public function getAssistedOffer(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("getAssistedOffer", sourceSystemCd, sourceCompanyId);
			sendAsyncResult(responder,"getAssistedOffer");
        }

		public function expectsDeactivateEntitlementUnit(id:String):Mock {
            return expects("deactivateEntitlementUnit").withArgs(id);
        }
		public function deactivateEntitlementUnit(id:String, responder:IResponder):void {
            record("deactivateEntitlementUnit", id);
			sendAsyncResult(responder,"deactivateEntitlementUnit");
        }

		public function expectsReactivateEntitlementUnit(id:String):Mock {
            return expects("reactivateEntitlementUnit").withArgs(id);
        }
		public function reactivateEntitlementUnit(id:String, responder:IResponder):void {
            record("reactivateEntitlementUnit", id);
			sendAsyncResult(responder,"reactivateEntitlementUnit");
        }

		public function expectsMoveEntitlementUnit(fromEntitlementId:String, toLicenseNumber:String, toEoc:String, toItemNumber:String):Mock {
            return expects("moveEntitlementUnit").withArgs(fromEntitlementId, toLicenseNumber, toEoc, toItemNumber);
        }
		public function moveEntitlementUnit(fromEntitlementId:String, toLicenseNumber:String, toEoc:String, toItemNumber:String, responder:IResponder):void {
            record("moveEntitlementUnit", fromEntitlementId, toLicenseNumber, toEoc, toItemNumber);
			sendAsyncResult(responder,"moveEntitlementUnit");
        }

		public function expectsGetAssetInfo(itemNumber:String):Mock {
            return expects("getAssetInfo").withArgs(itemNumber);
        }
		public function getAssetInfo(itemNumber:String, responder:IResponder):void {
            record("getAssetInfo", itemNumber);
			sendAsyncResult(responder,"getAssetInfo");
        }

		public function expectsGetEntityChangeHistory(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("getEntityChangeHistory").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function getEntityChangeHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("getEntityChangeHistory", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"getEntityChangeHistory");
        }

		public function expectsAddCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeDate:Date, pStrikeReason:String):Mock {
            return expects("addCompanyStrike").withArgs(pSourceSystemCd, pCompanyId, pStrikeDate, pStrikeReason);
        }
		public function addCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeDate:Date, pStrikeReason:String, responder:IResponder):void {
            record("addCompanyStrike", pSourceSystemCd, pCompanyId, pStrikeDate, pStrikeReason);
			sendAsyncResult(responder,"addCompanyStrike");
        }

		public function expectsCancelCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeId:String):Mock {
            return expects("cancelCompanyStrike").withArgs(pSourceSystemCd, pCompanyId, pStrikeId);
        }
		public function cancelCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeId:String, responder:IResponder):void {
            record("cancelCompanyStrike", pSourceSystemCd, pCompanyId, pStrikeId);
			sendAsyncResult(responder,"cancelCompanyStrike");
        }

		public function expectsGetLimitViolationEvents(pCompanyId:String, pSourceSystemCd:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("getLimitViolationEvents").withArgs(pCompanyId, pSourceSystemCd, pFromDate, pToDate);
        }
		public function getLimitViolationEvents(pCompanyId:String, pSourceSystemCd:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("getLimitViolationEvents", pCompanyId, pSourceSystemCd, pFromDate, pToDate);
			sendAsyncResult(responder,"getLimitViolationEvents");
        }

		public function expectsGetCompanyBankAccountsHistory(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getCompanyBankAccountsHistory").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getCompanyBankAccountsHistory(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getCompanyBankAccountsHistory", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getCompanyBankAccountsHistory");
        }

		public function expectsGetBankVerificationLimit(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("getBankVerificationLimit").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function getBankVerificationLimit(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("getBankVerificationLimit", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"getBankVerificationLimit");
        }

		public function expectsGetRandomDebitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourceBankAccountId:String):Mock {
            return expects("getRandomDebitTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourceBankAccountId);
        }
		public function getRandomDebitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourceBankAccountId:String, responder:IResponder):void {
            record("getRandomDebitTransactions", pCompanyId, pSourceSystemCd, pSourceBankAccountId);
			sendAsyncResult(responder,"getRandomDebitTransactions");
        }

		public function expectsReinitiateRandomDebit(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String):Mock {
            return expects("reinitiateRandomDebit").withArgs(pCompanyId, pSourceSystemCd, pBankAccountId);
        }
		public function reinitiateRandomDebit(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
            record("reinitiateRandomDebit", pCompanyId, pSourceSystemCd, pBankAccountId);
			sendAsyncResult(responder,"reinitiateRandomDebit");
        }

		public function expectsIsPendingRandomDebit(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("isPendingRandomDebit").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function isPendingRandomDebit(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("isPendingRandomDebit", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"isPendingRandomDebit");
        }

		public function expectsVerifyCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String):Mock {
            return expects("verifyCompanyBankAccount").withArgs(pCompanyId, pSourceSystemCd, pBankAccountId);
        }
		public function verifyCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
            record("verifyCompanyBankAccount", pCompanyId, pSourceSystemCd, pBankAccountId);
			sendAsyncResult(responder,"verifyCompanyBankAccount");
        }

		public function expectsGenerateRandomPin(pSourceSystemCd:String, pSourceCompanyId:String):Mock {
            return expects("generateRandomPin").withArgs(pSourceSystemCd, pSourceCompanyId);
        }
		public function generateRandomPin(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void {
            record("generateRandomPin", pSourceSystemCd, pSourceCompanyId);
			sendAsyncResult(responder,"generateRandomPin");
        }

		public function expectsUnlockCompany(pSourceSystemCd:String, pSourceCompanyId:String):Mock {
            return expects("unlockCompany").withArgs(pSourceSystemCd, pSourceCompanyId);
        }
		public function unlockCompany(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void {
            record("unlockCompany", pSourceSystemCd, pSourceCompanyId);
			sendAsyncResult(responder,"unlockCompany");
        }

		public function expectsFindTransmissions(pSourceSystemCd:String, pSourceCompanyId:String, pFromDate:Date, pToDate:Date, pFromSourceSystemCode:String):Mock {
            return expects("findTransmissions").withArgs(pSourceSystemCd, pSourceCompanyId, pFromDate, pToDate, pFromSourceSystemCode);
        }
		public function findTransmissions(pSourceSystemCd:String, pSourceCompanyId:String, pFromDate:Date, pToDate:Date, pFromSourceSystemCode:String, responder:IResponder):void {
            record("findTransmissions", pSourceSystemCd, pSourceCompanyId, pFromDate, pToDate, pFromSourceSystemCode);
			sendAsyncResult(responder,"findTransmissions");
        }

		public function expectsFindTransmissionById(pTransmissionId:String):Mock {
            return expects("findTransmissionById").withArgs(pTransmissionId);
        }
		public function findTransmissionById(pTransmissionId:String, responder:IResponder):void {
            record("findTransmissionById", pTransmissionId);
			sendAsyncResult(responder,"findTransmissionById");
        }

		public function expectsRemoveFraudFlag(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("removeFraudFlag").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function removeFraudFlag(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("removeFraudFlag", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"removeFraudFlag");
        }

		public function expectsSwitchDebugLogging(pSourceSystemCd:String, pCompanyId:String, pDebugLogging:Boolean):Mock {
            return expects("switchDebugLogging").withArgs(pSourceSystemCd, pCompanyId, pDebugLogging);
        }
		public function switchDebugLogging(pSourceSystemCd:String, pCompanyId:String, pDebugLogging:Boolean, responder:IResponder):void {
            record("switchDebugLogging", pSourceSystemCd, pCompanyId, pDebugLogging);
			sendAsyncResult(responder,"switchDebugLogging");
        }

        public function expectsSwitchProcessTransmissions(pSourceSystemCd:String, pCompanyId:String, pProcessTransmissions:Boolean):Mock {
            return expects("switchProcessTransmissions").withArgs(pSourceSystemCd, pCompanyId, pProcessTransmissions);
        }
        public function switchProcessTransmissions(pSourceSystemCd:String, pCompanyId:String, pProcessTransmissions:Boolean, responder:IResponder):void {
            record("switchDebugLogging", pSourceSystemCd, pCompanyId, pProcessTransmissions);
            sendAsyncResult(responder,"switchProcessTransmissions");
        }

		public function expectsFindCompanyFraudEvents(pEinCid:String, pFraudEventCategory:String, pPayrollAmount:Number, pFromDate:Date, pToDate:Date, eventTypeCodes:ArrayCollection):Mock {
            return expects("findCompanyFraudEvents").withArgs(pEinCid, pFraudEventCategory, pPayrollAmount, pFromDate, pToDate, eventTypeCodes);
        }
		public function findCompanyFraudEvents(pEinCid:String, pFraudEventCategory:String, pPayrollAmount:Number, pFromDate:Date, pToDate:Date, eventTypeCodes:ArrayCollection, responder:IResponder):void {
            record("findCompanyFraudEvents", pEinCid, pFraudEventCategory, pPayrollAmount, pFromDate, pToDate, eventTypeCodes);
			sendAsyncResult(responder,"findCompanyFraudEvents");
        }

		public function expectsGetFraudEventTypes():Mock {
            return expects("getFraudEventTypes").withArgs();
        }
		public function getFraudEventTypes(responder:IResponder):void {
            record("getFraudEventTypes");
			sendAsyncResult(responder,"getFraudEventTypes");
        }

		public function expectsFindCompanyNotes(pSourceSystemCd:String, pCompanyId:String, pCompanyEventId:String, pCompanyEventTransmissionId:String):Mock {
            return expects("findCompanyNotes").withArgs(pSourceSystemCd, pCompanyId, pCompanyEventId, pCompanyEventTransmissionId);
        }
		public function findCompanyNotes(pSourceSystemCd:String, pCompanyId:String, pCompanyEventId:String, pCompanyEventTransmissionId:String, responder:IResponder):void {
            record("findCompanyNotes", pSourceSystemCd, pCompanyId, pCompanyEventId, pCompanyEventTransmissionId);
			sendAsyncResult(responder,"findCompanyNotes");
        }

		public function expectsFindManualNotes(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("findManualNotes").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function findManualNotes(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("findManualNotes", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"findManualNotes");
        }

		public function expectsFindCompanyEventGroups(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("findCompanyEventGroups").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function findCompanyEventGroups(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("findCompanyEventGroups", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"findCompanyEventGroups");
        }

		public function expectsFindCompanyEventCreators(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("findCompanyEventCreators").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function findCompanyEventCreators(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("findCompanyEventCreators", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"findCompanyEventCreators");
        }

		public function expectsFindCompanyEvents(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, creatorId:String, eventTypes:ArrayCollection, includeAS400Events:Boolean):Mock {
            return expects("findCompanyEvents").withArgs(pSourceSystemCd, pCompanyId, pFromDate, pToDate, creatorId, eventTypes, includeAS400Events);
        }
		public function findCompanyEvents(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, creatorId:String, eventTypes:ArrayCollection, includeAS400Events:Boolean, responder:IResponder):void {
            record("findCompanyEvents", pSourceSystemCd, pCompanyId, pFromDate, pToDate, creatorId, eventTypes, includeAS400Events);
			sendAsyncResult(responder,"findCompanyEvents");
        }

		public function expectsGetEmployeeBankAccountHistory(pCompanyId:String, pEmployeeId:String, pSourceSystemCd:String):Mock {
            return expects("getEmployeeBankAccountHistory").withArgs(pCompanyId, pEmployeeId, pSourceSystemCd);
        }
		public function getEmployeeBankAccountHistory(pCompanyId:String, pEmployeeId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("getEmployeeBankAccountHistory", pCompanyId, pEmployeeId, pSourceSystemCd);
			sendAsyncResult(responder,"getEmployeeBankAccountHistory");
        }

		public function expectsGetCloudEmployees(pCompanyId:String, pSourceSystemCd:String, pUserViewsNonServiceableData:Boolean):Mock {
            return expects("getCloudEmployees").withArgs(pCompanyId, pSourceSystemCd, pUserViewsNonServiceableData);
        }
		public function getCloudEmployees(pCompanyId:String, pSourceSystemCd:String, pUserViewsNonServiceableData:Boolean, responder:IResponder):void {
            record("getCloudEmployees", pCompanyId, pSourceSystemCd, pUserViewsNonServiceableData);
			sendAsyncResult(responder,"getCloudEmployees");
        }

		public function expectsGetVendors(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("getVendors").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function getVendors(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("getVendors", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"getVendors");
        }

		public function expectsDeactivateCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String):Mock {
            return expects("deactivateCompanyBankAccount").withArgs(pCompanyId, pSourceSystemCd, pBankAccountId);
        }
		public function deactivateCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
            record("deactivateCompanyBankAccount", pCompanyId, pSourceSystemCd, pBankAccountId);
			sendAsyncResult(responder,"deactivateCompanyBankAccount");
        }

		public function expectsSaveCompanyService(pSourceSystemCd:String, pCompanyId:String, pServiceCode:String, pSubStatuses:ArrayCollection, pSAPCompanyDdLimits:CompanyDdLimits, pFundingModelCd:String):Mock {
            return expects("saveCompanyService").withArgs(pSourceSystemCd, pCompanyId, pServiceCode, pSubStatuses, pSAPCompanyDdLimits, pFundingModelCd);
        }
		public function saveCompanyService(pSourceSystemCd:String, pCompanyId:String, pServiceCode:String, pSubStatuses:ArrayCollection, pSAPCompanyDdLimits:CompanyDdLimits, pFundingModelCd:String, responder:IResponder):void {
            record("saveCompanyService", pSourceSystemCd, pCompanyId, pServiceCode, pSubStatuses, pSAPCompanyDdLimits, pFundingModelCd);
			sendAsyncResult(responder,"saveCompanyService");
        }

		public function expectsGetVendorBankAccountHistory(pSourceSystemCd:String, pCompanyId:String, pVendorId:String):Mock {
            return expects("getVendorBankAccountHistory").withArgs(pSourceSystemCd, pCompanyId, pVendorId);
        }
		public function getVendorBankAccountHistory(pSourceSystemCd:String, pCompanyId:String, pVendorId:String, responder:IResponder):void {
            record("getVendorBankAccountHistory", pSourceSystemCd, pCompanyId, pVendorId);
			sendAsyncResult(responder,"getVendorBankAccountHistory");
        }

		public function expectsGetEmployees(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("getEmployees").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function getEmployees(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("getEmployees", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"getEmployees");
        }

		public function expectsGetEmployee(pCompanyId:String, pSourceSystemCd:String, sourceEmployeeId:String):Mock {
            return expects("getEmployee").withArgs(pCompanyId, pSourceSystemCd, sourceEmployeeId);
        }
		public function getEmployee(pCompanyId:String, pSourceSystemCd:String, sourceEmployeeId:String, responder:IResponder):void {
            record("getEmployee", pCompanyId, pSourceSystemCd, sourceEmployeeId);
			sendAsyncResult(responder,"getEmployee");
        }

		public function expectsGetCompanyDisplayStatus(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getCompanyDisplayStatus").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getCompanyDisplayStatus(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getCompanyDisplayStatus", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getCompanyDisplayStatus");
        }

		public function expectsGetCompanyStatus(pSourceSystemCd:String, pCompanyId:String, findTransitions:Boolean, findLimitViolations:Boolean):Mock {
            return expects("getCompanyStatus").withArgs(pSourceSystemCd, pCompanyId, findTransitions, findLimitViolations);
        }
		public function getCompanyStatus(pSourceSystemCd:String, pCompanyId:String, findTransitions:Boolean, findLimitViolations:Boolean, responder:IResponder):void {
            record("getCompanyStatus", pSourceSystemCd, pCompanyId, findTransitions, findLimitViolations);
			sendAsyncResult(responder,"getCompanyStatus");
        }

		public function expectsEditBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String):Mock {
            return expects("editBankAccount").withArgs(pSourceSystemCd, pSourceCompanyId, pCompanyBankAccountID, pSourceBankAccountName, pAccountNumber, pRoutingNumber, pAccountType, pBankName);
        }
		public function expectsGetFeeOfferingServiceChargePrices(pSourceSystemCd:String, pCompanyId:String, pPayrollRunId:String):Mock {
            return expects("getFeeOfferingServiceChargePrices").withArgs(pSourceSystemCd, pCompanyId, pPayrollRunId);
        }
		public function getFeeOfferingServiceChargePrices(pSourceSystemCd:String, pCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            record("getFeeOfferingServiceChargePrices", pSourceSystemCd, pCompanyId, pPayrollRunId);
			sendAsyncResult(responder,"getFeeOfferingServiceChargePrices");
        }

		public function editBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, responder:IResponder):void {
            record("editBankAccount", pSourceSystemCd, pSourceCompanyId, pCompanyBankAccountID, pSourceBankAccountName, pAccountNumber, pRoutingNumber, pAccountType, pBankName);
			sendAsyncResult(responder,"editBankAccount");
        }

		public function expectsAddBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, pShouldAddRandomDebits:Boolean, pShouldAllowPendingTransactions:Boolean, pShouldMovePendingTransactionsToAccount:Boolean):Mock {
            return expects("addBankAccount").withArgs(pSourceSystemCd, pSourceCompanyId, pCompanyBankAccountID, pSourceBankAccountName, pAccountNumber, pRoutingNumber, pAccountType, pBankName, pShouldAddRandomDebits, pShouldAllowPendingTransactions, pShouldMovePendingTransactionsToAccount);
        }
		public function addBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, pShouldAddRandomDebits:Boolean, pShouldAllowPendingTransactions:Boolean, pShouldMovePendingTransactionsToAccount:Boolean, responder:IResponder):void {
            record("addBankAccount", pSourceSystemCd, pSourceCompanyId, pCompanyBankAccountID, pSourceBankAccountName, pAccountNumber, pRoutingNumber, pAccountType, pBankName, pShouldAddRandomDebits, pShouldAllowPendingTransactions, pShouldMovePendingTransactionsToAccount);
			sendAsyncResult(responder,"addBankAccount");
        }

		public function expectsResetVerifyAttempts(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String):Mock {
            return expects("resetVerifyAttempts").withArgs(pCompanyId, pSourceSystemCd, pBankAccountId);
        }
		public function resetVerifyAttempts(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
            record("resetVerifyAttempts", pCompanyId, pSourceSystemCd, pBankAccountId);
			sendAsyncResult(responder,"resetVerifyAttempts");
        }

		public function expectsGetCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("getCompanyBankAccount").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function getCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("getCompanyBankAccount", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"getCompanyBankAccount");
        }

		public function expectsGetRecentCompanyEvents(pSourceSystemCd:String, pCompanyId:String, max:int):Mock {
            return expects("getRecentCompanyEvents").withArgs(pSourceSystemCd, pCompanyId, max);
        }
		public function getRecentCompanyEvents(pSourceSystemCd:String, pCompanyId:String, max:int, responder:IResponder):void {
            record("getRecentCompanyEvents", pSourceSystemCd, pCompanyId, max);
			sendAsyncResult(responder,"getRecentCompanyEvents");
        }

		public function expectsResendEmail(pSourceSystemCd:String, pCompanyId:String, emailId:String):Mock {
            return expects("resendEmail").withArgs(pSourceSystemCd, pCompanyId, emailId);
        }
		public function resendEmail(pSourceSystemCd:String, pCompanyId:String, emailId:String, responder:IResponder):void {
            record("resendEmail", pSourceSystemCd, pCompanyId, emailId);
			sendAsyncResult(responder,"resendEmail");
        }

		public function expectsFindCompanyServiceStatusHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date):Mock {
            return expects("findCompanyServiceStatusHistory").withArgs(pCompanyId, pSourceSystemId, pFromDate);
        }
		public function findCompanyServiceStatusHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
            record("findCompanyServiceStatusHistory", pCompanyId, pSourceSystemId, pFromDate);
			sendAsyncResult(responder,"findCompanyServiceStatusHistory");
        }

		public function expectsAdjustCompanyTokens(pSourceSystemCode:String, pSourceCompanyId:String, tokens:QBDTTokens):Mock {
            return expects("adjustCompanyTokens").withArgs(pSourceSystemCode, pSourceCompanyId, tokens);
        }
		public function adjustCompanyTokens(pSourceSystemCode:String, pSourceCompanyId:String, tokens:QBDTTokens, responder:IResponder):void {
            record("adjustCompanyTokens", pSourceSystemCode, pSourceCompanyId, tokens);
			sendAsyncResult(responder,"adjustCompanyTokens");
        }

		public function expectsGetCompaniesByServiceSubstatuses(pSubStatuses:ArrayCollection, searchForOnHoldStatuses:Boolean, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int):Mock {
            return expects("getCompaniesByServiceSubstatuses").withArgs(pSubStatuses, searchForOnHoldStatuses, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
        }
		public function getCompaniesByServiceSubstatuses(pSubStatuses:ArrayCollection, searchForOnHoldStatuses:Boolean, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
            record("getCompaniesByServiceSubstatuses", pSubStatuses, searchForOnHoldStatuses, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
			sendAsyncResult(responder,"getCompaniesByServiceSubstatuses");
        }

		public function expectsGetCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getCompanyLegalInfo").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getCompanyLegalInfo", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getCompanyLegalInfo");
        }

		public function expectsUpdateCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, pLegalInfo:CompanyLegalInfo):Mock {
            return expects("updateCompanyLegalInfo").withArgs(pSourceSystemCd, pCompanyId, pLegalInfo);
        }
		public function updateCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, pLegalInfo:CompanyLegalInfo, responder:IResponder):void {
            record("updateCompanyLegalInfo", pSourceSystemCd, pCompanyId, pLegalInfo);
			sendAsyncResult(responder,"updateCompanyLegalInfo");
        }

		public function expectsGetCompanyContacts(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getCompanyContacts").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getCompanyContacts(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getCompanyContacts", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getCompanyContacts");
        }

		public function expectsUpdateCompanyContacts(pSourceSystemCd:String, pCompanyId:String, pContacts:ArrayCollection):Mock {
            return expects("updateCompanyContacts").withArgs(pSourceSystemCd, pCompanyId, pContacts);
        }
		public function updateCompanyContacts(pSourceSystemCd:String, pCompanyId:String, pContacts:ArrayCollection, responder:IResponder):void {
            record("updateCompanyContacts", pSourceSystemCd, pCompanyId, pContacts);
			sendAsyncResult(responder,"updateCompanyContacts");
        }

		public function expectsGetTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("getTaxExemptStatus").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function getTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("getTaxExemptStatus", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"getTaxExemptStatus");
        }

		public function expectsUpdateTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, info:TaxExemptInfo):Mock {
            return expects("updateTaxExemptStatus").withArgs(pSourceSystemCd, pCompanyId, info);
        }
		public function updateTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, info:TaxExemptInfo, responder:IResponder):void {
            record("updateTaxExemptStatus", pSourceSystemCd, pCompanyId, info);
			sendAsyncResult(responder,"updateTaxExemptStatus");
        }

		public function expectsGetServiceStatusList():Mock {
            return expects("getServiceStatusList").withArgs();
        }
		public function getServiceStatusList(responder:IResponder):void {
            record("getServiceStatusList");
			sendAsyncResult(responder,"getServiceStatusList");
        }

		public function expectsGetServiceSubStatusList():Mock {
            return expects("getServiceSubStatusList").withArgs();
        }
		public function getServiceSubStatusList(responder:IResponder):void {
            record("getServiceSubStatusList");
			sendAsyncResult(responder,"getServiceSubStatusList");
        }

		public function expectsGetFundingModelList():Mock {
            return expects("getFundingModelList").withArgs();
        }
		public function getFundingModelList(responder:IResponder):void {
            record("getFundingModelList");
			sendAsyncResult(responder,"getFundingModelList");
        }

		public function expectsGetSourceSystemList():Mock {
            return expects("getSourceSystemList").withArgs();
        }
		public function getSourceSystemList(responder:IResponder):void {
            record("getSourceSystemList");
			sendAsyncResult(responder,"getSourceSystemList");
        }

		public function expectsGetCompanyOffers(sourceSystemCd:String, companyId:String, serviceCd:String):Mock {
            return expects("getCompanyOffers").withArgs(sourceSystemCd, companyId, serviceCd);
        }
		public function getCompanyOffers(sourceSystemCd:String, companyId:String, serviceCd:String, responder:IResponder):void {
            record("getCompanyOffers", sourceSystemCd, companyId, serviceCd);
			sendAsyncResult(responder,"getCompanyOffers");
        }

		public function expectsAddServiceToCompany(pSourceSystemCd:String, pCompanyId:String, pServiceCd:String):Mock {
            return expects("addServiceToCompany").withArgs(pSourceSystemCd, pCompanyId, pServiceCd);
        }
		public function addServiceToCompany(pSourceSystemCd:String, pCompanyId:String, pServiceCd:String, responder:IResponder):void {
            record("addServiceToCompany", pSourceSystemCd, pCompanyId, pServiceCd);
			sendAsyncResult(responder,"addServiceToCompany");
        }

		public function expectsFindCheckPrintingBatches(pEinPsid:String, pBatchStatus:String, pCheckFromDate:Date, pCheckToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int):Mock {
            return expects("findCheckPrintingBatches").withArgs(pEinPsid, pBatchStatus, pCheckFromDate, pCheckToDate, pPrintFromDate, pPrintToDate, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
        }
		public function findCheckPrintingBatches(pEinPsid:String, pBatchStatus:String, pCheckFromDate:Date, pCheckToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
            record("findCheckPrintingBatches", pEinPsid, pBatchStatus, pCheckFromDate, pCheckToDate, pPrintFromDate, pPrintToDate, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
			sendAsyncResult(responder,"findCheckPrintingBatches");
        }

		public function expectsFindAgencyPrintingBatches(pPaymentTemplateCd:String, pBatchStatus:String, pInitiationFromDate:Date, pInitiationToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int):Mock {
            return expects("findAgencyPrintingBatches").withArgs(pPaymentTemplateCd, pBatchStatus, pInitiationFromDate, pInitiationToDate, pPrintFromDate, pPrintToDate, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
        }
		public function findAgencyPrintingBatches(pPaymentTemplateCd:String, pBatchStatus:String, pInitiationFromDate:Date, pInitiationToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
            record("findAgencyPrintingBatches", pPaymentTemplateCd, pBatchStatus, pInitiationFromDate, pInitiationToDate, pPrintFromDate, pPrintToDate, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
			sendAsyncResult(responder,"findAgencyPrintingBatches");
        }

		public function expectsSavePrintBatchStatus(pCheckPrintBatchId:String, pNewBatchStatus:String):Mock {
            return expects("savePrintBatchStatus").withArgs(pCheckPrintBatchId, pNewBatchStatus);
        }
		public function savePrintBatchStatus(pCheckPrintBatchId:String, pNewBatchStatus:String, responder:IResponder):void {
            record("savePrintBatchStatus", pCheckPrintBatchId, pNewBatchStatus);
			sendAsyncResult(responder,"savePrintBatchStatus");
        }

		public function expectsUploadSignatureFile(sourceSystemCd:String, companyId:String, signatureImage:ByteArray):Mock {
            return expects("uploadSignatureFile").withArgs(sourceSystemCd, companyId, signatureImage);
        }
		public function uploadSignatureFile(sourceSystemCd:String, companyId:String, signatureImage:ByteArray, responder:IResponder):void {
            record("uploadSignatureFile", sourceSystemCd, companyId, signatureImage);
			sendAsyncResult(responder,"uploadSignatureFile");
        }

		public function expectsGetCompanySignatureImage(sourceSystemCd:String, companyId:String):Mock {
            return expects("getCompanySignatureImage").withArgs(sourceSystemCd, companyId);
        }
		public function getCompanySignatureImage(sourceSystemCd:String, companyId:String, responder:IResponder):void {
            record("getCompanySignatureImage", sourceSystemCd, companyId);
			sendAsyncResult(responder,"getCompanySignatureImage");
        }

		public function expectsAddCheckPrintTestBatch(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("addCheckPrintTestBatch").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function addCheckPrintTestBatch(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("addCheckPrintTestBatch", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"addCheckPrintTestBatch");
        }

		public function expectsFindBankAccounts(pRoutingNumber:String, pAccountNumber:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int):Mock {
            return expects("findBankAccounts").withArgs(pRoutingNumber, pAccountNumber, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
        }
		public function findBankAccounts(pRoutingNumber:String, pAccountNumber:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
            record("findBankAccounts", pRoutingNumber, pAccountNumber, pOrderBy, pOrderDesc, pFirstResult, pMaxResults);
			sendAsyncResult(responder,"findBankAccounts");
        }

		public function expectsFindCurrentEINs(licenseNumber:String, eoc:String):Mock {
            return expects("findCurrentEINs").withArgs(licenseNumber, eoc);
        }
		public function findCurrentEINs(licenseNumber:String, eoc:String, responder:IResponder):void {
            record("findCurrentEINs", licenseNumber, eoc);
			sendAsyncResult(responder,"findCurrentEINs");
        }

		public function expectsFindEntitlementUnits(ein:String):Mock {
            return expects("findEntitlementUnits").withArgs(ein);
        }
		public function findEntitlementUnits(ein:String, responder:IResponder):void {
            record("findEntitlementUnits", ein);
			sendAsyncResult(responder,"findEntitlementUnits");
        }

		public function expectsFindCompaniesByEIN(ein:String):Mock {
            return expects("findCompaniesByEIN").withArgs(ein);
        }
		public function findCompaniesByEIN(ein:String, responder:IResponder):void {
            record("findCompaniesByEIN", ein);
			sendAsyncResult(responder,"findCompaniesByEIN");
        }

		public function expectsGetLicenseFromOrderNumber(orderNumber:String):Mock {
            return expects("getLicenseFromOrderNumber").withArgs(orderNumber);
        }
		public function getLicenseFromOrderNumber(orderNumber:String, responder:IResponder):void {
            record("getLicenseFromOrderNumber", orderNumber);
			sendAsyncResult(responder,"getLicenseFromOrderNumber");
        }

		public function expectsGetEntitlementInfo(licenseNumber:String, eoc:String):Mock {
            return expects("getEntitlementInfo").withArgs(licenseNumber, eoc);
        }
		public function getEntitlementInfo(licenseNumber:String, eoc:String, responder:IResponder):void {
            record("getEntitlementInfo", licenseNumber, eoc);
			sendAsyncResult(responder,"getEntitlementInfo");
        }

		public function expectsGetEntitlementUnits(sourceSystemCd:String, companyId:String):Mock {
            return expects("getEntitlementUnits").withArgs(sourceSystemCd, companyId);
        }
		public function getEntitlementUnits(sourceSystemCd:String, companyId:String, responder:IResponder):void {
            record("getEntitlementUnits", sourceSystemCd, companyId);
			sendAsyncResult(responder,"getEntitlementUnits");
        }

		public function syncEntitlementUnit(entitlementUnitId:String, responder:IResponder):void {
            record("syncEntitlementUnitFromSourceSystems", entitlementUnitId);
			sendAsyncResult(responder,"syncEntitlementUnitFromSourceSystems");
        }

        public function updateSubscriptionEndDate(entitlementUnitId:String, responder:IResponder):void {
            record("updateSubscriptionEndDate", entitlementUnitId);
            sendAsyncResult(responder,"updateSubscriptionEndDate");
        }

		public function expectsGetAdditionalContacts(licenseNumber:String, eoc:String):Mock {
            return expects("getAdditionalContacts").withArgs(licenseNumber, eoc);
        }
		public function getAdditionalContacts(licenseNumber:String, eoc:String, responder:IResponder):void {
            record("getAdditionalContacts", licenseNumber, eoc);
			sendAsyncResult(responder,"getAdditionalContacts");
        }

		public function expectsGetAdditionalAddresses(licenseNumber:String, eoc:String):Mock {
            return expects("getAdditionalAddresses").withArgs(licenseNumber, eoc);
        }
		public function getAdditionalAddresses(licenseNumber:String, eoc:String, responder:IResponder):void {
            record("getAdditionalAddresses", licenseNumber, eoc);
			sendAsyncResult(responder,"getAdditionalAddresses");
        }

		public function expectsAddCompany(addCompany:AddCompany):Mock {
            return expects("addCompany").withArgs(addCompany);
        }
		public function addCompany(addCompany:AddCompany, responder:IResponder):void {
            record("addCompany", addCompany);
			sendAsyncResult(responder,"addCompany");
        }



		public function expectsReCalculateLedgerBalances(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("reCalculateLedgerBalances").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function reCalculateLedgerBalances(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("reCalculateLedgerBalances", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"reCalculateLedgerBalances");
        }

		public function expectsGetQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("getQuickbooksInfo").withArgs(sourceSystemCd, sourceCompanyId);
        }
		public function getQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("getQuickbooksInfo", sourceSystemCd, sourceCompanyId);
			sendAsyncResult(responder,"getQuickbooksInfo");
        }

		public function expectsGetAvailableQuickBooksFileIds(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("getAvailableQuickBooksFileIds").withArgs(sourceSystemCd, sourceCompanyId);
        }
		public function getAvailableQuickBooksFileIds(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("getAvailableQuickBooksFileIds", sourceSystemCd, sourceCompanyId);
			sendAsyncResult(responder,"getAvailableQuickBooksFileIds");
        }

		public function expectsUpdateQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, feeCoa:String, saleTaxCoa:String, fileId:String):Mock {
            return expects("updateQuickbooksInfo").withArgs(sourceSystemCd, sourceCompanyId, feeCoa, saleTaxCoa, fileId);
        }
		public function updateQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, feeCoa:String, saleTaxCoa:String, fileId:String, responder:IResponder):void {
            record("updateQuickbooksInfo", sourceSystemCd, sourceCompanyId, feeCoa, saleTaxCoa, fileId);
			sendAsyncResult(responder,"updateQuickbooksInfo");
        }

        public function getEntityChange(pSourceSystemCode:String, pCompanyId:String, oldEin:String, newEin:String, responder:IResponder):void {
        }

        public function switchAllowTransmissions(pSourceSystemCd:String, pCompanyId:String, pAllowTransmissions:Boolean, responder:IResponder):void {
        }

        public function updateEntityChange(pSourceSystemCd:String, pCompanyId:String, pEntityChange:EntityChange, responder:IResponder):void {
        }

        public function getWorkersCompServiceInfo(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
        }

		public function expectsRemoveInvalidFlagOnEmailAddresses(pSourceSystemCd:String, pSourceCompanyId:String, pEmailAddress:String):Mock {
            return expects("removeInvalidFlagOnEmailAddresses").withArgs(pSourceSystemCd, pSourceCompanyId, pEmailAddress);
        }
		public function removeInvalidFlagOnEmailAddresses(pSourceSystemCd:String, pSourceCompanyId:String, pEmailAddress:String, responder:IResponder):void {
            record("removeInvalidFlagOnEmailAddresses", pSourceSystemCd, pSourceCompanyId, pEmailAddress);
			sendAsyncResult(responder,"removeInvalidFlagOnEmailAddresses");
        }
    }
}
