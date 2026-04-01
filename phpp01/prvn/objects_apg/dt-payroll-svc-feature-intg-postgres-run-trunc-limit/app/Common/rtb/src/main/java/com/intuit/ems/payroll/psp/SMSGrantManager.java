package com.intuit.ems.payroll.psp;

import au.com.bytecode.opencsv.CSVReader;
import com.intuit.platform.integration.ius.common.types.FeatureSetObject;
import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.platform.integration.ius.common.types.OptionalFeature;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateTRONGrantProcessor;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * This class is intended to fix the missing Payroll Grant issue in the Realm and also attach the DIRECT_DEPOSIT feature set object for all SMS onboarded companies
 *
 */
public class SMSGrantManager {

    private RateLimiter rateLimiter;
    private AtomicInteger totalCount;
    private AtomicInteger successCount;
    private AtomicInteger failureCount;
    private AtomicInteger companyNotFoundCount;
    private RealmManager realmManager;

    public SMSGrantManager() {
        init();
    }

    public static void main(String[] args) throws Exception {
        Triple<GrantProcessorMode, Mode, String> triple = parseCommandLine(args);

        SMSGrantManager smsGrantManager = new SMSGrantManager();
        smsGrantManager.processGrants(triple.getLeft(), triple.getMiddle(), triple.getRight());
    }

    private static Triple<GrantProcessorMode, Mode, String> parseCommandLine(String[] args) throws ParseException {
        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine = commandLineParser.parse(getOptions(), args);

        MutableTriple<GrantProcessorMode, Mode, String> triple = new MutableTriple<>();

        // Extract Processor Mode
        String processorMode = "VALIDATE";
        if(commandLine.hasOption("p")) {
            processorMode = commandLine.getOptionValue("p");
        }

        GrantProcessorMode grantProcessorMode = GrantProcessorMode.valueOf(processorMode);
        if(Objects.nonNull(grantProcessorMode)) {
            triple.setLeft(grantProcessorMode);
        } else {
            triple.setLeft(GrantProcessorMode.VALIDATE);
        }

        // Extract File Path & Mode
        String filePath = null;
        if(commandLine.hasOption("f")) {
            filePath = commandLine.getOptionValue("f");
        }

        if(Objects.nonNull(filePath)) {
            triple.setMiddle(Mode.FILE);
            triple.setRight(filePath);
        } else {
            triple.setMiddle(Mode.DATABASE);
        }

        return triple;
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption("p","processor", true, "Grant Processor Mode");
        options.addOption("f","file", true, "CSV File containing the realm ids");
        return options;
    }

    private void init() {
        rateLimiter = getRateLimiter();
        totalCount = new AtomicInteger();
        successCount = new AtomicInteger();
        failureCount = new AtomicInteger();
        companyNotFoundCount = new AtomicInteger();
        realmManager = new RealmManager();
    }

    private void processGrants(GrantProcessorMode grantProcessorMode, Mode mode, String filePath) throws Exception {
        switch (grantProcessorMode) {
            case UPDATE:
                attachPayrollGrantsToSMSCompanies(mode, filePath);
                break;
            case VALIDATE:
            default:
                validatePayrollGrantsAttachedToSMSCompanies();
                break;
        }
    }

    private void attachPayrollGrantsToSMSCompanies(Mode runMode, String filePath) throws Exception {
        CompanyExtractor companyExtractor = getCompanyExtractor(runMode, filePath);
        try {
            companyExtractor.open();
            Company company = null;
            while ((company = companyExtractor.next())!=null) {
                totalCount.incrementAndGet();
                if(Objects.isNull(company.getSourceCompanyId())) {
                    companyNotFoundCount.incrementAndGet();
                    continue;
                }
                attachPayrollGrants(company);
            }
            printStats();
        } finally {
            companyExtractor.close();
        }
    }

