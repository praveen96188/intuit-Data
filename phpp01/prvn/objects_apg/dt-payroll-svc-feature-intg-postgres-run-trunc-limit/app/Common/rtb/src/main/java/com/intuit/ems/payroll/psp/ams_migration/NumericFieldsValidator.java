package com.intuit.ems.payroll.psp.ams_migration;

import com.google.common.collect.Lists;
import com.intuit.ems.payroll.psp.model.AddressReportModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Contact;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static com.intuit.ems.payroll.psp.ams_migration.NumericFieldsValidator.CompanyDataField.*;
import static com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils.deterministicDecrypt;
import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.*;

@Slf4j
public class NumericFieldsValidator {

    private final String fileName;
    private Map<String, CompanyData> outputMap;
    private final int batchSize;
    private final String outputFile;

    private static final String NUMBER_HAS_SPECIAL_CHAR_REGEX = "^[0-9]+$";
    private static final String EQUAl = "Y";
    private static final String NOT_EQUAL = "N";

    private static final String COMPANY_HQL =
            "Select C.SourceCompanyId, C.IAMRealmId, BA.AccountNumberEnc, BA.RoutingNumber, BA.AccountTypeCd," +
                    "             CT.SocialSecurityNumberEnc, IND.Phone, C.FedTaxIdEnc, EC.IsPrimary, EC.AssetTypeCd" +
                    "             from" +
                    "             com.intuit.sbd.payroll.psp.domain.Company as C, " +
                    "             com.intuit.sbd.payroll.psp.domain.SMSMigration as S, " +
                    "             com.intuit.sbd.payroll.psp.domain.Contact as CT, " +
                    "             com.intuit.sbd.payroll.psp.domain.Individual as IND, " +
                    "             com.intuit.sbd.payroll.psp.domain.CompanyBankAccount as CBA, " +
                    "             com.intuit.sbd.payroll.psp.domain.BankAccount as BA, " +
                    "             com.intuit.sbd.payroll.psp.domain.CompanyService as CS, " +
                    "             com.intuit.sbd.payroll.psp.domain.Entitlement as E, " +
                    "             com.intuit.sbd.payroll.psp.domain.EntitlementCode as EC, " +
                    "             com.intuit.sbd.payroll.psp.domain.EntitlementUnit as EU " +

                    // merge with SMSMigration Table
                    "             where S.Company = C " +
                    "             and S.MigrationStatus = 'DataCollectionComplete' and SUBSTR(C.OIIFlag, 5, 1) = 0" +

                    // merge with DD Company Service table
                    "             and C = CS.Company " +
                    "             and CS.Service='DirectDeposit' and CS.StatusCd IN ('ActiveCurrent', 'PendingFirstPayroll') " +

                    // merge with Entitlement & Entitlement_Unit tables
                    "             and C = EU.Company " +
                    "             and E = EU.Entitlement and EC = E.EntitlementCode " +
                    "             and EC.IsPrimary = true " +
                    "             and E.EntitlementState = 'Enabled' and EU.EntitlementUnitStatus IN " +
                    "                 ('Activated', 'PendingActivation', 'PendingReactivation') " +

                    // merge with Contact & Individual tables
                    "             and C = CT.Company " +
                    "             and IND = CT AND CT.ContactRoleCd='PrimaryPrincipal' " +

                    // merge with BankAccount & CompanyBankAccount tables
                    "             and C=CBA.Company and BA = CBA.BankAccount " +
                    "             and CBA.StatusCd='Active' and C.IAMRealmId in (:REALM_LIST)";

    private NumericFieldsValidator(String fileName, int batchSize, String outputFile) {
        this.fileName = fileName;
        this.batchSize = batchSize;
        this.outputFile = outputFile;
        this.outputMap = new HashMap<>();
    }

