package psp.sap.service
{
import flash.utils.ByteArray;

import mx.collections.ArrayCollection;
import mx.logging.ILogger;
import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.rpc.remoting.RemoteObject;

import psp.sap.application.ClientLoggingTarget;
import psp.sap.model.AddCompany;
import psp.sap.model.CompanyDdLimits;
import psp.sap.model.CompanyKey;
import psp.sap.model.CompanyLegalInfo;
import psp.sap.model.CompanyNote;
import psp.sap.model.EntityChange;
import psp.sap.model.QBDTTokens;
import psp.sap.model.TaxCompanyServiceInfo;
import psp.sap.model.TaxExemptInfo;
import psp.sap.service.interfaces.ICompanyService;

public class CompanyService
		extends AbstractCompanyService
		implements ICompanyService
	{

		private var logger:ILogger = ClientLoggingTarget.getLogger(this);

		public function CompanyService()
		{
			super();
			remoteObjectPool = new RemoteObjectPool("companyservice", 4, true);
		}

		public function get companyRemoteService():RemoteObject {
			return remoteObjectPool.nextAvailable();
		}

		public function search(searchMethod:String, searchInput:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.search(searchMethod, searchInput));
			remoteToken.addResponder(responder);
        }

		public function findCompany(source:String, id:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompany(source, id));
			remoteToken.addResponder(responder);
        }

		public function getCompanyCancellationInfo(source:String, id:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyCancellationInfo(source, id));
			remoteToken.addResponder(responder);
        }

		public function updateCompanyCancellationInfo(pSource:String, pId:String, pSapTaxCompanyServiceInfo:TaxCompanyServiceInfo, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.updateCompanyCancellationInfo(pSource, pId, pSapTaxCompanyServiceInfo));
			remoteToken.addResponder(responder);
        }

		public function getStrikeInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getStrikeInfo(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getPayrollRunCount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getPayrollRunCount(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getBankReturnTransactionCount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getBankReturnTransactionCount(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getPINInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getPINInfo(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getFundingModelCd(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getFundingModelCd(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getActiveBankAccount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getActiveBankAccount(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function isDebugLogging(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.isDebugLogging(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateCompanyFundingModel(pSourceSystemCd:String, pCompanyId:String, pFundingModelCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.updateCompanyFundingModel(pSourceSystemCd, pCompanyId, pFundingModelCd));
			remoteToken.addResponder(responder);
        }

		public function addCompanyNote(pSourceSystemCd:String, pCompanyId:String, companyEventId:String, companyEventTransmissionId:String, sapCompanyNote:CompanyNote, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addCompanyNote(pSourceSystemCd, pCompanyId, companyEventId, companyEventTransmissionId, sapCompanyNote));
			remoteToken.addResponder(responder);
        }

		public function removeCompanyNoteAlert(noteId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.removeCompanyNoteAlert(noteId));
			remoteToken.addResponder(responder);
        }

		public function getMostRecentAlertNote(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getMostRecentAlertNote(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function addEntitlementUnitToCompany(sourceSystemCd:String, sourceCompanyId:String, licenseNumber:String, eoc:String, itemNumber:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addEntitlementUnitToCompany(sourceSystemCd, sourceCompanyId, licenseNumber, eoc, itemNumber));
			remoteToken.addResponder(responder);
        }

		public function getPriceType(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getPriceType(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

        public function getAvailablePriceTypesByCompanyKey(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
      			var remoteToken:AsyncToken =
      				AsyncToken(companyRemoteService.getAvailablePriceTypes(sourceSystemCd, sourceCompanyId));
      			remoteToken.addResponder(responder);
        }

        public function getAvailablePriceTypesByItemNumber(itemNumber:String, responder:IResponder):void {
      			var remoteToken:AsyncToken =
      				AsyncToken(companyRemoteService.getAvailablePriceTypes(itemNumber));
      			remoteToken.addResponder(responder);
        }

		public function setAssistedPriceTypeAndOffer(sourceSystemCd:String, sourceCompanyId:String, priceType:String, offerCode:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.setAssistedPriceTypeAndOffer(sourceSystemCd, sourceCompanyId, priceType, offerCode));
			remoteToken.addResponder(responder);
        }

		public function getAssistedOffer(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getAssistedOffer(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function deactivateEntitlementUnit(id:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.deactivateEntitlementUnit(id));
			remoteToken.addResponder(responder);
        }

		public function reactivateEntitlementUnit(id:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.reactivateEntitlementUnit(id));
			remoteToken.addResponder(responder);
        }

		public function moveEntitlementUnit(fromEntitlementId:String, toLicenseNumber:String, toEoc:String, toItemNumber:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.moveEntitlementUnit(fromEntitlementId, toLicenseNumber, toEoc, toItemNumber));
			remoteToken.addResponder(responder);
        }

		public function getAssetInfo(itemNumber:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getAssetInfo(itemNumber));
			remoteToken.addResponder(responder);
        }

		public function getEntityChangeHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getEntityChangeHistory(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

        public function getEntityChange(pSourceSystemCode:String, pCompanyId:String, oldEin:String, newEin:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(companyRemoteService.getEntityChange(pSourceSystemCode, pCompanyId, oldEin, newEin));
            remoteToken.addResponder(responder);
        }

		public function addCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeDate:Date, pStrikeReason:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addCompanyStrike(pSourceSystemCd, pCompanyId, pStrikeDate, pStrikeReason));
			remoteToken.addResponder(responder);
        }

		public function cancelCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.cancelCompanyStrike(pSourceSystemCd, pCompanyId, pStrikeId));
			remoteToken.addResponder(responder);
        }

		public function getLimitViolationEvents(pCompanyId:String, pSourceSystemCd:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getLimitViolationEvents(pCompanyId, pSourceSystemCd, pFromDate, pToDate));
			remoteToken.addResponder(responder);
        }

		public function getCompanyBankAccountsHistory(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyBankAccountsHistory(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getBankVerificationLimit(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getBankVerificationLimit(pCompanyId, pSourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function getRandomDebitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourceBankAccountId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getRandomDebitTransactions(pCompanyId, pSourceSystemCd, pSourceBankAccountId));
			remoteToken.addResponder(responder);
        }

		public function reinitiateRandomDebit(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.reinitiateRandomDebit(pCompanyId, pSourceSystemCd, pBankAccountId));
			remoteToken.addResponder(responder);
        }

		public function isPendingRandomDebit(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.isPendingRandomDebit(pCompanyId, pSourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function verifyCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.verifyCompanyBankAccount(pCompanyId, pSourceSystemCd, pBankAccountId));
			remoteToken.addResponder(responder);
        }

		public function generateRandomPin(pSourceSystemCd:String, pSourceCompanyId:String, pCaseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.generateRandomPin(pSourceSystemCd, pSourceCompanyId, pCaseId));
			remoteToken.addResponder(responder);
        }

		public function unlockCompany(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.unlockCompany(pSourceSystemCd, pSourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function findTransmissions(pSourceSystemCd:String, pSourceCompanyId:String, pFromDate:Date, pToDate:Date, pFromSourceSystemCode:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findTransmissions(pSourceSystemCd, pSourceCompanyId, pFromDate, pToDate, pFromSourceSystemCode));
			remoteToken.addResponder(responder);
        }

		public function findTransmissionById(pTransmissionId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findTransmissionById(pTransmissionId));
			remoteToken.addResponder(responder);
        }

        public function findTransmissionByIPAndDate(pIPAddress:String,pFromDate:Date,pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.findTransmissionByIPAndDate(pIPAddress,pFromDate,pToDate ));
            remoteToken.addResponder(responder);
        }

		public function removeFraudFlag(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.removeFraudFlag(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function switchDebugLogging(pSourceSystemCd:String, pCompanyId:String, pDebugLogging:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.switchDebugLogging(pSourceSystemCd, pCompanyId, pDebugLogging));
			remoteToken.addResponder(responder);
        }

        public function switchProcessTransmissions(pSourceSystemCd:String, pCompanyId:String, pProcessTransmissions:Boolean, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.switchProcessTransmissions(pSourceSystemCd, pCompanyId, pProcessTransmissions));
            remoteToken.addResponder(responder);
        }

        public function switchAllowTransmissions(pSourceSystemCd:String, pCompanyId:String, pAllowTransmissions:Boolean, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.switchAllowTransmissions(pSourceSystemCd, pCompanyId, pAllowTransmissions));
            remoteToken.addResponder(responder);
        }

		public function findCompanyFraudEvents(pEinCid:String, pFraudEventCategory:String, pPayrollAmount:Number, pFromDate:Date, pToDate:Date, eventTypeCodes:ArrayCollection, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompanyFraudEvents(pEinCid, pFraudEventCategory, pPayrollAmount, pFromDate, pToDate, eventTypeCodes));
			remoteToken.addResponder(responder);
        }

		public function getFraudEventTypes(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getFraudEventTypes());
			remoteToken.addResponder(responder);
        }

		public function findCompanyNotes(pSourceSystemCd:String, pCompanyId:String, pCompanyEventId:String, pCompanyEventTransmissionId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompanyNotes(pSourceSystemCd, pCompanyId, pCompanyEventId, pCompanyEventTransmissionId));
			remoteToken.addResponder(responder);
        }

		public function findManualNotes(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findManualNotes(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function findCompanyEventGroups(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompanyEventGroups(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function findCompanyEventCreators(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompanyEventCreators(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function findCompanyEvents(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, creatorId:String, eventTypes:ArrayCollection, includeAS400Events:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompanyEvents(pSourceSystemCd, pCompanyId, pFromDate, pToDate, creatorId, eventTypes, includeAS400Events));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeBankAccountHistory(pCompanyId:String, pEmployeeId:String, pSourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getEmployeeBankAccountHistory(pCompanyId, pEmployeeId, pSourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function getCloudEmployees(pCompanyId:String, pSourceSystemCd:String, pUserViewsNonServiceableData:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCloudEmployees(pCompanyId, pSourceSystemCd, pUserViewsNonServiceableData));
			remoteToken.addResponder(responder);
        }

		public function getVendors(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getVendors(pCompanyId, pSourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function deactivateCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.deactivateCompanyBankAccount(pCompanyId, pSourceSystemCd, pBankAccountId));
			remoteToken.addResponder(responder);
        }

		public function saveCompanyService(pSourceSystemCd:String, pCompanyId:String, pServiceCode:String, pSubStatuses:ArrayCollection, pSAPCompanyDdLimits:CompanyDdLimits, pFundingModelCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.saveCompanyService(pSourceSystemCd, pCompanyId, pServiceCode, pSubStatuses, pSAPCompanyDdLimits, pFundingModelCd));
			remoteToken.addResponder(responder);
        }

		public function getVendorBankAccountHistory(pSourceSystemCd:String, pCompanyId:String, pVendorId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getVendorBankAccountHistory(pSourceSystemCd, pCompanyId, pVendorId));
			remoteToken.addResponder(responder);
        }

		public function getEmployees(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getEmployees(pCompanyId, pSourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function getEmployee(pCompanyId:String, pSourceSystemCd:String, sourceEmployeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getEmployee(pCompanyId, pSourceSystemCd, sourceEmployeeId));
			remoteToken.addResponder(responder);
        }

		public function getCompanyDisplayStatus(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyDisplayStatus(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getCompanyStatus(pSourceSystemCd:String, pCompanyId:String, findTransitions:Boolean, findLimitViolations:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyStatus(pSourceSystemCd, pCompanyId, findTransitions, findLimitViolations));
			remoteToken.addResponder(responder);
        }

		public function getFeeOfferingServiceChargePrices(pSourceSystemCd:String, pCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getFeeOfferingServiceChargePrices(pSourceSystemCd, pCompanyId, pPayrollRunId));
			remoteToken.addResponder(responder);
        }

		public function editBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.editBankAccount(pSourceSystemCd, pSourceCompanyId, pCompanyBankAccountID, pSourceBankAccountName, pAccountNumber, pRoutingNumber, pAccountType, pBankName));
			remoteToken.addResponder(responder);
        }

		public function addBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, pShouldAddRandomDebits:Boolean, pShouldAllowPendingTransactions:Boolean, pShouldMovePendingTransactionsToAccount:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addBankAccount(pSourceSystemCd, pSourceCompanyId, pCompanyBankAccountID, pSourceBankAccountName, pAccountNumber, pRoutingNumber, pAccountType, pBankName, pShouldAddRandomDebits, pShouldAllowPendingTransactions, pShouldMovePendingTransactionsToAccount));
			remoteToken.addResponder(responder);
        }

		public function resetVerifyAttempts(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.resetVerifyAttempts(pCompanyId, pSourceSystemCd, pBankAccountId));
			remoteToken.addResponder(responder);
        }

		public function getCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyBankAccount(pCompanyId, pSourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function getRecentCompanyEvents(pSourceSystemCd:String, pCompanyId:String, max:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getRecentCompanyEvents(pSourceSystemCd, pCompanyId, max));
			remoteToken.addResponder(responder);
        }

		public function resendEmail(pSourceSystemCd:String, pCompanyId:String, emailId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.resendEmail(pSourceSystemCd, pCompanyId, emailId));
			remoteToken.addResponder(responder);
        }

		public function sendEmailToMtl(pSourceSystemCd:String, pCompanyId:String, emailSeqId:String, sessionUserEmailAddress:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(companyRemoteService.sendEmailToMtl(pSourceSystemCd, pCompanyId, emailSeqId, sessionUserEmailAddress));
			remoteToken.addResponder(responder);
		}

		public function findCompanyServiceStatusHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompanyServiceStatusHistory(pCompanyId, pSourceSystemId, pFromDate));
			remoteToken.addResponder(responder);
        }

		public function adjustCompanyTokens(pSourceSystemCode:String, pSourceCompanyId:String, tokens:QBDTTokens, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.adjustCompanyTokens(pSourceSystemCode, pSourceCompanyId, tokens));
			remoteToken.addResponder(responder);
        }

		public function getCompaniesByServiceSubstatuses(pSubStatuses:ArrayCollection, searchForOnHoldStatuses:Boolean, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompaniesByServiceSubstatuses(pSubStatuses, searchForOnHoldStatuses, pOrderBy, pOrderDesc, pFirstResult, pMaxResults));
			remoteToken.addResponder(responder);
        }

		public function getCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyLegalInfo(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, pLegalInfo:CompanyLegalInfo, pCaseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.updateCompanyLegalInfo(pSourceSystemCd, pCompanyId, pLegalInfo, pCaseId));
			remoteToken.addResponder(responder);
        }

        public function updateEntityChange(pSourceSystemCd:String, pCompanyId:String, pEntityChange:EntityChange, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(companyRemoteService.updateEntityChange(pSourceSystemCd,pCompanyId,pEntityChange));
            remoteToken.addResponder(responder);
        }

		public function getCompanyContacts(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyContacts(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateCompanyContacts(pSourceSystemCd:String, pCompanyId:String, pContacts:ArrayCollection, pCaseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.updateCompanyContacts(pSourceSystemCd, pCompanyId, pContacts, pCaseId));
			remoteToken.addResponder(responder);
        }

		public function getTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getTaxExemptStatus(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, info:TaxExemptInfo, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.updateTaxExemptStatus(pSourceSystemCd, pCompanyId, info));
			remoteToken.addResponder(responder);
        }

		public function getServiceStatusList(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getServiceStatusList());
			remoteToken.addResponder(responder);
        }

		public function getServiceSubStatusList(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getServiceSubStatusList());
			remoteToken.addResponder(responder);
        }

		public function getFundingModelList(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getFundingModelList());
			remoteToken.addResponder(responder);
        }

		public function getSourceSystemList(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getSourceSystemList());
			remoteToken.addResponder(responder);
        }

		public function getCompanyOffers(sourceSystemCd:String, companyId:String, serviceCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanyOffers(sourceSystemCd, companyId, serviceCd));
			remoteToken.addResponder(responder);
        }

		public function addServiceToCompany(pSourceSystemCd:String, pCompanyId:String, pServiceCd:String, caseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addServiceToCompany(pSourceSystemCd, pCompanyId, pServiceCd, caseId));
			remoteToken.addResponder(responder);
        }

		public function findCheckPrintingBatches(pEinPsid:String, pBatchStatus:String, pCheckFromDate:Date, pCheckToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCheckPrintingBatches(pEinPsid, pBatchStatus, pCheckFromDate, pCheckToDate, pPrintFromDate, pPrintToDate, pOrderBy, pOrderDesc, pFirstResult, pMaxResults));
			remoteToken.addResponder(responder);
        }

		public function findAgencyPrintingBatches(pPaymentTemplateCd:String, pBatchStatus:String, pInitiationFromDate:Date, pInitiationToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findAgencyPrintingBatches(pPaymentTemplateCd, pBatchStatus, pInitiationFromDate, pInitiationToDate, pPrintFromDate, pPrintToDate, pOrderBy, pOrderDesc, pFirstResult, pMaxResults));
			remoteToken.addResponder(responder);
        }

		public function savePrintBatchStatus(pCheckPrintBatchId:String, pNewBatchStatus:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.savePrintBatchStatus(pCheckPrintBatchId, pNewBatchStatus));
			remoteToken.addResponder(responder);
        }

		public function uploadSignatureFile(sourceSystemCd:String, companyId:String, signatureImage:ByteArray, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.uploadSignatureFile(sourceSystemCd, companyId, signatureImage));
			remoteToken.addResponder(responder);
        }

		public function getCompanySignatureImage(sourceSystemCd:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getCompanySignatureImage(sourceSystemCd, companyId));
			remoteToken.addResponder(responder);
        }

		public function addCheckPrintTestBatch(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addCheckPrintTestBatch(pSourceSystemCd, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function findBankAccounts(pRoutingNumber:String, pAccountNumber:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findBankAccounts(pRoutingNumber, pAccountNumber, pOrderBy, pOrderDesc, pFirstResult, pMaxResults));
			remoteToken.addResponder(responder);
        }

		public function findCurrentEINs(licenseNumber:String, eoc:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCurrentEINs(licenseNumber, eoc));
			remoteToken.addResponder(responder);
        }

		public function hasActiveEINsForLicenseExceededMaxAllowed(licenseNumber:String, eoc:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(companyRemoteService.hasActiveEINsForLicenseExceededMaxAllowed(licenseNumber, eoc));
			remoteToken.addResponder(responder);
		}

		public function findEntitlementUnits(ein:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findEntitlementUnits(ein));
			remoteToken.addResponder(responder);
        }

		public function findCompaniesByEIN(ein:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.findCompaniesByEIN(ein));
			remoteToken.addResponder(responder);
        }

		public function getLicenseFromOrderNumber(orderNumber:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getLicenseFromOrderNumber(orderNumber));
			remoteToken.addResponder(responder);
        }

		public function getEntitlementInfo(licenseNumber:String, eoc:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getEntitlementInfo(licenseNumber, eoc));
			remoteToken.addResponder(responder);
        }

		public function getEntitlementUnits(sourceSystemCd:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getEntitlementUnits(sourceSystemCd, companyId));
			remoteToken.addResponder(responder);
        }

		public function syncEntitlementUnit(entitlementUnitId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.syncEntitlementUnitFromSourceSystems(entitlementUnitId));
			remoteToken.addResponder(responder);
        }

        public function updateSubscriptionEndDate(entitlementUnitId:String, responder:IResponder):void {
      			var remoteToken:AsyncToken =
      				AsyncToken(companyRemoteService.updateSubscriptionEndDate(entitlementUnitId));
      			remoteToken.addResponder(responder);
              }

		public function getAdditionalContacts(licenseNumber:String, eoc:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getAdditionalContacts(licenseNumber, eoc));
			remoteToken.addResponder(responder);
        }

		public function getAdditionalAddresses(licenseNumber:String, eoc:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getAdditionalAddresses(licenseNumber, eoc));
			remoteToken.addResponder(responder);
        }

		public function addCompany(addCompany:AddCompany, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.addCompany(addCompany));
			remoteToken.addResponder(responder);
        }

		public function reCalculateLedgerBalances(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.reCalculateLedgerBalances(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getQuickbooksInfo(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getAvailableQuickBooksFileIds(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.getAvailableQuickBooksFileIds(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, feeCoa:String, saleTaxCoa:String, fileId:String, pCaseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.updateQuickbooksInfo(sourceSystemCd, sourceCompanyId, feeCoa, saleTaxCoa, fileId, pCaseId));
			remoteToken.addResponder(responder);
        }

        public function getWorkersCompServiceInfo(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.getWorkersCompServiceInfo(sourceSystemCd, sourceCompanyId));
            remoteToken.addResponder(responder);
        }

		public function removeInvalidFlagOnEmailAddresses(pSourceSystemCd:String, pSourceCompanyId:String, pEmailAddress:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(companyRemoteService.removeInvalidFlagOnEmailAddresses(pSourceSystemCd, pSourceCompanyId, pEmailAddress));
			remoteToken.addResponder(responder);
        }

        public function getIndustryTypes(responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.getIndustryTypes());
            remoteToken.addResponder(responder);
        }

        public function deleteVMPData(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.deleteVMPData(sourceSystemCd, sourceCompanyId));
            remoteToken.addResponder(responder);
        }

        public function getOfferByItemNumber(itemNumber:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.getOfferByItemNumber(itemNumber));
            remoteToken.addResponder(responder);
        }

        public function getOfferByCompanyKey(companyKey:CompanyKey, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.getOfferByCompanyKey(companyKey));
            remoteToken.addResponder(responder);
        }

        public function checkIfOpenLedgerOperationFTsExist(companyKey:CompanyKey, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.checkIfOpenLedgerOperationFTsExist(companyKey));
            remoteToken.addResponder(responder);
        }
        public function getOfferByPriceType(itemNumber:String, responder:IResponder):void{
            var remoteToken:AsyncToken =
                    AsyncToken(companyRemoteService.getOfferByPriceType(itemNumber));
            remoteToken.addResponder(responder);

        }
    }
}
