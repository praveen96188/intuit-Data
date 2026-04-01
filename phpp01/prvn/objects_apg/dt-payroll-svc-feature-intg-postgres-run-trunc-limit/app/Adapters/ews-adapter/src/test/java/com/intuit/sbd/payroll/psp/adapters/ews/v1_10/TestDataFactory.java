package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400.GetPayrollInfoWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400.GetPayrollStatusWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400.UserRoleEnum;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * @author Jeff Jones
 */

@Ignore 
public class TestDataFactory {

    public static EwsEinServiceEligibility createEwsEinServiceEligibility(String pEIN) {
        EwsEinServiceEligibility ewsEinServiceEligibility = new EwsEinServiceEligibility();

        ewsEinServiceEligibility.setEin(pEIN);
        ewsEinServiceEligibility.setEnableCurrentTaxYearValidation(true);
        ewsEinServiceEligibility.setDateTimeStamp(Calendar.getInstance());
        ewsEinServiceEligibility.setIpAddress("127.0.0.1");

        return ewsEinServiceEligibility;
    }

    public static EwsUpdatePin createEwsUpdatePin(String pPSID) {
        EwsUpdatePin ewsUpdatePin = new EwsUpdatePin();

        ewsUpdatePin.setPsid(pPSID);
        ewsUpdatePin.setPin("1234ABCDabcd");
        ewsUpdatePin.setOldPin("ABCDefgh1234");

        ewsUpdatePin.setDateTimeStamp(Calendar.getInstance());
        ewsUpdatePin.setIpAddress("127.0.0.1");

        return ewsUpdatePin;
    }

    public static EwsResetPin createEwsResetPin(String pPSID, String pPinSignature) {
        EwsResetPin ewsResetPin = new EwsResetPin();

        ewsResetPin.setPsid(pPSID);
        ewsResetPin.setPinSignature(pPinSignature);
        ewsResetPin.setDateTimeStamp(Calendar.getInstance());
        ewsResetPin.setUserName("Admin");
        ewsResetPin.setIpAddress("127.0.0.1");

        return ewsResetPin;
    }

    public static GetPayrollInfoWSDTO createGetPayrollInfoWSDTO(String pPSID, UserRoleEnum pUserRoleEnum) {
        GetPayrollInfoWSDTO getPayrollInfoWSDTO = new GetPayrollInfoWSDTO();

        getPayrollInfoWSDTO.setPayrollStatusWSDTO(new GetPayrollStatusWSDTO());
        getPayrollInfoWSDTO.getPayrollStatusWSDTO().setUserID(pPSID);
        getPayrollInfoWSDTO.getPayrollStatusWSDTO().setRoleId(pUserRoleEnum);

        return getPayrollInfoWSDTO;
    }

    public static EwsQuerySubscriptions createEwsQuerySubscriptions(Company pCompany) {
        EntitlementUnit entitlementUnit = pCompany.getEntitlementUnitCollection().get(0);

        EwsQuerySubscriptions ewsQuerySubscriptions = new EwsQuerySubscriptions();

        ewsQuerySubscriptions.setSubscriptionNumber(entitlementUnit.getEntitlement().getSubscriptionNumber());

        ewsQuerySubscriptions.setIpAddress("127.0.0.1");
        ewsQuerySubscriptions.setDateTimeStamp(Calendar.getInstance());

        return ewsQuerySubscriptions;
    }

    public static EwsQueryAccount createEwsQueryAccountPsid(Company pCompany) {
        EwsQueryAccount ewsQueryAccount = new EwsQueryAccount();

        ewsQueryAccount.setPsid(pCompany.getSourceCompanyId());

        ewsQueryAccount.setIpAddress("127.0.0.1");
        ewsQueryAccount.setDateTimeStamp(Calendar.getInstance());

        EwsBaseCompany ewsBaseCompany = new EwsBaseCompany();
        ewsQueryAccount.setEwsBaseCompany(ewsBaseCompany);

        ewsBaseCompany.setEin(pCompany.getFedTaxId());

        return ewsQueryAccount;
    }

    public static EwsQueryServiceKey createEwsQueryServiceKey(Company pCompany) {
        EwsQueryServiceKey ewsQueryServiceKey = new EwsQueryServiceKey();

        ewsQueryServiceKey.setEin(pCompany.getFedTaxId());
        ewsQueryServiceKey.setAuthId(pCompany.getContactCollection().get(0).getIAMAuthenticationId());

        ewsQueryServiceKey.setIpAddress("127.0.0.1");
        ewsQueryServiceKey.setDateTimeStamp(Calendar.getInstance());

        return ewsQueryServiceKey;
    }

    public static EwsQueryAccount createEwsQueryAccountEin(Company pCompany) {
        EwsQueryAccount ewsQueryAccount = new EwsQueryAccount();

        ewsQueryAccount.setIpAddress("127.0.0.1");
        ewsQueryAccount.setDateTimeStamp(Calendar.getInstance());

        EwsBaseCompany ewsBaseCompany = new EwsBaseCompany();
        ewsQueryAccount.setEwsBaseCompany(ewsBaseCompany);

        ewsBaseCompany.setEin(pCompany.getFedTaxId());

        EntitlementUnit eu = pCompany.getActivePrimaryEntitlementUnit();
        ewsQueryAccount.setSubscriptionNumber(eu.getEntitlement().getSubscriptionNumber());
        
        return ewsQueryAccount;
    }

