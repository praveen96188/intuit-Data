package psp.sap.service
{
    import flash.utils.ByteArray;

    import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.remoting.mxml.RemoteObject;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.model.DataSyncItems;
    import psp.sap.model.FilerType;
    import psp.sap.model.LedgerItemDetailsCriterion;
    import psp.sap.model.PaymentSearch;
    import psp.sap.model.Quarter;
    import psp.sap.model.QuarterCompanyFilingAmounts;
    import psp.sap.model.QuarterLawRates;
    import psp.sap.model.RAFEnrollmentSearch;
    import psp.sap.model.TaxRepaymentOptions;
    import psp.sap.service.interfaces.ITaxService;

    public class TaxService extends PSPService implements ITaxService
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);

		public function TaxService():void {			
			remoteObjectPool = new RemoteObjectPool("taxservice", 2, true);
		}		
		
		public function get taxRemoteService():RemoteObject {
			return remoteObjectPool.nextAvailable();
		}

		public function getAgencyList(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAgencyList());
			remoteToken.addResponder(responder);
        }

		public function getPaymentTemplateYears(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentTemplateYears(sourceSystemCd, companyId, includePossibleBackdateYears));
			remoteToken.addResponder(responder);
        }

		public function getTemplateYearPayment(sourceSystemCd:String, companyId:String, taxYear:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getTemplateYearPayment(sourceSystemCd, companyId, taxYear, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getPaymentTemplateQuarterPayment(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, year:String, quarter:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentTemplateQuarterPayment(sourceSystemCd, companyId, paymentTemplateCd, year, quarter));
			remoteToken.addResponder(responder);
        }

		public function findPaymentDetailTransactions(moneyMovementTransactionId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findPaymentDetailTransactions(moneyMovementTransactionId, companyId));
			remoteToken.addResponder(responder);
        }

		public function findCompanyTaxYears(sourceSystemCd:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findCompanyTaxYears(sourceSystemCd, companyId));
			remoteToken.addResponder(responder);
        }

		public function findCompanyAgencies(sourceSystemCd:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findCompanyAgencies(sourceSystemCd, companyId));
			remoteToken.addResponder(responder);
        }

		public function findTaxTransactions(sourceSystemCd:String, companyId:String, transactionDescription:String, agencyCd:String, paymentTemplateCd:String, specifiedLawId:String, paymentMethod:String, yearQuarterStartDate:Date, yearQuarterEndDate:Date, includeNotPostedPayments:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findTaxTransactions(sourceSystemCd, companyId, transactionDescription, agencyCd, paymentTemplateCd, specifiedLawId, paymentMethod, yearQuarterStartDate, yearQuarterEndDate, includeNotPostedPayments));
			remoteToken.addResponder(responder);
        }

		public function findEmployeeLedgerItems(pLedgerItemDetailsCriterion:LedgerItemDetailsCriterion, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findEmployeeLedgerItems(pLedgerItemDetailsCriterion));
			remoteToken.addResponder(responder);
        }


		public function getManualLedgerLines(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, specifiedLawId:String, checkDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getManualLedgerLines(sourceSystemCd, companyId, paymentTemplateCd, specifiedLawId, checkDate));
			remoteToken.addResponder(responder);
        }

		public function createManualLedgerEntry(sourceSystemCd:String, companyId:String, entryType:String, lines:ArrayCollection, checkDate:Date, memo:String, recordingOption:int, datePaid:Date, allowLimitOutsideOfBoundaries:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.createManualLedgerEntry(sourceSystemCd, companyId, entryType, lines, checkDate, memo, recordingOption, datePaid, allowLimitOutsideOfBoundaries));
			remoteToken.addResponder(responder);
        }

		public function getEFTPSEnrollmentRejections(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getEFTPSEnrollmentRejections());
			remoteToken.addResponder(responder);
        }

		public function getEftpsEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getEftpsEnrollmentsHistory(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function initiateReEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.initiateReEnrollment(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function createManualEFTPSEnrollment(pSourceSystemCode:String, pCompanyId:String, ein:String, legalName:String, zip:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.createManualEFTPSEnrollment(pSourceSystemCode, pCompanyId, ein, legalName, zip));
			remoteToken.addResponder(responder);
        }

		public function getLawAmounts(mmtId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getLawAmounts(mmtId, companyId));
			remoteToken.addResponder(responder);
        }

		public function finalizePayment(mmtId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.finalizePayment(mmtId, companyId));
			remoteToken.addResponder(responder);
        }

		public function unFinalizePayment(mmtId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.unFinalizePayment(mmtId, companyId));
			remoteToken.addResponder(responder);
        }

		public function finalizePayments(searchCriteria:PaymentSearch, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.finalizePayments(searchCriteria));
			remoteToken.addResponder(responder);
        }

		public function updateInitiationDates(searchCriteria:PaymentSearch, newInitiationDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateInitiationDates(searchCriteria, newInitiationDate));
			remoteToken.addResponder(responder);
        }

		public function updateGroupPaymentMethods(searchCriteria:PaymentSearch, paymentMethod:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateGroupPaymentMethods(searchCriteria, paymentMethod));
			remoteToken.addResponder(responder);
        }

		public function editPaymentAmount(mmtId:String, splitFTIDs:ArrayCollection, lawAmounts:ArrayCollection, memo:String, immediateDebitOrCredit:Boolean, allowLimitOutsideOfBoundaries:Boolean, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.editPaymentAmount(mmtId, splitFTIDs, lawAmounts, memo, immediateDebitOrCredit, allowLimitOutsideOfBoundaries, companyId));
			remoteToken.addResponder(responder);
        }

		public function findTaxPayments(searchCriteria:PaymentSearch, firstResult:int, maxResults:int, sortColumn:String, sortDescending:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findTaxPayments(searchCriteria, firstResult, maxResults, sortColumn, sortDescending));
			remoteToken.addResponder(responder);
        }

		public function getRAFEnrollmentsByStatusAndCompany(search:RAFEnrollmentSearch, pIncludePayrollStatus:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getRAFEnrollmentsByStatusAndCompany(search, pIncludePayrollStatus, pFirstResult, pMaxResults));
			remoteToken.addResponder(responder);
        }

		public function getRAFEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getRAFEnrollmentsHistory(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateRAFEnrollmentStatus(pSourceSystemCode:String, pCompanyId:String, pNewStatus:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateRAFEnrollmentStatus(pSourceSystemCode, pCompanyId, pNewStatus));
			remoteToken.addResponder(responder);
        }

		public function rejectRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pReason:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.rejectRAFEnrollment(pSourceSystemCode, pCompanyId, pReason));
			remoteToken.addResponder(responder);
        }

		public function initiateRAFTapeCreation(pActionCode:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.initiateRAFTapeCreation(pActionCode));
			remoteToken.addResponder(responder);
        }

		public function initiateACHFileCreation(pACHEnrollmentFileType:String, pSAPQuarter:Quarter, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.initiateACHFileCreation(pACHEnrollmentFileType, pSAPQuarter));
			remoteToken.addResponder(responder);
        }

		public function findEnrollmentFiles(actionCode:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findEnrollmentFiles(actionCode));
			remoteToken.addResponder(responder);
        }

		public function getACHEnrollmentQuarters(actionCode:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getACHEnrollmentQuarters(actionCode));
			remoteToken.addResponder(responder);
        }

		public function reInitiateRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.reInitiateRAFEnrollment(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function deleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.deleteRAFEnrollment(pSourceSystemCode, pCompanyId, pEnrollmentID));
			remoteToken.addResponder(responder);
        }

		public function reInitiateRAFTapeCreation(pFileId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.reInitiateRAFTapeCreation(pFileId));
			remoteToken.addResponder(responder);
        }

		public function cancelDeleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.cancelDeleteRAFEnrollment(pSourceSystemCode, pCompanyId, pEnrollmentId));
			remoteToken.addResponder(responder);
        }

		public function enrollAllRAFEnrollments(enrollmentCriteria:RAFEnrollmentSearch, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.enrollAllRAFEnrollments(enrollmentCriteria));
			remoteToken.addResponder(responder);
        }

		public function findACHEnrollments(status:String, pFirstResult:int, pMaxResults:int, sortColumn:String, sortDescending:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findACHEnrollments(status, pFirstResult, pMaxResults, sortColumn, sortDescending));
			remoteToken.addResponder(responder);
        }

		public function uploadACHResponseFile(fileName:String, file:ByteArray, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.uploadACHResponseFile(fileName, file));
			remoteToken.addResponder(responder);
        }

		public function updateACHEnrollmentAsEnrolled(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateACHEnrollmentAsEnrolled(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function reInitiateACHEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.reInitiateACHEnrollment(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function deleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.deleteACHEnrollment(pSourceSystemCode, pCompanyId, pEnrollmentID));
			remoteToken.addResponder(responder);
        }

		public function cancelDeleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.cancelDeleteACHEnrollment(pSourceSystemCode, pCompanyId, pEnrollmentId));
			remoteToken.addResponder(responder);
        }

		public function getACHEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getACHEnrollmentsHistory(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getAgencyInfoArray(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAgencyInfoArray(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getCompanyFilingAmountHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getCompanyFilingAmountHistory(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getAdditionalAgencyIdsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAdditionalAgencyIdsHistory(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getPaymentMethodsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, pFieldName:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentMethodsHistory(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, pFieldName));
			remoteToken.addResponder(responder);
        }

		public function getAgencyIdHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAgencyIdHistory(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getCompanyAgencyHistory(pSourceSystemCode:String, pSourceCompanyId:String, pAgencyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(taxRemoteService.getCompanyAgencyHistory(pSourceSystemCode, pSourceCompanyId, pAgencyId));
			remoteToken.addResponder(responder);
		}

		public function updateErFicaDeferral(pSourceSystemCode:String, pSourceCompanyId:String, pEnabled:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(taxRemoteService.updateErFicaDeferral(pSourceSystemCode, pSourceCompanyId, pEnabled));
			remoteToken.addResponder(responder);
		}
    
		public function getCompanyLawRatesHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getCompanyLawRatesHistory(pSourceSystemCode, pCompanyId, pPaymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getDepositFrequencyHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getDepositFrequencyHistory(pSourceSystemCode, pCompanyId, pPaymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getDefaultDepositFrequency(pPaymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getDefaultDepositFrequency(pPaymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getFilerTypeHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getFilerTypeHistory(pSourceSystemCode, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updateFilerType(pSourceSystemCd:String, pSourceSystemId:String, filerType:FilerType, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateFilerType(pSourceSystemCd, pSourceSystemId, filerType));
			remoteToken.addResponder(responder);
        }

		public function updateAgentEnabled(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agentEnabled:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateAgentEnabled(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agentEnabled));
			remoteToken.addResponder(responder);
        }

		public function findCompanyLaws(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findCompanyLaws(pSourceSystemCd, pSourceSystemId));
			remoteToken.addResponder(responder);
        }

		public function editCompanyLawAgencyId(pSourceSystemCd:String, pSourceSystemId:String, sourceId:String, agencyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.editCompanyLawAgencyId(pSourceSystemCd, pSourceSystemId, sourceId, agencyId));
			remoteToken.addResponder(responder);
        }

		public function updatePayDate(paymentId:String, pNewPayDate:Date, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updatePayDate(paymentId, pNewPayDate, companyId));
			remoteToken.addResponder(responder);
        }

		public function getStatusHistoryData(paymentId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getStatusHistoryData(paymentId, companyId));
			remoteToken.addResponder(responder);
        }

		public function getHoldsHistoryData(paymentId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getHoldsHistoryData(paymentId, companyId));
			remoteToken.addResponder(responder);
        }

		public function getPaymentsPayDateAuditHistory(paymentId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentsPayDateAuditHistory(paymentId, companyId));
			remoteToken.addResponder(responder);
        }

		public function getPaymentAmountDetails(sourceSystemCd:String, companyId:String, paymentId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentAmountDetails(sourceSystemCd, companyId, paymentId));
			remoteToken.addResponder(responder);
        }

		public function getNextInitiationDate(pPaymentMethod:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getNextInitiationDate(pPaymentMethod));
			remoteToken.addResponder(responder);
        }

		public function getOffloadDate(pPaymentMethod:String,pPaymentTemplate:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getOffloadDate(pPaymentMethod,pPaymentTemplate));
			remoteToken.addResponder(responder);
        }

		public function addTaxPaymentAgentOnHoldReason(paymentId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.addTaxPaymentAgentOnHoldReason(paymentId, companyId));
			remoteToken.addResponder(responder);
        }

		public function removePaymentOnHoldReason(paymentId:String, holdReasonCd:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.removePaymentOnHoldReason(paymentId, holdReasonCd, companyId));
			remoteToken.addResponder(responder);
        }

		public function updatePaymentMethod(paymentId:String, pPaymentMethod:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updatePaymentMethod(paymentId, pPaymentMethod, companyId));
			remoteToken.addResponder(responder);
        }
    
        public function getCompanyAgencyTemplates(pSourceSystemCode:String, pSourceCompanyId:String, pShowForThisCompanyOnly:Boolean, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(taxRemoteService.getCompanyAgencyTemplates(pSourceSystemCode, pSourceCompanyId, pShowForThisCompanyOnly));
            remoteToken.addResponder(responder);
        }

		public function getPaymentMethodAuditHistory(paymentId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentMethodAuditHistory(paymentId, companyId));
			remoteToken.addResponder(responder);
        }

		public function rejectPayment(paymentId:String, reason:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.rejectPayment(paymentId, reason, companyId));
			remoteToken.addResponder(responder);
        }

		public function initiateRepayment(paymentId:String, options:TaxRepaymentOptions, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.initiateRepayment(paymentId, options, companyId));
			remoteToken.addResponder(responder);
        }

		public function getValidPaymentMethods(paymentId:String, companyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getValidPaymentMethods(paymentId, companyId));
			remoteToken.addResponder(responder);
        }

		public function getValidPaymentMethodsByTemplate(paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getValidPaymentMethodsByTemplate(paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getMoneyMovementTransactionsForVerification(pSourceSystemCode:String, pCompanyId:String, pInitiationStartDate:Date, pInitiationEndDate:Date, pTotalAmountFrom:String, pTotalAmountTo:String, pRelatedAmountFrom:String, pRelatedAmountTo:String, pStateTemplate:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getMoneyMovementTransactionsForVerification(pSourceSystemCode, pCompanyId, pInitiationStartDate, pInitiationEndDate, pTotalAmountFrom, pTotalAmountTo, pRelatedAmountFrom, pRelatedAmountTo, pStateTemplate));
			remoteToken.addResponder(responder);
        }

		public function getSupportedPaymentTemplates(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getSupportedPaymentTemplates());
			remoteToken.addResponder(responder);
        }

		public function getSupportedPaymentTemplatesForCompany(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getSupportedPaymentTemplatesForCompany(pSourceSystemCd, pSourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getDataSyncDetails(sourceSystemCd:String, sourceCompanyId:String, itemType:String, idSearchTypeString:String, fromId:int, toId:int, typeString:String, fromDate:Date, toDate:Date, checkNumber:String, amount:String, pItemName:String, pageSize:int, orderBy:String, descending:Boolean, firstResult:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getDataSyncDetails(sourceSystemCd, sourceCompanyId, itemType, idSearchTypeString, fromId, toId, typeString, fromDate, toDate, checkNumber, amount, pItemName, pageSize, orderBy, descending, firstResult));
			remoteToken.addResponder(responder);
        }

		public function getQBDTTokens(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getQBDTTokens(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function updatedDataSyncTokensOnSelectedItems(sourceSystemCd:String, sourceCompanyId:String, items:DataSyncItems, action:String, undelete:Boolean, comment:String, caseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updatedDataSyncTokensOnSelectedItems(sourceSystemCd, sourceCompanyId, items, action, undelete, comment, caseId));
			remoteToken.addResponder(responder);
        }

		public function updateDataSyncTokens(sourceSystemCd:String, sourceCompanyId:String, action:String, actions:ArrayCollection, comment:String, caseId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateDataSyncTokens(sourceSystemCd, sourceCompanyId, action, actions, comment, caseId));
			remoteToken.addResponder(responder);
        }

        public function findRefundTransactions(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(taxRemoteService.findRefundTransactions(sourceSystemCd, sourceCompanyId));
            remoteToken.addResponder(responder);
        }

		public function findCourtesyRefundTransactions(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findCourtesyRefundTransactions(pSourceSystemCd, pSourceSystemId));
			remoteToken.addResponder(responder);
        }

        public function createPenaltiesAndInterestRefunds(sourceSystemCd:String, sourceCompanyId:String, penaltiesRefundAmount:Number, interestRefundAmount:Number,
                                                          note:String, settlementTypeCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(taxRemoteService.createPenaltiesAndInterestRefunds(sourceSystemCd, sourceCompanyId, penaltiesRefundAmount, interestRefundAmount, note, settlementTypeCd));
            remoteToken.addResponder(responder);
        }

		public function createRefundDebit(pFinancialTransactionId:String, pNote:String, pSettlementTypeCd:String, responder:IResponder, companyId:String):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.createRefundDebit(pFinancialTransactionId, pNote, pSettlementTypeCd, companyId));
			remoteToken.addResponder(responder);
        }

		public function createCourtesyRefund(pSourceSystemCd:String, pSourceSystemId:String, pRefundAmount:Number, pNote:String, pSettlementTypeCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.createCourtesyRefund(pSourceSystemCd, pSourceSystemId, pRefundAmount, pNote, pSettlementTypeCd));
			remoteToken.addResponder(responder);
        }

		public function getAgencyTaxRefundBreakdown(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAgencyTaxRefundBreakdown(pSourceSystemCd, pSourceSystemId));
			remoteToken.addResponder(responder);
        }

		public function createTORTransactions(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, quarter:Quarter, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.createTORTransactions(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, quarter));
			remoteToken.addResponder(responder);
        }

		public function findEditableQuarterRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findEditableQuarterRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getFirstTaxYear(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getFirstTaxYear(pSourceSystemCd, pSourceSystemId));
			remoteToken.addResponder(responder);
        }

		public function findAllEditableRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findAllEditableRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function updateRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:QuarterLawRates, pushToQuickbooks:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks));
			remoteToken.addResponder(responder);
        }

		public function getPaymentTemplateQuarters(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getPaymentTemplateQuarters(sourceSystemCd, companyId, includePossibleBackdateYears));
			remoteToken.addResponder(responder);
        }

		public function updateAllRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:ArrayCollection, pushToQuickbooks:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateAllRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks));
			remoteToken.addResponder(responder);
        }

		public function findEditableAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findEditableAdditionalFilingAmounts(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function updateAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, amounts:QuarterCompanyFilingAmounts, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateAdditionalFilingAmounts(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, amounts));
			remoteToken.addResponder(responder);
        }

		public function getAllDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAllDepositFrequencies(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function updateDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, depositFrequencies:ArrayCollection, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateDepositFrequencies(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, depositFrequencies));
			remoteToken.addResponder(responder);
        }

		public function findAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.findAgencyIDs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function checkAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.checkAgencyIDs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agencyIds));
			remoteToken.addResponder(responder);
        }

		public function updateAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateAgencyIDs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agencyIds));
			remoteToken.addResponder(responder);
        }

		public function getAllLawFlags(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getAllLawFlags(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function updateLawFlags(pSourceSystemCd:String, pSourceSystemId:String, lawFlags:ArrayCollection, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.updateLawFlags(pSourceSystemCd, pSourceSystemId, lawFlags));
			remoteToken.addResponder(responder);
        }

		public function getLawFlagHistory(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getLawFlagHistory(pSourceSystemCd, pSourceSystemId, paymentTemplateCd));
			remoteToken.addResponder(responder);
        }

		public function getFilerType(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.getFilerType(pSourceSystemCd, pSourceSystemId));
			remoteToken.addResponder(responder);
        }

		public function hasNonNumericSourceIds(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.hasNonNumericSourceIds(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getManualLedgerLimit(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxRemoteService.hasNonNumericSourceIds(sourceSystemCd, sourceCompanyId));
			remoteToken.addResponder(responder);
        }

		public function createPendingTaxRefund(sourceSystemCd:String, sourceCompanyId:String, paymentId:String, reason:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(taxRemoteService.createPendingTaxRefund(sourceSystemCd, sourceCompanyId, paymentId, reason));
			remoteToken.addResponder(responder);
		}

    }
}
