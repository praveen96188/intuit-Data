package com.intuit.ems.payroll.psp.ams_migration;

import com.google.common.collect.Lists;
import com.intuit.ems.payroll.psp.model.AddressReportModel;
import com.intuit.payments.cdm.v2.client.*;
import com.intuit.payments.cdm.v2.client.enums.VbdStatusEnum;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPStringUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.intuit.ems.payroll.psp.ams_migration.AmsSyncValidation.CompanyDataField.*;
import static com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils.deterministicDecrypt;
import static com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils.probabilisticDecryptDate;
import static com.intuit.spc.foundations.portability.util.SpcfCalendar.createInstance;
import static java.lang.Boolean.parseBoolean;
import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.*;


@Slf4j
public class AmsSyncValidation {

    private final String fileName;
    private Map<String, CompanyData> outputMap;
    private final int batchSize;
    private final String outputFile;
    private final boolean verboseSensitiveInfo;
    private final AccountServiceGateway accountServiceGateway;
    private final AccountServiceTranslator accountServiceTranslator;

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("((\\d){5})(\\-)?((\\d){4})?$");
    private static final String ADDRESS_SPLIT_REGEX = "\\r?\\n|\\r";
    private static final String NUMBER_SPECIAL_CHAR_REGEX = "^[0-9]+$";
    private static final String ONLY_DIGITS = "[^0-9]";
    private static final String US = "US";
    private static final String USA = "USA";
    private static final String EQUAl = "Y";
    private static final String NOT_EQUAL = "N";
    private static final String PAYMENTS_ACCOUNT_NOT_FOUND = "PA-404";

    private static final String COMPANY_HQL =
            "Select C.SourceCompanyId, C.IAMRealmId, C.LegalName, C.DbaName, C.FedTaxIdEnc, C.LegalAddress, C.MailingAddress, " +
                    "             C.ComplianceAddress, IND.MailingAddress, IND.FirstName, " +
                    "             IND.MiddleName, IND.LastName, IND.Email, IND.Phone, CT.DateOfBirthEnc, " +
                    "             BA.AccountNumberEnc, BA.RoutingNumber, BA.AccountTypeCd, BA.BankName, " +
                    "             CT.SocialSecurityNumberEnc" +
                    "             from" +
                    "             com.intuit.sbd.payroll.psp.domain.Company as C, " +
                    "             com.intuit.sbd.payroll.psp.domain.SMSMigration as S, " +
                    "             com.intuit.sbd.payroll.psp.domain.Contact as CT, " +
                    "             com.intuit.sbd.payroll.psp.domain.Individual as IND, " +
                    "             com.intuit.sbd.payroll.psp.domain.CompanyBankAccount as CBA, " +
                    "             com.intuit.sbd.payroll.psp.domain.BankAccount as BA " +
                    "             where C = CT.Company " +
                    "             and S.Company = C and S.MigrationStatus != 'MigrationOnHold' " +
                    "             and IND = CT AND CT.ContactRoleCd='PrimaryPrincipal' " +
                    "             and C=CBA.Company and BA = CBA.BankAccount " +
                    "             and CBA.StatusCd='Active' and C.IAMRealmId in (:REALM_LIST)";

    private AmsSyncValidation(String fileName, int batchSize, String outputFile, String verboseSensitiveInfo) {
        this.fileName = fileName;
        this.batchSize = batchSize;
        this.outputFile = outputFile;
        this.outputMap = new HashMap<>();
        this.verboseSensitiveInfo = !isEmpty(verboseSensitiveInfo) && parseBoolean(verboseSensitiveInfo);
        this.accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
        accountServiceTranslator = PayrollApplicationBeanFactory.getBean(AccountServiceTranslator.class);
    }

    public static void main(String[] args) {
        try {
            log.info("Main fn started");
            Application.initialize();
            String fileName = args[0];
            int batchSize = Integer.parseInt(args[1]);
            String outputFile = args[2];
            String verboseAccountNumber = args[3];
            log.info("Action=mainFn, FileName={}, batchSize={}, outputFile={}", fileName, batchSize, outputFile);
            AmsSyncValidation amsSyncValidation = new AmsSyncValidation(fileName, batchSize, outputFile, verboseAccountNumber);
            amsSyncValidation.process();
        } finally {
            log.info("Main fn ended");
            Application.uninitialize();
            System.exit(0);
        }
    }

