package com.intuit.sbd.payroll.psp.processes.accountservice.validation;


import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;

import com.intuit.sbd.payroll.psp.domain.BaseSMSMigration;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.ValidationType;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory.ValidationStepsFactory;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import com.intuit.sbg.psp.validationservices.types.v1.ErrorDetail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.*;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
public class AccountServiceValidationCoreTests {
    @Before
    public void beforeEachTest() {
        Application.truncateTables();
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        SalesTaxGatewayFactory.setInstanceClass(FakeSalesTaxGateway.class);
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testAccountValidationWithMultipleValidationSteps(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        System.out.println("The industry  type code here is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        companyDTO.getLegalAddress().setAddressLine1("P O box  244");
        System.out.println("the fein is "+companyDTO.getFein());

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("875649567");



        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(companyDTO,company,true, ValidationType.FULL_VALIDATION);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();

        assertEquals(processResult.getResult().getErrors().size(),2);
        Set<String> errorSet = new HashSet<>();
        errorSet.add(processResult.getResult().getErrors().get(0).getEntity());
        errorSet.add(processResult.getResult().getErrors().get(1).getEntity());
        assertTrue(errorSet.contains("businessInfo.addressLine1") && errorSet.contains("businessInfo.address.streetAddress"));

        assertEquals(processResult.getResult().getWarnings().size(),3);
        List<ErrorDetail> warnings = processResult.getResult().getWarnings();
        assertTrue(warnings.get(0).getEntity().equals("businessOwner[0].address"));
        //assertTrue(warnings.get(1).getEntity().equals("Error:"));
        assertTrue(warnings.get(2).getEntity().equals("messageNoIssueId"));
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testOnlyAddressValidationForBusiness(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        System.out.println("The industry  type code here is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getLegalAddress().setAddressLine1("P O box  244");
        System.out.println("the fein is "+companyDTO.getFein());

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("875649567");

        AddressDTO addressDTO = companyDTO.getLegalAddress();

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(addressDTO,AddressTypeEnum.COMPANY,company,false);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();
        assertEquals(processResult.getResult().getErrors().size(),1);
        ErrorDetail ed= processResult.getResult().getErrors().get(0);
        assertTrue(ed.getEntity().equals("businessInfo.addressLine1"));


        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testOnlyAddressValidationForPrimaryPrincipal(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        System.out.println("The industry  type code here is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getLegalAddress().setAddressLine1("P O box  244");
        System.out.println("the fein is "+companyDTO.getFein());

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("875649567");

        AddressDTO addressDTO = companyDTO.getLegalAddress();

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(addressDTO,AddressTypeEnum.RESIDENCE,company,false);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();
        assertEquals(processResult.getResult().getErrors().size(),1);
        ErrorDetail ed= processResult.getResult().getErrors().get(0);
        assertEquals(ed.getEntity(),"businessOwner[0].addressLine1");


        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testAddressValidationForCompliance(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        System.out.println("The industry  type code here is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getLegalAddress().setAddressLine1("P O box  244");


        AddressDTO addressDTO = companyDTO.getLegalAddress();

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(addressDTO,AddressTypeEnum.LEGAL,company,false);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();
        assertEquals(processResult.getResult().getErrors().size(),1);
        ErrorDetail ed= processResult.getResult().getErrors().get(0);
        assertEquals(ed.getEntity(),"complianceAddress.addressLine1");

        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testAddressValidationForComplianceShouldBePersisted(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        System.out.println("The industry  type code here is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getLegalAddress().setAddressLine1("P O box  244");


        AddressDTO addressDTO = companyDTO.getLegalAddress();

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(addressDTO,AddressTypeEnum.LEGAL,company,true);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();
        assertEquals(processResult.getResult().getErrors().size(),1);
        ErrorDetail ed= processResult.getResult().getErrors().get(0);
        assertEquals(ed.getEntity(),"complianceAddress.addressLine1");

        PayrollServices.commitUnitOfWork();


        DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationBySourceCompanyId(psid);
        assertEquals(smsMigrations.size(),1);

        BaseSMSMigration smsMigration = smsMigrations.get(0);
        com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus smsMigrationStatus = smsMigration.getMigrationStatus();
        assertEquals(smsMigrationStatus,SMSMigrationStatus.ValidationError);
    }


    @Test
    public void testValidationResultShouldBePersisted(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        companyDTO.getLegalAddress().setAddressLine1("po box355");

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("875649567");

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(companyDTO,company,true,ValidationType.FULL_VALIDATION);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();

        assertEquals(processResult.getResult().getErrors().size(),2);
        Set<String> errorSet = new HashSet<>();
        errorSet.add(processResult.getResult().getErrors().get(0).getEntity());
        errorSet.add(processResult.getResult().getErrors().get(1).getEntity());
        assertTrue(errorSet.contains("businessInfo.addressLine1") && errorSet.contains("businessInfo.address.streetAddress"));

        assertEquals(processResult.getResult().getWarnings().size(),3);
        List<ErrorDetail> warnings = processResult.getResult().getWarnings();
        assertTrue(warnings.get(0).getEntity().equals("businessOwner[0].address"));
        //assertTrue(warnings.get(1).getEntity().equals("Error:"));
        //assertTrue(warnings.get(2).getEntity().equals("messageNoIssueId"));
        PayrollServices.commitUnitOfWork();


        DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationBySourceCompanyId(psid);
        assertEquals(smsMigrations.size(),1);

        BaseSMSMigration smsMigration = smsMigrations.get(0);
        com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus smsMigrationStatus = smsMigration.getMigrationStatus();
        assertEquals(smsMigrationStatus,SMSMigrationStatus.ValidationError);

    }

    @Test
    public void testValidationResultShouldBeUpdated(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        companyDTO.getLegalAddress().setAddressLine1("po box355");

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("875649567");

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(companyDTO,company,true,ValidationType.FULL_VALIDATION);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();

        assertEquals(processResult.getResult().getErrors().size(),2);
        Set<String> errorSet = new HashSet<>();
        errorSet.add(processResult.getResult().getErrors().get(0).getEntity());
        errorSet.add(processResult.getResult().getErrors().get(1).getEntity());
        assertTrue(errorSet.contains("businessInfo.addressLine1") && errorSet.contains("businessInfo.address.streetAddress"));

        assertEquals(processResult.getResult().getWarnings().size(),3);
        List<ErrorDetail> warnings = processResult.getResult().getWarnings();
        assertTrue(warnings.get(0).getEntity().equals("businessOwner[0].address"));
        //assertTrue(warnings.get(1).getEntity().equals("Error:"));
        //assertTrue(warnings.get(2).getEntity().equals("messageNoIssueId"));
        PayrollServices.commitUnitOfWork();



        DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationByCompany(company);
        assertEquals(smsMigrations.size(),1);

        BaseSMSMigration smsMigration = smsMigrations.get(0);
        com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus smsMigrationStatus = smsMigration.getMigrationStatus();
        assertEquals(smsMigrationStatus,SMSMigrationStatus.ValidationError);

        //revalidate the same company but with valid address this time
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        companyDTO.getLegalAddress().setAddressLine1("1500 broadway");
        AccountServiceValidationCore accountServiceValidationCore2 = new AccountServiceValidationCore(companyDTO,company,true,ValidationType.FULL_VALIDATION);
        ProcessResult<SMSValidationResult> processResult2 = accountServiceValidationCore2.execute();
        PayrollServices.commitUnitOfWork();

        smsMigrations = SMSMigration.getSmsMigrationByCompany(company);
        assertEquals(smsMigrations.size(),1);

        smsMigration = smsMigrations.get(0);
        smsMigrationStatus = smsMigration.getMigrationStatus();
        assertEquals(smsMigrationStatus,SMSMigrationStatus.ValidationSuccess);

    }


    @Test
    public void testPartialValidation(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        companyDTO.getLegalAddress().setAddressLine1("po box355");
        companyDTO.setComplianceAddress(createAddressDTO());
        companyDTO.getComplianceAddress().setAddressLine1("po box 3435");

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("875649567");

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(companyDTO,company,true,ValidationType.PARTIAL_VALIDATION);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();

        assertEquals(processResult.getResult().getErrors().size(),2);
        Set<String> errorSet = new HashSet<>();
        errorSet.add(processResult.getResult().getErrors().get(0).getEntity());
        errorSet.add(processResult.getResult().getErrors().get(1).getEntity());
        assertTrue(errorSet.contains("complianceAddress.addressLine1") && errorSet.contains("businessInfo.address.streetAddress"));

        assertEquals(processResult.getResult().getWarnings().size(),1);
        List<ErrorDetail> warnings = processResult.getResult().getWarnings();
        assertTrue(warnings.get(0).getEntity().equals("businessOwner[0].address"));
        PayrollServices.commitUnitOfWork();


        DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationBySourceCompanyId(psid);
        assertEquals(smsMigrations.size(),1);

        BaseSMSMigration smsMigration = smsMigrations.get(0);
        com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus smsMigrationStatus = smsMigration.getMigrationStatus();
        assertEquals(smsMigrationStatus,SMSMigrationStatus.ValidationError);

    }


    @Test
    public void testSingleValidationStepExecution(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        companyDTO.getLegalAddress().setAddressLine1("po box355");
        companyDTO.setComplianceAddress(createAddressDTO());
        companyDTO.getComplianceAddress().setAddressLine1("po box 3435");

        Optional<ContactDTO> optionalContactDTO = companyDTO.getContacts()
                .stream()
                .filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        DateDTO dateDTO = new DateDTO();
        dateDTO.set(1988,02,02);
        optionalContactDTO.get().setDateOfBirth(dateDTO);
        optionalContactDTO.get().setSocialSecurityNumber("");

        List<ValidationStep<SMSValidationResult>> paymentsAccountOnly = ValidationStepsFactory.getPaymentsAccountValidationStep(companyDTO,company,psid);

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(companyDTO,company,true,paymentsAccountOnly);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();

        assertEquals(processResult.getResult().getErrors().size(),2);
        Map<String,ErrorDetail> errorDetailMap = new HashMap<>();
        errorDetailMap.put(processResult.getResult().getErrors().get(0).getEntity(),
                processResult.getResult().getErrors().get(0));
        errorDetailMap.put(processResult.getResult().getErrors().get(1).getEntity(),
                processResult.getResult().getErrors().get(1));
        assertTrue(errorDetailMap.containsKey("businessOwners[0].ssn") && errorDetailMap.containsKey("businessInfo.address.streetAddress"));

        assertTrue(errorDetailMap.get("businessOwners[0].ssn").getMessage().equals("ssn is required"));

        assertEquals(processResult.getResult().getWarnings().size(),0);

        PayrollServices.commitUnitOfWork();


        DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationBySourceCompanyId(psid);
        assertEquals(smsMigrations.size(),1);

        BaseSMSMigration smsMigration = smsMigrations.get(0);
        com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus smsMigrationStatus = smsMigration.getMigrationStatus();
        assertEquals(smsMigrationStatus,SMSMigrationStatus.ValidationError);

    }


    @Test
    public void testAddressValidationForComplianceShouldBeRejectedDuetoPOBox(){

        String psid = TestCompanyCreatorUtil.createCompanyWithAdditionalInfo();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        System.out.println("The industry  type code here is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getLegalAddress().setAddressLine1("P O box  244");
        companyDTO.getLegalAddress().setState("qwerty");


        AddressDTO addressDTO = companyDTO.getLegalAddress();

        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(addressDTO,AddressTypeEnum.LEGAL,company,false);
        ProcessResult<SMSValidationResult> processResult = accountServiceValidationCore.execute();
        assertTrue(validationWasRejectedDueToPOBox(processResult.getResult()));
        assertEquals(processResult.getResult().getErrors().size(),3);
        boolean entityHasComplianceAddress = processResult.getResult().getErrors().stream().anyMatch(x->x.getEntity().equals("complianceAddress.addressLine1"));
        ErrorDetail ed= processResult.getResult().getErrors().get(0);
        assertTrue(entityHasComplianceAddress);

        PayrollServices.commitUnitOfWork();
    }


    private boolean validationWasRejectedDueToPOBox(SMSValidationResult smsValidationResult) {
        return  smsValidationResult.getErrors().stream().anyMatch(x->x.getMessage().contains("Can not be a PO box address"));

    }


    public AddressDTO createAddressDTO() {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("1500 broadway");
        addressDTO.setAddressLine2("");
        addressDTO.setAddressLine3("");
        addressDTO.setCity("SFO");
        addressDTO.setCountry("US");
        addressDTO.setState("CA");
        addressDTO.setZipCode("90404");
        addressDTO.setZipCodeExtension("8345");
        return addressDTO;
    }
}
