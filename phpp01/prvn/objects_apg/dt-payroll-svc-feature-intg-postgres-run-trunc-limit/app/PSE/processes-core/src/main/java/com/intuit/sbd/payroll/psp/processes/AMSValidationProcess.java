package com.intuit.sbd.payroll.psp.processes;

import com.intuit.payments.cdm.v2.client.BusinessOwner;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.PrimaryBusiness;
import com.intuit.payments.cdm.v2.client.enums.VbdStatusEnum;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

import static com.intuit.spc.foundations.portability.util.SpcfCalendar.createInstance;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class AMSValidationProcess extends Process implements IProcess {

    private Company company;

    private String realmId;

    private String psid;

    private PaymentsAccount paymentsAccount;
    private final AccountServiceTranslator accountServiceTranslator;

    private AccountServiceGateway accountServiceGateway;

    private Contact pspOwner;

    private boolean countryCheckEnabled = true;

    private boolean addressCheckEnabled = true;

    private boolean beforeSyncValidation = true;

    private boolean migrationStatus = true;


    public AMSValidationProcess(Company company) {
        this.company = company;
        this.psid = company.getSourceCompanyId();
        this.realmId = company.getIAMRealmId();
        accountServiceTranslator = PayrollApplicationBeanFactory.getBean(AccountServiceTranslator.class);
        accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
    }


    @Override
    public ProcessResult validate() {
        return null;
    }


    @Override
    public ProcessResult process() {

        StopWatch timer = StopWatch.startTimer();

        boolean isEqual;
        ProcessResult pr = new ProcessResult();

        String logPrefix = "job=AMSValidationProcess, Action=process, Status={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}{}";
        log.info(logPrefix, "Start", realmId, migrationStatus, beforeSyncValidation, StringUtils.EMPTY);

        paymentsAccount = getPaymentsAccount(realmId);

        //Getting owner from PSP company
        DomainEntitySet<Contact> contacts = company.getContactCollection();
        Optional<Contact> primaryPrincipalOp = contacts.stream().filter(contact -> contact.getContactRoleCd() == ContactRole.PrimaryPrincipal).findFirst();
        pspOwner = primaryPrincipalOp.get();

        boolean isBusinessInfoEqual = isBusinessInfoEqual(psid, realmId, paymentsAccount);
        boolean isBusinessOwnerEqual = isBusinessOwnerEqual(psid, realmId, paymentsAccount);
        boolean isBankEqual = isBankEqual(psid, realmId, paymentsAccount);

        isEqual = isBusinessInfoEqual && isBusinessOwnerEqual && isBankEqual;

        log.info(logPrefix, "Complete", realmId + ", isEqual=" + isEqual, migrationStatus, beforeSyncValidation + ", elapsed_time=" + timer.stop().getElapsedTimeString(), StringUtils.EMPTY);
        pr.setResult(isEqual);
        return pr;

    }

    private boolean isBusinessInfoEqual(String psid, String realmId, PaymentsAccount pa) {
        boolean isEqual;

        PrimaryBusiness businessInfo = getBusinessInfoFromPaymentsAccount(pa);

        if (isNull(businessInfo)) {
            return false;
        }

        Address amsAddress = copyAddress(realmId, businessInfo.getAddress());

        String pspDbaName = company.getDbaName();

        //AMS copies over LegalName to DBAName in case psp sends null/empty DBAName, hence validation is added to check the same behaviour.
        if (isBlank(pspDbaName)) {
            pspDbaName = company.getLegalName();
        }

        boolean isLegalNameEqual = isFieldEqual(psid, realmId, "LEGALNAME", company.getLegalName(), businessInfo.getLegalName(), true);
        boolean isDbaNameEqual = isFieldEqual(psid, realmId, "DBANAME", pspDbaName, businessInfo.getBusinessName(), true);
        boolean isEinEqual = areNumbersEqual(psid, realmId, "EIN", company.getFedTaxId(), businessInfo.getEin());

        boolean isOwnerEmailEqual = isFieldEqual(psid, realmId, "OWNEREMAIL", pspOwner.getEmail(), businessInfo.getEmail(), true);
        boolean isComplianceAddressEqual = isAddressEqual(psid, realmId, "COMPLIANCEADDRESS", amsAddress, company.getComplianceAddress());
        boolean isMailingAddressEqual = isAddressEqual(psid, realmId, "MAILINGADDRESS", amsAddress, company.getMailingAddress());

        isEqual = isLegalNameEqual && isDbaNameEqual && isEinEqual && isOwnerEmailEqual &&
                isComplianceAddressEqual;

        if (addressCheckEnabled) {
            isEqual = isEqual && isMailingAddressEqual;
        }

        return isEqual;
    }

    private boolean isBusinessOwnerEqual(String psid, String realmId, PaymentsAccount pa) {
        boolean isEqual = false;
        BusinessOwner owner = accountServiceTranslator.getPaymentsPrimaryPrincipal(pa);
        if (isNull(owner)) {
            log.error("job=AMSValidationProcess, Action=isBusinessOwnerEqual, status=Error, msg=Owner_Null, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", psid, realmId, migrationStatus, beforeSyncValidation);
            return isEqual;
        }


        boolean isFirstNameEqual = isFieldEqual(psid, realmId, "OWNERFIRSTNAME", pspOwner.getFirstName(), owner.getFirstName(), true);
        boolean isLastNameEqual = isFieldEqual(psid, realmId, "OWNERLASTNAME", pspOwner.getLastName(), owner.getLastName(), true);
        boolean isPhoneEqual = areNumbersEqual(psid, realmId, "OWNERPHONE", pspOwner.getPhone(), owner.getPhone());

        boolean isSsnEqual = areNumbersEqual(psid, realmId, "SSN", pspOwner.getSocialSecurityNumber(), owner.getSsn());

        Address amsAddress = copyAddress(realmId, owner.getAddress());
        boolean isOwnerAddressEqual = isAddressEqual(psid, realmId, "OWNERADDRESS", amsAddress, pspOwner.getMailingAddress());

        boolean isDobEqual = compareDate(psid, realmId, "OWNERDOB", pspOwner.getDateOfBirth(), owner.getDateOfBirth());

        isEqual = isFirstNameEqual && isLastNameEqual && isPhoneEqual && isSsnEqual && isOwnerAddressEqual && isDobEqual;

        return isEqual;
    }

    private boolean isBankEqual(String psid, String realmId, PaymentsAccount pa) {
        boolean isEqual = false;
        com.intuit.payments.cdm.v2.client.BankAccount amsBankAccount = accountServiceTranslator.getPaymentBank(pa);
        if (isNull(amsBankAccount)) {
            log.error("job=AMSValidationProcess, Action=isBankEqual, status=Error, msg=AmsBankAccount_Null, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", psid, realmId, migrationStatus, beforeSyncValidation);
            return isEqual;
        }

        CompanyBankAccount pspCompanyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);

        if (Objects.isNull(pspCompanyBankAccount)) {
            log.error("job=AMSValidationProcess, Action=isBankEqual, status=Error, msg=pspCompBankAccount_Null, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", psid, realmId, migrationStatus, beforeSyncValidation);
            return isEqual;
        }

        if (Objects.isNull(pspCompanyBankAccount.getBankAccount())) {
            log.error("job=AMSValidationProcess, Action=isBankEqual, status=Error, msg=pspBankAccount_Null, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", psid, realmId, migrationStatus, beforeSyncValidation);
            return isEqual;
        }


        if (isBlank(pspCompanyBankAccount.getBankAccount().getAccountNumber())) {
            log.error("job=AMSValidationProcess, Action=isBankEqual, status=Error, msg=pspBankAccountNum_Null, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", psid, realmId, migrationStatus, beforeSyncValidation);
            return isEqual;
        }


        BankAccount pspBankAccount = pspCompanyBankAccount.getBankAccount();

        boolean isBankAccountNumberEqual = areNumbersEqual(psid, realmId, "BANKACCOUNTNUMBER", pspBankAccount.getAccountNumber(), amsBankAccount.getAccountNumber());
        boolean isBankRoutingNumberEqual = areNumbersEqual(psid, realmId, "BANKROUTINGNUMBER", pspBankAccount.getRoutingNumber(), amsBankAccount.getRoutingNumber());
        boolean isBankAccountTypeEqual = pspBankAccount.getAccountTypeCd() == accountServiceTranslator.getBankAccountType(amsBankAccount);

        if(!isBankAccountTypeEqual){
            log.error("job=AMSValidationProcess, Action=isBankAccountTypeEqual, status={}, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", isBankAccountTypeEqual, psid, realmId, migrationStatus, beforeSyncValidation);
        }

        boolean isBankStatusEqual = amsBankAccount.getVbdStatus().equals(VbdStatusEnum.VERIFIED);

        if(!isBankStatusEqual){
            log.error("job=AMSValidationProcess, Action=isBankStatusEqual, status={}, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}", isBankStatusEqual, psid, realmId, migrationStatus, beforeSyncValidation);
        }

        isEqual = isBankAccountNumberEqual && isBankRoutingNumberEqual && isBankAccountTypeEqual && isBankStatusEqual;

        return isEqual;
    }

    private boolean isAddressEqual(String psid, String realmId, String type, Address amsAddress, Address pspAddress) {

        String errMsg;

        String logPrefix = "job=AMSValidationProcess, Action=isAddressEqual, errMsg={}, psid={}, realmId={}, MigrationStatus={}, beforeSyncValidation={}";

        if(isBlank(pspAddress.getAddressLine2()) && pspAddress.getAddressLine1().contains("\\R")){
            String[] addr =  pspAddress.getAddressLine1().split("\\R");
            pspAddress.setAddressLine1(addr[0]);
            pspAddress.setAddressLine2(addr[1]);
        }

        boolean isAddressLine1Equal = equalsIgnoreCase(amsAddress.getAddressLine1(), pspAddress.getAddressLine1());
        boolean isAddressLine2Equal = compareAddressLine(amsAddress.getAddressLine2(), pspAddress.getAddressLine2());
        boolean isCityEqual = equalsIgnoreCase(amsAddress.getCity(), pspAddress.getCity());
        boolean isStateEqual = equalsIgnoreCase(amsAddress.getState(), pspAddress.getState());

        boolean isCountryEqual = true;

        if (countryCheckEnabled) {
            isCountryEqual = (
                    // either both countries are same or one is 'US' & other is 'USA'
                    (equalsIgnoreCase(amsAddress.getCountry(), pspAddress.getCountry())) ||
                            ((Address.US.equals(amsAddress.getCountry()) || Address.USA.equals(amsAddress.getCountry()))
                                    && (Address.US.equals(pspAddress.getCountry()) || Address.USA.equals(pspAddress.getCountry())))
            );
        }

        String amsPostalCode = Address.buildPostalCode(amsAddress.getZipCode(), amsAddress.getZipCodeExtension());
        String pspPostalCode = Address.buildPostalCode(pspAddress.getZipCode(), pspAddress.getZipCodeExtension());


        boolean isPostalCodeEqual = equalsIgnoreCase(amsPostalCode, pspPostalCode);

        if (!isAddressLine1Equal) {
            errMsg = "AddressLine1 does not match!, Type=" + type;
            log.error(logPrefix, errMsg, psid, realmId, migrationStatus, beforeSyncValidation);
        }

        if (!isAddressLine2Equal) {
            errMsg = "AddressLine2 does not match!, Type=" + type;
            log.error(logPrefix, errMsg, psid, realmId, migrationStatus, beforeSyncValidation);
        }

        if (!isCityEqual) {
            errMsg = "City does not match!, Type=" + type;
            log.error(logPrefix, errMsg, psid, realmId, migrationStatus, beforeSyncValidation);
        }

        if (!isStateEqual) {
            errMsg = "Region does not match!, Type=" + type;
            log.error(logPrefix, errMsg, psid, realmId, migrationStatus, beforeSyncValidation);
        }

        if (!isCountryEqual) {
            errMsg = "Country does not match!, Type=" + type;
            log.error(logPrefix, errMsg, psid, realmId, migrationStatus, beforeSyncValidation);
        }


        if (!isPostalCodeEqual) {
            errMsg = "PostalCode does not match!, Type=" + type;
            log.error(logPrefix, errMsg, psid, realmId);
        }

        boolean isEqual = isAddressLine1Equal && isAddressLine2Equal && isCityEqual && isStateEqual && isCountryEqual
                && isPostalCodeEqual;

        return isEqual;
    }


    private Address copyAddress(String realmId, PhysicalAddress a) {

        if (isBlank(a.getStreetAddress())) {
            log.error("job=AMSValidationProcess, action=copyAddress, errMsg=StreetAddress is blank, realmId={}, MigrationStatus={}, beforeSyncValidation={}", realmId, migrationStatus, beforeSyncValidation);
            return null;
        }

        Address b = new Address();
        String[] lines = a.getStreetAddress().split(Address.ADDRESS_SPLIT_REGEX);
        if (lines.length > 0) {
            b.setAddressLine1(lines[0]);
        }

        if (lines.length > 1) {
            b.setAddressLine2(lines[1]);
        }

        b.setCity(a.getCity());
        b.setState(a.getRegion());
        b.setCountry(a.getCountry());
        Matcher matcher = Address.ZIP_CODE_PATTERN.matcher(a.getPostalCode());
        if (matcher.matches()) {
            b.setZipCode(matcher.group(1));
            b.setZipCodeExtension(matcher.group(4));
        }
        return b;
    }

    private boolean isFieldEqual(String psid, String realmId, String fieldName, String pspField, String amsField,
                                 boolean ignoreCase) {
        boolean isEqual = ignoreCase ? equalsIgnoreCase(pspField, amsField) : StringUtils.equals(pspField, amsField);
        if (!isEqual) {
            log.error("job=AMSValidationProcess, Action=isFieldEqual, status={}, FieldName={}, realmId={}, psid={}, MigrationStatus={}, beforeSyncValidation={}", isEqual, fieldName, realmId, psid, migrationStatus, beforeSyncValidation);
        }
        return isEqual;
    }

    private boolean areNumbersEqual(String psid, String realmId, String fieldName, String pspField, String amsField) {
        boolean isEqual = StringUtils.equals(extractDigitsOnly(pspField), extractDigitsOnly(amsField));
        if (!isEqual) {
            log.error("job=AMSValidationProcess, Action=areNumbersEqual, status={}, FieldName={}, realmId={}, psid={}, MigrationStatus={}, beforeSyncValidation={}", isEqual, fieldName, realmId, psid, migrationStatus, beforeSyncValidation);
        }
        return isEqual;
    }

    private CharSequence extractDigitsOnly(String value) {
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll(Address.ONLY_DIGITS, StringUtils.EMPTY);
    }

    private PrimaryBusiness getBusinessInfoFromPaymentsAccount(PaymentsAccount paymentsAccount) {
        if (isNull(paymentsAccount) || isNull(paymentsAccount.getBusinessInfo())) {
            return null;
        }
        return paymentsAccount.getBusinessInfo();
    }

    private boolean compareDate(String psid, String realmId, String fieldName, SpcfCalendar d1, Date d2) {

        if (isNull(d1)) {
            log.error("job=AMSValidationProcess, Action=compareDate, status=Error, FieldName={}, errType=Null, realmId={}, psid={}, MigrationStatus={}, beforeSyncValidation={}", fieldName, realmId, psid, migrationStatus, beforeSyncValidation);
            return false;
        }

        SpcfCalendar d2Calendar = createInstance(d2.getTime());

        // sample log:
        // d1: 1990/11/11 08:00:00.0, d2: 1990-11-11T00:00:00.000-0800, isEqual=true
        boolean isEqual = d1.compareTo(d2Calendar) == 0;

        if (!isEqual) {
            log.error("job=AMSValidationProcess, Action=compareDate, status={}, FieldName={}, realmId={}, psid={}, MigrationStatus={}, beforeSyncValidation={}", isEqual, fieldName, realmId, psid, migrationStatus, beforeSyncValidation);
        }
        return isEqual;
    }


    private PaymentsAccount getPaymentsAccount(String realmId) {
        String logPrefix = "job=AMSValidationProcess, Action=getPaymentsAccount, Status={}, realmId={}, migrationStatus={}, beforeSyncValidation={}{}";

        try {
            paymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
        } catch (AccountServicesException e) {
            // PSP defensively checks existence of AMS account, before creating a new one. Hence marking 404 as info in this case
            if (HttpStatus.SC_NOT_FOUND == e.getHttpServiceResponse().getStatusCode()) {
                log.info(logPrefix, "PaymentsAccountNotFound", realmId, migrationStatus, beforeSyncValidation, StringUtils.EMPTY);
            } else {
                log.error(logPrefix, "getPaymentsAccountError", realmId, migrationStatus, beforeSyncValidation, e, StringUtils.EMPTY);
            }
        } catch(HttpClientErrorException excp) {
            log.info(logPrefix, "ClientErrorExceptionPaymentsAccountNotFound", realmId, migrationStatus, beforeSyncValidation, StringUtils.EMPTY);
        } catch (CallNotPermittedException cnpe) {
            log.error(logPrefix, "getPaymentsAccountCallNotPermittedException", realmId, migrationStatus, beforeSyncValidation, cnpe, StringUtils.EMPTY);
        }

        return paymentsAccount;
    }

    public void setCountryCheckEnabled(boolean countryCheckEnabled) {
        this.countryCheckEnabled = countryCheckEnabled;
    }

    public void setAddressCheckEnabled(boolean addressCheckEnabled) {
        this.addressCheckEnabled = addressCheckEnabled;
    }


    public void setBeforeSyncValidation(boolean beforeSyncValidation) {
        this.beforeSyncValidation = beforeSyncValidation;
    }

    public void setMigrationStatus(boolean migrationStatus) {
        this.migrationStatus = migrationStatus;
    }


    private boolean compareAddressLine(String amsAddrLine, String pspAddrLine) {

        boolean isAddressLineEqual;

        //This check is to compare use cases where amsAddrLine is null & pspAddressLine is empty/whiteSpace & vice-versa
        if (isBlank(amsAddrLine) && isBlank(pspAddrLine)) {
            isAddressLineEqual = true;
        } else {
            isAddressLineEqual = equalsIgnoreCase(amsAddrLine, pspAddrLine);
        }

        return isAddressLineEqual;
    }

}