    private List<String> readRealmIdsFromFile(String fileName) throws IOException {
        List<String> realmList = lines(get(fileName)).collect(Collectors.toList());
        realmList.replaceAll(String::trim);
        log.info("RealmIdCount={}", realmList.size());
        return realmList;
    }

    private void process() {
        String logPrefix = "Action=process, Status={}, {}";
        try {
            log.info(logPrefix, "Start", StringUtils.EMPTY);
            List<String> realmIds = readRealmIdsFromFile(fileName);

            long s = System.currentTimeMillis();
            List<List<String>> realmIdsList = Lists.partition(realmIds, batchSize);
            log.info("RealmIdListsCount={}", realmIdsList.size());

            for (List<String> l : realmIdsList) {
                try {
                    compare(l);
                } catch (Exception e) {
                    log.error(logPrefix, "Error", "l: " + l + ", errType=Exception", e);
                }
            }
            generateReport(new ArrayList(outputMap.values()));
            long e = System.currentTimeMillis();
            log.info(logPrefix, "Complete", ", time_taken_millis=" + (e - s));
        } catch (IOException e) {
            log.error(logPrefix, "Error", ", errType=IOException", e);
        } catch (Exception e) {
            log.error(logPrefix, "Error", ", errType=" + e.getMessage(), e);
        }
    }

    private void compare(List<String> realmIds) {
        String logPrefix = "Action=validateList, Status={}, {}";
        log.info(logPrefix, "Start", StringUtils.EMPTY);
        String realmIdsLoggerStr = StringUtils.join(realmIds, ",");

        long s = System.currentTimeMillis();
        Map<String, CompanyData> companyDataMap = getCompanyData(realmIds);
        log.info(logPrefix, "Interim", "companyDataMap=" + companyDataMap.size() + ", realmIdsSize=" + realmIds.size());

        // return if map is empty
        if (MapUtils.isEmpty(companyDataMap)) {
            log.error(logPrefix, "Error", ", errType=CompanyDataMapEmpty");
            return;
        }

        // update error msg if companies not found
        updateErrMsg(realmIds, companyDataMap);

        // compare PSP & AMS accounts
        comparePspAndAms(companyDataMap);
        long e = System.currentTimeMillis();

        log.info(logPrefix, "Complete", ", time_taken_millis=" + (e - s) + ", realmIds=" + realmIdsLoggerStr);
    }

    private void updateErrMsg(List<String> realmIds, Map<String, CompanyData> companyDataMap) {
        if (companyDataMap.size() != realmIds.size()) {
            log.error("Action=updateErrMsg, Status={}, errType=CompanyDataMapSizeMismatch", "Error");
            for (String r : realmIds) {
                if (!companyDataMap.containsKey(r)) {
                    updateOutput(EMPTY, r, ERR_MSG, false, "PSP_COMPANY_DATA_NOT_LOADED");
                }
            }
        }
    }

    private void comparePspAndAms(Map<String, CompanyData> companyDataMap) {

        String logPrefix = "Action=comparePspAndAms, Status={}, realmId={}, {}";
        for (HashMap.Entry<String, CompanyData> e : companyDataMap.entrySet()) {
            String realmId = e.getKey();
            CompanyData c = e.getValue();
            String errMsg = EMPTY;
            log.info(logPrefix, "Start", realmId, StringUtils.EMPTY);
            try {
                PaymentsAccount pa = getPaymentsAccount(realmId);
                boolean isEqual = compare(c.getPsid(), realmId, pa, c);
                updateOutput(c.getPsid(), realmId, CONSOLIDATED, isEqual);
            } catch (AccountServicesException e1) {
                if (e1.getHttpServiceResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    log.error(logPrefix, "Error", realmId, ", errType=PaymentsAccountNotFound, errMsg=" + e1.getHttpServiceResponse().toDetailedString(), e1);
                    errMsg = PAYMENTS_ACCOUNT_NOT_FOUND;
                } else {
                    log.error(logPrefix, "Error", realmId, ", errType=AccountServicesException, errMsg=" + e1.getHttpServiceResponse().toDetailedString(), e1);
                    errMsg = e1.getHttpServiceResponse().toDetailedString();
                }
            } catch (Exception e1) {
                log.error(logPrefix, "Error", realmId, "errType=Exception", e1);
                errMsg = e1.getMessage();
            }

            if (!isEmpty(errMsg)) {
                updateOutput(c.getPsid(), realmId, ERR_MSG, false, errMsg);
            }
        }
    }

