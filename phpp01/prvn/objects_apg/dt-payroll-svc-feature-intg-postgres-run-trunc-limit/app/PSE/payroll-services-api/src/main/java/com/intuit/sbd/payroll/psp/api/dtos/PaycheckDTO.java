/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/PaycheckDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.AccrualType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

public class PaycheckDTO {
    private String paycheckId;
    private String employeeId;
    private DateDTO payPeriodBeginDate;
    private DateDTO payPeriodEndDate;
    private Collection<LiabilityTransactionDTO> liabilityTransactions;
    private Collection<CompensationTransactionDTO> compensationTransactions;
    private Collection<DeductionTransactionDTO> deductionTransactions;
    private Collection<EmployerContributionTransactionDTO> employerContributionTransactions;
    private List<DDTransactionDTO> ddTransactions;
    private SpcfMoney paycheckNetAmount;
    private SpcfMoney paycheckGrossAmount;
    private SpcfMoney paycheckYTDNetAmount;
    private SpcfMoney paycheckYTDGrossAmount;
    private boolean mIsYTDAdjustment;
    private boolean mIsVoid = false;
    private QBDTPaycheckInfoDTO mQBDTPaycheckInfoDTO;
    private Map<AccrualType, Double> mAccruedHours;
    private String sessionID;
    
    public String getPaycheckId() {
        return paycheckId;
    }

    public void setPaycheckId(String pPaycheckId) {
        this.paycheckId = pPaycheckId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        this.employeeId = pEmployeeId;
    }

    public DateDTO getPayPeriodBeginDate() {
        return payPeriodBeginDate;
    }

    public void setPayPeriodBeginDate(DateDTO payPeriodBeginDate) {
        this.payPeriodBeginDate = payPeriodBeginDate;
    }

    public DateDTO getPayPeriodEndDate() {
        return payPeriodEndDate;
    }

    public void setPayPeriodEndDate(DateDTO payPeriodEndDate) {
        this.payPeriodEndDate = payPeriodEndDate;
    }

    public Collection<LiabilityTransactionDTO> getLiabilityTransactions() {
        if (liabilityTransactions == null) {
            liabilityTransactions = new ArrayList<LiabilityTransactionDTO>();
        }
        return liabilityTransactions;
    }

    public void setLiabilityTransactions(Collection<LiabilityTransactionDTO> pLiabilityTransactions) {
        this.liabilityTransactions = pLiabilityTransactions;
    }

    public Collection<CompensationTransactionDTO> getCompensationTransactions() {
        if (compensationTransactions == null) {
            compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        }
        return compensationTransactions;
    }

    public void setCompensationTransactions(Collection<CompensationTransactionDTO> compensationTransactions) {
        this.compensationTransactions = compensationTransactions;
    }

    public Collection<DeductionTransactionDTO> getDeductionTransactions() {
        if (deductionTransactions == null) {
            deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        }
        return deductionTransactions;
    }

    public void setDeductionTransactions(Collection<DeductionTransactionDTO> deductionTransactions) {
        this.deductionTransactions = deductionTransactions;
    }

    public Collection<EmployerContributionTransactionDTO> getEmployerContributionTransactions() {
        if (employerContributionTransactions == null) {
            employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();
        }
        return employerContributionTransactions;
    }

    public void setEmployerContributionTransactions(Collection<EmployerContributionTransactionDTO> employerContributionTransactions) {
        this.employerContributionTransactions = employerContributionTransactions;
    }

    public List<DDTransactionDTO> getDdTransactions() {
        return ddTransactions;
    }

    public void setDdTransactions(List<DDTransactionDTO> pDDTransactions) {
        this.ddTransactions = pDDTransactions;
    }

    public SpcfMoney getPaycheckNetAmount() {
        return paycheckNetAmount;
    }

    public void setPaycheckNetAmount(SpcfMoney pPaycheckNetAmount) {
        this.paycheckNetAmount = pPaycheckNetAmount;
    }

    public SpcfMoney getPaycheckGrossAmount() {
        return paycheckGrossAmount;
    }

    public void setPaycheckGrossAmount(SpcfMoney paycheckGrossAmount) {
        this.paycheckGrossAmount = paycheckGrossAmount;
    }

    public SpcfMoney getPaycheckYTDNetAmount() {
        return paycheckYTDNetAmount;
    }

    public void setPaycheckYTDNetAmount(SpcfMoney paycheckYTDNetAmount) {
        this.paycheckYTDNetAmount = paycheckYTDNetAmount;
    }

    public SpcfMoney getPaycheckYTDGrossAmount() {
        return paycheckYTDGrossAmount;
    }

    public void setPaycheckYTDGrossAmount(SpcfMoney paycheckYTDGrossAmount) {
        this.paycheckYTDGrossAmount = paycheckYTDGrossAmount;
    }

    public boolean isIsYTDAdjustment() {
        return mIsYTDAdjustment;
    }

    public void setIsYTDAdjustment(boolean pIsYTDAdjustment) {
        mIsYTDAdjustment = pIsYTDAdjustment;
    }

    public boolean isVoid() {
        return mIsVoid;
    }

    public void setIsVoid(boolean pIsVoid) {
        mIsVoid = pIsVoid;
    }

    public QBDTPaycheckInfoDTO getQBDTPaycheckInfoDTO() {
        return mQBDTPaycheckInfoDTO;
    }

    public void setQBDTPaycheckInfoDTO(QBDTPaycheckInfoDTO pQBDTPaycheckInfoDTO) {
        mQBDTPaycheckInfoDTO = pQBDTPaycheckInfoDTO;
    }

    /**
     * Validates a Paycheck DTO
     *
     * @return
     */
    public ProcessResult validatePaycheckDTO() {
        ProcessResult validationResult = new ProcessResult();

        if (paycheckId == null || !Validator.isValidLength(paycheckId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Paycheck, null, "PaycheckId");
            return validationResult;
        }

        if (employeeId == null || !Validator.isValidLength(employeeId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Paycheck, paycheckId, "EmployeeId");
        }

        if (payPeriodBeginDate != null) {
            validationResult.merge(payPeriodBeginDate.validate());
        }

        if (payPeriodEndDate != null) {
            validationResult.merge(payPeriodEndDate.validate());
        }

        if (paycheckNetAmount == null || SpcfUtils.convertToBigDecimal(paycheckNetAmount).scale() >2) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.Paycheck, paycheckId, "PaycheckNetAmount");
        }

        if (liabilityTransactions != null) {
            for (LiabilityTransactionDTO currLiabilityTxnDTO : liabilityTransactions) {
                validationResult.merge(currLiabilityTxnDTO.validateLiabilityTransactionDTO());
            }
        }

        if (compensationTransactions != null) {
            for (CompensationTransactionDTO currCompensationTxnDTO : compensationTransactions) {
                validationResult.merge(currCompensationTxnDTO.validateCompensationTransactionDTO());
            }
        }
        if (deductionTransactions != null) {
            for (DeductionTransactionDTO currDeductionTxnDTO : deductionTransactions) {
                validationResult.merge(currDeductionTxnDTO.validateDeductionTransactionDTO());
            }
        }
        if (employerContributionTransactions != null) {
            for (EmployerContributionTransactionDTO currDeductionTxnDTO : employerContributionTransactions) {
                validationResult.merge(currDeductionTxnDTO.validateEmployerContributionTransactionDTO());
            }
        }
        if (ddTransactions!=null) {
            for (DDTransactionDTO currDDTxnDTO : ddTransactions) {
                validationResult.merge(currDDTxnDTO.validateDDTransactionDTO());
            }
        }
        
        return validationResult;
    }

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
}