    public static EwsQueryAccount createEwsQueryAccountEin(Company pCompany, EntitlementUnit pEntitlementUnit) {
        EwsQueryAccount ewsQueryAccount = new EwsQueryAccount();

        ewsQueryAccount.setIpAddress("127.0.0.1");
        ewsQueryAccount.setDateTimeStamp(Calendar.getInstance());

        EwsBaseCompany ewsBaseCompany = new EwsBaseCompany();
        ewsQueryAccount.setEwsBaseCompany(ewsBaseCompany);

        ewsBaseCompany.setEin(pCompany.getFedTaxId());

        ewsQueryAccount.setSubscriptionNumber(pEntitlementUnit.getEntitlement().getSubscriptionNumber());

        return ewsQueryAccount;
    }

    public static EwsEntitlement createAssistedEntitlement() {
        EwsEntitlement ewsEntitlement = new EwsEntitlement();

        ewsEntitlement.setBuyerEmailAddress("test4@intuit.com");
        ewsEntitlement.setBillingAccountId("1");
        ewsEntitlement.setLicenseNumber("2");
        ewsEntitlement.setEntitlementOfferingCode("1234567891");
        ewsEntitlement.setEdition(null);
        ewsEntitlement.setTier(null);
        ewsEntitlement.setAssetItemNumber(DataLoadServices.AssetItemNumber.ASSISTED.toString());
        ewsEntitlement.setAddEin(false);

        return ewsEntitlement;
    }

    public static EwsEntitlement createAssistedSymphonyEntitlement() {
        EwsEntitlement ewsEntitlement = new EwsEntitlement();

        ewsEntitlement.setBuyerEmailAddress("test4@intuit.com");
        ewsEntitlement.setBillingAccountId("1");
        ewsEntitlement.setLicenseNumber("2");
        ewsEntitlement.setEntitlementOfferingCode("1234567891");
        ewsEntitlement.setEdition(null);
        ewsEntitlement.setTier(null);
        ewsEntitlement.setAssetItemNumber(DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY.toString());
        ewsEntitlement.setAddEin(false);

        return ewsEntitlement;
    }

    public static EwsAddService createEwsAddServiceAssistedToCloud(String pPsid) {
        EwsAddService ewsAddService = new EwsAddService();

        ewsAddService.setPsid(pPsid);

        EwsBaseServices ewsBaseServices = new EwsBaseServices();
        ewsAddService.setEwsBaseServices(ewsBaseServices);

        EwsAssistedService ewsAssistedService = new EwsAssistedService();
        ewsAddService.getEwsBaseServices().setAssistedService(ewsAssistedService);

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsAssistedService.setEwsBankAccount(ewsBankAccount);
        ewsAssistedService.setMostCurrentTaxYear("2010");

        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);
        ewsBankAccount.setCreateRandomDebits(true);

        ewsAddService.setForceRandomDollar(true);

        ewsAddService.setIpAddress("127.0.0.1");
        ewsAddService.setDateTimeStamp(Calendar.getInstance());