    private boolean compare(String psid, String realmId, PaymentsAccount pa, CompanyData c) {
        boolean isEqual;
        String logPrefix = "Action=compare, Status={}, realmId={}{}";
        log.info(logPrefix, "Start", realmId, StringUtils.EMPTY);

        boolean isBusinessInfoEqual = isBusinessInfoEqual(psid, realmId, c, pa);
        boolean isBusinessOwnerEqual = isBusinessOwnerEqual(psid, realmId, c, pa);
        boolean isBankEqual = isBankEqual(psid, realmId, c, pa);

        isEqual = isBusinessInfoEqual && isBusinessOwnerEqual && isBankEqual;

        log.info(logPrefix, "Complete", realmId, ", isEqual=" + isEqual);
        return isEqual;
    }

    private boolean isBusinessInfoEqual(String psid, String realmId, CompanyData c, PaymentsAccount pa) {
        boolean isEqual;
        PrimaryBusiness businessInfo = getBusinessInfoFromPaymentsAccount(pa);
        if (isNull(businessInfo)) {
            return false;
        }

        Address amsAddress = copyAddress(realmId, businessInfo.getAddress());
        String decryptedEin = deterministicDecrypt(Company.FedTaxIdKeyName, c.getEin());

        boolean isLegalNameEqual = isFieldEqual(psid, realmId, LEGALNAME, c.getLegalName(), businessInfo.getLegalName(), true);
        boolean isDbaNameEqual = isFieldEqual(psid, realmId, DBANAME, c.getDbaName(), businessInfo.getBusinessName(), true);
        boolean isEinEqual = areNumbersEqual(psid, realmId, EIN, decryptedEin, businessInfo.getEin());
        boolean isOwnerEmailEqual = isFieldEqual(psid, realmId, OWNEREMAIL, c.getOwnerEmail(), businessInfo.getEmail(), true);
        boolean isComplianceAddressEqual = isAddressEqual(psid, realmId, COMPLIANCEADDRESS, amsAddress, c.getComplianceAddress());
        boolean isLegalAddressEqual = isAddressEqual(psid, realmId, LEGALADDRESS, amsAddress, c.getLegalAddress());
        boolean isMailingAddressEqual = isAddressEqual(psid, realmId, MAILINGADDRESS, amsAddress, c.getMailingAddress());

        isEqual = isLegalNameEqual && isDbaNameEqual && isEinEqual && isOwnerEmailEqual &&
                isComplianceAddressEqual && isLegalAddressEqual && isMailingAddressEqual;

        return isEqual;
    }

    private boolean isBusinessOwnerEqual(String psid, String realmId, CompanyData c, PaymentsAccount pa) {
        boolean isEqual = false;
        BusinessOwner owner = accountServiceTranslator.getPaymentsPrimaryPrincipal(pa);
        if (isNull(owner)) {
            log.error("Action=isBusinessOwnerEqual, status=Error, msg=Owner_Null, psid={}, realmId={}", psid, realmId);
            return isEqual;
        }

        boolean isFirstNameEqual = isFieldEqual(psid, realmId, OWNERFIRSTNAME, c.getOwnerFirstName(), owner.getFirstName(), true);
        boolean isLastNameEqual = isFieldEqual(psid, realmId, OWNERLASTNAME, c.getOwnerLastName(), owner.getLastName(), true);
        boolean isPhoneEqual = areNumbersEqual(psid, realmId, OWNERPHONE, c.getOwnerPhone(), owner.getPhone());

        String decryptedSsn = deterministicDecrypt(Contact.SSOKeyName, c.getSsn());
        boolean isSsnEqual = areNumbersEqual(psid, realmId, SSN, decryptedSsn, owner.getSsn());

        Address amsAddress = copyAddress(realmId, owner.getAddress());
        boolean isOwnerAddressEqual = isAddressEqual(psid, realmId, OWNERADDRESS, amsAddress, c.getOwnerAddress());

        SpcfCalendar decryptedDob = probabilisticDecryptDate(Contact.DOBKeyName, c.getOwnerDob());
        boolean isDobEqual = compareDate(psid, realmId, OWNERDOB, decryptedDob, owner.getDateOfBirth());
        if (verboseSensitiveInfo) {
            log.info("PSP Dob: {}, AMS Dob: {}, isEqual={}", decryptedDob, owner.getDateOfBirth(), isDobEqual);
        }

        isEqual = isFirstNameEqual && isLastNameEqual && isPhoneEqual && isSsnEqual && isOwnerAddressEqual && isDobEqual;

        return isEqual;
    }

