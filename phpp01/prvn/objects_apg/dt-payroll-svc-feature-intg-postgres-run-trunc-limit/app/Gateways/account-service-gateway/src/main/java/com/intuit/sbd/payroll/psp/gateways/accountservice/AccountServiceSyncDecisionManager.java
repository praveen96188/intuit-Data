package com.intuit.sbd.payroll.psp.gateways.accountservice;

import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.enums.AccountStatusEnum;
import com.intuit.payments.cdm.v2.client.enums.BankAccountTypeEnum;
import com.intuit.payments.cdm.v2.client.enums.VbdStatusEnum;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPStringUtils;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

import java.util.*;

public class AccountServiceSyncDecisionManager {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(AccountServiceSyncDecisionManager.class);

    private AccountServiceTranslator accountServiceTranslator;
    public static final List<ServiceSubStatusCode> REMOVABLE_HOLDS =Arrays.asList(ServiceSubStatusCode.AMLHold,ServiceSubStatusCode.PendingTermination,ServiceSubStatusCode.SuspendedDirectDeposit);

    private static final String ONLY_DIGITS = "[^0-9]";
    private static final String US = "US";
    private static final String USA = "USA";


    public AccountServiceSyncDecisionManager(AccountServiceTranslator accountServiceTranslator) {
        this.accountServiceTranslator= accountServiceTranslator;
    }

    public boolean isPaymentsAccountValid(PaymentsAccount paymentsAccount){
        if(Objects.isNull(accountServiceTranslator.getBusinessInfo(paymentsAccount))){
            logger.error("Received Empty Business info from Account service");
            return false;
        }

        if(Objects.isNull(accountServiceTranslator.getPaymentsPrimaryPrincipal(paymentsAccount))){
            logger.error("Received Empty Primary owner from Account service");
            return false;
        }

        if(Objects.isNull(accountServiceTranslator.getBusinessAddress(paymentsAccount))){
            logger.error("Received Empty Business address from Account service");
            return false;
        }

        return true;
    }


    /**
     * Returns the VBD status for a Company Bank account number from PSP.
     *
     * VBD Status event has three details in it:
     * CompanyBankAccountId, OldBAStatus and NewBAStatus.
     *
     * @param paymentsBankAccount
     * @param company
     * @return the VBD Status of a Company Bank Account Number from PSP
     */
    public String getPSPVBDStatus(BankAccount paymentsBankAccount, Company company, String realmId) {
        // Fetching all the CompanyBankAccountVBDStatusChange events for a PSP company in descending order of creation
        DomainEntitySet<CompanyEvent> vbdEventsList = CompanyEvent.findCompanyEventsEagerLoadCompanyEventDetail(company, EventTypeCode.CompanyBankAccountVBDStatusChange, CompanyEventStatus.Active, true);

        String pspVBDStatus = null;
        if(vbdEventsList.size() == 0) {
            logger.info("Action=getPSPVBDStatus, msg=no_existing_CompanyBankAccountVBDStatusChange_event_found, realmId=" + realmId);
            return null;
        }
        // Fetch the latest event from the list of VBD events because it will give us the current VBD status of PSP company
        CompanyEvent companyEvent = vbdEventsList.getFirst();
        DomainEntitySet<CompanyEventDetail> companyEventDetailsSet = companyEvent.getCompanyEventDetailCollection();
        String companyBankAccountId = null;
        // Fetching `CompanyBankAccountId` and `NewBAStatus`

        for(CompanyEventDetail companyEventDetail: companyEventDetailsSet) {

            if(companyEventDetail.getEventDetailTypeCd() == EventDetailTypeCode.CompanyBankAccountId) {
                companyBankAccountId = companyEventDetail.getValue();
            }

            if(companyEventDetail.getEventDetailTypeCd() == EventDetailTypeCode.NewBAStatus) {
                pspVBDStatus = companyEventDetail.getValue();
            }
        }
        CompanyBankAccount companyBankAccount = accountServiceTranslator.getCompanyBankAccountByAccountNumber(paymentsBankAccount, company);

        logger.info("Action=getPSPVBDStatus, method=findCompanyBankAccountByAccountNumber, isPspCompanyBankAccountFound=" + Objects.nonNull(companyBankAccount) + ", realmId=" + realmId);

        // Comparing CompanyBankAccountId of PSP Bank Account and Payments bank account
        if(companyBankAccountId.equals(companyBankAccount.getId().toString())) {
            return pspVBDStatus;

        }

        return null;
    }

