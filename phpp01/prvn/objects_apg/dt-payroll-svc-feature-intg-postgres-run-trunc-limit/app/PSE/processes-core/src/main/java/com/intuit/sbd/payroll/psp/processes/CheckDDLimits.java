/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/CheckDDLimits.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.intuit.pmo.client.model.DirectDepositDate;
import com.intuit.pmo.client.model.LimitCheckError;
import com.intuit.psp.dd.pojo.LimitResponse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.AutoLimitIncreaseTier;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountOwnerType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.DDCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.EventLimitCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.LimitRule;
import com.intuit.sbd.payroll.psp.domain.LimitValue;
import com.intuit.sbd.payroll.psp.domain.LimitValueType;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.psp.dd.exception.DateMismatchException;
import com.intuit.sbg.psp.dd.limitcheck.LimitCheckHelper;
import com.intuit.sbg.psp.dd.limitcheck.PSPDirectDepositDateHelper;
import com.intuit.sbg.psp.dd.util.LimitCheckResponse;
import com.intuit.sbg.psp.dd.util.LimitConverter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.time.StopWatch;

/**
 * DD process for submitting transactions
 *
 * @author Dawn Martens
 */
public class CheckDDLimits extends Process implements IProcess {

    public static SpcfLogger logger = SpcfLogManager.getLogger(CheckDDLimits.class);

    //Inputs
    private PayrollRunDTO payrollRunDTO;
    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;

    private Company company;
    private DDCompanyServiceInfo ddCompanyServiceInfo;
    private PayrollRunLimitInfo payrollRunLimitInfo;

    private boolean qualifiesForAutoIncrease;
    AutoLimitIncreaseTier autoIncreaseTier;
    private LimitRule mLimitRule;

    private LimitCheckHelper ddlimitCheck=new LimitCheckHelper();
    private LimitResponse ddLimitResponse;


    private static final String PAYROLLS_IN_MEMORY_CACHE_KEY = "PayrollInMemoryFromPayrollSubmitCore";
    private static final String DDPAYROLLS_IN_MEMORY_CACHE_KEY = "DDPayrollInMemoryFromPayrollSubmitCore";

    //required for saving dd settlement date and initiation date
    private PayrollRun payrollRun;

    public void setPayrollRun(PayrollRun pPayrollRun) {
        payrollRun = pPayrollRun;
    }

    //this is a test
    public CheckDDLimits(PayrollRunDTO pPayrollRunDTO, SourceSystemCode pSourceSystem, String pSourceCompanyId) {
        sourceCompanyId = pSourceCompanyId;
        sourceSystemCd = pSourceSystem;

        payrollRunDTO = pPayrollRunDTO;
        payrollRunLimitInfo = new PayrollRunLimitInfo();
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Check if company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if company exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        //This should never fail, as it should be caught by DD common validation
        ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);
        mLimitRule = LimitRule.findLimitRule(ddCompanyServiceInfo.getCompany(), ddCompanyServiceInfo.getService().getServiceCd());

        // override dd limits check if company is on prefunding hold
        if(!company.isCompanyHold(ServiceSubStatusCode.PendingPrefundingWire)){
            //Check DD Limits
            if(!company.isPhase2DDMigrated()){


                //for the Phase 1 : we do limit check and validations of settlement dates
                //, However only log results
                //This needs to be removed in case of complete migration to
                //Phase 2
                if(company.isPhase1DDMigrated() && company.getDDPublishFlag()){

                    ddLimitResponse = ddlimitCheck.isValidDDLimit(payrollRunDTO.getTargetPayrollTXDate(), company);
                }

                violatesDDLimits(payrollRunDTO, payrollRunLimitInfo);

                //If the company violated DD, limits do processing
                if (payrollRunLimitInfo.violatesLimits) {
                    SpcfMoney maxCompanySum = new SpcfMoney("0.00");
                    SpcfMoney maxEmployeeSum = getMaxEmployeeAmount();
                    if (payrollRunLimitInfo.maxCompanySum != null) {
                        maxCompanySum = payrollRunLimitInfo.maxCompanySum;
                    }

                    qualifiesForAutoIncrease = qualifiesForAutoLimitIncrease(ddCompanyServiceInfo,
                            maxCompanySum, maxEmployeeSum);
                    if (!qualifiesForAutoIncrease) {
                        ProcessResult pr = PayrollServices.executeTransactionThread(new TransactionThread() {
                            public ProcessResult transaction() {
                                return processFailedDDLimits(CheckDDLimits.this);
                            }
                        });
                        validationResult.merge(pr);
                    }
                }
            } else {
                //if company is migrated

                ddLimitResponse=ddlimitCheck.isValidDDLimit(payrollRunDTO.getTargetPayrollTXDate(), company);
                if(!ddLimitResponse.getLimitCheckResponse().equals(LimitCheckResponse.PASS)){
                    ProcessResult pr = PayrollServices.executeTransactionThread(new TransactionThread() {
                        public ProcessResult transaction() {
                            return processFailedDDLimitsForDDMigrated(CheckDDLimits.this,ddLimitResponse);
                        }
                    });
                    validationResult.merge(pr);
                }

            }
        }