    /**
     * AMS & PSP Bank accounts are equal if below fields match & the two Bank accounts are Active/Verified:
     * Account number, routing number, account type, bank name
     */
    private boolean isBankEqual(String psid, String realmId, CompanyData c, PaymentsAccount pa) {
        boolean isEqual = false;
        com.intuit.payments.cdm.v2.client.BankAccount amsBankAccount = accountServiceTranslator.getPaymentBank(pa);
        if (isNull(amsBankAccount)) {
            log.error("Action=isBankEqual, status=Error, msg=AmsBankAccount_Null, psid={}, realmId={}", psid, realmId);
            return isEqual;
        }

        if (isBlank(c.getBankAccountNumber())) {
            log.error("Action=isBankEqual, status=Error, msg=CompanyBankAccount_Null, psid={}, realmId={}", psid, realmId);
            return isEqual;
        }

        String decryptedBankAccountNumber = deterministicDecrypt(BankAccount.AccountNumberKeyName, c.getBankAccountNumber());
        boolean isBankAccountNumberEqual = areNumbersEqual(psid, realmId, BANKACCOUNTNUMBER, decryptedBankAccountNumber, amsBankAccount.getAccountNumber());
        boolean isBankRoutingNumberEqual = areNumbersEqual(psid, realmId, BANKROUTINGNUMBER, c.getBankRoutingNumber(), amsBankAccount.getRoutingNumber());
        boolean isBankAccountTypeEqual = c.getBankAccountType() == accountServiceTranslator.getBankAccountType(amsBankAccount);
        boolean isBankNameEqual = PSPStringUtils.isEqual(c.getBankName(), amsBankAccount.getName());
        boolean isBankStatusEqual = amsBankAccount.getVbdStatus().equals(VbdStatusEnum.VERIFIED);
        isEqual = isBankAccountNumberEqual && isBankRoutingNumberEqual && isBankAccountTypeEqual && isBankNameEqual && isBankStatusEqual;

        // updateOutput already completed in 'areNumbersEqual' for account & routing numbers
        updateOutput(psid, realmId, BANKACCOUNTTYPE, isBankAccountTypeEqual);
        updateOutput(psid, realmId, BANKNAME, isBankNameEqual);
        updateOutput(psid, realmId, BANKSTATUS, isBankStatusEqual);

        validateAccountNumber(psid, realmId, decryptedBankAccountNumber);

        return isEqual;
    }

    private void validateAccountNumber(String psid, String realmId, String accountNumber) {
        if (!accountNumber.matches(NUMBER_SPECIAL_CHAR_REGEX)) {
            updateOutput(psid, realmId, BANKACCOUNTSPECIALCHAR, true);
            if (verboseSensitiveInfo) {
                log.info("psid={}, realmId={}, accountNumber={}", psid, realmId, accountNumber);
            }
        } else {
            updateOutput(psid, realmId, BANKACCOUNTSPECIALCHAR, false);
        }
    }