    /**
     * Generate an event if the VBD Status of a company has been changed.
     *
     * @param paymentsAccount
     * @param company
     */
    public void handleVBDEvents(PaymentsAccount paymentsAccount, Company company, String realmId) {
        BankAccount paymentsBankAccount = accountServiceTranslator.getPaymentBank(paymentsAccount);
        if(Objects.isNull(paymentsBankAccount)) {
            return;
        }

        CompanyBankAccount companyBankAccount = accountServiceTranslator.getCompanyBankAccountByAccountNumber(paymentsBankAccount, company);
        String paymentsAccountVBDStatus = paymentsBankAccount.getVbdStatus().value();
        String pspVBDStatus = getPSPVBDStatus(paymentsBankAccount, company, realmId);
        // If the VBD status of payments bank account and psp bank account are different, then raise a VBDStatusChange Event
        if(!paymentsAccountVBDStatus.equals(pspVBDStatus)) {
            CompanyEvent.createVBDStatusChangeEvent(company, companyBankAccount, pspVBDStatus, paymentsAccountVBDStatus);
        }
    }

    public boolean isActivateBankAccountRequired(PaymentsAccount paymentsAccount, Company company, String realmId) {

        BankAccount paymentsBankAccount = accountServiceTranslator.getPaymentBank(paymentsAccount);

        if(Objects.isNull(paymentsBankAccount) || !paymentsBankAccount.getVbdStatus().equals(VbdStatusEnum.VERIFIED)) {
            return false;
        }

        CompanyBankAccount companyBankAccount = accountServiceTranslator.getCompanyBankAccountByAccountNumber(paymentsBankAccount, company);
        logger.info("Action=isActivateBankAccountRequired, method=findCompanyBankAccountByAccountNumber, isPspCompanyBankAccountFound=" + Objects.nonNull(companyBankAccount) + ", realmId=" + realmId);

        if(Objects.isNull(companyBankAccount) || companyBankAccount.getStatusCd().equals(BankAccountStatus.Active)){
            return false;
        }

        return true;
    }


    public boolean isUpdateCompanyRequired(CompanyDTO companyDTO, Company company){

        return !isCompanyAttributesMatched(companyDTO,company);
    }


    private boolean isCompanyAttributesMatched(CompanyDTO companyDTO, Company company) {

        if(!PSPStringUtils.isEqual(companyDTO.getFein(),company.getFedTaxId())){
            return false;
        }

        if(!PSPStringUtils.isEqual(companyDTO.getLegalName(),company.getLegalName())){
            return false;
        }

        if(!PSPStringUtils.isEqual(companyDTO.getDBA(),company.getDbaName())){
            return false;
        }

        if(!isAddressEquals(companyDTO.getMailingAddress(),company.getMailingAddress())){
            return false;
        }

        if (accountServiceTranslator.isSMSAddressFixEnabled()) {
            if(!isAddressEquals(companyDTO.getComplianceAddress(),company.getComplianceAddress())){
                return false;
            }
        } else {
            if(!isAddressEquals(companyDTO.getLegalAddress(),company.getLegalAddress())){
                return false;
            }
        }

        if(!isContactEquals(companyDTO.getContacts(),company.getContactCollection())){
            return false;
        }


        if(!Objects.isNull(companyDTO.getCompanyAdditionalInfo())){
            if(Objects.isNull(company.getCompanyAdditionalInfo())){
                return false;
            }

            if(Objects.isNull(company.getCompanyAdditionalInfo().getIndustryType())){
                return false;
            }

            if(Objects.isNull(company.getCompanyAdditionalInfo().getIndustryType().getIndustry())){
                return false;
            }

            if(!StringUtils.equals(companyDTO.getCompanyAdditionalInfo().getIndustry(),company.getCompanyAdditionalInfo().getIndustryType().getIndustry())){
                return false;
            }
        }



        return true;
    }

