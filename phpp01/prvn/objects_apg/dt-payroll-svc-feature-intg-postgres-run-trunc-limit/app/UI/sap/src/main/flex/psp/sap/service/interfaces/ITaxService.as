package psp.sap.service.interfaces
{
    import flash.utils.ByteArray;

    import mx.collections.ArrayCollection;
    import mx.rpc.IResponder;

    import psp.sap.model.DataSyncItems;
    import psp.sap.model.FilerType;
    import psp.sap.model.LedgerItemDetailsCriterion;
    import psp.sap.model.PaymentSearch;
    import psp.sap.model.Quarter;
    import psp.sap.model.QuarterCompanyFilingAmounts;
    import psp.sap.model.QuarterLawRates;
    import psp.sap.model.RAFEnrollmentSearch;
    import psp.sap.model.TaxRepaymentOptions;

    public interface ITaxService extends IPSPService
    {

        function getAgencyList(responder:IResponder):void;

        function getPaymentTemplateYears(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean, responder:IResponder):void;

        function getTemplateYearPayment(sourceSystemCd:String, companyId:String, taxYear:String, paymentTemplateCd:String, responder:IResponder):void;

        function getPaymentTemplateQuarterPayment(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, year:String, quarter:String, responder:IResponder):void;

        function findPaymentDetailTransactions(moneyMovementTransactionId:String, companyId:String, responder:IResponder):void;

        function findCompanyTaxYears(sourceSystemCd:String, companyId:String, responder:IResponder):void;

        function findCompanyAgencies(sourceSystemCd:String, companyId:String, responder:IResponder):void;

        function findTaxTransactions(sourceSystemCd:String, companyId:String, transactionDescription:String, agencyCd:String, paymentTemplateCd:String, specifiedLawId:String, paymentMethod:String, yearQuarterStartDate:Date, yearQuarterEndDate:Date, includeNotPostedPayments:Boolean, responder:IResponder):void;

        function findEmployeeLedgerItems(pLedgerItemDetailsCriterion:LedgerItemDetailsCriterion, responder:IResponder):void;

        function getManualLedgerLines(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, specifiedLawId:String, checkDate:Date, responder:IResponder):void;

        function createManualLedgerEntry(sourceSystemCd:String, companyId:String, entryType:String, lines:ArrayCollection, checkDate:Date, memo:String, recordingOption:int, datePaid:Date, allowLimitOutsideOfBoundaries:Boolean, responder:IResponder):void;

        function getEFTPSEnrollmentRejections(responder:IResponder):void;

        function getEftpsEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function initiateReEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function createManualEFTPSEnrollment(pSourceSystemCode:String, pCompanyId:String, ein:String, legalName:String, zip:String, responder:IResponder):void;

        function getLawAmounts(mmtId:String, companyId:String, responder:IResponder):void;

        function finalizePayment(mmtId:String, companyId:String, responder:IResponder):void;

        function unFinalizePayment(mmtId:String, companyId:String, responder:IResponder):void;

        function finalizePayments(searchCriteria:PaymentSearch, responder:IResponder):void;

        function updateInitiationDates(searchCriteria:PaymentSearch, newInitiationDate:Date, responder:IResponder):void;

        function updateGroupPaymentMethods(searchCriteria:PaymentSearch, paymentMethod:String, responder:IResponder):void;

        function editPaymentAmount(mmtId:String, splitFTIDs:ArrayCollection, lawAmounts:ArrayCollection, memo:String, immediateDebitOrCredit:Boolean, allowLimitOutsideOfBoundaries:Boolean, companyId:String, responder:IResponder):void;

        function findTaxPayments(searchCriteria:PaymentSearch, firstResult:int, maxResults:int, sortColumn:String, sortDescending:Boolean, responder:IResponder):void;

        function getRAFEnrollmentsByStatusAndCompany(search:RAFEnrollmentSearch, pIncludePayrollStatus:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void;

        function getRAFEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function updateRAFEnrollmentStatus(pSourceSystemCode:String, pCompanyId:String, pNewStatus:String, responder:IResponder):void;

        function rejectRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pReason:String, responder:IResponder):void;

        function initiateRAFTapeCreation(pActionCode:String, responder:IResponder):void;

		function initiateACHFileCreation(pACHEnrollmentFileType:String, pSAPQuarter:Quarter, responder:IResponder):void;

        function findEnrollmentFiles(actionCode:String, responder:IResponder):void;

		function getACHEnrollmentQuarters(actionCode:String, responder:IResponder):void;

        function reInitiateRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function deleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String, responder:IResponder):void;

        function reInitiateRAFTapeCreation(pFileId:String, responder:IResponder):void;

        function cancelDeleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String, responder:IResponder):void;

        function enrollAllRAFEnrollments(enrollmentCriteria:RAFEnrollmentSearch, responder:IResponder):void;

		function findACHEnrollments(status:String, pFirstResult:int, pMaxResults:int, sortColumn:String, sortDescending:Boolean, responder:IResponder):void;

        function uploadACHResponseFile(fileName:String, file:ByteArray, responder:IResponder):void;

        function updateACHEnrollmentAsEnrolled(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function reInitiateACHEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function deleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String, responder:IResponder):void;

        function cancelDeleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String, responder:IResponder):void;

        function getACHEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function getAgencyInfoArray(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function getCompanyFilingAmountHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void;

        function getAdditionalAgencyIdsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void;

        function getPaymentMethodsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, pFieldName:String, responder:IResponder):void;

        function getAgencyIdHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void;

        function getCompanyAgencyHistory(pSourceSystemCode:String, pSourceCompanyId:String, pAgencyId:String, responder:IResponder):void;

        function updateErFicaDeferral(sourceSystemCd:String, sourceCompanyId:String, enabled:Boolean, responder:IResponder):void;

        function getCompanyLawRatesHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void;

        function getDepositFrequencyHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void;

        function getDefaultDepositFrequency(pPaymentTemplateCd:String, responder:IResponder):void;

        function getFilerTypeHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void;

        function updateFilerType(pSourceSystemCd:String, pSourceSystemId:String, filerType:FilerType, responder:IResponder):void;

        function updateAgentEnabled(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agentEnabled:Boolean, responder:IResponder):void;

		function findCompanyLaws(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void;

		function editCompanyLawAgencyId(pSourceSystemCd:String, pSourceSystemId:String, sourceId:String, agencyId:String, responder:IResponder):void;

        function updatePayDate(paymentId:String, pNewPayDate:Date, companyId:String, responder:IResponder):void;

        function getStatusHistoryData(paymentId:String, companyId:String, responder:IResponder):void;

        function getHoldsHistoryData(paymentId:String, companyId:String, responder:IResponder):void;

        function getPaymentsPayDateAuditHistory(paymentId:String, companyId:String, responder:IResponder):void;

        function getPaymentAmountDetails(sourceSystemCd:String, companyId:String, paymentId:String, responder:IResponder):void;

        function getNextInitiationDate(pPaymentMethod:String, responder:IResponder):void;

        function getOffloadDate(pPaymentMethod:String,pPaymentTemplate:String, responder:IResponder):void;

        function addTaxPaymentAgentOnHoldReason(paymentId:String, companyId:String, responder:IResponder):void;

        function removePaymentOnHoldReason(paymentId:String, holdReasonCd:String, companyId:String, responder:IResponder):void;

        function updatePaymentMethod(paymentId:String, pPaymentMethod:String, companyId:String, responder:IResponder):void;

        function getPaymentMethodAuditHistory(paymentId:String, companyId:String, responder:IResponder):void;

        function rejectPayment(paymentId:String, reason:String, companyId:String, responder:IResponder):void;

        function initiateRepayment(paymentId:String, options:TaxRepaymentOptions, companyId:String, responder:IResponder):void;

        function getCompanyAgencyTemplates(pSourceSystemCode:String, pSourceCompanyId:String, pShowForThisCompanyOnly:Boolean, responder:IResponder):void;

        function getValidPaymentMethods(paymentId:String, companyId:String, responder:IResponder):void;

        function getValidPaymentMethodsByTemplate(paymentTemplateCd:String, responder:IResponder):void;

        function getMoneyMovementTransactionsForVerification(pSourceSystemCode:String, pCompanyId:String, pInitiationStartDate:Date, pInitiationEndDate:Date, pTotalAmountFrom:String, pTotalAmountTo:String, pRelatedAmountFrom:String, pRelatedAmountTo:String, pStateTemplate:String, responder:IResponder):void;

        function getSupportedPaymentTemplates(responder:IResponder):void;

        function getSupportedPaymentTemplatesForCompany(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void;

        function getDataSyncDetails(sourceSystemCd:String, sourceCompanyId:String, itemType:String, idSearchTypeString:String, fromId:int, toId:int, typeString:String, fromDate:Date, toDate:Date, checkNumber:String, amount:String, pItemName:String, pageSize:int, orderBy:String, descending:Boolean, firstResult:int, responder:IResponder):void;

        function getQBDTTokens(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

        function updatedDataSyncTokensOnSelectedItems(sourceSystemCd:String, sourceCompanyId:String, items:DataSyncItems, action:String, undelete:Boolean, comment:String, caseId:String, responder:IResponder):void;

        function updateDataSyncTokens(sourceSystemCd:String, sourceCompanyId:String, action:String, actions:ArrayCollection, comment:String, pCaseId:String, responder:IResponder):void;

        function findRefundTransactions(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

        function findCourtesyRefundTransactions(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void;

        function createPenaltiesAndInterestRefunds(sourceSystemCd:String, sourceCompanyId:String, penaltiesRefundAmount:Number, interestRefundAmount:Number, note:String, settlementTypeCd:String, responder:IResponder):void;

        function createRefundDebit(pFinancialTransactionId:String, pNote:String, pSettlementTypeCd:String, responder:IResponder, companyId:String):void;

        function createCourtesyRefund(pSourceSystemCd:String, pSourceSystemId:String, pRefundAmount:Number, pNote:String, pSettlementTypeCd:String, responder:IResponder):void;

        function getAgencyTaxRefundBreakdown(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void;

        function createTORTransactions(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, quarter:Quarter, responder:IResponder):void;

        function findEditableQuarterRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function getFirstTaxYear(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void;

        function findAllEditableRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function updateRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:QuarterLawRates, pushToQuickbooks:Boolean, responder:IResponder):void;

        function getPaymentTemplateQuarters(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean, responder:IResponder):void;

        function updateAllRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:ArrayCollection, pushToQuickbooks:Boolean, responder:IResponder):void;

        function findEditableAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function updateAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, amounts:QuarterCompanyFilingAmounts, responder:IResponder):void;

        function getAllDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function updateDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, depositFrequencies:ArrayCollection, responder:IResponder):void;

        function findAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function checkAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection, responder:IResponder):void;

        function updateAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection, responder:IResponder):void;

        function getAllLawFlags(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function updateLawFlags(pSourceSystemCd:String, pSourceSystemId:String, lawFlags:ArrayCollection, responder:IResponder):void;

        function getLawFlagHistory(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void;

        function getFilerType(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void;

		function hasNonNumericSourceIds(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

		function getManualLedgerLimit(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void;

        function createPendingTaxRefund(sourceSystemCd:String, sourceCompanyId:String , paymentId:String, reason:String, responder:IResponder):void;

    }
}

