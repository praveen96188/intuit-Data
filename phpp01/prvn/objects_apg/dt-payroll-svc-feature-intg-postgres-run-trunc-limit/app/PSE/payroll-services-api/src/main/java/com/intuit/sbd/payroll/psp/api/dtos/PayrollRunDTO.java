/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/PayrollRunDTO.java#5 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class PayrollRunDTO {
    private String payrollTXBatchId;
    private DateDTO targetPayrollTXDate;
    private DateDTO settlementDate;
    private boolean isBalanceFilePayroll = false;
    private PayrollType mPayrollType;
    private boolean mChargeTransmissionFee;
    private int mEmployeesPaidInTransmission;
    private boolean mTransmissionHasBackdatedPayrolls = false;
    private boolean mIsAssisted = false;
    private String sessionId;
    
    private Collection<PaycheckDTO> paychecks;
    private Collection<ServiceBankAccountDTO> serviceBankAccounts;
    private String transmissionId;

    private Collection<CompanyAdjustmentSubmissionDTO> companyAdjustmentSubmissionDTOs;

    
    
    
    public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean getIsAssisted() {
        return mIsAssisted;
    }

    public void setIsAssisted(boolean pIsAssisted) {
        mIsAssisted = pIsAssisted;
    }

    public String getPayrollTXBatchId() {
        return payrollTXBatchId;
    }

    public void setPayrollTXBatchId(String pPayrollTXBatchId) {
        this.payrollTXBatchId = pPayrollTXBatchId;
    }

    public DateDTO getTargetPayrollTXDate() {
        return targetPayrollTXDate;
    }

    public void setTargetPayrollTXDate(DateDTO pTargetPayrollTXDate) {
        this.targetPayrollTXDate = pTargetPayrollTXDate;
    }

    public Collection<PaycheckDTO> getPaychecks() {
        if (paychecks == null) {
            paychecks = new ArrayList<PaycheckDTO>();
        }
        return paychecks;
    }

    public void setPaychecks(Collection<PaycheckDTO> pPaychecks) {
        this.paychecks = pPaychecks;
    }


    public Collection<ServiceBankAccountDTO> getCompanyBankAccounts() {
        return serviceBankAccounts;
    }

    public void setCompanyBankAccounts(Collection<ServiceBankAccountDTO> pCompanyBankAccounts) {
        this.serviceBankAccounts = pCompanyBankAccounts;
    }

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setTransmissionId(String pTransmissionId) {
        this.transmissionId = pTransmissionId;
    }

    public Collection<CompanyAdjustmentSubmissionDTO> getCompanyAdjustmentSubmissionDTOs() {
        if(companyAdjustmentSubmissionDTOs == null) {
            companyAdjustmentSubmissionDTOs = new ArrayList<CompanyAdjustmentSubmissionDTO>();
        }
        return companyAdjustmentSubmissionDTOs;
    }

    public void setCompanyAdjustmentSubmissionDTOs(Collection<CompanyAdjustmentSubmissionDTO> companyAdjustmentSubmissionDTOs) {
        this.companyAdjustmentSubmissionDTOs = companyAdjustmentSubmissionDTOs;
    }

    public DateDTO getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(DateDTO settlementDate) {
        this.settlementDate = settlementDate;
    }

    public boolean getBalanceFilePayroll() {
        return isBalanceFilePayroll;
    }

    public void setBalanceFilePayroll(boolean balanceFilePayroll) {
        isBalanceFilePayroll = balanceFilePayroll;
    }

    public PayrollType getPayrollType() {
        return mPayrollType;
    }

    public void setPayrollType(PayrollType pPayrollType) {
        mPayrollType = pPayrollType;
    }

    public boolean isChargeTransmissionFee() {
        return mChargeTransmissionFee;
    }

    public void setChargeTransmissionFee(boolean pChargeTransmissionFee) {
        mChargeTransmissionFee = pChargeTransmissionFee;
    }

    public int getEmployeesPaidInTransmission() {
        return mEmployeesPaidInTransmission;
    }

    // this should only be set on one payroll run per transmission
    public void setEmployeesPaidInTransmission(int pEmployeesPaidInTransmission) {
        mEmployeesPaidInTransmission = pEmployeesPaidInTransmission;
    }

    public boolean transmissionHasBackdatedPayrolls() {
        return mTransmissionHasBackdatedPayrolls;
    }

    public void setTransmissionHasBackdatedPayrolls(boolean pTransmissionHasBackdatedPayrolls) {
        mTransmissionHasBackdatedPayrolls = pTransmissionHasBackdatedPayrolls;
    }

    /**
     * validates a Payroll Run DTO
     *
     * @return ProcessResult containing information on validation failures
     */
    public ProcessResult validatePayrollRunDTO() {
        return validatePayrollRunDTO(true);
    }

    public ProcessResult validatePayrollRunDTO(boolean pValidateBankAccounts) {
        ProcessResult validationResult = new ProcessResult();

        if (payrollTXBatchId == null || !Validator.isValidLength(payrollTXBatchId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, payrollTXBatchId, "PayrollTXBatchId");
        }

        if (targetPayrollTXDate == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayrollRun, payrollTXBatchId, "TargetPayrollTXCalendar");
        } else {
            validationResult.merge(targetPayrollTXDate.validate());
        }


        if ((paychecks == null || paychecks.size() == 0) && (companyAdjustmentSubmissionDTOs == null || companyAdjustmentSubmissionDTOs.size() == 0)) {
            validationResult.getMessages().PaycheckNotSpecified(EntityName.PayrollRun, payrollTXBatchId);
            return validationResult;
        }

        if(paychecks != null) {
            for (PaycheckDTO currPaycheck : paychecks) {
                validationResult.merge(currPaycheck.validatePaycheckDTO());
            }
        }

        if(companyAdjustmentSubmissionDTOs != null) {
            for (CompanyAdjustmentSubmissionDTO liabilityAdjustment : companyAdjustmentSubmissionDTOs) {
                validationResult.merge(liabilityAdjustment.validate());
            }
        }

        if (!pValidateBankAccounts) return validationResult;

        if (serviceBankAccounts != null) {
            for (ServiceBankAccountDTO currServiceBankAccount : serviceBankAccounts) {
                CompanyBankAccountDTO currCompanyBankAccount = currServiceBankAccount.getCompanyBankAccount();
                if (currCompanyBankAccount == null) {
                    validationResult.getMessages().CompanyBankAccountNotSpecified(EntityName.PayrollRun, payrollTXBatchId);
                } else {
                    validationResult.merge(currCompanyBankAccount.validateCompanyBankAccount());
                }
            }
        }

        if (settlementDate != null) {
            validationResult.merge(settlementDate.validate());
        }

        return validationResult;
    }

    public SpcfMoney getPayrollDirectDepositAmount() {
        Collection<PaycheckDTO> paychecks = getPaychecks();
        SpcfMoney totalPayrollRunNetAmount = new SpcfMoney();

        if (paychecks != null) {
            for (PaycheckDTO currCheck : paychecks) {
                if(currCheck.getDdTransactions() != null) {
                    for (DDTransactionDTO ddTransactionDTO : currCheck.getDdTransactions()) {
                        SpcfMoney currAmount = SpcfUtils.convertToSpcfMoney(ddTransactionDTO.getDDTransactionAmount());
                        totalPayrollRunNetAmount = (SpcfMoney) totalPayrollRunNetAmount.add(currAmount);
                    }
                }
            }
        }

        return totalPayrollRunNetAmount;
    }

    /**
     * @return HashMap with a key of the routing number and the account number separated by a colon
     *         and a value of the net amount for that routing num/account num combo for the given payroll
     */
    public HashMap<String, SpcfMoney> getNetAmountPerBankAccount() {
        HashMap<String, SpcfMoney> amountsPerBankAccount = new HashMap<String, SpcfMoney>();
        Collection<PaycheckDTO> paychecks = getPaychecks();

        if (paychecks != null) {
            //iterate paychecks
            for (PaycheckDTO currPaycheck : paychecks) {
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();

                for (DDTransactionDTO currDDTxn : ddTxns) {
                    EmployeeBankAccountDTO currBankAccount = currDDTxn.getEmployeeBankAccount();
                    if (currBankAccount != null) {
                        String sourceBankAccountRoutingNum = currBankAccount.getBankAccount().getRoutingNumber();
                        String sourceBankAccountAcctNum = currBankAccount.getBankAccount().getAccountNumber();
                        String sourceBankAccountKey = sourceBankAccountRoutingNum + ":" + sourceBankAccountAcctNum;

                        if (!amountsPerBankAccount.containsKey(sourceBankAccountKey)) {
                            //add id to hashmap as key if does not exist
                            amountsPerBankAccount
                                    .put(sourceBankAccountKey,
                                            new SpcfMoney(currDDTxn.getDDTransactionAmount().toString()));
                        } else {
                            //add amount to current value in hashmap
                            SpcfMoney combinedAmount = (SpcfMoney) amountsPerBankAccount.get(sourceBankAccountKey)
                                    .add(new SpcfMoney(currDDTxn.getDDTransactionAmount().toString()));
                            amountsPerBankAccount.put(sourceBankAccountKey, combinedAmount);
                        }
                    }
                }
            }
        }
        return amountsPerBankAccount;
    }

    /**
     * @return HashMap with a key of the source employee Id and a value of the net amount for that employee for
     *         the given payroll
     */
    public HashMap<String, SpcfMoney> getNetAmountPerEmployee() {
        HashMap<String, SpcfMoney> amountsPerEmployee = new HashMap<String, SpcfMoney>();
        Collection<PaycheckDTO> paychecks = getPaychecks();

        if (paychecks != null) {
            //iterate paychecks
            for (PaycheckDTO currPaycheck : paychecks) {
                if(currPaycheck.getDdTransactions() == null || currPaycheck.getDdTransactions().size() == 0) {
                    continue;
                }

                String spsEEId = currPaycheck.getEmployeeId();
                SpcfDecimal paycheckNetAmount = SpcfMoney.ZERO;

                for (DDTransactionDTO ddTransactionDTO : currPaycheck.getDdTransactions()) {
                    paycheckNetAmount = paycheckNetAmount.add(SpcfUtils.convertToSpcfDecimal(ddTransactionDTO.getDDTransactionAmount()));
                }

                if (!amountsPerEmployee.containsKey(spsEEId)) {
                    //add id to hashmap as key if does not exist
                    amountsPerEmployee.put(spsEEId, (SpcfMoney)paycheckNetAmount);
                } else {
                    //add amount to current value in hashmap
                    SpcfMoney combinedAmount = (SpcfMoney) amountsPerEmployee.get(spsEEId).add(paycheckNetAmount);
                    amountsPerEmployee.put(spsEEId, combinedAmount);
                }
            }
        }
        return amountsPerEmployee;
    }

 
    /**
     * @return HashMap with a key of the routing number and the account number separated by a colon
     *         and a value of the net amount for that routing num/account num combo for the given payroll
     */
    public HashMap<String, String> getEmployeeIdPerBankAccountMap() {
        HashMap<String, String> employeePerBankAccount = new HashMap<String, String>();
        Collection<PaycheckDTO> paychecks = getPaychecks();

        if (paychecks != null) {
            //iterate paychecks
            for (PaycheckDTO currPaycheck : paychecks) {
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();

                for (DDTransactionDTO currDDTxn : ddTxns) {
                    EmployeeBankAccountDTO currBankAccount = currDDTxn.getEmployeeBankAccount();
                    if (currBankAccount != null) {
                        String sourceBankAccountRoutingNum = currBankAccount.getBankAccount().getRoutingNumber();
                        String sourceBankAccountAcctNum = currBankAccount.getBankAccount().getAccountNumber();
                        String sourceEmployeeId = currPaycheck.getEmployeeId();
                        String sourceBankAccountKey = sourceBankAccountRoutingNum + ":" + sourceBankAccountAcctNum;

                        employeePerBankAccount.put(sourceBankAccountKey, sourceEmployeeId);

                    }
                }
            }
        }
        return employeePerBankAccount;
    }

    public boolean isYTDAdjustment(){
        Iterator<PaycheckDTO> dtoIterator = getPaychecks().iterator();
        if(dtoIterator.hasNext()){
            return getPaychecks().iterator().next().isIsYTDAdjustment();
        }
        return false;
    }

    public boolean chargeFees() {
        return !getBalanceFilePayroll() && !isYTDAdjustment();
    }
}
