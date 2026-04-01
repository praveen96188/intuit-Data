package com.intuit.ems.payroll.psp.ams_migration;

import com.google.common.collect.Lists;
import com.intuit.ems.payroll.psp.model.AddressReportModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
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

import static com.intuit.ems.payroll.psp.ams_migration.CompareLegalAndComplianceAddress.CompanyDataField.*;
import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.*;


@Slf4j
public class CompareLegalAndComplianceAddress {

    private final String fileName;
    private Map<String, CompanyData> outputMap;
    private final int batchSize;
    private final String outputFile;
    private static final String ONLY_DIGITS = "[^0-9]";
    private static final String US = "US";
    private static final String USA = "USA";
    private static final String EQUAl = "Y";
    private static final String NOT_EQUAL = "N";

    private static final String COMPANY_HQL =
            "Select C.SourceCompanyId, C.IAMRealmId, C.LegalAddress, C.ComplianceAddress" +
                    "             from" +
                    "             com.intuit.sbd.payroll.psp.domain.Company as C " +
                    "             where C.IAMRealmId in (:REALM_LIST)";

    private CompareLegalAndComplianceAddress(String fileName, int batchSize, String outputFile) {
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
            CompareLegalAndComplianceAddress compare = new CompareLegalAndComplianceAddress(fileName, batchSize, outputFile);
            compare.process();
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
        Map<String, CompanyData> companyDataMap = getCompanyData(realmIds); //TODO
        log.info(logPrefix, "Interim", "companyDataMap=" + companyDataMap.size() + ", realmIdsSize=" + realmIds.size());

        // return if map is empty
        if (MapUtils.isEmpty(companyDataMap)) {
            log.error(logPrefix, "Error", ", errType=CompanyDataMapEmpty");
            return;
        }

        // update error msg if companies not found
        updateErrMsg(realmIds, companyDataMap); //TODO

        // compare PSP & AMS accounts
        compare(companyDataMap); //TODO
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

    private void compare(Map<String, CompanyData> companyDataMap) {

        String logPrefix = "Action=compare, Status={}, realmId={}, {}";
        for (HashMap.Entry<String, CompanyData> e : companyDataMap.entrySet()) {
            String realmId = e.getKey();
            CompanyData c = e.getValue();
            String errMsg = EMPTY;
            log.info(logPrefix, "Start", realmId, StringUtils.EMPTY);
            try {
                boolean isEqual = isAddressEqual(c.getPsid(), realmId, LEGALADDRESS, c.getLegalAddress(), c.getComplianceAddress());
                log.info(logPrefix, "Complete", realmId, ", isEqual=" + isEqual);
                updateOutput(c.getPsid(), realmId, CONSOLIDATED, isEqual);
            } catch (Exception e1) {
                log.error(logPrefix, "Error", realmId, "errType=Exception", e1);
                errMsg = e1.getMessage();
            }

            if (!isEmpty(errMsg)) {
                updateOutput(c.getPsid(), realmId, ERR_MSG, false, errMsg);
            }
        }
    }

    private boolean isAddressEqual(String psid, String realmId, CompanyDataField f, Address a, Address b) {
        boolean isAddressLine1Equal = equalsIgnoreCase(a.getAddressLine1(), b.getAddressLine1());
        boolean isAddressLine2Equal = equalsIgnoreCase(a.getAddressLine2(), b.getAddressLine2());
        boolean isAddressLine3Equal = equalsIgnoreCase(a.getAddressLine3(), b.getAddressLine3());
        boolean isCityEqual = equalsIgnoreCase(a.getCity(), b.getCity());
        boolean isStateEqual = equalsIgnoreCase(a.getState(), b.getState());
        boolean isCountryEqual = (
                // either both countries are same or one is 'US' & other is 'USA'
                (equalsIgnoreCase(a.getCountry(), b.getCountry())) ||
                        ((US.equals(a.getCountry()) || USA.equals(a.getCountry()))
                                && (US.equals(b.getCountry()) || USA.equals(b.getCountry())))
        );
        boolean isZipEqual = areNumbersEqual(a.getZipCode(), b.getZipCode());
        boolean isZipExtnEqual = areNumbersEqual(a.getZipCodeExtension(), b.getZipCodeExtension());

        if (!isAddressLine1Equal) {
            updateOutput(psid, realmId, ADDRESSLINE1, false);
        }
        if (!isAddressLine2Equal) {
            updateOutput(psid, realmId, ADDRESSLINE2, false);
        }
        if (!isAddressLine3Equal) {
            updateOutput(psid, realmId, ADDRESSLINE3, false);
        }
        if (!isCityEqual) {
            updateOutput(psid, realmId, CITY, false);
        }
        if (!isStateEqual) {
            updateOutput(psid, realmId, STATE, false);
        }
        if (!isCountryEqual) {
            updateOutput(psid, realmId, COUNTRY, false);
        }
        if (!isZipEqual) {
            updateOutput(psid, realmId, ZIP, false);
        }
        if (!isZipExtnEqual) {
            updateOutput(psid, realmId, ZIPEXTN, false);
        }

        return isAddressLine1Equal && isAddressLine2Equal && isAddressLine3Equal && isCityEqual && isStateEqual /*&& isCountryEqual*/
                && isZipEqual && isZipExtnEqual;
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

    private void updateOutput(String psid, String realmId, CompanyDataField f, boolean isEqual) {
        updateOutput(psid, realmId, f, isEqual, EMPTY);
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
            case ADDRESSLINE1:
                c.setAddressLine1(output);
                break;
            case ADDRESSLINE2:
                c.setAddressLine2(output);
                break;
            case ADDRESSLINE3:
                c.setAddressLine3(output);
                break;
            case CITY:
                c.setCity(output);
                break;
            case STATE:
                c.setState(output);
                break;
            case COUNTRY:
                c.setCountry(output);
                break;
            case ZIP:
                c.setZip(output);
                break;
            case ZIPEXTN:
                c.setZipExtn(output);
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
        c.setLegalAddress((Address) r[2]);
        c.setComplianceAddress((Address) r[3]);
        return c;
    }

    @Getter
    @Setter
    @ToString
    public static class CompanyData {
        private String psid;
        private String realmId;
        private Address legalAddress;
        private Address complianceAddress;
        private String consolidated;
        private String errMsg;


        private String addressLine1;
        private String addressLine2;
        private String addressLine3;
        private String city;
        private String state;
        private String country;
        private String zip;
        private String zipExtn;
    }

    public enum CompanyDataField {
        PSID, REALMID, ERR_MSG,
        LEGALADDRESS, COMPLIANCEADDRESS,
        ADDRESSLINE1, ADDRESSLINE2, ADDRESSLINE3, CITY, STATE, COUNTRY, ZIP, ZIPEXTN,
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
