package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayItemDTO;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Jan 24, 2011
 * Time: 11:03:38 AM
 */
public class AddLiabilityAdjustmentsCore extends Process implements IProcess {

    private static final SpcfLogger logger = Application.getLogger(AddLiabilityAdjustmentsCore.class);

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mPayrollRunId;
    private CompanyAdjustmentSubmissionDTO mCompanyAdjustmentSubmissionDTO;
    private LiabilityAdjustmentOptionsDTO mLiabilityAdjustmentOptionsDTO;
    private SpcfCalendar mLiabilityAdjustmentDate;


    // Event detail tells what type of Adjustment was made - Normal, Reconciling or RecordedOnly
    private String eventDetail = "Normal";

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private CompanyAdjustmentSubmission mOriginalSubmission;
    private String mTransmissionId;
    private boolean mIsNotPartOfPayrollSubmission = true;

    public AddLiabilityAdjustmentsCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId,
                                       CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate,
                                       LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO, String pTransmissionId) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mPayrollRunId = pPayrollRunId;
        mCompanyAdjustmentSubmissionDTO = pCompanyAdjustmentSubmissionDTO;
        mLiabilityAdjustmentDate = DateDTO.convertToSpcfCalendar(pLiabilityAdjustmentDate);
        mLiabilityAdjustmentOptionsDTO = pLiabilityAdjustmentOptionsDTO;
        mTransmissionId = pTransmissionId;
    }

    public AddLiabilityAdjustmentsCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId,
                                       CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO, DateDTO pLiabilityAdjustmentDate,
                                       LiabilityAdjustmentOptionsDTO pLiabilityAdjustmentOptionsDTO, String pTransmissionId, boolean pIsNotPartOfPayrollSubmission) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mPayrollRunId = pPayrollRunId;
        mCompanyAdjustmentSubmissionDTO = pCompanyAdjustmentSubmissionDTO;
        mLiabilityAdjustmentDate = DateDTO.convertToSpcfCalendar(pLiabilityAdjustmentDate);
        mLiabilityAdjustmentOptionsDTO = pLiabilityAdjustmentOptionsDTO;
        mTransmissionId = pTransmissionId;
        mIsNotPartOfPayrollSubmission = pIsNotPartOfPayrollSubmission;
    }

    // used in payroll submit
    public void setPayrollRun(PayrollRun pPayrollRun) {
        mPayrollRunId = pPayrollRun.getSourcePayRunId();
        mPayrollRun = pPayrollRun;
    }

    public PayrollRun getPayrollRun() {
        return mPayrollRun;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate Company Parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate DTOs

        if (mCompanyAdjustmentSubmissionDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.CompanyAdjustmentSubmission,
                    "AddLiabilityAdjustments",
                    "CompanyAdjustmentSubmissionDTO");
            return validationResult;
        }

        validationResult.merge(mCompanyAdjustmentSubmissionDTO.validate());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mLiabilityAdjustmentOptionsDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.CompanyAdjustmentSubmission,
                    "AddLiabilityAdjustments",
                    "LiabilityAdjustmentOptionsDTO");
            return validationResult;
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate Liability Adjustment Date
        if (mLiabilityAdjustmentDate == null) {
            validationResult.getMessages().InvalidValue(EntityName.Date, mSourceCompanyId, "Liability Adjustment Date");
            return validationResult;
        }

        // Validate Company
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Validate Company Bank Account
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

        if (companyBankAccount == null) {
            validationResult.getMessages().CompanyActiveBankAccountNotFound(EntityName.CompanyBankAccount, mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId, null);
        }

        // Validate PayrollRun
        if (mPayrollRunId != null) {
            mPayrollRun = PayrollRun.findPayrollRun(mCompany, mPayrollRunId);
            if (mPayrollRun == null) {
                validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, mPayrollRunId, mPayrollRunId, mSourceSystemCd.toString(), mSourceCompanyId);
                return validationResult;
            }
        }

        // Validate Laws
        List<Law> companyLawsToEagerLoad = new ArrayList<Law>();
        for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : mCompanyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
            Law law = PayrollServices.entityFinder.findById(Law.class, liabilityAdjustmentDTO.getLawId());

            if (law == null) {
                validationResult.getMessages().LawDoesNotExist(EntityName.Law, liabilityAdjustmentDTO.getLawId());
                return validationResult;
            } else {
                CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(
                        mSourceSystemCd, mSourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());
                if (companyAgency == null) {
                    validationResult.getMessages().CompanyAgencyNotFound(
                            EntityName.CompanyAgency, mSourceSystemCd.toString(), mSourceSystemCd.toString(), mSourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());
                    return validationResult;
                }
            }

            // validate cobra
            if (liabilityAdjustmentDTO.getAmount() != null &&
                    liabilityAdjustmentDTO.getLawId().equals(Law.COBRA) &&
                    liabilityAdjustmentDTO.getAmount().isGreaterThan(SpcfMoney.ZERO) &&
                    !(mLiabilityAdjustmentOptionsDTO.isRecall() || mLiabilityAdjustmentOptionsDTO.isVoid())) {
                validationResult.getMessages().InvalidCOBRALiabilityAmount(EntityName.Law, mCompany.getSourceSystemCd().toString(),
                        mCompany.getSourceCompanyId(),
                        mCompanyAdjustmentSubmissionDTO.getSourceId(),
                        liabilityAdjustmentDTO.getAmount().toString());
                return validationResult;
            }

            if (liabilityAdjustmentDTO.getSourceEmployeeId() != null) {
                Employee employee = Employee.findEmployee(mCompany, liabilityAdjustmentDTO.getSourceEmployeeId());
                if (employee == null) {
                    validationResult.getMessages()
                            .EmployeeDoesNotExist(EntityName.Employee, liabilityAdjustmentDTO.getSourceEmployeeId(), mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId(), liabilityAdjustmentDTO.getSourceEmployeeId());
                    return validationResult;
                }
            }

            companyLawsToEagerLoad.add(law);
        }

        CompanyLaw.eagerLoadCompanyLaws(mCompany, companyLawsToEagerLoad);

        // validate original submission
        if (mCompanyAdjustmentSubmissionDTO.getOriginalSubmissionId() != null) {
            mOriginalSubmission = Application.findById(CompanyAdjustmentSubmission.class, mCompanyAdjustmentSubmissionDTO.getOriginalSubmissionId());
            if (mOriginalSubmission == null) {
                validationResult.getMessages().InvalidArgument(EntityName.CompanyAdjustmentSubmission, mCompanyAdjustmentSubmissionDTO.getSourceId(), "OriginalSubmissionId");
            }
        } else {
            if (mLiabilityAdjustmentOptionsDTO.isRecall()) {
                validationResult.getMessages().InvalidArgument(EntityName.CompanyAdjustmentSubmission, mCompanyAdjustmentSubmissionDTO.getSourceId(), "OriginalSubmissionId");
            }
        }

        // If this is a recall, validate impound has not been offloaded
        if (mLiabilityAdjustmentOptionsDTO.isRecall()) {
            mPayrollRun = mOriginalSubmission.getPayrollRun();
            mPayrollRunId = mPayrollRun.getSourcePayRunId();
            if (mPayrollRun.hasTaxImpoundOffloaded()) {
                validationResult.getMessages().PayrollRunAlreadyOffloaded(EntityName.PayrollRun,
                        mPayrollRun.getSourcePayRunId(), mPayrollRun.getSourcePayRunId(),
                        mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());
                return validationResult;
            }
        }

        if (mLiabilityAdjustmentOptionsDTO.recordFinancialTransactions()) {
            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : mCompanyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
                if (Law.COVID_ADVANCE_CREDIT.equals(liabilityAdjustmentDTO.getLawId())) {
                    // covid advance must be recorded by itself to ensure ATR is applied correctly
                    if (mCompanyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().size() > 1) {
                        validationResult.getMessages().GenericError(EntityName.CompanyAdjustmentSubmission, Law.COVID_ADVANCE_CREDIT, "COVID Advance must be recorded by itself to ensure ATR is applied correctly");
                    } else {
                        // do not allow recording an amount greater than the balance available in ATR for the 941 payment if recording covid advance
                        SpcfMoney atrAmount = LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, PaymentTemplate.getIRS_941(), mCompany, liabilityAdjustmentDTO.getEffectiveDate().toSpcfCalendar())
                                                           .values().stream().reduce(SpcfMoney.ZERO, (a, b) -> new SpcfMoney(a.add(b)));
                        if(atrAmount.isLessThan(liabilityAdjustmentDTO.getAmount())) {
                            validationResult.getMessages().GenericError(EntityName.CompanyAdjustmentSubmission, Law.COVID_ADVANCE_CREDIT, "COVID Advance amount must be less that ATR when recording financial transactions. ATR: " + atrAmount);
                        }
                    }
                }
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<CompanyAdjustmentSubmission> process() {

        ProcessResult<CompanyAdjustmentSubmission> processResult = new ProcessResult<CompanyAdjustmentSubmission>();
        if (mPayrollRun == null) {
            // Create a PayrollRun of Type = Adjustment
            mPayrollRun = PayrollRun.createAdjustmentPayrollRun(mCompany, mLiabilityAdjustmentDate);
            PayrollRun.getPayrollsInMemory(mCompany).add(mPayrollRun);
            Application.getSessionCache().addPrimaryKey(mPayrollRun.getNaturalKey(), mPayrollRun.getId());

            // Associate with transmission

            if (mTransmissionId != null) {
                mPayrollRun = Application.save(mPayrollRun);


                TransmissionPayrollRun transmissionPayrollRun = new TransmissionPayrollRun();
                transmissionPayrollRun.setPayrollRun(mPayrollRun);
                SourceSystemTransmission transmissionSecondary = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(mTransmissionId);
                transmissionPayrollRun.setSourceSystemTransmissionId(transmissionSecondary.getId().toString());
                transmissionPayrollRun.setPayrollProcess(PayrollProcessCode.SubmitPayroll);
                transmissionPayrollRun = Application.save(transmissionPayrollRun);

                transmissionSecondary.addTransmissionPayrollRun(transmissionPayrollRun);
                mPayrollRun.addTransmissionPayrollRun(transmissionPayrollRun);
                mPayrollRun = Application.save(mPayrollRun);
            }
        }

        // Create Company Adjustment Submission
        CompanyAdjustmentSubmission companyAdjustmentSubmission = createCompanyAdjustmentSubmission();

        // Create Liability Adjustments if indicated. This option should be used for creating
        // customer payments, which will create financial transactions but won't create liability
        // adjustments.
        if (mLiabilityAdjustmentOptionsDTO.recordLiabilities()) {
            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : mCompanyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
                createLiabilityAdjustment(liabilityAdjustmentDTO, mPayrollRun, companyAdjustmentSubmission);
            }
        }

        // if the payroll run id is null this is a new submission
        boolean isNewPayroll = mPayrollRunId == null;

        if (mLiabilityAdjustmentOptionsDTO.recordFinancialTransactions()) {
            if (isNewPayroll) {
                if (mLiabilityAdjustmentOptionsDTO.recordLiabilities()) {
                    processResult.merge(PayrollTaxHelper.createTaxTransactions(mPayrollRun, new PayrollTaxHelper.CreateTaxTransactionsOptions(mLiabilityAdjustmentOptionsDTO.debitCustomer(), LiabilityBalances.getLiabilityBalances(mPayrollRun, mLiabilityAdjustmentOptionsDTO.isBALF(), mLiabilityAdjustmentOptionsDTO.isForceToRecordFTs()), mLiabilityAdjustmentOptionsDTO.isForceToRecordFTs(), mLiabilityAdjustmentOptionsDTO.getUseVarianceAccount(), mLiabilityAdjustmentOptionsDTO.creditCustomer(), mLiabilityAdjustmentOptionsDTO.isSUIAdjustment(), mLiabilityAdjustmentOptionsDTO.getSettlementDate())));
                } else {
                    processResult.merge(PayrollTaxHelper.createTaxTransactions(mPayrollRun, new PayrollTaxHelper.CreateTaxTransactionsOptions(mLiabilityAdjustmentOptionsDTO.debitCustomer(), mCompanyAdjustmentSubmissionDTO.getLiabilityBalances(), mLiabilityAdjustmentOptionsDTO.isForceToRecordFTs(), mLiabilityAdjustmentOptionsDTO.creditCustomer())));
                }
            } else {
                processResult.merge(PayrollTaxHelper.updateTaxTransactions(mPayrollRun, mCompanyAdjustmentSubmissionDTO.getLiabilityBalances()));
            }
        }

        // Create LiabilityAdjustmentCreated Event
        if (mCompanyAdjustmentSubmissionDTO.getMemo() != null) {
            CompanyEvent.createManualLedgerEntryEvent(mCompany, companyAdjustmentSubmission.getId().toString(), mPayrollRun.getId().toString(), mCompanyAdjustmentSubmissionDTO.getMemo(), eventDetail);
        }
        CompanyEvent.createLiabilityAdjustmentCreatedEvent(mCompany, companyAdjustmentSubmission.getId().toString(), mPayrollRun.getId().toString(), null, eventDetail);

        processResult.setResult(companyAdjustmentSubmission);

        if (mIsNotPartOfPayrollSubmission) {
            if (mPayrollRun.updateEETotalsCalculationRequired()) {
                EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(mPayrollRun);
            }

            PayrollTaxHelper.checkForPayrollCompletion(mPayrollRun);
        }

        return processResult;
    }

    private CompanyAdjustmentSubmission createCompanyAdjustmentSubmission() {
        //PSRV003286	Liability adjustment voids are processed twice by the QBDT adapter
        //Check if the  CompanyAdjustmentSubmission is cached already
        CompanyAdjustmentSubmission cachedCompanyAdjustmentSubmission = CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(mCompany,
                mOriginalSubmission,
                null,
                mCompanyAdjustmentSubmissionDTO.getTotalAmount(),
                mCompanyAdjustmentSubmissionDTO.getSourceId(),
                mCompanyAdjustmentSubmissionDTO.getSubmissionDate().toSpcfCalendar());
        if (cachedCompanyAdjustmentSubmission == null) {
            CompanyAdjustmentSubmission companyAdjustmentSubmission = new CompanyAdjustmentSubmission();
            if (mCompanyAdjustmentSubmissionDTO.getSubmissionDate() != null) {
                companyAdjustmentSubmission.setSubmissionDate(mCompanyAdjustmentSubmissionDTO.getSubmissionDate().toSpcfCalendar());
            } else {
                companyAdjustmentSubmission.setSubmissionDate(PSPDate.getPSPTime());
            }

            companyAdjustmentSubmission.setSourceId(mCompanyAdjustmentSubmissionDTO.getSourceId());
            companyAdjustmentSubmission.setAmount(mCompanyAdjustmentSubmissionDTO.getTotalAmount());
            companyAdjustmentSubmission.setOriginalSubmission(mOriginalSubmission);
            companyAdjustmentSubmission.setCompany(mCompany);


            if (mCompanyAdjustmentSubmissionDTO.getQBDTTransactionInfoDTO() != null) {
                QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
                qbdtTransactionInfo.setCompany(mCompany);
                mCompanyAdjustmentSubmissionDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(qbdtTransactionInfo);
                qbdtTransactionInfo.setCompanyAdjustmentSubmission(companyAdjustmentSubmission);
                qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
                companyAdjustmentSubmission.setQbdtTransactionInfo(qbdtTransactionInfo);
            }

            companyAdjustmentSubmission = Application.save(companyAdjustmentSubmission);

            if (companyAdjustmentSubmission.getSourceId() != null) {
                NaturalKey naturalKey2 = new NaturalKey(CompanyAdjustmentSubmission.class, mCompany.getId(), companyAdjustmentSubmission.getSourceId());
                Application.getSessionCache().addPrimaryKey(naturalKey2, companyAdjustmentSubmission.getId());
                // This below cache is required for the de-dupe logic for the object in memory.(CI:PSRV003286)
                // The  erstwhile cache (using naturalKey2) from previous lines had to be kept,
                // since CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(Company pCompany, String pSourceId)  looks for it.
                // This object unfortunately is double-cached :(
                companyAdjustmentSubmission.cache();
            }
            return companyAdjustmentSubmission;
        } else {
            return cachedCompanyAdjustmentSubmission;
        }
    }

    private void createLiabilityAdjustment(LiabilityAdjustmentDTO pLiabilityAdjustmentDTO, PayrollRun pPayrollRun, CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        Law law = Application.findById(Law.class, pLiabilityAdjustmentDTO.getLawId());
        CompanyLaw companyLaw;
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(
                mSourceSystemCd, mSourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());

        // if the payroll item id is not null get the company law from the cache
        if (pLiabilityAdjustmentDTO.getPayrollItemId() != null) {
            companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, pLiabilityAdjustmentDTO.getPayrollItemId());
        } else {
            companyLaw = CompanyLaw.findCompanyLaw(companyAgency, law);
        }

        /*
         *PSP-10168
         *Fixing Null amount, taxable wages and total wages that can come through
         * 1. LiabilityAdjustmentDto created in AdjustSUITaxPayment.java in case of manual adjustments
         * 2. LiabilityAdjustmentDto created in LiabilityAdjustmentsCleanUp.java
         * 3. OFX
         */

        SpcfMoney amount = pLiabilityAdjustmentDTO.getAmount();
        if (amount == null || (amount != null && !amount.isNumber())) {
            amount = SpcfMoney.ZERO;
            logger.info(String.format("Liability adjustment: Invalid amount received for law Id: %s, company %s:%s.",
                    law.getLawId(),
                    mCompany.getSourceSystemCd().toString(),
                    mCompany.getSourceCompanyId()));
        }
        SpcfMoney taxableWages = pLiabilityAdjustmentDTO.getTaxableWages();
        if (taxableWages == null || (taxableWages != null && !taxableWages.isNumber())) {
            taxableWages = SpcfMoney.ZERO;
            logger.info(String.format("Liability adjustment: Invalid taxable wages received for law Id: %s, company %s:%s.",
                    law.getLawId(),
                    mCompany.getSourceSystemCd().toString(),
                    mCompany.getSourceCompanyId()));
        }
        SpcfMoney totalWages = pLiabilityAdjustmentDTO.getTotalWages();
        if (totalWages == null || (totalWages != null && !totalWages.isNumber())) {
            totalWages = SpcfMoney.ZERO;
            logger.info(String.format("Liability adjustment: Invalid total wages received for law Id: %s, company %s:%s.",
                    law.getLawId(),
                    mCompany.getSourceSystemCd().toString(),
                    mCompany.getSourceCompanyId()));
        }
        //PSRV003286	Liability adjustment voids are processed twice by the QBDT adapter
        // CHeck if the Liability Adjustments are in cache already - we dont want to dupe these
        LiabilityAdjustment cachedLiabilityAdjustment = LiabilityAdjustment.findLiabilityAdjustment(mCompany,
                pPayrollRun,
                null,
                law,
                companyLaw,
                pCompanyAdjustmentSubmission,
                pLiabilityAdjustmentDTO.isReconcilingAdjustment(),
                amount,
                totalWages,
                taxableWages,
                mLiabilityAdjustmentDate
        );

        if (cachedLiabilityAdjustment == null) {
            LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
            liabilityAdjustment.setCompany(mCompany);
            liabilityAdjustment.setAmount(amount);
            liabilityAdjustment.setTaxableWages(taxableWages);
            liabilityAdjustment.setTotalWages(totalWages);
            liabilityAdjustment.setEffectiveDate(mLiabilityAdjustmentDate);
            liabilityAdjustment.setIsReconcilingAdjustment(pLiabilityAdjustmentDTO.isReconcilingAdjustment());
            if (pLiabilityAdjustmentDTO.isReconcilingAdjustment()) {
                eventDetail = "Reconciling";
            }
            liabilityAdjustment.setLaw(law);
            liabilityAdjustment.setCompanyLaw(companyLaw);
            liabilityAdjustment.setPayrollRun(pPayrollRun);
            liabilityAdjustment.setCompanyAdjustmentSubmission(pCompanyAdjustmentSubmission);
            pPayrollRun.addLiabilityAdjustment(liabilityAdjustment);
            if (pLiabilityAdjustmentDTO.getQBDTTransactionInfoDTO() != null) {
                QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
                qbdtTransactionInfo.setCompany(mCompany);
                pLiabilityAdjustmentDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(qbdtTransactionInfo);
                qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
                liabilityAdjustment.setQbdtTransactionInfo(qbdtTransactionInfo);
                qbdtTransactionInfo.setLiabilityAdjustment(liabilityAdjustment);
            }

            if (pLiabilityAdjustmentDTO.getSourceEmployeeId() != null) {
                Employee employee = Employee.findEmployee(mCompany, pLiabilityAdjustmentDTO.getSourceEmployeeId());
                liabilityAdjustment.setEmployee(employee);
            }

            liabilityAdjustment = Application.save(liabilityAdjustment);
            liabilityAdjustment.cache();

            for (PayItemDTO payItemDTO : pLiabilityAdjustmentDTO.getPayItemDTOs()) {
                PayItem payItem = new PayItem();
                payItem.setLiabilityAdjustment(liabilityAdjustment);
                payItem.setAmount(payItemDTO.getAmount());
                payItem.setPayItemCd(payItemDTO.getPayItemCode());
                payItem.setEffectiveDate(payItemDTO.getEffectiveDate().toSpcfCalendar());
                Employee employee = Employee.findEmployee(mCompany, payItemDTO.getEmployeeId());
                payItem.setEmployee(employee);
                payItem = Application.save(payItem);
                liabilityAdjustment.getPayItemCollection().add(payItem);
            }

            // update objects in cache
            pCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection().add(liabilityAdjustment);
            pPayrollRun.getLiabilityAdjustmentCollection().add(liabilityAdjustment);

            // cache adjustments
            LiabilityAdjustment.getAdjustmentsInMemory(mCompany).add(liabilityAdjustment);
        }
    }

}
