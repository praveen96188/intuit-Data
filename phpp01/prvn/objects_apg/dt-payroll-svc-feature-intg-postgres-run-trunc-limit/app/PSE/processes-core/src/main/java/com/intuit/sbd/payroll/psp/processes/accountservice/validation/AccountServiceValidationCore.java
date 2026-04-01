package com.intuit.sbd.payroll.psp.processes.accountservice.validation;

import com.google.gson.Gson;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdditionalInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGateway;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.ValidationResultAccumulator;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.ValidationType;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory.ValidationStepsFactory;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountServiceValidationCore extends Process {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(AccountServiceValidationCore.class);


    private CompanyDTO mDtoCompany;
    private Company mDomainCompany;
    private AddressDTO mAddressDTO;
    private AddressTypeEnum addressType;
    private boolean shouldPersist;
    private boolean fullValidation=true;
    private ValidationType validationType;
    private List<ValidationStep<SMSValidationResult>> validationSteps = new ArrayList<>();

    /**
     * This instance will lead to full validation
     * for exhaustive list of validations carried out see {@link
     * com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory.ValidationStepsFactory#getFullValidationSteps}
     * @param pDtoCompany companyDto object
     * @param pDomainCompany domain company
     * @param persist flag to indicate whether to persist the validation result
     * @param  fullValidation flag to indicate whether to do full validation or not
     */
   /* public AccountServiceValidationCore(CompanyDTO pDtoCompany, Company pDomainCompany, boolean persist,boolean fullValidation) {
        this(pDomainCompany, persist);
        this.mDtoCompany = pDtoCompany;
        this.fullValidation = fullValidation;
    }*/


    /**
     * Client can specify the type of validation
     * @param pDtoCompany
     * @param pDomainCompany
     * @param persist
     * @param validationType
     */
    public AccountServiceValidationCore(CompanyDTO pDtoCompany, Company pDomainCompany, boolean persist, ValidationType validationType) {
        this(pDomainCompany, persist);
        this.mDtoCompany = pDtoCompany;
        this.validationType = validationType;
    }

    /**
     * Client can specify the list of validation steps to be executed
     * @param pDtoCompany
     * @param pDomainCompany
     * @param persist
     * @param validationSteps
     */
    public AccountServiceValidationCore(CompanyDTO pDtoCompany, Company pDomainCompany, boolean persist,
                                        List<ValidationStep<SMSValidationResult>> validationSteps) {
        this(pDomainCompany, persist);
        this.mDtoCompany = pDtoCompany;
        this.validationSteps = validationSteps;
    }




    private AccountServiceValidationCore(Company pDomainCompany, boolean persist) {
        this.mDomainCompany = pDomainCompany;
        this.shouldPersist = persist;

    }

    /** This instance will only do the address validation
     * @param mAddressDTO    addressDto object
     * @param addressType    AddressType should be COMPANY for businsess address, and Residence for Primary principal
     *                       and Legal for compliance Address
     * @param pDomainCompany domain company
     */
    public AccountServiceValidationCore(AddressDTO mAddressDTO, AddressTypeEnum addressType, Company pDomainCompany, boolean persist) {
        this(pDomainCompany, persist);
        this.mAddressDTO = mAddressDTO;
        this.addressType = addressType;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();
        try {

            if (Objects.isNull(mDomainCompany)) {
                validationResult.getMessages()
                        .BadProcessArgument("DomainCompany ");
                return validationResult;
            }
            if (Objects.isNull(mDtoCompany) && Objects.isNull(mAddressDTO)) {
                validationResult.getMessages()
                        .BadProcessArgument("DtoCompany and mAddressDTO both can't be null");
                return validationResult;
            }
            Contact primaryPrincipal = mDomainCompany.getContactByRoleCode(ContactRole.PrimaryPrincipal);
            if (Objects.isNull(primaryPrincipal)) {
                validationResult.getMessages()
                        .BadProcessArgument("DomainCompany.PrimaryPrincipal");
                return validationResult;
            }
       /* //todo check if we need to do it for dtocompany also
        if(Objects.isNull(primaryPrincipal.getDateOfBirth())){
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany.PrimaryPrincipal.DateOfBirth");
            return validationResult;
        }
        if(Objects.isNull(primaryPrincipal.getSocialSecurityNumber())){
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany.PrimaryPrincipal.SSN");
            return validationResult;
        }*/

        } catch (Exception e) {
            logger.info("Exception while validation " + e);
            validationResult.getMessages().ExceptionOccurred(
                    "Exception while validating details of data for account service", e);
            return validationResult;
        }
        return validationResult;
    }


  /*  @Override
    public ProcessResult process() {
        ProcessResult<SMSValidationResult> processResult = new ProcessResult();
        logger.info("Validation started for company " + mDomainCompany.getSourceCompanyId());
        SMSValidationResult smsValidationResult = new SMSValidationResult();
        SMSMigrationStatus smsMigrationStatus = SMSMigrationStatus.NeedsValidation;

        Gson gson = new Gson();

        if (Objects.nonNull(mAddressDTO)) {
            try {
                smsValidationResult = executeAddressValidationSteps(mAddressDTO);
                processResult.setResult(smsValidationResult);
                logger.info("The validation result for the address " + mAddressDTO + " is " + smsValidationResult);
            } catch (Exception e) {
                smsMigrationStatus = SMSMigrationStatus.ValidationInternalError;
                logger.error("Validation completed with error " + e.getMessage());
                processResult.getMessages().ExceptionOccurred("Unknown Error in validating the CompanyInfo for psid"
                        + mDomainCompany.getSourceCompanyId() + " Error:" + e.getMessage());

            }

        }
        else if(!fullValidation){
            //do the PPO address validation
            // compliance address validation and payments account validation
            //sometimes dto may not have industry type, so use this , mostly called from PAPI as of now
            CompanyAdditionalInfoDTO cafDto = new CompanyAdditionalInfoDTO();
            CompanyAdditionalInfo caf = mDomainCompany.getCompanyAdditionalInfo();
            cafDto.setIndustry(caf.getIndustryType().getIndustry());
            mDtoCompany.setCompanyAdditionalInfo(cafDto);

            smsValidationResult = executePartialValidationSteps(mDtoCompany);
            logger.info("Partial Validation completed for "+mDomainCompany.getSourceCompanyId());

        }
        else {

            try {
                //sometimes dto may not have industry type, so use this
                CompanyAdditionalInfoDTO cafDto = new CompanyAdditionalInfoDTO();
                CompanyAdditionalInfo caf = mDomainCompany.getCompanyAdditionalInfo();
                cafDto.setIndustry(caf.getIndustryType().getIndustry());
                mDtoCompany.setCompanyAdditionalInfo(cafDto);

          *//*  PrimaryBusiness primaryBusiness = new PrimaryBusiness();
            //prepopulated with a fixed ownership type
            //TODO revisit the ownershiptype
            primaryBusiness.setOwnershipType(OwnershipTypeEnum.SOLE_PROPRIETORSHIP);
            primaryBusiness.setEin(mDtoCompany.getFein());

            // mDtoCompany.setCompanyAdditionalInfo(cafDto);
            PaymentsAccount paymentAccountToValidate = accountServiceTranslator.createPaymentsAccount(primaryBusiness, new BusinessOwner(), mDtoCompany);
            paymentAccountToValidate.setRealmId(mDtoCompany.getIAMRealmId());
            enrichPaymentAccounRequest(paymentAccountToValidate);*//*

                smsValidationResult = executeFullValidationSteps(mDtoCompany);

               logger.info("Full Validation completed for "+mDomainCompany.getSourceCompanyId());

            } catch (Exception e) {
                smsMigrationStatus = SMSMigrationStatus.ValidationInternalError;
                logger.error("Validation completed with error " + e.getMessage());
                processResult.getMessages().ExceptionOccurred("Unknown Error in validating the CompanyInfo for psid"
                        + mDomainCompany.getSourceCompanyId() + " Error:" + e.getMessage());

            }
        }
        smsMigrationStatus = smsValidationResult.isValidationPassed() ? SMSMigrationStatus.ValidationSuccess : SMSMigrationStatus.ValidationError;
       // propagateErrorToSap(processResult, smsValidationResult);

        processResult.setResult(smsValidationResult);

        if (this.shouldPersist) {
            SMSMigration.saveSMSValidationResult(mDomainCompany.getSourceCompanyId(), gson.toJson(smsValidationResult), smsMigrationStatus);

        }
        logger.info("The validation result for the company " + mDomainCompany.getSourceCompanyId() + " is " + smsValidationResult);
        return processResult;
    }*/





    @Override
    public ProcessResult process() {
        ProcessResult<SMSValidationResult> processResult = new ProcessResult();

        try{
            Gson gson = new Gson();
            logger.info("Validation started for company " + mDomainCompany.getSourceCompanyId());
            SMSValidationResult smsValidationResult = new SMSValidationResult();
            SMSMigrationStatus smsMigrationStatus = SMSMigrationStatus.NeedsValidation;

            if(Objects.nonNull(this.mAddressDTO)){
                /* this will be called from QBDT/SAP while updating the address*/
                try {
                    smsValidationResult = executeAddressValidationSteps(mAddressDTO);
                    processResult.setResult(smsValidationResult);
                    logger.info("The validation result for  address of addressType "+this.addressType+" psid " + mDomainCompany.getSourceCompanyId() + " is " + gson.toJson(smsValidationResult));
                } catch (Exception e) {
                    smsMigrationStatus = SMSMigrationStatus.ValidationInternalError;
                    logger.error("Address Validation completed with error " + e.getMessage());
                    processResult.getMessages().ExceptionOccurred("Unknown Error in validating the CompanyInfo for psid"
                            + mDomainCompany.getSourceCompanyId() + " Error:" + e.getMessage());

                }

            }

            if(Objects.nonNull(this.validationType)){
                switch (this.validationType){
                    case FULL_VALIDATION:
                        /* this flow will be called by Batch job*/
                        try {
                            smsValidationResult = executeFullValidation(smsValidationResult);

                            logger.info("Full Validation completed for "+mDomainCompany.getSourceCompanyId());

                        } catch (Exception e) {
                            smsMigrationStatus = SMSMigrationStatus.ValidationInternalError;
                            logger.error("Full Validation completed with error " + e.getMessage());
                            processResult.getMessages().ExceptionOccurred("Unknown Error in validating the CompanyInfo for psid"
                                    + mDomainCompany.getSourceCompanyId() + " Error:" + e.getMessage());

                        }
                        break;
                    case PARTIAL_VALIDATION:
                        /* this flow will be called from PAPI while updating the address*/
                        try {
                            smsValidationResult = executePartialValidation(smsValidationResult);
                            logger.info("Partial Validation completed for "+mDomainCompany.getSourceCompanyId());
                        } catch (Exception e) {
                            smsMigrationStatus = SMSMigrationStatus.ValidationInternalError;
                            logger.error("Partial Validation completed with error " + e.getMessage());
                            processResult.getMessages().ExceptionOccurred("Unknown Error in validating the CompanyInfo for psid"
                                    + mDomainCompany.getSourceCompanyId() + " Error:" + e.getMessage());

                        }
                        break;

                }

            }
            else if(this.validationSteps.size()>0){
                smsValidationResult = executeStepsAndMergeResult(this.validationSteps);

            }
        /*else{
            logger.error("Invalid execution type of validation : " +
                    "neither addressDTO was specified nor ValidationType nor list of validations was passed ");
        }*/

            smsMigrationStatus = smsValidationResult.isValidationPassed() ? SMSMigrationStatus.ValidationSuccess : SMSMigrationStatus.ValidationError;

            processResult.setResult(smsValidationResult);

            if (this.shouldPersist) {
                SMSMigration.saveSMSValidationResult(mDomainCompany.getSourceCompanyId(), gson.toJson(smsValidationResult), smsMigrationStatus);

            }
            logger.info("The validation result for the company " + mDomainCompany.getSourceCompanyId() + " is " + gson.toJson(smsValidationResult));
        }catch (Exception ex){
            logger.info("The validation result for the company " + mDomainCompany.getSourceCompanyId() );
            processResult.getMessages().ExceptionOccurred("Unknown Error in validating the CompanyInfo for psid"
                    + mDomainCompany.getSourceCompanyId() + " Error:" + ex.getMessage());
        }
       
        return processResult;


    }

    private SMSValidationResult executePartialValidation(SMSValidationResult smsValidationResult) {


        smsValidationResult = executePartialValidationSteps(mDtoCompany);
        return smsValidationResult;
    }

    private SMSValidationResult executeFullValidation(SMSValidationResult smsValidationResult) {

        smsValidationResult = executeFullValidationSteps(mDtoCompany);
        return smsValidationResult;
    }


    private SMSValidationResult executeFullValidationSteps(CompanyDTO companyDTO) {
        List<ValidationStep<SMSValidationResult>> fullValidationSteps = ValidationStepsFactory.getFullValidationSteps(companyDTO,mDomainCompany.getSourceCompanyId());

        SMSValidationResult smsValidationResult = executeStepsAndMergeResult(fullValidationSteps);

        return smsValidationResult;
    }

    private SMSValidationResult executePartialValidationSteps(CompanyDTO companyDTO) {
        List<ValidationStep<SMSValidationResult>> partialValidationSteps = ValidationStepsFactory.getPartialValidationSteps(companyDTO,mDomainCompany.getSourceCompanyId());

        SMSValidationResult smsValidationResult = executeStepsAndMergeResult(partialValidationSteps);

        return smsValidationResult;
    }


    private SMSValidationResult executeAddressValidationSteps(AddressDTO mAddressDTO) {
        List<ValidationStep<SMSValidationResult>> addressValidationSteps = ValidationStepsFactory.getAddressValidationSteps(mAddressDTO, this.addressType);
        SMSValidationResult smsValidationResult = executeStepsAndMergeResult(addressValidationSteps);
        return smsValidationResult;

    }

    /**
     * Takes a list of validation steps and executes all the validation a
     * and combines the result through a reducers
     *
     * @param validationSteps
     * @return
     */
    private SMSValidationResult executeStepsAndMergeResult(List<ValidationStep<SMSValidationResult>> validationSteps) {

        SMSValidationResult identitySMSValidationResult = new SMSValidationResult();
        identitySMSValidationResult.setValidationPassed(true);

        return validationSteps.stream().map(validationStep -> validationStep.process())
                .reduce(identitySMSValidationResult, (previousMergedSMSValidationResult, currentSMSValidationResult) -> {
                    return new ValidationResultAccumulator().merge(previousMergedSMSValidationResult, currentSMSValidationResult);
                });
    }


}
