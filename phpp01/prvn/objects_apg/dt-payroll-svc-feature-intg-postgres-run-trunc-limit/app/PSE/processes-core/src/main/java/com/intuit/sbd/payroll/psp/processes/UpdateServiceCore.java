/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Core process for updating a service.
 *
 * @author Dawn Martens
 */
public class UpdateServiceCore extends Process implements IProcess {
    private CompanyService domainCompanyService;
    private ServiceInfoDTO dtoServiceInfo;
    private SourceSystemCode sourceSystem;
    private String sourceCompanyId;

    public UpdateServiceCore(SourceSystemCode pSourceSystem, String pSourceCompanyId,
                             ServiceInfoDTO pServiceToCopyFrom) {
        sourceSystem = pSourceSystem;
        sourceCompanyId = pSourceCompanyId;
        dtoServiceInfo = pServiceToCopyFrom;
    }

    public ProcessResult process() {
        ProcessResult<CompanyService> processResult = new ProcessResult<CompanyService>();

        if (dtoServiceInfo instanceof DDServiceInfoDTO) {
            UpdateServiceDDProcess updateServiceDDProcess = new UpdateServiceDDProcess(
                    (DDCompanyServiceInfo) domainCompanyService, (DDServiceInfoDTO) dtoServiceInfo);
            processResult.merge(updateServiceDDProcess.execute());
        } else if (dtoServiceInfo instanceof ThirdParty401kServiceInfoDTO) {
            UpdateService401kProcess updateService401kProcess = new UpdateService401kProcess(
                    (ThirdParty401kCompanyServiceInfo) domainCompanyService, (ThirdParty401kServiceInfoDTO) dtoServiceInfo);
            processResult.merge(updateService401kProcess.execute());
        } else if(dtoServiceInfo instanceof TaxServiceInfoDTO){
            UpdateServiceTax updateServiceTax = new UpdateServiceTax(domainCompanyService.getCompany(),dtoServiceInfo.getServiceStartDate(), (TaxServiceInfoDTO) dtoServiceInfo);
            processResult.merge(updateServiceTax.execute());
        }

        if(dtoServiceInfo.getServiceStartDate() != null){
            domainCompanyService.setServiceStartDate(dtoServiceInfo.getServiceStartDate());
        }
        domainCompanyService = Application.save(domainCompanyService);

        processResult.setResult(domainCompanyService);
        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystem, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate DTO is not null
        if (dtoServiceInfo == null) {
            validationResult.getMessages().CompanyServiceNotSpecified(EntityName.CompanyService, null);
            return validationResult;
        }

        //Validate DTO
        validationResult.merge(dtoServiceInfo.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        Company existingCompany = Company.findCompany(sourceCompanyId, sourceSystem);
        if (existingCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystem.toString(), sourceCompanyId);
            return validationResult;
        }

        ServiceCode serviceCd;
        if (dtoServiceInfo instanceof DDServiceInfoDTO) {
            serviceCd = ServiceCode.DirectDeposit;
        } else if (dtoServiceInfo instanceof ThirdParty401kServiceInfoDTO) {
            serviceCd = ServiceCode.ThirdParty401k;
        } else if (dtoServiceInfo.getServiceCode().equals(ServiceCode.Tax)){
            serviceCd = ServiceCode.Tax;
        } else if (dtoServiceInfo instanceof WorkersCompServiceInfoDTO){
            serviceCd = ServiceCode.WorkersComp;
        }else if(dtoServiceInfo instanceof CloudV3ServiceInfoDTO){
            serviceCd= ServiceCode.CloudV3;
        }
        else{
            throw new RuntimeException("Invalid ServiceInfoDTO type: "+ dtoServiceInfo);
        }

        domainCompanyService = CompanyService.findCompanyService(existingCompany, serviceCd);
        //Validate company has service
        if (domainCompanyService == null) {
           validationResult.getMessages().CompanyServiceNotSpecified(EntityName.CompanyService, null);
            return validationResult;
        }

        if (! existingCompany.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                existingCompany.getSourceSystemCd().toString(),
                existingCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }

        if (Application.getCurrentPrincipal().isCustomer() && domainCompanyService.additionalCancelTermValidationRequired()) {
            if(!domainCompanyService.isV3Service()) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        domainCompanyService.getCompany().getSourceSystemCd().toString(),
                        domainCompanyService.getCompany().getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            }
        }

        return validationResult;
    }
}
