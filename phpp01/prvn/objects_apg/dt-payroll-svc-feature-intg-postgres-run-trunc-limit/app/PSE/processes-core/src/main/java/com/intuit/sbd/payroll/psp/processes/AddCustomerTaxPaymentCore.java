package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CustomerTaxPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PaymentStatus;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Jan 24, 2011
 * Time: 11:03:38 AM
 */
public class AddCustomerTaxPaymentCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private CustomerTaxPaymentDTO mCustomerTaxPaymentDTO;
    private Company mCompany;

    private PaymentTemplate mPaymentTemplate;
    private HashMap<Law, SpcfMoney> mLawPaymentAmounts = new HashMap<Law, SpcfMoney>();


    public AddCustomerTaxPaymentCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CustomerTaxPaymentDTO pCustomerTaxPaymentDTO) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCustomerTaxPaymentDTO = pCustomerTaxPaymentDTO;
    }


    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate Company Parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate DTO

        if (mCustomerTaxPaymentDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.MoneyMovementTransaction,
                    "AddCustomerTaxPayment",
                    "CustomerTaxPaymentDTO");
            return validationResult;
        }

        // Validate Company
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Validate Payment Template
        mPaymentTemplate = PaymentTemplate.findPaymentTemplate(mCustomerTaxPaymentDTO.getPaymentTemplateId());
        if (mPaymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateDoesNotExist(
                    EntityName.PaymentTemplate, mCustomerTaxPaymentDTO.getPaymentTemplateId(), mCustomerTaxPaymentDTO.getPaymentTemplateId());
        }

        // All laws have to belong to the same payment template
        // Validate Laws


        for (String lawId : mCustomerTaxPaymentDTO.getPaymentAmounts().keySet()) {
            Law law = PayrollServices.entityFinder.findById(Law.class, lawId);
            if (law == null) {
                validationResult.getMessages().LawDoesNotExist(EntityName.Law, lawId);
            } else {
                CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(
                        mSourceSystemCd, mSourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());

                if (companyAgency == null) {
                    validationResult.getMessages().CompanyAgencyNotFound(
                            EntityName.CompanyAgency, mSourceSystemCd.toString(), mSourceSystemCd.toString(), mSourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());
                }
                mLawPaymentAmounts.put(law, new SpcfMoney(mCustomerTaxPaymentDTO.getPaymentAmounts().get(lawId).toString()));
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<PayrollRun> process() {
        DateDTO paymentDate = getPaymentDate();

        // Create CompanyAdjustmentSubmission and Financial Transactions
        return createAdjustmentSubmission(paymentDate);
    }

    @NotNull
    protected DateDTO getPaymentDate() {
        DomainEntitySet<MoneyMovementTransaction> quarterMMTs = mPaymentTemplate.findQuarterEFTPSMMTs(mCompany, mCustomerTaxPaymentDTO.getYear(), mCustomerTaxPaymentDTO.getQuarter(), PaymentStatus.Created, PaymentStatus.OnHold);
        // Try to find a MMT that hasn't been executed yet for the quarter.
        // If it exists call the create adjustment process with the mmt periodenddate so it will create AgencyTaxDebit Transactions
        // If it doesn't exist try to find an executed mmt

        if (quarterMMTs.size() == 0) {
            quarterMMTs = mPaymentTemplate.findQuarterEFTPSMMTs(mCompany, mCustomerTaxPaymentDTO.getYear(), mCustomerTaxPaymentDTO.getQuarter(), PaymentStatus.Executed);
        }


        DateDTO paymentDate = new DateDTO(CalendarUtils.getFirstDayOfQuarter(mCustomerTaxPaymentDTO.getYear(), mCustomerTaxPaymentDTO.getQuarter()));
        if (quarterMMTs.size() > 0) {
            quarterMMTs = quarterMMTs.sort(MoneyMovementTransaction.PaymentPeriodEnd());
            paymentDate = new DateDTO(quarterMMTs.get(0).getPaymentPeriodEnd());
        }
        return paymentDate;
    }

    private ProcessResult<PayrollRun> createAdjustmentSubmission(DateDTO pPaymentDate) {
        // Create an adjustment and force the financial transactions to be added to the first MMT of the list
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        companyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
        companyAdjustmentSubmissionDTO.setIsVoid(false);

        SpcfMoney total = SpcfMoney.ZERO;
        for (Law law : mLawPaymentAmounts.keySet()) {
            total = new SpcfMoney(total.add(mLawPaymentAmounts.get(law)));
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
            SpcfDecimal money = new SpcfMoney(mLawPaymentAmounts.get(law)).negate();
            if(money.isGreaterThan(SpcfMoney.ZERO) && law.isCOBRA()) {
                money = money.negate();
            }
            liabilityAdjustmentDTO.setAmount((SpcfMoney) money);
            liabilityAdjustmentDTO.setEffectiveDate(pPaymentDate);
            liabilityAdjustmentDTO.setLawId(law.getLawId());
            liabilityAdjustmentDTO.setReconcilingAdjustment(false);
            liabilityAdjustmentDTO.setTaxableWages(SpcfMoney.ZERO);
            liabilityAdjustmentDTO.setTotalWages(SpcfMoney.ZERO);
            companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);

        }
        companyAdjustmentSubmissionDTO.setTotalAmount(total);
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(false);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(mCustomerTaxPaymentDTO.applyPayments());
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(mCustomerTaxPaymentDTO.applyPayments());
        liabilityAdjustmentOptionsDTO.setCreditCustomer(mCustomerTaxPaymentDTO.isImmediateCredit());

        ProcessResult<PayrollRun> processResult = new ProcessResult<PayrollRun>();
        AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore = new AddLiabilityAdjustmentsCore(mSourceSystemCd, mSourceCompanyId, null, companyAdjustmentSubmissionDTO, pPaymentDate, liabilityAdjustmentOptionsDTO, null);
        ProcessResult<CompanyAdjustmentSubmission> addLiabilityAdjustmentPR = addLiabilityAdjustmentsCore.execute();
        processResult.merge(addLiabilityAdjustmentPR);

        if(!validateTransactionTypeCode(addLiabilityAdjustmentsCore)){
            processResult.setSuccess(false);
            processResult.getMessages().ExceptionOccurred("Transaction can't be refunded because it's creating AgencyTaxOverpayment.");
        }

        if(!processResult.isSuccess()) {
            return processResult;
        }

        // Create HPDE MMT and transactions
        MoneyMovementTransaction hpdeMMT = createHPDETransactions(addLiabilityAdjustmentPR.getResult());

        // Create Event
        if (mCustomerTaxPaymentDTO.getMemo() != null) {
            createEvent(getCompany(), hpdeMMT.getId().toString(), addLiabilityAdjustmentsCore.getPayrollRun().getId().toString(),
                    getCustomerTaxPaymentDTO().getMemo());
        }
        CompanyEvent.createCustomerTaxPaymentCreatedEvent(mCompany, hpdeMMT.getId().toString(), null);

        processResult.setResult(addLiabilityAdjustmentsCore.getPayrollRun());
        return processResult;
    }

    protected boolean validateTransactionTypeCode(AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore) {
        return true;
    }

    protected void createEvent(Company pCompany, String pUniqueIdentifier, String pPayrollRunId, String pNoteText) {
        CompanyEvent.createManualLedgerEntryEvent(pCompany, pUniqueIdentifier, pNoteText);
    }

    protected MoneyMovementTransaction createHPDETransactions(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        SpcfMoney spcfMoney = pCompanyAdjustmentSubmission.getAmount();
        boolean isRefund = false;
        if(spcfMoney.isLessThan(SpcfMoney.ZERO)) {
            spcfMoney = (SpcfMoney) spcfMoney.negate();
            isRefund = true;
        }
        MoneyMovementTransaction mmt = MoneyMovementTransaction.createHPDEMoneyMovementTransaction(mCompany,
                null,
                mPaymentTemplate,
                CalendarUtils.getLastDayOfQuarter(mCustomerTaxPaymentDTO.getYear(), mCustomerTaxPaymentDTO.getQuarter()),
                mCustomerTaxPaymentDTO.getPaymentDate().toSpcfCalendar(),
                spcfMoney,
                isRefund,
                null,
                true);

        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(mCompany, mPaymentTemplate.getAgency().getAgencyId());

        for (Law law : mLawPaymentAmounts.keySet()) {

            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(companyAgency, law);
            TransactionTypeCode transactionTypeCode;
            SpcfMoney lawAmount = new SpcfMoney(mLawPaymentAmounts.get(law));
            if(lawAmount.isGreaterThan(SpcfMoney.ZERO)) {
                if(law.isCOBRA()) {
                    transactionTypeCode = TransactionTypeCode.AgencyHPDETaxRefund;
                } else {
                    transactionTypeCode = TransactionTypeCode.AgencyHPDETaxPayment;
                }
            } else {
                if(law.isCOBRA()) {
                    transactionTypeCode = TransactionTypeCode.AgencyHPDETaxPayment;
                } else {
                    transactionTypeCode = TransactionTypeCode.AgencyHPDETaxRefund;
                }
                lawAmount = (SpcfMoney) lawAmount.negate();
            }

            FinancialTransaction.createHPDETransaction(mCompany, null,
                    transactionTypeCode,
                    lawAmount,
                    mCustomerTaxPaymentDTO.getPaymentDate().toSpcfCalendar(),
                    companyLaw,
                    mmt,
                    null);
        }
        // Explicitly call this here since we are creating the MMT first followed by the FTs
        // the calls to this in the MMT setters will not see the FTs.
        MoneyMovementTransaction.recalculateATFPayments(mmt);

        return mmt;
    }


    protected void setCompany(Company mCompany) {
        this.mCompany = mCompany;
    }

    protected Company getCompany(){
        return mCompany;
    }

    protected void setCustomerTaxPaymentDTO(CustomerTaxPaymentDTO mCustomerTaxPaymentDTO) {
        this.mCustomerTaxPaymentDTO = mCustomerTaxPaymentDTO;
    }

    protected CustomerTaxPaymentDTO getCustomerTaxPaymentDTO() {
        return mCustomerTaxPaymentDTO;
    }
}
