/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/ReactivateServiceCore.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateGuidelineGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateTRONGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateVMPGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.Objects;

/**
 * Core process for reactivating an "inactive" service that a company already has.
 *
 * @author Dawn Martens
 */
public class ReactivateServiceCore extends Process implements IProcess {
    private CompanyService companyService;
    private ServiceCode enumServiceCodeDTO;
    
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;

    private Company existingCompany;

    private AddOrUpdateTRONGrantProcessor addOrUpdateTRONGrantProcessor;
    private AddOrUpdateVMPGrantProcessor addOrUpdateVMPGrantProcessor;
    private AddOrUpdateGuidelineGrantProcessor addOrUpdateGuidelineGrantProcessor;


    public CompanyService getCompanyService() {
        return companyService;
    }

    public ReactivateServiceCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyID, ServiceCode pDTOServiceCd) {
        sourceSystemCd=pSourceSystemCd;
        sourceCompanyId=pSourceCompanyID;
        enumServiceCodeDTO = pDTOServiceCd;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        companyService.updateCompanyServiceStatus(companyService.getNextValidServiceStatus(ServiceSubStatusCode.Cancelled));

        companyService = Application.save(companyService);
        existingCompany = Application.save(existingCompany);

        // If Tax Service execute ReactivateServiceTaxCore
        if (companyService.getService().getServiceCd() == com.intuit.sbd.payroll.psp.domain.ServiceCode.Tax) {
            ReactivateServiceTax reactivateTaxServiceProcess = new ReactivateServiceTax(companyService, ServiceCode.Tax);
            processResult.merge(reactivateTaxServiceProcess.execute());
        }

        // If VMP or WC, reactivate CloudV2 if needed
        if (companyService.getService().getServiceCd().in(ServiceCode.ViewMyPaycheck, ServiceCode.WorkersComp)
                && !existingCompany.isCompanyOnService(ServiceCode.CloudV2)) {

            if (existingCompany.hasService(ServiceCode.CloudV2)) {
                processResult.merge(
                        PayrollServices.companyManager.reactivateService(
                                sourceSystemCd,
                                sourceCompanyId,
                                ServiceCode.CloudV2));
            }
            else {
                ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
                serviceInfoDTO.setServiceCode(ServiceCode.CloudV2);
                serviceInfoDTO.setOfferingCode(OfferingInfoDTO.CLOUD_V2.getOfferingCode());
                processResult.merge(
                        PayrollServices.companyManager.addService(
                                existingCompany.getSourceSystemCd(),
                                existingCompany.getSourceCompanyId(),
                                serviceInfoDTO));
            }
        }

        // If WorkersComp Service, execute ReactivateServiceWorkersComp
        if (companyService.getService().getServiceCd() == ServiceCode.WorkersComp) {
            ReactivateServiceWorkersComp reactivateWCServiceProcess = new ReactivateServiceWorkersComp(companyService);
            processResult.merge(reactivateWCServiceProcess.execute());
        }

        if(Objects.nonNull(addOrUpdateTRONGrantProcessor)) {
            processResult.merge(addOrUpdateTRONGrantProcessor.process());
        }

        if(Objects.nonNull(addOrUpdateGuidelineGrantProcessor)) {
            processResult.merge(addOrUpdateGuidelineGrantProcessor.process());
        }

        // Check for the addOrUpdateVMPGrantProcessor object,
        // if not null will try to add grant if the request has the special header i.e intuit_autovmpsource
        if(Objects.nonNull(addOrUpdateVMPGrantProcessor)) {
            processResult.merge(addOrUpdateVMPGrantProcessor.process());
        }

        processResult.setResult(companyService);
        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Ensure valid company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Ensure service is valid
        Service domainService = ProcessesToDTO.getDomainService(enumServiceCodeDTO);
        if (domainService == null) {
            validationResult.getMessages().CompanyServiceNotSpecified(EntityName.Service, null);
            return validationResult;
        }

        //Validate company exists
        existingCompany = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (existingCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        //Ensure the company has the service
        companyService = CompanyService.findCompanyService(existingCompany, domainService.getServiceCd());
        if (companyService == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                    existingCompany.getSourceCompanyId(),
                    existingCompany.getSourceSystemCd().toString(), existingCompany.getSourceCompanyId(),
                    domainService.getServiceCd().toString());
            return validationResult;
        }

        //Get the status for the service
        ServiceSubStatusCode existingServiceSubStatusCd = companyService.getStatusCd();

        //We'll reactive in process() any company that is "inactive" (e.g. cancelled itself)
        if (existingServiceSubStatusCd != ServiceSubStatusCode.Cancelled ) {
            //Company already active/on hold/pending term on the service
            validationResult.getMessages()
                    .CompanyAlreadyAssociatedWithService(EntityName.Company, existingCompany.getSourceCompanyId(),
                            existingCompany.getSourceSystemCd().toString(),
                            existingCompany.getSourceCompanyId(),
                            domainService.getServiceCd().toString());
            return validationResult;
        }

        if (! existingCompany.isAllowedCapability(SystemCapabilityCode.AddService)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                existingCompany.getSourceSystemCd().toString(),
                existingCompany.getSourceCompanyId(), SystemCapabilityCode.AddService.toString());
        }

        // Add or update TRON Grant only for Direct Deposit service
        if(enumServiceCodeDTO == ServiceCode.DirectDeposit) {
            addOrUpdateTRONGrantProcessor = new AddOrUpdateTRONGrantProcessor(existingCompany);
            validationResult.merge(addOrUpdateTRONGrantProcessor.validate());
        }

        // Add or update Guideline Grant only for Guideline service
        if(enumServiceCodeDTO == ServiceCode.Guideline401k) {
            addOrUpdateGuidelineGrantProcessor = new AddOrUpdateGuidelineGrantProcessor(existingCompany);
            validationResult.merge(addOrUpdateGuidelineGrantProcessor.validate());
        }

        // Add or update VMP grant only for VMP Service
        if(enumServiceCodeDTO == ServiceCode.ViewMyPaycheck) {
            addOrUpdateVMPGrantProcessor = new AddOrUpdateVMPGrantProcessor(existingCompany);
            validationResult.merge(addOrUpdateVMPGrantProcessor.validate());
        }

        return validationResult;
    }
}
