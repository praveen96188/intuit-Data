/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/AdministrationAdapter.java#3 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.FluxUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbg.psp.common.gateway.JSSGateway;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.ScrollableResults;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.stream.Collectors;
import com.intuit.sbd.payroll.psp.domain.SystemParameter.Code;


/**
 * SourcePayrollParameterAdapter
 *
 * @author Joe Warmelink
 */
public class AdministrationAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(AdministrationAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private static String SCHEDULE_DATE_FORMAT = "yyyy-MM-dd'T'HH:MM:ss";
    private static final String PSP_SYSTEM_PARAMETER = "PSP_SYSTEM_PARAMETER";
    private static String[] RESTRICTED_SYSTEM_PARAMETER_VALUES = new String[]{Code.PSP_DATE_OFFSET.toString(),
            Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET.toString(),
            Code.JPMC_ENABLE_ENCRYPTION.toString(), Code.PSP_DATE_TIMEZONE_OFFSET.toString()};

    public AdministrationAdapter() {
    }

    @FlexMethod
    @Operation(operationIds = OperationId.UploadToGems)
    public void postToGems() throws Throwable {
        ProcessResult processResult;
        try {
            PayrollServices.beginUnitOfWork();

            processResult = PayrollServices.batchJobManager.monthlyGemsUpload();
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error Posting to GEMS.", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error Posting to GEMS.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.UploadToGems)
    public ArrayList<SAPGemsMonthlyBalance> getGemsMonthlyBalances() throws Throwable {
        ArrayList<SAPGemsMonthlyBalance> sapBalances = new ArrayList<SAPGemsMonthlyBalance>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            //Get the previous month from the PSPDate
            Calendar currentDate = CalendarUtils.convertToCalendar(PSPDate.getPSPTime());
            currentDate.add(Calendar.MONTH, -1);
            String reportingPeriod = new SimpleDateFormat("yyyyMM").format(currentDate.getTime());

            //Get the GemsMonthlyBalance list for the reporting period.
            DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalanceList = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ReportingPeriod().equalTo(reportingPeriod));

            for (GemsMonthlyBalance balance : gemsMonthlyBalanceList) {
                SAPGemsMonthlyBalance sapBalance = new SAPGemsMonthlyBalance();
                sapBalance.setAccount(balance.getGemsLedgerPostingRule().getAccount());
                sapBalance.setCompany(balance.getGemsLedgerPostingRule().getCompany());
                sapBalance.setDepartment(balance.getGemsLedgerPostingRule().getDepartment());
                sapBalance.setGroupCode(balance.getGemsLedgerPostingRule().getGroupCode());
                sapBalance.setInterCompany(balance.getGemsLedgerPostingRule().getInterCompany());
                sapBalance.setReportedBalance(SAPTranslator.getDoubleFromSpcfMoney(balance.getPeriodBalance()));
                sapBalance.setUploadStatus(balance.getGemsUploadBatch().getUploadStatus().toString());
                sapBalances.add(sapBalance);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding monthly GEMS balances.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapBalances;

    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewSystemParameters)
    public ArrayList<SAPSystemParameter> getAllSystemParameters() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Expression<SystemParameter> expr = new Query<SystemParameter>().Where(SystemParameter.IsSecured().equalTo(false)).OrderBy(SystemParameter.SystemParameterCd());


            DomainEntitySet<SystemParameter> systemParameters =
                    PayrollServices.entityFinder.find(SystemParameter.class, expr);

            ArrayList<SAPSystemParameter> sapSystemParameters = new ArrayList<SAPSystemParameter>();
            for (SystemParameter systemParameter : systemParameters) {
                SAPSystemParameter sapSystemParameter = AdministrationTranslator.getSAPSystemParameter(systemParameter);
                sapSystemParameters.add(sapSystemParameter);
            }

            return sapSystemParameters;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding system parameters.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewSystemParameters)
    public void saveSystemParameters(ArrayList<SAPSystemParameter> pSystemParameters) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            for (SAPSystemParameter systemParameter : pSystemParameters) {
                PayrollServices.systemParameterManager.updateSystemParameterValue(systemParameter.getCode(), systemParameter.getValue());
            }

            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating system parameters.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SettingUpdate)
    public SAPDirectDepositLimitSettings getDirectDepositLimitSettings(String pLimitRuleId) throws Throwable {
        SAPDirectDepositLimitSettings settings = new SAPDirectDepositLimitSettings();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            LimitRule limitRule = Application.findById(LimitRule.class, SpcfUniqueId.createInstance(pLimitRuleId));
            for (LimitValue limitValue : limitRule.getLimitValueCollection()) {
                switch (limitValue.getName()) {
                    case DefaultCompanyLimit:
                        settings.setDefaultDDCompanyLimit(limitValue.getValue());
                        break;
                    case DefaultEmployeeLimit:
                        settings.setDefaultDDEmployeeLimit(limitValue.getValue());
                        break;
                    case CompanyLimitDuration:
                        settings.setDDCompanyLimitDuration(limitValue.getValue());
                        break;
                    case EmployeeLimitDuration:
                        settings.setDDEmployeeLimitDuration(limitValue.getValue());
                        break;
                    case MaxCompanyLimitDefault:
                        settings.setMaxDDCompanyLimitDefault(limitValue.getValue());
                        break;
                    case ConsecutiveLimitViolationLimit:
                        settings.setConsecutiveLimitViolationLimit(limitValue.getValue());
                        break;
                    case CompanyBankAccountVerificationAttemptLimit:
                        settings.setCompanyBankAccountVerificationAttemptLimit(limitValue.getValue());
                        break;
                    case CompanyBankAccountDurationLimitForVerification:
                        settings.setCompanyBankAccountDurationLimitForVerification(limitValue.getValue());
                        break;
                    case MinimumNonSuspectPayrollAmount:
                        settings.setMinimumNonSuspectPayrollAmount(limitValue.getValue());
                        break;
                }
            }
            settings.setAutoLimitIncreaseTiers(SourcePayrollParameterTranslator.getSAPAutoLimitIncreaseTiers(limitRule.getAutoLimitIncreaseTiers()));
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding direct deposit settings.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return settings;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SettingUpdate)
    public SAPFraudSettings getFraudSettings(String pFraudLimitId) throws Throwable {
        SAPFraudSettings settings = new SAPFraudSettings();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            FraudRule fraudRule = Application.findById(FraudRule.class, SpcfUniqueId.createInstance(pFraudLimitId));

            for (FraudValue fraudValue : fraudRule.getFraudValueCollection()) {
                switch (fraudValue.getName()) {
                    case FraudEEPaidMax:
                        settings.setFraudEEPaidMax(fraudValue.getValue());
                        break;
                    case FraudEEPaidMaxXPayrolls:
                        settings.setFraudEEPaidMaxXPayrolls(fraudValue.getValue());
                        break;
                    case FraudEEAcctUpdateMax:
                        settings.setFraudEEAcctUpdateMax(fraudValue.getValue());
                        break;
                    case FraudEEAcctUpdateXDays:
                        settings.setFraudEEAcctUpdateXDays(fraudValue.getValue());
                        break;
                    case FraudEERoundPaidXPayrolls:
                        settings.setFraudEERoundPaidXPayrolls(fraudValue.getValue());
                        break;
                    case FraudEERoundPaidXAmount:
                        settings.setFraudEERoundPaidXAmount(fraudValue.getValue());
                        break;
                    case FraudBPRoundPaidXPayrolls:
                        settings.setFraudBPRoundPaidXPayrolls(fraudValue.getValue());
                        break;
                    case FraudBPRoundPaidXAmount:
                        settings.setFraudBPRoundPaidXAmount(fraudValue.getValue());
                        break;
                    case FraudEEPaidXTimes:
                        settings.setFraudEEPaidXTimes(fraudValue.getValue());
                        break;
                    case FraudEENumberOfDaysMultiplePaychecks:
                        settings.setFraudEENumberOfDaysMultiplePaychecks(fraudValue.getValue());
                        break;
                    case FraudEENewEmployeeAddedXDays:
                        settings.setFraudEENewEmployeeAddedXDays(fraudValue.getValue());
                        break;
                    case FraudEEPercentGreaterThanOtherEEs:
                        settings.setFraudEEPercentGreaterThanOtherEEs(fraudValue.getValue());
                        break;
                    case FraudPRMax:
                        settings.setFraudPRMax(fraudValue.getValue());
                        break;
                    case FraudPRMaxXPayrolls:
                        settings.setFraudPRMaxXPayrolls(fraudValue.getValue());
                        break;
                    case FraudEEPercentIncreaseMax:
                        settings.setFraudEEPercentIncreaseMax(fraudValue.getValue());
                        break;
                    case FraudEEPercentIncreaseMaxXPayrolls:
                        settings.setFraudEEPercentIncreaseMaxXPayrolls(fraudValue.getValue());
                        break;
                    case FraudPRPercentIncreaseMax:
                        settings.setFraudPRPercentIncreaseMax(fraudValue.getValue());
                        break;
                    case FraudPRPercentIncreaseMaxXPayrolls:
                        settings.setFraudPRPercentIncreaseMaxXPayrolls(fraudValue.getValue());
                        break;
                    case FraudPRNumberOfDaysForXPayrolls:
                        settings.setFraudPRNumberOfDaysForXPayrolls(fraudValue.getValue());
                        break;
                    case FraudPRNumberOfPayrollsInXDays:
                        settings.setFraudPRNumberOfPayrollsInXDays(fraudValue.getValue());
                        break;
                    case FraudEENumberOfPaychecksSpikeInPay:
                        settings.setFraudEENumberOfPaychecksSpikeInPay(fraudValue.getValue());
                        break;
                    case FraudEEPercentGreaterThanAverage:
                        settings.setFraudEEPercentGreaterThanAverage(fraudValue.getValue());
                        break;
                    case FraudEENumberOfDaysBankAcctUpdated:
                        settings.setFraudEENumberOfDaysBankAcctUpdated(fraudValue.getValue());
                        break;
                    case FraudPRNumberOfPayrollsToCheckSameBank:
                        settings.setFraudPRNumberOfPayrollsToCheckSameBank(fraudValue.getValue());
                        break;
                    case FraudPRPercentEmployeesPaidSameBank:
                        settings.setFraudPRPercentEmployeesPaidSameBank(fraudValue.getValue());
                        break;
                    case FraudPRTotalEmployeesToCheckSameBank:
                        settings.setFraudPRTotalEmployeesToCheckSameBank(fraudValue.getValue());
                        break;
                    case FraudPREmployeesSameBankAccountMax:
                        settings.setFraudPREmployeesSameBankAccountMax(fraudValue.getValue());
                        break;
                    case FraudDDInactivityDays:
                        settings.setFraudDDInactivityDays(fraudValue.getValue());
                        break;
                    case FraudDDInactivityPayrollAmount:
                        settings.setFraudDDInactivityPayrollAmount(fraudValue.getValue());
                        break;
                    case FraudPayeePaidMax:
                        settings.setFraudPayeePaidMax(fraudValue.getValue());
                        break;
                    case FraudPayeePaidMaxXPayrolls:
                        settings.setFraudPayeePaidMaxXPayrolls(fraudValue.getValue());
                        break;
                    case FraudBPAcctUpdateMax:
                        settings.setFraudBPAcctUpdateMax(fraudValue.getValue());
                        break;
                    case FraudBPAcctUpdateXDays:
                        settings.setFraudBPAcctUpdateXDays(fraudValue.getValue());
                        break;
                    case FraudBPMax:
                        settings.setFraudBPMax(fraudValue.getValue());
                        break;
                    case FraudBPMaxXPayrolls:
                        settings.setFraudBPMaxXPayrolls(fraudValue.getValue());
                        break;
                    case FraudPayeePaidXTimes:
                        settings.setFraudPayeePaidXTimes(fraudValue.getValue());
                        break;
                    case FraudPayeeNumberOfDaysMultiplePayments:
                        settings.setFraudPayeeNumberOfDaysMultiplePayments(fraudValue.getValue());
                        break;
                    case FraudBPInactivityDays:
                        settings.setFraudBPInactivityDays(fraudValue.getValue());
                        break;
                    case FraudBPInactivityPayrollAmount:
                        settings.setFraudBPInactivityPayrollAmount(fraudValue.getValue());
                        break;
                    case FraudBPNumberOfDaysForXPayments:
                        settings.setFraudBPNumberOfDaysForXPayments(fraudValue.getValue());
                        break;
                    case FraudBPNumberOfPaymentsInXDays:
                        settings.setFraudBPNumberOfPaymentsInXDays(fraudValue.getValue());
                        break;
                    case FraudBPNumberOfPaymentsToCheckSameBank:
                        settings.setFraudBPNumberOfPaymentsToCheckSameBank(fraudValue.getValue());
                        break;
                    case FraudBPPercentPayeesPaidSameBank:
                        settings.setFraudBPPercentPayeesPaidSameBank(fraudValue.getValue());
                        break;
                    case FraudBPTotalPayeesToCheckSameBank:
                        settings.setFraudBPTotalPayeesToCheckSameBank(fraudValue.getValue());
                        break;
                    case FraudPRXPayrollAmount:
                        settings.setFraudPRXPayrollAmount(fraudValue.getValue());
                        break;
                    case FraudBPXPayrollAmount:
                        settings.setFraudBPXPayrollAmount(fraudValue.getValue());
                        break;
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding fraud settings.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return settings;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SettingUpdate)
    public List<SAPLimitRule> getLimitRules() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<LimitRule> limitRules = Application.find(LimitRule.class);

            List<SAPLimitRule> sapLimitRules = new ArrayList<SAPLimitRule>();
            for (LimitRule limitRule : limitRules) {
                SAPLimitRule sapLimitRule = new SAPLimitRule();
                sapLimitRule.setDescription(limitRule.getDescription());
                sapLimitRule.setId(limitRule.getId().toString());
                sapLimitRules.add(sapLimitRule);
            }

            return sapLimitRules;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding direct deposit limit rules", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SettingUpdate)
    public List<SAPLimitRule> getFraudRules() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<FraudRule> fraudRules = Application.find(FraudRule.class);

            List<SAPLimitRule> sapLimitRules = new ArrayList<SAPLimitRule>();
            for (FraudRule fraudRule : fraudRules) {
                SAPLimitRule sapLimitRule = new SAPLimitRule();
                sapLimitRule.setDescription(fraudRule.getDescription());
                sapLimitRule.setId(fraudRule.getId().toString());
                sapLimitRule.setSourceSystem(fraudRule.getSourceSystemCd().name());
                sapLimitRules.add(sapLimitRule);
            }

            return sapLimitRules;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding fraud rules", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SettingUpdate)
    public void saveDirectDepositLimitSettings(SAPDirectDepositLimitSettings sapDirectDepositLimitSettings,
                                               String pLimitRuleId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            // todo this should probably be pushed down into a process
            LimitRule limitRule = Application.findById(LimitRule.class, SpcfUniqueId.createInstance(pLimitRuleId));
            for (LimitValue limitValue : limitRule.getLimitValueCollection()) {
                switch (limitValue.getName()) {
                    case DefaultCompanyLimit:
                        limitValue.setValue(sapDirectDepositLimitSettings.getDefaultDDCompanyLimit());
                        break;
                    case DefaultEmployeeLimit:
                        limitValue.setValue(sapDirectDepositLimitSettings.getDefaultDDEmployeeLimit());
                        break;
                    case CompanyLimitDuration:
                        limitValue.setValue(sapDirectDepositLimitSettings.getDDCompanyLimitDuration());
                        break;
                    case EmployeeLimitDuration:
                        limitValue.setValue(sapDirectDepositLimitSettings.getDDEmployeeLimitDuration());
                        break;
                    case MaxCompanyLimitDefault:
                        limitValue.setValue(sapDirectDepositLimitSettings.getMaxDDCompanyLimitDefault());
                        break;
                    case ConsecutiveLimitViolationLimit:
                        limitValue.setValue(sapDirectDepositLimitSettings.getConsecutiveLimitViolationLimit());
                        break;
                    case CompanyBankAccountVerificationAttemptLimit:
                        limitValue.setValue(sapDirectDepositLimitSettings.getCompanyBankAccountVerificationAttemptLimit());
                        break;
                    case CompanyBankAccountDurationLimitForVerification:
                        limitValue.setValue(sapDirectDepositLimitSettings.getCompanyBankAccountDurationLimitForVerification());
                        break;
                    case MinimumNonSuspectPayrollAmount:
                        limitValue.setValue(sapDirectDepositLimitSettings.getMinimumNonSuspectPayrollAmount());
                        break;
                }
            }

            DDAutoLimitIncreaseTierDTO[] autoLimitIncreaseTierDTOs =
                    SourcePayrollParameterTranslator.getDDAutoLimitIncreaseTierDTO(sapDirectDepositLimitSettings.getAutoLimitIncreaseTiers());
            ProcessResult processResult = PayrollServices.payrollManager.updateDDAutoLimitIncreaseTiers(pLimitRuleId, autoLimitIncreaseTierDTOs);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error saving direct deposit settings", processResult);
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error saving direct deposit settings", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SettingUpdate)
    public void saveFraudSettings(SAPFraudSettings sapFraudSettings,
                                  String pFraudRuleId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            FraudRule fraudRule = Application.findById(FraudRule.class, SpcfUniqueId.createInstance(pFraudRuleId));
            for (FraudValue fraudValue : fraudRule.getFraudValueCollection()) {
                switch (fraudValue.getName()) {
                    case FraudEEPaidMax:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPaidMax());
                        break;
                    case FraudEEPaidMaxXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPaidMaxXPayrolls());
                        break;
                    case FraudEEAcctUpdateMax:
                        fraudValue.setValue(sapFraudSettings.getFraudEEAcctUpdateMax());
                        break;
                    case FraudEEAcctUpdateXDays:
                        fraudValue.setValue(sapFraudSettings.getFraudEEAcctUpdateXDays());
                        break;
                    case FraudEERoundPaidXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudEERoundPaidXPayrolls());
                        break;
                    case FraudEERoundPaidXAmount:
                        fraudValue.setValue(sapFraudSettings.getFraudEERoundPaidXAmount());
                        break;
                    case FraudBPRoundPaidXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudBPRoundPaidXPayrolls());
                        break;
                    case FraudBPRoundPaidXAmount:
                        fraudValue.setValue(sapFraudSettings.getFraudBPRoundPaidXAmount());
                        break;
                    case FraudPRMax:
                        fraudValue.setValue(sapFraudSettings.getFraudPRMax());
                        break;
                    case FraudPRMaxXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudPRMaxXPayrolls());
                        break;
                    case FraudEEPercentIncreaseMax:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPercentIncreaseMax());
                        break;
                    case FraudEEPercentIncreaseMaxXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPercentIncreaseMaxXPayrolls());
                        break;
                    case FraudPRPercentIncreaseMax:
                        fraudValue.setValue(sapFraudSettings.getFraudPRPercentIncreaseMax());
                        break;
                    case FraudPRPercentIncreaseMaxXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudPRPercentIncreaseMaxXPayrolls());
                        break;
                    case FraudPRNumberOfDaysForXPayrolls:
                        fraudValue.setValue(sapFraudSettings.getFraudPRNumberOfDaysForXPayrolls());
                        break;
                    case FraudPRNumberOfPayrollsInXDays:
                        fraudValue.setValue(sapFraudSettings.getFraudPRNumberOfPayrollsInXDays());
                        break;
                    case FraudEEPaidXTimes:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPaidXTimes());
                        break;
                    case FraudEENumberOfDaysMultiplePaychecks:
                        fraudValue.setValue(sapFraudSettings.getFraudEENumberOfDaysMultiplePaychecks());
                        break;
                    case FraudEENewEmployeeAddedXDays:
                        fraudValue.setValue(sapFraudSettings.getFraudEENewEmployeeAddedXDays());
                        break;
                    case FraudEEPercentGreaterThanOtherEEs:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPercentGreaterThanOtherEEs());
                        break;
                    case FraudEENumberOfPaychecksSpikeInPay:
                        fraudValue.setValue(sapFraudSettings.getFraudEENumberOfPaychecksSpikeInPay());
                        break;
                    case FraudEEPercentGreaterThanAverage:
                        fraudValue.setValue(sapFraudSettings.getFraudEEPercentGreaterThanAverage());
                        break;
                    case FraudEENumberOfDaysBankAcctUpdated:
                        fraudValue.setValue(sapFraudSettings.getFraudEENumberOfDaysBankAcctUpdated());
                        break;
                    case FraudPRNumberOfPayrollsToCheckSameBank:
                        fraudValue.setValue(sapFraudSettings.getFraudPRNumberOfPayrollsToCheckSameBank());
                        break;
                    case FraudPRPercentEmployeesPaidSameBank:
                        fraudValue.setValue(sapFraudSettings.getFraudPRPercentEmployeesPaidSameBank());
                        break;
                    case FraudPRTotalEmployeesToCheckSameBank:
                        fraudValue.setValue(sapFraudSettings.getFraudPRTotalEmployeesToCheckSameBank());
                        break;
                    case FraudPREmployeesSameBankAccountMax:
                        fraudValue.setValue(sapFraudSettings.getFraudPREmployeesSameBankAccountMax());
                        break;
                    case FraudDDInactivityDays:
                        fraudValue.setValue(sapFraudSettings.getFraudDDInactivityDays());
                        break;
                    case FraudDDInactivityPayrollAmount:
                        fraudValue.setValue(sapFraudSettings.getFraudDDInactivityPayrollAmount());
                        break;
                    case FraudPRXPayrollAmount:
                        fraudValue.setValue(sapFraudSettings.getFraudPRXPayrollAmount());
                        break;
                    case FraudBPXPayrollAmount:
                        fraudValue.setValue(sapFraudSettings.getFraudBPXPayrollAmount());
                        break;
                }
                if (fraudRule.getSourceSystemCd() == SourceSystemCode.QBDT || fraudRule.getSourceSystemCd() == SourceSystemCode.IOP) {
                    switch (fraudValue.getName()) {
                        case FraudPayeePaidMax:
                            fraudValue.setValue(sapFraudSettings.getFraudPayeePaidMax());
                            break;
                        case FraudPayeePaidMaxXPayrolls:
                            fraudValue.setValue(sapFraudSettings.getFraudPayeePaidMaxXPayrolls());
                            break;
                        case FraudBPMax:
                            fraudValue.setValue(sapFraudSettings.getFraudBPMax());
                            break;
                        case FraudBPMaxXPayrolls:
                            fraudValue.setValue(sapFraudSettings.getFraudBPMaxXPayrolls());
                            break;
                        case FraudPayeePaidXTimes:
                            fraudValue.setValue(sapFraudSettings.getFraudPayeePaidXTimes());
                            break;
                        case FraudPayeeNumberOfDaysMultiplePayments:
                            fraudValue.setValue(sapFraudSettings.getFraudPayeeNumberOfDaysMultiplePayments());
                            break;
                        case FraudBPInactivityDays:
                            fraudValue.setValue(sapFraudSettings.getFraudBPInactivityDays());
                            break;
                        case FraudBPInactivityPayrollAmount:
                            fraudValue.setValue(sapFraudSettings.getFraudBPInactivityPayrollAmount());
                            break;
                        case FraudBPNumberOfDaysForXPayments:
                            fraudValue.setValue(sapFraudSettings.getFraudBPNumberOfDaysForXPayments());
                            break;
                        case FraudBPNumberOfPaymentsInXDays:
                            fraudValue.setValue(sapFraudSettings.getFraudBPNumberOfPaymentsInXDays());
                            break;
                        case FraudBPNumberOfPaymentsToCheckSameBank:
                            fraudValue.setValue(sapFraudSettings.getFraudBPNumberOfPaymentsToCheckSameBank());
                            break;
                        case FraudBPPercentPayeesPaidSameBank:
                            fraudValue.setValue(sapFraudSettings.getFraudBPPercentPayeesPaidSameBank());
                            break;
                        case FraudBPTotalPayeesToCheckSameBank:
                            fraudValue.setValue(sapFraudSettings.getFraudBPTotalPayeesToCheckSameBank());
                            break;
                        case FraudBPAcctUpdateMax:
                            fraudValue.setValue(sapFraudSettings.getFraudBPAcctUpdateMax());
                            break;
                        case FraudBPAcctUpdateXDays:
                            fraudValue.setValue(sapFraudSettings.getFraudBPAcctUpdateXDays());
                            break;

                    }
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error saving fraud settings", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ConfirmOffload)
    public ArrayList<SAPNachaFile> getNachaFilesForOffload() throws Throwable {
        ArrayList<SAPNachaFile> nachaFiles = new ArrayList<SAPNachaFile>();
        int day = 0;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            while (day > -4) {
                SpcfCalendar today = PSPDate.getPSPTime();
                today.addDays(day);
                CalendarUtils.clearTime(today);
                DomainEntitySet<NACHAFile> files = NACHAFile.getNACHAFilesForOffloadDate(today, NACHAFileStatus.Archived, NACHAFileStatus.Transmitted, NACHAFileStatus.Acknowledged, NACHAFileStatus.PendingAcknowledgement);
                if (files != null) {
                    for (NACHAFile file : files) {
                        SAPNachaFile nachaFile = new SAPNachaFile();
                        nachaFile.setFileName(file.getFileName());
                        if (file.getCreditTxnTotalAmount() != null)
                            nachaFile.setTotalCredits(SAPTranslator.getDoubleFromSpcfMoney(file.getCreditTxnTotalAmount()));
                        if (file.getDebitTxnTotalAmount() != null)
                            nachaFile.setTotalDebits(SAPTranslator.getDoubleFromSpcfMoney(file.getDebitTxnTotalAmount()));
                        nachaFile.setConfirmationCode(file.getConfirmationCode());
                        nachaFile.setFileId(file.getId().toString());     // Entity Id - not File Id
                        if (file.getFinalizationDate() != null)
                            nachaFile.setFinalizedTime(SAPTranslator.getDateFromSpcfCalendar(file.getFinalizationDate()));
                        if (file.getTransmissionDate() != null)
                            nachaFile.setTransmissionTime(SAPTranslator.getDateFromSpcfCalendar(file.getTransmissionDate()));

                        nachaFiles.add(nachaFile);

                    }
                }
                day--;
            }
            return nachaFiles;
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error finding Offloaded NACHA Files.", ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ConfirmOffload)
    public void confirmOffloadFiles(ArrayList<SAPNachaFile> nachaFiles) throws Throwable {
        PayrollServices.beginUnitOfWork();
        ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
        try {
            for (SAPNachaFile file : nachaFiles) {
                prList.add(PayrollServices.payrollManager.confirmNACHAFile(file.getFileId(), file.getConfirmationCode()));
            }

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error Confirming Nacha File.", prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error Confirming Nacha File.", ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RequestSecondOffload)
    public void scheduleSecondaryOffload() throws Throwable {
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult;
        try {
            SpcfCalendar today = PSPDate.getPSPTime();
            today.setValues(today.getYear(), today.getMonth(), today.getDay(), 19, 0, 0, 0);
            OffloadGroup group = OffloadGroup.findStandardOffloadGroup();
            processResult = PayrollServices.batchJobManager.scheduleSecondOffload(group, today);
            if (processResult.isSuccess())
                PayrollServices.commitUnitOfWork();
            else {
                aeFactory.throwGenericException("Error scheduling second offload.", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error scheduling second offload.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    // checks whether a second offload has already been scheduled for the day
    @FlexMethod
    @Operation(operationIds = OperationId.ConfirmOffload)
    public boolean isSecondOffloadScheduled() throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            SpcfCalendar today = PSPDate.getPSPTime();
            OffloadGroup group = OffloadGroup.findStandardOffloadGroup();
            SecondOffload secondOffload = group.getSecondOffload(today);
            return secondOffload != null;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error checking for existing second offload.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return false;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ExecuteSQL)
    public SAPSqlExecutionResult executeSql(String pSQL, String pReason, int pExpectedRowCount) throws Throwable {

        if (pExpectedRowCount == 0) {
            throw new RuntimeException("Expected Row Count Cannot Be Zero");
        }

        if(queryHasRestrictedAttributes(pSQL)) {
            throw new RuntimeException("You are not allowed to execute this sql. Sql=" + pSQL);
        }

        // record the impending action
        PayrollServices.beginUnitOfWork();
        SqlExecutionLogEntry sqlLogEntry = new SqlExecutionLogEntry();
        sqlLogEntry.setUserName(Application.getCurrentPrincipal().getName());
        sqlLogEntry.setReason(pReason);
        sqlLogEntry.setCommitted(false);
        sqlLogEntry.setRowCount(-1);
        sqlLogEntry.setExecutionTime(-1);
        Application.save(sqlLogEntry);
        sqlLogEntry.setSQL(pSQL);
        PayrollServices.commitUnitOfWork();

        // execute the statement and record update count and execution time
        int updateCount = 0;
        PayrollServices.beginUnitOfWork();
        String errorMessage = "";
        String[] statements = {};
        String statement = "";
        StopWatch sw = StopWatch.create(true);
        try {
            // split on Oracle statement execution character
            statements = pSQL.split("\\n/(\\s|\\n)*", -1);
            for (int i = 0; i < statements.length; i++) {
                statement = statements[i];
                if (statement.endsWith("/")) {
                    statement = statement.substring(0, statement.length() - 1);
                }

                if (statement.trim().length() == 0) {
                    continue;
                }

                logger.info("SQLConsole: executing statement: " + statement);
                updateCount += Application.executeSqlCommand(statement, false);
            }
            sw.stop();

            if ((pExpectedRowCount > 0) && (updateCount != pExpectedRowCount)) {
                throw new RuntimeException("Expected row count (" + pExpectedRowCount + ") did not equal update count (" + updateCount + ")");
            } else if ((pExpectedRowCount < 0) && (updateCount > Math.abs(pExpectedRowCount))) {
                throw new RuntimeException("Expected maximum count (" + Math.abs(pExpectedRowCount) + ") exceeded by actual update count (" + updateCount + ")");
            }

            finalizeLogEntry(pSQL, pReason, updateCount, sw.getElapsedMillis(), errorMessage);
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();
            updateCount = 0;

            errorMessage = t.getMessage();
            while ((t = t.getCause()) != null) {
                if (t instanceof SQLException) {
                    errorMessage = t.getMessage();
                    if (statements.length > 0) {
                        errorMessage += "\n\n-- on statement --\n\n" + statement;
                    }
                    break;
                }
            }

            PayrollServices.beginUnitOfWork();
            finalizeLogEntry(pSQL, pReason, updateCount, sw.getElapsedMillis(), errorMessage);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        SAPSqlExecutionResult sqlExecutionResult = new SAPSqlExecutionResult();
        sqlExecutionResult.setSqlStatement(pSQL);
        sqlExecutionResult.setReason(pReason);
        sqlExecutionResult.setExpectedRowCount(pExpectedRowCount);
        sqlExecutionResult.setRowCount(updateCount);
        sqlExecutionResult.setExecutionTime(sw.getElapsedTimeString());
        sqlExecutionResult.setErrorMessage(errorMessage);
        return sqlExecutionResult;
    }

    private boolean queryHasRestrictedAttributes(String sql) {
        if(!Application.isProdEnvironment()) {
            return false;
        }
        String upperCaseQuery = sql.toUpperCase();
        if(upperCaseQuery != null && upperCaseQuery.contains(PSP_SYSTEM_PARAMETER)) {
            for(String value : RESTRICTED_SYSTEM_PARAMETER_VALUES) {
                if(upperCaseQuery.contains(value))
                    return true;
            }
        }
        return false;
    }

    private void finalizeLogEntry(String pSQL, String pReason, int updateCount, long elapsedTime, String errorMessage) {
        SqlExecutionLogEntry sqlLogEntry = new SqlExecutionLogEntry();
        sqlLogEntry.setUserName(Application.getCurrentPrincipal().getName());
        sqlLogEntry.setReason(pReason);
        sqlLogEntry.setCommitted(true);
        sqlLogEntry.setRowCount(updateCount);
        sqlLogEntry.setExecutionTime(elapsedTime);
        sqlLogEntry.setErrorMessage(errorMessage);
        Application.save(sqlLogEntry);
        sqlLogEntry.setSQL(pSQL);
    }

    /*
    Screen

        Job: NotifyAchOffloadStarted
                     Start Time: <start-time>
               Current Run Time: <now -  start-time>     <-- only show when incomplete
            Expected Completion: <expected-end>
              Actual Completion: <actual>                <-- only show when complete

        Job: Offload ACH Data
                     Start Time: <start-time>
               Current Run Time: <now -  start-time>     <-- only show when incomplete
            Expected Completion: <expected-end>
              Actual Completion: <actual>                <-- only show when complete

            Step                     Status            Started At       Duration
            ------------------------|-----------------|----------------|--------------
            Update MMT               Completed         <start-time>     <elapsed>
            Update FT                Executing         <start-time>
            Update EDR CCD           Pending
            Update EDR PPD           Pending
            Update PR  PPD           Pending
            Update PR Reversals      Pending
     */

    public static enum BatchJob {
        StartPrimaryAchOffloadMonitor,
        NotifyAchOffloadStarted,
        OffloadAchData(new BatchJobStep[]{
                BatchJobStep.UpdateMMT,
                BatchJobStep.UpdateEDR_CCD,
                BatchJobStep.UpdateEDR_PPD}),
        CreateAchFiles,
        UploadAchFiles,
        NotifyAchOffloadComplete,
        DownloadDicrFilesDelayPeriod,
        DownloadDicrFiles,
        ArchiveDailyFiles,
        UpdateFinancialTransaction(new BatchJobStep[]{
                BatchJobStep.UpdateFT,
        }),
        UpdatePayrollStatus(new BatchJobStep[]{
                BatchJobStep.UpdatePR_PPD,
                BatchJobStep.UpdatePR_Reversals}),
        InsertFinancialTransactionState,
        CreateTransactionOffloadedEvents,
        MissedPayrollProcessor,
        MissedTransactionProcessor,
        GemsAccountsReceivableProcessor,
        SalesTaxExceptionProcessor;

        private BatchJobStep[] steps;

        BatchJob() {
            this(new BatchJobStep[]{});
        }

        BatchJob(BatchJobStep[] jobSteps) {
            steps = jobSteps;
        }

        public BatchJobStep[] getSteps() {
            return steps;
        }

        /**
         *
         */
        public enum BatchJobStep {
            UpdateMMT("Updating MONEY_MOVEMENT_TRANSACTION"),
            UpdateFT("Updating FINANCIAL_TRANSACTION"),
            UpdateEDR_CCD("Updating ENTRY_DETAIL_RECORD CCD"),
            UpdateEDR_PPD("Updating ENTRY_DETAIL_RECORD PPD"),
            UpdatePR_PPD("Updating PAYROLL_RUN non-reversals"),
            UpdatePR_Reversals("Updating PAYROLL_RUN reversals");

            private String displayName;

            BatchJobStep(String stepDisplayName) {
                displayName = stepDisplayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewOffloadStatus)
    public SAPACHOffloadStatus getOffloadStatus(Date statusDate) throws Throwable {
        SAPACHOffloadStatus offloadStatus = new SAPACHOffloadStatus();

        try {
            offloadStatus.setJobLogEntries(new ArrayList<SAPACHOffloadJobLogEntry>());

            // create 'template' instances for each expected job log entry and its dependent jobStep entries
            HashMap<String, SAPACHOffloadJobLogEntry> jobLogEntryMap = new HashMap<String, SAPACHOffloadJobLogEntry>();
            for (BatchJob job : BatchJob.values()) {
                SAPACHOffloadJobLogEntry entry = createSAPEntry(job);
                jobLogEntryMap.put(entry.getJobName(), entry);
                offloadStatus.getJobLogEntries().add(entry);
            }

            // fill in the templates w/data from the PSP_BATCH_JOB_AUDIT_LOG and PSE_EVENT_LOG tables
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

                // get all the batch job log entries for the passed in date
                // -- NOT HANDLED: it is possible that there are mulitiple offloads on a given day (scheduled, possible failures)
                String[] offloadJobNames = new String[BatchJob.values().length];
                for (int i = 0; i < offloadJobNames.length; i++)
                    offloadJobNames[i] = BatchJob.values()[i].name();

                OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD);
                SpcfCalendar offloadCal = offloadGroup.getCalendarForCutoffTime(CalendarUtils.convertToSpcfCalendar(statusDate));
                Expression<BatchJobAuditLog> batchJobLogExpr =
                        new Query<BatchJobAuditLog>().Where(BatchJobAuditLog.JobNamespace().like("/PSP/HIGH/PrimaryDailyBatchJobs%")
                                .And(BatchJobAuditLog.JobAction().in(offloadJobNames))
                                .And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(offloadCal)))
                                .OrderBy(BatchJobAuditLog.CreatedDate());
                DomainEntitySet<BatchJobAuditLog> dbJobLogEntries = PayrollServices.entityFinder.find(BatchJobAuditLog.class, batchJobLogExpr);

                // offload not started
                if (dbJobLogEntries.size() == 0)
                    return offloadStatus;

                // collapse start/finished messages into single record
                for (BatchJobAuditLog dbJobLogEntry : dbJobLogEntries) {

                    // convert to enum, skip if unexpected value and can't convert
                    BatchJob batchJob = null;
                    try {
                        batchJob = BatchJob.valueOf(dbJobLogEntry.getJobAction());
                    } catch (IllegalArgumentException iae) {
                        continue;
                    }

                    SAPACHOffloadJobLogEntry sapJobLogEntry = jobLogEntryMap.get(batchJob.name());
                    if (sapJobLogEntry == null) {
                        // skip unexpected
                        continue;
                    }

                    if (dbJobLogEntry.getMessage().equals("Started")) {
                        sapJobLogEntry.setStartDateTime(SAPTranslator.getDateFromSpcfCalendar(dbJobLogEntry.getCreatedDate()));
                    } else if (dbJobLogEntry.getMessage().equals("Finished")) {
                        sapJobLogEntry.setFinishDateTime(SAPTranslator.getDateFromSpcfCalendar(dbJobLogEntry.getCreatedDate()));
                    } else {
                        // skip the unexpected
                        continue;
                    }
                }

                // get forecasted numbers for each step in the offload
                // don't let a forecast error stop from method returning event step progress
                Forecast todaysForecast = Forecast.findForecast(offloadCal);
                if (todaysForecast != null) {
                    offloadStatus.setEstimatedTransactionCount(todaysForecast.getEstimatedTransactionCount());
                    offloadStatus.setActualTransactionCount(todaysForecast.getActualTransactionCount());

                    for (ForecastDetail forecastDetail : todaysForecast.getForecastDetailCollection()) {
                        SAPACHOffloadJobLogEntry sapJobLogEntry = jobLogEntryMap.get(forecastDetail.getJobAction());
                        if (sapJobLogEntry == null)
                            continue;

                        sapJobLogEntry.setEstimatedRunTimeInMillis(forecastDetail.getEstimatedRunTime());
                        sapJobLogEntry.setActualRunTimeInMillis(forecastDetail.getActualRunTime());
                    }
                }

                // specific to OffloadAchData
                // get step info (current step status) and apply
                SAPACHOffloadJobLogEntry offloadAchDataJobEntry = jobLogEntryMap.get(BatchJob.OffloadAchData.name());
                if (offloadAchDataJobEntry != null) {
                    Expression<EventLog> expr = new Query<EventLog>().Where(EventLog.ArchitectureName().equalTo("PSP")
                            .And(EventLog.ComponentName().equalTo("PRC_OFFLOAD"))
                            .And(EventLog.ApplicationName().equalTo("Offload Stored Proc"))
                            .And(EventLog.MessageDttm().greaterOrEqualThan(offloadCal)))
                            .OrderBy(EventLog.CreatedDate());
                    DomainEntitySet<EventLog> offloadAchDataDbStepEntries = PayrollServices.entityFinder.find(EventLog.class, expr);

                    // each step record that has a step record after it must have completed
                    // the last step record is marked complete only if the owning job has finished executing, otherwise
                    // the last step record is marked executing
                    HashMap<String, SAPACHOffloadJobStepLogEntry> stepDTOMap = new HashMap<String, SAPACHOffloadJobStepLogEntry>();
                    for (SAPACHOffloadJobStepLogEntry jobStepLogEntry : offloadAchDataJobEntry.getStepLogs()) {
                        stepDTOMap.put(jobStepLogEntry.getStepName(), jobStepLogEntry);
                        jobStepLogEntry.setStatus("Pending");
                    }

                    for (int i = 0; i < offloadAchDataDbStepEntries.size(); i++) {
                        EventLog dbStepEntry = offloadAchDataDbStepEntries.get(i);
                        SAPACHOffloadJobStepLogEntry jobStepLogEntry = stepDTOMap.get(dbStepEntry.getMessage());
                        if (jobStepLogEntry == null)
                            continue;

                        String status = (i < offloadAchDataDbStepEntries.size() - 1 ? "Completed" : "Executing");
                        jobStepLogEntry.setStatus(status);
                        jobStepLogEntry.setStepBeginDateTime(SAPTranslator.getDateFromSpcfCalendar(dbStepEntry.getCreatedDate()));
                    }

                    if (offloadAchDataJobEntry.getFinishDateTime() != null) {
                        offloadAchDataJobEntry.getStepLogs().get(offloadAchDataJobEntry.getStepLogs().size() - 1).setStatus("Completed");
                    }
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving offload status.", t);
        }

        return offloadStatus;
    }

    private SAPACHOffloadJobLogEntry createSAPEntry(BatchJob job) {
        SAPACHOffloadJobLogEntry entry = new SAPACHOffloadJobLogEntry();
        entry.setJobName(job.name());
        entry.setStepLogs(new ArrayList<SAPACHOffloadJobStepLogEntry>());

        for (BatchJob.BatchJobStep jobStep : job.getSteps()) {
            SAPACHOffloadJobStepLogEntry stepLog = new SAPACHOffloadJobStepLogEntry();
            stepLog.setJobName(job.name());
            stepLog.setStepName(jobStep.getDisplayName());
            entry.getStepLogs().add(stepLog);
        }
        return entry;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewOffloadStatus)
    public SAPSearchResults<SAPTransmission> getAllTransmissions(String pFromSourceSystemCode, Date pFromDate, Date pToDate, int pFirstIndex, int pMaxResults)
            throws Throwable {
        SAPSearchResults<SAPTransmission> sapResults = new SAPSearchResults<SAPTransmission>();
        ArrayList<SAPTransmission> returnList = new ArrayList<SAPTransmission>();
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);
            ApplicationSecondary.getHibernateSession().setFlushMode(FlushMode.MANUAL);
            if (pFromSourceSystemCode.equals(SourceSystemCode.AMO.name())) {
                /*Retrieving (Page size + 1) Nos of record to Determine if records are available for next page*/
                ArrayList<SAPTransmission> transmissionListAmo = findAMOTransmissions(pFromSourceSystemCode, pFromDate, pToDate, pFirstIndex, pMaxResults + 1);
                /* Setting the value for totalRecords, as it is used to determine whether we have to display the Next button*/
                if (transmissionListAmo.size() > pMaxResults) {
                    sapResults.setTotalRecords(pFirstIndex + pMaxResults + 1);
                    /*Removing the additional row fetched*/
                    transmissionListAmo.remove(pMaxResults);
                } else {
                    sapResults.setTotalRecords(pFirstIndex + transmissionListAmo.size());
                }
                sapResults.setReturnsList(transmissionListAmo);

            } else {
                /*Retrieving (Page size + 1) Nos of record to Determine if records are available for next page*/
                DomainEntitySet<SourceSystemTransmission> transmissionList = findAllTransmissionsSecondary(pFromSourceSystemCode, pFromDate, pToDate, pFirstIndex, pMaxResults + 1);
                /* Setting the value for totalRecords, as it is used to determine whether we have to display the Next button*/
                if (transmissionList.size() > pMaxResults) {
                    sapResults.setTotalRecords(pFirstIndex + pMaxResults + 1);
                    /*Removing the additional row fetched*/
                    transmissionList.remove(pMaxResults);
                } else {
                    sapResults.setTotalRecords(pFirstIndex + transmissionList.size());
                }
                for (SourceSystemTransmission transmission : transmissionList) {
                    String companyId = transmission.getCompanyId();
                    Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(companyId));
                    if (null != company && company.getSourceCompanyId() != "100000000")
                        returnList.add(CompanyTranslator.getTransmissionFromDomainEntity(transmission, false));
                }
                sapResults.setReturnsList(returnList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting transmissions list.", pFromSourceSystemCode, pFromSourceSystemCode, t);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
        return sapResults;
    }

    private DomainEntitySet<SourceSystemTransmission> findAllTransmissionsSecondary(String pFromSourceSystemCode, Date pFromDate, Date pToDate, int pStartIndex, int pMaxResults)
            throws Throwable {
        DomainEntitySet<SourceSystemTransmission> transmissionList = null;
        try {
            Expression<SourceSystemTransmission> expr = new Query<SourceSystemTransmission>()
                    .Where(getTransmissionCriteriaSecondary(pFromDate, pToDate, pFromSourceSystemCode)).OrderBy(SourceSystemTransmission.CreatedDate().Descending()).LimitResults(pStartIndex, pMaxResults);
            transmissionList = PayrollServices.entityFinderSecondary.find(SourceSystemTransmission.class, expr);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transmissions.", pFromSourceSystemCode, pFromSourceSystemCode, t);
        }
        return transmissionList;
    }

    private ArrayList<SAPTransmission> findAMOTransmissions(String pFromSourceSystemCode,
                                                            Date pFromDate, Date pToDate,
                                                            int pStartIndex,
                                                            int pMaxResults
    ) throws Throwable {
        ArrayList<SAPTransmission> returnList = new ArrayList<SAPTransmission>();
        try {
            DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findEntitlementMessages(
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                    pStartIndex, pMaxResults);
            for (EntitlementMessage entitlementMessage : entitlementMessages) {
                SAPTransmission transmission = CompanyTranslator.getTransmissionFromDomainEntity(entitlementMessage, false);
                String companyNames = "";
                String psids = "";
                Entitlement entitlement = Entitlement.findEntitlement(entitlementMessage.getLicenseNumber(), entitlementMessage.getEntitlementOfferingCode());
                String separator = "";
                for (EntitlementUnit eu : entitlement.getActiveEntitlementUnitCollection()) {
                    companyNames += separator + eu.getCompany().getDbaName();
                    psids += separator + eu.getCompany().getSourceCompanyId();
                    separator = ", ";
                }
                transmission.setCompanyName(companyNames);
                transmission.setCompanyKey(new SAPCompanyKey("", psids));
                returnList.add(transmission);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding AMO transmissions.", t);
        }
        return returnList;
    }

    private Criterion<SourceSystemTransmission> getTransmissionCriteriaSecondary(Date pFromDate, Date pToDate, String pFromSourceSystemCode) {
        Company comp = Company.findCompany("100000000", SourceSystemCode.valueOf(pFromSourceSystemCode));
        if (null != comp && null != comp.getId()) {
            return SourceSystemTransmission.FromSourceSystem().equalTo(SourceSystemCode.valueOf(pFromSourceSystemCode))
                    .And(SourceSystemTransmission.CreatedDate().greaterOrEqualThan(new SpcfCalendarImpl(pFromDate.getTime())))
                    .And(SourceSystemTransmission.CreatedDate().lessOrEqualThan(new SpcfCalendarImpl(pToDate.getTime())))
                    .And(SourceSystemTransmission.CompanyId().notEqualTo(comp.getId().toString()));
        } else {
            return SourceSystemTransmission.FromSourceSystem().equalTo(SourceSystemCode.valueOf(pFromSourceSystemCode))
                    .And(SourceSystemTransmission.CreatedDate().greaterOrEqualThan(new SpcfCalendarImpl(pFromDate.getTime())))
                    .And(SourceSystemTransmission.CreatedDate().lessOrEqualThan(new SpcfCalendarImpl(pToDate.getTime())));
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ScheduleATFExtract)
    public void scheduleATFExtract(String year, int quarter, Date scheduleDate) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            String formatedScheduledDate = SAPTranslator.getDateFormat(scheduleDate);

            SpcfCalendar spcfCalendarFromDate = null;
            if (scheduleDate != null) {
                spcfCalendarFromDate = SAPTranslator.getSpcfCalendarFromDate(scheduleDate);
                if (spcfCalendarFromDate.before(PSPDate.getPSPTime())) {
                    throw new RuntimeException("Scheduled date must be in the future.");
                }
            }

            scheduleJob(BatchJobType.ATFDataExtract.name() + "_" + year + "Q" + quarter + "_" + formatedScheduledDate, spcfCalendarFromDate, String.format("%s %s %s", ATFDataExtractRunType.QuarterlyData.toString(),
                    year,
                    Integer.toString(quarter)));
            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error scheduling ATF extract", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public List<SAPLedgerOperationJob> getLedgerOperationJobs() throws Throwable {
        List<SAPLedgerOperationJob> sapJobs = new ArrayList<SAPLedgerOperationJob>();

        try {
            SpcfCalendar createdDate = SpcfCalendar.getNow();
            createdDate.addMonths(-24);
            CalendarUtils.clearTime(createdDate);
            logger.info("GetLedgerOperationJobs entered will retrieve the data from "+createdDate.toString());

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            HqlBuilder hql = new HqlBuilder(true);
            hql.append("SELECT ledgerOperation.LedgerOperationJob.Id, ledgerOperation.Memo " +
                    "FROM com.intuit.sbd.payroll.psp.domain.LedgerOperation ledgerOperation " +
                    "WHERE ledgerOperation.CreatedDate >= :createdDate AND ledgerOperation.Memo LIKE 'TOR%' " +
                    "GROUP BY ledgerOperation.LedgerOperationJob.Id, ledgerOperation.Memo")
                    .setParameter("createdDate", createdDate);
            List<Object[]> memoResult = hql.list();
            Map<SpcfUniqueId, String> ledgerOperationlist = memoResult.stream().collect(Collectors.toMap(key -> (SpcfUniqueId)key[0], key -> (String)key[1]));

            hql = new HqlBuilder(true);
            hql.append("SELECT ledgerOperationJob.Id, ledgerOperationJob.CreatorId, ledgerOperationJob.CreatedDate, ledgerOperationJob.ModifierId, " +
                                "ledgerOperationJob.ModifiedDate, ledgerOperationJob.Description, ledgerOperationJob.Status, ledgerOperationJob.StartTime, " +
                                "ledgerOperationJob.FinishTime, ledgerOperationJob.JobType, COUNT(*), SUM(CASE WHEN ledgerOperation.Status='Completed' THEN 1 ELSE 0 END), " +
                                "SUM(CASE WHEN ledgerOperation.Status='Error' THEN 1 ELSE 0 END)  " +
                    "FROM com.intuit.sbd.payroll.psp.domain.LedgerOperation ledgerOperation, " +
                    "com.intuit.sbd.payroll.psp.domain.LedgerOperationJob ledgerOperationJob " +
                    "WHERE ledgerOperationJob.CreatedDate >= :createdDate " +
                    "AND ledgerOperation.LedgerOperationJob = ledgerOperationJob " +
                    "GROUP BY ledgerOperationJob.Id, ledgerOperationJob.CreatorId, ledgerOperationJob.CreatedDate, ledgerOperationJob.ModifierId, " +
                    "ledgerOperationJob.ModifiedDate, ledgerOperationJob.Description, ledgerOperationJob.Status, ledgerOperationJob.StartTime, " +
                    "ledgerOperationJob.FinishTime, ledgerOperationJob.JobType " +
                    "ORDER BY ledgerOperationJob.CreatedDate DESC").setParameter("createdDate", createdDate);
            List<Object[]> ledgerJobResult = hql.list();

            LedgerOperationJob job = null;
            String memo = null;

            Method method = ReflectionUtils.findMethod(LedgerOperationJob.class, "setId", SpcfUniqueId.class);
            method.setAccessible(true);
            for (Object[] queryResult : ledgerJobResult) {
                job = new LedgerOperationJob();
                ReflectionUtils.invokeMethod(method, job, (SpcfUniqueId)queryResult[0]);
                job.setDescription((String)queryResult[5]);
                job.setStatus((LedgerOperationJobStatus)queryResult[6]);
                job.setStartTime((SpcfCalendar)queryResult[7]);
                job.setFinishTime((SpcfCalendar)queryResult[8]);
                job.setCreatedDate((SpcfCalendar)queryResult[2]);
                job.setJobType((LedgerOperationJobType)queryResult[9]);

                Long totalRecords = (Long) queryResult[10];
                Long completedRecords = (Long) queryResult[11];
                Long errorRecords = (Long) queryResult[12];
                Long processedRecords = new Long(0);
                if (job.getStatus() == LedgerOperationJobStatus.InProgress) {
                    processedRecords = completedRecords + errorRecords;
                }

                if (job.getJobType() == LedgerOperationJobType.TOR) {
                    memo = ledgerOperationlist.get(job.getId());
                } else {
                    memo = null;
                }

                sapJobs.add(AdministrationTranslator.getSAPLedgerOperationJob(job, totalRecords.intValue(), processedRecords.intValue(), memo));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ledger operation jobs", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.info("GetLedgerOperationJobs Finished");
        return sapJobs;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public void queueLedgerOperationJob(String jobId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult processResult = PayrollServices.batchJobManager.queueLedgerOperationJob(SpcfUniqueId.createInstance(jobId));
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error queueing ledger operation job.", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error queueing ledger operation job.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Operation(operationIds = OperationId.LedgerOperations)
    public void uploadLedgerOperationsFile(byte[] file, String description) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            Map<String, Law> lawTypeCodeMap = new HashMap<String, Law>();
            Map<String, AdditionalFilingAmount> nameAdditionalFilingAmountMap = new HashMap<String, AdditionalFilingAmount>();

            LedgerOperationJobDTO ledgerOperationJobDTO = new LedgerOperationJobDTO();
            ledgerOperationJobDTO.setDescription(description);
            ledgerOperationJobDTO.setOriginalFile(new String(file));

            CsvReader reader = new CsvReader(new ByteArrayInputStream(file), Charset.defaultCharset());

            reader.readRecord();
            //set the type to the first row
            String masterJobType = readField(reader, LedgerOpsField.Type);
            LedgerOperationJobType jobType = LedgerOperationJobType.valueOf(masterJobType);
            ledgerOperationJobDTO.setType(jobType);
            do {
                String jobTypeString = readField(reader, LedgerOpsField.Type);
                if (!StringUtils.equals(jobTypeString, masterJobType)) {
                    aeFactory.throwGenericException("Job type must be the same for all records");
                }

                LedgerOperationDTO ledgerOperationDTO = new LedgerOperationDTO();

                String sourceSystemCdString = readField(reader, LedgerOpsField.SourceSystemCd);
                ledgerOperationDTO.setSourceSystemCd(SourceSystemCode.valueOf(sourceSystemCdString));

                ledgerOperationDTO.setSourceCompanyId(readField(reader, LedgerOpsField.SourceCompanyId));

                String updatedValueString = readField(reader, LedgerOpsField.Amount);
                if (StringUtils.isNotEmpty(updatedValueString)) {
                    if (jobType.in(LedgerOperationJobType.RateUpdate, LedgerOperationJobType.AdditionalFilingAmountUpdate)) {
                        ledgerOperationDTO.setRate(Double.parseDouble(updatedValueString));
                    } else if (jobType == LedgerOperationJobType.DepositFrequencyUpdate) {
                        ledgerOperationDTO.setDepositFrequencyCode(DepositFrequencyCode.valueOf(updatedValueString));
                    } else {
                        ledgerOperationDTO.setAmount(new SpcfMoney(updatedValueString));
                    }
                }

                String taxableWagesString = readField(reader, LedgerOpsField.TaxableWages);
                if (StringUtils.isNotEmpty(taxableWagesString)) {
                    ledgerOperationDTO.setTaxableWages(new SpcfMoney(taxableWagesString));
                }

                ledgerOperationDTO.setMemo(readField(reader, LedgerOpsField.Memo));

                String lawTypeCdOrAdditionalFilingAmountName = readField(reader, LedgerOpsField.LawTypeCd);
                Law law = getLawFromTypeCd(lawTypeCodeMap, lawTypeCdOrAdditionalFilingAmountName);
                if (law == null) {
                    AdditionalFilingAmount additionalFilingAmount = getAdditionalFilingAmountFromName(nameAdditionalFilingAmountMap, lawTypeCdOrAdditionalFilingAmountName);
                    law = additionalFilingAmount.getPaymentTemplate().getLawCollection().sort(Law.LawId()).getFirst();
                    ledgerOperationDTO.setAdditionalAmountName(lawTypeCdOrAdditionalFilingAmountName);
                }
                ledgerOperationDTO.setLawId(law.getLawId());

                ledgerOperationDTO.setOriginalLegalName(readField(reader, LedgerOpsField.LegalName));

                String checkDateString = readField(reader, LedgerOpsField.CheckDate);
                ledgerOperationDTO.setCheckDate(new DateDTO(checkDateString));

                String pushToQuickBooks = readField(reader, LedgerOpsField.PushToQuickBooks);
                if (StringUtils.equals(pushToQuickBooks, "Y")) {
                    ledgerOperationDTO.setPushToQuickBooks(true);
                }

                ledgerOperationJobDTO.getLedgerOperations().add(ledgerOperationDTO);

            } while (reader.readRecord());

            ProcessResult processResult = PayrollServices.batchJobManager.addLedgerOperationJob(ledgerOperationJobDTO);
            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error adding job", processResult);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error uploading ledger operations file", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public void createTORLedgerOperationJob(String paymentTemplateCd, Date quarterDate) throws Throwable {
        try {
            if("ALL".equalsIgnoreCase(paymentTemplateCd)) {
                int availableProcessors = Runtime.getRuntime().availableProcessors();
                ExecutorService executorService = null;
                final PspPrincipal pspPrincipal = Application.getCurrentPrincipal();

                try {
                    TaxAdapter taxAdapter = new TaxAdapter();
                    List<SAPAgency> sapAgencyList = taxAdapter.getAgencyList();
                    logger.info("Available Processors : "+availableProcessors);
                    executorService = Executors.newFixedThreadPool(availableProcessors * 2);
                    CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);
                    for (final SAPAgency sapAgency : sapAgencyList) {
                        for (final SAPPaymentTemplate sapPaymentTemplate : sapAgency.getPaymentTemplates()) {
                            completionService.submit(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    try {
                                        Application.setCurrentPrincipal(pspPrincipal);
                                        createTORLedgerOperationJob(sapPaymentTemplate.getPaymentTemplateCd(), quarterDate);
                                    } catch (Throwable t) {
                                        logger.error("Error while creating TOR job under payment " + sapPaymentTemplate.getPaymentTemplateCd() + " for " + quarterDate, t);
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                } catch(Throwable t) {
                    aeFactory.throwGenericException("Available Processor : "+availableProcessors+". Error while creating TOR ledger operation", t);
                } finally {
                    ThreadingUtils.shutdownAndAwaitTermination(executorService, 10, 300);
                }
            } else {
            PayrollServices.beginUnitOfWork();

            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            Law law = paymentTemplate.getLawCollection().sort(Law.LawId()).getFirst();
            SpcfCalendar lastDayOfQuarter = CalendarUtils.getLastDayOfQuarter(SAPTranslator.getSpcfCalendarFromDate(quarterDate));
            DateDTO quarterEndDate = new DateDTO(lastDayOfQuarter);

            StringWriter stringWriter = new StringWriter();
            CsvWriter writer = new CsvWriter(stringWriter, ',');

            ScrollableResults scrollableResults = Application.findScrollable(
                    CompanyAgencyPaymentTemplate.class,
                    new Query<CompanyAgencyPaymentTemplate>().Select(CompanyAgencyPaymentTemplate.CompanyAgency().Company().SourceSystemCd(),
                            CompanyAgencyPaymentTemplate.CompanyAgency().Company().SourceCompanyId(),
                            CompanyAgencyPaymentTemplate.CompanyAgency().Company().LegalName())
                            .Where(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate)
                                    .And(CompanyAgencyPaymentTemplate.CompanyAgency().Company().CompanyServiceSet()
                                            .Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax))))
                            .OrderBy(CompanyAgencyPaymentTemplate.CompanyAgency().Company().SourceCompanyId()));
            LedgerOperationJobDTO ledgerOperationJobDTO = new LedgerOperationJobDTO();
            ledgerOperationJobDTO.setType(LedgerOperationJobType.TOR);
            while (scrollableResults.next()) {
                SourceSystemCode sourceSystemCode = (SourceSystemCode) scrollableResults.get(0);
                String sourceCompanyId = (String) scrollableResults.get(1);
                String legalName = (String) scrollableResults.get(2);

                LedgerOperationDTO ledgerOperationDTO = new LedgerOperationDTO();

                writer.write(sourceSystemCode.toString());
                ledgerOperationDTO.setSourceSystemCd(sourceSystemCode);

                writer.write(sourceCompanyId);
                ledgerOperationDTO.setSourceCompanyId(sourceCompanyId);

                writer.write("");
                ledgerOperationDTO.setAmount(null);

                //memo string has used in query for search pattern if changed then it might be impact (refer AdministrationAdapter.java -> getLedgerOperationJobs)
                String memo = "TOR " + paymentTemplateCd + " " + lastDayOfQuarter.getYear() + " Q" + CalendarUtils.getQuarterAsInt(lastDayOfQuarter);
                writer.write(memo);
                ledgerOperationDTO.setMemo(memo);

                writer.write(LedgerOperationJobType.TOR.toString());

                writer.write(law.getLawTypeCd());
                ledgerOperationDTO.setLawId(law.getLawId());

                writer.write(legalName);
                ledgerOperationDTO.setOriginalLegalName(legalName);

                writer.write(lastDayOfQuarter.format("yyyy-MM-dd"));
                ledgerOperationDTO.setCheckDate(quarterEndDate);

                writer.endRecord();
                ledgerOperationJobDTO.getLedgerOperations().add(ledgerOperationDTO);
            }

            stringWriter.flush();
            ledgerOperationJobDTO.setOriginalFile(stringWriter.toString());

            ProcessResult processResult = PayrollServices.batchJobManager.addLedgerOperationJob(ledgerOperationJobDTO);
            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error adding job", processResult);
            } else {
                PayrollServices.commitUnitOfWork();
                    }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating TOR ledger operation", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    private static enum LedgerOpsField {
        SourceSystemCd,
        SourceCompanyId,
        Amount,
        Memo,
        Type,
        LawTypeCd,
        LegalName,
        CheckDate,
        PushToQuickBooks,
        TaxableWages
    }

    private String readField(CsvReader reader, LedgerOpsField field) throws IOException {
        return reader.get(field.ordinal());
    }

    private Law getLawFromTypeCd(Map<String, Law> cache, String lawTypeCd) {
        if (cache.containsKey(lawTypeCd)) {
            return cache.get(lawTypeCd);
        } else {
            DomainEntitySet<Law> laws = Application.find(Law.class, Law.LawTypeCd().equalTo(lawTypeCd));
            if (laws.size() > 1) {
                throw new RuntimeException("Multiple laws for type code " + lawTypeCd);
            }
            Law law = laws.getFirst();
            cache.put(lawTypeCd, law);
            return law;
        }
    }

    private AdditionalFilingAmount getAdditionalFilingAmountFromName(Map<String, AdditionalFilingAmount> cache, String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        } else {
            AdditionalFilingAmount additionalFilingAmount = AdditionalFilingAmount.findByName(name);
            cache.put(name, additionalFilingAmount);
            return additionalFilingAmount;
        }
    }


    @FlexMethod
    public int getMinSupportedQuickbooksVersion(String sourceSystemCd) throws Throwable {
        int version = 0;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            version = SourcePayrollParameter.findIntValue(SourceSystemCode.valueOf(sourceSystemCd), SourcePayrollParameterCode.MinQBVersionSupported);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding min supported quickbooks version.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return version;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public List<String> getAvailableSUICreditTemplates() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            return Application.executeQuery(AdditionalFilingAmount.class,
                    new Query<AdditionalFilingAmount>()
                            .Select(AdditionalFilingAmount.PaymentTemplate().PaymentTemplateCd())
                            .Where(AdditionalFilingAmount.IsSystemAppliedCredit().equalTo(true)));

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding available templates.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public List<SAPSUICreditsJob> getSUICreditsJobList() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            List<SAPSUICreditsJob> jobList = new ArrayList<SAPSUICreditsJob>();
            for (SUICreditsJob suiCreditsJob : Application.find(SUICreditsJob.class).sort(SUICreditsJob.<SUICreditsJob>CreatedDate().Descending())) {
                jobList.add(AdministrationTranslator.getSAPSUICreditsJob(suiCreditsJob));
            }
            return jobList;

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding current SUI Credit jobs.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public void createSUICreditsJob(SAPQuarter quarter, String paymentTemplateCd) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(quarter.getYear(), quarter.getQuarter(), paymentTemplateCd));
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();

                scheduleJob(BatchJobType.SUICreditsBatchJob.name(), null);
            } else {
                aeFactory.throwGenericException("Error creating SUI Credits Job.", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating SUI Credits Job.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RemoveFromSignupFraudHold)
    public void processBulkDDLimitUpdates(String fileContents) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            logger.info("Processing DD Limits File:\n" + fileContents);

            CsvReader reader = new CsvReader(new StringReader(fileContents));

            reader.readHeaders();

            List<ProcessResult> prList = new ArrayList<ProcessResult>();
            while (reader.readRecord()) {
                String psid = reader.get("PSID");
                String erLimitString = reader.get("Recommended ER Limit");
                String eeLimitString = reader.get("Recommended EE Limit");
                SpcfMoney erLimit = new SpcfMoney(erLimitString.replaceAll("[$, ]", ""));
                SpcfMoney eeLimit = new SpcfMoney(eeLimitString.replaceAll("[$, ]", ""));

                prList.add(PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT, psid, erLimit, eeLimit));
            }

            if (prList.isEmpty()) {
                aeFactory.throwGenericException("Found no records to process");
            }

            if (!aeFactory.errorsOccurred(prList)) {
                logger.info("Starting to commit DD limits");
                PayrollServices.commitUnitOfWork();
                logger.info("Completed updating DD limits");
            } else {
                aeFactory.throwGenericException("Error updating DD limits.", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating DD limits.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerOperations)
    public void deleteLedgerOperationJob(String jobId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult processResult = PayrollServices.batchJobManager.deleteLedgerOperationJob(SpcfUniqueId.createInstance(jobId));
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error deleting ledger operation job.", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error deleting ledger operation job.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static String scheduleJob(String jobName, SpcfCalendar timerExpression, String... jobInstanceParameters) throws Exception {
        Boolean USE_FLUX = SystemParameter.findBooleanValue(SystemParameter.Code.SCHEDULE_SAP_BATCHJOBS_USING_FLUX, Boolean.FALSE);
        logger.info("USE_FLUX : " + USE_FLUX);
        logger.info("TimerExpression : " + timerExpression);
        String jobId = null;

        //Adding the rollback flag, calling the FLUX BatchJobManager if flag is ON
        if (USE_FLUX) {
            String timerExpressionFromSpcfCalendar = null;
            if (timerExpression != null)
                timerExpressionFromSpcfCalendar = FluxUtils.getTimerExpressionFromSpcfCalendar(timerExpression);
            BatchJobManager batchJobManager = new BatchJobManager();
            jobId = batchJobManager.scheduleJob(BatchJobType.valueOf(jobName), jobInstanceParameters.toString(), timerExpressionFromSpcfCalendar);
        } else {
            String when = null;
            if (timerExpression != null)
                when = timerExpression.format(SCHEDULE_DATE_FORMAT);
            jobId = JSSGateway.scheduleJob(jobName, when, jobInstanceParameters);
        }
        return jobId;
    }

}
