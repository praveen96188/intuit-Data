package com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 10, 2008
 * Time: 4:04:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MonthlyGemsUploadBatchProcess {
    private static final SpcfLogger logger;
    private static final String OUTPUT_DIRECTORY;
    private static final String FILE_PREFIX = "PSPGL";
    private static final String FILE_EOL = "\n";
    private static final String DELIMITOR = ",";

    private GemsUploadBatch mUploadBatch;
    private String processingDate;


    public enum Commands {
        file, gen, regen, verify
    }

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(MonthlyGemsUploadBatchProcess.class);

        OUTPUT_DIRECTORY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
    }

    public static void verifyDeployment() {
        logger.info("MonthlyGemsUploadBatchProcess deployed successfully.");
        System.exit(0);
    }

    public static void main(String args[]) {
        String processDate = null;
        String batchId = null;
        Commands commandName;
        try {
            //Validate input params
            if (args.length == 2) {
                commandName = Commands.valueOf(args[0]);

                if (commandName != null && Commands.gen.equals(commandName)) {
                    // date must be formatted as yyyyMM (more precisely, the format must be 20yyMM)
                    if (!args[1].matches(BatchUtils.VALIDYYYYMM)) {
                        throw new RuntimeException("Invalid processing date format" + args[1]);
                    }
                    processDate = args[1];
                } else if (commandName != null && (Commands.file.equals(commandName) || Commands.regen.equals(commandName))) {
                    batchId = args[1];
                } else {
                    throw new RuntimeException("Invalid Command Name. Should be gen/file/regen/verify. ");
                }
            } else if (args.length == 1) {
                commandName = Commands.valueOf(args[0]);

                if (commandName == Commands.verify) {
                    verifyDeployment();
                }
                else {
                    throw new RuntimeException("Invalid Command Name. Should be gen/file/regen/verify. ");
                }
            } else {
                throw new RuntimeException("Wrong number of parameters.");
            }

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.GemsGeneralLedgerBatchJob));
            //Monthly Gems Upload process
            try {
                PayrollServices.beginUnitOfWork();

                new MonthlyGemsUploadBatchProcess().process(commandName.toString(), processDate, batchId);

                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();
                throw t;
            }
        } catch (Throwable t) {
            logger.fatal("Exception in MonthlyGemsUploadBatchProcess.main() ", t);
            System.exit(1);
        }
    }

    /**
     * Process method to execute generate monthly gems data/generate gems monthly file/regenrate  an existing batch
     * functions based on the input command name.
     * @param pCommandName (gen/file/regen)
     * @param pProcessingDate Sting
     * @param pBatchId String
     */
    public void process(String pCommandName, String pProcessingDate, String pBatchId) {
        try {
            Commands commandName = Commands.valueOf(pCommandName);
            processingDate = pProcessingDate;

            switch (commandName) {
                case file:
                    generateMonthlyGemsFile(pBatchId);
                    break;
                case regen:
                    regenerateAnExistingBatch(pBatchId);
                    break;
                default:
                    generateMonthlyGemsData();
                    break;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Function to generate the Gems Monthly Data for the given reporting period
     */
    private void generateMonthlyGemsData() {
        mUploadBatch = new GemsUploadBatch();
        mUploadBatch.setBatchId(generateNewBatchId());
        mUploadBatch.setUploadStatus(GemsUploadBatchStatus.InProcess);
        mUploadBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        mUploadBatch.setBatchType(ReportingFrequency.Monthly);
        mUploadBatch = Application.save(mUploadBatch);

        TreeMap<String, TreeMap<ReportingType, BigDecimal>> toDateBalanceMap = new TreeMap<String, TreeMap<ReportingType, BigDecimal>>();
        TreeMap<String, TreeMap<ReportingType, BigDecimal>> periodBalanceMap = new TreeMap<String, TreeMap<ReportingType, BigDecimal>>();

        Calendar calendar = getCalendar(processingDate, -1);
        String previousPeriod = new SimpleDateFormat("yyyyMM").format(calendar.getTime());

        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalanceList = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ReportingPeriod().equalTo(processingDate));

        if (gemsMonthlyBalanceList.size() > 0) {
            throw new RuntimeException("The period being requested has already been uploaded to GEMS " +
                    "(batch id: " + gemsMonthlyBalanceList.get(0).getGemsUploadBatch().getBatchId() + "). " +
                    "If you need to re-generate a batch, use the 'regen' switch with the appropriate batch id.");
        }

        logger.info("Retrieving Ledger Account info...");

        Map<LedgerAccountCode, Map<ReportingType, SpcfMoney>> toDateBalancesByLedgerAccount = null;
        DomainEntitySet<LedgerAccount> ledgerAccounts = Application.findObjects(LedgerAccount.class);

        for (LedgerAccount ledgerAccount : ledgerAccounts) {
            if (ledgerAccount.getReportingFrequency() != null && ledgerAccount.getReportingFrequency().equals(ReportingFrequency.Monthly)) {
                String ledgerAccountCd = ledgerAccount.getLedgerAccountCd().toString();

                toDateBalanceMap.put(ledgerAccountCd, new TreeMap<ReportingType, BigDecimal>());
                toDateBalanceMap.get(ledgerAccountCd).put(ReportingType.Tax, new BigDecimal("0.00"));
                toDateBalanceMap.get(ledgerAccountCd).put(ReportingType.DirectDeposit, new BigDecimal("0.00"));

                periodBalanceMap.put(ledgerAccountCd, new TreeMap<ReportingType, BigDecimal>());
                periodBalanceMap.get(ledgerAccountCd).put(ReportingType.Tax, new BigDecimal("0.00"));
                periodBalanceMap.get(ledgerAccountCd).put(ReportingType.DirectDeposit, new BigDecimal("0.00"));

                Map<ReportingType, SpcfMoney> toDateBalances = getPreviousPeriodGemsMonthlyBalance(previousPeriod, ledgerAccount.getLedgerAccountCd());
                if (toDateBalances.isEmpty()) {
                    if (toDateBalancesByLedgerAccount == null) {
                        toDateBalancesByLedgerAccount = getPreviousPeriodLedgerBalance(previousPeriod);
                    }
                    toDateBalances = toDateBalancesByLedgerAccount.get(ledgerAccount.getLedgerAccountCd());
                }

                for (ReportingType reportingType : toDateBalances.keySet()) {
                    SpcfMoney previousPeriodGemsMonthlyAmount = toDateBalances.get(reportingType);
                    SpcfMoney toDateAmount = SpcfUtils.convertToSpcfMoney(toDateBalanceMap.get(ledgerAccountCd).get(reportingType)) ;

                    previousPeriodGemsMonthlyAmount = new SpcfMoney(toDateAmount.subtract(previousPeriodGemsMonthlyAmount));
                    periodBalanceMap.get(ledgerAccountCd).put(reportingType, SpcfUtils.convertToBigDecimal(previousPeriodGemsMonthlyAmount));
                }
            }
        }

        logger.info(String.format("Calculating current period GEMS monthly balance (all accounts) [%s]", processingDate));

        DomainEntitySet<LedgerBalance> retList = getCurrentPeriodLedgerBalance();

        for (LedgerBalance ledgerBalance : retList) {
            String ledgerAccountCd = ledgerBalance.getLedgerAccount().getLedgerAccountCd().toString();
            ReportingType reportingType = ledgerBalance.getReportingType();

            if (reportingType == null) {
                Company company = ledgerBalance.getCompany();
                Offering offering = Offering.findOffering(company, ServiceCode.DirectDeposit);
                if (offering != null) {
                    reportingType = offering.getReportingType();
                }
            }

            BigDecimal balanceAmount = SpcfUtils.convertToBigDecimal(ledgerBalance.getBalanceAmount());

            BigDecimal toDateBalanceMapAmount = toDateBalanceMap.get(ledgerAccountCd).get(reportingType);
            toDateBalanceMapAmount = toDateBalanceMapAmount.add(balanceAmount);

            toDateBalanceMap.get(ledgerAccountCd).put(reportingType, toDateBalanceMapAmount);

            BigDecimal periodBalanceMapAmount = periodBalanceMap.get(ledgerAccountCd).get(reportingType);
            periodBalanceMapAmount = periodBalanceMapAmount.add(balanceAmount);

            periodBalanceMap.get(ledgerAccountCd).put(reportingType, periodBalanceMapAmount);
        }

        GemsMonthlyBalance gemsMonthlyBalance;
        for (LedgerAccount ledgerAccount : ledgerAccounts) {
            if (ledgerAccount.getReportingFrequency() != null && ledgerAccount.getReportingFrequency().equals(ReportingFrequency.Monthly)) {
                String ledgerAccountCd = ledgerAccount.getLedgerAccountCd().toString();
                for (ReportingType reportingType : toDateBalanceMap.get(ledgerAccountCd).keySet()) {

                    logger.info(String.format("Normalizing GemsMonthlyBalance to reporting business rules [%s]:[%s]", ledgerAccountCd, reportingType.toString()));

                    gemsMonthlyBalance = new GemsMonthlyBalance();

                    gemsMonthlyBalance.setReportingPeriod(processingDate);
                    gemsMonthlyBalance.setToDateBalance(SpcfUtils.convertToSpcfMoney(toDateBalanceMap.get(ledgerAccountCd).get(reportingType)));
                    BigDecimal periodBalance = periodBalanceMap.get(ledgerAccountCd).get(reportingType);

                    gemsMonthlyBalance.setPeriodBalance(SpcfUtils.convertToSpcfMoney(periodBalance));

                    if (ledgerAccount.getBalanceCalculationRule().equals(LedgerBalanceCalculationRuleEnum.CreditAddsToBalance)) {
                        periodBalance = periodBalance.multiply(new BigDecimal("-1.00"));
                        gemsMonthlyBalance.setReportedBalance(SpcfUtils.convertToSpcfMoney(periodBalance));
                    }
                    else {
                        periodBalance = periodBalance.multiply(new BigDecimal("1.00"));
                        gemsMonthlyBalance.setReportedBalance(SpcfUtils.convertToSpcfMoney(periodBalance));
                    }

                    gemsMonthlyBalance.setGemsUploadBatch(mUploadBatch);

                    DomainEntitySet<GemsLedgerPostingRule> gemsPostingRuleList = Application.find(GemsLedgerPostingRule.class,
                            GemsLedgerPostingRule.LedgerAccount().equalTo(ledgerAccount).And(GemsLedgerPostingRule.ReportingType().equalTo(reportingType)));

                    GemsLedgerPostingRule gemsPostingRule = gemsPostingRuleList.getFirst();
                    gemsMonthlyBalance.setGemsLedgerPostingRule(gemsPostingRule);
                    Application.save(gemsMonthlyBalance);
                }
            }
        }

        mUploadBatch.setUploadStatus(GemsUploadBatchStatus.Finalized);
        mUploadBatch = Application.save(mUploadBatch);
    }

    /**
     * Function to get the Previous period Gems Monthly Balance for a given ledger account code
     * @param pPreviousPeriod String
     * @param pLedgerAccountCode LedgerAccountCode
     * @return toDateBalance SpcfMoney
     */
    private Map<ReportingType, SpcfMoney> getPreviousPeriodGemsMonthlyBalance(String pPreviousPeriod, LedgerAccountCode pLedgerAccountCode) {
        logger.info(String.format("Retrieving previous period GEMS monthly balance [%s, %s]",
                                  pPreviousPeriod,
                                  pLedgerAccountCode.toString()));

        Map<ReportingType, SpcfMoney> toDateBalances = new HashMap<ReportingType, SpcfMoney>();

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "reportingPeriod";
        paramNames[1] = "ledgerAccountCode";

        paramValues[0] = pPreviousPeriod;
        paramValues[1] = pLedgerAccountCode;

        DomainEntitySet<GemsMonthlyBalance> monthlyBalanceList =
                Application.findByNamedQuery("findPreviousPeriodGemsMonthlyBalance", paramNames, paramValues);

        if (monthlyBalanceList.size() > 0) {
            toDateBalances.put(ReportingType.Tax, SpcfMoney.ZERO);
            toDateBalances.put(ReportingType.DirectDeposit, SpcfMoney.ZERO);

            for (GemsMonthlyBalance gemsMonthlyBalance : monthlyBalanceList) {
                ReportingType reportingType = gemsMonthlyBalance.getGemsLedgerPostingRule().getReportingType();
                SpcfMoney toDateBalance = toDateBalances.remove(reportingType);
                toDateBalance = new SpcfMoney(toDateBalance.add(gemsMonthlyBalance.getToDateBalance()));
                toDateBalances.put(reportingType, toDateBalance);
            }
        }

        return toDateBalances;
    }

    /**
     * Function to get the Previous Period Ledger Balance for a given ledger account code
     * @param pPreviousPeriod String
     * @return toDateBalance SpcfMoney
     */
    private Map<LedgerAccountCode, Map<ReportingType, SpcfMoney>> getPreviousPeriodLedgerBalance(String pPreviousPeriod) {
        logger.info(String.format("Calculating previous period GEMS monthly balance [%s]", pPreviousPeriod));

        DomainEntitySet<LedgerAccount> ledgerAccounts = Application.find(LedgerAccount.class, LedgerAccount.ReportingFrequency().equalTo(ReportingFrequency.Monthly));

        Map<LedgerAccountCode, Map<ReportingType, SpcfMoney>> toDateBalances = new HashMap<LedgerAccountCode, Map<ReportingType, SpcfMoney>>();
        for (LedgerAccount ledgerAccount : ledgerAccounts) {
            toDateBalances.put(ledgerAccount.getLedgerAccountCd(), new HashMap<ReportingType, SpcfMoney>());
            toDateBalances.get(ledgerAccount.getLedgerAccountCd()).put(ReportingType.Tax, SpcfMoney.ZERO);
            toDateBalances.get(ledgerAccount.getLedgerAccountCd()).put(ReportingType.DirectDeposit, SpcfMoney.ZERO);
        }

        Calendar calendar = getCalendar(pPreviousPeriod, 0);
        String lastDayOfMonth = Integer.toString(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        //paramNames[0] = "fromDate";
        paramNames[0] = "toDate";
        paramNames[1] = "reportingFrequency";

        //paramValues[0] = pPreviousPeriod + "01";
        paramValues[0] = pPreviousPeriod + lastDayOfMonth;
        paramValues[1] = ReportingFrequency.Monthly;

        List<Object[]> retList =
                Application.executeNamedQuery(
                        Application.getQueryName("findPreviousPeriodLedgerAccountBalance"), paramNames, paramValues);

        for (Object[] objects : retList) {
            LedgerAccountCode ledgerAccountCode = (LedgerAccountCode) objects[0];
            ReportingType reportingType = (ReportingType) objects[1];
            SpcfMoney toDateBalance = (SpcfMoney) objects[2];

            SpcfMoney amount = toDateBalances.get(ledgerAccountCode).get(reportingType);
            toDateBalances.get(ledgerAccountCode).put(reportingType, new SpcfMoney(amount.add(toDateBalance)));
        }

        return toDateBalances;
    }

    /**
     * Function to get the current period ledge balance list
     * @return list DomainEntitySet<LedgerBalance>
     */
    private DomainEntitySet<LedgerBalance> getCurrentPeriodLedgerBalance() {
        Calendar calendar = getCalendar(processingDate, 0);
        String lastDayOfMonth = Integer.toString(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        //paramNames[0] = "fromDate";
        paramNames[0] = "toDate";
        paramNames[1] = "reportingFrequency";

        //paramValues[0] = processingDate + "01";
        paramValues[0] = processingDate + lastDayOfMonth;
        paramValues[1] = ReportingFrequency.Monthly;

        return Application.findByNamedQuery(
                Application.getQueryName("findCurrentPeriodLedgerAccountBalance"), paramNames, paramValues);
    }

    /**
     * Function to genenrate the Monthly Gems File for a given upload batch id
     * @param pGemsUploadBatchId  String
     * @throws Exception  exception
     */
    private void generateMonthlyGemsFile(String pGemsUploadBatchId) throws Exception {
        OutputStreamWriter fileWriter = null;
        String lastDayOfMonth = "";
        String reportingPeriod = "";
        try {
            DomainEntitySet<GemsMonthlyBalance> monthlyBalanceList = findGemsMonthlyBalanceList(pGemsUploadBatchId);

            String strFileName = generateFileName();
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            fileWriter = new IDPSFileWriter(strFileName,key);

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            if (monthlyBalanceList.size() > 0) {
                reportingPeriod = monthlyBalanceList.get(0).getReportingPeriod();
                Calendar calendar = getCalendar(monthlyBalanceList.get(0).getReportingPeriod(), 0);
                lastDayOfMonth = Integer.toString(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("yyyyMMdd");
                SpcfCalendar parsedRunDate = dateFormat.parse(reportingPeriod + "" + lastDayOfMonth);
                //Set the date on the calendar that has the local time zone
                date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            for (GemsMonthlyBalance monthlyBalance : monthlyBalanceList) {
                fileWriter.write(String.valueOf(monthlyBalance.getGemsUploadBatch().getBatchId()));
                fileWriter.write(DELIMITOR);
                fileWriter.write(monthlyBalance.getGemsLedgerPostingRule().getCompany());
                fileWriter.write(DELIMITOR);
                fileWriter.write(monthlyBalance.getGemsLedgerPostingRule().getGroupCode());
                fileWriter.write(DELIMITOR);
                fileWriter.write(monthlyBalance.getGemsLedgerPostingRule().getDepartment());
                fileWriter.write(DELIMITOR);
                fileWriter.write(monthlyBalance.getGemsLedgerPostingRule().getAccount());
                fileWriter.write(DELIMITOR);
                fileWriter.write(monthlyBalance.getGemsLedgerPostingRule().getInterCompany());
                fileWriter.write(DELIMITOR);
                fileWriter.write(String.valueOf(monthlyBalance.getReportedBalance()));
                fileWriter.write(DELIMITOR);
                fileWriter.write(StringFormatter.formatDate(date, "yyMMdd"));
                fileWriter.write(FILE_EOL);
            }

            mUploadBatch.setFileName(strFileName);
            mUploadBatch.setUploadStatus(GemsUploadBatchStatus.PendingTransmission);
            mUploadBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            mUploadBatch = Application.save(mUploadBatch);
            fileWriter.flush();
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    /**
     * Function to regenrate an exisiting batch for a given upload batch id
     * @param pGemsUploadBatchId String
     */
    private void regenerateAnExistingBatch(String pGemsUploadBatchId) {

        DomainEntitySet<GemsMonthlyBalance> monthlyBalanceList = findGemsMonthlyBalanceList(pGemsUploadBatchId);

        String reportingPeriod = monthlyBalanceList.get(0).getReportingPeriod();

        for (GemsMonthlyBalance gemsMonthlyBalance : monthlyBalanceList) {
            Application.delete(gemsMonthlyBalance);
        }
        mUploadBatch.setUploadStatus(GemsUploadBatchStatus.Superceded);
        mUploadBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        mUploadBatch = Application.save(mUploadBatch);

        process(Commands.gen.toString(), reportingPeriod, null);
    }

    /**
     * Method to get the Gems Monthly Balance List for the given upload batch id
     * @param pGemsUploadBatchId String
     * @return DomainEntitySet<GemsMonthlyBalance>
     */
    private DomainEntitySet<GemsMonthlyBalance> findGemsMonthlyBalanceList(String pGemsUploadBatchId) {
        DomainEntitySet<GemsUploadBatch> uploadBatchList = Application.find(GemsUploadBatch.class, GemsUploadBatch.BatchId().equalTo(Integer.parseInt(pGemsUploadBatchId)));

        if (uploadBatchList.size() > 0) {
            mUploadBatch = uploadBatchList.get(0);
        }
        else {
            throw new RuntimeException("Invalid Batch Id: " + pGemsUploadBatchId);
        }

        return Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.GemsUploadBatch().equalTo(mUploadBatch));
    }

    /**
     * Function to get the new upload batch id from the sequence
     * @return int
     */
    private int generateNewBatchId() {
        return Application.nextSequenceValue(SequenceId.SEQ_GEMS_UPLOAD_BATCH_ID, Long.class).intValue();
    }

    /**
     * Function to generate the gems upload file name
     * @return String
     */
    private String generateFileName() {
        SpcfCalendar systemDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        String outputDir = OUTPUT_DIRECTORY;
        String filePrefix = outputDir + File.separator;
        String fileSuffix = FILE_PREFIX + StringFormatter.formatDate(systemDate, "yyMMdd") + "-" +
                StringFormatter.formatDate(systemDate, "HHmmss") + ".txt";
        return filePrefix + fileSuffix;
    }

    public GemsUploadBatch getUploadBatch() {
        return mUploadBatch;
    }

    /**
     * Method to get the Calendar object for the given input date
     * @param pInputDate(yyyyMM) String
     * @param pValue int (value to add/subtract the number of months)
     * @return Calendar
     */
    private Calendar getCalendar(String pInputDate, int pValue) {
        int year = Integer.parseInt(pInputDate.substring(0, 4));
        int month = Integer.parseInt(pInputDate.substring(4, 6));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, pValue);

        return calendar;
    }
}
