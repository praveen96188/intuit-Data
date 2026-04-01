package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntityChangeDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EntityChange;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * User: rnorian
 * Date: Feb 7, 2011
 * Time: 2:05:01 PM
 */
public class AddOrUpdateEntityChangeCore extends Process implements IProcess {
    private static SpcfLogger logger = PayrollServices.getLogger(AddOrUpdateEntityChangeCore.class);

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private EntityChangeDTO entityChangeDTO;

    private Company company;

    public AddOrUpdateEntityChangeCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, EntityChangeDTO pEntityChangeDTO) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        entityChangeDTO = pEntityChangeDTO;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (entityChangeDTO == null) {
            validationResult.getMessages().BadProcessArgument("entityChangeDTO");
        }

        if (entityChangeDTO.getOldEIN() != null && !entityChangeDTO.getOldEIN().equals("0") && !com.intuit.sbd.payroll.psp.util.Validator.isValidEIN(entityChangeDTO.getOldEIN())) {
            validationResult.getMessages().InvalidValue(EntityName.EntityChange, sourceCompanyId, "OldEIN", entityChangeDTO.getOldEIN());
        }


        if (!com.intuit.sbd.payroll.psp.util.Validator.isValidEIN(entityChangeDTO.getNewEIN())) {
            validationResult.getMessages().InvalidValue(EntityName.EntityChange, sourceCompanyId, "NewEIN", entityChangeDTO.getNewEIN());
        }

        if (entityChangeDTO.getEffectiveDate() == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntityChange, sourceCompanyId, "EffectiveDate", entityChangeDTO.getEffectiveDate());
        }
        validationResult.merge(entityChangeDTO.getEffectiveDate().validate());

        if (!entityChangeDTO.getNewEIN().equals(company.getFedTaxId())) {
            //todo_rhn: add error message
        }

        return validationResult;
    }

    @Override
    public ProcessResult<EntityChange> process() {
        ProcessResult<EntityChange> pr = new ProcessResult<EntityChange>();
        DomainEntitySet<EntityChange> existingRecords = null;
        List<String> newEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntityChange.NewEinKeyName, entityChangeDTO.getNewEIN());
        existingRecords = company.getEntityChangeCollection().find(EntityChange.NewEinEnc().in(newEinEncList));


        boolean isDelete = entityChangeDTO.getOldEIN() == null || entityChangeDTO.getOldEIN().trim().length() == 0 || entityChangeDTO.getOldEIN().equals("0");
        boolean isUpdate = !isDelete && existingRecords.size() == 1;
        boolean isNew = !isDelete && !isUpdate;

        if (isNew) {
            EntityChange entityChange = new EntityChange();
            CompanyEvent.createCompanyInfoChangeEvent(company, entityChangeDTO.getOldEIN(), entityChangeDTO.getNewEIN(), EventTypeCode.EINChanged);
            entityChange.setSourceCompanyId(company.getSourceCompanyId());
            entityChange.setEffectiveDate(entityChangeDTO.getEffectiveDate().toSpcfCalendar());
            entityChange.setOldEIN(entityChangeDTO.getOldEIN());
            entityChange.setNewEIN(entityChangeDTO.getNewEIN());
            entityChange.setAgentId(entityChangeDTO.getUserId());
            entityChange.setCompany(company);
            entityChange.setIsSuccessor(entityChangeDTO.getIsSuccessor());
            entityChange.setIsError(entityChangeDTO.getIsError());
            entityChange.setHasNewDataFile(entityChangeDTO.getHasNewDataFile());
            entityChange = Application.save(entityChange);
            company.getEntityChangeCollection().add(entityChange);
            pr.setResult(entityChange);
        } else if (isUpdate) {
            EntityChange entityChange = existingRecords.get(0);
            entityChange.setEffectiveDate(entityChangeDTO.getEffectiveDate().toSpcfCalendar());
            entityChange.setOldEIN(entityChangeDTO.getOldEIN());
            entityChange.setNewEIN(entityChangeDTO.getNewEIN());
            entityChange.setCompany(company);
            entityChange.setAgentId(entityChangeDTO.getUserId());
            entityChange.setIsSuccessor(entityChangeDTO.getIsSuccessor());
            entityChange.setIsError(entityChangeDTO.getIsError());
            entityChange.setHasNewDataFile(entityChangeDTO.getHasNewDataFile());
            entityChange = Application.save(entityChange);
            pr.setResult(entityChange);
        } else if (isDelete && existingRecords.size() == 1) {
            // this means the 'CLI_ORIGINAL_FEIN' has been cleared out in the AS400, indicating that the EIN change was accidental
            EntityChange entityChange = existingRecords.get(0);
            company.getEntityChangeCollection().remove(entityChange);
            Application.delete(entityChange);
        } else if (isDelete && existingRecords.size() == 0) {
            logger.error("received EntityChange record with no old EIN but could not find existing EntityChange record in PSP with EIN: " + entityChangeDTO.getNewEIN());
        }

        return pr;
    }
}