    private boolean isAddressEqual(String psid, String realmId, CompanyDataField f, Address a, Address b) {
        boolean isAddressLine1Equal = equalsIgnoreCase(a.getAddressLine1(), b.getAddressLine1());
        boolean isAddressLine2Equal = equalsIgnoreCase(a.getAddressLine2(), b.getAddressLine2());
        boolean isCityEqual = equalsIgnoreCase(a.getCity(), b.getCity());
        boolean isStateEqual = equalsIgnoreCase(a.getState(), b.getState());
        boolean isCountryEqual = (
                // either both countries are same or one is 'US' & other is 'USA'
                (equalsIgnoreCase(a.getCountry(), b.getCountry())) ||
                        ((US.equals(a.getCountry()) || USA.equals(a.getCountry()))
                                && (US.equals(b.getCountry()) || USA.equals(b.getCountry())))
        );
        boolean isZipEqual = equalsIgnoreCase(a.getZipCode(), b.getZipCode());
        boolean isZipExtnEqual = equalsIgnoreCase(a.getZipCodeExtension(), b.getZipCodeExtension());

        if (!isAddressLine1Equal) {
            updateOutput(psid, realmId, f, "AddressLine1", false);
        }
        if (!isAddressLine2Equal) {
            updateOutput(psid, realmId, f, "AddressLine2", false);
        }
        if (!isCityEqual) {
            updateOutput(psid, realmId, f, "City", false);
        }
        if (!isStateEqual) {
            updateOutput(psid, realmId, f, "State", false);
        }
        if (!isCountryEqual) {
            updateOutput(psid, realmId, f, "Country", false);
        }
        if (!isZipEqual) {
            updateOutput(psid, realmId, f, "Zip", false);
        }
        if (!isZipExtnEqual) {
            updateOutput(psid, realmId, f, "ZipExtn", false);
        }

        boolean isEqual = isAddressLine1Equal && isAddressLine2Equal && isCityEqual && isStateEqual /*&& isCountryEqual*/
                && isZipEqual && isZipExtnEqual;

        updateOutput(psid, realmId, f, isEqual);
        return isEqual;
    }

    private boolean compareDate(String psid, String realmId, CompanyDataField f, SpcfCalendar d1, Date d2) {
        if (isNull(d1)) {
            updateOutput(psid, realmId, f, false);
            updateOutput(psid, realmId, ERR_MSG, false, f + " is null");
            return false;
        }
        SpcfCalendar d2Calendar = createInstance(d2.getTime());
        // sample log:
        // d1: 1990/11/11 08:00:00.0, d2: 1990-11-11T00:00:00.000-0800, isEqual=true
        boolean isEqual = d1.compareTo(d2Calendar) == 0;
        updateOutput(psid, realmId, f, isEqual);
        return isEqual;
    }

    private Address copyAddress(String realmId, PhysicalAddress a) {
        if (isBlank(a.getStreetAddress())) {
            log.error("realmId=" + realmId + ", StreetAddress is blank");
            return null;
        }
        Address b = new Address();
        String[] lines = a.getStreetAddress().split(ADDRESS_SPLIT_REGEX);
        if (lines.length > 0) {
            b.setAddressLine1(lines[0]);
        }

        if (lines.length > 1) {
            b.setAddressLine2(lines[1]);
        }

        b.setCity(a.getCity());
        b.setState(a.getRegion());
        b.setCountry(a.getCountry());
        Matcher matcher = ZIP_CODE_PATTERN.matcher(a.getPostalCode());
        if (matcher.matches()) {
            b.setZipCode(matcher.group(1));
            b.setZipCodeExtension(matcher.group(4));
        }
        return b;
    }

    private boolean isFieldEqual(String psid, String realmId, CompanyDataField f, String pspField, String amsField) {
        return isFieldEqual(psid, realmId, f, pspField, amsField, false);
    }

    private boolean isFieldEqual(String psid, String realmId, CompanyDataField f, String pspField, String amsField,
                                 boolean ignoreCase) {
        boolean isEqual = ignoreCase ? equalsIgnoreCase(pspField, amsField) : StringUtils.equals(pspField, amsField);
        updateOutput(psid, realmId, f, isEqual);
        return isEqual;
    }

    private boolean areNumbersEqual(String psid, String realmId, CompanyDataField f, String pspField, String amsField) {
        boolean isEqual = StringUtils.equals(extractDigitsOnly(pspField), extractDigitsOnly(amsField));
        updateOutput(psid, realmId, f, isEqual);
        return isEqual;
    }

    private CharSequence extractDigitsOnly(String value) {
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll(ONLY_DIGITS, StringUtils.EMPTY);
    }

    private void updateOutput(String psid, String realmId, CompanyDataField f, boolean isEqual) {
        updateOutput(psid, realmId, f, isEqual, EMPTY);
    }

