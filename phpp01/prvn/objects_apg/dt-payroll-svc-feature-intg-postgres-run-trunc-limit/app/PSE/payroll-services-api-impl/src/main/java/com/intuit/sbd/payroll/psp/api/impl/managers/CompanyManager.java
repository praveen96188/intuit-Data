/*
 * $Id: //psp/dev/PSE/PayrollServicesAPIImpl/src/com/intuit/sbd/payroll/psp/api/impl/managers/CompanyManager.java#5 $
 * $Id: //psp/dev/PSE/PayrollServicesAPIImpl/src/com/intuit/sbd/payroll/psp/api/impl/managers/CompanyManager.java#5 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.managers.ICompanyManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.datamanager.DGAuthBasedDeleteEmployerProcessCore;
import com.intuit.sbd.payroll.psp.processes.guideline401k.employer.Add401kCompanyQBDTPItemCore;
import com.intuit.sbd.payroll.psp.processes.guideline401k.employer.AddOrUpdate401kEmployerDeductionCore;
import com.intuit.sbd.payroll.psp.processes.guideline401k.employer.AddOrUpdate401kEmployerPensionCore;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import com.intuit.v4.payroll.employer.EmployerPension;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Allen Chaves
 */
class CompanyManager implements ICompanyManager {

    public ProcessResult<Company> addCompany(CompanyDTO pCompany) {
        AddCompanyCore processCore = new AddCompanyCore(pCompany);
        ProcessResult<Company> processResult = processCore.execute();

        processResult.setResult(processCore.getCompany());

        return processResult;
    }

    public ProcessResult<Company> updateCompany(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyDTO pCompany) {
        UpdateCompanyCore processCore = new UpdateCompanyCore(pSourceSystemCode, pSourceCompanyId, pCompany);
        ProcessResult<Company> processResult = processCore.execute();

        processResult.setResult(processCore.getUpdatedCompany());

        return processResult;
    }

