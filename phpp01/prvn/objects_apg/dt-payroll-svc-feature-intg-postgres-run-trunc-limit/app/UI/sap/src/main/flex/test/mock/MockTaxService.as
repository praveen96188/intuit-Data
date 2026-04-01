package test.mock
{
    import flash.utils.ByteArray;

    import mx.collections.ArrayCollection;
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

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

    public class MockTaxService extends MockAsyncService implements ITaxService
	{
		public function MockTaxService()
		{
		}

		public function expectsGetAgencyList():Mock {
            return expects("getAgencyList").withArgs();
        }
		public function getAgencyList(responder:IResponder):void {
            record("getAgencyList");
			sendAsyncResult(responder,"getAgencyList");
        }

		public function expectsGetTemplateYearPayment(sourceSystemCd:String, companyId:String, taxYear:String, paymentTemplateCd:String):Mock {
            return expects("getTemplateYearPayment").withArgs(sourceSystemCd, companyId, taxYear, paymentTemplateCd);
        }
		public function expectsGetPaymentTemplateYears(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean):Mock {
            return expects("getPaymentTemplateYears").withArgs(sourceSystemCd, companyId, includePossibleBackdateYears);
        }
		public function getPaymentTemplateYears(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean, responder:IResponder):void {
            record("getPaymentTemplateYears", sourceSystemCd, companyId, includePossibleBackdateYears);
			sendAsyncResult(responder,"getPaymentTemplateYears");
        }

		public function getTemplateYearPayment(sourceSystemCd:String, companyId:String, taxYear:String, paymentTemplateCd:String, responder:IResponder):void {
            record("getTemplateYearPayment", sourceSystemCd, companyId, taxYear, paymentTemplateCd);
			sendAsyncResult(responder,"getTemplateYearPayment");
        }

		public function expectsGetPaymentTemplateQuarterPayment(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, year:String, quarter:String):Mock {
            return expects("getPaymentTemplateQuarterPayment").withArgs(sourceSystemCd, companyId, paymentTemplateCd, year, quarter);
        }
		public function getPaymentTemplateQuarterPayment(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, year:String, quarter:String, responder:IResponder):void {
            record("getPaymentTemplateQuarterPayment", sourceSystemCd, companyId, paymentTemplateCd, year, quarter);
			sendAsyncResult(responder,"getPaymentTemplateQuarterPayment");
        }

		public function expectsFindPaymentDetailTransactions(moneyMovementTransactionId:String, companyId:String):Mock {
            return expects("findPaymentDetailTransactions").withArgs(moneyMovementTransactionId, companyId);
        }
		public function findPaymentDetailTransactions(moneyMovementTransactionId:String, companyId:String, responder:IResponder):void {
            record("findPaymentDetailTransactions", moneyMovementTransactionId, companyId);
			sendAsyncResult(responder,"findPaymentDetailTransactions");
        }

		public function expectsFindCompanyTaxYears(sourceSystemCd:String, companyId:String):Mock {
            return expects("findCompanyTaxYears").withArgs(sourceSystemCd, companyId);
        }
		public function findCompanyTaxYears(sourceSystemCd:String, companyId:String, responder:IResponder):void {
            record("findCompanyTaxYears", sourceSystemCd, companyId);
			sendAsyncResult(responder,"findCompanyTaxYears");
        }

		public function expectsFindCompanyAgencies(sourceSystemCd:String, companyId:String):Mock {
            return expects("findCompanyAgencies").withArgs(sourceSystemCd, companyId);
        }
		public function findCompanyAgencies(sourceSystemCd:String, companyId:String, responder:IResponder):void {
            record("findCompanyAgencies", sourceSystemCd, companyId);
			sendAsyncResult(responder,"findCompanyAgencies");
        }

		public function expectsFindTaxTransactions(sourceSystemCd:String, companyId:String, transactionDescription:String, agencyCd:String, paymentTemplateCd:String, specifiedLawId:String, paymentMethod:String, yearQuarterStartDate:Date, yearQuarterEndDate:Date, includeNotPostedPayments:Boolean):Mock {
            return expects("findTaxTransactions").withArgs(sourceSystemCd, companyId, transactionDescription, agencyCd, paymentTemplateCd, specifiedLawId, paymentMethod, yearQuarterStartDate, yearQuarterEndDate, includeNotPostedPayments);
        }
		public function findTaxTransactions(sourceSystemCd:String, companyId:String, transactionDescription:String, agencyCd:String, paymentTemplateCd:String, specifiedLawId:String, paymentMethod:String, yearQuarterStartDate:Date, yearQuarterEndDate:Date, includeNotPostedPayments:Boolean, responder:IResponder):void {
            record("findTaxTransactions", sourceSystemCd, companyId, transactionDescription, agencyCd, paymentTemplateCd, specifiedLawId, paymentMethod, yearQuarterStartDate, yearQuarterEndDate, includeNotPostedPayments);
			sendAsyncResult(responder,"findTaxTransactions");
        }

		public function expectsFindEmployeeLedgerItems(pLedgerItemDetailsCriterion:LedgerItemDetailsCriterion):Mock {
            return expects("findEmployeeLedgerItems").withArgs(pLedgerItemDetailsCriterion);
        }
		public function findEmployeeLedgerItems(pLedgerItemDetailsCriterion:LedgerItemDetailsCriterion, responder:IResponder):void {
            record("findEmployeeLedgerItems", pLedgerItemDetailsCriterion);
			sendAsyncResult(responder,"findEmployeeLedgerItems");
        }


		public function expectsGetManualLedgerLines(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, specifiedLawId:String, checkDate:Date):Mock {
            return expects("getManualLedgerLines").withArgs(sourceSystemCd, companyId, paymentTemplateCd, specifiedLawId, checkDate);
        }
		public function getManualLedgerLines(sourceSystemCd:String, companyId:String, paymentTemplateCd:String, specifiedLawId:String, checkDate:Date, responder:IResponder):void {
            record("getManualLedgerLines", sourceSystemCd, companyId, paymentTemplateCd, specifiedLawId, checkDate);
			sendAsyncResult(responder,"getManualLedgerLines");
        }

		public function expectsCreateManualLedgerEntry(sourceSystemCd:String, companyId:String, entryType:String, lines:ArrayCollection, checkDate:Date, memo:String, recordingOption:int, datePaid:Date):Mock {
            return expects("createManualLedgerEntry").withArgs(sourceSystemCd, companyId, entryType, lines, checkDate, memo, recordingOption, datePaid);
        }
		public function createManualLedgerEntry(sourceSystemCd:String, companyId:String, entryType:String, lines:ArrayCollection, checkDate:Date, memo:String, recordingOption:int, datePaid:Date, allowLimitOutsideOfBoundaries:Boolean, responder:IResponder):void {
            record("createManualLedgerEntry", sourceSystemCd, companyId, entryType, lines, checkDate, memo, recordingOption, datePaid, allowLimitOutsideOfBoundaries);
			sendAsyncResult(responder,"createManualLedgerEntry");
        }

		public function expectsGetEFTPSEnrollmentRejections():Mock {
            return expects("getEFTPSEnrollmentRejections").withArgs();
        }
		public function getEFTPSEnrollmentRejections(responder:IResponder):void {
            record("getEFTPSEnrollmentRejections");
			sendAsyncResult(responder,"getEFTPSEnrollmentRejections");
        }

		public function expectsGetEftpsEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("getEftpsEnrollmentsHistory").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function getEftpsEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("getEftpsEnrollmentsHistory", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"getEftpsEnrollmentsHistory");
        }

		public function expectsInitiateReEnrollment(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("initiateReEnrollment").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function initiateReEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("initiateReEnrollment", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"initiateReEnrollment");
        }

		public function expectsCreateManualEFTPSEnrollment(pSourceSystemCode:String, pCompanyId:String, ein:String, legalName:String, zip:String):Mock {
            return expects("createManualEFTPSEnrollment").withArgs(pSourceSystemCode, pCompanyId, ein, legalName, zip);
        }
		public function createManualEFTPSEnrollment(pSourceSystemCode:String, pCompanyId:String, ein:String, legalName:String, zip:String, responder:IResponder):void {
            record("createManualEFTPSEnrollment", pSourceSystemCode, pCompanyId, ein, legalName, zip);
			sendAsyncResult(responder,"createManualEFTPSEnrollment");
        }

		public function expectsGetLawAmounts(mmtId:String, companyId:String):Mock {
            return expects("getLawAmounts").withArgs(mmtId, companyId);
        }
		public function getLawAmounts(mmtId:String, companyId:String, responder:IResponder):void {
            record("getLawAmounts", mmtId, companyId);
			sendAsyncResult(responder,"getLawAmounts");
        }

		public function expectsFinalizePayment(mmtId:String, companyId:String):Mock {
            return expects("finalizePayment").withArgs(mmtId, companyId);
        }
		public function finalizePayment(mmtId:String, companyId:String, responder:IResponder):void {
            record("finalizePayment", mmtId, companyId);
			sendAsyncResult(responder,"finalizePayment");
        }

		public function expectsUnFinalizePayment(mmtId:String, companyId:String):Mock {
            return expects("unFinalizePayment").withArgs(mmtId, companyId);
        }
		public function unFinalizePayment(mmtId:String, companyId:String, responder:IResponder):void {
            record("unFinalizePayment", mmtId, companyId);
			sendAsyncResult(responder,"unFinalizePayment");
        }

		public function expectsFinalizePayments(searchCriteria:PaymentSearch):Mock {
            return expects("finalizePayments").withArgs(searchCriteria);
        }
		public function finalizePayments(searchCriteria:PaymentSearch, responder:IResponder):void {
            record("finalizePayments", searchCriteria);
			sendAsyncResult(responder,"finalizePayments");
        }

		public function expectsUpdateInitiationDates(searchCriteria:PaymentSearch, newInitiationDate:Date):Mock {
            return expects("updateInitiationDates").withArgs(searchCriteria, newInitiationDate);
        }
		public function updateInitiationDates(searchCriteria:PaymentSearch, newInitiationDate:Date, responder:IResponder):void {
            record("updateInitiationDates", searchCriteria, newInitiationDate);
			sendAsyncResult(responder,"updateInitiationDates");
        }

		public function expectsUpdateGroupPaymentMethods(searchCriteria:PaymentSearch, paymentMethod:String):Mock {
            return expects("updateGroupPaymentMethods").withArgs(searchCriteria, paymentMethod);
        }
		public function updateGroupPaymentMethods(searchCriteria:PaymentSearch, paymentMethod:String, responder:IResponder):void {
            record("updateGroupPaymentMethods", searchCriteria, paymentMethod);
			sendAsyncResult(responder,"updateGroupPaymentMethods");
        }

		public function expectsEditPaymentAmount(mmtId:String, splitFTIDs:ArrayCollection, lawAmounts:ArrayCollection, memo:String, immediateDebitOrCredit:Boolean, companyId:String):Mock {
            return expects("editPaymentAmount").withArgs(mmtId, splitFTIDs, lawAmounts, memo, immediateDebitOrCredit, companyId);
        }
		public function editPaymentAmount(mmtId:String, splitFTIDs:ArrayCollection, lawAmounts:ArrayCollection, memo:String, immediateDebitOrCredit:Boolean, allowLimitOutsideOfBoundaries:Boolean, companyId:String, responder:IResponder):void {
            record("editPaymentAmount", mmtId, splitFTIDs, lawAmounts, memo, immediateDebitOrCredit, allowLimitOutsideOfBoundaries, companyId);
			sendAsyncResult(responder,"editPaymentAmount");
        }


		public function expectsFindTaxPayments(searchCriteria:PaymentSearch, firstResult:int, maxResults:int, sortColumn:String, sortDescending:Boolean):Mock {
            return expects("findTaxPayments").withArgs(searchCriteria, firstResult, maxResults, sortColumn, sortDescending);
        }
		public function findTaxPayments(searchCriteria:PaymentSearch, firstResult:int, maxResults:int, sortColumn:String, sortDescending:Boolean, responder:IResponder):void {
            record("findTaxPayments", searchCriteria, firstResult, maxResults, sortColumn, sortDescending);
			sendAsyncResult(responder,"findTaxPayments");
        }

		public function expectsGetRAFEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("getRAFEnrollmentsHistory").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function expectsGetRAFEnrollmentsByStatusAndCompany(search:RAFEnrollmentSearch, pIncludePayrollStatus:Boolean, pFirstResult:int, pMaxResults:int):Mock {
            return expects("getRAFEnrollmentsByStatusAndCompany").withArgs(search, pIncludePayrollStatus, pFirstResult, pMaxResults);
        }
		public function getRAFEnrollmentsByStatusAndCompany(search:RAFEnrollmentSearch, pIncludePayrollStatus:Boolean, pFirstResult:int, pMaxResults:int, responder:IResponder):void {
            record("getRAFEnrollmentsByStatusAndCompany", search, pIncludePayrollStatus, pFirstResult, pMaxResults);
			sendAsyncResult(responder,"getRAFEnrollmentsByStatusAndCompany");
        }

		public function getRAFEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("getRAFEnrollmentsHistory", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"getRAFEnrollmentsHistory");
        }

		public function expectsUpdateRAFEnrollmentStatus(pSourceSystemCode:String, pCompanyId:String, pNewStatus:String):Mock {
            return expects("updateRAFEnrollmentStatus").withArgs(pSourceSystemCode, pCompanyId, pNewStatus);
        }
		public function updateRAFEnrollmentStatus(pSourceSystemCode:String, pCompanyId:String, pNewStatus:String, responder:IResponder):void {
            record("updateRAFEnrollmentStatus", pSourceSystemCode, pCompanyId, pNewStatus);
			sendAsyncResult(responder,"updateRAFEnrollmentStatus");
        }

		public function expectsRejectRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pReason:String):Mock {
            return expects("rejectRAFEnrollment").withArgs(pSourceSystemCode, pCompanyId, pReason);
        }
		public function rejectRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pReason:String, responder:IResponder):void {
            record("rejectRAFEnrollment", pSourceSystemCode, pCompanyId, pReason);
			sendAsyncResult(responder,"rejectRAFEnrollment");
        }

		public function expectsInitiateRAFTapeCreation(pActionCode:String):Mock {
            return expects("initiateRAFTapeCreation").withArgs(pActionCode);
        }
		public function initiateRAFTapeCreation(pActionCode:String, responder:IResponder):void {
            record("initiateRAFTapeCreation", pActionCode);
			sendAsyncResult(responder,"initiateRAFTapeCreation");
        }

		public function expectsInitiateACHFileCreation(pACHEnrollmentFileType:String, pSAPQuarter:Quarter):Mock {
            return expects("initiateACHFileCreation").withArgs(pACHEnrollmentFileType, pSAPQuarter);
        }
		public function initiateACHFileCreation(pACHEnrollmentFileType:String, pSAPQuarter:Quarter, responder:IResponder):void {
            record("initiateACHFileCreation", pACHEnrollmentFileType, pSAPQuarter);
			sendAsyncResult(responder,"initiateACHFileCreation");
        }

		public function expectsFindEnrollmentFiles(actionCode:String):Mock {
            return expects("findEnrollmentFiles").withArgs(actionCode);
        }
		public function findEnrollmentFiles(actionCode:String, responder:IResponder):void {
            record("findEnrollmentFiles", actionCode);
			sendAsyncResult(responder,"findEnrollmentFiles");
        }

		public function expectsGetACHEnrollmentQuarters(actionCode:String):Mock {
            return expects("getACHEnrollmentQuarters").withArgs(actionCode);
        }
		public function getACHEnrollmentQuarters(actionCode:String, responder:IResponder):void {
            record("getACHEnrollmentQuarters", actionCode);
			sendAsyncResult(responder,"getACHEnrollmentQuarters");
        }

		public function expectsReInitiateRAFEnrollment(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("reInitiateRAFEnrollment").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function reInitiateRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("reInitiateRAFEnrollment", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"reInitiateRAFEnrollment");
        }

		public function expectsDeleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String):Mock {
            return expects("deleteRAFEnrollment").withArgs(pSourceSystemCode, pCompanyId, pEnrollmentID);
        }
		public function deleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String, responder:IResponder):void {
            record("deleteRAFEnrollment", pSourceSystemCode, pCompanyId, pEnrollmentID);
			sendAsyncResult(responder,"deleteRAFEnrollment");
        }

		public function expectsReInitiateRAFTapeCreation(pFileId:String):Mock {
            return expects("reInitiateRAFTapeCreation").withArgs(pFileId);
        }
		public function reInitiateRAFTapeCreation(pFileId:String, responder:IResponder):void {
            record("reInitiateRAFTapeCreation", pFileId);
			sendAsyncResult(responder,"reInitiateRAFTapeCreation");
        }

		public function expectsCancelDeleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String):Mock {
            return expects("cancelDeleteRAFEnrollment").withArgs(pSourceSystemCode, pCompanyId, pEnrollmentId);
        }
		public function cancelDeleteRAFEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String, responder:IResponder):void {
            record("cancelDeleteRAFEnrollment", pSourceSystemCode, pCompanyId, pEnrollmentId);
			sendAsyncResult(responder,"cancelDeleteRAFEnrollment");
        }

		public function expectsEnrollAllRAFEnrollments(enrollmentCriteria:RAFEnrollmentSearch):Mock {
            return expects("enrollAllRAFEnrollments").withArgs(enrollmentCriteria);
        }
		public function enrollAllRAFEnrollments(enrollmentCriteria:RAFEnrollmentSearch, responder:IResponder):void {
            record("enrollAllRAFEnrollments", enrollmentCriteria);
			sendAsyncResult(responder,"enrollAllRAFEnrollments");
        }

		public function expectsFindACHEnrollments(status:String, pFirstResult:int, pMaxResults:int, sortColumn:String, sortDescending:Boolean):Mock {
            return expects("findACHEnrollments").withArgs(status, pFirstResult, pMaxResults, sortColumn, sortDescending);
        }
		public function findACHEnrollments(status:String, pFirstResult:int, pMaxResults:int, sortColumn:String, sortDescending:Boolean, responder:IResponder):void {
            record("findACHEnrollments", status, pFirstResult, pMaxResults, sortColumn, sortDescending);
			sendAsyncResult(responder,"findACHEnrollments");
        }

		public function expectsUploadACHResponseFile(fileName:String, file:ByteArray):Mock {
            return expects("uploadACHResponseFile").withArgs(fileName, file);
        }
		public function uploadACHResponseFile(fileName:String, file:ByteArray, responder:IResponder):void {
            record("uploadACHResponseFile", fileName, file);
			sendAsyncResult(responder,"uploadACHResponseFile");
        }

		public function expectsUpdateACHEnrollmentAsEnrolled(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("updateACHEnrollmentAsEnrolled").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function updateACHEnrollmentAsEnrolled(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("updateACHEnrollmentAsEnrolled", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"updateACHEnrollmentAsEnrolled");
        }

		public function expectsReInitiateACHEnrollment(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("reInitiateACHEnrollment").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function reInitiateACHEnrollment(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("reInitiateACHEnrollment", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"reInitiateACHEnrollment");
        }

		public function expectsDeleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String):Mock {
            return expects("deleteACHEnrollment").withArgs(pSourceSystemCode, pCompanyId, pEnrollmentID);
        }
		public function deleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentID:String, responder:IResponder):void {
            record("deleteACHEnrollment", pSourceSystemCode, pCompanyId, pEnrollmentID);
			sendAsyncResult(responder,"deleteACHEnrollment");
        }

		public function expectsCancelDeleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String):Mock {
            return expects("cancelDeleteACHEnrollment").withArgs(pSourceSystemCode, pCompanyId, pEnrollmentId);
        }
		public function cancelDeleteACHEnrollment(pSourceSystemCode:String, pCompanyId:String, pEnrollmentId:String, responder:IResponder):void {
            record("cancelDeleteACHEnrollment", pSourceSystemCode, pCompanyId, pEnrollmentId);
			sendAsyncResult(responder,"cancelDeleteACHEnrollment");
        }

		public function expectsGetACHEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("getACHEnrollmentsHistory").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function getACHEnrollmentsHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("getACHEnrollmentsHistory", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"getACHEnrollmentsHistory");
        }

		public function expectsGetAgencyInfoArray(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("getAgencyInfoArray").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function getAgencyInfoArray(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("getAgencyInfoArray", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"getAgencyInfoArray");
        }

		public function expectsGetCompanyFilingAmountHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String):Mock {
            return expects("getCompanyFilingAmountHistory").withArgs(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd);
        }
		public function getCompanyFilingAmountHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
            record("getCompanyFilingAmountHistory", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd);
			sendAsyncResult(responder,"getCompanyFilingAmountHistory");
        }

		public function expectsGetAdditionalAgencyIdsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String):Mock {
            return expects("getAdditionalAgencyIdsHistory").withArgs(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd);
        }
		public function getAdditionalAgencyIdsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
            record("getAdditionalAgencyIdsHistory", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd);
			sendAsyncResult(responder,"getAdditionalAgencyIdsHistory");
        }

		public function expectsGetPaymentMethodsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, pFieldName:String):Mock {
            return expects("getPaymentMethodsHistory").withArgs(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, pFieldName);
        }
		public function getPaymentMethodsHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, pFieldName:String, responder:IResponder):void {
            record("getPaymentMethodsHistory", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, pFieldName);
			sendAsyncResult(responder,"getPaymentMethodsHistory");
        }

		public function expectsGetAgencyIdHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String):Mock {
            return expects("getAgencyIdHistory").withArgs(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd);
        }
		public function getAgencyIdHistory(pSourceSystemCode:String, pSourceCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
            record("getAgencyIdHistory", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd);
			sendAsyncResult(responder,"getAgencyIdHistory");
        }

		public function expectsGetCompanyLawRatesHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String):Mock {
            return expects("getCompanyLawRatesHistory").withArgs(pSourceSystemCode, pCompanyId, pPaymentTemplateCd);
        }
		public function getCompanyLawRatesHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
            record("getCompanyLawRatesHistory", pSourceSystemCode, pCompanyId, pPaymentTemplateCd);
			sendAsyncResult(responder,"getCompanyLawRatesHistory");
        }

		public function expectsGetDepositFrequencyHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String):Mock {
            return expects("getDepositFrequencyHistory").withArgs(pSourceSystemCode, pCompanyId, pPaymentTemplateCd);
        }
		public function getDepositFrequencyHistory(pSourceSystemCode:String, pCompanyId:String, pPaymentTemplateCd:String, responder:IResponder):void {
            record("getDepositFrequencyHistory", pSourceSystemCode, pCompanyId, pPaymentTemplateCd);
			sendAsyncResult(responder,"getDepositFrequencyHistory");
        }

		public function expectsGetDefaultDepositFrequency(pPaymentTemplateCd:String):Mock {
            return expects("getDefaultDepositFrequency").withArgs(pPaymentTemplateCd);
        }
		public function getDefaultDepositFrequency(pPaymentTemplateCd:String, responder:IResponder):void {
            record("getDefaultDepositFrequency", pPaymentTemplateCd);
			sendAsyncResult(responder,"getDefaultDepositFrequency");
        }

		public function expectsGetFilerTypeHistory(pSourceSystemCode:String, pCompanyId:String):Mock {
            return expects("getFilerTypeHistory").withArgs(pSourceSystemCode, pCompanyId);
        }
		public function getFilerTypeHistory(pSourceSystemCode:String, pCompanyId:String, responder:IResponder):void {
            record("getFilerTypeHistory", pSourceSystemCode, pCompanyId);
			sendAsyncResult(responder,"getFilerTypeHistory");
        }

		public function expectsUpdateFilerType(pSourceSystemCd:String, pSourceSystemId:String, filerType:FilerType):Mock {
            return expects("updateFilerType").withArgs(pSourceSystemCd, pSourceSystemId, filerType);
        }
		public function updateFilerType(pSourceSystemCd:String, pSourceSystemId:String, filerType:FilerType, responder:IResponder):void {
            record("updateFilerType", pSourceSystemCd, pSourceSystemId, filerType);
			sendAsyncResult(responder,"updateFilerType");
        }

		public function expectsUpdateAgentEnabled(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agentEnabled:Boolean):Mock {
            return expects("updateAgentEnabled").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agentEnabled);
        }
		public function updateAgentEnabled(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agentEnabled:Boolean, responder:IResponder):void {
            record("updateAgentEnabled", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agentEnabled);
			sendAsyncResult(responder,"updateAgentEnabled");
        }

		public function expectsFindCompanyLaws(pSourceSystemCd:String, pSourceSystemId:String):Mock {
            return expects("findCompanyLaws").withArgs(pSourceSystemCd, pSourceSystemId);
        }
		public function findCompanyLaws(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
            record("findCompanyLaws", pSourceSystemCd, pSourceSystemId);
			sendAsyncResult(responder,"findCompanyLaws");
        }

		public function expectsEditCompanyLawAgencyId(pSourceSystemCd:String, pSourceSystemId:String, sourceId:String, agencyId:String):Mock {
            return expects("editCompanyLawAgencyId").withArgs(pSourceSystemCd, pSourceSystemId, sourceId, agencyId);
        }
		public function editCompanyLawAgencyId(pSourceSystemCd:String, pSourceSystemId:String, sourceId:String, agencyId:String, responder:IResponder):void {
            record("editCompanyLawAgencyId", pSourceSystemCd, pSourceSystemId, sourceId, agencyId);
			sendAsyncResult(responder,"editCompanyLawAgencyId");
        }

		public function expectsUpdatePayDate(paymentId:String, pNewPayDate:Date, companyId:String):Mock {
            return expects("updatePayDate").withArgs(paymentId, pNewPayDate, companyId);
        }
		public function updatePayDate(paymentId:String, pNewPayDate:Date, companyId:String, responder:IResponder):void {
            record("updatePayDate", paymentId, pNewPayDate, companyId);
			sendAsyncResult(responder,"updatePayDate");
        }

		public function expectsGetStatusHistoryData(paymentId:String, companyId:String):Mock {
            return expects("getStatusHistoryData").withArgs(paymentId, companyId);
        }
		public function getStatusHistoryData(paymentId:String, companyId:String, responder:IResponder):void {
            record("getStatusHistoryData", paymentId, companyId);
			sendAsyncResult(responder,"getStatusHistoryData");
        }

		public function expectsGetHoldsHistoryData(paymentId:String, companyId:String):Mock {
            return expects("getHoldsHistoryData").withArgs(paymentId, companyId);
        }
		public function getHoldsHistoryData(paymentId:String, companyId:String, responder:IResponder):void {
            record("getHoldsHistoryData", paymentId, companyId);
			sendAsyncResult(responder,"getHoldsHistoryData");
        }

		public function expectsGetPaymentsPayDateAuditHistory(paymentId:String, companyId:String ):Mock {
            return expects("getPaymentsPayDateAuditHistory").withArgs(paymentId, companyId);
        }
		public function getPaymentsPayDateAuditHistory(paymentId:String, companyId:String, responder:IResponder):void {
            record("getPaymentsPayDateAuditHistory", paymentId, companyId);
			sendAsyncResult(responder,"getPaymentsPayDateAuditHistory");
        }

		public function expectsGetPaymentAmountDetails(sourceSystemCd:String, companyId:String, paymentId:String):Mock {
            return expects("getPaymentAmountDetails").withArgs(sourceSystemCd, companyId, paymentId);
        }
    
		public function getPaymentAmountDetails(sourceSystemCd:String, companyId:String, paymentId:String, responder:IResponder):void {
            record("getPaymentAmountDetails", sourceSystemCd, companyId, paymentId);
			sendAsyncResult(responder,"getPaymentAmountDetails");
        }

		public function expectsGetNextInitiationDate(pPaymentMethod:String):Mock {
            return expects("getNextInitiationDate").withArgs(pPaymentMethod);
        }
		public function getNextInitiationDate(pPaymentMethod:String, responder:IResponder):void {
            record("getNextInitiationDate", pPaymentMethod);
			sendAsyncResult(responder,"getNextInitiationDate");
        }

		public function expectsGetOffloadDate(pPaymentMethod:String):Mock {
            return expects("getOffloadDate").withArgs(pPaymentMethod,null);
        }
		public function getOffloadDate(pPaymentMethod:String,pPaymentTemplate:String, responder:IResponder):void {
            record("getOffloadDate", pPaymentMethod);
			sendAsyncResult(responder,"getOffloadDate");
        }

		public function expectsAddTaxPaymentAgentOnHoldReason(paymentId:String, companyId:String):Mock {
            return expects("addTaxPaymentAgentOnHoldReason").withArgs(paymentId, companyId);
        }
    
		public function addTaxPaymentAgentOnHoldReason(paymentId:String, companyId:String, responder:IResponder):void {
            record("addTaxPaymentAgentOnHoldReason", paymentId, companyId);
			sendAsyncResult(responder,"addTaxPaymentAgentOnHoldReason");
        }

		public function expectsRemovePaymentOnHoldReason(paymentId:String, holdReasonCd:String, companyId:String):Mock {
            return expects("removePaymentOnHoldReason").withArgs(paymentId, holdReasonCd, companyId);
        }
		public function removePaymentOnHoldReason(paymentId:String, holdReasonCd:String, companyId:String, responder:IResponder):void {
            record("removePaymentOnHoldReason", paymentId, holdReasonCd, companyId);
			sendAsyncResult(responder,"removePaymentOnHoldReason");
        }

		public function expectsUpdatePaymentMethod(paymentId:String, pPaymentMethod:String, companyId:String):Mock {
            return expects("updatePaymentMethod").withArgs(paymentId, pPaymentMethod, companyId);
        }

        public function expectsGetCompanyAgencyTemplates(pSourceSystemCode:String, pSourceCompanyId:String, pShowForThisCompanyOnly:Boolean):Mock {
            return expects("getCompanyAgencyTemplates").withArgs(pSourceSystemCode, pSourceCompanyId, pShowForThisCompanyOnly);
        }

        public function getCompanyAgencyTemplates(pSourceSystemCode:String, pSourceCompanyId:String, pShowForThisCompanyOnly:Boolean, responder:IResponder):void {
            record("getCompanyAgencyTemplates", pSourceSystemCode, pSourceCompanyId, pShowForThisCompanyOnly);
			sendAsyncResult(responder,"getCompanyAgencyTemplates");
        }
    
		public function updatePaymentMethod(paymentId:String, pPaymentMethod:String, companyId:String, responder:IResponder):void {
            record("updatePaymentMethod", paymentId, pPaymentMethod, companyId);
			sendAsyncResult(responder,"updatePaymentMethod");
        }

		public function expectsGetPaymentMethodAuditHistory(paymentId:String, companyId:String):Mock {
            return expects("getPaymentMethodAuditHistory").withArgs(paymentId, companyId);
        }
    
		public function getPaymentMethodAuditHistory(paymentId:String, companyId:String, responder:IResponder):void {
            record("getPaymentMethodAuditHistory", paymentId, companyId);
			sendAsyncResult(responder,"getPaymentMethodAuditHistory");
        }

		public function expectsRejectPayment(paymentId:String, reason:String, companyId:String):Mock {
            return expects("rejectPayment").withArgs(paymentId, reason, companyId);
        }

		public function rejectPayment(paymentId:String, reason:String, companyId:String, responder:IResponder):void {
            record("rejectPayment", paymentId, reason, companyId);
			sendAsyncResult(responder,"rejectPayment");
        }

		public function expectsInitiateRepayment(paymentId:String, options:TaxRepaymentOptions, companyId:String):Mock {
            return expects("initiateRepayment").withArgs(paymentId, options, companyId);
        }
		public function initiateRepayment(paymentId:String, options:TaxRepaymentOptions, companyId:String, responder:IResponder):void {
            record("initiateRepayment", paymentId, options, companyId);
			sendAsyncResult(responder,"initiateRepayment");
        }

		public function expectsGetValidPaymentMethods(paymentId:String, companyId:String):Mock {
            return expects("getValidPaymentMethods").withArgs(paymentId, companyId);
        }

		public function getValidPaymentMethods(paymentId:String, companyId:String, responder:IResponder):void {
            record("getValidPaymentMethods", paymentId, companyId);
			sendAsyncResult(responder,"getValidPaymentMethods");
        }

		public function expectsGetValidPaymentMethodsByTemplate(paymentTemplateCd:String):Mock {
            return expects("getValidPaymentMethodsByTemplate").withArgs(paymentTemplateCd);
        }
		public function getValidPaymentMethodsByTemplate(paymentTemplateCd:String, responder:IResponder):void {
            record("getValidPaymentMethodsByTemplate", paymentTemplateCd);
			sendAsyncResult(responder,"getValidPaymentMethodsByTemplate");
        }

		public function expectsGetMoneyMovementTransactionsForVerification(pSourceSystemCode:String, pCompanyId:String, pInitiationStartDate:Date, pInitiationEndDate:Date, pTotalAmountFrom:String, pTotalAmountTo:String, pRelatedAmountFrom:String, pRelatedAmountTo:String, pStateTemplate:String):Mock {
            return expects("getMoneyMovementTransactionsForVerification").withArgs(pSourceSystemCode, pCompanyId, pInitiationStartDate, pInitiationEndDate, pTotalAmountFrom, pTotalAmountTo, pRelatedAmountFrom, pRelatedAmountTo, pStateTemplate);
        }

		public function getMoneyMovementTransactionsForVerification(pSourceSystemCode:String, pCompanyId:String, pInitiationStartDate:Date, pInitiationEndDate:Date, pTotalAmountFrom:String, pTotalAmountTo:String, pRelatedAmountFrom:String, pRelatedAmountTo:String, pStateTemplate:String, responder:IResponder):void {
            record("getMoneyMovementTransactionsForVerification", pSourceSystemCode, pCompanyId, pInitiationStartDate, pInitiationEndDate, pTotalAmountFrom, pTotalAmountTo, pRelatedAmountFrom, pRelatedAmountTo, pStateTemplate);
			sendAsyncResult(responder,"getMoneyMovementTransactionsForVerification");
        }

		public function expectsGetSupportedPaymentTemplates():Mock {
            return expects("getSupportedPaymentTemplates").withArgs();
        }

		public function getSupportedPaymentTemplates(responder:IResponder):void {
            record("getSupportedPaymentTemplates");
			sendAsyncResult(responder,"getSupportedPaymentTemplates");
        }

		public function expectsGetSupportedPaymentTemplatesForCompany(pSourceSystemCd:String, pSourceCompanyId:String):Mock {
            return expects("getSupportedPaymentTemplatesForCompany").withArgs(pSourceSystemCd, pSourceCompanyId);
        }
		public function getSupportedPaymentTemplatesForCompany(pSourceSystemCd:String, pSourceCompanyId:String, responder:IResponder):void {
            record("getSupportedPaymentTemplatesForCompany", pSourceSystemCd, pSourceCompanyId);
			sendAsyncResult(responder,"getSupportedPaymentTemplatesForCompany");
        }

		public function expectsGetDataSyncDetails(sourceSystemCd:String, sourceCompanyId:String, itemType:String, idSearchTypeString:String, fromId:int, toId:int, typeString:String, fromDate:Date, toDate:Date, checkNumber:String, amount:String, pItemName:String, pageSize:int, orderBy:String, descending:Boolean, firstResult:int):Mock {
            return expects("getDataSyncDetails").withArgs(sourceSystemCd, sourceCompanyId, itemType, idSearchTypeString, fromId, toId, typeString, fromDate, toDate, checkNumber, amount, pItemName, pageSize, orderBy, descending, firstResult);
        }
		public function getDataSyncDetails(sourceSystemCd:String, sourceCompanyId:String, itemType:String, idSearchTypeString:String, fromId:int, toId:int, typeString:String, fromDate:Date, toDate:Date, checkNumber:String, amount:String, pItemName:String, pageSize:int, orderBy:String, descending:Boolean, firstResult:int, responder:IResponder):void {
            record("getDataSyncDetails", sourceSystemCd, sourceCompanyId, itemType, idSearchTypeString, fromId, toId, typeString, fromDate, toDate, checkNumber, amount, pItemName, pageSize, orderBy, descending, firstResult);
			sendAsyncResult(responder,"getDataSyncDetails");
        }

		public function expectsGetQBDTTokens(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("getQBDTTokens").withArgs(sourceSystemCd, sourceCompanyId);
        }
		public function getQBDTTokens(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("getQBDTTokens", sourceSystemCd, sourceCompanyId);
			sendAsyncResult(responder,"getQBDTTokens");
        }

		public function expectsUpdatedDataSyncTokensOnSelectedItems(sourceSystemCd:String, sourceCompanyId:String, items:DataSyncItems, action:String, undelete:Boolean, comment:String):Mock {
            return expects("updatedDataSyncTokensOnSelectedItems").withArgs(sourceSystemCd, sourceCompanyId, items, action, undelete, comment);
        }
		public function updatedDataSyncTokensOnSelectedItems(sourceSystemCd:String, sourceCompanyId:String, items:DataSyncItems, action:String, undelete:Boolean, comment:String, caseId:String, responder:IResponder):void {
            record("updatedDataSyncTokensOnSelectedItems", sourceSystemCd, sourceCompanyId, items, action, undelete, comment);
			sendAsyncResult(responder,"updatedDataSyncTokensOnSelectedItems");
        }

		public function expectsUpdateDataSyncTokens(sourceSystemCd:String, sourceCompanyId:String, action:String, actions:ArrayCollection, comment:String):Mock {
            return expects("updateDataSyncTokens").withArgs(sourceSystemCd, sourceCompanyId, action, actions, comment);
        }
		public function updateDataSyncTokens(sourceSystemCd:String, sourceCompanyId:String, action:String, actions:ArrayCollection, comment:String, pCaseId:String, responder:IResponder):void {
            record("updateDataSyncTokens", sourceSystemCd, sourceCompanyId, action, actions, comment);
			sendAsyncResult(responder,"updateDataSyncTokens");
        }

        public function expectsFindRefundTransactions(sourceSystemCode:String, sourceCompanyId:String):Mock {
            return expects("findRefundTransactions").withArgs(sourceSystemCode, sourceCompanyId);
        }
        public function findRefundTransactions(sourceSystemCode:String, sourceCompanyId:String, responder:IResponder):void {
            record("findRefundTransactions", sourceSystemCode, sourceCompanyId);
            sendAsyncResult(responder,"findRefundTransactions");
        }

		public function expectsFindCourtesyRefundTransactions(pSourceSystemCd:String, pSourceSystemId:String):Mock {
            return expects("findCourtesyRefundTransactions").withArgs(pSourceSystemCd, pSourceSystemId);
        }
		public function findCourtesyRefundTransactions(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
            record("findCourtesyRefundTransactions", pSourceSystemCd, pSourceSystemId);
			sendAsyncResult(responder,"findCourtesyRefundTransactions");
        }

        public function expectsCreatePenaltiesAndInterestRefunds(sourceSystemCd:String, sourceCompanyId:String, penaltiesRefundAmount:Number, interestRefundAmount:Number,
                                                                 note:String, settlementTypeCd:String):Mock {
            return expects("createPenaltiesAndInterestRefunds").withArgs(sourceSystemCd, sourceCompanyId, penaltiesRefundAmount, interestRefundAmount, note, settlementTypeCd);
        }

        public function createPenaltiesAndInterestRefunds(sourceSystemCd:String, sourceCompanyId:String, penaltiesRefundAmount:Number, interestRefundAmount:Number,
                                                                   note:String, settlementTypeCd:String, responder:IResponder):void {
            record("createPenaltiesAndInterestRefunds", sourceSystemCd, sourceCompanyId, penaltiesRefundAmount, interestRefundAmount, note, settlementTypeCd);
            sendAsyncResult(responder,"createPenaltiesAndInterestRefunds");
        }

		public function expectsCreateRefundDebit(pFinancialTransactionId:String, pNote:String, pSettlementTypeCd:String, companyId:String):Mock {
            return expects("createRefundDebit").withArgs(pFinancialTransactionId, pNote, pSettlementTypeCd, companyId);
        }
		public function createRefundDebit(pFinancialTransactionId:String, pNote:String, pSettlementTypeCd:String, responder:IResponder, companyId:companyId):void {
            record("createRefundDebit", pFinancialTransactionId, pNote, pSettlementTypeCd,companyId);
			sendAsyncResult(responder,"createRefundDebit");
        }

		public function expectsCreateCourtesyRefund(pSourceSystemCd:String, pSourceSystemId:String, pRefundAmount:Number, pNote:String, pSettlementTypeCd:String):Mock {
            return expects("createCourtesyRefund").withArgs(pSourceSystemCd, pSourceSystemId, pRefundAmount, pNote, pSettlementTypeCd);
        }
		public function createCourtesyRefund(pSourceSystemCd:String, pSourceSystemId:String, pRefundAmount:Number, pNote:String, pSettlementTypeCd:String, responder:IResponder):void {
            record("createCourtesyRefund", pSourceSystemCd, pSourceSystemId, pRefundAmount, pNote, pSettlementTypeCd);
			sendAsyncResult(responder,"createCourtesyRefund");
        }

		public function expectsGetAgencyTaxRefundBreakdown(pSourceSystemCd:String, pSourceSystemId:String):Mock {
            return expects("getAgencyTaxRefundBreakdown").withArgs(pSourceSystemCd, pSourceSystemId);
        }
		public function getAgencyTaxRefundBreakdown(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
            record("getAgencyTaxRefundBreakdown", pSourceSystemCd, pSourceSystemId);
			sendAsyncResult(responder,"getAgencyTaxRefundBreakdown");
        }

		public function expectsCreateTORTransactions(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, quarter:Quarter):Mock {
            return expects("createTORTransactions").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, quarter);
        }
		public function createTORTransactions(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, quarter:Quarter, responder:IResponder):void {
            record("createTORTransactions", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, quarter);
			sendAsyncResult(responder,"createTORTransactions");
        }

		public function expectsFindEditableQuarterRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("findEditableQuarterRates").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function findEditableQuarterRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("findEditableQuarterRates", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"findEditableQuarterRates");
        }

		public function expectsGetFirstTaxYear(pSourceSystemCd:String, pSourceSystemId:String):Mock {
            return expects("getFirstTaxYear").withArgs(pSourceSystemCd, pSourceSystemId);
        }
		public function getFirstTaxYear(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
            record("getFirstTaxYear", pSourceSystemCd, pSourceSystemId);
			sendAsyncResult(responder,"getFirstTaxYear");
        }

		public function expectsFindAllEditableRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("findAllEditableRates").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function findAllEditableRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("findAllEditableRates", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"findAllEditableRates");
        }

		public function expectsUpdateRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:QuarterLawRates, pushToQuickbooks:Boolean):Mock {
            return expects("updateRates").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks);
        }
		public function updateRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:QuarterLawRates, pushToQuickbooks:Boolean, responder:IResponder):void {
            record("updateRates", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks);
			sendAsyncResult(responder,"updateRates");
        }

		public function expectsGetPaymentTemplateQuarters(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean):Mock {
            return expects("getPaymentTemplateQuarters").withArgs(sourceSystemCd, companyId, includePossibleBackdateYears);
        }
		public function getPaymentTemplateQuarters(sourceSystemCd:String, companyId:String, includePossibleBackdateYears:Boolean, responder:IResponder):void {
            record("getPaymentTemplateQuarters", sourceSystemCd, companyId, includePossibleBackdateYears);
			sendAsyncResult(responder,"getPaymentTemplateQuarters");
        }

		public function expectsUpdateAllRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:ArrayCollection, pushToQuickbooks:Boolean):Mock {
            return expects("updateAllRates").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks);
        }
		public function updateAllRates(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, rates:ArrayCollection, pushToQuickbooks:Boolean, responder:IResponder):void {
            record("updateAllRates", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks);
			sendAsyncResult(responder,"updateAllRates");
        }

		public function expectsFindEditableAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("findEditableAdditionalFilingAmounts").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function findEditableAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("findEditableAdditionalFilingAmounts", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"findEditableAdditionalFilingAmounts");
        }

		public function expectsUpdateAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, amounts:QuarterCompanyFilingAmounts):Mock {
            return expects("updateAdditionalFilingAmounts").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, amounts);
        }
		public function updateAdditionalFilingAmounts(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, amounts:QuarterCompanyFilingAmounts, responder:IResponder):void {
            record("updateAdditionalFilingAmounts", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, amounts);
			sendAsyncResult(responder,"updateAdditionalFilingAmounts");
        }

		public function expectsGetAllDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("getAllDepositFrequencies").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function getAllDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("getAllDepositFrequencies", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"getAllDepositFrequencies");
        }

		public function expectsUpdateDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, depositFrequencies:ArrayCollection):Mock {
            return expects("updateDepositFrequencies").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, depositFrequencies);
        }
		public function updateDepositFrequencies(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, depositFrequencies:ArrayCollection, responder:IResponder):void {
            record("updateDepositFrequencies", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, depositFrequencies);
			sendAsyncResult(responder,"updateDepositFrequencies");
        }

		public function expectsFindAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("findAgencyIDs").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function findAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("findAgencyIDs", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"findAgencyIDs");
        }

		public function expectsCheckAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection):Mock {
            return expects("checkAgencyIDs").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agencyIds);
        }
		public function checkAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection, responder:IResponder):void {
            record("checkAgencyIDs", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agencyIds);
			sendAsyncResult(responder,"checkAgencyIDs");
        }

		public function expectsUpdateAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection):Mock {
            return expects("updateAgencyIDs").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agencyIds);
        }
		public function updateAgencyIDs(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, agencyIds:ArrayCollection, responder:IResponder):void {
            record("updateAgencyIDs", pSourceSystemCd, pSourceSystemId, paymentTemplateCd, agencyIds);
			sendAsyncResult(responder,"updateAgencyIDs");
        }

		public function expectsGetAllLawFlags(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("getAllLawFlags").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function getAllLawFlags(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("getAllLawFlags", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"getAllLawFlags");
        }

		public function expectsUpdateLawFlags(pSourceSystemCd:String, pSourceSystemId:String, lawFlags:ArrayCollection):Mock {
            return expects("updateLawFlags").withArgs(pSourceSystemCd, pSourceSystemId, lawFlags);
        }
		public function updateLawFlags(pSourceSystemCd:String, pSourceSystemId:String, lawFlags:ArrayCollection, responder:IResponder):void {
            record("updateLawFlags", pSourceSystemCd, pSourceSystemId, lawFlags);
			sendAsyncResult(responder,"updateLawFlags");
        }

		public function expectsGetLawFlagHistory(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String):Mock {
            return expects("getLawFlagHistory").withArgs(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        }
		public function getLawFlagHistory(pSourceSystemCd:String, pSourceSystemId:String, paymentTemplateCd:String, responder:IResponder):void {
            record("getLawFlagHistory", pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
			sendAsyncResult(responder,"getLawFlagHistory");
        }

		public function expectsGetFilerType(pSourceSystemCd:String, pSourceSystemId:String):Mock {
            return expects("getFilerType").withArgs(pSourceSystemCd, pSourceSystemId);
        }
		public function getFilerType(pSourceSystemCd:String, pSourceSystemId:String, responder:IResponder):void {
            record("getFilerType", pSourceSystemCd, pSourceSystemId);
			sendAsyncResult(responder,"getFilerType");
        }

		public function expectsHasNonNumericSourceIds(sourceSystemCd:String, sourceCompanyId:String):Mock {
            return expects("hasNonNumericSourceIds").withArgs(sourceSystemCd, sourceCompanyId);
        }
		public function hasNonNumericSourceIds(sourceSystemCd:String, sourceCompanyId:String, responder:IResponder):void {
            record("hasNonNumericSourceIds", sourceSystemCd, sourceCompanyId);
			sendAsyncResult(responder,"hasNonNumericSourceIds");
        }

        public function getCompanyAgencyHistory(pSourceSystemCode:String, pSourceCompanyId:String, pAgencyId:String, responder:IResponder):void {
            record("getCompanyAgencyHistory", pSourceSystemCode, pSourceCompanyId, pAgencyId);
            sendAsyncResult(responder,"getCompanyAgencyHistory");
        }

        public function updateErFicaDeferral(sourceSystemCd:String, sourceCompanyId:String, enabled:Boolean, responder:IResponder):void {
            record("updateErFicaDeferral", sourceSystemCd, sourceCompanyId, enabled);
            sendAsyncResult(responder,"updateErFicaDeferral");
        }
    }
}