    public static void main(String[] args) {
        try {
            log.info("Main fn started");
            Application.initialize();
            String fileName = args[0];
            int batchSize = Integer.parseInt(args[1]);
            String outputFile = args[2];
            log.info("Action=mainFn, FileName={}, batchSize={}, outputFile={}", fileName, batchSize, outputFile);
            NumericFieldsValidator NumericFieldsValidator = new NumericFieldsValidator(fileName, batchSize, outputFile);
            NumericFieldsValidator.process();
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

        validate(companyDataMap);

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

    private void validate(Map<String, CompanyData> companyDataMap) {
        for (Map.Entry<String, CompanyData> e : companyDataMap.entrySet()) {
            CompanyData d = e.getValue();

            String realm = d.getRealmId();
            String psid = d.getPsid();
            String accountNumber = deterministicDecrypt(BankAccount.AccountNumberKeyName, d.getBankAccountNumber());
            String routingNumber = d.getBankRoutingNumber();
            String bankType = isNull(d.getBankType()) ? EMPTY : d.getBankType().toString();
            String ssn = deterministicDecrypt(Contact.SSOKeyName, d.getSsn());
            String phone = d.getPhoneNumber();
            String ein = deterministicDecrypt(Company.FedTaxIdKeyName, d.getEin());

            /*log.info("BankAccountNumber={}", accountNumber);
            log.info("BankRoutingNumber={}", routingNumber);
            log.info("BankType={}", bankType);
            log.info("SSN={}", ssn);
            log.info("Phone={}", phone);
            log.info("EIN={}", ein);*/

            if (isEmpty(accountNumber)) {
                updateOutput(psid, realm, ERR_MSG, false, "ACCOUNT_EMPTY");
            }

            if (isEmpty(routingNumber)) {
                updateOutput(psid, realm, ERR_MSG, false, "ROUTING_EMPTY");
            }

            if (isEmpty(bankType)) {
                updateOutput(psid, realm, ERR_MSG, false, "BANKTYPE_EMPTY");
            }

            if (isEmpty(ssn)) {
                updateOutput(psid, realm, ERR_MSG, false, "SSN_EMPTY");
            }

            if (isEmpty(phone)) {
                updateOutput(psid, realm, ERR_MSG, false, "PHONE_EMPTY");
            }

            if (isEmpty(ein)) {
                updateOutput(psid, realm, ERR_MSG, false, "EIN_EMPTY");
            }

            validateSpecialChar(psid, realm, BANKACCOUNTSPECIALCHAR, accountNumber);
            validateSpecialChar(psid, realm, BANKROUTINGSPECIALCHAR, routingNumber);
            validateSpecialChar(psid, realm, SSNSPECIALCHAR, ssn);
            validateSpecialChar(psid, realm, PHONENUMBERSPECIALCHAR, phone);
            validateSpecialChar(psid, realm, EINSPECIALCHAR, ein);
            validateField(psid, realm, BANKTYPE_SAVINGS, bankType, "Savings");
        }
    }

    private void validateSpecialChar(String psid, String realmId, CompanyDataField f, String s) {
        if (isEmpty(s)) {
            return;
        }

        if (!s.matches(NUMBER_HAS_SPECIAL_CHAR_REGEX)) {
            updateOutput(psid, realmId, f, true, EMPTY);
        } else {
            updateOutput(psid, realmId, f, false, EMPTY);
        }
    }

    private void validateField(String psid, String realmId, CompanyDataField f, String a, String b) {
        if ((isEmpty(a) || isEmpty(b)) && !StringUtils.equals(a, b)) {
            return;
        }

        if (a.equalsIgnoreCase(b)) {
            updateOutput(psid, realmId, f, true, EMPTY);
        } else {
            updateOutput(psid, realmId, f, false, EMPTY);
        }
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
            case BANKACCOUNTSPECIALCHAR:
                c.setBankAccountSpecial(output);
                break;
            case BANKROUTINGSPECIALCHAR:
                c.setBankRoutingSpecial(output);
                break;
            case BANKTYPE_SAVINGS:
                c.setBankTypeSavings(output);
                break;
            case SSNSPECIALCHAR:
                c.setSsnSpecial(output);
                break;
            case PHONENUMBERSPECIALCHAR:
                c.setPhoneNumberSpecial(output);
                break;
            case EINSPECIALCHAR:
                c.setEinSpecial(output);
                break;
            case CONSOLIDATED:
                c.setConsolidated(output);
                break;
            default:
                throw new IllegalStateException("Unexpected CompanyDataField, fieldName=" + f + ", realmId=" + realmId);
        }
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
        CompanyData c = new CompanyData();
        c.setPsid((String) r[0]);
        c.setRealmId((String) r[1]);
        c.setBankAccountNumber((String) r[2]);
        c.setBankRoutingNumber((String) r[3]);
        c.setBankType((BankAccountType) r[4]);

        c.setSsn((String) r[5]);
        c.setPhoneNumber((String) r[6]);
        c.setEin((String) r[7]);

        if ("false".equalsIgnoreCase(((Boolean) r[8]).toString())) {
            log.info("IsPrimary={}, assetType={}, realmId={}", r[8], r[9], r[1]);
        }

        return c;
    }

    @Getter
    @Setter
    @ToString
    public static class CompanyData {
        private String psid;
        private String realmId;
        private String errMsg;

        private String bankAccountNumber;
        private String bankRoutingNumber;
        private BankAccountType bankType;
        private String ssn;
        private String phoneNumber;
        private String ein;

        private String bankAccountSpecial;
        private String bankRoutingSpecial;
        private String bankTypeSavings;
        private String ssnSpecial;
        private String phoneNumberSpecial;
        private String einSpecial;

        private String consolidated;
    }

    public enum CompanyDataField {
        PSID, REALMID, ERR_MSG,
        BANKACCOUNTSPECIALCHAR, BANKROUTINGSPECIALCHAR, BANKTYPE_SAVINGS,
        SSNSPECIALCHAR, PHONENUMBERSPECIALCHAR, EINSPECIALCHAR,
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