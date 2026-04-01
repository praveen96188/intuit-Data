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
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateGuidelineGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateTRONGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateVMPGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Core process for adding a service to an existing company.
 *
 * @author Dawn Martens
 */
public class AddServiceCore extends Process implements IProcess {
    private Company domainCompany;
    private SourceSystemCode sourceSystem;
    private String sourceCompanyId;
    private ServiceInfoDTO dtoService;
    private Service service;
    private CompanyService domainCompanyService;
    private Offering mDefaultOffering;
    private CompanyOffering existingDirectDepositCompanyOffering;

    private AddOrUpdateTRONGrantProcessor addOrUpdateTRONGrantProcessor;
    private AddOrUpdateVMPGrantProcessor addOrUpdateVMPGrantProcessor;
    private AddOrUpdateGuidelineGrantProcessor addOrUpdateGuidelineGrantProcessor;


    public AddServiceCore(SourceSystemCode pSourceSystemCd, String pCompanyId, ServiceInfoDTO pCompanyService) {
        sourceSystem = pSourceSystemCd;
        sourceCompanyId = pCompanyId;
        dtoService = pCompanyService;
    }

    public ProcessResult process() {
        ProcessResult<CompanyService> processResult = new ProcessResult<CompanyService>();

        //todo refactor
        switch (dtoService.getServiceCode()) {
            case DirectDeposit:
                domainCompanyService = new DDCompanyServiceInfo();
                AddServiceDD addDDServiceProcess = new AddServiceDD(
                        (DDCompanyServiceInfo) domainCompanyService, (DDServiceInfoDTO) dtoService);
                processResult.merge(addDDServiceProcess.execute());
                break;
            case Tax:
                domainCompanyService = new TaxCompanyServiceInfo();
                domainCompanyService.setCompany(domainCompany);
                domainCompanyService.setService(service);
                domainCompanyService.setStatusCd(domainCompanyService.getNextValidServiceStatus(null));
                domainCompanyService.setStatusEffectiveDate(PSPDate.getPSPTime());
                AddServiceTax addTaxServiceProcess = new AddServiceTax((TaxCompanyServiceInfo) domainCompanyService, (TaxServiceInfoDTO) dtoService);
                processResult.merge(addTaxServiceProcess.execute());
                break;
            case ThirdParty401k:
                domainCompanyService = new ThirdParty401kCompanyServiceInfo();
                // early setting these values required due to AddService401k processing
                domainCompanyService.setCompany(domainCompany);
                domainCompanyService.setService(service);
                domainCompanyService.setStatusCd(domainCompanyService.getNextValidServiceStatus(null));
                domainCompanyService.setStatusEffectiveDate(PSPDate.getPSPTime());
                AddService401k add401kServiceProcess = new AddService401k(
                        (ThirdParty401kCompanyServiceInfo) domainCompanyService, (ThirdParty401kServiceInfoDTO) dtoService);
                processResult.merge(add401kServiceProcess.execute());
                break;
            case BillPayment:
                domainCompanyService = new BPCompanyServiceInfo();
                ((BPCompanyServiceInfo) domainCompanyService).setOverrideCompanyLimitAmount(null);
                ((BPCompanyServiceInfo) domainCompanyService).setOverridePayeeLimitAmount(null);
                AddServiceBillPayment addServiceBillPaymentProcess = new AddServiceBillPayment(domainCompany, dtoService);
                processResult.merge(addServiceBillPaymentProcess.process());
                break;
            case CheckDistribution:
                AddServiceCheckDistribution addServiceCheckDistributionProcess = new AddServiceCheckDistribution(
                        domainCompany, (CheckDistributionServiceInfoDTO) dtoService);
                ProcessResult<CDCompanyServiceInfo> result = addServiceCheckDistributionProcess.process();
                domainCompanyService = result.getResult();
                processResult.merge(result);
                break;
            case Cloud:
                AddServiceCloud addServiceCloud = new AddServiceCloud(domainCompany);
                ProcessResult<CompanyService> cloudPR = addServiceCloud.process();
                domainCompanyService = cloudPR.getResult();
                processResult.merge(cloudPR);
                break;
            case WorkersComp:
                // Turn on CloudV2 first
                if (!domainCompany.isCompanyOnService(ServiceCode.CloudV2)) {
                    if (domainCompany.hasService(ServiceCode.CloudV2)) {
                        processResult.merge(
                                PayrollServices.companyManager.reactivateService(
                                        domainCompany.getSourceSystemCd(),
                                        domainCompany.getSourceCompanyId(),
                                        ServiceCode.CloudV2));
                    }
                    else {
                        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
                        serviceInfoDTO.setServiceCode(ServiceCode.CloudV2);
                        serviceInfoDTO.setOfferingCode(OfferingInfoDTO.CLOUD_V2.getOfferingCode());
                        processResult.merge(
                                PayrollServices.companyManager.addService(
                                        domainCompany.getSourceSystemCd(),
                                        domainCompany.getSourceCompanyId(),
                                        serviceInfoDTO));
                    }
                }
                domainCompanyService = new CompanyService();
                AddServiceWorkersComp addServiceWorkersComp = new AddServiceWorkersComp(domainCompanyService, dtoService);
                processResult.merge(addServiceWorkersComp.process());
                break;
            case ViewMyPaycheck:
                // Turn on CloudV2 first
                if (!domainCompany.isCompanyOnService(ServiceCode.CloudV2)) {
                    ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
                    serviceInfoDTO.setServiceCode(ServiceCode.CloudV2);
                    serviceInfoDTO.setOfferingCode(OfferingInfoDTO.CLOUD_V2.getOfferingCode());
                    processResult.merge(
                            PayrollServices.companyManager.addService(
                                    domainCompany.getSourceSystemCd(),
                                    domainCompany.getSourceCompanyId(),
                                    serviceInfoDTO));
                }
                //Proceed with adding VMP
                AddServiceViewMyPaycheck addServiceViewMyPaycheck = new AddServiceViewMyPaycheck();
                ProcessResult<CompanyService> vmpPR = addServiceViewMyPaycheck.process();
                domainCompanyService = vmpPR.getResult();
                processResult.merge(vmpPR);
                break;
            case CloudV2:
                AddServiceCloudV2 addServiceCloudV2 = new AddServiceCloudV2(domainCompany);
                ProcessResult<CompanyService> cloudV2PR = addServiceCloudV2.process();
                domainCompanyService = cloudV2PR.getResult();
                processResult.merge(cloudV2PR);
                break;

            default:
                domainCompanyService = new CompanyService();
                break;
        }

        //Set the company on the company service
        domainCompanyService.setCompany(domainCompany);
        domainCompanyService.setService(service);
        domainCompanyService.setStatusCd(domainCompanyService.getNextValidServiceStatus(null));
        domainCompanyService.setStatusEffectiveDate(PSPDate.getPSPTime());

        if (dtoService.getServiceStartDate() != null) {
            domainCompanyService.setServiceStartDate(dtoService.getServiceStartDate());
        }

        // If the service has a specific funding model, set the funding model at the service level. Otherwise the
        // service will follow the company's funding model
        if (dtoService.getFundingModel() != null) {
            domainCompanyService.setFundingModel(dtoService.getFundingModel());
        }

        domainCompanyService = Application.save(domainCompanyService);
        // hack added b/c 401k needs service added earlier
        if (dtoService.getServiceCode().notIn(ServiceCode.ThirdParty401k, ServiceCode.Tax)) {
            domainCompany.addCompanyService(domainCompanyService);
        }

        //Create initial workers comp paychecks
        if(ServiceCode.WorkersComp == dtoService.getServiceCode()) {
            processResult.merge(AddServiceWorkersComp.createWorkersCompPaychecks(domainCompanyService));
        }

        // See if they have Assisted service.
        CompanyService existingAssistedCompanyService = CompanyService.findCompanyService(domainCompany, ServiceCode.Tax );

        // Existing Assisted customers adding Bill Payment service will use the Assisted Bill Payment offering.
        if (existingAssistedCompanyService != null && ServiceCode.BillPayment == dtoService.getServiceCode()) {
            Offering assistedBillPaymentOffering = Offering.findByOfferingCode(OfferingCode.BillPaymentSTDFY16);
            CompanyOffering domainCompanyOffering = new CompanyOffering();
            domainCompanyOffering.setOffering(assistedBillPaymentOffering);
            domainCompanyOffering.setCompany(domainCompany);
            domainCompanyOffering = Application.save(domainCompanyOffering);
            domainCompany.addCompanyOffering(domainCompanyOffering);
        // Migrating from DIY to DD/assisted or new set up DD/assisted
        } else if(existingDirectDepositCompanyOffering == null || mDefaultOffering.getServiceCode() != ServiceCode.DirectDeposit) {
            CompanyOffering domainCompanyOffering = new CompanyOffering();
            domainCompanyOffering.setOffering(mDefaultOffering);
            domainCompanyOffering.setCompany(domainCompany);
            domainCompanyOffering = Application.save(domainCompanyOffering);
            domainCompany.addCompanyOffering(domainCompanyOffering);
        } else if (existingDirectDepositCompanyOffering.getOffering().getOfferingCode().in(OfferingCode.DIYDDSTD, OfferingCode.DIYDDSTD3,OfferingCode.DIYDDFY14,OfferingCode.DIYDDFY143,OfferingCode.DIYDDFY15,OfferingCode.DIYDDFY153,OfferingCode.DIYDDFY16,OfferingCode.DIYDDFY163) &&
                domainCompany.getCompanyService(ServiceCode.DirectDeposit).isPending()) {
            //Initial sign for Assisted, since DD is still in pending
            domainCompany.removeCompanyOffering(existingDirectDepositCompanyOffering);
            Application.delete(existingDirectDepositCompanyOffering); // Deleting existing Assisted CompanyOffering
            CompanyOffering companyOffering = new CompanyOffering();
            companyOffering.setOffering(mDefaultOffering);
            companyOffering.setCompany(domainCompany);
            companyOffering = Application.save(companyOffering);
            domainCompany.addCompanyOffering(companyOffering);

            CompanyEvent.createOfferingUpdatedEvent(domainCompany, existingDirectDepositCompanyOffering.getOffering().getSKU(), mDefaultOffering.getSKU());
        }

        // todo: do we need to have this event when adding Tax service
        CompanyEvent.createCustomerSignedUpEvent(domainCompanyService);

        //Find already-existing companies that have certain criteria in common with the new company to be added
        //Don't "save" domainCompany before querying (done in companyMeetsFraudCriteria) because of auto flushing
        // the cache
        if (service.getPSPProvidesCustomerService() && !doesPSPAlreadyProvideCustomerService(service)) {
            StringBuilder fraudNotes = new StringBuilder();
            if (domainCompany.companyMeetsFraudCriteria(fraudNotes)) {
                processResult.getMessages().CompanyPendingActivation(EntityName.Company, domainCompany.getSourceCompanyId(),
                        domainCompany.getSourceSystemCd().toString(), domainCompany.getSourceCompanyId());
                //But do save now before adding to HoldReason collection (because it is new, domainCompany needs to be
                //added to the hibernate session (which is done upon .save)
                domainCompany = Application.save(domainCompany);
                processResult.merge(PayrollServices.companyManager.addOnHoldReason(domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), ServiceSubStatusCode.FraudReview));

                //Create CompanyMatchesFraudulentCompany event
                CompanyEvent.createFraudSignUpEvent(domainCompany, EventTypeCode.CompanyMatchesFraudulentCompany, fraudNotes.toString());
            }
        }

