package com.intuit.sbd.payroll.psp.processes.fundingmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

public class NextDayFundingMigrationCore extends Process {

    private final String sourceCompanyId;
    private final List<SpcfUniqueId> payrollRunSeqList;
    private Map<SpcfUniqueId, FinancialTransaction> employerDebitFtMap;
    private Map<SpcfUniqueId, DomainEntitySet<FinancialTransaction>> employeeCreditFtMap;
    private List<PayrollRun> payrollRunList;

    private static final SpcfLogger LOGGER = Application.getLogger(NextDayFundingMigrationCore.class);

    public NextDayFundingMigrationCore(String sourceCompanyId, List<SpcfUniqueId> payrollrunSeqList) {
        this.sourceCompanyId = sourceCompanyId;
        this.payrollRunSeqList = payrollrunSeqList;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if (StringUtils.isBlank(sourceCompanyId)) {
            validationResult.getMessages().BadProcessArgument("SourceCompanyId");
            return validationResult;
        }

        if (CollectionUtils.isEmpty(payrollRunSeqList)) {
            validationResult.getMessages().BadProcessArgument("PayrollRunSeqList");
            return validationResult;
        }

        payrollRunList = new ArrayList<PayrollRun>();
        employerDebitFtMap = new HashMap<SpcfUniqueId, FinancialTransaction>();
        employeeCreditFtMap = new HashMap<SpcfUniqueId, DomainEntitySet<FinancialTransaction>>();


        for (SpcfUniqueId payrollRunSeq : payrollRunSeqList) {
            if (Objects.isNull(payrollRunSeq)) {
                validationResult.getMessages().BadProcessArgument("PayrollRunSeq");
                return validationResult;
            }

            // Validate Pending Payroll Run is Present for the SourceCompanyId
            PayrollRun payrollRun = PayrollRun.findPayrollRunByIdStatusAndSourceCompanyId(sourceCompanyId, payrollRunSeq,
                    PayrollStatus.Pending);

            if (Objects.isNull(payrollRun)) {
                validationResult.getMessages().NoPendingPayroll(sourceCompanyId, payrollRunSeq.toString());
                return validationResult;
            }

            SpcfDecimal zero = SpcfDecimalImpl.createInstance(0);

            // Validate if the payroll is DD
            if (!payrollRun.getPayrollDirectDepositAmount().isGreaterThan(zero)) {
                validationResult.getMessages().NonDDPayroll(sourceCompanyId, payrollRunSeq.toString());
                return validationResult;
            }

            // Validate Payroll is Two Day
            if (!payrollRun.getFundingModel().equals(FundingModel.Codes.TWO_DAY)) {
                validationResult.getMessages().NotTwoDayPayroll(sourceCompanyId, payrollRunSeq.toString());
                return validationResult;
            }

            List<TransactionStateCode> financialTransactionStateList = new ArrayList<TransactionStateCode>();
            financialTransactionStateList.add(TransactionStateCode.Created);

            DomainEntitySet<FinancialTransaction> employerDebitFts = payrollRun.getTransactionByTypeAndStatus(
                    TransactionTypeCode.EmployerDdDebit, financialTransactionStateList, SettlementType.ACH);

            DomainEntitySet<FinancialTransaction> employeeCreditFts = payrollRun.getTransactionByTypeAndStatus(TransactionTypeCode.EmployeeDdCredit,
                    financialTransactionStateList, SettlementType.ACH);


            // Validate if ER/EE should be present. Exclude fee only payrolls and other
            // types of payrolls.
            if (CollectionUtils.isEmpty(employerDebitFts) || CollectionUtils.isEmpty(employeeCreditFts)) {
                validationResult.getMessages().NoPendingEmployerEmployeeTransaction(sourceCompanyId,
                        payrollRunSeq.toString());
                return validationResult;
            }

            // Validate Settlement Date
            FinancialTransaction employerDebitFt = employerDebitFts.get(0);

            SpcfCalendar employerDebitSettlementDate = employerDebitFt.getSettlementDate();
            SpcfCalendar employeeCreditSettlementDate = employeeCreditFts.get(0).getSettlementDate();

            SpcfCalendar erTxOnlyDate = new SpcfCalendarImpl(employerDebitSettlementDate.getYear(),
                    employerDebitSettlementDate.getMonth(), employerDebitSettlementDate.getDay(),
                    SpcfTimeZone.getLocalTimeZone());
            SpcfCalendar eeTxOnlyDate = new SpcfCalendarImpl(employeeCreditSettlementDate.getYear(),
                    employeeCreditSettlementDate.getMonth(), employeeCreditSettlementDate.getDay(),
                    SpcfTimeZone.getLocalTimeZone());

            CalendarUtils.addBusinessDays(eeTxOnlyDate, -1);

            int compareDates = erTxOnlyDate.compareTo(eeTxOnlyDate);
            if (compareDates != 0) {
                validationResult.getMessages().UnexpectedErEeSettlementDates(sourceCompanyId,
                        payrollRunSeq.toString());
                return validationResult;
            }

            payrollRunList.add(payrollRun);
            employerDebitFtMap.put(payrollRunSeq, employerDebitFt);
            employeeCreditFtMap.put(payrollRunSeq, employeeCreditFts);
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {

        ProcessResult processResult = new ProcessResult();
        for (PayrollRun payrollRun : payrollRunList) {
            SpcfUniqueId payrollRunSeq = payrollRun.getId();
            FinancialTransaction employerDebitFt = employerDebitFtMap.get(payrollRunSeq);
            LOGGER.info(String.format("Event=MigrateTwoDayPayroll,Status=Start,SourceCompanyId=%s,PayrollRunSeq=%s",sourceCompanyId, payrollRunSeq.toString()));
            try {
                SpcfCalendar employerDebitSettlementDate = employerDebitFt.getSettlementDate().copy();
                updateSettlementDate(payrollRun, employerDebitSettlementDate);
                migratePayrollRun(payrollRun);
                LOGGER.info(String.format("Event=MigrateTwoDayPayroll,Status=Done,SourceCompanyId=%s,PayrollRunSeq=%s",sourceCompanyId, payrollRunSeq.toString()));
            } catch (Exception e) {
                LOGGER.error(String.format("Event=MigrateTwoDayPayroll,Status=Failed,SourceCompanyId=%s,PayrollRunSeq=%s",sourceCompanyId, payrollRunSeq.toString()), e);
                processResult.getMessages().ExceptionOccurred("Unable to Migration Payroll. Reason=" + e);
            }
        }
        return processResult;
    }

    private void updateSettlementDate(PayrollRun payrollRun, SpcfCalendar newSettlementDate) {
        SpcfUniqueId payrollrunSeq = payrollRun.getId();
        DomainEntitySet<FinancialTransaction> employeeCreditFts = employeeCreditFtMap.get(payrollrunSeq);
        LOGGER.info(String.format("Event=MigrateTwoDayPayroll,SubEvent=UpdateSettlementdate,Status=Start,SourceCompanyId=%s,PayrollRunSeq=%s",sourceCompanyId, payrollrunSeq.toString()));
        for (FinancialTransaction employeeFt : employeeCreditFts) {
            employeeFt.updateACHSettlementDate(newSettlementDate);
        }
        LOGGER.info(String.format("Event=MigrateTwoDayPayroll,SubEvent=UpdateSettlementdate,Status=Done,SourceCompanyId=%s,PayrollRunSeq=%s,FTCount=%s",sourceCompanyId, payrollrunSeq.toString(), employeeCreditFts.size()));
    }

    private void migratePayrollRun(PayrollRun payrollRun) {
        SpcfUniqueId payrollrunSeq = payrollRun.getId();
        LOGGER.info(String.format("Event=MigrateTwoDayPayroll,SubEvent=MigratePayrollRun,Status=Start,SourceCompanyId=%s,PayrollRunSeq=%s",sourceCompanyId, payrollrunSeq.toString()));
        payrollRun.setFundingModel(FundingModel.Codes.ONE_DAY);
        LOGGER.info(String.format("Event=MigrateTwoDayPayroll,SubEvent=MigratePayrollRun,Status=Done,SourceCompanyId=%s,PayrollRunSeq=%s",sourceCompanyId, payrollrunSeq.toString()));
    }
}
