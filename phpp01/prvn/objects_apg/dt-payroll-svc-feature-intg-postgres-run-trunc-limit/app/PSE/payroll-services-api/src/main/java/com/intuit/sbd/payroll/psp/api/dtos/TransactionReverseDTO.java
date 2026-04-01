package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;

import java.util.List;
import java.util.Calendar;

/**
 *
 * User: kpaul
 * Date: Jan 6, 2008
 * Time: 1:42:49 AM

 */
public class TransactionReverseDTO {
    private SettlementTypeDTO txSettlementTypeCd = null;    // required (1)
    private Calendar txDate = null;                         // optional (0..1, required for non-ACH txns)
    private boolean chargeFee = false;                      // required (1)
    private String sourcePayrollRunId = null;                      // required (1)
    private List<String> ddTransactionIdList = null;        // optional (0..1, containing 0..* txn id's)
    private String transmissionId;

    private boolean isIntuitInitiatedReversals;
    
    public SettlementTypeDTO getTxSettlementTypeCd() {
        return txSettlementTypeCd;
    }

    public void setTxSettlementTypeCd(SettlementTypeDTO pTxSettlementTypeCd) {
        this.txSettlementTypeCd = pTxSettlementTypeCd;
    }

    public Calendar getTxDate() {
        return txDate;
    }

    public void setTxDate(Calendar pTxDate) {
        this.txDate = pTxDate;
    }

    public boolean isChargeFee() {
        return chargeFee;
    }

    public void setChargeFee(boolean pChargeFee) {
        this.chargeFee = pChargeFee;
    }

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setSourcePayrollRunId(String pSourcePayrollRunId) {
        this.sourcePayrollRunId = pSourcePayrollRunId;
    }

    public List<String> getDdTransactionIdList() {
        return ddTransactionIdList;
    }

    public void setDdTransactionIdList(List<String> pDdTransactionIdList) {
        this.ddTransactionIdList = pDdTransactionIdList;
    }

    public boolean isIntuitInitiatedReversals() {
       return isIntuitInitiatedReversals;
   }

   public void setIntuitInitiatedReversals(boolean intuitInitiatedReversals) {
       isIntuitInitiatedReversals = intuitInitiatedReversals;
   }

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setTransmissionId(String pTransmissionId) {
        this.transmissionId = pTransmissionId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (txSettlementTypeCd == null) {
            validationResult.getMessages().InvalidArgument(EntityName.SettlementType, null, null);
        } else {
            // Only need to validate the txn date iff settlement type is non-ACH
            if (!SettlementTypeDTO.ACH.equals(txSettlementTypeCd) && (txDate == null)) {
                validationResult.getMessages().SettlementDateNotSpecified(EntityName.PayrollRun, sourcePayrollRunId);
            }
        }

        if ((sourcePayrollRunId == null) || !Validator.isValidLength(sourcePayrollRunId, 1, 50)) {
            validationResult.getMessages().SourcePayrollRunIdNotSpecified(EntityName.PayrollRun, sourcePayrollRunId);
        }

        if (ddTransactionIdList != null) {
            for (String transId : ddTransactionIdList) {
                if ((transId == null) || !Validator.isValidLength(transId, 1, 50)) {
                    validationResult.getMessages().InvalidArgument(EntityName.DDTransaction, transId, transId);
                }
            }
        }

        return validationResult;
    }
}