        domainCompany = Application.save(domainCompany);

        if (domainCompany.getSourceSystemCd().equals(SourceSystemCode.QBDT)
                && (dtoService.getServiceCode() == ServiceCode.Tax || dtoService.getServiceCode() == ServiceCode.DirectDeposit)) {
            CompanyEvent.createOFXServiceActivatedEvent(domainCompanyService);
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

        processResult.setResult(domainCompanyService);

        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(domainCompanyService.getNaturalKey(), domainCompanyService.getId());
        }

        return processResult;
    }

    private boolean doesPSPAlreadyProvideCustomerService(Service service) {
        for (CompanyService currentService : domainCompany.getCompanyServiceCollection()) {
            //Exclude this service
            if (currentService.getService().getServiceCd() != service.getServiceCd() && currentService.getService().getPSPProvidesCustomerService()) {
                return true;
            }
        }
        return false;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Check if company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystem, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Ensure DTO is not null
        if (dtoService == null) {
            validationResult.getMessages().CompanyServiceNotSpecified(EntityName.Service, null);
            return validationResult;
        }

        //Validate DTO
        validationResult.merge(dtoService.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }


        //Check if company exists
        domainCompany = Company.findCompany(sourceCompanyId, sourceSystem);
        if (domainCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystem.toString(), sourceCompanyId);
            return validationResult;
        }

        service = Application.findById(Service.class, dtoService.getServiceCode());

        if (!domainCompany.isAllowedCapability(SystemCapabilityCode.AddService, service.getServiceCd())) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    domainCompany.getSourceSystemCd().toString(),
                    domainCompany.getSourceCompanyId(), SystemCapabilityCode.AddService.toString());
        }

        //Get any service info this company may already have for the given service
        CompanyService existingService = CompanyService.findCompanyService(domainCompany, service.getServiceCd());

        if (existingService != null) {
            if (existingService.getStatusCd().equals(ServiceSubStatusCode.Terminated)) {
                //Company's service agreement was terminated
                validationResult.getMessages()
                        .CompanyAlreadyTerminatedForService(EntityName.Company, sourceCompanyId,
                                sourceSystem.toString(), sourceCompanyId);
            } else {
                //Company already exists on the service
                validationResult.getMessages()
                        .CompanyAlreadyAssociatedWithService(EntityName.Company, sourceCompanyId,
                                sourceSystem.toString(), sourceCompanyId, service.getServiceCd().toString());
            }
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mDefaultOffering = Offering.findDefaultOffering(domainCompany, dtoService.getServiceCode());
        if(mDefaultOffering == null) {
            validationResult.getMessages().CannotFindDefaultOffering(EntityName.Company, sourceCompanyId, domainCompany.getSourceCompanyId(), dtoService.getServiceCode().toString());
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        DomainEntitySet<CompanyOffering> existingOfferings = domainCompany.getCompanyOfferingCollection();
        for (CompanyOffering currentOffering : existingOfferings) {
            if (currentOffering.getOffering().getServiceCode() == ServiceCode.DirectDeposit) {
                existingDirectDepositCompanyOffering = currentOffering;
            } else if(mDefaultOffering.getServiceCode() == currentOffering.getOffering().getServiceCode()) {
                validationResult.getMessages().InvalidValue(EntityName.Company, sourceCompanyId, "Company cannot have two offerings for the same service: " + mDefaultOffering.getServiceCode());
            }
        }

        switch (dtoService.getServiceCode()) {
            case BillPayment:
                AddServiceBillPayment addServiceBillPaymentProcess = new AddServiceBillPayment(domainCompany, dtoService);
                validationResult.merge(addServiceBillPaymentProcess.validate());
                break;
            case CheckDistribution:
                AddServiceCheckDistribution addServiceCheckDistribution = new AddServiceCheckDistribution(domainCompany, (CheckDistributionServiceInfoDTO) dtoService);
                validationResult.merge(addServiceCheckDistribution.validate());
                break;
            case Cloud:
                AddServiceCloud addServiceCloud = new AddServiceCloud(domainCompany);
                validationResult.merge(addServiceCloud.validate());
                break;
            case CloudV2:
                AddServiceCloudV2 addServiceCloudV2 = new AddServiceCloudV2(domainCompany);
                validationResult.merge(addServiceCloudV2.validate());
                break;
            case WorkersComp:
                domainCompanyService = new CompanyService();
                domainCompanyService.setCompany(domainCompany);
                AddServiceWorkersComp addServiceWorkersComp = new AddServiceWorkersComp(domainCompanyService, dtoService);
                validationResult.merge(addServiceWorkersComp.validate());
                break;
            case ViewMyPaycheck:
                AddServiceViewMyPaycheck addServiceViewMyPaycheck = new AddServiceViewMyPaycheck();
                validationResult.merge(addServiceViewMyPaycheck.validate());
                break;
        }

        //make sure no other ein (excluding current sourceCompanyId and SourceSystemCode- IOP) has a active non cloud service. IOP - Redundant EINs allowed.
        if (!dtoService.getServiceCode().equals(ServiceCode.CloudV2) && !dtoService.getServiceCode().equals(ServiceCode.Cloud) && !domainCompany.getSourceSystemCd().equals(SourceSystemCode.IOP)) {
            DomainEntitySet<CompanyService> companyServices = CompanyService.findActiveCompanyServicesByFeinExcludingSourceSystemIdAndServiceCode
                    (domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), domainCompany.getFedTaxId(), ServiceCode.Cloud);
            if (!companyServices.isEmpty()) {
                CompanyService companyService = companyServices.get(0);
                validationResult.getMessages().ActiveServiceExistsForEin(EntityName.CompanyService, domainCompany.getSourceCompanyId(),
                        dtoService.getServiceCode().toString(), domainCompany.getFedTaxId(), companyService.getService().getServiceCd().toString());
            }
        }

        // Add or update TRON Grant only for Direct Deposit service
        if(dtoService.getServiceCode() == ServiceCode.DirectDeposit || (dtoService.getServiceCode() == ServiceCode.Tax && domainCompany.isCompanyOnService(ServiceCode.DirectDeposit))) {
            addOrUpdateTRONGrantProcessor = new AddOrUpdateTRONGrantProcessor(domainCompany);
            validationResult.merge(addOrUpdateTRONGrantProcessor.validate());
        }

        // Add or update Guideline Grant only for Guideline service
        if(dtoService.getServiceCode() == ServiceCode.Guideline401k) {
            addOrUpdateGuidelineGrantProcessor = new AddOrUpdateGuidelineGrantProcessor(domainCompany);
            validationResult.merge(addOrUpdateGuidelineGrantProcessor.validate());
        }

        // Add or update VMP grant only for VMP Service
        if(dtoService.getServiceCode() == ServiceCode.ViewMyPaycheck) {
            addOrUpdateVMPGrantProcessor = new AddOrUpdateVMPGrantProcessor(domainCompany);
            validationResult.merge(addOrUpdateVMPGrantProcessor.validate());
        }

        return validationResult;
    }
}