    private boolean isContactEquals(Collection<ContactDTO> contactsDTOSet, DomainEntitySet<Contact> companyContactSet) {

        if(Objects.isNull(contactsDTOSet) || Objects.isNull(companyContactSet)){
            return true;
        }

        if(contactsDTOSet.size() != companyContactSet.size()){
            return false;
        }

        ContactDTO primaryPrincipleDTO=null;
        Optional<ContactDTO> primaryPrincipleOptionDTO = contactsDTOSet.stream().filter(contactDTO -> contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        if(primaryPrincipleOptionDTO.isPresent()){
            primaryPrincipleDTO = primaryPrincipleOptionDTO.get();
        }

        Contact primaryPrinciple=null;

        Optional<Contact> primaryPrincipleOptional = companyContactSet.stream().filter(Contact -> Contact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)).findFirst();

        if(primaryPrincipleOptional.isPresent()){
            primaryPrinciple = primaryPrincipleOptional.get();
        }

        if(Objects.isNull(primaryPrincipleDTO) && !Objects.isNull(primaryPrinciple)){
            return false;
        }

        if(!Objects.isNull(primaryPrincipleDTO) && Objects.isNull(primaryPrinciple)){
            return false;
        }

        if(!PSPStringUtils.isEqual(primaryPrincipleDTO.getEmail(),primaryPrinciple.getEmail())){
            return false;
        }

        if(!PSPStringUtils.isEqual(primaryPrincipleDTO.getFirstName(),primaryPrinciple.getFirstName())){
            return false;
        }

        if(!PSPStringUtils.isEqual(primaryPrincipleDTO.getLastName(),primaryPrinciple.getLastName())){
            return false;
        }

        /*
        if(!PSPStringUtils.isEqual(primaryPrincipleDTO.getMiddleName(),primaryPrinciple.getMiddleName())){
            return false;
        }*/

        if(!PSPStringUtils.isEqual(primaryPrincipleDTO.getPhoneNumber(),primaryPrinciple.getPhone())){
            return false;
        }

        if(!PSPStringUtils.isEqual(primaryPrincipleDTO.getSocialSecurityNumber(),primaryPrinciple.getSocialSecurityNumber())){
            return false;
        }

        if(!isAddressEquals(primaryPrincipleDTO.getAddress(),primaryPrinciple.getMailingAddress())){
            return false;
        }

        return true;
    }


    private boolean isAddressEquals(AddressDTO updateAddressDTO, Address companyAddress){

        if(Objects.isNull(updateAddressDTO) && Objects.isNull(companyAddress)){
            return true;
        }

        if(Objects.isNull(updateAddressDTO) && !Objects.isNull(companyAddress)){
            return false;
        }

        if(!Objects.isNull(updateAddressDTO) && Objects.isNull(companyAddress)){
            return false;
        }

        if(!PSPStringUtils.isEqual(updateAddressDTO.getAddressLine1(), companyAddress.getAddressLine1())){
            return false;
        }

        if(!PSPStringUtils.isEqual(updateAddressDTO.getAddressLine2(),companyAddress.getAddressLine2())){
            return false;
        }

        if(!PSPStringUtils.isEqual(updateAddressDTO.getAddressLine3(),companyAddress.getAddressLine3())){
            return false;
        }

        if(!PSPStringUtils.isEqual(updateAddressDTO.getCity(),companyAddress.getCity())){
            return false;
        }

        if(!PSPStringUtils.isEqual(updateAddressDTO.getState(),companyAddress.getState())){
            return false;
        }

        if (!isCountryEqual(updateAddressDTO, companyAddress)){
            return false;
        }

        String updateAddressZipCode = updateAddressDTO.getZipCode();
        String domainCompanyAddressZipCode = companyAddress.getZipCode();
        String updateAddressZipCodeExtension = updateAddressDTO.getZipCodeExtension();
        String domainCompanyAddressZipCodeExtension = companyAddress.getZipCodeExtension();
        if(Objects.nonNull(updateAddressDTO.getZipCode()) && updateAddressDTO.getZipCode().contains("-") ){
            String zipCodeAndExtension [] =  updateAddressDTO.getZipCode().split("-");
            updateAddressZipCode = zipCodeAndExtension[0];
            updateAddressZipCodeExtension = zipCodeAndExtension[1];
        }


        if(Objects.nonNull(companyAddress.getZipCode()) && companyAddress.getZipCode().contains("-")){
            String domainZipCodeAndExtension [] =  companyAddress.getZipCode().split("-");
            domainCompanyAddressZipCode = domainZipCodeAndExtension[0];
            domainCompanyAddressZipCodeExtension = domainZipCodeAndExtension[1];
        }

        if(!PSPStringUtils.isEqual(updateAddressZipCode,domainCompanyAddressZipCode)){
            return false;
        }

        if(!PSPStringUtils.isEqual(updateAddressZipCodeExtension,domainCompanyAddressZipCodeExtension)){
            return false;
        }

        return true;
    }