        return validationResult;
    }

    /**
     * In order to see if a company qualifies for an increase, we must make use of the "maximum employee amount"
     * This amount is determined to be the greater of either the maximum excceded dd limit for a particular employee
     * or a particular bank account.
     *
     * @return Either the maximum bank account sum or the maximum employee sum, depending on which is greater
     */
    private SpcfMoney getMaxEmployeeAmount() {
        SpcfMoney maxEmployeeSum = new SpcfMoney("0.00");
        if (payrollRunLimitInfo.maxEmployeeSum != null && payrollRunLimitInfo.maxBankAccountSum != null) {
            if (payrollRunLimitInfo.maxEmployeeSum.compareTo(payrollRunLimitInfo.maxBankAccountSum) > 0) {
                maxEmployeeSum = payrollRunLimitInfo.maxEmployeeSum;
            }
            else {
                maxEmployeeSum = payrollRunLimitInfo.maxBankAccountSum;
            }
        }
        else if (payrollRunLimitInfo.maxEmployeeSum == null && payrollRunLimitInfo.maxBankAccountSum != null) {
            maxEmployeeSum = payrollRunLimitInfo.maxBankAccountSum;
        }
        else if (payrollRunLimitInfo.maxBankAccountSum == null && payrollRunLimitInfo.maxEmployeeSum != null) {
            maxEmployeeSum = payrollRunLimitInfo.maxEmployeeSum;
        }
        return maxEmployeeSum;
    }

    private SpcfMoney min(SpcfMoney val1, SpcfMoney val2) {
        if (val1.compareTo(val2) <= 0)
            return val1;
        else
            return val2;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if((company.isPhase1DDMigrated() && company.getDDPublishFlag()) || company.isPhase2DDMigrated() ){
            DirectDepositDate ddLimitCheckDate = ddLimitResponse.getDDLimitCheckDate();
            if(ddLimitCheckDate != null) {
                logger.debug("CheckDDLimits:Directdeposit dates are valid");
                payrollRun.setDebitSettlementDate(LimitConverter.convertLocalTimeToSpcfCalendar(ddLimitCheckDate.getDebitSettlementDate()));
                payrollRun.setInitiationDate(LimitConverter.convertDateTimeToSpcfCalendar(ddLimitCheckDate.getInitiationDate()));
                Application.save(payrollRun);
            }
            else{
                logger.debug("CheckDDLimits:Directdeposit dates are null");

                PSPDirectDepositDateHelper pspDDDateHelper = new PSPDirectDepositDateHelper();
                try {
                    DirectDepositDate pspDate = pspDDDateHelper.pspSettlementDate(company, payrollRunDTO.getTargetPayrollTXDate());
                    if(pspDate != null) {
                        payrollRun.setDebitSettlementDate(LimitConverter.convertLocalTimeToSpcfCalendar(pspDate.getDebitSettlementDate()));
                        payrollRun.setInitiationDate(LimitConverter.convertDateTimeToSpcfCalendar(pspDate.getInitiationDate()));
                        Application.save(payrollRun);
                    }
                }
                catch(DateMismatchException dateMismatchException){
                    logger.warn("LimitCheckHelper:isValidDDLimit the PSP and DD dates dont match for company:"+ company.getSourceCompanyId()
                            +" with exception:"+dateMismatchException.getMessage());
                }
            }

        }

        //If the company qualified for an auto-limit increase, update the company and/or employee limits as applicable
        if (qualifiesForAutoIncrease) {
            if (payrollRunLimitInfo.violatesCompanyLimits) {
                SpcfMoney oldERLimit = getDDCompanyLimitAmount(ddCompanyServiceInfo);
                SpcfMoney autoERLimit = new SpcfMoney(payrollRunLimitInfo.maxCompanySum.multiply(autoIncreaseTier.getIncreaseMultiplier()));

                // enforce auto increase cap rules
                SpcfMoney newERLimit = min(autoERLimit, autoIncreaseTier.getCompanyCap());

                ddCompanyServiceInfo.setOverrideCompanyLimitAmount(newERLimit);

                CompanyEvent.createLimitIncreaseEvent(company,
                                                     EventLimitCode.Company,
                                                     payrollRunDTO.getPayrollTXBatchId(),
                                                     null,
                                                     newERLimit,
                                                     oldERLimit,
                                                     PSPDate.getPSPTime());
            }

            if (payrollRunLimitInfo.violatesEmployeeBankAccountLimits || payrollRunLimitInfo.violatesEmployeeLimits) {
                SpcfMoney oldEELimit = getDDEmployeeLimitAmount(ddCompanyServiceInfo);
                SpcfMoney eeMax = getMaxEmployeeAmount();
                SpcfMoney autoEELimit = new SpcfMoney(eeMax.multiply(autoIncreaseTier.getIncreaseMultiplier()));

                // enforce auto increase cap rules
                SpcfMoney newEELimit = min(autoEELimit, autoIncreaseTier.getPayeeCap());

                ddCompanyServiceInfo.setOverrideEmployeeLimitAmount(newEELimit);

                CompanyEvent.createLimitIncreaseEvent(company,
                                                     EventLimitCode.Employee,
                                                     payrollRunDTO.getPayrollTXBatchId(),
                                                     payrollRunLimitInfo.limitViolatingEmployee,
                                                     newEELimit,
                                                     oldEELimit,
                                                     PSPDate.getPSPTime());
            }

        }

        ddCompanyServiceInfo.setConsecutiveLimitViolationCount(new Long("0"));

        ddCompanyServiceInfo = Application.save(ddCompanyServiceInfo);

        //TODO:v1.1 Update Company.setHasEnoughPayrolls... to true (so the payroll run qualification query is
        //not executed again). Not adding this for v1 because unit tests that clean up payroll runs and strikes
        //but do not clean up this flag accordingly will break
        return processResult;
    }

    // This method can be called from a different unit of work
    protected ProcessResult processFailedDDLimits(CheckDDLimits pCheckDDLimits) {
        ProcessResult processResult = new ProcessResult();
        Company company = Application.findById(Company.class, pCheckDDLimits.company.getId());
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);
        PayrollRunDTO payrollRunDTO = pCheckDDLimits.payrollRunDTO;
        PayrollRunLimitInfo payrollRunLimitInfo = pCheckDDLimits.payrollRunLimitInfo;

        Employee limitViolatingEmployee = null;
        if (payrollRunLimitInfo.limitViolatingEmployee != null) {
            limitViolatingEmployee = Application.findById(Employee.class, payrollRunLimitInfo.limitViolatingEmployee.getId());
        }

        //Store DD Limit Violation Company Event(s)
        if (payrollRunLimitInfo.violatesEmployeeBankAccountLimits && !payrollRunLimitInfo.violatesEmployeeLimits) {
            CompanyEvent.createLimitViolationEvent(company,
                                                  EventLimitCode.BankAccount,
                                                  payrollRunDTO.getPayrollTXBatchId(),
                                                  payrollRunLimitInfo.limitViolatingBAAccountNum,
                                                  payrollRunLimitInfo.limitViolatingBARoutingNum,
                                                  limitViolatingEmployee,
                                                  getDDEmployeeLimitAmount(ddCompanyServiceInfo),
                                                  payrollRunLimitInfo.maxBankAccountSum,
                                                  PSPDate.getPSPTime());
        }

        if (payrollRunLimitInfo.violatesEmployeeLimits) {
            CompanyEvent.createLimitViolationEvent(company,
                                                  EventLimitCode.Employee,
                                                  payrollRunDTO.getPayrollTXBatchId(),
                                                  null, null,
                                                  limitViolatingEmployee,
                                                  getDDEmployeeLimitAmount(ddCompanyServiceInfo),
                                                  payrollRunLimitInfo.maxEmployeeSum,
                                                  PSPDate.getPSPTime());
        }

        if (payrollRunLimitInfo.violatesCompanyLimits) {
            CompanyEvent.createLimitViolationEvent(company,
                                                  EventLimitCode.Company,
                                                  payrollRunDTO.getPayrollTXBatchId(),
                                                  null, null, null,
                                                  getDDCompanyLimitAmount(ddCompanyServiceInfo),
                                                  payrollRunLimitInfo.maxCompanySum,
                                                  PSPDate.getPSPTime());
        }

        //carry out limit violations- suspend the company
        limitViolations( company, ddCompanyServiceInfo );

        //Limit exceeded message
        processResult.getMessages()
                .PayrollRunExceedsDDLimits(EntityName.PayrollRun, payrollRunDTO.getPayrollTXBatchId(),
                        company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                        payrollRunDTO.getPayrollTXBatchId());
        return processResult;
    }

    protected ProcessResult processFailedDDLimitsForDDMigrated(CheckDDLimits pCheckDDLimits, LimitResponse ddLimitResponse) {
        ProcessResult processResult = new ProcessResult();
        Company company = Application.findById(Company.class, pCheckDDLimits.company.getId());
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);
        PayrollRunDTO payrollRunDTO = pCheckDDLimits.payrollRunDTO;

        CompanyEvent.createDDLimitViolationEvent(company,
                EventLimitCode.Company,
                payrollRunDTO.getPayrollTXBatchId(),
                PSPDate.getPSPTime());

        //carry out limit violations- suspend the company
        limitViolations(company, ddCompanyServiceInfo);

        //Limit exceeded message
        if(ddLimitResponse.getLimitCheckResponse().equals(LimitCheckResponse.FAIL)){
            List<LimitCheckError> errorList = ddLimitResponse.getErrorMessages();
            if (errorList != null && !errorList.isEmpty()) {
                for (Iterator iterator = errorList.iterator(); iterator.hasNext(); ) {
                    LimitCheckError error = (LimitCheckError) iterator.next();
                    if (error != null) {
                        //If error code in the response is either 101, 102
                        if(error.getCode().equals(101)) {//company exceeds
                            CompanyEvent.createDDLimitViolationEvent(company,
                                    EventLimitCode.Company,
                                    payrollRunDTO.getPayrollTXBatchId(),
                                    PSPDate.getPSPTime());
                        }else if(error.getCode().equals(102)) {//payee exceeds
                            CompanyEvent.createDDLimitViolationEvent(company,
                                    EventLimitCode.Employee,
                                    payrollRunDTO.getPayrollTXBatchId(),
                                    PSPDate.getPSPTime());
                        }
                    }
                }
            }
            processResult.getMessages()
                    .PayrollRunExceedsDDLimits(EntityName.PayrollRun, payrollRunDTO.getPayrollTXBatchId(),
                            company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                            payrollRunDTO.getPayrollTXBatchId());
        }else if(ddLimitResponse.getLimitCheckResponse().equals(LimitCheckResponse.PARTIAL)){
            processResult.getMessages()
                    .PayrollRunExceedsDDLimits(EntityName.PayrollRun, payrollRunDTO.getPayrollTXBatchId(),
                            company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                            payrollRunDTO.getPayrollTXBatchId());
        }else{//validation errors
            processResult.getMessages()
                    .PayrollRunExceedsDDLimits(EntityName.PayrollRun, payrollRunDTO.getPayrollTXBatchId(),
                            company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                            payrollRunDTO.getPayrollTXBatchId());
        }

        return processResult;
    }

    protected void limitViolations( Company company, DDCompanyServiceInfo ddCompanyServiceInfo){
        //Increment the number of consecutive limit violations
        Long currentLimitViolationsCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        Long newLimitViolationCount = currentLimitViolationsCount + 1;
        ddCompanyServiceInfo.setConsecutiveLimitViolationCount(newLimitViolationCount);

        logger.info("The currentLimitViolationsCount is:"+currentLimitViolationsCount+" The newLimitViolationCount:"+newLimitViolationCount);
        //If the counter exceeds the maximum allowable consecutive limit violations, update company status to suspended
        // get the duration of days from the source system val table
        Long lMaxConsecutiveLimitViolations = Long.parseLong(getLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.ConsecutiveLimitViolationLimit).getValue());
        if (newLimitViolationCount.compareTo(lMaxConsecutiveLimitViolations) > 0) {
            logger.warn("The company:"+ company.getSourceCompanyId()+" is put on Hold");
            PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.DirectDepositLimit);
        }

        ddCompanyServiceInfo = Application.save(ddCompanyServiceInfo);
        company = Application.save(company);
    }

    //Helper methods
    protected void violatesDDLimits(PayrollRunDTO pPayrollRun, PayrollRunLimitInfo pPayrollRunLimitInfo) {
        companyViolatesDDLimits(pPayrollRun, pPayrollRunLimitInfo);
        anyEmployeeViolatesDDLimits(pPayrollRun, pPayrollRunLimitInfo);
        anyBankAccountViolatesDDLimits(pPayrollRun, pPayrollRunLimitInfo);
    }


    /**
     * If we consider all payrolls in the range [thisPayrollCheckDate-fraudWindowDays .. thisPayrollCheckDate+fraudWindowDays]
     * and we take each rolling window from [thisPayrollCheckDate-fraudWindowDays .. thisPayrollCheckDate]
     * and we accumulate the amounts paid to each bank account during each rolling window,
     * no total per bank account can be more than the source payroll parameter DDEmployeeLimitAmount
     *
     * @param pPayrollRun
     * @param pPayrollRunLimitInfo
     */
    protected void anyBankAccountViolatesDDLimits(PayrollRunDTO pPayrollRun,
                                                  PayrollRunLimitInfo pPayrollRunLimitInfo) {
        HashMap<String, SpcfMoney> amountPerBAForPayroll = pPayrollRun.getNetAmountPerBankAccount();
        HashMap<String, String> employeeForBA = pPayrollRun.getEmployeeIdPerBankAccountMap();
        SpcfCalendar procCheckDate = company.getNextValidPaycheckDepositDate(DateDTO.convertToSpcfCalendar(pPayrollRun.getTargetPayrollTXDate()));
//        SpcfCalendar procCheckDate = DateDTO.convertToSpcfCalendar(pPayrollRun.getTargetPayrollTXDate());

        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);
        SpcfMoney ddLimitForEmployeeBa = getDDEmployeeLimitAmount(ddCompServiceInfo);

        //Get the aggregate sum per bank account, per paycheck date for other payrolls within the fraud check date range
        HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>> amountPerBankAccountPerPayCheckDate = getAmountsPerBankAccountPerPayCheckDate(procCheckDate);

        for (String currBankAccountKey : amountPerBAForPayroll.keySet()) {
            SpcfMoney amountForBankAccount = amountPerBAForPayroll.get(currBankAccountKey);
            String[] keyFields = currBankAccountKey.split(":");
            String currRoutingNumber = keyFields[0];
            String currAccountNumber = keyFields[1];

            TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = getAmountsPerPayCheckDate(amountPerBankAccountPerPayCheckDate, currBankAccountKey, procCheckDate);

            //Get the amount for each range within the entire range
            for (SpcfCalendar currCheckDate : amountsPerPayCheckDate.keySet()) {
                if (currCheckDate.before(procCheckDate) || currCheckDate.equals(procCheckDate)) {
                    SpcfMoney amountForRange = accumulateToCheckDate(amountsPerPayCheckDate, currCheckDate, false);
                    amountForRange = (SpcfMoney) amountForRange.add(amountForBankAccount);
                    if (amountForRange.compareTo(ddLimitForEmployeeBa) > 0) {
                        if (pPayrollRunLimitInfo.maxBankAccountSum == null || amountForRange
                                .compareTo(pPayrollRunLimitInfo.maxBankAccountSum) > 0) {
                            pPayrollRunLimitInfo.maxBankAccountSum = amountForRange;
                            pPayrollRunLimitInfo.limitViolatingBARoutingNum = currRoutingNumber;
                            pPayrollRunLimitInfo.limitViolatingBAAccountNum = currAccountNumber;
                        }
                        Employee currEmployee = Employee.findEmployee(company, employeeForBA.get(currBankAccountKey));
                        pPayrollRunLimitInfo.limitViolatingEmployee = currEmployee;
                        pPayrollRunLimitInfo.violatesEmployeeBankAccountLimits = true;
                        pPayrollRunLimitInfo.violatesLimits = true;
                    }
                }
            }
        }
    }

    /**
     * If we consider all payrolls in the range [thisPayrollCheckDate-fraudWindowDays .. thisPayrollCheckDate+fraudWindowDays]
     * and we take each rolling window from [thisPayrollCheckDate-fraudWindowDays .. thisPayrollCheckDate]
     * and we accumulate the amounts paid to each employee during each rolling window,
     * no total per employee can be more than the source payroll parameter DDEmployeeLimitAmount
     *
     * @param pPayrollRun
     * @param pPayrollRunLimitInfo
     */
    protected void anyEmployeeViolatesDDLimits(PayrollRunDTO pPayrollRun,
                                               PayrollRunLimitInfo pPayrollRunLimitInfo) {
        SpcfCalendar procCheckDate = company.getNextValidPaycheckDepositDate(DateDTO.convertToSpcfCalendar(pPayrollRun.getTargetPayrollTXDate()));
//        SpcfCalendar procCheckDate = DateDTO.convertToSpcfCalendar(pPayrollRun.getTargetPayrollTXDate());

        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);
        SpcfMoney ddLimitForEmployee = getDDEmployeeLimitAmount(ddCompServiceInfo);

        //Get the net amounts for all the employees in the payroll
        HashMap<String, SpcfMoney> amountsPerEmployeeForPayroll = pPayrollRun.getNetAmountPerEmployee();
        Set<String> employeesInPayroll = amountsPerEmployeeForPayroll.keySet();

        //Get the aggregate sum per employee, per paycheck date for other payrolls within the fraud check date range
        HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> amountPerEmployeePerPayCheckDate = getAmountsPerEmployeePerPayCheckDate(procCheckDate);

        //Examine all the employees for the payroll
        for (String currSPSEEId : employeesInPayroll) {
            Employee currEmployee = Employee.findEmployee(company, currSPSEEId);
            SpcfMoney employeeAmount = amountsPerEmployeeForPayroll.get(currSPSEEId);

            TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = getAmountsPerPayCheckDate(amountPerEmployeePerPayCheckDate, currEmployee.getId(), procCheckDate);

            //Get the amount for each range within the entire range
            for (SpcfCalendar currCheckDate : amountsPerPayCheckDate.keySet()) {
                if (currCheckDate.before(procCheckDate) || currCheckDate.equals(procCheckDate)) {
                    SpcfMoney amountForRange = accumulateToCheckDate(amountsPerPayCheckDate, currCheckDate, false);

                    //Add the amount for this employee for this particular payroll to the found amount
                    amountForRange = (SpcfMoney) amountForRange.add(employeeAmount);
                    if (amountForRange.compareTo(ddLimitForEmployee) > 0) {
                        if (pPayrollRunLimitInfo.maxEmployeeSum == null || amountForRange
                                .compareTo(pPayrollRunLimitInfo.maxEmployeeSum) > 0) {
                            pPayrollRunLimitInfo.maxEmployeeSum = amountForRange;
                            pPayrollRunLimitInfo.limitViolatingEmployee = currEmployee;
                        }
                        pPayrollRunLimitInfo.violatesEmployeeLimits = true;
                        pPayrollRunLimitInfo.violatesLimits = true;
                    }
                }
            }

        }
    }

     /**
      * If we consider all payrolls in the range [thisPayrollCheckDate-fraudWindowDays .. thisPayrollCheckDate+fraudWindowDays]
      * and we take each rolling window from [thisPayrollCheckDate-fraudWindowDays .. thisPayrollCheckDate]
      * and we accumulate the amounts paid during each rolling window,
      * no total can be more than the source payroll parameter DDCompanyLimitAmount

     * @param pPayrollRun
     * @param pPayrollRunLimitInfo
     */
    protected void companyViolatesDDLimits(PayrollRunDTO pPayrollRun, PayrollRunLimitInfo pPayrollRunLimitInfo) {

        SpcfCalendar procCheckDate = company.getNextValidPaycheckDepositDate(DateDTO.convertToSpcfCalendar(pPayrollRun.getTargetPayrollTXDate()));
        SpcfMoney payrollNetAmount = pPayrollRun.getPayrollDirectDepositAmount();

        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);
        SpcfMoney ddLimitForCompany = getDDCompanyLimitAmount(ddCompServiceInfo);

        //Get the aggregate sum per employee, per paycheck date for other payrolls within the fraud check date range
        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = getAmountsPerPayCheckDate(procCheckDate);

        //Get the amount for each range within the entire range
        for (SpcfCalendar currCheckDate : amountsPerPayCheckDate.keySet()) {
            if (currCheckDate.before(procCheckDate) || currCheckDate.equals(procCheckDate)) {
                SpcfMoney amountForRange = accumulateToCheckDate(amountsPerPayCheckDate, currCheckDate, true);

                //Add the current payroll net amount to the amount for the entire range
                amountForRange = (SpcfMoney) amountForRange.add(payrollNetAmount);
                if (amountForRange.compareTo(ddLimitForCompany) > 0) {
                    if (pPayrollRunLimitInfo.maxCompanySum == null || amountForRange
                            .compareTo(pPayrollRunLimitInfo.maxCompanySum) > 0) {
                        pPayrollRunLimitInfo.maxCompanySum = amountForRange;
                    }
                    pPayrollRunLimitInfo.violatesCompanyLimits = true;
                    pPayrollRunLimitInfo.violatesLimits = true;
                }
            }
        }
    }


    private TreeMap<SpcfCalendar, SpcfMoney> getAmountsPerPayCheckDate(HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> pAmountPerEmployeePerPayCheckDate, SpcfUniqueId pEmployeeId, SpcfCalendar pBaseCheckDate) {
        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = pAmountPerEmployeePerPayCheckDate.get(pEmployeeId);
        if (amountsPerPayCheckDate == null) {
            amountsPerPayCheckDate = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
        }

        //Add current paycheck date if not already there
        if (!amountsPerPayCheckDate.containsKey(pBaseCheckDate)) {
            amountsPerPayCheckDate.put(pBaseCheckDate, new SpcfMoney("0.0"));
        }

        return amountsPerPayCheckDate;
    }

    private TreeMap<SpcfCalendar, SpcfMoney> getAmountsPerPayCheckDate(HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>> pAmountPerBankAccountPerPayCheckDate, String pBankAccountKey, SpcfCalendar pBaseCheckDate) {
        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = pAmountPerBankAccountPerPayCheckDate.get(pBankAccountKey);
        if (amountsPerPayCheckDate == null) {
            amountsPerPayCheckDate = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
        }

        //Add current paycheck date if not already there
        if (!amountsPerPayCheckDate.containsKey(pBaseCheckDate)) {
            amountsPerPayCheckDate.put(pBaseCheckDate, new SpcfMoney("0.0"));
        }

        return amountsPerPayCheckDate;
    }    

    private SpcfMoney accumulateToCheckDate(TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate, SpcfCalendar pInitialCheckDate, boolean pIsCompanyLimitViolateChk) {
        SpcfCalendar finalCheckDate = pInitialCheckDate.copy();
        if(pIsCompanyLimitViolateChk){
            finalCheckDate.addDays(getDDCompanyLimitDuration(ddCompanyServiceInfo));
        }else{
            finalCheckDate.addDays(getDDEmployeeLimitDuration(ddCompanyServiceInfo));
        }


        SpcfMoney accumulatedAmount = new SpcfMoney("0.0");

        for (Map.Entry<SpcfCalendar, SpcfMoney> amountOnDate : amountsPerPayCheckDate.entrySet()) {
            if ((amountOnDate.getKey().before(finalCheckDate) || amountOnDate.getKey().equals(finalCheckDate)) &&
                    (amountOnDate.getKey().after(pInitialCheckDate) || amountOnDate.getKey().equals(pInitialCheckDate))) {
                accumulatedAmount = (SpcfMoney)accumulatedAmount.add(amountOnDate.getValue());
            }
        }

        return accumulatedAmount;
    }

    /**
     * Get the aggregate sum per paycheck date for existing payrolls within the fraud check date range
     *
     * @param pBaseCheckDate
     * @return
     */
    private TreeMap<SpcfCalendar, SpcfMoney> getAmountsPerPayCheckDate(SpcfCalendar pBaseCheckDate) {
        int intHalfEeLimitDuration = getDDCompanyLimitDuration(ddCompanyServiceInfo);

        String[] paramNames = {"companyId", "fromDate", "toDate", "txnType", "txnState"};
        Object[] paramValues = new Object[5];

        paramValues[0] = company.getId();

        //from date
        SpcfCalendar fromCalendar = pBaseCheckDate.copy();
        fromCalendar.addDays(-intHalfEeLimitDuration);
        paramValues[1] = fromCalendar;

        //to date
        SpcfCalendar toCalendar = pBaseCheckDate.copy();
        toCalendar.addDays(intHalfEeLimitDuration);
        paramValues[2] = toCalendar;

        paramValues[3] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[4] = TransactionStateCode.Cancelled;

        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
        List<Object> retList = Application.executeNamedQuery("SumAmountGroupByPayCheckDate", paramNames, paramValues);

        for (Object row : retList) {
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[0];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[1];

            amountsPerPayCheckDate.put(checkDate, checkAmount);
        }

        //Add current paycheck date if not already there
        if (!amountsPerPayCheckDate.containsKey(pBaseCheckDate)) {
            amountsPerPayCheckDate.put(pBaseCheckDate, new SpcfMoney("0.0"));
        }

        //
        // Now we need to add payrolls that are being processed (currently in the cache)
        //
        ArrayList<PayrollRunDTO> payrollsInMemory = getPayrollsInMemory(company);
        for (PayrollRunDTO payrollInMemory : payrollsInMemory) {
            SpcfCalendar checkDate = DateDTO.convertToSpcfCalendar(payrollInMemory.getTargetPayrollTXDate());
            SpcfMoney payrollNetAmount = payrollInMemory.getPayrollDirectDepositAmount();

            // Get the amount that needs to be added to
            SpcfMoney amountOnDate = amountsPerPayCheckDate.get(checkDate);
            if (amountOnDate == null) {
                amountOnDate = new SpcfMoney("0.00");
            }
            amountOnDate = (SpcfMoney) amountOnDate.add(payrollNetAmount);

            // Write back the accumulated amount to checkDateAmountMap
            amountsPerPayCheckDate.put(checkDate, amountOnDate);
        }


        return amountsPerPayCheckDate;
    }

    /**
     * Get the aggregate sum per employee, per paycheck date for existing payrolls within the fraud check date range
     *
     * @param pBaseCheckDate
     * @return
     */
    private HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> getAmountsPerEmployeePerPayCheckDate(SpcfCalendar pBaseCheckDate) {
        //
        // First add all payrolls that are in the database
        //
        int intHalfEeLimitDuration = getDDEmployeeLimitDuration(ddCompanyServiceInfo);

        String[] paramNames = {"companyId", "fromDate", "toDate", "txnType", "txnState"};
        Object[] paramValues = new Object[5];

        paramValues[0] = company.getId();

        //from date
        SpcfCalendar fromCalendar = pBaseCheckDate.copy();
        fromCalendar.addDays(-intHalfEeLimitDuration);
        paramValues[1] = fromCalendar;

        //to date
        SpcfCalendar toCalendar = pBaseCheckDate.copy();
        toCalendar.addDays(intHalfEeLimitDuration);
        paramValues[2] = toCalendar;

        paramValues[3] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[4] = TransactionStateCode.Cancelled;

        HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> amountsPerEmployeePerPayCheckDate = new HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>>();
        List<Object> retList = Application.executeNamedQuery("SumAmountGroupByPayCheckDateAndEmployee", paramNames, paramValues);

        for (Object row : retList) {
            SpcfUniqueId employeeId = (SpcfUniqueId) ((Object[]) row)[0];
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[1];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[2];

            TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerEmployeePerPayCheckDate.get(employeeId);
            if (checkDateAmountMap == null) {
                checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                amountsPerEmployeePerPayCheckDate.put(employeeId, checkDateAmountMap);
            }
            checkDateAmountMap.put(checkDate, checkAmount);
        }

        //
        // Now we need to add payrolls that are being processed (currently in the cache)
        //
        ArrayList<PayrollRunDTO> payrollsInMemory = getPayrollsInMemory(company);
        for (PayrollRunDTO payrollInMemory : payrollsInMemory) {
            SpcfCalendar checkDate = DateDTO.convertToSpcfCalendar(payrollInMemory.getTargetPayrollTXDate());
            
            //Get the net amounts for all the employees in the payroll
            HashMap<String, SpcfMoney> amountsPerEmployeeForPayroll = payrollInMemory.getNetAmountPerEmployee();

            for (String currSPSEEId : amountsPerEmployeeForPayroll.keySet()) {
                Employee currEmployee = Employee.findEmployee(company, currSPSEEId);
                SpcfMoney employeeAmount = amountsPerEmployeeForPayroll.get(currSPSEEId);
                SpcfUniqueId employeeId = currEmployee.getId();

                // Get the checkDateAmountMap that needs to be added to
                TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerEmployeePerPayCheckDate.get(employeeId);
                if (checkDateAmountMap == null) {
                    checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                    amountsPerEmployeePerPayCheckDate.put(employeeId, checkDateAmountMap);
                }

                // Get the amount that needs to be added to
                SpcfMoney amountOnDate = checkDateAmountMap.get(checkDate);
                if (amountOnDate == null) {
                    amountOnDate = new SpcfMoney("0.00");
                }
                amountOnDate = (SpcfMoney) amountOnDate.add(amountsPerEmployeeForPayroll.get(currSPSEEId));

                // Write back the accumulated amount to checkDateAmountMap
                checkDateAmountMap.put(checkDate, amountOnDate);
            }
        }

        return amountsPerEmployeePerPayCheckDate;
    }

    /**
     * Get the aggregate sum per bank account, per paycheck date for existing payrolls within the fraud check date range
     *
     * @param pBaseCheckDate
     * @return
     */
    private HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>> getAmountsPerBankAccountPerPayCheckDate(SpcfCalendar pBaseCheckDate) {
        int intHalfEeLimitDuration = getDDEmployeeLimitDuration(ddCompanyServiceInfo);

        String[] paramNames = {"companyId", "creditBankAccountType", "fromDate", "toDate", "txnType", "txnState"};
        Object[] paramValues = new Object[6];

        paramValues[0] = company.getId();
        paramValues[1] = BankAccountOwnerType.Employee;

        //from date
        SpcfCalendar fromCalendar = pBaseCheckDate.copy();
        fromCalendar.addDays(-intHalfEeLimitDuration);
        paramValues[2] = fromCalendar;

        //to date
        SpcfCalendar toCalendar = pBaseCheckDate.copy();
        toCalendar.addDays(intHalfEeLimitDuration);
        paramValues[3] = toCalendar;

        paramValues[4] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[5] = TransactionStateCode.Cancelled;

        HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>> amountsPerBankAccountPerPayCheckDate = new HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>>();
        String namedQuery = "SumAmountGroupByPayCheckDateAndBankAccountENC";
        List<Object> retList = Application.executeNamedQuery(namedQuery, paramNames, paramValues);

        String bankAccount = null;
        for (Object row : retList) {
            if(null != row && null != ((Object[]) row)[1]) {
                bankAccount = ((Object[]) row)[1].toString();
            }
            bankAccount = EncryptionUtils.deterministicDecrypt(BankAccount.AccountNumberKeyName, bankAccount);

            String routingNumberAndBankAccountNumber = ((Object[]) row)[0].toString() + ":" + bankAccount;
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[2];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[3];

            TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerBankAccountPerPayCheckDate.get(routingNumberAndBankAccountNumber);
            if (checkDateAmountMap == null) {
                checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                amountsPerBankAccountPerPayCheckDate.put(routingNumberAndBankAccountNumber, checkDateAmountMap);
            }
            checkDateAmountMap.put(checkDate, checkAmount);
        }

        //
        // Now we need to add payrolls that are being processed (currently in the cache)
        //
        ArrayList<PayrollRunDTO> payrollsInMemory = getPayrollsInMemory(company);
        for (PayrollRunDTO payrollInMemory : payrollsInMemory) {
            SpcfCalendar checkDate = DateDTO.convertToSpcfCalendar(payrollInMemory.getTargetPayrollTXDate());

            //Get the net amounts for all the employees in the payroll
            HashMap<String, SpcfMoney> amountPerBAForPayroll = payrollInMemory.getNetAmountPerBankAccount();

            for (String currBankAccountKey : amountPerBAForPayroll.keySet()) {
                SpcfMoney amountForBankAccount = amountPerBAForPayroll.get(currBankAccountKey);

                // Get the checkDateAmountMap that needs to be added to
                TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerBankAccountPerPayCheckDate.get(currBankAccountKey);
                if (checkDateAmountMap == null) {
                    checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                    amountsPerBankAccountPerPayCheckDate.put(currBankAccountKey, checkDateAmountMap);
                }

                // Get the amount that needs to be added to
                SpcfMoney amountOnDate = checkDateAmountMap.get(checkDate);
                if (amountOnDate == null) {
                    amountOnDate = new SpcfMoney("0.00");
                }
                amountOnDate = (SpcfMoney) amountOnDate.add(amountForBankAccount);

                // Write back the accumulated amount to checkDateAmountMap
                checkDateAmountMap.put(checkDate, amountOnDate);
            }
        }
        return amountsPerBankAccountPerPayCheckDate;
    }


    /**
     * Get the employee limit "duration" for this source payroll system
     * The duration is used to calculate the range of payrolls to be considered for fraud checks
     *
     * @return
     */
    private int getDDEmployeeLimitDuration(DDCompanyServiceInfo pDDCompanyServiceInfo) {
        LimitValue limitValue = getLimitRule(pDDCompanyServiceInfo.getCompany(), pDDCompanyServiceInfo.getService().getServiceCd())
                .findLimitValueByName(LimitValueType.EmployeeLimitDuration);
        return Integer.parseInt(limitValue.getValue());
    }

    /**
     * Get the company limit amount for this company
     *
     * @param pDDCompanyServiceInfo
     * @return
     */
    protected SpcfMoney getDDCompanyLimitAmount(DDCompanyServiceInfo pDDCompanyServiceInfo) {
        SpcfMoney companyLimitAmount = pDDCompanyServiceInfo.getOverrideCompanyLimitAmount();
        if (companyLimitAmount == null) {
            LimitValue limitValue = getLimitRule(pDDCompanyServiceInfo.getCompany(), pDDCompanyServiceInfo.getService().getServiceCd())
                    .findLimitValueByName(LimitValueType.DefaultCompanyLimit);
            if(limitValue != null) {
                companyLimitAmount = new SpcfMoney(limitValue.getValue());
            }
        }

        return companyLimitAmount;
    }

    protected SpcfMoney getDDEmployeeLimitAmount(DDCompanyServiceInfo pDDCompanyServiceInfo) {
        SpcfMoney employeeLimitAmount = pDDCompanyServiceInfo.getOverrideEmployeeLimitAmount();
        if (employeeLimitAmount == null) {
            LimitValue limitValue = getLimitRule(pDDCompanyServiceInfo.getCompany(), pDDCompanyServiceInfo.getService().getServiceCd())
                    .findLimitValueByName(LimitValueType.DefaultEmployeeLimit);
            if(limitValue != null) {
                employeeLimitAmount = new SpcfMoney(limitValue.getValue());
            }
        }
        return employeeLimitAmount;
    }

    protected boolean qualifiesForAutoLimitIncrease(DDCompanyServiceInfo pDdCompanyServiceInfo,
                                                    SpcfMoney pTotalCompanySum, SpcfMoney pTotalEmployeeSum) {

        Company domainCompany = pDdCompanyServiceInfo.getCompany();
        SpcfUniqueId companyId = domainCompany.getId();

        /*
         * Qualification: condition 1
         * The company meets the qualifications of number of payrolls run and months on service for
         * an auto-increase tier
         */

        List<AutoLimitIncreaseTier> autoLimitIncreaseTiers = mLimitRule.getAutoLimitIncreaseTiers();

        // apply the highest tier company qualifies for
        autoIncreaseTier = null;
        for (AutoLimitIncreaseTier tier : autoLimitIncreaseTiers) {
            long payrollRunCount = getQualifyingPayrollRunCount(domainCompany, tier.getDaysSinceFirstPayroll());
            if (payrollRunCount >= tier.getPayrollsRun()) {
                autoIncreaseTier = tier;
                break;
            }
        }

        if (autoIncreaseTier == null) {
            return false;
        }

        /*
         * Qualification: condition 2
         * The company has no CURRENT strikes (within the last 12
         * months)
        */
        if (CompanyEvent.hasActiveStrikeEventWithinLastYear(company)) {
            return false;
        }

        /*
         * Qualification: condition 3
         */
        boolean bCompanySumLessThan2xCurrLim = pTotalCompanySum
                .compareTo(getDDCompanyLimitAmount(pDdCompanyServiceInfo).multiply(new SpcfDecimalImpl(2))) <= 0;
        boolean bCompanySumLessThanMaxThreshhold = pTotalCompanySum.compareTo(autoIncreaseTier.getCompanyCap()) <= 0;

        boolean bEmployeeSumLessThan2xCurrLim = pTotalEmployeeSum
                .compareTo(getDDEmployeeLimitAmount(pDdCompanyServiceInfo).multiply(new SpcfDecimalImpl(2))) <= 0;
        boolean bEmployeeSumLessThanMaxThreshold = pTotalEmployeeSum.compareTo(autoIncreaseTier.getPayeeCap()) <= 0;

        return bCompanySumLessThan2xCurrLim
                && bCompanySumLessThanMaxThreshhold
                && bEmployeeSumLessThan2xCurrLim
                && bEmployeeSumLessThanMaxThreshold;

    }

    private long getQualifyingPayrollRunCount(Company company, int daysOnService) {
        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];
        paramNames[0] = "companyId";
        paramValues[0] = company.getId().toString();

        paramNames[1] = "eventTypeCd";
        paramValues[1] = EventTypeCode.FirstPayrollReceived;

        Integer minDaysInPastForPayRunDate = -1 * daysOnService;

        paramNames[2] = "earliestPayrollRunDateMin";
        SpcfCalendar earliestPayrollRunDateMin = PSPDate.getPSPTime();
        earliestPayrollRunDateMin.addDays(minDaysInPastForPayRunDate);
        paramValues[2] = earliestPayrollRunDateMin;

        DomainEntitySet<PayrollRun> retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        return retList.size() + company.getQuickbooksInfo().getAS400PayrollCount();
    }

    public static class PayrollRunLimitInfo {
        public SpcfMoney maxCompanySum;
        public SpcfMoney maxEmployeeSum;
        public SpcfMoney maxBankAccountSum;

        public boolean violatesLimits = false;

        public boolean violatesCompanyLimits = false;
        public boolean violatesEmployeeLimits = false;
        public boolean violatesEmployeeBankAccountLimits = false;

        public Employee limitViolatingEmployee = null;
        public EmployeeBankAccount employeeBankAccount = null;
        public String limitViolatingBARoutingNum = null;
        public String limitViolatingBAAccountNum = null;
    }

    /**
     * This class is needed to use SpcfCalendar as the key in a KeyMap, because SpcfCalendar does not
     * implement Comparable
     */
    private class SpcfCalendarComparator implements Comparator<SpcfCalendar> {
        public int compare(SpcfCalendar o1, SpcfCalendar o2) {
            if (o1.before(o2)) return -1;
            if (o2.before(o1)) return 1;
            return 0;
        }
    }
    
    static ArrayList<PayrollRunDTO> getPayrollsInMemory(Company company) {
        ArrayList<PayrollRunDTO> payrollsInMemory = Application.getSessionCache().getNonHibernateObject(PAYROLLS_IN_MEMORY_CACHE_KEY  + ":" + company.getId());

        if (payrollsInMemory == null) {
            payrollsInMemory = new ArrayList<PayrollRunDTO>();
            Application.getSessionCache().addNonHibernateObject(PAYROLLS_IN_MEMORY_CACHE_KEY + ":" + company.getId(), payrollsInMemory);
        }

        return payrollsInMemory;
    }

    static ArrayList<PayrollRunDTO> getPayrollsInMemoryforDDMigrated(Company company) {
        ArrayList<PayrollRunDTO> payrollsInMemory = Application.getSessionCache().getNonHibernateObject(DDPAYROLLS_IN_MEMORY_CACHE_KEY  + ":" + company.getId());

        if (payrollsInMemory == null) {
            payrollsInMemory = new ArrayList<PayrollRunDTO>();
            Application.getSessionCache().addNonHibernateObject(DDPAYROLLS_IN_MEMORY_CACHE_KEY + ":" + company.getId(), payrollsInMemory);
        }

        return payrollsInMemory;
    }


    private int getDDCompanyLimitDuration(DDCompanyServiceInfo pDDCompanyServiceInfo) {
        LimitValue limitValue = getLimitRule(pDDCompanyServiceInfo.getCompany(), pDDCompanyServiceInfo.getService().getServiceCd())
                .findLimitValueByName(LimitValueType.CompanyLimitDuration);
        return Integer.parseInt(limitValue.getValue());
    }

    private LimitRule getLimitRule(Company pCompany, ServiceCode pServiceCode) {
        if(mLimitRule == null) {
            mLimitRule = LimitRule.findLimitRule(pCompany, pServiceCode);
        }
        return mLimitRule;
    }
}