    public void validatePayrollGrantsAttachedToSMSCompanies() throws Exception {
        CompanyExtractor companyExtractor = getCompanyExtractor(Mode.DATABASE, null);
        try {
            companyExtractor.open();
            Company company = null;
            while ((company = companyExtractor.next())!=null) {
                totalCount.incrementAndGet();
                if(Objects.isNull(company.getSourceCompanyId())) {
                    companyNotFoundCount.incrementAndGet();
                    continue;
                }
                if(Objects.isNull(company.getIAMRealmId())) {
                    companyNotFoundCount.incrementAndGet();
                    continue;
                }
                validateGrantAndDirectDepositFeature(company);
            }
            printStats();
        } finally {
            companyExtractor.close();
        }
    }

    private CompanyExtractor getCompanyExtractor(Mode runMode, String filePath) {
        CompanyExtractor companyExtractor = null;
        switch (runMode) {
            case FILE:
                companyExtractor = new CSVCompanyExtractor(filePath);
                break;
            case DATABASE:
            default:
                companyExtractor = new DatabaseCompanyExtractor();
                break;
        }
        return companyExtractor;
    }

    private void attachPayrollGrants(Company company) {
        Supplier<Grant> grantSupplier = RateLimiter.decorateSupplier(rateLimiter, () -> {
            log(String.format("Attach Payroll Grant started for PSID=%s, realmId=%s", company.getSourceCompanyId(), company.getIAMRealmId()));
            AddOrUpdateTRONGrantProcessor addOrUpdateTRONGrantProcessor = new AddOrUpdateTRONGrantProcessor(company, true);
            ProcessResult<Grant> processResult = addOrUpdateTRONGrantProcessor.execute();
            if(!processResult.isSuccess()) {
                return null;
            }
            return processResult.getResult();
        });

        Grant grant = grantSupplier.get();

        if(Objects.nonNull(grant)) {
            successCount.incrementAndGet();
            log(String.format("Payroll Grant add or update success for realmId=%s", company.getIAMRealmId()));
        } else {
            failureCount.incrementAndGet();
            log(String.format("Payroll Grant add or update failed for realmId=%s", company.getIAMRealmId()));
        }
    }

    private void validateGrantAndDirectDepositFeature(Company company) {
        Supplier<Grant> grantSupplier = RateLimiter.decorateSupplier(rateLimiter, () -> {
            log(String.format("Payroll Grant validation started for PSID=%s, realmId=%s", company.getSourceCompanyId(), company.getIAMRealmId()));
            try {
                return realmManager.findPayrollGrant(company.getIAMRealmId());
            } catch (Exception e) {
                //Ignore the error
                return null;
            }
        });

        Grant grant = grantSupplier.get();
        // Return false, if the EWS Payroll Grant is missing
        if(Objects.isNull(grant)) {
            failureCount.incrementAndGet();
            log(String.format("Payroll Grant is missing grant for realmId=%s", company.getIAMRealmId()));
            return;
        }

        FeatureSetObject featureSetObject = grant.getFeatureSetObj();
        // Return false, if the FeatureSetObject is missing
        if(Objects.isNull(featureSetObject)) {
            failureCount.incrementAndGet();
            log(String.format("Payroll Grant is missing feature set for realmId=%s", company.getIAMRealmId()));
            return;
        }

        List<OptionalFeature> optionalFeatures = featureSetObject.getOptionalFeatures();
        // Return false, if the OptionalFeature is missing
        if(Objects.isNull(optionalFeatures) || optionalFeatures.isEmpty()) {
            failureCount.incrementAndGet();
            log(String.format("Payroll Grant is missing optional feature for realmId=%s", company.getIAMRealmId()));
            return;
        }

        boolean directDepositFeatureAvailable = optionalFeatures.stream()
                .anyMatch(optionalFeature -> StringUtils.equals(optionalFeature.getCode(), "DIRECT_DEPOSIT"));

        if(directDepositFeatureAvailable) {
            successCount.incrementAndGet();
            log(String.format("Payroll Grant has direct deposit optional feature for realmId=%s", company.getIAMRealmId()));
        } else {
            failureCount.incrementAndGet();
            log(String.format("Payroll Grant is missing direct deposit optional feature for realmId=%s", company.getIAMRealmId()));
        }
    }

    private RateLimiter getRateLimiter() {
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
        return rateLimiterRegistry.rateLimiter("grants", getRateLimiterConfig());
    }