    private boolean isCountryEqual(AddressDTO updateAddressDTO, Address companyAddress) {
        return (PSPStringUtils.isEqual(updateAddressDTO.getCountry(), companyAddress.getCountry())) ||
                ((PSPStringUtils.isEqual(US,updateAddressDTO.getCountry()) || PSPStringUtils.isEqual(USA,updateAddressDTO.getCountry()))
                        && (PSPStringUtils.isEqual(US,companyAddress.getCountry()) || PSPStringUtils.isEqual(USA,companyAddress.getCountry())));
    }

    public Collection<ServiceSubStatusCode> getHoldsTobeRemovedTemporary(PaymentsAccount paymentsAccount, Company mDomainCompany) {

        return getCurrentHolds(mDomainCompany);
    }

    public Collection<ServiceSubStatusCode> getHoldsTobeRemoved(PaymentsAccount paymentsAccount, Company mDomainCompany) {

        Collection<ServiceSubStatusCode> holdsToBeRemoved = getCurrentHolds(mDomainCompany);

        holdsToBeRemoved.retainAll(REMOVABLE_HOLDS);

        ServiceSubStatusCode hold = getHoldsForNonActiveCompany(paymentsAccount);
        if(hold != null){
            holdsToBeRemoved.remove(hold);
        }

        return holdsToBeRemoved;
    }

    public Collection<ServiceSubStatusCode> getCurrentHolds(Company mDomainCompany) {
        return mDomainCompany.getCurrentOnHoldReasonCodes();
    }


    public boolean isPaymentsAccountActive(PaymentsAccount paymentsAccount) {

        return paymentsAccount.getMoneyOutStatus()==AccountStatusEnum.ACTIVE ;

    }

    public boolean isPaymentsAccountClosed(PaymentsAccount paymentsAccount) {

        return paymentsAccount.getMoneyOutStatus()==AccountStatusEnum.CLOSED ;

    }

    public ServiceSubStatusCode getHoldsForNonActiveCompany(PaymentsAccount paymentsAccount) {

        switch (paymentsAccount.getMoneyOutStatus()){
            case PENDING:
                return ServiceSubStatusCode.AMLHold;
            case SUSPENDED:
                return ServiceSubStatusCode.SuspendedDirectDeposit;
            case DENIED:
                return ServiceSubStatusCode.AMLHold;
        }

        return null;
    }


