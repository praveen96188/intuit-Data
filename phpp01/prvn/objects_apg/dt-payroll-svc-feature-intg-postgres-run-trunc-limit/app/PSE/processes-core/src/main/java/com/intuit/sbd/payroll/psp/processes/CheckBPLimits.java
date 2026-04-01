/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/CheckBPLimits.java#2 $
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
import java.util.TreeMap;

import com.intuit.pmo.client.model.LimitCheckError;
import com.intuit.psp.dd.pojo.LimitResponse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.AutoLimitIncreaseTier;
import com.intuit.sbd.payroll.psp.domain.BPCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.EventLimitCode;
import com.intuit.sbd.payroll.psp.domain.LimitRule;
import com.intuit.sbd.payroll.psp.domain.LimitValue;
import com.intuit.sbd.payroll.psp.domain.LimitValueType;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbg.psp.dd.limitcheck.LimitCheckHelper;
import com.intuit.sbg.psp.dd.util.LimitCheckResponse;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;


/**
 *
 * @author Jeff Jones
 */
public class CheckBPLimits extends Process implements IProcess {

    private Company company;
    private ArrayList<BillPaymentDTO> billPaymentDTOs;
    private BillPaymentDTO mBillPaymentDTO;
    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;
    private BillPaymentLimitInfo billPaymentLimitInfo;
    private ServiceCode serviceCode;
    private BPCompanyServiceInfo bpCompanyServiceInfo;

    private boolean qualifiesForAutoIncrease;
    AutoLimitIncreaseTier autoIncreaseTier;
    private LimitRule mLimitRule;
    private LimitCheckHelper ddlimitCheck=new LimitCheckHelper();

    private static final String BILLPAYMENTS_IN_MEMORY_CACHE_KEY = "BillPaymentInMemoryFromBillPaymentSubmitCore";

