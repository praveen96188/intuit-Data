package psp.sap.service.interfaces
{
import flash.utils.ByteArray;

import mx.collections.ArrayCollection;
import mx.rpc.IResponder;

import psp.sap.model.AddCompany;
import psp.sap.model.CompanyDdLimits;
import psp.sap.model.CompanyKey;
import psp.sap.model.CompanyLegalInfo;
import psp.sap.model.CompanyNote;
import psp.sap.model.EntityChange;
import psp.sap.model.QBDTTokens;
import psp.sap.model.TaxCompanyServiceInfo;
import psp.sap.model.TaxExemptInfo;

public interface ICompanyService extends IPSPService
	{

		function search(searchMethod:String, searchInput:String, responder:IResponder):void;

		function findCompany(source:String, id:String, responder:IResponder):void;

		function getCompanyCancellationInfo(source:String, id:String, responder:IResponder):void;

		function updateCompanyCancellationInfo(pSource:String, pId:String, pSapTaxCompanyServiceInfo:TaxCompanyServiceInfo, responder:IResponder):void;

		function getStrikeInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getPayrollRunCount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getBankReturnTransactionCount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getPINInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getFundingModelCd(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getActiveBankAccount(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function isDebugLogging(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function updateCompanyFundingModel(pSourceSystemCd:String, pCompanyId:String, pFundingModelCd:String, responder:IResponder):void;

		function addCompanyNote(pSourceSystemCd:String, pCompanyId:String, companyEventId:String, companyEventTransmissionId:String, sapCompanyNote:CompanyNote, responder:IResponder):void;

		function removeCompanyNoteAlert(noteId:String, responder:IResponder):void;

		function getMostRecentAlertNote(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function addEntitlementUnitToCompany(sourceSystemCd:String, sourceCompanyId:String, licenseNumber:String, eoc:String, itemNumber:String, responder:IResponder):void;

		function getPriceType(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

        function getIndustryTypes(responder:IResponder):void;

        function getAvailablePriceTypesByCompanyKey(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void

        function getAvailablePriceTypesByItemNumber(itemNumber:String, responder:IResponder):void

		function setAssistedPriceTypeAndOffer(sourceSystemCd:String, sourceCompanyId:String, priceType:String, offerCode:String, responder:IResponder):void;

		function getAssistedOffer(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

		function deactivateEntitlementUnit(id:String, responder:IResponder):void;

		function reactivateEntitlementUnit(id:String, responder:IResponder):void;

		function moveEntitlementUnit(fromEntitlementId:String, toLicenseNumber:String, toEoc:String, toItemNumber:String, responder:IResponder):void;

		function getAssetInfo(itemNumber:String, responder:IResponder):void;

		function getEntityChangeHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function getEntityChange(pSourceSystemCode:String, pCompanyId:String, oldEin:String, newEin:String, responder:IResponder):void;

		function addCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeDate:Date, pStrikeReason:String, responder:IResponder):void;

		function cancelCompanyStrike(pSourceSystemCd:String, pCompanyId:String, pStrikeId:String, responder:IResponder):void;

		function getLimitViolationEvents(pCompanyId:String, pSourceSystemCd:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function getCompanyBankAccountsHistory(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getBankVerificationLimit(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function getRandomDebitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourceBankAccountId:String, responder:IResponder):void;

		function reinitiateRandomDebit(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void;

		function isPendingRandomDebit(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function verifyCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void;

		function generateRandomPin(pSourceSystemCd:String, pSourceCompanyId:String, pCaseId:String, responder:IResponder):void;

		function unlockCompany(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void;

		function findTransmissions(pSourceSystemCd:String, pSourceCompanyId:String, pFromDate:Date, pToDate:Date, pFromSourceSystemCode:String, responder:IResponder):void;

		function findTransmissionById(pTransmissionId:String, responder:IResponder):void;

        function findTransmissionByIPAndDate(pIPAddress:String,pFromDate:Date,pToDate:Date, responder:IResponder):void;

		function removeFraudFlag(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function switchDebugLogging(pSourceSystemCd:String, pCompanyId:String, pDebugLogging:Boolean, responder:IResponder):void;

        function switchProcessTransmissions(pSourceSystemCd:String, pCompanyId:String, pProcessTransmissions:Boolean, responder:IResponder):void;

        function switchAllowTransmissions(pSourceSystemCd:String, pCompanyId:String, pAllowTransmissions:Boolean, responder:IResponder):void;

		function findCompanyFraudEvents(pEinCid:String, pFraudEventCategory:String, pPayrollAmount:Number, pFromDate:Date, pToDate:Date, eventTypeCodes:ArrayCollection, responder:IResponder):void;

		function getFraudEventTypes(responder:IResponder):void;

		function findCompanyNotes(pSourceSystemCd:String, pCompanyId:String, pCompanyEventId:String, pCompanyEventTransmissionId:String, responder:IResponder):void;

		function findManualNotes(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function findCompanyEventGroups(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function findCompanyEventCreators(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function findCompanyEvents(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, creatorId:String, eventTypes:ArrayCollection, includeAS400Events:Boolean, responder:IResponder):void;

		function getEmployeeBankAccountHistory(pCompanyId:String, pEmployeeId:String, pSourceSystemCd:String, responder:IResponder):void;

		function getCloudEmployees(pCompanyId:String, pSourceSystemCd:String, pUserViewsNonServiceableData:Boolean, responder:IResponder):void;

		function getVendors(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function deactivateCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void;

		function saveCompanyService(pSourceSystemCd:String, pCompanyId:String, pServiceCode:String, pSubStatuses:ArrayCollection, pSAPCompanyDdLimits:CompanyDdLimits, pFundingModelCd:String,  responder:IResponder):void;

		function getVendorBankAccountHistory(pSourceSystemCd:String, pCompanyId:String, pVendorId:String, responder:IResponder):void;

		function getEmployees(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function getEmployee(pCompanyId:String, pSourceSystemCd:String, sourceEmployeeId:String, responder:IResponder):void;

		function getCompanyDisplayStatus(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function getCompanyStatus(pSourceSystemCd:String, pCompanyId:String, findTransitions:Boolean, findLimitViolations:Boolean, responder:IResponder):void;

		function getFeeOfferingServiceChargePrices(pSourceSystemCd:String, pCompanyId:String, pPayrollRunId:String, responder:IResponder):void;

		function editBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, responder:IResponder):void;

		function addBankAccount(pSourceSystemCd:String, pSourceCompanyId:String, pCompanyBankAccountID:String, pSourceBankAccountName:String, pAccountNumber:String, pRoutingNumber:String, pAccountType:String, pBankName:String, pShouldAddRandomDebits:Boolean, pShouldAllowPendingTransactions:Boolean, pShouldMovePendingTransactionsToAccount:Boolean, responder:IResponder):void;

		function resetVerifyAttempts(pCompanyId:String, pSourceSystemCd:String, pBankAccountId:String, responder:IResponder):void;

		function getCompanyBankAccount(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function getRecentCompanyEvents(pSourceSystemCd:String, pCompanyId:String, max:int, responder:IResponder):void;

		function resendEmail(pSourceSystemCd:String, pCompanyId:String, emailId:String, responder:IResponder):void;

		function sendEmailToMtl(pSourceSystemCd:String, pCompanyId:String, emailSeqId:String, sessionUserEmailAddress:String, responder:IResponder):void;

		function findCompanyServiceStatusHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void;

		function adjustCompanyTokens(pSourceSystemCode:String, pSourceCompanyId:String, tokens:QBDTTokens, responder:IResponder):void;

		function getCompaniesByServiceSubstatuses(pSubStatuses:ArrayCollection, searchForOnHoldStatuses:Boolean, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void;

		function getCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function updateCompanyLegalInfo(pSourceSystemCd:String, pCompanyId:String, pLegalInfo:CompanyLegalInfo, pCaseId:String, responder:IResponder):void;

        function updateEntityChange(pSourceSystemCd:String, pCompanyId:String, pEntityChange:EntityChange, responder:IResponder):void;

		function getCompanyContacts(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function updateCompanyContacts(pSourceSystemCd:String, pCompanyId:String, pContacts:ArrayCollection, pCaseId:String, responder:IResponder):void;

		function getTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function updateTaxExemptStatus(pSourceSystemCd:String, pCompanyId:String, info:TaxExemptInfo, responder:IResponder):void;

		function getServiceStatusList(responder:IResponder):void;

		function getServiceSubStatusList(responder:IResponder):void;

		function getFundingModelList(responder:IResponder):void;

		function getSourceSystemList(responder:IResponder):void;

		function getCompanyOffers(sourceSystemCd:String, companyId:String, serviceCd:String, responder:IResponder):void;

		function addServiceToCompany(pSourceSystemCd:String, pCompanyId:String, pServiceCd:String, caseId:String, responder:IResponder):void;

		function findCheckPrintingBatches(pEinPsid:String, pBatchStatus:String, pCheckFromDate:Date, pCheckToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void;

		function findAgencyPrintingBatches(pPaymentTemplateCd:String, pBatchStatus:String, pInitiationFromDate:Date, pInitiationToDate:Date, pPrintFromDate:Date, pPrintToDate:Date, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void;

		function savePrintBatchStatus(pCheckPrintBatchId:String, pNewBatchStatus:String, responder:IResponder):void;

		function uploadSignatureFile(sourceSystemCd:String, companyId:String, signatureImage:ByteArray, responder:IResponder):void;

		function getCompanySignatureImage(sourceSystemCd:String, companyId:String, responder:IResponder):void;

		function addCheckPrintTestBatch(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function findBankAccounts(pRoutingNumber:String, pAccountNumber:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void;

		function findCurrentEINs(licenseNumber:String, eoc:String, responder:IResponder):void;

		function hasActiveEINsForLicenseExceededMaxAllowed(licenseNumber:String, eoc:String, responder:IResponder):void;

		function findEntitlementUnits(ein:String, responder:IResponder):void;

		function findCompaniesByEIN(ein:String, responder:IResponder):void;

		function getLicenseFromOrderNumber(orderNumber:String, responder:IResponder):void;

		function getEntitlementInfo(licenseNumber:String, eoc:String, responder:IResponder):void;

		function getEntitlementUnits(sourceSystemCd:String, companyId:String, responder:IResponder):void;

        function syncEntitlementUnit(entitlementUnitId:String, responder:IResponder):void

        function updateSubscriptionEndDate(entitlementUnitId:String, responder:IResponder):void

		function getAdditionalContacts(licenseNumber:String, eoc:String, responder:IResponder):void;

		function getAdditionalAddresses(licenseNumber:String, eoc:String, responder:IResponder):void;

		function addCompany(addCompany:AddCompany, responder:IResponder):void;

		function reCalculateLedgerBalances(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

		function getQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

		function getAvailableQuickBooksFileIds(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

		function updateQuickbooksInfo(sourceSystemCd:String, sourceCompanyId:String, feeCoa:String, saleTaxCoa:String, fileId:String, pCaseId:String, responder:IResponder):void;

        function getWorkersCompServiceInfo(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

		function removeInvalidFlagOnEmailAddresses(pSourceSystemCd:String, pSourceCompanyId:String, pEmailAddress:String, responder:IResponder):void;

        function deleteVMPData(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

        function getOfferByCompanyKey(companyKey:CompanyKey, responder:IResponder):void;

        function getOfferByItemNumber(itemNumber:String, responder:IResponder):void;

        function checkIfOpenLedgerOperationFTsExist(companykey:CompanyKey, responder:IResponder):void;

        function getOfferByPriceType(itemNumber:String, responder:IResponder):void;

    }
}