    private void updateOutput(String psid, String realmId, CompanyDataField f, String subField, boolean isEqual) {
        String msg = f + ": " + subField + "=" + (isEqual ? EQUAl : NOT_EQUAL);
        updateOutput(psid, realmId, ERR_MSG, isEqual, msg);
    }

    private void updateOutput(String psid, String realmId, CompanyDataField f, boolean isEqual, String msg) {
        log.info("psid={}, realmId={}, fieldName={}, isEqual={}", psid, realmId, f, isEqual);
        if (!outputMap.containsKey(realmId)) {
            CompanyData c = new CompanyData();
            c.setPsid(psid);
            c.setRealmId(realmId);
            outputMap.put(realmId, c);
        }
        CompanyData c = outputMap.get(realmId);

        if (!isEmpty(msg)) {
            String oldMsg = isBlank(c.getErrMsg()) ? EMPTY : c.getErrMsg();
            c.setErrMsg(oldMsg + msg + "; ");
            return;
        }
        String output = isEqual ? EQUAl : NOT_EQUAL;

        switch (f) {
            case PSID:
                c.setPsid(output);
                break;
            case LEGALNAME:
                c.setLegalName(output);
                break;
            case DBANAME:
                c.setDbaName(output);
                break;
            case EIN:
                c.setEin(output);
                break;
            case LEGALADDRESS:
                c.setLegalAddressStr(output);
                break;
            case MAILINGADDRESS:
                c.setMailingAddressStr(output);
                break;
            case COMPLIANCEADDRESS:
                c.setComplianceAddressStr(output);
                break;
            case OWNERADDRESS:
                c.setOwnerAddressStr(output);
                break;
            case OWNERFIRSTNAME:
                c.setOwnerFirstName(output);
                break;
            case OWNERLASTNAME:
                c.setOwnerLastName(output);
                break;
            case OWNEREMAIL:
                c.setOwnerEmail(output);
                break;
            case OWNERPHONE:
                c.setOwnerPhone(output);
                break;
            case SSN:
                c.setSsn(output);
                break;
            case OWNERDOB:
                c.setOwnerDob(output);
                break;
            case BANKACCOUNTNUMBER:
                c.setBankAccountNumber(output);
                break;
            case BANKROUTINGNUMBER:
                c.setBankRoutingNumber(output);
                break;
            case BANKACCOUNTTYPE:
                c.setBankAccountTypeStr(output);
                break;
            case BANKNAME:
                c.setBankName(output);
                break;
            case BANKSTATUS:
                c.setBankStatus(output);
                break;
            case BANKACCOUNTSPECIALCHAR:
                c.setBankAccountSpecial(output);
                break;
            case CONSOLIDATED:
                c.setConsolidated(output);
                break;
            default:
                throw new IllegalStateException("Unexpected CompanyDataField, fieldName=" + f + ", realmId=" + realmId);
        }
    }

    private PaymentsAccount getPaymentsAccount(String realmId) {
        String logPrefix = "Action=getPaymentsAccount, Status={}, realmId={}{}";
        log.info(logPrefix, "Start", realmId, StringUtils.EMPTY);
        PaymentsAccount pa = accountServiceGateway.getPaymentsAccount(realmId);
        log.info(logPrefix, "Complete", realmId, StringUtils.EMPTY);
        return pa;
    }

    private PrimaryBusiness getBusinessInfoFromPaymentsAccount(PaymentsAccount paymentsAccount) {
        if (isNull(paymentsAccount) || isNull(paymentsAccount.getBusinessInfo())) {
            return null;
        }
        return paymentsAccount.getBusinessInfo();
    }

