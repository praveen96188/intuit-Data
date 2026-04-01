package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.payments.cdm.v2.client.*;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.payments.cdm.v2.client.enums.ApplicationSourceEnum;
import com.intuit.payments.cdm.v2.client.enums.OwnershipTypeEnum;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdditionalInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAdditionalInfo;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.IndustryType;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.*;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationStepsFactory {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(ValidationStepsFactory.class);


    private static AccountServiceTranslator accountServiceTranslator =  PayrollApplicationBeanFactory.getBean(AccountServiceTranslator.class);;

    public  static List<ValidationStep<SMSValidationResult>> getFullValidationSteps(CompanyDTO companyDTO,String psid){

        PaymentsAccount paymentsAccountToValidate = getPaymentsAccountFromCompany(companyDTO);

        AddressDTO addressDTO = companyDTO.getLegalAddress();
        PhysicalAddress companyAddress= accountServiceTranslator.createPhysicalAddress(addressDTO, AddressTypeEnum.LEGAL);
        companyAddress.setCountry("US");
        ContactDTO primaryPrincipalContact = accountServiceTranslator.getContactDTO(companyDTO, ContactRole.PrimaryPrincipal);
        RemoteAddressValidationStep remoteBusinessAddressValidationStep = new RemoteAddressValidationStep(companyAddress,AddressTypeEnum.COMPANY);

        // primary  principal address validation step
        PhysicalAddress primaryPrincipalPhysicalAddressToValidate = accountServiceTranslator.createPhysicalAddress(primaryPrincipalContact.getAddress(), AddressTypeEnum.RESIDENCE);
        primaryPrincipalPhysicalAddressToValidate.setCountry("US");
        RemoteAddressValidationStep remotePPOAddressValidationStep = new RemoteAddressValidationStep(primaryPrincipalPhysicalAddressToValidate,AddressTypeEnum.RESIDENCE);

        PaymentsAccountValidationStep paymentsAccountValidationStep = new PaymentsAccountValidationStep(paymentsAccountToValidate,psid);
        SSNValidationStep ssnValidationStep = new SSNValidationStep(paymentsAccountToValidate);
        TINValidationStep tinValidationStep = new TINValidationStep(companyDTO.getLegalName(), companyDTO.getFein());
        SyntacticAddressValidationStep syntacticBusinessAddressValidationStep = new SyntacticAddressValidationStep(companyDTO.getLegalAddress(),AddressTypeEnum.COMPANY);
        SyntacticAddressValidationStep syntacticPPOAddressValidationStep = new SyntacticAddressValidationStep(primaryPrincipalContact.getAddress(),AddressTypeEnum.RESIDENCE);


        List<ValidationStep<SMSValidationResult>> validationSteps = new ArrayList<>();
        validationSteps.add(syntacticBusinessAddressValidationStep);
        validationSteps.add(syntacticPPOAddressValidationStep);
        validationSteps.add(remoteBusinessAddressValidationStep);
        validationSteps.add(remotePPOAddressValidationStep);
        validationSteps.add(paymentsAccountValidationStep);
        validationSteps.add(ssnValidationStep);
        validationSteps.add(tinValidationStep);

        return   validationSteps;
    }

    private static PaymentsAccount getPaymentsAccountFromCompany(CompanyDTO companyDTO) {
        PaymentsAccount paymentsAccountToValidate = createPaymentsAccountToValidate(companyDTO);
        enrichPaymentAccountRequest(paymentsAccountToValidate);
        return paymentsAccountToValidate;
    }


    public static List<ValidationStep<SMSValidationResult>> getAddressValidationSteps(AddressDTO addressDTO,AddressTypeEnum addressType){

        PhysicalAddress companyAddress= accountServiceTranslator.createPhysicalAddress(addressDTO, AddressTypeEnum.LEGAL);

        SyntacticAddressValidationStep syntacticAddressValidationStep = new SyntacticAddressValidationStep(addressDTO,addressType);
        RemoteAddressValidationStep remoteAddressValidationStep = new RemoteAddressValidationStep(companyAddress,addressType);

        List<ValidationStep<SMSValidationResult>> validationSteps = new ArrayList<>();
        validationSteps.add(syntacticAddressValidationStep);
        validationSteps.add(remoteAddressValidationStep);

        return validationSteps;
    }

    public static List<ValidationStep<SMSValidationResult>> getSSNValidationSteps(CompanyDTO companyDTO){

       SSNValidationStep ssnValidationStep = new SSNValidationStep(getPaymentsAccountFromCompany(companyDTO));
          List<ValidationStep<SMSValidationResult>> ssnValidationSteps = new ArrayList<>(Collections.singletonList(ssnValidationStep));
          return  ssnValidationSteps;
    }


    public static List<ValidationStep<SMSValidationResult>> getTINValidationSteps(CompanyDTO companyDTO){

        TINValidationStep tinValidationStep = new TINValidationStep(companyDTO.getLegalName(),companyDTO.getFein());
        List<ValidationStep<SMSValidationResult>> tinValidationSteps = new ArrayList<>(Collections.singletonList(tinValidationStep));
        return  tinValidationSteps;
    }


    public static List<ValidationStep<SMSValidationResult>> getPaymentsAccountValidationStep(CompanyDTO companyDTO, Company domainCompany, String psid){


        PaymentsAccount paymentsAccountToValidate = getPaymentsAccountFromCompany(companyDTO);
        PaymentsAccountValidationStep paymentsAccountValidationStep = new PaymentsAccountValidationStep(paymentsAccountToValidate,psid);
        List<ValidationStep<SMSValidationResult>> paymentAccountStep = new ArrayList<>(Collections.singletonList(paymentsAccountValidationStep));
        return  paymentAccountStep;
    }



    private static PaymentsAccount createPaymentsAccountToValidate(CompanyDTO companyDTO) {
        PrimaryBusiness primaryBusiness = new PrimaryBusiness();
        OwnershipTypeEnum ownershipTypeEnum = OwnershipTypeEnum.SOLE_PROPRIETORSHIP;
        String industryType="Advertising Services";
        IndustryType industry = IndustryType.findIndustryType(industryType);

        //prepopulated with a fixed ownership type
        //TODO revisit the ownershiptype and use below
        if(Objects.nonNull(companyDTO.getCompanyAdditionalInfo()) && Objects.nonNull(companyDTO.getCompanyAdditionalInfo().getOwnership())){
             ownershipTypeEnum =  OwnershipTypeEnum.fromValue(companyDTO.getCompanyAdditionalInfo().getOwnership());
        }
        primaryBusiness.setOwnershipType(ownershipTypeEnum);
        logger.info("deaflut sic is "+industry.getStandardIndustryCode());
        if(Objects.nonNull(companyDTO.getCompanyAdditionalInfo()) && Objects.nonNull(companyDTO.getCompanyAdditionalInfo().getIndustry())){

            industry=IndustryType.findIndustryType(industryType);
        companyDTO.getCompanyAdditionalInfo().getIndustry();
        }

        primaryBusiness.setSic(industry.getStandardIndustryCode());
        primaryBusiness.setDescription(industryType);
        primaryBusiness.setEin(companyDTO.getFein());

        logger.info(" sic is "+primaryBusiness.getSic());


        ContactDTO primaryPrincipalContact = accountServiceTranslator.getContactDTO(companyDTO, ContactRole.PrimaryPrincipal);
        primaryBusiness.setPhone(Objects.nonNull(primaryPrincipalContact.getPhoneNumber())
                ?primaryPrincipalContact.getPhoneNumber().replaceAll("\\D+","")
                :null);

        PaymentsAccount paymentAccountToValidate = accountServiceTranslator.createPaymentsAccount(primaryBusiness, new BusinessOwner(), companyDTO);

        paymentAccountToValidate.setRealmId(companyDTO.getIAMRealmId());
        logger.info(" new sic is "+primaryBusiness.getSic());
        
        return paymentAccountToValidate;
    }


    private static void enrichPaymentAccountRequest(PaymentsAccount paymentAccountToValidate) {
        paymentAccountToValidate.setAccountPreferences(new AccountPreferences());
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setAcceptedTermsAndConditions(true);
        orderDetails.setApplicationSource(ApplicationSourceEnum.IPD);
        orderDetails.setApplicationChannel("None");
        paymentAccountToValidate.setOrderDetails(orderDetails);
    }

    public static List<ValidationStep<SMSValidationResult>> getPartialValidationSteps(CompanyDTO companyDTO,String psid) {

        PaymentsAccount paymentsAccountToValidate = getPaymentsAccountFromCompany(companyDTO);

        AddressDTO addressDTO = companyDTO.getComplianceAddress();
        PhysicalAddress complianceAddress = accountServiceTranslator.createPhysicalAddress(addressDTO, AddressTypeEnum.LEGAL);
        complianceAddress.setCountry("US");

        //Setting the address to compliance
        paymentsAccountToValidate.getBusinessInfo().setAddress(complianceAddress);
        logger.info("setting payments acct to validate with compliance address for realm=" + companyDTO.getIAMRealmId() + " and psid=" +psid);

        RemoteAddressValidationStep remoteComplianceAddressValidationStep = new RemoteAddressValidationStep(complianceAddress,AddressTypeEnum.LEGAL);

        // primary  principal address validation step
        ContactDTO primaryPrincipalContact = accountServiceTranslator.getContactDTO(companyDTO, ContactRole.PrimaryPrincipal);
        PhysicalAddress primaryPrincipalPhysicalAddressToValidate = accountServiceTranslator.createPhysicalAddress(primaryPrincipalContact.getAddress(), AddressTypeEnum.RESIDENCE);
        primaryPrincipalPhysicalAddressToValidate.setCountry("US");
        RemoteAddressValidationStep remotePPOAddressValidationStep = new RemoteAddressValidationStep(primaryPrincipalPhysicalAddressToValidate,AddressTypeEnum.RESIDENCE);

        PaymentsAccountValidationStep paymentsAccountValidationStep = new PaymentsAccountValidationStep(paymentsAccountToValidate,psid);
        SyntacticAddressValidationStep syntacticComplianceAddressValidationStep = new SyntacticAddressValidationStep(companyDTO.getComplianceAddress(),AddressTypeEnum.LEGAL);
        SyntacticAddressValidationStep syntacticPPOAddressValidationStep = new SyntacticAddressValidationStep(primaryPrincipalContact.getAddress(),AddressTypeEnum.RESIDENCE);

        List<ValidationStep<SMSValidationResult>> validationSteps = new ArrayList<>();
        validationSteps.add(syntacticComplianceAddressValidationStep);
        validationSteps.add(syntacticPPOAddressValidationStep);
        validationSteps.add(remoteComplianceAddressValidationStep);
        validationSteps.add(remotePPOAddressValidationStep);
        validationSteps.add(paymentsAccountValidationStep);

        return   validationSteps;
    }



}
