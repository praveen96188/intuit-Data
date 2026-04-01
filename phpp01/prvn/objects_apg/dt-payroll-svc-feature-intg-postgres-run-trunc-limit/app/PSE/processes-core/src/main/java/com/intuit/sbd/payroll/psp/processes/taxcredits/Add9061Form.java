package com.intuit.sbd.payroll.psp.processes.taxcredits;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.TaxCredits9061DTO;
import com.intuit.sbd.payroll.psp.domain.TaxCredits9061;
import com.intuit.sbd.payroll.psp.domain.TaxCreditsApplication;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: dweinberg
 * Date: Jan 25, 2010
 * Time: 12:21:05 PM
 */
public class Add9061Form extends Process {

    TaxCredits9061DTO dto;

    public Add9061Form(TaxCredits9061DTO dto) {
        this.dto = dto;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (dto.getEin() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, "unknown", "EIN not specified");
            return validationResult;
        }

        if (dto.getEmployeeName() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Employee name not specified");
            return validationResult;
        }

        if (dto.getSsn() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Employee SSN not specified");
            return validationResult;
        }

        if (dto.getEmployerEmail() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Employer Email not specified");
            return validationResult;
        }

        if (dto.getEmployeeEmail() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Employee Email not specified");
            return validationResult;
        }

        if (dto.getForm9061() == null || dto.getForm9061().length == 0){
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "9061 cannot be empty");
            return validationResult;
        }

        if (dto.getPacketPassword() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Packet password cannot be empty");
            return validationResult;
        }

        if (dto.getPacketDocumentKey() == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Packet document key cannot be empty");
            return validationResult;
        }

        if (dto.getUnsignedPacket() == null || dto.getUnsignedPacket().length == 0) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Packet cannot be empty");
            return validationResult;            
        }

        DomainEntitySet<TaxCreditsApplication> applications = Application.find(TaxCreditsApplication.class, TaxCreditsApplication.DocumentKey().equalTo(dto.getPacketDocumentKey()));
        if (applications.size() != 0) {
            validationResult.getMessages().GenericError(EntityName.TaxCredits9061, dto.getEin(), "Packet document key already exists");
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult pr = new ProcessResult();

        TaxCreditsApplication application = new TaxCreditsApplication();
        application.setEmployerEmail(dto.getEmployerEmail());
        application.setEmployeeEmail(dto.getEmployeeEmail());
        application.setDocumentKey(dto.getPacketDocumentKey());
        application.setDocumentPassword(dto.getPacketPassword());
        application.setUnsignedDocumentBytes(dto.getUnsignedPacket());
        application.setSignersRemaining("ER, EE");
        Application.save(application);

        TaxCredits9061 tc9061 = new TaxCredits9061();
        tc9061.set9061Bytes(dto.getForm9061());
        tc9061.setEmployeeName(dto.getEmployeeName());
        tc9061.setFedTaxId(dto.getEin());
        tc9061.setSSN(dto.getSsn());
        tc9061.setTaxCreditsApplication(application);
        Application.save(tc9061);

        return pr;
    }
}