    /**
     * @param realmIds list of realm Ids
     * @return Map of realmId & CompanyData
     */
    private Map<String, CompanyData> getCompanyData(List<String> realmIds) {
        String logPrefix = "Action=getCompanyData, Status={}, {}";
        String realmIdsLoggerStr = StringUtils.join(realmIds, ",");
        log.info(logPrefix, "Start", ", realmIds=" + realmIdsLoggerStr);

        try {
            Application.beginUnitOfWork();
            String[] paramNames = {"REALM_LIST"};
            Object[] paramValues = {realmIds};
            List<Object[]> results = Application.executeHQLQuery(COMPANY_HQL, paramNames, paramValues);
            log.info(logPrefix, "QueryComplete, resultsCount=" + results.size() + ", realmIds=" + realmIdsLoggerStr);

            Map<String, CompanyData> companyDataMap = new HashMap<>();
            for (Object[] r : results) {
                companyDataMap.put((String) r[1], readCompanyData(r));
            }
            log.info(logPrefix, "Complete", ", realmCount=" + realmIds.size() + ", mapSize=" + companyDataMap.size());
            return companyDataMap;

        } catch (Exception e) {
            log.error(logPrefix, "Error", ", realmIds=" + realmIdsLoggerStr + ", batchSize=" + realmIds.size() + ", errType=" + e.getMessage(), e);
            return MapUtils.EMPTY_MAP;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private CompanyData readCompanyData(Object[] r) {
        if (verboseSensitiveInfo) {
            log.info(ArrayUtils.toString(r));
        }
        CompanyData c = new CompanyData();
        c.setPsid((String) r[0]);
        c.setRealmId((String) r[1]);
        c.setLegalName((String) r[2]);
        c.setDbaName((String) r[3]);
        c.setEin((String) r[4]);
        c.setLegalAddress((Address) r[5]);
        c.setMailingAddress((Address) r[6]);
        c.setComplianceAddress((Address) r[7]);
        c.setOwnerAddress((Address) r[8]);
        c.setOwnerFirstName((String) r[9]);
        c.setOwnerMiddleName((String) r[10]);
        c.setOwnerLastName((String) r[11]);
        c.setOwnerEmail((String) r[12]);
        c.setOwnerPhone((String) r[13]);
        c.setOwnerDob((String) r[14]);
        c.setBankAccountNumber((String) r[15]);
        c.setBankRoutingNumber((String) r[16]);
        c.setBankAccountType((BankAccountType) r[17]);
        c.setBankName((String) r[18]);
        c.setSsn((String) r[19]);
        return c;
    }

    @Getter
    @Setter
    @ToString
    public static class CompanyData {
        private String psid;
        private String realmId;
        private String errMsg;

        private String legalName;
        private String dbaName;
        private String ein;
        private String ssn;

        private Address legalAddress;
        private Address mailingAddress;
        private Address complianceAddress;
        private Address ownerAddress;

        private String legalAddressStr;
        private String mailingAddressStr;
        private String complianceAddressStr;
        private String ownerAddressStr;

        private String ownerFirstName;
        private String ownerMiddleName;
        private String ownerLastName;
        private String ownerEmail;
        private String ownerPhone;
        private String ownerDob;

        private String bankAccountNumber;
        private String bankRoutingNumber;
        private BankAccountType bankAccountType;
        private String bankAccountTypeStr;
        private String bankName;
        private String bankStatus;
        private String bankAccountSpecial;

        private String consolidated;
    }

    public enum CompanyDataField {
        PSID, REALMID, ERR_MSG,
        LEGALNAME, DBANAME, EIN,
        LEGALADDRESS, MAILINGADDRESS, COMPLIANCEADDRESS, OWNERADDRESS,
        OWNERFIRSTNAME, OWNERLASTNAME, OWNEREMAIL, OWNERPHONE, OWNERDOB, SSN,
        BANKACCOUNTNUMBER, BANKROUTINGNUMBER, BANKACCOUNTTYPE, BANKNAME, BANKSTATUS, BANKACCOUNTSPECIALCHAR,
        CONSOLIDATED;
    }

    private void generateReport(List<CompanyData> outputReport) {
        String logPrefix = "Job=AmsValidation, Action=generateReport, Status={}";
        log.info(logPrefix, "Start");
        Writer writer = null;
        try {
            writer = new FileWriter(outputFile);
            final StatefulBeanToCsvBuilder<AddressReportModel> builder =
                    new StatefulBeanToCsvBuilder(writer);
            StatefulBeanToCsv beanWriter =
                    builder.withApplyQuotesToAll(false).build();

            beanWriter.write(outputReport);
            log.info(logPrefix, "Completed" + " Processed=" + (outputReport.size()));
        } catch (Exception e) {
            log.error(logPrefix, "Error" + " errMsg={}" + e.getMessage(), e);
        } finally {
            try {
                if (!isNull(writer)) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error(logPrefix, "Error2" + " errMsg={}" + e.getMessage(), e);
            }
        }
    }
}