    public CheckBPLimits(ArrayList<BillPaymentDTO> pBillPaymentDTOs, SourceSystemCode pSourceSystem, String pSourceCompanyId, ServiceCode pServiceCode) {
        sourceCompanyId = pSourceCompanyId;
        sourceSystemCd = pSourceSystem;

        billPaymentDTOs = pBillPaymentDTOs;
        billPaymentLimitInfo = new BillPaymentLimitInfo();
        serviceCode = pServiceCode;
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

        //This should never fail, as it should be caught by BP common validation
        bpCompanyServiceInfo = (BPCompanyServiceInfo) CompanyService
                .findCompanyService(company, serviceCode);

        // override bp limits check if company is on pre-funding hold
        if(!company.isCompanyHold(ServiceSubStatusCode.PendingPrefundingWire)){
            Boolean firstLimitViolation = true;
            for (BillPaymentDTO billPaymentDTO : billPaymentDTOs){
                mBillPaymentDTO = billPaymentDTO;
                if(!company.isPhase2DDMigrated()){

                    //for the Phase 1 : we do limit check and validations of settlement dates
                    //, However only log results
                    //This needs to be removed in case of complete migration to
                    //Phase 2
                    if(company.isPhase1DDMigrated() && company.getDDPublishFlag()){
                        ddlimitCheck.isValidDDLimit( billPaymentDTO.getDepositDate(), company );
                    }
                    violatesBPLimits(billPaymentDTO, billPaymentLimitInfo);

                    //If the company violated BP limits do processing
                    if (billPaymentLimitInfo.violatesLimits) {
                        SpcfMoney maxCompanySum = new SpcfMoney("0.00");
                        SpcfMoney maxPayeeSum = billPaymentDTO.getAmount();
                        if (billPaymentLimitInfo.maxCompanySum != null) {
                            maxCompanySum = billPaymentLimitInfo.maxCompanySum;
                        }

                        qualifiesForAutoIncrease = qualifiesForAutoLimitIncrease(bpCompanyServiceInfo,
                                maxCompanySum, maxPayeeSum);
                        if (!qualifiesForAutoIncrease) {
                            final Boolean fFirstLimitViolation = firstLimitViolation;
                            ProcessResult pr = PayrollServices.executeTransactionThread(new TransactionThread() {
                                public ProcessResult transaction() {
                                    return processFailedBPLimits(CheckBPLimits.this, fFirstLimitViolation);
                                }
                            });
                            firstLimitViolation = false;
                            validationResult.merge(pr);
                        }
                    }

                    getBillPaymentsInMemory(company).add(billPaymentDTO);
                }else{
                    getBillPaymentsInMemory(company).add(billPaymentDTO);

                    //if company is migrated
                    final Boolean fFirstLimitViolation = firstLimitViolation;
                    LimitResponse ddLimitResponse=ddlimitCheck.isValidDDLimit(billPaymentDTO.getDepositDate(), company);
                    if(!ddLimitResponse.getLimitCheckResponse().equals(LimitCheckResponse.PASS)){
                        ProcessResult pr = PayrollServices.executeTransactionThread(new TransactionThread() {
                            public ProcessResult transaction() {
                                return processFailedBPLimitsForDDMigrated(CheckBPLimits.this, fFirstLimitViolation,ddLimitResponse);
                            }
                        });
                        validationResult.merge(pr);
                    }
                }
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        //If the company qualified for an auto-limit increase, update the company and/or payee limits as applicable
        if (qualifiesForAutoIncrease) {
            if (billPaymentLimitInfo.violatesCompanyLimits) {
                SpcfMoney oldERLimit = getBPCompanyLimitAmount(bpCompanyServiceInfo);
                SpcfMoney autoERLimit = new SpcfMoney(billPaymentLimitInfo.maxCompanySum.multiply(autoIncreaseTier.getIncreaseMultiplier()));

                // enforce auto increase cap rules
                SpcfMoney newERLimit = min(autoERLimit, autoIncreaseTier.getCompanyCap());

                bpCompanyServiceInfo.setOverrideCompanyLimitAmount(newERLimit);

                CompanyEvent.createBPLimitIncreaseEvent(company,
                                                      EventLimitCode.Company,
                                                      mBillPaymentDTO.getBillPaymentId(),
                                                      null,
                                                      newERLimit,
                                                      oldERLimit,
                                                      PSPDate.getPSPTime());
            }

            if (billPaymentLimitInfo.violatesPayeeBankAccountLimits || billPaymentLimitInfo.violatesPayeeLimits) {
                SpcfMoney oldEELimit = getBPPayeeLimitAmount(bpCompanyServiceInfo);
                SpcfMoney eeMax = getMaxPayeeAmount();
                SpcfMoney autoEELimit = new SpcfMoney(eeMax.multiply(autoIncreaseTier.getIncreaseMultiplier()));

                // enforce auto increase cap rules
                SpcfMoney newEELimit = min(autoEELimit, autoIncreaseTier.getPayeeCap());

                bpCompanyServiceInfo.setOverridePayeeLimitAmount(newEELimit);

                CompanyEvent.createBPLimitIncreaseEvent(company,
                                                      EventLimitCode.Payee,
                                                      mBillPaymentDTO.getBillPaymentId(),
                                                      billPaymentLimitInfo.limitViolatingPayee,
                                                      newEELimit,
                                                      oldEELimit,
                                                      PSPDate.getPSPTime());
            }

        }

        bpCompanyServiceInfo.setConsecutiveLimitViolationCount(new Long("0"));

        bpCompanyServiceInfo = Application.save(bpCompanyServiceInfo);

        //Reset counter
        bpCompanyServiceInfo.setConsecutiveLimitViolationCount(new Long("0"));
        bpCompanyServiceInfo = Application.save(bpCompanyServiceInfo);

        return processResult;
    }

    //Helper methods
    protected void violatesBPLimits(BillPaymentDTO pBillPaymentDTO, BillPaymentLimitInfo pBillPaymentLimitInfo) {
        companyViolatesBPLimits(pBillPaymentDTO, pBillPaymentLimitInfo);
        anyPayeeViolatesBPLimits(pBillPaymentDTO, pBillPaymentLimitInfo);

        //todo not implemented yet
        //anyBankAccountViolatesBPLimits(pBillPaymentDTO, pBillPaymentLimitInfo);
    }

    protected void companyViolatesBPLimits(BillPaymentDTO pBillPaymentDTO, BillPaymentLimitInfo pBillPaymentLimitInfo) {

        SpcfCalendar procCheckDate = company.getNextValidPaycheckDepositDate(DateDTO.convertToSpcfCalendar(pBillPaymentDTO.getDepositDate()));
//        SpcfCalendar procCheckDate = DateDTO.convertToSpcfCalendar(pPayrollRun.getTargetPayrollTXDate());
        SpcfMoney payrollNetAmount = pBillPaymentDTO.getAmount();

        BPCompanyServiceInfo bpCompanyServiceInfo = (BPCompanyServiceInfo) CompanyService
                .findCompanyService(company, serviceCode);

        SpcfMoney bpLimitForCompany = getBPCompanyLimitAmount(bpCompanyServiceInfo);

        //Get the aggregate sum per Payee, per paycheck date for other payrolls within the fraud check date range
        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = getAmountsPerPayCheckDate(procCheckDate);

        //Get the amount for each range within the entire range
        for (SpcfCalendar currCheckDate : amountsPerPayCheckDate.keySet()) {
            if (currCheckDate.before(procCheckDate) || currCheckDate.equals(procCheckDate)) {
                SpcfMoney amountForRange = accumulateToCheckDate(amountsPerPayCheckDate, currCheckDate, true);

                //Add the current payroll net amount to the amount for the entire range
                amountForRange = (SpcfMoney) amountForRange.add(payrollNetAmount);
                if (amountForRange.compareTo(bpLimitForCompany) > 0) {
                    if (pBillPaymentLimitInfo.maxCompanySum == null || amountForRange
                            .compareTo(pBillPaymentLimitInfo.maxCompanySum) > 0) {
                        pBillPaymentLimitInfo.maxCompanySum = amountForRange;
                    }
                    pBillPaymentLimitInfo.violatesCompanyLimits = true;
                    pBillPaymentLimitInfo.violatesLimits = true;
                }
            }
        }
    }

    protected void anyPayeeViolatesBPLimits(BillPaymentDTO pBillPaymentDTO,
                                               BillPaymentLimitInfo pBillPaymentLimitInfo) {
        SpcfCalendar procCheckDate = company.getNextValidPaycheckDepositDate(DateDTO.convertToSpcfCalendar(pBillPaymentDTO.getDepositDate()));

        BPCompanyServiceInfo bpCompanyServiceInfo = (BPCompanyServiceInfo) CompanyService
                .findCompanyService(company, serviceCode);

        SpcfMoney bpLimitForPayee = getBPPayeeLimitAmount(bpCompanyServiceInfo);

        //Get the aggregate sum per Payee, per paycheck date for other payrolls within the fraud check date range
        HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> amountPerPayeePerPayCheckDate = getAmountsPerPayeePerPayCheckDate(procCheckDate);

        //Examine all the payees for the payroll
            Payee currPayee = Payee.findPayee(company, pBillPaymentDTO.getPayeeDTO().getSourcePayeeId());
        SpcfMoney payeeAmount = pBillPaymentDTO.getAmount();

            TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = getAmountsPerPayCheckDate(amountPerPayeePerPayCheckDate, currPayee.getId(), procCheckDate);

            //Get the amount for each range within the entire range
            for (SpcfCalendar currCheckDate : amountsPerPayCheckDate.keySet()) {
                if (currCheckDate.before(procCheckDate) || currCheckDate.equals(procCheckDate)) {
                    SpcfMoney amountForRange = accumulateToCheckDate(amountsPerPayCheckDate, currCheckDate, false);

                    //Add the amount for this payee for this particular payroll to the found amount
                amountForRange = (SpcfMoney) amountForRange.add(payeeAmount);
                    if (amountForRange.compareTo(bpLimitForPayee) > 0) {
                        if (pBillPaymentLimitInfo.maxPayeeSum == null || amountForRange
                                .compareTo(pBillPaymentLimitInfo.maxPayeeSum) > 0) {
                            pBillPaymentLimitInfo.maxPayeeSum = amountForRange;
                            pBillPaymentLimitInfo.limitViolatingPayee = currPayee;
                        }
                        pBillPaymentLimitInfo.violatesPayeeLimits = true;
                        pBillPaymentLimitInfo.violatesLimits = true;
                    }
                }
            }
    }

    private SpcfMoney accumulateToCheckDate(TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate, SpcfCalendar pInitialCheckDate, boolean pIsCompanyLimitViolateChk) {
        SpcfCalendar finalCheckDate = pInitialCheckDate.copy();
        if(pIsCompanyLimitViolateChk){
            finalCheckDate.addDays(getCompanyLimitDuration());
        }else{
            finalCheckDate.addDays(getPayeeLimitDuration());
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

    protected SpcfMoney getBPCompanyLimitAmount(BPCompanyServiceInfo pBPCompanyServiceInfo) {
        SpcfMoney companyLimitAmount = pBPCompanyServiceInfo.getOverrideCompanyLimitAmount();
        String strErLimitAmount = getLimitRule(pBPCompanyServiceInfo.getCompany(),
                                               pBPCompanyServiceInfo.getService().getServiceCd()).findLimitValueByName(LimitValueType.DefaultCompanyLimit).getValue();
        if (companyLimitAmount == null) {
            companyLimitAmount = new SpcfMoney(strErLimitAmount);
        }
        return companyLimitAmount;
    }

    protected SpcfMoney getBPPayeeLimitAmount(BPCompanyServiceInfo pBPCompanyServiceInfo) {
        SpcfMoney payeeLimitAmount = pBPCompanyServiceInfo.getOverridePayeeLimitAmount();
        String limitAmount = getLimitRule(pBPCompanyServiceInfo.getCompany(), pBPCompanyServiceInfo.getService().getServiceCd()).findLimitValueByName(LimitValueType.DefaultEmployeeLimit).getValue();
        if (payeeLimitAmount == null) {
            payeeLimitAmount = new SpcfMoney(limitAmount);
        }
        return payeeLimitAmount;
    }

    private TreeMap<SpcfCalendar, SpcfMoney> getAmountsPerPayCheckDate(SpcfCalendar pBaseCheckDate) {
        int intHalfEeLimitDuration = getCompanyLimitDuration();

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
        List<Object> retList = Application.executeNamedQuery("SumBPAmountGroupByPayCheckDate", paramNames, paramValues);

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
        ArrayList<BillPaymentDTO> billPaymentsInMemory = getBillPaymentsInMemory(company);
        for (BillPaymentDTO billPaymentInMemory : billPaymentsInMemory) {
            SpcfCalendar checkDate = DateDTO.convertToSpcfCalendar(billPaymentInMemory.getDepositDate());
            SpcfMoney payrollNetAmount = billPaymentInMemory.getAmount();

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

    private TreeMap<SpcfCalendar, SpcfMoney> getAmountsPerPayCheckDate(HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> pAmountPerPayeePerPayCheckDate,
                                                                       SpcfUniqueId pPayeeId, SpcfCalendar pBaseCheckDate) {
        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = pAmountPerPayeePerPayCheckDate.get(pPayeeId);
        if (amountsPerPayCheckDate == null) {
            amountsPerPayCheckDate = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
        }

        //Add current paycheck date if not already there
        if (!amountsPerPayCheckDate.containsKey(pBaseCheckDate)) {
            amountsPerPayCheckDate.put(pBaseCheckDate, new SpcfMoney("0.0"));
        }

        return amountsPerPayCheckDate;
    }

    private class SpcfCalendarComparator implements Comparator<SpcfCalendar> {
        public int compare(SpcfCalendar o1, SpcfCalendar o2) {
            if (o1.before(o2)) return -1;
            if (o2.before(o1)) return 1;
            return 0;
        }
    }

    /**
     * Get the aggregate sum per payee, per paycheck date for existing payrolls within the fraud check date range
     *
     * @param pBaseCheckDate
     * @return
     */
    private HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> getAmountsPerPayeePerPayCheckDate(SpcfCalendar pBaseCheckDate) {
        //
        // First add all payrolls that are in the database
        //
        int intHalfEeLimitDuration = getPayeeLimitDuration();

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

        HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> amountsPerPayeePerPayCheckDate = new HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>>();
        List<Object> retList = Application.executeNamedQuery("SumAmountGroupByBillPaymentCheckDateAndPayee", paramNames, paramValues);

        for (Object row : retList) {
            SpcfUniqueId payeeId = (SpcfUniqueId) ((Object[]) row)[0];
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[1];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[2];

            TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerPayeePerPayCheckDate.get(payeeId);
            if (checkDateAmountMap == null) {
                checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                amountsPerPayeePerPayCheckDate.put(payeeId, checkDateAmountMap);
            }
            checkDateAmountMap.put(checkDate, checkAmount);
        }

        //
        // Now we need to add payrolls that are being processed (currently in the cache)
        //
        ArrayList<BillPaymentDTO> billPaymentsInMemory = getBillPaymentsInMemory(company);
        for (BillPaymentDTO billPaymentInMemory : billPaymentsInMemory) {
            SpcfCalendar checkDate = DateDTO.convertToSpcfCalendar(billPaymentInMemory.getDepositDate());

            Payee currPayee = Payee.findPayee(company, billPaymentInMemory.getPayeeDTO().getSourcePayeeId());
            SpcfMoney payeeAmount = billPaymentInMemory.getAmount();
            SpcfUniqueId payeeId = currPayee.getId();

            // Get the checkDateAmountMap that needs to be added to
            TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerPayeePerPayCheckDate.get(payeeId);
            if (checkDateAmountMap == null) {
                checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                amountsPerPayeePerPayCheckDate.put(payeeId, checkDateAmountMap);
            }

            // Get the amount that needs to be added to
            SpcfMoney amountOnDate = checkDateAmountMap.get(checkDate);
            if (amountOnDate == null) {
                amountOnDate = new SpcfMoney("0.00");
            }
            amountOnDate = (SpcfMoney) amountOnDate.add(payeeAmount);

            // Write back the accumulated amount to checkDateAmountMap
            checkDateAmountMap.put(checkDate, amountOnDate);
        }

        return amountsPerPayeePerPayCheckDate;
    }

    public static class BillPaymentLimitInfo {
        public SpcfMoney maxCompanySum;
        public SpcfMoney maxPayeeSum;
        public SpcfMoney maxBankAccountSum;

        public boolean violatesLimits = false;

        public boolean violatesCompanyLimits = false;
        public boolean violatesPayeeLimits = false;
        public boolean violatesPayeeBankAccountLimits = false;

        public Payee limitViolatingPayee = null;
        public PayeeBankAccount payeeBankAccount = null;
    }

    private int getCompanyLimitDuration() {
        LimitValue limitValue = getLimitRule(bpCompanyServiceInfo.getCompany(), bpCompanyServiceInfo.getService().getServiceCd())
                .findLimitValueByName(LimitValueType.CompanyLimitDuration);
        return Integer.parseInt(limitValue.getValue());
    }

    private int getPayeeLimitDuration() {
        LimitValue limitValue = getLimitRule(bpCompanyServiceInfo.getCompany(), bpCompanyServiceInfo.getService().getServiceCd())
                .findLimitValueByName(LimitValueType.EmployeeLimitDuration);
        return Integer.parseInt(limitValue.getValue());
    }

    // This method can be called from a different unit of work
    protected ProcessResult processFailedBPLimits(CheckBPLimits pCheckBPLimits, Boolean pFirstLimitViolation) {
        ProcessResult processResult = new ProcessResult();

        // Update Limit Violation Count
        Company company = Application.findById(Company.class, pCheckBPLimits.company.getId());

        BPCompanyServiceInfo bpServiceInfo = (BPCompanyServiceInfo) company.getService(serviceCode);

        //limit violation
        limitviolation(company,pFirstLimitViolation,bpServiceInfo);

        BillPaymentDTO billPaymentDTO = pCheckBPLimits.mBillPaymentDTO;
        String payrollRunId = billPaymentDTO.getDepositDate().toString();
        Payee payee = Payee.findPayee(company, billPaymentDTO.getPayeeDTO().getSourcePayeeId());
        SpcfMoney amount = billPaymentDTO.getAmount();
        BillPaymentLimitInfo billPaymentLimitInfo = pCheckBPLimits.billPaymentLimitInfo;

        if (billPaymentLimitInfo.violatesPayeeLimits) {
            CompanyEvent.createBPLimitViolationEvent(company,
                        EventLimitCode.Payee,
                        payrollRunId,
                        null, null,
                        payee,
                        bpServiceInfo.getPayeeLimit(),
                        billPaymentLimitInfo.maxPayeeSum,
                        PSPDate.getPSPTime());

            processResult.getMessages().LimitExceededForPayee(EntityName.BillPayment, company.getSourceCompanyId(), payee.getName(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        }

        if (billPaymentLimitInfo.violatesCompanyLimits) {
            CompanyEvent.createBPLimitViolationEvent(company,
                    EventLimitCode.Company,
                    payrollRunId,
                    null, null,
                    null,
                    bpServiceInfo.getCompanyLimit(),
                    billPaymentLimitInfo.maxCompanySum,
                    PSPDate.getPSPTime());


            processResult.getMessages().PaymentSubmissionExceedsLimits(EntityName.BillPayment, company.getSourceCompanyId(), billPaymentDTO.getDepositDate().getMMDDYYYY(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        }

        return processResult;
    }

    protected ProcessResult processFailedBPLimitsForDDMigrated(CheckBPLimits pCheckBPLimits, Boolean pFirstLimitViolation ,LimitResponse ddLimitResponse) {

        ProcessResult processResult = new ProcessResult();
        Company company = Application.findById(Company.class, pCheckBPLimits.company.getId());
        BPCompanyServiceInfo bpServiceInfo = (BPCompanyServiceInfo) company.getService(serviceCode);

        BillPaymentDTO billPaymentDTO = pCheckBPLimits.mBillPaymentDTO;

        //limit violation
        limitviolation(company,pFirstLimitViolation,bpServiceInfo);

        String payrollRunId = billPaymentDTO.getDepositDate().toString();
        Payee payee = Payee.findPayee(company, billPaymentDTO.getPayeeDTO().getSourcePayeeId());

        if(ddLimitResponse.getLimitCheckResponse().equals(LimitCheckResponse.FAIL)){
            List<LimitCheckError> errorList = ddLimitResponse.getErrorMessages();
            if (errorList != null && !errorList.isEmpty()) {
                for (Iterator iterator = errorList.iterator(); iterator.hasNext(); ) {
                    LimitCheckError error = (LimitCheckError) iterator.next();
                    if (error != null) {
                        //If error code in the response is either 101, 102
                        if(error.getCode().equals(101)) {//company exceeds
                            CompanyEvent.createBPLimitViolationEvent(company,
                                    EventLimitCode.Company,
                                    payrollRunId,
                                    null, null,
                                    null,
                                    bpServiceInfo.getCompanyLimit(),
                                    billPaymentLimitInfo.maxCompanySum,
                                    PSPDate.getPSPTime());


                            processResult.getMessages().PaymentSubmissionExceedsLimits(EntityName.BillPayment, company.getSourceCompanyId(),
                                    billPaymentDTO.getDepositDate().getMMDDYYYY(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
                        }

                        else if(error.getCode().equals(102)) {//payee exceeds
                            CompanyEvent.createBPLimitViolationEvent(company,
                                    EventLimitCode.Payee,
                                    payrollRunId,
                                    null, null,
                                    payee,
                                    bpServiceInfo.getPayeeLimit(),
                                    billPaymentLimitInfo.maxPayeeSum,
                                    PSPDate.getPSPTime());

                            processResult.getMessages().LimitExceededForPayee(EntityName.BillPayment, company.getSourceCompanyId(),
                                    payee.getName(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
                        }
                    }else{
                        //generic error
                        //TODO
                        CompanyEvent.createBPLimitViolationEvent(company,
                                EventLimitCode.Payee,
                                payrollRunId,
                                null, null,
                                payee,
                                bpServiceInfo.getPayeeLimit(),
                                billPaymentLimitInfo.maxPayeeSum,
                                PSPDate.getPSPTime());
                        //TODO
                        processResult.getMessages().LimitExceededForPayee(EntityName.BillPayment, company.getSourceCompanyId(),
                                payee.getName(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
                    }
                }
            }
        }

        //Limit exceeded message
        if(ddLimitResponse.getLimitCheckResponse().equals(LimitCheckResponse.PARTIAL)){
            CompanyEvent.createBPLimitViolationEvent(company,
                    EventLimitCode.Payee,
                    payrollRunId,
                    null, null,
                    payee,
                    bpServiceInfo.getPayeeLimit(),
                    billPaymentLimitInfo.maxPayeeSum,
                    PSPDate.getPSPTime());
            //TODO
            processResult.getMessages().LimitExceededForPayee(EntityName.BillPayment, company.getSourceCompanyId(),
                    payee.getName(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        }else{//validation errors
            CompanyEvent.createBPLimitViolationEvent(company,
                    EventLimitCode.Payee,
                    payrollRunId,
                    null, null,
                    payee,
                    bpServiceInfo.getPayeeLimit(),
                    billPaymentLimitInfo.maxPayeeSum,
                    PSPDate.getPSPTime());
            //TODO
            processResult.getMessages().LimitExceededForPayee(EntityName.BillPayment, company.getSourceCompanyId(),
                    payee.getName(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        }

        return processResult;

    }

    protected void limitviolation(Company company , Boolean pFirstLimitViolation ,BPCompanyServiceInfo bpServiceInfo){

        Long limitViolationCount = bpServiceInfo.getConsecutiveLimitViolationCount();
        limitViolationCount++;
        //If the counter exceeds the maximum allowable consecutive limit violations, update company status to suspended
        // get the duration of days from the source system val table
        Long lMaxConsecutiveLimitViolations = Long.parseLong(getLimitRule(company, serviceCode).findLimitValueByName(LimitValueType.ConsecutiveLimitViolationLimit).getValue());

        if (pFirstLimitViolation) {
            bpServiceInfo.setConsecutiveLimitViolationCount(limitViolationCount);
            if (limitViolationCount.compareTo(lMaxConsecutiveLimitViolations) > 0) {
                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.BillPaymentLimit);
            }
        }
        bpServiceInfo = Application.save(bpServiceInfo);

    }

    static ArrayList<BillPaymentDTO> getBillPaymentsInMemory(Company company) {
        ArrayList<BillPaymentDTO> payrollsInMemory = Application.getSessionCache().getNonHibernateObject(BILLPAYMENTS_IN_MEMORY_CACHE_KEY  + ":" + company.getId());

        if (payrollsInMemory == null) {
            payrollsInMemory = new ArrayList<BillPaymentDTO>();
            Application.getSessionCache().addNonHibernateObject(BILLPAYMENTS_IN_MEMORY_CACHE_KEY + ":" + company.getId(), payrollsInMemory);
        }

        return payrollsInMemory;
    }

    private LimitRule getLimitRule(Company pCompany, ServiceCode pServiceCode) {
        if(mLimitRule == null) {
            mLimitRule = LimitRule.findLimitRule(pCompany, pServiceCode);
        }
        return mLimitRule;
    }

    protected boolean qualifiesForAutoLimitIncrease(BPCompanyServiceInfo pBpCompanyServiceInfo,
                                                    SpcfMoney pTotalCompanySum, SpcfMoney pTotalPayeeSum) {

        Company domainCompany = pBpCompanyServiceInfo.getCompany();
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
                .compareTo(getBPCompanyLimitAmount(pBpCompanyServiceInfo).multiply(new SpcfDecimalImpl(2))) <= 0;
        boolean bCompanySumLessThanMaxThreshold = pTotalCompanySum.compareTo(autoIncreaseTier.getCompanyCap()) <= 0;

        boolean bPayeeSumLessThan2xCurrLim = pTotalPayeeSum
                .compareTo(getBPPayeeLimitAmount(pBpCompanyServiceInfo).multiply(new SpcfDecimalImpl(2))) <= 0;
        boolean bPayeeSumLessThanMaxThreshold = pTotalPayeeSum.compareTo(autoIncreaseTier.getPayeeCap()) <= 0;

        return bCompanySumLessThan2xCurrLim
                && bCompanySumLessThanMaxThreshold
                && bPayeeSumLessThan2xCurrLim
                && bPayeeSumLessThanMaxThreshold;

    }

    private long getQualifyingPayrollRunCount(Company company, int daysOnService) {

        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(PayrollRun.Company().equalTo(company)
                                         .And(PayrollRun.PayrollRunType().equalTo(PayrollType.BillPayment))
                                         .And(PayrollRun.PayrollRunStatus().in(PayrollStatus.Complete, PayrollStatus.OffloadedAll, PayrollStatus.OffloadedDebit))
                                         .And(PayrollRun.PayrollDirectDepositAmount().greaterThan(new SpcfMoney("0.00"))))
                        .OrderBy(PayrollRun.PaycheckDate());
        DomainEntitySet<PayrollRun> bpPayrolls = Application.find(PayrollRun.class, query);

        if (bpPayrolls.size() > 0) {
            // Calculate the needed first payroll run.
            Integer minDaysInPastForPayRunDate = -1 * daysOnService;
            SpcfCalendar earliestPayrollRunDateMin = PSPDate.getPSPTime();
            earliestPayrollRunDateMin.addDays(minDaysInPastForPayRunDate);

            // First row should be the oldest.
            SpcfCalendar firstPayrollRun = bpPayrolls.getFirst().getPayrollRunDate();

            if (firstPayrollRun.before(earliestPayrollRunDateMin)) {
                 return bpPayrolls.size();
            }
        }

        return 0;
    }

    private SpcfMoney min(SpcfMoney val1, SpcfMoney val2) {
        if (val1.compareTo(val2) <= 0)
            return val1;
        else
            return val2;
    }

    /**
     * In order to see if a company qualifies for an increase, we must make use of the "maximum payee amount"
     * This amount is determined to be the greater of either the maximum exceded BP limit for a particular payee
     * or a particular bank account.
     *
     * @return Either the maximum bank account sum or the maximum payee sum, depending on which is greater
     */
    private SpcfMoney getMaxPayeeAmount() {
        SpcfMoney maxPayeeSum = new SpcfMoney("0.00");
        if (billPaymentLimitInfo.maxPayeeSum != null && billPaymentLimitInfo.maxBankAccountSum != null) {
            if (billPaymentLimitInfo.maxPayeeSum.compareTo(billPaymentLimitInfo.maxBankAccountSum) > 0) {
                maxPayeeSum = billPaymentLimitInfo.maxPayeeSum;
            }
            else {
                maxPayeeSum = billPaymentLimitInfo.maxBankAccountSum;
            }
        }
        else if (billPaymentLimitInfo.maxPayeeSum == null && billPaymentLimitInfo.maxBankAccountSum != null) {
            maxPayeeSum = billPaymentLimitInfo.maxBankAccountSum;
        }
        else if (billPaymentLimitInfo.maxBankAccountSum == null && billPaymentLimitInfo.maxPayeeSum != null) {
            maxPayeeSum = billPaymentLimitInfo.maxPayeeSum;
        }
        return maxPayeeSum;
    }

}