    private RateLimiterConfig getRateLimiterConfig() {
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(15)
                .limitRefreshPeriod(Duration.ofSeconds(5))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        return rateLimiterConfig;
    }

    private void printStats() {
        log(String.format("Total number of records processsed=%d", totalCount.get()));
        log(String.format("Total number of records successfully processed=%d", successCount.get()));
        log(String.format("Total number of records failed=%d", failureCount.get()));
        log(String.format("Total number of records where company not found=%d", companyNotFoundCount.get()));
    }

    private void log(String message) {
        System.out.println(String.format("%s %s [%s] - %s", Instant.now().toString(), "INFO", Thread.currentThread().getName(), message));
    }

    interface CompanyExtractor extends AutoCloseable {
        void open() throws IOException;
        Company next() throws IOException;
    }

    class DatabaseCompanyExtractor implements CompanyExtractor {

        private ScrollableResults scrollableResults;

        public void open() {
            Application.beginUnitOfWork();
            scrollableResults = findSMSCompanies();
        }

        public Company next() {
            boolean recordExists = scrollableResults.next();
            if(recordExists) {
                return (Company) scrollableResults.get(0);
            }
            return null;
        }

        public void close() {
            if(Objects.nonNull(scrollableResults)) {
                scrollableResults.close();
            }

            Application.rollbackUnitOfWork();
        }

        private ScrollableResults findSMSCompanies() {
            String hqlQuery = "select company from com.intuit.sbd.payroll.psp.domain.Company company, com.intuit.sbd.payroll.psp.domain.CompanyService companyService " +
                    "where companyService.Company = company and company.OIIFlag like '___11%' and companyService.Service='DirectDeposit' order by companyService.ModifiedDate";
            ScrollableResults scrollableResults = scrollableResultsByNamedQuery(hqlQuery);
            return scrollableResults;
        }

        public ScrollableResults scrollableResultsByNamedQuery(String queryName) {
            Boolean manageTransaction = !Application.hasActiveTransaction();
            try {
                if (manageTransaction) Application.beginUnitOfWork();
                Query queryObject = getHibernateHQLQuery(queryName, true);
                return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
            } finally {
                if (manageTransaction) Application.rollbackUnitOfWork();
            }
        }

        private Query getHibernateHQLQuery(String queryName, Boolean readOnly) {
            Query queryObject = Application.getHibernateSession().createQuery(queryName);
            queryObject.setReadOnly(readOnly);
            return queryObject;
        }

    }

    class CSVCompanyExtractor implements CompanyExtractor {

        private String filePath;
        private CSVReader csvReader;

        public CSVCompanyExtractor(String filePath) {
            this.filePath = filePath;
        }

        public void open() throws IOException {
            Application.beginUnitOfWork();
            Path csvPath = Paths.get(filePath);
            BufferedReader bufferedReader = Files.newBufferedReader(csvPath);
            csvReader = new CSVReader(bufferedReader);
            String[] headers = csvReader.readNext(); //Skip the headers
        }

        public Company next() throws IOException {
            String[] realms = csvReader.readNext();
            if(Objects.isNull(realms)) {
                return null;
            }
            String realmId = StringUtils.trimToEmpty(realms[0]);
            if(StringUtils.isEmpty(realmId)) {
                return new Company();
            }
            Company company = findCompanyByRealmId(realmId);
            if(Objects.isNull(company)) {
                return new Company();
            }
            return company;
        }

        public void close() throws IOException {
            if(Objects.nonNull(csvReader)) {
                csvReader.close();
            }
            Application.rollbackUnitOfWork();
        }

        private Company findCompanyByRealmId(String realmId) {
            DomainEntitySet<Company> companies = Company.findAllCompaniesByRealmId(realmId);
            if(companies.isEmpty()) {
                log(String.format("Company not found for realmId=%s", realmId));
                return null;
            }
            companies = companies.sort(Company.IAMRealmId());
            return companies.getFirst();
        }
    }

    enum Mode {
        DATABASE,
        FILE
    }

    enum GrantProcessorMode {
        UPDATE,
        VALIDATE
    }

}