        return ewsAddService;
    }

    public static EwsAddService createEwsAddServiceDDToCloud(String pPsid) {
        EwsAddService ewsAddService = new EwsAddService();

        ewsAddService.setPsid(pPsid);

        EwsBaseServices ewsBaseServices = new EwsBaseServices();
        ewsAddService.setEwsBaseServices(ewsBaseServices);

        EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
        ewsAddService.getEwsBaseServices().setDirectDepositService(ewsDirectDepositService);

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsDirectDepositService.setEwsBankAccount(ewsBankAccount);

        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);
        ewsBankAccount.setCreateRandomDebits(true);

        ewsAddService.setForceRandomDollar(true);
        ewsAddService.setIpAddress("127.0.0.1");
        ewsAddService.setDateTimeStamp(Calendar.getInstance());

        return ewsAddService;
    }

    public static EwsAddService createEwsAddServiceVMPToCloud(String pPsid){
        EwsAddService ewsAddService = new EwsAddService();
        ewsAddService.setPsid(pPsid);
        EwsBaseServices ewsBaseServices = new EwsBaseServices();
        ewsAddService.setEwsBaseServices(ewsBaseServices);
        ewsAddService.getEwsBaseServices().setViewMyPaycheck(new EwsBaseService());
        ewsAddService.setIpAddress("127.0.0.1");
        ewsAddService.setDateTimeStamp(Calendar.getInstance());
        return ewsAddService;
    }

    public static EwsAddService createEwsAddServiceDDVMPToCloud(String pPsid){
        EwsAddService ewsAddService = new EwsAddService();
        ewsAddService.setPsid(pPsid);
        EwsBaseServices ewsBaseServices = new EwsBaseServices();
        ewsAddService.setEwsBaseServices(ewsBaseServices);
        EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
        ewsAddService.getEwsBaseServices().setDirectDepositService(ewsDirectDepositService);
        ewsAddService.getEwsBaseServices().setViewMyPaycheck(new EwsBaseService());
        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsDirectDepositService.setEwsBankAccount(ewsBankAccount);
        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);
        ewsBankAccount.setCreateRandomDebits(true);
        ewsAddService.setForceRandomDollar(true);
        ewsAddService.setIpAddress("127.0.0.1");
        ewsAddService.setDateTimeStamp(Calendar.getInstance());
        return ewsAddService;
    }

    public static EwsAddService createEwsAddServiceDDToCloudWithFDPBankToken(String pPsid) {
        EwsAddService ewsAddService =createEwsAddServiceDDToCloud(pPsid);

        EwsBaseServices ewsBaseServices = ewsAddService.getEwsBaseServices();

        EwsDirectDepositService ewsDirectDepositService = ewsBaseServices.getDirectDepositService();

        EwsBankAccount ewsBankAccount = ewsDirectDepositService.getEwsBankAccount();

        ewsBankAccount.setAccountNumber("token:9707455231939953020");

        return ewsAddService;
    }

    public static EwsAddService createEwsAddServiceBillPaymentToDD(String pPsid) {
        EwsAddService ewsAddService = new EwsAddService();

        ewsAddService.setPsid(pPsid);

        EwsBaseServices ewsBaseServices = new EwsBaseServices();
        ewsAddService.setEwsBaseServices(ewsBaseServices);

        EwsBaseService ewsBaseService = new EwsBaseService();
        ewsAddService.getEwsBaseServices().setBillPayment(ewsBaseService);

        ewsAddService.setForceRandomDollar(true);
        ewsAddService.setIpAddress("127.0.0.1");
        ewsAddService.setDateTimeStamp(Calendar.getInstance());

        return ewsAddService;
    }

    public static EwsBasePin createEwsAuthenticatePin(String pPsid) {
        EwsBasePin ewsBasePin = new EwsBasePin();

        ewsBasePin.setPsid(pPsid);
        ewsBasePin.setPin("ABCDefgh1234");

        ewsBasePin.setIpAddress("127.0.0.1");
        ewsBasePin.setDateTimeStamp(Calendar.getInstance());

        return ewsBasePin;
    }

    public static EwsBasePin createEwsCreatePin(String pPsid) {
        EwsBasePin ewsBasePin = new EwsBasePin();

        ewsBasePin.setPsid(pPsid);
        ewsBasePin.setPin("ABCDefgh1234");

        ewsBasePin.setIpAddress("127.0.0.1");
        ewsBasePin.setDateTimeStamp(Calendar.getInstance());

        return ewsBasePin;
    }

    public static EwsUpdateBank createEwsUpdateBankAssisted(String pPsid) {
        EwsUpdateBank ewsUpdateBank = new EwsUpdateBank();

        ewsUpdateBank.setPsid(pPsid);

        EwsUpdateBankServices ewsUpdateBankServices = new EwsUpdateBankServices();
        ewsUpdateBank.setEwsUpdateBankServices(ewsUpdateBankServices);

        EwsUpdateBankAssistedService ewsUpdateBankAssistedService = new EwsUpdateBankAssistedService();
        ewsUpdateBankServices.setEwsUpdateBankAssistedService(ewsUpdateBankAssistedService);

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsUpdateBankAssistedService.setEwsBankAccount(ewsBankAccount);

        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setAccountType(EwsBankAccountType.Savings);
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setCreateRandomDebits(true);

        ewsUpdateBank.setIpAddress("127.0.0.1");
        ewsUpdateBank.setForceRandomDollar(true);

        return ewsUpdateBank;
    }

    public static EwsUpdateBank createEwsUpdateBankDD(String pPsid) {
        EwsUpdateBank ewsUpdateBank = new EwsUpdateBank();

        ewsUpdateBank.setPsid(pPsid);

        EwsUpdateBankServices ewsUpdateBankServices = new EwsUpdateBankServices();
        ewsUpdateBank.setEwsUpdateBankServices(ewsUpdateBankServices);

        EwsUpdateBankDirectDepositService ewsUpdateBankDirectDepositService = new EwsUpdateBankDirectDepositService();
        ewsUpdateBankServices.setEwsUpdateBankDirectDepositService(ewsUpdateBankDirectDepositService);

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsUpdateBankDirectDepositService.setEwsBankAccount(ewsBankAccount);

        ewsBankAccount.setAccountNumber(Integer.toString(new Random().nextInt(999999)));
        ewsBankAccount.setAccountType(EwsBankAccountType.Savings);
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setCreateRandomDebits(true);

        ewsUpdateBank.setIpAddress("127.0.0.1");
        ewsUpdateBank.setForceRandomDollar(true);

        return ewsUpdateBank;
    }

    public static EwsUpdateBank createEwsUpdateBankNameOnlyDD(String pPsid) {
        EwsUpdateBank ewsUpdateBank = new EwsUpdateBank();

        ewsUpdateBank.setPsid(pPsid);

        EwsUpdateBankServices ewsUpdateBankServices = new EwsUpdateBankServices();
        ewsUpdateBank.setEwsUpdateBankServices(ewsUpdateBankServices);

        EwsUpdateBankDirectDepositService ewsUpdateBankDirectDepositService = new EwsUpdateBankDirectDepositService();
        ewsUpdateBankServices.setEwsUpdateBankDirectDepositService(ewsUpdateBankDirectDepositService);

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsUpdateBankDirectDepositService.setEwsBankAccount(ewsBankAccount);

        ewsBankAccount.setCreateRandomDebits(false);
        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Whatever");
        ewsBankAccount.setQuickBooksName("Whatever");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);

        ewsUpdateBank.setIpAddress("127.0.0.1");
        ewsUpdateBank.setForceRandomDollar(true);

        return ewsUpdateBank;
    }

    public static EwsValidateBank createEwsValidateBankAssisted(String pPsid) {
        EwsValidateBank ewsValidateBank = new EwsValidateBank();

        ewsValidateBank.setPsid(pPsid);

        EwsValidateBankServices ewsValidateBankServices = new EwsValidateBankServices();
        ewsValidateBank.setEwsValidateBankServices(ewsValidateBankServices);

        EwsValidateBankAssistedService ewsValidateBankAssistedService = new EwsValidateBankAssistedService();
        ewsValidateBankServices.setEwsValidateBankAssistedService(ewsValidateBankAssistedService);

        EwsValidateBankAccount ewsValidateBankAccount = new EwsValidateBankAccount();
        ewsValidateBankAssistedService.setEwsValidateBankAccount(ewsValidateBankAccount);

        ewsValidateBankAccount.setRandomDebit1("0.12");
        ewsValidateBankAccount.setRandomDebit2("0.34");

        ewsValidateBank.setIpAddress("127.0.0.1");
        ewsValidateBank.setDateTimeStamp(Calendar.getInstance());

        return ewsValidateBank;
    }

    public static EwsValidateBank createEwsValidateBankDD(String pPsid) {
        EwsValidateBank ewsValidateBank = new EwsValidateBank();

        ewsValidateBank.setPsid(pPsid);

        EwsValidateBankServices ewsValidateBankServices = new EwsValidateBankServices();
        ewsValidateBank.setEwsValidateBankServices(ewsValidateBankServices);

        EwsValidateBankDirectDepositService ewsValidateBankDirectDepositService = new EwsValidateBankDirectDepositService();
        ewsValidateBankServices.setEwsValidateBankDirectDepositService(ewsValidateBankDirectDepositService);

        EwsValidateBankAccount ewsValidateBankAccount = new EwsValidateBankAccount();
        ewsValidateBankDirectDepositService.setEwsValidateBankAccount(ewsValidateBankAccount);

        ewsValidateBankAccount.setRandomDebit1(".12");
        ewsValidateBankAccount.setRandomDebit2(".34");

        ewsValidateBank.setIpAddress("127.0.0.1");
        ewsValidateBank.setDateTimeStamp(Calendar.getInstance());

        return ewsValidateBank;
    }

    public static EwsQueryAccount createEwsQueryAccount(String pPsid, String pEin) {
        EwsQueryAccount ewsQueryAccount = new EwsQueryAccount();

        ewsQueryAccount.setPsid(pPsid);
        ewsQueryAccount.setIpAddress("127.0.0.1");

        EwsBaseCompany ewsBaseCompany = new EwsBaseCompany();
        ewsQueryAccount.setEwsBaseCompany(ewsBaseCompany);
        ewsBaseCompany.setEin(pEin);

        return ewsQueryAccount;
    }

    public static EwsCreateAccount createEwsCreateAccountDirectDeposit() {
        EwsCreateAccount ewsCreateAccount = createEwsCreateAccount();

        EwsCompany ewsCompany = ewsCreateAccount.getEwsCompany();

        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
        ewsCompany.setLegalInfo(ewsLegalInfo);

        ewsLegalInfo.setLegalName("Acme Systems");
        ewsLegalInfo.setAddressLine1("123 Main St");
        ewsLegalInfo.setCity("Reno");
        ewsLegalInfo.setState("NV");
        ewsLegalInfo.setZip("89511");

        EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
        ewsCreateAccount.getEwsServices().setDirectDepositService(ewsDirectDepositService);

        ewsDirectDepositService.setOfferCode("Waive all major fees");

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsDirectDepositService.setEwsBankAccount(ewsBankAccount);

        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);

        return ewsCreateAccount;
    }

    public static EwsCreateAccount createEwsCreateAccountSymphonyDirectDeposit() {
        EwsCreateAccount ewsCreateAccount = createEwsCreateAccountSymphony();

        EwsCompany ewsCompany = ewsCreateAccount.getEwsCompany();

        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
        ewsCompany.setLegalInfo(ewsLegalInfo);

        ewsLegalInfo.setLegalName("Acme Systems");
        ewsLegalInfo.setAddressLine1("123 Main St");
        ewsLegalInfo.setCity("Reno");
        ewsLegalInfo.setState("NV");
        ewsLegalInfo.setZip("89511");

        EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
        ewsCreateAccount.getEwsServices().setDirectDepositService(ewsDirectDepositService);

        ewsDirectDepositService.setOfferCode("Waive all major fees");

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsDirectDepositService.setEwsBankAccount(ewsBankAccount);

        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);

        return ewsCreateAccount;
    }

    public static EwsCreateAccount createEwsCreateAccountAssisted() {
        EwsCreateAccount ewsCreateAccount = createEwsCreateAccount();

        EwsCompany ewsCompany = ewsCreateAccount.getEwsCompany();

        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
        ewsCompany.setLegalInfo(ewsLegalInfo);

        ewsLegalInfo.setLegalName("Acme Systems");
        ewsLegalInfo.setAddressLine1("123 Main St");
        ewsLegalInfo.setCity("Reno");
        ewsLegalInfo.setState("NV");
        ewsLegalInfo.setZip("89511");

        ewsCompany.setDba(null);
        ewsCompany.setMailingAddress(null);

        ewsCreateAccount.getEwsEntitlements().get(0).setEdition(null);
        ewsCreateAccount.getEwsEntitlements().get(0).setTier(null);
        ewsCreateAccount.getEwsEntitlements().get(0).setAssetItemNumber("1099750");

        EwsAssistedService ewsAssistedService = new EwsAssistedService();
        ewsCreateAccount.getEwsServices().setAssistedService(ewsAssistedService);

        ewsAssistedService.setPromotionId("1099426");

        EwsBankAccount ewsBankAccount = new EwsBankAccount();
        ewsAssistedService.setEwsBankAccount(ewsBankAccount);
        ewsAssistedService.setMostCurrentTaxYear("2010");

        ewsBankAccount.setAccountNumber("12345-12345");
        ewsBankAccount.setRoutingNumber("091050807");
        ewsBankAccount.setBankName("Bank of Intuit");
        ewsBankAccount.setQuickBooksName("BOFI");
        ewsBankAccount.setAccountType(EwsBankAccountType.Checking);
        ewsBankAccount.setCreateRandomDebits(true);

        return ewsCreateAccount;
    }

    public static EwsCreateAccount createEwsCreateAccountAssistedCloud() {
        EwsCreateAccount ewsCreateAccount = new EwsCreateAccount();

        EwsCompany ewsCompany = new EwsCompany();
        ewsCreateAccount.setEwsCompany(ewsCompany);

        String ein = "";
        Random random = new Random();
        while (ein.length() < 9) {
            ein = String.valueOf(random.nextInt(999999999));
        }
        ewsCompany.setEin(ein);
        ewsCompany.setRealmId("1234567890");

        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
        ewsCompany.setLegalInfo(ewsLegalInfo);

        ewsLegalInfo.setLegalName("Acme Software");
        ewsLegalInfo.setAddressLine1("123 Main St");
        ewsLegalInfo.setCity("Reno");
        ewsLegalInfo.setState("NV");
        ewsLegalInfo.setZip("89511");

        EwsContact payrollAdmin = new EwsContact();
        ewsCompany.setPayrollAdmin(payrollAdmin);

        payrollAdmin.setFirstName("First1");
        payrollAdmin.setLastName("Last1");
        payrollAdmin.seteMail("test1@intuit.com");
        payrollAdmin.setWorkPhone("999-999-9999");
        payrollAdmin.setAuthenticationId("12345");

        EwsContact primaryPrincipal = new EwsContact();
        ewsCompany.setPrimaryPrincipal(primaryPrincipal);

        primaryPrincipal.setFirstName("First2");
        primaryPrincipal.setLastName("Last2");
        primaryPrincipal.seteMail("test2@intuit.com");
        primaryPrincipal.setWorkPhone("888-888-8888");
        primaryPrincipal.setAuthenticationId("67890");
        primaryPrincipal.setSocialSecurityNumber("999999888");

        EwsQuickBooks quickBooks = new EwsQuickBooks();
        ewsCompany.setQuickBooks(quickBooks);

        quickBooks.setAppVersion("17.00.R.9/20716#professional");
        quickBooks.setLicenseNumber("6487-4844-4441-476");

        ewsCompany.setDba("Acme Software");

        EwsAddress ewsAddress = new EwsAddress();
        ewsCompany.setMailingAddress(ewsAddress);

        ewsAddress.setAddressLine1("123 Main St");
        ewsAddress.setCity("Reno");
        ewsAddress.setState("NV");
        ewsAddress.setZip("89511");

        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsCreateAccount.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = new EwsEntitlement();
        ewsEntitlements.add(ewsEntitlement);

        ewsEntitlement.setBuyerEmailAddress("test3@intuit.com");
        ewsEntitlement.setBillingAccountId("0");
        ewsEntitlement.setLicenseNumber("1");
        ewsEntitlement.setEntitlementOfferingCode("1234567890");
        ewsEntitlement.setEdition(null);
        ewsEntitlement.setTier(null);
        ewsEntitlement.setAssetItemNumber(DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY.toString());
        ewsEntitlement.setAddEin(false);

        EwsServices ewsServices = new EwsServices();
        ewsCreateAccount.setEwsServices(ewsServices);

        ewsServices.setCloudService(new EwsBaseService());

        ewsCreateAccount.setIpAddress("127.0.0.1");
        ewsCreateAccount.setForceRandomDollar(false);

        ewsCreateAccount.setDateTimeStamp(Calendar.getInstance());

        return ewsCreateAccount;
    }

    public static EwsCreateAccount createEwsCreateAccountSymphony() {
        EwsCreateAccount ewsCreateAccount = createEwsCreateAccount();

        ewsCreateAccount.getEwsEntitlements().get(0).setEdition(EwsEditionType.Basic);
        ewsCreateAccount.getEwsEntitlements().get(0).setTier(null);
        ewsCreateAccount.getEwsEntitlements().get(0).setAssetItemNumber("1100520");

        return ewsCreateAccount;
    }

    public static EwsUpdateAccount createEwsUpdateAccountCloudAndDD(String pPSID) {
        EwsUpdateAccount ewsUpdateAccount = createEwsUpdateAccountCloudOnly(pPSID);

        EwsCompany ewsCompany = ewsUpdateAccount.getEwsCompany();

        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
        ewsCompany.setLegalInfo(ewsLegalInfo);

        ewsLegalInfo.setLegalName("Acme Systems");
        ewsLegalInfo.setAddressLine1("123 Main St");
        ewsLegalInfo.setCity("Reno");
        ewsLegalInfo.setState("NV");
        ewsLegalInfo.setZip("89511");

        return ewsUpdateAccount;
    }

    public static EwsUpdateAccount createEwsUpdateAccountCloudAndAssisted(String pPSID) {
        EwsUpdateAccount ewsUpdateAccount = createEwsUpdateAccountCloudOnly(pPSID);

        EwsCompany ewsCompany = ewsUpdateAccount.getEwsCompany();

        EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
        ewsCompany.setLegalInfo(ewsLegalInfo);

        ewsLegalInfo.setLegalName("Acme Systems");
        ewsLegalInfo.setAddressLine1("123 Main St");
        ewsLegalInfo.setCity("Reno");
        ewsLegalInfo.setState("NV");
        ewsLegalInfo.setZip("89511");

        ewsCompany.setDba(null);
        ewsCompany.setMailingAddress(null);

        return ewsUpdateAccount;
    }

    public static EwsUpdateAccount createEwsUpdateAccountCloudOnly(String pPSID) {
        EwsUpdateAccount ewsUpdateAccount = new EwsUpdateAccount();

        EwsCompany ewsCompany = new EwsCompany();
        ewsUpdateAccount.setEwsCompany(ewsCompany);

        ewsUpdateAccount.setPsid(pPSID);

        String ein = "";
        Random random = new Random();
        while (ein.length() < 9) {
            ein = String.valueOf(random.nextInt(999999999));
        }
        ewsCompany.setEin(ein);
        ewsCompany.setRealmId("0987654321");

        EwsContact payrollAdmin = new EwsContact();
        ewsCompany.setPayrollAdmin(payrollAdmin);

        payrollAdmin.setFirstName("UpdatedFirst1");
        payrollAdmin.setLastName("UpdatedLast1");
        payrollAdmin.seteMail("updatedtest1@intuit.com");
        payrollAdmin.setWorkPhone("999-999-9998");
        payrollAdmin.setAuthenticationId("09876");

        EwsContact primaryPrincipal = new EwsContact();
        ewsCompany.setPrimaryPrincipal(primaryPrincipal);

        primaryPrincipal.setFirstName("UpdatedFirst2");
        primaryPrincipal.setLastName("UpdatedLast2");
        primaryPrincipal.seteMail("updatedtest2@intuit.com");
        primaryPrincipal.setWorkPhone("888-888-8887");
        primaryPrincipal.setAuthenticationId("54321");

        EwsQuickBooks quickBooks = new EwsQuickBooks();
        ewsCompany.setQuickBooks(quickBooks);

        quickBooks.setAppVersion("18.00.R.9/20716#professional");
        quickBooks.setLicenseNumber("6487-4844-4441-477");

        ewsCompany.setDba("Acme Software 2");

        EwsAddress ewsAddress = new EwsAddress();
        ewsCompany.setMailingAddress(ewsAddress);

        ewsAddress.setAddressLine1("1234 Main St");
        ewsAddress.setCity("Carson");
        ewsAddress.setState("CA");
        ewsAddress.setZip("89512");

        ewsUpdateAccount.setIpAddress("127.0.0.1");

        return ewsUpdateAccount;
    }
    public static EwsUpdateAccount createAlphaBetaEwsCreateAccount(String pPSID, String pAppVersion, String License){

        EwsUpdateAccount ewsUpdateAccount = new EwsUpdateAccount();

        EwsCompany ewsCompany = new EwsCompany();
        ewsUpdateAccount.setEwsCompany(ewsCompany);

        ewsUpdateAccount.setPsid(pPSID);

        String ein = "";
        Random random = new Random();
        while (ein.length() < 9) {
            ein = String.valueOf(random.nextInt(999999999));
        }
        ewsCompany.setEin(ein);
        ewsCompany.setRealmId("0987654321");

        EwsContact payrollAdmin = new EwsContact();
        ewsCompany.setPayrollAdmin(payrollAdmin);

        payrollAdmin.setFirstName("UpdatedFirst1");
        payrollAdmin.setLastName("UpdatedLast1");
        payrollAdmin.seteMail("updatedtest1@intuit.com");
        payrollAdmin.setWorkPhone("999-999-9998");
        payrollAdmin.setAuthenticationId("09876");

        EwsContact primaryPrincipal = new EwsContact();
        ewsCompany.setPrimaryPrincipal(primaryPrincipal);

        primaryPrincipal.setFirstName("UpdatedFirst2");
        primaryPrincipal.setLastName("UpdatedLast2");
        primaryPrincipal.seteMail("updatedtest2@intuit.com");
        primaryPrincipal.setWorkPhone("888-888-8887");
        primaryPrincipal.setAuthenticationId("54321");

        EwsQuickBooks quickBooks = new EwsQuickBooks();
        ewsCompany.setQuickBooks(quickBooks);

        quickBooks.setAppVersion(pAppVersion);
        quickBooks.setLicenseNumber(License);

        ewsCompany.setDba("Acme Software 2");

        EwsAddress ewsAddress = new EwsAddress();
        ewsCompany.setMailingAddress(ewsAddress);

        ewsAddress.setAddressLine1("1234 Main St");
        ewsAddress.setCity("Carson");
        ewsAddress.setState("CA");
        ewsAddress.setZip("89512");

        ewsUpdateAccount.setIpAddress("127.0.0.1");

        return ewsUpdateAccount;

    }



    public static EwsCreateAccount createEwsCreateAccount() {
        EwsCreateAccount ewsCreateAccount = new EwsCreateAccount();

        EwsCompany ewsCompany = new EwsCompany();
        ewsCreateAccount.setEwsCompany(ewsCompany);

        String ein = "";
        Random random = new Random();
        while (ein.length() < 9) {
            ein = String.valueOf(random.nextInt(999999999));
        }
        ewsCompany.setEin(ein);
        ewsCompany.setRealmId("9130360425658956");
        ewsCompany.setClientPacketDeliveryPreference(EwsDeliveryType.mail);
        ewsCompany.setW2DeliveryPreference(EwsDeliveryType.mail);

        EwsContact payrollAdmin = new EwsContact();
        ewsCompany.setPayrollAdmin(payrollAdmin);

        payrollAdmin.setFirstName("First1");
        payrollAdmin.setLastName("Last1");
        payrollAdmin.seteMail("test1@intuit.com");
        payrollAdmin.setWorkPhone("999-999-9999");
        payrollAdmin.setAuthenticationId("12345");

        EwsContact primaryPrincipal = new EwsContact();
        ewsCompany.setPrimaryPrincipal(primaryPrincipal);

        primaryPrincipal.setFirstName("First2");
        primaryPrincipal.setLastName("Last2");
        primaryPrincipal.seteMail("test2@intuit.com");
        primaryPrincipal.setWorkPhone("888-888-8888");
        primaryPrincipal.setAuthenticationId("67890");
        primaryPrincipal.setSocialSecurityNumber("999999888");

        EwsQuickBooks quickBooks = new EwsQuickBooks();
        ewsCompany.setQuickBooks(quickBooks);

        quickBooks.setAppVersion("17.00.R.9/20716#professional");
        quickBooks.setLicenseNumber("6487-4844-4441-476");

        ewsCompany.setDba("Acme Software");

        EwsAddress ewsAddress = new EwsAddress();
        ewsCompany.setMailingAddress(ewsAddress);

        ewsAddress.setAddressLine1("123 Main St");
        ewsAddress.setCity("Reno");
        ewsAddress.setState("NV");
        ewsAddress.setZip("89511");

        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsCreateAccount.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = new EwsEntitlement();
        ewsEntitlements.add(ewsEntitlement);

        ewsEntitlement.setBuyerEmailAddress("test3@intuit.com");
        ewsEntitlement.setBillingAccountId("0");
        ewsEntitlement.setLicenseNumber("1");
        ewsEntitlement.setEntitlementOfferingCode("1234567890");
        ewsEntitlement.setEdition(EwsEditionType.Basic);
        ewsEntitlement.setTier(EwsTierType.One);
        ewsEntitlement.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        ewsEntitlement.setAddEin(false);

        EwsBillingDetails ewsBillingDetails = new EwsBillingDetails();
        ewsEntitlement.setEwsBillingDetails(ewsBillingDetails);

        ewsBillingDetails.setSubscriptionNextBillDate(Calendar.getInstance());
        ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.CC);
        ewsBillingDetails.setCreditCardNumber("1111");
        ewsBillingDetails.setCreditCardType(EwsCreditCardType.VISA);
        ewsBillingDetails.setCreditCardExp("11/2014");
        
        EwsServices ewsServices = new EwsServices();
        ewsCreateAccount.setEwsServices(ewsServices);

        ewsServices.setCloudService(new EwsBaseService());

        ewsCreateAccount.setIpAddress("127.0.0.1");
        ewsCreateAccount.setForceRandomDollar(true);

        ewsCreateAccount.setDateTimeStamp(Calendar.getInstance());

        return ewsCreateAccount;
    }

    public static EwsMigrateAccount createEwsMigrateAccount(EwsCompany pEwsCompany, String pPSID) {
        EwsMigrateAccount ewsMigrateAccount = new EwsMigrateAccount();
        ewsMigrateAccount.setPsid(pPSID);
        ewsMigrateAccount.setEwsCompany(pEwsCompany);


        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsMigrateAccount.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = createAssistedEntitlement();
        ewsEntitlements.add(ewsEntitlement);


        EwsServices ewsServices = new EwsServices();
        ewsMigrateAccount.setEwsServices(ewsServices);

        EwsAddService assistedService = createEwsAddServiceAssistedToCloud(pPSID);
        ewsServices.setAssistedService(assistedService.getEwsBaseServices().getAssistedService());
        ewsMigrateAccount.setIpAddress("127.0.0.1");
        ewsMigrateAccount.setForceRandomDollar(true);

        ewsMigrateAccount.setDateTimeStamp(Calendar.getInstance());

        return ewsMigrateAccount;
    }

    public static EwsMigrateEntitlement createEwsMigrateEntitlement(EwsCompany pEwsCompany, String pPSID) {
        EwsMigrateEntitlement ewsMigrateEntitlement = new EwsMigrateEntitlement();
        ewsMigrateEntitlement.setPsid(pPSID);

        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsMigrateEntitlement.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = createAssistedEntitlement();
        ewsEntitlements.add(ewsEntitlement);

        ewsMigrateEntitlement.setIpAddress("127.0.0.1");
        ewsMigrateEntitlement.setDateTimeStamp(Calendar.getInstance());

        return ewsMigrateEntitlement;
    }

    public static EwsMigrateAccount createEwsMigrateAccountDIY(EwsCompany pEwsCompany, String pPSID) {
        EwsMigrateAccount ewsMigrateAccount = new EwsMigrateAccount();
        ewsMigrateAccount.setPsid(pPSID);
        ewsMigrateAccount.setEwsCompany(pEwsCompany);


        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsMigrateAccount.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = new EwsEntitlement();
        ewsEntitlement.setBuyerEmailAddress("test3@intuit.com");
        ewsEntitlement.setBillingAccountId("0");
        ewsEntitlement.setLicenseNumber("1");
        ewsEntitlement.setEntitlementOfferingCode("1234567890");
        ewsEntitlement.setEdition(EwsEditionType.Enhanced);
        ewsEntitlement.setTier(EwsTierType.Unlimited);
        ewsEntitlement.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        ewsEntitlement.setAddEin(false);

        EwsBillingDetails ewsBillingDetails = new EwsBillingDetails();
        ewsBillingDetails.setSubscriptionNextBillDate(Calendar.getInstance());
        ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.CC);
        ewsBillingDetails.setCreditCardNumber("1111");
        ewsBillingDetails.setCreditCardType(EwsCreditCardType.VISA);
        ewsBillingDetails.setCreditCardExp("11/2014");
        ewsEntitlement.setEwsBillingDetails(ewsBillingDetails);

        ewsEntitlements.add(ewsEntitlement);


        EwsServices ewsServices = new EwsServices();
        ewsMigrateAccount.setEwsServices(ewsServices);

        EwsAddService diyService = createEwsAddServiceDDToCloud(pPSID);
        ewsServices.setDirectDepositService(diyService.getEwsBaseServices().getDirectDepositService());
        ewsMigrateAccount.setIpAddress("127.0.0.1");
        ewsMigrateAccount.setForceRandomDollar(true);

        ewsMigrateAccount.setDateTimeStamp(Calendar.getInstance());

        return ewsMigrateAccount;
    }

    public static EwsMigrateEntitlement createEwsMigrateEntitlementDIY(EwsCompany pEwsCompany, String pPSID) {
        EwsMigrateEntitlement ewsMigrateEntitlement = new EwsMigrateEntitlement();
        ewsMigrateEntitlement.setPsid(pPSID);

        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsMigrateEntitlement.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = new EwsEntitlement();
        ewsEntitlement.setBuyerEmailAddress("test3@intuit.com");
        ewsEntitlement.setBillingAccountId("0");
        ewsEntitlement.setLicenseNumber("1");
        ewsEntitlement.setEntitlementOfferingCode("1234567890");
        ewsEntitlement.setEdition(EwsEditionType.Enhanced);
        ewsEntitlement.setTier(EwsTierType.Unlimited);
        ewsEntitlement.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        ewsEntitlement.setAddEin(false);

        EwsBillingDetails ewsBillingDetails = new EwsBillingDetails();
        ewsBillingDetails.setSubscriptionNextBillDate(Calendar.getInstance());
        ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.CC);
        ewsBillingDetails.setCreditCardNumber("1111");
        ewsBillingDetails.setCreditCardType(EwsCreditCardType.VISA);
        ewsBillingDetails.setCreditCardExp("11/2014");
        ewsEntitlement.setEwsBillingDetails(ewsBillingDetails);

        ewsEntitlements.add(ewsEntitlement);

        ewsMigrateEntitlement.setIpAddress("127.0.0.1");
        ewsMigrateEntitlement.setDateTimeStamp(Calendar.getInstance());

        return ewsMigrateEntitlement;
    }

    public static EwsValidateSubscription createEwsValidateAccount(Company company, String licenseNumber, String entitlementOfferingCode) {
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        return createEwsValidateAccount(company, entitlement);
    }

    public static EwsDeactivateService createEwsDeactivateVMPService(Company company) {
        EwsDeactivateService deactivateService = new EwsDeactivateService();
        deactivateService.setPsid(company.getSourceCompanyId());

        EwsBaseServices ewsBaseServices = new EwsBaseServices();
        deactivateService.setEwsBaseServices(ewsBaseServices);
        deactivateService.getEwsBaseServices().setViewMyPaycheck(new EwsBaseService());

        return deactivateService;
    }

    public static EwsValidateSubscription createEwsValidateAccount(Company company, Entitlement entitlement) {
        EwsValidateSubscription validateRequest = new EwsValidateSubscription();
        validateRequest.setEin(company.getFedTaxId());
        validateRequest.setPsid(company.getSourceCompanyId());
        validateRequest.setSubscriptionNumber(entitlement.getSubscriptionNumber());

        EwsQuickBooks ewsQuickBooks = new EwsQuickBooks();
        ewsQuickBooks.setAppVersion("17.00.R.9/20716#professional");
        ewsQuickBooks.setLicenseNumber("6487-4844-4441-477");
        validateRequest.setQuickBooks(ewsQuickBooks);

        validateRequest.setDateTimeStamp(CalendarUtils.convertToCalendar(PSPDate.getPSPTime()));
        validateRequest.setIpAddress("127.0.0.1");

        return validateRequest;
    }

    public static EwsUpdateBillingDetails createEwsUpdateBillingDetails(String pPsid) {
        EwsUpdateBillingDetails request = new EwsUpdateBillingDetails();

        request.setPsid(pPsid);
        request.setIpAddress("127.0.0.1");
       
        request.setLicenseNumber("1");
        request.setEntitlementOfferingCode("1234567890");

        EwsBillingDetails ewsBillingDetails = new EwsBillingDetails();
        request.setEwsBillingDetails(ewsBillingDetails);

        ewsBillingDetails.setSubscriptionNextBillDate(Calendar.getInstance());
        ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.CC);
        ewsBillingDetails.setCreditCardNumber("1111");
        ewsBillingDetails.setCreditCardType(EwsCreditCardType.VISA);
        ewsBillingDetails.setCreditCardExp("11/2014");

        return request;
    }

    public static EwsDeactivateEntitlement createEwsDeactivateEntitlementRequest(ArrayList<String> einList, String subscriptionNumber){

        EwsDeactivateEntitlement ewsDeactivateEntitlement = new EwsDeactivateEntitlement();
        ewsDeactivateEntitlement.setSubscriptionNumber(subscriptionNumber);
        ewsDeactivateEntitlement.setEins(einList);
        ewsDeactivateEntitlement.setIpAddress("127.0.0.1");
        ewsDeactivateEntitlement.setDateTimeStamp(Calendar.getInstance());
        return ewsDeactivateEntitlement;
    }


    public static EwsAddService createEwsAddServiceDDToCloudWithWallet(String pPsid) {
        EwsAddService ewsAddService =createEwsAddServiceDDToCloud(pPsid);

        EwsBaseServices ewsBaseServices = ewsAddService.getEwsBaseServices();

        EwsDirectDepositService ewsDirectDepositService = ewsBaseServices.getDirectDepositService();

        EwsBankAccount ewsBankAccount = ewsDirectDepositService.getEwsBankAccount();

        ewsBankAccount.setAccountNumber("walletId:200147507162164470667600");

        return ewsAddService;
    }
}
