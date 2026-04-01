package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationJobDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dweinberg
 * Date: 11/8/12
 * Time: 10:18 AM
 */
public class AddLedgerOperationJob extends Process {

    private LedgerOperationJobDTO jobDTO;

    private Map<LedgerOperationDTO, ValidatedOperationDTO> validatedOptions;

    public AddLedgerOperationJob(LedgerOperationJobDTO pJobDTO) {
        jobDTO = pJobDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(jobDTO.validate());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        validatedOptions = new HashMap<LedgerOperationDTO, ValidatedOperationDTO>();
        for (LedgerOperationDTO ledgerOperationDTO : jobDTO.getLedgerOperations()) {
            ValidatedOperationDTO validatedOption = new ValidatedOperationDTO(ledgerOperationDTO);

            validatedOption.law = Application.findById(Law.class, ledgerOperationDTO.getLawId());
            if (validatedOption.law == null) {
                validationResult.getMessages().LawDoesNotExist(EntityName.Law, ledgerOperationDTO.getLawId());
            }

            validatedOptions.put(ledgerOperationDTO, validatedOption);
        }

        if (validatedOptions.isEmpty()) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "Operations");
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        LedgerOperationJob job = new LedgerOperationJob();
        job.setJobType(jobDTO.getType());
        job.setDescription(jobDTO.getDescription());
        job.setStatus(LedgerOperationJobStatus.Created);
        Application.save(job);

        int index = 0;
        for (LedgerOperationDTO dto : jobDTO.getLedgerOperations()) {
            ValidatedOperationDTO validatedOperationDTO = validatedOptions.get(dto);

            LedgerOperation ledgerOperation;
            RateLedgerOperation rateLedgerOperation = null;
            DepositFrequencyLedgerOperation depositFrequencyLedgerOperation = null;
            if (jobDTO.getType().in(LedgerOperationJobType.RateUpdate, LedgerOperationJobType.AdditionalFilingAmountUpdate)) {
                rateLedgerOperation = new RateLedgerOperation();
                ledgerOperation = rateLedgerOperation;
            } else if (jobDTO.getType() == LedgerOperationJobType.DepositFrequencyUpdate) {
                depositFrequencyLedgerOperation = new DepositFrequencyLedgerOperation();
                ledgerOperation = depositFrequencyLedgerOperation;
            } else {
                ledgerOperation = new LedgerOperation();
            }
            ledgerOperation.setLedgerOperationJob(job);
            ledgerOperation.setOriginalIndex(index++);
            ledgerOperation.setStatus(LedgerOperationStatus.Created);
            ledgerOperation.setAmount(dto.getAmount());
            ledgerOperation.setWageAmount(dto.getTaxableWages());
            ledgerOperation.setCheckDate(dto.getCheckDate().toSpcfCalendar());
            ledgerOperation.setLaw(validatedOperationDTO.law);
            ledgerOperation.setMemo(dto.getMemo());
            ledgerOperation.setSourceCompanyId(dto.getSourceCompanyId());
            ledgerOperation.setSourceSystemCode(dto.getSourceSystemCd());
            ledgerOperation.setOriginalLegalName(dto.getOriginalLegalName());

            if (rateLedgerOperation != null) {
                rateLedgerOperation.setRate(dto.getRate());
                rateLedgerOperation.setAdditionalFilingAmountName(dto.getAdditionalAmountName());
                rateLedgerOperation.setPushToQuickBooks(dto.isPushToQuickBooks());
            } else if (depositFrequencyLedgerOperation != null) {
                depositFrequencyLedgerOperation.setDepositFrequency(dto.getDepositFrequencyCode());
            }

            job.getLedgerOperationCollection().add(ledgerOperation);
            Application.save(ledgerOperation);
        }

        //must set clob after original insert
        job.setOriginalFile(jobDTO.getOriginalFile());
        Application.save(job);

        return processResult;
    }

    private class ValidatedOperationDTO {
        public LedgerOperationDTO dto;
        public Law law;

        private ValidatedOperationDTO(LedgerOperationDTO pDto) {
            dto = pDto;
        }
    }
}
