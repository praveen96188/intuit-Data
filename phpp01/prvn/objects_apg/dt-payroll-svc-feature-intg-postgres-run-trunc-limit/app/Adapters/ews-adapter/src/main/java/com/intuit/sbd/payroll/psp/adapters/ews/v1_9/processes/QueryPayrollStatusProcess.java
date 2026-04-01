package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsAdapterConst;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.IJaxBManager;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.JaxBFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.ServiceSubStatusFactory;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Jeff Jones
 */
public class QueryPayrollStatusProcess {

    private GetPayrollInfoWSDTO mRequest;
    private Company mPspCompany;
    private String mPSID;
    private String mEIN;

    private SpcfMoney totalFees;
    private List<TransmissionWSDTO> transmissionsWSDTO;
    private List<TransmissionWSDTO> modTransmissionsWSDTO;

    private static final IJaxBManager requestManager;
    private static final IJaxBManager responseManager;
    private static final SpcfLogger logger;
    private static final int bufferSize = 10000;

    static {
        logger = PayrollServices.getLogger(QueryAccountProcess.class);
        try{
            requestManager = JaxBFactory.getManagerInstance(GetPayrollInfoWSDTO.class);
            responseManager = JaxBFactory.getManagerInstance(PayrollInfoWSDTO.class);

        } catch (Exception e) {
            logger.fatal("Error reading configuration files: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public QueryPayrollStatusProcess(GetPayrollInfoWSDTO pRequest) {
        this.mRequest = pRequest;
        this.mPSID = pRequest.getPayrollStatusWSDTO().getUserID();
        this.transmissionsWSDTO = new ArrayList<TransmissionWSDTO>();
        this.modTransmissionsWSDTO = new ArrayList<TransmissionWSDTO>();

        logger.info("Processing Query_Payroll_Status Request / PSID: " + this.mPSID );
    }

    public PayrollInfoWSDTO execute() {
        PayrollInfoWSDTO response = null;

        try {
            PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL);

            validate();
            response = process();

            PayrollServices.commitUnitOfWorkWithSecondary();
        } catch (EwsException e) {
            response = new PayrollInfoWSDTO();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new PayrollInfoWSDTO();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
            try {
                if (mPspCompany != null && mPspCompany.getDebugLogging()) {
                    LoggingUtils.logTransmissions(mRequest, response);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response;
    }

    private void validate() throws Exception {
        mRequest.validateGetPayrollStatus();

        GetPayrollStatusWSDTO getPayrollStatusWSDTO = mRequest.getPayrollStatusWSDTO();
        getPayrollStatusWSDTO.validateUserID();

        mPSID = mRequest.getPayrollStatusWSDTO().getUserID();
        mPspCompany = PspFactory.findCompany(mPSID);
        mEIN = mPspCompany.getFedTaxId();
    }

    private PayrollInfoWSDTO process() {
        PayrollInfoWSDTO response = new PayrollInfoWSDTO();

        response.setPayrollStatusWSDTO(createPayrollStatusWSDTO(mPspCompany));

        ErrorWSDTO errorWSDTO = new ErrorWSDTO();
        errorWSDTO.setCode("0");
        errorWSDTO.setDescription("Success");
        response.getPayrollStatusWSDTO().setErrorWSDTO(errorWSDTO);

        return response;
    }

    /**
     *
     * @param pCompany
     * @return
     */
    private PayrollStatusWSDTO createPayrollStatusWSDTO(Company pCompany) {
        PayrollStatusWSDTO payrollStatusWSDTO = new PayrollStatusWSDTO();

        UserRoleEnum roleId = mRequest.getPayrollStatusWSDTO().getRoleId();
        if (roleId == null) {
            roleId = UserRoleEnum.All;
        }

        SpcfCalendar pspTime  = PSPDate.getPSPTime().toLocal();
        CalendarUtils.addBusinessDays(pspTime, -7);

        payrollStatusWSDTO.setUserID(pCompany.getSourceCompanyId());
        payrollStatusWSDTO.setAccount(createAccountWSDTO(pCompany));

        createTransmissionsWSDTO(pCompany, roleId, pspTime.toLocal(), null);
        payrollStatusWSDTO.setTransmissionsWSDTO(transmissionsWSDTO);
        payrollStatusWSDTO.setModTransmissionsWSDTO(modTransmissionsWSDTO);

        payrollStatusWSDTO.setFailedTransmissionsWSDTO(createFailedTransmissionsWSDTO(pCompany, roleId, pspTime.toLocal(), null));
        payrollStatusWSDTO.setDDRejectionsWSDTO(createDDRejectionsWSDTO(pCompany, roleId, pspTime.toLocal(), null));

        return payrollStatusWSDTO;
    }

    /**
     *
     * @param pCompany
     * @return
     */
    private AccountWSDTO createAccountWSDTO(Company pCompany) {
        AccountWSDTO accountWSDTO = new AccountWSDTO();
        accountWSDTO.setStatus(getAccountStatusEnum(pCompany));
        return accountWSDTO;
    }

    /**
     *
     * @param pCompany
     * @param pRoleId
     * @param pFromDate
     * @param pToDate
     * @return
     */
    private List<TransmissionWSDTO> createTransmissionsWSDTO(Company pCompany,  UserRoleEnum pRoleId, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        List<TransmissionWSDTO> transmissions = new ArrayList<TransmissionWSDTO>();

        DomainEntitySet<TransmissionPayrollRun> transmissionPayrollRuns;
        DomainEntitySet<PayrollRun> payrollRuns =  PayrollRun.findPayrollRuns(pCompany, pFromDate, pToDate);
        if (payrollRuns == null || payrollRuns.size() == 0 ) {
            payrollRuns = new DomainEntitySet<PayrollRun>();
            transmissionPayrollRuns = PspFactory.getLastTransmissionWithPayroll(pCompany);
            if (transmissionPayrollRuns != null) {
                for (TransmissionPayrollRun transmissionPayrollRun : transmissionPayrollRuns) {
                    payrollRuns.add(transmissionPayrollRun.getPayrollRun());
                }
            }
        }

        for (PayrollRun payrollRun : payrollRuns) {

            if (!doesRoleMatchPayrollRunType(pRoleId, payrollRun)) {
                continue;
            }

            DomainEntitySet<FinancialTransaction> employerDdDebits = PspFactory.getFinancialTransactions
                    (pCompany, payrollRun, TransactionTypeCode.EmployerDdDebit);
            DomainEntitySet<FinancialTransaction> employerTaxDebits = PspFactory.getFinancialTransactions
                    (pCompany, payrollRun, TransactionTypeCode.EmployerTaxDebit);

            if (employerDdDebits.isNotEmpty() || employerTaxDebits.isNotEmpty()) {
                FinancialTransaction previousErDdDebit = null;
                FinancialTransaction previousErTaxDebit = null;

                //determine which list is larger
                if (employerDdDebits.size() >= employerTaxDebits.size()) {
                    previousErTaxDebit = employerTaxDebits.getFirst();

                    FinancialTransaction nextFt = null;
                    LinkedHashMap<FinancialTransaction, FinancialTransaction> ftMap = new LinkedHashMap<FinancialTransaction, FinancialTransaction>();
                    for (FinancialTransaction ddDebit : employerDdDebits) {
                        FinancialTransaction matchedTaxDebit = null;
                        for (FinancialTransaction taxDebit : employerTaxDebits) {
                            if (previousErTaxDebit == taxDebit) {
                                nextFt = null;
                            } else {
                                if (nextFt == null) {
                                    nextFt = taxDebit;
                                }
                            }

                            if (CalendarUtils.getDifferenceInSeconds(ddDebit.getCreatedDate(), taxDebit.getCreatedDate()) <= 15) {
                                matchedTaxDebit = taxDebit;
                                break;
                            }
                        }

                        if (matchedTaxDebit == null) {
                            if (nextFt != null && ddDebit.getCreatedDate().before(previousErDdDebit.getCreatedDate())) {
                                ftMap.put(ddDebit, nextFt);
                            } else {
                                ftMap.put(ddDebit, previousErTaxDebit);
                            }
                        } else {
                            ftMap.put(ddDebit, matchedTaxDebit);
                            previousErTaxDebit = matchedTaxDebit;
                        }
                    }

                    for (FinancialTransaction ddDebit : ftMap.keySet()) {
                        TransmissionWSDTO transmissionWSDTO = createTransmissionWSDTO(ddDebit, ftMap.get(ddDebit));

                        if (transmissionWSDTO == null) {
                            continue;
                        }

                        if (transmissionWSDTO.getOffloadDate().equals("Modified")) {
                            modTransmissionsWSDTO.add(transmissionWSDTO);
                        } else {
                            transmissionsWSDTO.add(transmissionWSDTO);
                        }
                    }
                } else {
                    previousErDdDebit = employerDdDebits.getFirst();

                    FinancialTransaction nextFt = null;
                    LinkedHashMap<FinancialTransaction, FinancialTransaction> ftMap = new LinkedHashMap<FinancialTransaction, FinancialTransaction>();
                    for (FinancialTransaction taxDebit : employerTaxDebits) {
                        FinancialTransaction matchedDdDebit = null;
                        for (FinancialTransaction ddDebit : employerDdDebits) {
                            if (previousErDdDebit == ddDebit) {
                                nextFt = null;
                            } else {
                                if (nextFt == null) {
                                    nextFt = ddDebit;
                                }
                            }

                            if (CalendarUtils.getDifferenceInSeconds(ddDebit.getCreatedDate(), taxDebit.getCreatedDate()) <= 15) {
                                matchedDdDebit = ddDebit;
                                break;
                            }
                        }

                        if (matchedDdDebit == null) {
                            if (nextFt != null && taxDebit.getCreatedDate().before(previousErDdDebit.getCreatedDate())) {
                                ftMap.put(taxDebit, nextFt);
                            } else {
                                ftMap.put(taxDebit, previousErDdDebit);
                            }

                        } else {
                            ftMap.put(taxDebit, matchedDdDebit);
                            previousErDdDebit = matchedDdDebit;
                        }
                    }

                    for (FinancialTransaction taxDebit : ftMap.keySet()) {
                        TransmissionWSDTO transmissionWSDTO = createTransmissionWSDTO(ftMap.get(taxDebit), taxDebit);

                        if (transmissionWSDTO == null) {
                            continue;
                        }

                        if (transmissionWSDTO.getOffloadDate().equals("Modified")) {
                            modTransmissionsWSDTO.add(transmissionWSDTO);
                        } else {
                            transmissionsWSDTO.add(transmissionWSDTO);
                        }
                    }
                }

                //If the customer cancels all checks there could still be pending Fees.
                if (transmissionsWSDTO.isEmpty() && payrollRun.getBillingDetailCollection().isNotEmpty()) {
                    TransactionTypeCode[] transactionTypeCodes = new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit};
                    TransactionStateCode[] transactionStateCodes = new TransactionStateCode[]{TransactionStateCode.Completed,
                                                                                              TransactionStateCode.Created,
                                                                                              TransactionStateCode.Executed};

                    FinancialTransaction erFeeDebit = FinancialTransaction.findFinancialTransactions(payrollRun,
                                                                                                     transactionTypeCodes,
                                                                                                     transactionStateCodes).getFirst();
                    if (erFeeDebit != null) {
                        TransmissionWSDTO transmissionWSDTO = createTransmissionWSDTO(erFeeDebit);
                        if (transmissionWSDTO != null) {
                            transmissionsWSDTO.add(transmissionWSDTO);
                        }
                    }
                }
            }
        }

        return transmissions;
    }

    private boolean doesRoleMatchPayrollRunType(UserRoleEnum pRoleId, PayrollRun pPayrollRun) {
        boolean response = true;
        switch (pRoleId) {
            case Payroll:
                if (PayrollType.BillPayment.equals(pPayrollRun.getPayrollRunType())) {
                    response = false;
                }
                break;
            case Vendor:
                if (!PayrollType.BillPayment.equals(pPayrollRun.getPayrollRunType())) {
                    response = false;
                }
        }
        return response;
    }

    private boolean doesRoleMatchTransmissionType(UserRoleEnum pRoleId, SourceSystemTransmission sourceSystemTransmission) {
        boolean response = true;
        switch (pRoleId) {
            case Payroll:
                if (TransmissionType.WSBillPayQueryPaymentStatus.equals(sourceSystemTransmission.getType()) ||
                        TransmissionType.WSBillPaySendPaymentsToPayees.equals(sourceSystemTransmission.getType()) ||
                        TransmissionType.WSBillPayVoidPayments.equals(sourceSystemTransmission.getType())) {
                    response = false;
                }
                break;
            case Vendor:
                if (!TransmissionType.WSBillPayQueryPaymentStatus.equals(sourceSystemTransmission.getType()) &&
                        !TransmissionType.WSBillPaySendPaymentsToPayees.equals(sourceSystemTransmission.getType()) &&
                        !TransmissionType.WSBillPayVoidPayments.equals(sourceSystemTransmission.getType())) {
                    response = false;
                }
        }
        return response;
    }

    /**
     *
     * @param pCompany
     * @param pRoleId
     * @param pFromDate
     * @param pToDate
     * @return
     */
    private List<FailedTransmissionWSDTO> createFailedTransmissionsWSDTO(Company pCompany, UserRoleEnum pRoleId, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        List<FailedTransmissionWSDTO> failedTransmissions = new ArrayList<FailedTransmissionWSDTO>();
        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions =
                SourceSystemTransmission.findFailedCompanyTransmissions(pCompany, pFromDate, pToDate);
        for (SourceSystemTransmission sourceSystemTransmission : sourceSystemTransmissions) {
            if (!doesRoleMatchTransmissionType(pRoleId, sourceSystemTransmission)) {
                continue;
            }

            FailedTransmissionWSDTO failedTransmissionWSDTO = new FailedTransmissionWSDTO();
            failedTransmissionWSDTO.setDate(formatDate(sourceSystemTransmission.getInitializeDateTime()));
            failedTransmissionWSDTO.setTime(formatTime(sourceSystemTransmission.getInitializeDateTime()));

            DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEvent.findCompanyEventDetails
                    (pCompany, EventTypeCode.TransmissionError, EventDetailTypeCode.TransmissionId,
                            sourceSystemTransmission.getTransmissionIdentifier());
            failedTransmissionWSDTO.setDescription(PspFactory.getFailedTransmissionMessage(companyEventDetails));
            failedTransmissions.add(failedTransmissionWSDTO);
        }
        return failedTransmissions;
    }

    /**
     *
     * @param pCompany
     * @param pRoleId
     * @param pFromDate
     * @param pToDate
     * @return
     */
    private List<DDRejectionWSDTO> createDDRejectionsWSDTO(Company pCompany, UserRoleEnum pRoleId, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        List<DDRejectionWSDTO> DDRejections = new ArrayList<DDRejectionWSDTO>();

        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        TransactionReturn.getTransactionReturnCollection
                (SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), null, null, null,
                        pFromDate, pToDate, 0, 0, transactionReturns);
        for (TransactionReturn transactionReturn : transactionReturns){
            DomainEntitySet<FinancialTransaction> financialTransactions =
                    TransactionReturn.findFinancialTransaction(transactionReturn);
            if (transactionReturn.isRejectReturn()) {
                for (FinancialTransaction financialTransaction : financialTransactions) {
                    if (financialTransaction.getTransactionType().getTransactionTypeCd()
                            .equals(TransactionTypeCode.EmployeeDdCredit)) {

                        if (!doesRoleMatchPayrollRunType(pRoleId, financialTransaction.getPayrollRun())) {
                            continue;
                        }

                        DDRejectionWSDTO ddRejectionWSDTO = new DDRejectionWSDTO();
                        ddRejectionWSDTO.setCheckDate(formatDate(financialTransaction.getSettlementDate()));
                        ddRejectionWSDTO.setFullName(financialTransaction.getNameOnCreditBankAccount());

                        BigDecimal txnAmt = SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount());
                        ddRejectionWSDTO.setCheckAmt(txnAmt);

                        BankAccount bankAccount = financialTransaction.getCreditBankAccount();
                        ddRejectionWSDTO.setBankAccountNumber(bankAccount.getAccountNumber());
                        ddRejectionWSDTO.setRoutingNumber(bankAccount.getRoutingNumber());

                        ddRejectionWSDTO.setDescription(ReturnReasonDesc.findReturnDescription(transactionReturn.getBankReturnCd()));

                        DDRejections.add(ddRejectionWSDTO);
                    }
                }
            }
        }
        return DDRejections;
    }

    /**
     *
     * @param pErDdDebitFt
     * @param pErTaxDebitFt
     * @return
     */
    private TransmissionWSDTO createTransmissionWSDTO(FinancialTransaction pErDdDebitFt, FinancialTransaction pErTaxDebitFt) {
        TransmissionWSDTO transmissionWSDTO = new TransmissionWSDTO();

        BigDecimal totalTax = new BigDecimal(0.00);
        BigDecimal totalDD = new BigDecimal(0.00);
        BigDecimal totalFee;
        BigDecimal grandTotal;

        FinancialTransaction financialTransaction;
        if (pErDdDebitFt != null && pErTaxDebitFt != null) {
            //get the newest financial transaction.
            if (pErDdDebitFt.getCreatedDate().before(pErTaxDebitFt.getCreatedDate())) {
                financialTransaction = pErTaxDebitFt;
            } else {
                financialTransaction = pErDdDebitFt;
            }
        } else {
            financialTransaction = pErDdDebitFt != null ? pErDdDebitFt : pErTaxDebitFt;
        }

        transmissionWSDTO.setDate(formatDate(financialTransaction.getCreatedDate()));
        transmissionWSDTO.setTime(formatTime(financialTransaction.getCreatedDate()));
        transmissionWSDTO.setCheckDate(formatDate(financialTransaction.getPayrollRun().getPaycheckDate()));

        PayrollType payrollType = financialTransaction.getPayrollRun().getPayrollRunType();
        if (payrollType != null) {
            transmissionWSDTO.setPayrollType(getPayrollType(payrollType));
        }

        switch (financialTransaction.getCurrentTransactionState().getTransactionStateCd()) {
            case Created:
                transmissionWSDTO.setOffloadDate("Pending");
                break;
            case Cancelled:
            case Voided:
                transmissionWSDTO.setOffloadDate("Modified");
                break;
            default:
                transmissionWSDTO.setOffloadDate(formatDate(financialTransaction.getSettlementDate()));
        }

        transmissionWSDTO.setFees(createTransmissionFeesWSDTO(financialTransaction.getPayrollRun()));
        totalFee = SpcfUtils.convertToBigDecimal(totalFees);
        grandTotal = totalFee;

        if (pErDdDebitFt != null) {
            totalDD = SpcfUtils.convertToBigDecimal(pErDdDebitFt.getFinancialTransactionAmount());
            grandTotal = grandTotal.add(totalDD);
        }

        if (pErTaxDebitFt != null) {
            totalTax = SpcfUtils.convertToBigDecimal(pErTaxDebitFt.getFinancialTransactionAmount());
            grandTotal = grandTotal.add(totalTax);
        }

        transmissionWSDTO.setTotalTaxes(totalTax);
        transmissionWSDTO.setTotalDD(totalDD);
        transmissionWSDTO.setTotalFees(totalFee);
        transmissionWSDTO.setTotal(grandTotal);

        if (PayrollType.Adjustment.equals(payrollType)) {
            if (areTotalsGreaterThanZero(transmissionWSDTO)) {
                return null;
            }
        }

        return transmissionWSDTO;
    }

    /**
     *
     * @param pErFeeDebit
     * @return
     */
    private TransmissionWSDTO createTransmissionWSDTO(FinancialTransaction pErFeeDebit) {
        TransmissionWSDTO transmissionWSDTO = new TransmissionWSDTO();

        BigDecimal totalTax = new BigDecimal(0.00);
        BigDecimal totalDD = new BigDecimal(0.00);
        BigDecimal totalFee;
        BigDecimal grandTotal;

        transmissionWSDTO.setDate(formatDate(pErFeeDebit.getCreatedDate()));
        transmissionWSDTO.setTime(formatTime(pErFeeDebit.getCreatedDate()));
        transmissionWSDTO.setCheckDate(formatDate(pErFeeDebit.getPayrollRun().getPaycheckDate()));

        PayrollType payrollType = pErFeeDebit.getPayrollRun().getPayrollRunType();
        if (payrollType != null) {
            transmissionWSDTO.setPayrollType(getPayrollType(payrollType));
        }

        switch (pErFeeDebit.getCurrentTransactionState().getTransactionStateCd()) {
            case Created:
                transmissionWSDTO.setOffloadDate("Pending");
                break;
            case Cancelled:
            case Voided:
                transmissionWSDTO.setOffloadDate("Modified");
                break;
            default:
                transmissionWSDTO.setOffloadDate(formatDate(pErFeeDebit.getSettlementDate()));
        }

        transmissionWSDTO.setFees(createTransmissionFeesWSDTO(pErFeeDebit.getPayrollRun()));
        totalFee = SpcfUtils.convertToBigDecimal(totalFees);
        grandTotal = totalFee;

        transmissionWSDTO.setTotalTaxes(totalTax);
        transmissionWSDTO.setTotalDD(totalDD);
        transmissionWSDTO.setTotalFees(totalFee);
        transmissionWSDTO.setTotal(grandTotal);

        if (PayrollType.Adjustment.equals(payrollType)) {
            if (areTotalsGreaterThanZero(transmissionWSDTO)) {
                return null;
            }
        }

        return transmissionWSDTO;
    }

    /**
     *
     * @param pSpcfCalendar
     * @return
     */
    private String formatDate(SpcfCalendar pSpcfCalendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = CalendarUtils.convertToCalendar(pSpcfCalendar.toLocal());
        return sdf.format(calendar.getTime());
    }

    private String getPayrollType(PayrollType pPayrollType) {
        String response = null;

        if (pPayrollType == null) {
            return response;
        }

        switch (pPayrollType) {
            case Adjustment:
            case CloudOnly:
            case Regular:
                response = "Regular";
                break;
            case BillPayment:
                response = "BillPayment";
                break;
        }

        return response;
    }

    private boolean areTotalsGreaterThanZero(TransmissionWSDTO pTransmissionWSDTO) {
        if (pTransmissionWSDTO != null) {
            if (pTransmissionWSDTO.getTotal().compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
            if (pTransmissionWSDTO.getTotalDD().compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
            if (pTransmissionWSDTO.getTotalFees().compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
            if (pTransmissionWSDTO.getTotalTaxes().compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param pSpcfCalendar
     * @return
     */
    private String formatTime(SpcfCalendar pSpcfCalendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        Calendar calendar = CalendarUtils.convertToCalendar(pSpcfCalendar.toLocal());
        return sdf.format(calendar.getTime());
    }

    /**
     *
     * @param pPayrollRun
     * @return
     */
    private List<TransmissionFeeWSDTO> createTransmissionFeesWSDTO(PayrollRun pPayrollRun) {
        List<TransmissionFeeWSDTO> transmissionFeesWSDTO = new ArrayList<TransmissionFeeWSDTO>();

        totalFees = new SpcfMoney();
        SpcfMoney salesTax = new SpcfMoney();
        TransmissionFeeWSDTO transmissionFeeWSDTO;
        for (BillingDetail billingDetail : pPayrollRun.getBillingDetailCollection().sort(BillingDetail.ItemName())) {
            SpcfDecimal itemTotal = billingDetail.getItemTotal();
            SpcfDecimal taxAmount = billingDetail.getTaxAmount();
            SpcfMoney itemSubTotal = new SpcfMoney(itemTotal.subtract(taxAmount));
            switch (billingDetail.getOfferingServiceChargeType()) {
                case PerPaycheck:
                case DirectDepositFee:
                case PerPayment:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(EwsMessages.perCheckFeeMessage
                            (billingDetail.getQuantity(), billingDetail.getUnitPrice()).getMessage());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
                case PerTransmission:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(EwsMessages.transmissionFeeMessage().getMessage());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
                case ReversalFee:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(EwsMessages.reversalFeeMessage().getMessage());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
                case DebitReturnFee:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(EwsMessages.nsfFeeMessage().getMessage());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
                case PaymentArrangementFee:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(EwsMessages.paymentArrangementFeeMessage().getMessage());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
                case BackdatedPayroll:
                case ExtraStateFee:
                case MonthlyFee:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(billingDetail.getItemName());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
                case EmployeesPaid:
                    transmissionFeeWSDTO = new TransmissionFeeWSDTO();
                    transmissionFeeWSDTO.setName(EwsMessages.employeesPaidFeeMessage
                            (billingDetail.getQuantity(), billingDetail.getUnitPrice()).getMessage());
                    transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(itemSubTotal));
                    transmissionFeesWSDTO.add(transmissionFeeWSDTO);
                    totalFees = new SpcfMoney(totalFees.add(itemSubTotal));
                    break;
            }
            salesTax = new SpcfMoney(salesTax.add(billingDetail.getTaxAmount())) ;
        }

        if (transmissionFeesWSDTO.size() > 0) {
            transmissionFeeWSDTO = new TransmissionFeeWSDTO();
            transmissionFeeWSDTO.setName(EwsMessages.salesTaxMessage().getMessage());
            transmissionFeeWSDTO.setAmt(SpcfUtils.convertToBigDecimal(salesTax));
            transmissionFeesWSDTO.add(transmissionFeeWSDTO);
            totalFees = new SpcfMoney(totalFees.add(salesTax));
        }

        return transmissionFeesWSDTO;
    }

    public static AccountStatusEnum getAccountStatusEnum(Company pCompany) {
        ServiceSubStatusCode serviceSubStatusCode =
                ServiceSubStatusFactory.getServiceSubStatusCodeBySeverity(pCompany);

        if (serviceSubStatusCode == null) {
            return null;
        }
        
        switch (serviceSubStatusCode) {
            case PendingBankVerification:
            case PendingPinCreation:
            case PendingBalanceFile:
            case PendingFirstPayroll:
            case PendingSetup:
            case ActiveCurrent:
            case ActiveSeasonal:
                return AccountStatusEnum.Active;
            case AchRejectR1R9:
            case AchRejectOther:
            case DirectDepositLimit:
            case IntuitCollections:
            case RiskCollections:
            case RiskAssessment:
            case Fraud:
            case AMLHold:
            case BillPaymentLimit:
            case FraudReview:
            case SuspendedDirectDeposit:
            case PendingTermination:
            case MissingPaperwork:
            case AuditCorrections:
            case PendingPrefundingWire:
                return AccountStatusEnum.Hold;
            case Cancelled:
                return AccountStatusEnum.Cancelled;
            case Terminated:
                return AccountStatusEnum.Terminated;
            default:
                throw new RuntimeException("Unexpected ServiceSubStatusCode with value " + serviceSubStatusCode.toString());
        }
    }

    protected void processEwsException(EwsException pEwsException, PayrollInfoWSDTO pResponse) {
        logger.info("PSID: " + mPSID + " ErrorCode: " + pEwsException.getCode(), pEwsException);
        EwsFactory.updatePayrollInfoWSDTO(pEwsException.getEwsMessage(), pResponse);
    }

    protected void processThrowable(Throwable pThrowable, PayrollInfoWSDTO pResponse) {
        logger.error("PSID: " + mPSID  + " " + pThrowable.getMessage(), pThrowable);
        EwsFactory.updatePayrollInfoWSDTO(EwsMessages.systemError(), pResponse);
    }
}
