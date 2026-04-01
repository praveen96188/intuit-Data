package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.LedgerOperationJobType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dweinberg
 * Date: 11/8/12
 * Time: 10:19 AM
 */
public class LedgerOperationJobDTO {
    private LedgerOperationJobType type;
    private String description;
    private String originalFile;
    List<LedgerOperationDTO> ledgerOperations = new ArrayList<LedgerOperationDTO>();

    public LedgerOperationJobType getType() {
        return type;
    }

    public void setType(LedgerOperationJobType pType) {
        type = pType;
    }

    public List<LedgerOperationDTO> getLedgerOperations() {
        return ledgerOperations;
    }

    public String getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(String pOriginalFile) {
        originalFile = pOriginalFile;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (type == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "Type");
        }
        if (StringUtils.isEmpty(originalFile)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "OriginalFile");
        }

        for (LedgerOperationDTO ledgerOperationDTO : ledgerOperations) {
            validationResult.merge(ledgerOperationDTO.validate());
            if (type == LedgerOperationJobType.BulkDebit) {
                if (ledgerOperationDTO.getAmount() == null) {
                    validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "Amount");
                }
            }
            if (type.in(LedgerOperationJobType.RateUpdate, LedgerOperationJobType.AdditionalFilingAmountUpdate)) {
                if (ledgerOperationDTO.getRate() < 0) {
                    validationResult.getMessages().AmountNotPositive(EntityName.LedgerOperation, "");
                }
            }
            if (type == LedgerOperationJobType.AdditionalFilingAmountUpdate) {
                if (StringUtils.isEmpty(ledgerOperationDTO.getAdditionalAmountName())) {
                    validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "AdditionalAmountName");
                }
            }
            if (type == LedgerOperationJobType.DepositFrequencyUpdate) {
                if (ledgerOperationDTO.getDepositFrequencyCode() == null) {
                    validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "DepositFrequency");
                }
            }

        }

        return validationResult;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }
}