    public ProcessResult<Company> updateCompanyWithAccountService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyDTO pCompany) {
            UpdateCompanyCore processCore = new UpdateCompanyCore(pSourceSystemCode, pSourceCompanyId, pCompany,true);
            ProcessResult<Company> processResult = processCore.execute();

            processResult.setResult(processCore.getUpdatedCompany());

            return processResult;
        }

    public ProcessResult<CompanyLawRate> updateCompanyLawRate(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                                              Law pLaw, SpcfCalendar pQuarterStartDate, Double pRate, boolean pPushToQuickbooks) {

        UpdateCompanyLawRateCore processCore = new UpdateCompanyLawRateCore(pSourceSystemCd, pSourceCompanyId, pLaw, pQuarterStartDate,
                                                                            pRate, pPushToQuickbooks);
        return processCore.execute();
    }

    public ProcessResult<Company> updateSourceCompanyId(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pNewSourceCompanyId) {
        UpdateSourceCompanyIdCore processCore = new UpdateSourceCompanyIdCore(pSourceSystemCode, pSourceCompanyId, pNewSourceCompanyId);
        ProcessResult<Company> processResult = processCore.execute();

        processResult.setResult(processCore.getUpdatedCompany());

        return processResult;
    }

    public ProcessResult<Company> updateQBCompanyInfo(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyDTO pCompany) {
        UpdateQBCompanyInfoCore processCore = new UpdateQBCompanyInfoCore(pSourceSystemCode, pSourceCompanyId, pCompany);
        ProcessResult<Company> processResult = processCore.execute();
        processResult.setResult(processCore.getUpdatedCompany());

        return processResult;
    }

    public ProcessResult<Company> updateCompanyFundingModel(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, FundingModel pNewFundingModel) {
        return new UpdateCompanyFundingModelCore(pSourceSystemCd, pSourceCompanyId, pNewFundingModel).execute();
    }

    // Service

    public ProcessResult<CompanyService> addService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ServiceInfoDTO pServiceInfo) {
        return new AddServiceCore(pSourceSystemCode, pSourceCompanyId, pServiceInfo).execute();
    }

    public ProcessResult<CompanyService> updateService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ServiceInfoDTO pServiceInfo) {
        return new UpdateServiceCore(pSourceSystemCode, pSourceCompanyId, pServiceInfo).execute();
    }

    public ProcessResult<CompanyService> reactivateService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ServiceCode pServiceCode) {
        return new ReactivateServiceCore(pSourceSystemCode, pSourceCompanyId, pServiceCode).execute();
    }

    public ProcessResult<CompanyService> deactivateService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ServiceCode pServiceCode) {
        return new DeactivateServiceCore(pSourceSystemCode, pSourceCompanyId, pServiceCode).execute();
    }

    public ProcessResult<CompanyService> terminateService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ServiceCode pServiceCode) {
        return new TerminateServiceCore(pSourceSystemCode, pSourceCompanyId, pServiceCode).execute();
    }

    // DDLimits

    public ProcessResult<DDCompanyServiceInfo> updateDDLimits(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, SpcfMoney pNewCompanyDDLimit, SpcfMoney pNewEmployeeDDLimit) {
        UpdateDDLimits updateDDLimits = new UpdateDDLimits(pSourceSystemCd, pSourceCompanyId, pNewCompanyDDLimit, pNewEmployeeDDLimit);
        ProcessResult<DDCompanyServiceInfo> processResult = updateDDLimits.execute();


        processResult.setResult(updateDDLimits.getDDCompanyServiceInfo());

        return processResult;
    }

    // BP Limits

    public ProcessResult<BPCompanyServiceInfo> updateBPLimits(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, SpcfMoney pNewCompanyDDLimit, SpcfMoney pNewPayeeDDLimit) {
        UpdateBPLimits updateBPLimits = new UpdateBPLimits(pSourceSystemCd, pSourceCompanyId, pNewCompanyDDLimit, pNewPayeeDDLimit);
        ProcessResult<BPCompanyServiceInfo> processResult = updateBPLimits.execute();


        processResult.setResult(updateBPLimits.getBPCompanyServiceInfo());

        return processResult;
    }

    // CompanyBankAccount

    public ProcessResult<CompanyBankAccount> addCompanyBankAccount(SourceSystemCode pSourceSystemCode,
                                                                   String pSourceCompanyId,
                                                                   CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                                   boolean pShouldAddRandomDebits,
                                                                   boolean pShouldCheckForExistingBA, boolean pIsPSPRandomDollarVerificationRequired) {
        AddCompanyBankAccountCore processCore = new AddCompanyBankAccountCore(pSourceSystemCode, pSourceCompanyId,
                pCompanyBankAccountDTO, pShouldAddRandomDebits, pShouldCheckForExistingBA, pIsPSPRandomDollarVerificationRequired);
        ProcessResult<CompanyBankAccount> processResult = processCore.execute();

        processResult.setResult(processCore.getCompanyBankAccount());

        return processResult;
    }

    public ProcessResult<CompanyBankAccount> addCompanyBankAccount(SourceSystemCode pSourceSystemCode,
                                                                   String pSourceCompanyId,
                                                                   CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                                   boolean pShouldAddRandomDebits,
                                                                   boolean pShouldCheckForExistingBA) {
        return addCompanyBankAccount(pSourceSystemCode, pSourceCompanyId, pCompanyBankAccountDTO, pShouldAddRandomDebits, pShouldCheckForExistingBA, true);
    }


    public ProcessResult<CompanyBankAccount> updateCompanyBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyBankAccountDTO pCompanyBankAccountDTO) {
        return new UpdateCompanyBankAccountCore(pSourceSystemCode, pSourceCompanyId, pCompanyBankAccountDTO).execute();
    }

    public ProcessResult<CompanyBankAccount> updateCompanyBankAccountWithAccountService(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyBankAccountDTO pCompanyBankAccountDTO) {
        return new UpdateCompanyBankAccountCore(pSourceSystemCode, pSourceCompanyId, pCompanyBankAccountDTO,true).execute();
    }

    public ProcessResult<CompanyBankAccount> deactivateCompanyBankAccount(SourceSystemCode pSourceSystemCode,
                                                                          String pSourceCompanyId,
                                                                          String pSourceCompanyBankAccountId,
                                                                          boolean pShouldAllowPendingTransactions,
                                                                          boolean pIgnoreSystemCapabilityChk) {
        return new DeactivateCompanyBankAccountCore(pSourceSystemCode, pSourceCompanyId,
                pSourceCompanyBankAccountId, pShouldAllowPendingTransactions, pIgnoreSystemCapabilityChk).execute();
    }

    public ProcessResult<CompanyBankAccount> verifyCompanyBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceCompanyBankAccountId, SpcfMoney pTxnToVerify1, SpcfMoney pTxnToVerify2, boolean mAgentVerify) {
        return new VerifyCompanyBankAccountCore(pSourceSystemCode, pSourceCompanyId, pSourceCompanyBankAccountId, pTxnToVerify1, pTxnToVerify2, mAgentVerify).execute();
    }

    /*
    @param pCompanyEventId OPTIONAL.  Will attach the note to this event is specified; otherwise create a new event
     */
    public ProcessResult<CompanyNote> addCompanyNote(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pCompanyEventId, String pInsertUserId, String pNotes, boolean pAlert) {
        return new AddCompanyEventNoteCore(pSourceSystemCd, pSourceCompanyId, pCompanyEventId, pInsertUserId, pNotes, pAlert).execute();
    }

    public ProcessResult<CompanyEvent> addStrikeEvent(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                      String pStrikeReason, SpcfCalendar pStrikeDate) {
        AddStrikeDD strikeDDProcess = new AddStrikeDD(pSourceSystemCode, pSourceCompanyId, pStrikeReason,
                pStrikeDate);

        ProcessResult<CompanyEvent> processResult = strikeDDProcess.execute();

        processResult.setResult(strikeDDProcess.getStrikeEvent());

        return processResult;
    }

    public ProcessResult<CompanyEvent> cancelStrikeEvent(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                         SpcfUniqueId pCompanyEventId) {
        CancelStrikeDDProcess strikeDDProcess = new CancelStrikeDDProcess(pSourceSystemCode, pSourceCompanyId,
                pCompanyEventId);

        ProcessResult<CompanyEvent> processResult = strikeDDProcess.execute();

        processResult.setResult(strikeDDProcess.getCompanyEvent());

        return processResult;
    }

    public String createSourceCompanyId(SourceSystemCode pSourceSystemCd) {
        return createSourceCompanyId(pSourceSystemCd, null);
    }

    public String createSourceCompanyId(SourceSystemCode pSourceSystemCd, String stateCode) {
        // For testing purposes, force all PSIDs to start in the "999" range.
        boolean use999 = SystemParameter.findBooleanValue(SystemParameter.Code.ASSISTED_PSIDS_START_WITH_999, false);

        //Since this method is only currently supported for QBDT, ensure that's the source system we are getting in
        if (pSourceSystemCd != SourceSystemCode.QBDT) {
            throw new UnsupportedOperationException("Create source company ID is currently only available for QuickBooks Desktop.");
        }

        String newSourceCompanyId = null;
        List<Long> retList = new ArrayList<Long>();

        if (pSourceSystemCd.equals(SourceSystemCode.QBDT)) {
            if (stateCode == null) {
                retList.add(Application.nextSequenceValue(SequenceId.SEQ_QBDT_SOURCE_COMPANY_ID, Long.class));
            } else {
                String[] paramNames = {"stateCode"};
                Object[] paramValues = {stateCode};
                if (use999) {
                    paramValues[0] = "XX";
                }
                retList = Application.executeNamedQuery("findNextQBDTSourceCompanyIdByState", paramNames, paramValues);
            }
        }

        if (retList.get(0) != null) {
            newSourceCompanyId = retList.get(0).toString();
        }

        return newSourceCompanyId;
    }

    public ProcessResult<CompanyBankAccount> resetBankAccountVerifyRetryCount(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pSourceCompanyBankAccountId) {
        ResetBankVerifyRetryCountCore processCore = new ResetBankVerifyRetryCountCore(pSourceSystemCd, pSourceCompanyId, pSourceCompanyBankAccountId);
        ProcessResult<CompanyBankAccount> processResult = processCore.execute();

        processResult.setResult(processCore.getCompanyBankAccount());

        return processResult;
    }

    public ProcessResult<CompanyBankAccount> reinitiateBankAccountRandomDebits(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pSourceCompanyBankAccountId) {
        ReinitiateBankAccountRandomDebitsCore processCore = new ReinitiateBankAccountRandomDebitsCore(pSourceSystemCd, pSourceCompanyId, pSourceCompanyBankAccountId);
        ProcessResult<CompanyBankAccount> processResult = processCore.execute();

        processResult.setResult(processCore.getCompanyBankAccount());

        return processResult;
    }

    /**
     * Claims (or redeems) an offer for a company.
     *
     * @param pOfferCd The offer-code identifying the offer to be claimed
     * @param pCompany The company claiming the offer
     * @return A ProcessResult containing, when successful, the CompanyOffer entity relating the Company to the Offer
     */
    public ProcessResult<CompanyOffer> claimOfferForCompany(String pOfferCd, String pPromotionId, Company pCompany) {
        return new ClaimOfferCore(pCompany, pOfferCd, pPromotionId).execute();
    }

    public ProcessResult<CompanyService> updateServiceStatus(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                             ServiceCode pServiceCode, ServiceSubStatusCode pServiceSubStatusCd) {
        UpdateServiceStatusCore updateServiceStatus = new UpdateServiceStatusCore(pSourceSystemCode, pSourceCompanyId,
                pServiceCode, pServiceSubStatusCd);
        ProcessResult<CompanyService> processResult = updateServiceStatus.execute();
        processResult.setResult(updateServiceStatus.getCompanyService());
        return processResult;
    }

    public ProcessResult<Company> addOnHoldReason(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                  ServiceSubStatusCode pServiceSubStatusCd) {
        AddOnHoldStatusCore addOnHoldProcess = new AddOnHoldStatusCore(pSourceSystemCode, pSourceCompanyId,
                pServiceSubStatusCd);
        ProcessResult<Company> processResult = addOnHoldProcess.execute();
        processResult.setResult(addOnHoldProcess.getCompany());
        return processResult;
    }

    public ProcessResult<Company> removeOnHoldReason(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                     ServiceSubStatusCode pServiceSubStatusCd) {
        RemoveOnHoldStatusCore removeOnHoldProcess = new RemoveOnHoldStatusCore(pSourceSystemCode, pSourceCompanyId,
                pServiceSubStatusCd);
        ProcessResult<Company> processResult = removeOnHoldProcess.execute();
        processResult.setResult(removeOnHoldProcess.getCompany());
        return processResult;
    }

    public ProcessResult movePendingTransactionsToBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                              String pSourceCompanyBankAccountId, boolean pShouldIgnoreBankAccountValidation) {
        return new MovePendingTransactionToBankAccount(pSourceSystemCode,
                pSourceCompanyId, pSourceCompanyBankAccountId, pShouldIgnoreBankAccountValidation).execute();
    }

    public ProcessResult<CompanyBankAccount> changeCompanyBankAccount(SourceSystemCode pSourceSystemCd,
                                                                      String pSourceCompanyId, CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                                      boolean pShouldAddRandomDebits,
                                                                      boolean pShouldAllowPendingTransactions,
                                                                      boolean pShouldMovePendingTransactionsToAccount) {

        ChangeCompanyBankAccountCore changeCBAProcess = new ChangeCompanyBankAccountCore(pSourceSystemCd,
                pSourceCompanyId, pCompanyBankAccountDTO, pShouldAddRandomDebits,
                pShouldAllowPendingTransactions, pShouldMovePendingTransactionsToAccount);
        ProcessResult<CompanyBankAccount> processResult = changeCBAProcess.execute();
        processResult.setResult(changeCBAProcess.getCompanyBankAccount());
        return processResult;
    }

    public ProcessResult<CompanyBankAccount> changeCompanyBankAccountWithAccountService(SourceSystemCode pSourceSystemCd,
                                                                      String pSourceCompanyId, CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                                      boolean pShouldAddRandomDebits,
                                                                      boolean pShouldAllowPendingTransactions,
                                                                      boolean pShouldMovePendingTransactionsToAccount,boolean pAccountServiceUpdate,boolean isPSPRandomDollarVerificationRequired) {

        ChangeCompanyBankAccountCore changeCBAProcess = new ChangeCompanyBankAccountCore(pSourceSystemCd,
                pSourceCompanyId, pCompanyBankAccountDTO, pShouldAddRandomDebits,
                pShouldAllowPendingTransactions, pShouldMovePendingTransactionsToAccount,pAccountServiceUpdate,isPSPRandomDollarVerificationRequired);
        ProcessResult<CompanyBankAccount> processResult = changeCBAProcess.execute();
        processResult.setResult(changeCBAProcess.getCompanyBankAccount());
        return processResult;
    }


    public ProcessResult updateSubStatuses(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                           ServiceCode pServiceCd, DomainEntitySet<ServiceSubStatus> pSubStatusList) {
        return new UpdateRoleSubStatusCore(pSourceSystemCd, pSourceCompanyId, pServiceCd,
                pSubStatusList).execute();
    }

    public ProcessResult<Company> removeFraudFlag(SourceSystemCode pSourceSystemCode, String pSourceCompanyId) {
        return new RemoveFraudFlagCore(pSourceSystemCode, pSourceCompanyId).execute();
    }

    public ProcessResult<CompanyEvent> addCompanyEvent(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyEventDTO pCompanyEventDTO) {
        return new AddCompanyEventCore(pSourceSystemCode, pSourceCompanyId, pCompanyEventDTO).execute();
    }

    public ProcessResult<CompanyEventEmail> resendEmail(SourceSystemCode sourceSystemCd, String sourceCompanyId, String emailId, boolean resendRelatedEvents) {
        return new ResendEmail(sourceSystemCd, sourceCompanyId, emailId, resendRelatedEvents,null).execute();
    }

    public ProcessResult<CompanyEventEmail> sendEmailToMtl(SourceSystemCode sourceSystemCd, String sourceCompanyId, String emailSeqId, boolean resendRelatedEvents, String sessionUserEmailAddress) {
        return new ResendEmail(sourceSystemCd, sourceCompanyId, emailSeqId, resendRelatedEvents, sessionUserEmailAddress).execute();
    }

    public ProcessResult updateCompanyAgency(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pAgencyId,
                                             CompanyAgencyDTO pCompanyAgencyDTO) {
        UpdateCompanyAgencyCore process = new UpdateCompanyAgencyCore(pSourceSystemCode, pSourceCompanyId, pAgencyId,
                pCompanyAgencyDTO);
        return process.execute();

    }

    public ProcessResult<CompanyFilingAmount> addOrUpdateCompanyFilingAmount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyFilingAmountDTO pCompanyFilingAmountDTO) {
        AddOrUpdateCompanyFilingAmountCore process = new AddOrUpdateCompanyFilingAmountCore(pSourceSystemCode, pSourceCompanyId, pCompanyFilingAmountDTO);
        return process.execute();
    }

    public ProcessResult<CompanyPayrollItem> addOrUpdateCompanyPayrollItem(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyPayrollItemDTO pCompanyPayrollItemDTO) {
        AddOrUpdateCompanyPayrollItemCore process = new AddOrUpdateCompanyPayrollItemCore(pSourceSystemCode, pSourceCompanyId, pCompanyPayrollItemDTO);
        return process.execute();
    }

    public ProcessResult<CompanyLaw> addOrUpdateCompanyLaw(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyLawDTO pCompanyLawDTO) {
        AddOrUpdateCompanyLawCore process = new AddOrUpdateCompanyLawCore(pSourceSystemCode, pSourceCompanyId, pCompanyLawDTO);
        return process.execute();
    }

    public ProcessResult<CompanyLaw> addOrUpdateCompanyLawRates(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceLawId, List<CompanyLawRateDTO> pCompanyLawRateDTOs, boolean pIsSystemUpdate) {
        AddOrUpdateCompanyLawRate process = new AddOrUpdateCompanyLawRate(pSourceSystemCode, pSourceCompanyId, pSourceLawId, pCompanyLawRateDTOs, pIsSystemUpdate);
        return process.execute();
    }

    public ProcessResult addOrUpdateAgencyId(SourceSystemCode sourceSystemCd, String sourceCompanyId, AgencyIdDTO agencyIdDTO) {
        return new  AddOrUpdateAgencyIdCore(sourceSystemCd, sourceCompanyId, agencyIdDTO).execute();
    }

    public ProcessResult<RAFEnrollment> updateRAFEnrollmentStatus(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RAFEnrollment pExistingRAFEnrollment, RAFEnrollmentStatus pStatus) {
        UpdateRAFEnrollmentStatusCore process = new UpdateRAFEnrollmentStatusCore(pSourceSystemCode, pSourceCompanyId, pExistingRAFEnrollment, pStatus);
        return process.execute();
    }

    public ProcessResult<RAFEnrollment> reInitiateRAFEnrollment(RAFEnrollment pRAFEnrollment) {
        ReInitiateRAFEnrollment process = new ReInitiateRAFEnrollment(pRAFEnrollment);
        return process.execute();
    }

    public ProcessResult<RAFEnrollment> rejectRAFEnrollment(RAFEnrollment pRAFEnrollment, String pRejectReason) {
        RejectRAFEnrollment process = new RejectRAFEnrollment(pRAFEnrollment, pRejectReason);
        return process.execute();
    }

    public ProcessResult<ACHEnrollment> addACHEnrollment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, boolean pReAddIfPresent) {
        return new AddACHEnrollmentCore(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, pReAddIfPresent).execute();
    }

    public ProcessResult<ACHEnrollment> deleteACHEnrollment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd) {
        return new DeleteACHEnrollmentCore(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd).execute();
    }

    public ProcessResult<ACHEnrollment> updateACHEnrollmentStatus(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, ACHEnrollmentStatus pACHEnrollmentStatus) {
        return new UpdateACHEnrollmentStatusCore(pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, pACHEnrollmentStatus).execute();
    }

    public ProcessResult<ACHEnrollmentFile> uploadACHResponseFile(String pFileName, String pFileContent) {
        return new UploadACHResponseFileCore(pFileName, pFileContent).execute();
    }

    public ProcessResult<EftpsEnrollment> updateEftpsEnrollment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EftpsEnrollmentStatus pEftpsEnrollmentStatus) {
        return new UpdateEftpsEnrollmentCore(pSourceSystemCode, pSourceCompanyId, pEftpsEnrollmentStatus).execute();
    }

    public ProcessResult<EftpsEnrollment> updateEftpsEnrollment(EftpsEnrollment pEftpsEnrollment, EftpsEnrollmentStatus pEftpsEnrollmentStatus) {
        return new UpdateEftpsEnrollmentCore(pEftpsEnrollment, pEftpsEnrollmentStatus).execute();
    }

    public ProcessResult<EftpsEnrollment> createSecondaryEftpsEnrollment(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pFedTaxId, String pLegalName, String pLegalZip) {
        return new CreateSecondaryEftpsEnrollment(pSourceSystemCode, pSourceCompanyId, pFedTaxId, pLegalName, pLegalZip).execute();
    }

    public ProcessResult<CheckPrintSignature> addOrUpdateCheckPrintSignature(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, byte[] pSignatureImage) {
        AddCheckPrintSignatureCore process = new AddCheckPrintSignatureCore(pSourceSystemCode, pSourceCompanyId, pSignatureImage);
        return process.execute();
    }

    public ProcessResult<CompanyPaycheckBatch> addCheckPrintTestBatch(SourceSystemCode pSourceSystemCode, String pSourceCompanyId) {
        AddCheckPrintTestBatchCore process = new AddCheckPrintTestBatchCore(pSourceSystemCode, pSourceCompanyId);
        return process.execute();
    }

    public ProcessResult<Company> updateCompanyTokensAndIdsCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyDTO pCompanyDTO, boolean pOverride) {
        UpdateCompanyTokensAndIdsCore process = new UpdateCompanyTokensAndIdsCore(pSourceSystemCode, pSourceCompanyId, pCompanyDTO, pOverride);
        return process.execute();
    }

    public ProcessResult updateDataSyncTokens(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, UpdateDataSyncTokensDTO dto) {
        return new UpdateDataSyncTokens(pSourceSystemCode, pSourceCompanyId, dto).execute();
    }

    public ProcessResult<LiabilityCheck> addOrUpdateLiabilityCheck(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, LiabilityCheckDTO pLiabilityCheckDTO) {
        AddOrUpdateLiabilityCheckCore process = new AddOrUpdateLiabilityCheckCore(pSourceSystemCode, pSourceCompanyId, pLiabilityCheckDTO);
        return process.execute();
    }

    public ProcessResult<QbdtPayrollTransaction> addOrUpdateQBDTPayrollTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO) {
        AddOrUpdatePayrollTransactionCore process = new AddOrUpdatePayrollTransactionCore(pSourceSystemCode, pSourceCompanyId, pQBDTPayrollTransactionDTO);
        return process.execute();
    }

    public ProcessResult<EntityChange> addOrUpdateEntityChange(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EntityChangeDTO pEntityChangeDTO) {
        AddOrUpdateEntityChangeCore process = new AddOrUpdateEntityChangeCore(pSourceSystemCode, pSourceCompanyId, pEntityChangeDTO);
        return process.execute();
    }

    public ProcessResult recalculateCompanyLedgerBalances(SourceSystemCode pSourceSystemCode, String pSourceCompanyId) {
        RecalculateCompanyLedgerBalances process = new RecalculateCompanyLedgerBalances(pSourceSystemCode, pSourceCompanyId);
        return process.execute();
    }

    public ProcessResult generateLiabilityChecks(Company pCompany, PayrollRun pPayrollRun) {
        return new GenerateLiabilityCheckCore(pCompany, pPayrollRun).execute();
    }

    public ProcessResult generateLiabilityCheck(Company pCompany, FinancialTransaction pFinancialTransaction) {
        return new GenerateLiabilityCheckCore(pCompany, pFinancialTransaction).execute();
    }

    public ProcessResult<CompanyOffering> updateCompanyOffering(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, OfferingInfoDTO pOfferingInfoDTO) {
        UpdateCompanyOfferingCore process = new UpdateCompanyOfferingCore(pSourceSystemCode, pSourceCompanyId, pOfferingInfoDTO);
        return process.execute();
    }

    public ProcessResult removeInvalidFlagOnCompanyContactsAndPayees(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pEmailAddress) {
        return new RemoveInvalidFlagOnCompanyContactsAndPayees(pSourceSystemCode, pSourceCompanyId, pEmailAddress).execute();
    }

    public ProcessResult addOrUpdateCompanyIdentity(Company pCompany, String workflow) {
        AddOrUpdateCompanyIdentityCore addOrUpdateCompanyIdentityCore = new AddOrUpdateCompanyIdentityCore(pCompany, workflow);
        return addOrUpdateCompanyIdentityCore.execute();
    }

    public ProcessResult disassociateCompanyByPSID(String psid, SourceSystemCode sourceSystemCode,
                                                   String workOrderId, String workOrderCreatedTime){
        return new DGAuthBasedDeleteEmployerProcessCore(psid,sourceSystemCode,workOrderId,
                workOrderCreatedTime).execute();
    }

    public ProcessResult addOrUpdate401kCompanyQBDTPItem(Hcm401kCompanyPolicy hcm401kCompanyPolicy, String employerPItemId, String employeePItemId){
        return new Add401kCompanyQBDTPItemCore(hcm401kCompanyPolicy, employerPItemId, employeePItemId).execute();
    }

    public ProcessResult addOrUpdate401kEmployerDeduction(Company company, EmployerDeduction employerDeduction){
        return new AddOrUpdate401kEmployerDeductionCore(company, employerDeduction).execute();
    }

    public ProcessResult addOrUpdate401kEmployerPension(Company company, EmployerPension employerPension){
        return new AddOrUpdate401kEmployerPensionCore(company, employerPension).execute();
    }

    public ProcessResult<SMSMigrationStatus> revertSMSMigratedCompany(String psId, String tid){
        RevertSMSMigratedCompaniesCore revertSMSMigratedCompaniesCore = new RevertSMSMigratedCompaniesCore(psId, SourceSystemCode.QBDT, tid);
        ProcessResult<SMSMigrationStatus> pr = revertSMSMigratedCompaniesCore.execute();
        return pr;
    }

    public ProcessResult<SMSMigrationStatus> enableSMSMigratedFlags(String psId, String tid){
        EnableSMSMigrationFlagCore enableSMSMigrationFlagCore = new EnableSMSMigrationFlagCore(psId, SourceSystemCode.QBDT, tid);
        ProcessResult<SMSMigrationStatus> pr = enableSMSMigrationFlagCore.execute();
        return pr;
    }

    public ProcessResult<SMSMigrationStatus> migratePSPToSMS(String psId, String tid){
        MigrateCompanyFromPSPToSMSCore migrateCompanyFromPSPToSMSCore = new MigrateCompanyFromPSPToSMSCore(psId, SourceSystemCode.QBDT, tid);
        ProcessResult<SMSMigrationStatus> pr = migrateCompanyFromPSPToSMSCore.execute();
        return pr;
    }

    public ProcessResult<SMSMigrationStatus> migratePSPToSMS(String psId, String tid, boolean debugEnabled, boolean riskLmtMigrationEnabled){
        MigrateCompanyFromPSPToSMSCore migrateCompanyFromPSPToSMSCore = new MigrateCompanyFromPSPToSMSCore(psId, SourceSystemCode.QBDT, tid,debugEnabled );
        ProcessResult<SMSMigrationStatus> pr = migrateCompanyFromPSPToSMSCore.execute();
        if(riskLmtMigrationEnabled){
            //TODO (!isRiskLimitMigrated || pr.isSuccessful()) &&  riskLmtMigrationEnabled) + pr.getResult() == MigrationComplete
            MigrateRiskLimitsCore migrateRiskLimitsCore = new MigrateRiskLimitsCore();
            ProcessResult<SMSMigrationStatus> res = migrateRiskLimitsCore.execute();
        }
        return pr;
    }
}