    public boolean isUpdateBankAccountRequired(PaymentsAccount paymentsAccount, Company company, String realmId) {


        BankAccount paymentsBankAccount = accountServiceTranslator.getPaymentBank(paymentsAccount);
        if(Objects.isNull(paymentsBankAccount)){
            logger.info("Action=isUpdateBankAccountRequired, updateRequired=false, msg=paymentsBankAccount_not_found, realmId=" + realmId);
            return false;
        }

        try {
            CompanyBankAccount pspCompanyBankAccountByAccountNumber = accountServiceTranslator.getCompanyBankAccountByAccountNumber(paymentsBankAccount, company);
            logger.info("Action=isUpdateBankAccountRequired, method=findCompanyBankAccountByAccountNumber, isPspCompanyBankAccountFound=" + Objects.nonNull(pspCompanyBankAccountByAccountNumber) + ", realmId=" + realmId);
        } catch (Exception e) {
            logger.error("Action=isUpdateBankAccountRequired, status=Error, method=findCompanyBankAccountByAccountNumber, realmId=" + realmId, e);
        }


        CompanyBankAccount pspCompanyBankAccount =  CompanyBankAccount.findCompanyBankAccount(company);
        logger.info("Action=isUpdateBankAccountRequired, method=findCompanyBankAccountByStatus, isPspCompanyBankAccountFound=" + Objects.nonNull(pspCompanyBankAccount) + ", realmId=" + realmId);

        if(Objects.isNull(pspCompanyBankAccount)){
            logger.info("Action=isUpdateBankAccountRequired, updateRequired=true, msg=pspCompanyBankAccount_not_found, realmId=" + realmId);
            return true;
        }

        com.intuit.sbd.payroll.psp.domain.BankAccount pspBankAccount = pspCompanyBankAccount.getBankAccount();


        if(isBankAccountEquals(paymentsBankAccount,pspBankAccount, realmId) &&
                isBankNameEquals(paymentsBankAccount,pspBankAccount, realmId)){
            logger.info("Action=isUpdateBankAccountRequired, updateRequired=false, msg=same_account, realmId=" + realmId);
            return false;
        }

        logger.info("Action=isUpdateBankAccountRequired, updateRequired=true, msg=different_account, realmId=" + realmId);
        return true;
    }

    public boolean isBankAccountEquals(com.intuit.payments.cdm.v2.client.BankAccount paymentsBankAccount,
                                       com.intuit.sbd.payroll.psp.domain.BankAccount pspBankAccount, String realmId){

        if (!areNumbersEqual(pspBankAccount.getAccountNumber(), paymentsBankAccount.getAccountNumber())) {
            logger.info("Action=isBankAccountEquals, type=AccountNumber, equal=false, realmId=" + realmId);
            return false;
        }

        if (!areNumbersEqual(pspBankAccount.getRoutingNumber(), paymentsBankAccount.getRoutingNumber())) {
            logger.info("Action=isBankAccountEquals, type=RoutingNumber, equal=false, realmId=" + realmId);
            return false;
        }

        BankAccountType bankAccountType = accountServiceTranslator.getBankAccountType(paymentsBankAccount);
        if (pspBankAccount.getAccountTypeCd() != bankAccountType) {
            logger.info("Action=isBankAccountEquals, type=AccountType, equal=false, realmId=" + realmId);
            return false;
        }

        logger.info("Action=isBankAccountEquals, equal=true, realmId=" + realmId);
        return true;
    }

    public boolean isBankNameEquals(com.intuit.payments.cdm.v2.client.BankAccount paymentsBankAccount,
                                    com.intuit.sbd.payroll.psp.domain.BankAccount pspBankAccount, String realmId){
        if(!PSPStringUtils.isEqual(pspBankAccount.getBankName(),paymentsBankAccount.getName())){
            logger.info("Action=isBankNameEquals, type=BankName, equal=false, realmId=" + realmId);
            return false;
        }
        logger.info("Action=isBankNameEquals, type=BankName, equal=true, realmId=" + realmId);
        return true;
    }


    public boolean isDeactivateServiceRequired(PaymentsAccount paymentsAccount) {
        return isPaymentsAccountClosed(paymentsAccount);

    }

    public boolean isActivateServiceRequired(PaymentsAccount paymentsAccount) {
        return isPaymentsAccountActive(paymentsAccount);

    }

    private boolean areNumbersEqual(String a, String b) {
        return StringUtils.equals(extractDigitsOnly(a), extractDigitsOnly(b));
    }

    private CharSequence extractDigitsOnly(String value) {
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll(ONLY_DIGITS, StringUtils.EMPTY);
    }
}
