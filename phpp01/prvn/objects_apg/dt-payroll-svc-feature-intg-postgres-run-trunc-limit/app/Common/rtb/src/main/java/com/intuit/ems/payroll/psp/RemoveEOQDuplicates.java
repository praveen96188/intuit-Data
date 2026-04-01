package com.intuit.ems.payroll.psp;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 10/7/14
 * Time: 11:35 PM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawTransactions;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by anandp233 on 8/21/14.
 */
public class RemoveEOQDuplicates {

    private static String mYearAndQuarter = null;
    private static boolean mCommit = false;
    private static String mAction = "report";
    private static String mFileName = null;
    private static String mAuthUser = null;
    private static final String PROCESSING_YEAR_AND_QUARTER_COMMAND = "-yearAndQuarter";
    private static final String COMMIT_COMMAND = "-commit";
    private static final String ACTION = "-action";
    private static final String REPORT = "REPORT";
    private static final String PROCESS = "PROCESS";
    private static final String FILE_NAME_COMMAND = "-file";
    private static final String AUTH_USER_COMMAND = "-authUser";
    private int SUCCESS_COUNT = 0;
    private int FAIL_COUNT = 0;


    public static void main(String[] args) {


        List<String> compAdjustSubSeqs = new ArrayList<String>();

        try {
            parseArgs(args);

            System.out.println("Starting RemoveEOQDuplicates with filename - " + mFileName);

            // Set the Principal to the provided Corp ID if provided.
            AuthUser user = null;
            if (!(mAuthUser == null || mAuthUser.trim().length() == 0)) {
                user = AuthUser.findUser(mAuthUser);
            }

            if (user != null) {
                PayrollServices.setCurrentPrincipal(user.createPrincipal());
            } else {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));
                // PayrollServices.setCurrentPrincipal(SystemPrincipal.EoqSUITaxAdjustmentsBatchJob);
            }

            // Open the file
            FileReader fileReader = null;
            try {
                File f = new File(mFileName);
                fileReader = new FileReader(f);
                BufferedReader input = new BufferedReader(fileReader);
                String line;
                // For each license number in the file
                while ((line = input.readLine()) != null) {
                    //String[] values = line.split(",");
                    if (line == null || line.trim().length() == 0) {
                        System.out.println("ERROR : Invalid line in input file - " + line);
                    } else {
                        String compAdjustSubSeq = line.trim();
                        if (compAdjustSubSeq != null) {
                            compAdjustSubSeqs.add(compAdjustSubSeq);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("ERROR : I/O Exception while reading file" + e.getMessage());
                System.exit(-1);
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR : Error in main while processing the RemoveEOQDuplicates " + e.getMessage());
            System.exit(-1);
        }
        if (REPORT.equalsIgnoreCase(mAction)) {
            System.out.println("In Process." + "Total number of compAdjustSubSeqs to get report  =" + compAdjustSubSeqs.size());
        } else {
            System.out.println("In Process with commit='" + mCommit + "'." + "Total number of compAdjustSubSeqs to be process =" + compAdjustSubSeqs.size());
        }

        RemoveEOQDuplicates removeEOQDuplicates = new RemoveEOQDuplicates();
        removeEOQDuplicates.removeEOQDuplicates(compAdjustSubSeqs);

        if (REPORT.equalsIgnoreCase(mAction)) {
            System.out.println("RemoveEOQDuplicates report Process is completed. Total number of records processed = " + (removeEOQDuplicates.SUCCESS_COUNT + removeEOQDuplicates.FAIL_COUNT));
            System.out.println("Valid Count is " + removeEOQDuplicates.SUCCESS_COUNT + ".Invalid Count is " + removeEOQDuplicates.FAIL_COUNT);
        } else {
            System.out.println("RemoveEOQDuplicates Process completed. Total number of records processed = " + (removeEOQDuplicates.SUCCESS_COUNT + removeEOQDuplicates.FAIL_COUNT));
            System.out.println("Success Count is " + removeEOQDuplicates.SUCCESS_COUNT + ".Failed Count is " + removeEOQDuplicates.FAIL_COUNT);
        }

        System.exit(0);


    }

    /**
     * examples commands are a s below
     * To get reports
     * -file=C:\liability\liability.txt -commit=false -yearAndQuarter=20122 -action-=REPORT -authUser=AL_admin
     * To process records
     * -file=C:\liability\liability.txt -commit=true -yearAndQuarter=20122 -action=PROCESS -authUser=AL_admin
     *
     * @param args
     */
    private static void parseArgs(String[] args) {
        if (args.length < 3) {
            throw new RuntimeException("Invalid number of arguments: " + args.toString());
        }

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts[0].equalsIgnoreCase(PROCESSING_YEAR_AND_QUARTER_COMMAND)) {
                if (argParts[1] != null && argParts[1].length() == 5 && isNumber(argParts[1])) {
                    mYearAndQuarter = argParts[1];
                } else {
                    throw new RuntimeException("Invalid value for argument -yearAndQuarter: " + argParts[1]);
                }

            } else if (argParts[0].equalsIgnoreCase(COMMIT_COMMAND)) {
                String commit = argParts[1];
                if (commit != null && (commit.equalsIgnoreCase("TRUE") || commit.equalsIgnoreCase("FALSE"))) {
                    mCommit = Boolean.valueOf(argParts[1]);
                } else {
                    throw new RuntimeException("Invalid value for argument -commit: " + commit);
                }

            } else if (argParts[0].equalsIgnoreCase(ACTION)) {
                mAction = argParts[1];
            } else if (argParts[0].equalsIgnoreCase(FILE_NAME_COMMAND)) {
                mFileName = argParts[1];
            } else if (argParts[0].equalsIgnoreCase(AUTH_USER_COMMAND)) {
                mAuthUser = argParts[1];
            }
        }
        if (mAction == null || mAction.trim().length() == 0) {
            mAction = REPORT;
        }

        if ((mAction.equalsIgnoreCase(PROCESS) || mAction.equalsIgnoreCase(REPORT)) && (mYearAndQuarter == null || mYearAndQuarter.trim().length() == 0 || mFileName == null || mFileName.trim().length() == 0)) {
            System.out.println("ERROR : Invalid parameters - Must provide all required parameters.");
            System.exit(-1);
        } else {
            System.out.println("Given Parameters for RemoveEOQDuplicates, mYearAndQuarter='" + mYearAndQuarter + "' mCommit='" + mCommit + "' mAction='" + mAction + "' mFileName='" + mFileName + "' mAuthUser='" + mAuthUser + "'");
        }


    }

    private void removeEOQDuplicates(List<String> pCompAdjustSubSeqs) {
        for (String compAdjustSubSeq : pCompAdjustSubSeqs) {
            process(compAdjustSubSeq, mYearAndQuarter, mCommit);
        }
    }

    public void process(String pCompAdjustSubSeq, String pYearAndQuarter, boolean pCommit) {
        int year = 0;
        int quarter = 1;
        try {

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            //get or parse details
            year = Integer.parseInt(pYearAndQuarter.substring(0, 4));
            quarter = Integer.parseInt(pYearAndQuarter.substring(4, 5));

            DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = getCompanyAdjustmentSubmissions(pCompAdjustSubSeq);
            boolean iReverseRecordsCreated = false;
            //create the report or apply the changes
            if (companyAdjustmentSubmissions.size() > 0) {
                if (REPORT.equalsIgnoreCase(mAction)) {
                    findEOQDuplicatesEntries(companyAdjustmentSubmissions.get(0), year, quarter, pCommit);
                } else {
                    iReverseRecordsCreated = processCompany(companyAdjustmentSubmissions.get(0), year, quarter, pCommit);
                }
            } else {
                System.out.println("ERROR : The given CompAdjustSubSeq is not found, CompAdjustSubSeq='" + pCompAdjustSubSeq + "'");
            }

            //commit if any
            if (pCommit && !REPORT.equalsIgnoreCase(mAction) && iReverseRecordsCreated) {// commit only when it not report
                System.out.println("Committing updates for CompAdjustSubSeq='" + pCompAdjustSubSeq + "'");
                PayrollServices.commitUnitOfWork();
            }

            if (!REPORT.equalsIgnoreCase(mAction)) {
                System.out.println("Completed processing the CompAdjustSubSeq='" + pCompAdjustSubSeq + "'.");
            }

        } catch (Throwable e) {
            System.out.println("ERROR : Error while processing the CompAdjustSubSeq='" + pCompAdjustSubSeq + "'." + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
            if (!REPORT.equalsIgnoreCase(mAction)) {
                System.out.println("");  //This is just for printing next set of record logs separately to view each record in a set of logs.
            }
        }
    }


    /**
     * @param pCompAdjustSubSeq
     * @return
     */
    private DomainEntitySet<CompanyAdjustmentSubmission> getCompanyAdjustmentSubmissions(String pCompAdjustSubSeq) {
        SpcfUniqueId compAdjustSubSeqId = new SpcfUniqueIdImpl(pCompAdjustSubSeq);

        //find the seq or id
        Expression<CompanyAdjustmentSubmission> query =
                new Query<CompanyAdjustmentSubmission>()
                        .Where(CompanyAdjustmentSubmission.Id().equalTo(compAdjustSubSeqId));
        DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class, query);
        return companyAdjustmentSubmissions;
    }

    /**
     * @param pCompanyAdjustmentSubmission
     * @param pYear
     * @param pQuarter
     * @param pCommit
     */
    public boolean processCompany(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission, int pYear, int pQuarter, boolean pCommit) {
        String sourceCompanyId = null;

        sourceCompanyId = pCompanyAdjustmentSubmission.getCompany().getSourceCompanyId();
        System.out.println("Started processing the CompAdjustSubSeq='" + pCompanyAdjustmentSubmission.getId() + "' ,PSID='" + sourceCompanyId + "'");
        if (isValidForReverseEOQAdjustment(pCompanyAdjustmentSubmission, pYear, pQuarter)) {
            System.out.println("The given CompanyAdjustmentSubmission is valid for EOQ reverse transaction, so adding the reverse entries for compAdjSeq='" + pCompanyAdjustmentSubmission.getId() + "'");
            addReverseEntries(pCompanyAdjustmentSubmission);
            System.out.println("The reverse entries are added for compAdjSeq='" + pCompanyAdjustmentSubmission.getId() + "'");
            SUCCESS_COUNT++;
            return true;
        } else {
            FAIL_COUNT++;
            System.out.println("The given CompanyAdjustmentSubmission is not valid for EOQ reverse transaction. compAdjSeq='" + pCompanyAdjustmentSubmission.getId() + "'");
        }
        return false;

    }

    /**
     * @param pCompanyAdjustmentSubmission
     * @param year
     * @param quarter
     * @return
     */
    public boolean isValidForReverseEOQAdjustment(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission, int year, int quarter) {
        Set<Law> laws = new HashSet<Law>();
        //TODO : need year, quarter, law id
        //1. get the comp adjust seq
        //2. get all liab adjusts and copy all laws
        //3. check the amount of company adjust amt with remaining the payment in tax ledger for one of the law if it matches then reverse all transactions
        // else no change
        DomainEntitySet<LiabilityAdjustment> newLiabilityAdjustments = pCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        if (newLiabilityAdjustments != null) {
            for (LiabilityAdjustment adjustment : newLiabilityAdjustments) {
                //laws.add(adjustment.getLaw());
                if (isValidForReverseEOQLibAdjustment(pCompanyAdjustmentSubmission, adjustment.getLaw(), year, quarter)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the given CompanyAdjustmentSubmission has same amount as tax due amount for given law, quarter,year.
     *
     * @param pCompanyAdjustmentSubmission
     * @param law
     * @param year
     * @param quarter
     * @return
     */
    public boolean isValidForReverseEOQLibAdjustment(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission, Law law, int year, int quarter) {
        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPLawTransactions> lawTransactions = null;
        SpcfMoney compAdjustAmt = null;
        SpcfMoney currentTaxesSum = null;
        String agencyId = null;
        String paymentTemplate = null;
        Date yearQuarterStartDate = null;
        Date yearQuarterEndDate = null;
        try {
            yearQuarterStartDate = new Date(CalendarUtils.getFirstDayOfQuarter(year, quarter).getTimeInMilliseconds());
            yearQuarterEndDate = new Date(CalendarUtils.getLastDayOfQuarter(year, quarter).getTimeInMilliseconds());
            agencyId = law.getPaymentTemplate().getAgency().getAgencyId();
            paymentTemplate = law.getPaymentTemplate().getPaymentTemplateCd();
            lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), pCompanyAdjustmentSubmission.getCompany().getSourceCompanyId(), null, agencyId, paymentTemplate, law.getLawId(), null, yearQuarterStartDate, yearQuarterEndDate, true, false);
            if(lawTransactions.size() == 0){
               return false;
            }
            compAdjustAmt = pCompanyAdjustmentSubmission.getAmount();
            currentTaxesSum = getSpcfMoneyFromString(String.valueOf(lawTransactions.get(0).getCurrentTaxesSum()), SpcfMoney.ZERO);
            if (currentTaxesSum.equals(compAdjustAmt)) {
                return true;
            }

        } catch (Throwable pThrowable) {
            System.out.println("Error while getting data in isValidForReverseEOQLibAdjustment() for given CompanyAdjustmentSubmission compAdjSeq='" + pCompanyAdjustmentSubmission.getId() + "'. Reason is:" + pThrowable.getMessage());
            pThrowable.printStackTrace(System.out);
        }
        return false;
    }

    /**
     * Add Entries for companyadjustments, Then use newly created companyadjustments as reference while creating new payrollrun.
     * Use newly created  companyadjustments   and payrollrun  as reference  while creating liability adjustments.
     *
     * @param pCompanyAdjustmentSubmission
     */
    public void addReverseEntries(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        CompanyAdjustmentSubmission newCompanyAdjustmentSubmission = addReverseEntryInCompAdjustSubmission(pCompanyAdjustmentSubmission);

        PayrollRun payrollRun = addReverseEntryInPayrollRun(pCompanyAdjustmentSubmission);

        addReverseEntryInLiabilityAdjustment(pCompanyAdjustmentSubmission, newCompanyAdjustmentSubmission, payrollRun);
    }

    /**
     * Create new CompanyAdjustmentSubmission with nagating the amount for given CompanyAdjustmentSubmission
     *
     * @param pCompanyAdjustmentSubmission
     * @return
     */
    public CompanyAdjustmentSubmission addReverseEntryInCompAdjustSubmission(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        SpcfMoney amount = new SpcfMoney(pCompanyAdjustmentSubmission.getAmount().negate()); //is it correct

        CompanyAdjustmentSubmission companyAdjustmentSubmission = new CompanyAdjustmentSubmission();
        if (pCompanyAdjustmentSubmission.getSubmissionDate() != null) {
            companyAdjustmentSubmission.setSubmissionDate(pCompanyAdjustmentSubmission.getSubmissionDate());
        } else {
            companyAdjustmentSubmission.setSubmissionDate(PSPDate.getPSPTime());
        }
        companyAdjustmentSubmission.setSourceId(pCompanyAdjustmentSubmission.getSourceId());
        companyAdjustmentSubmission.setAmount(amount);
        companyAdjustmentSubmission.setOriginalSubmission(pCompanyAdjustmentSubmission.getOriginalSubmission());
        companyAdjustmentSubmission.setCompany(pCompanyAdjustmentSubmission.getCompany());

        if (pCompanyAdjustmentSubmission.getQbdtPayrollTransaction() != null) {
            QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
            qbdtTransactionInfo.setCompany(pCompanyAdjustmentSubmission.getCompany());
            qbdtTransactionInfo.setCompanyAdjustmentSubmission(companyAdjustmentSubmission);
            qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
            companyAdjustmentSubmission.setQbdtTransactionInfo(qbdtTransactionInfo);
        }

        companyAdjustmentSubmission = Application.save(companyAdjustmentSubmission);

        System.out.println("Added the entry for CompanyAdjustmentSubmission oldSeq='" + pCompanyAdjustmentSubmission.getId() + "' newSeq='" + companyAdjustmentSubmission.getId() + "'");

        return companyAdjustmentSubmission;
    }

    /**
     * Create payrollrun for given old CompanyAdjustmentSubmission
     *
     * @param oldCompanyAdjustmentSubmission
     * @return
     */
    public PayrollRun addReverseEntryInPayrollRun(CompanyAdjustmentSubmission oldCompanyAdjustmentSubmission) {

        //use give date from command

        PayrollRun payrollRun = new PayrollRun();

        payrollRun.setSourcePayRunId(SpcfUniqueId.generateRandomUniqueIdString());
        Company company = oldCompanyAdjustmentSubmission.getCompany();
        // Associate Company and Payroll Run
        payrollRun.setCompany(company);
        payrollRun.setFundingModel(company.getFundingModel().getFundingModelCd());

        // Set PayrollRun date
        payrollRun.setPayrollRunDate(PSPDate.getPSPTime());

        //Set paycheck date and paycheck settlement date
        payrollRun.setPaycheckDate(oldCompanyAdjustmentSubmission.getPayrollRun().getPaycheckDate());

        SpcfCalendar settlementDate = oldCompanyAdjustmentSubmission.getCompany().getNextValidPaycheckDepositDate(PSPDate.getPSPTime());
        CalendarUtils.clearTime(settlementDate);
        payrollRun.setPaycheckSettlementDate(settlementDate);

        // Set PayrollRun status
        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);//TODO:Recorded?

        // Set PayrollRun type = Adjustment
        payrollRun.setPayrollRunType(PayrollType.Adjustment);   //TODO : Payroll

        payrollRun = Application.save(payrollRun);
        System.out.println("Added the entry for PayrollRun for CompanyAdjustmentSubmission oldSeq='" + oldCompanyAdjustmentSubmission.getId() + "'");
        return payrollRun;

    }

    /**
     * Create LiabiltyAdjustments for given old  CompanyAdjustmentSubmission with existingLiabilityAdjustments  by  nagating the amount.
     *
     * @param pOldCompanyAdjustmentSubmission
     *
     * @param pCompanyAdjustmentSubmission
     * @param pPayrollRun
     * @return
     */
    public DomainEntitySet<LiabilityAdjustment> addReverseEntryInLiabilityAdjustment(CompanyAdjustmentSubmission pOldCompanyAdjustmentSubmission, CompanyAdjustmentSubmission pCompanyAdjustmentSubmission, PayrollRun pPayrollRun) {
        PayrollRun oldPayrollRun = pOldCompanyAdjustmentSubmission.getPayrollRun();
        DomainEntitySet<LiabilityAdjustment> existingLiabilityAdjustments = pOldCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection();//Application.find(LiabilityAdjustment.class, query);
        DomainEntitySet<LiabilityAdjustment> newLiabilityAdjustments = new DomainEntitySet<LiabilityAdjustment>();

        for (LiabilityAdjustment liabilityAdj : existingLiabilityAdjustments) {
            LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
            liabilityAdjustment.setCompany(oldPayrollRun.getCompany());
            liabilityAdjustment.setIsReconcilingAdjustment(true);
            liabilityAdjustment.setAmount((SpcfMoney) liabilityAdj.getAmount().negate());
            liabilityAdjustment.setLaw(liabilityAdj.getLaw());
            liabilityAdjustment.setPayrollRun(pPayrollRun);
            liabilityAdjustment.setCompanyLaw(liabilityAdj.getCompanyLaw());
            liabilityAdjustment.setEffectiveDate(oldPayrollRun.getPaycheckDate());
            Application.save(liabilityAdjustment);
            System.out.println("Added the entry for LiabilityAdjustment for CompanyAdjustmentSubmission oldSeq='" + pOldCompanyAdjustmentSubmission.getId() + "' newSeq='" + pCompanyAdjustmentSubmission.getId() + "' for existing liabilityAdj seq='" + liabilityAdj.getId() + "'");
            newLiabilityAdjustments.add(liabilityAdjustment);
        }


        return newLiabilityAdjustments;

    }

    /**
     * @param pCompanyAdjustmentSubmission
     * @param pYear
     * @param pQuarter
     * @param pCommit
     */
    public void findEOQDuplicatesEntries(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission, int pYear, int pQuarter, boolean pCommit) {
        String sourceCompanyId = null;
        sourceCompanyId = pCompanyAdjustmentSubmission.getCompany().getSourceCompanyId();
        Law law= pCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection().getFirst().getLaw();
        boolean isValid=false;
        if (isValidForReverseEOQAdjustment(pCompanyAdjustmentSubmission, pYear, pQuarter)) {
            isValid=true;
            System.out.println("Valid='"+pCompanyAdjustmentSubmission.getId()+"'");
            SUCCESS_COUNT++;
        } else {
            FAIL_COUNT++;
            System.out.println("Invalid='"+pCompanyAdjustmentSubmission.getId()+"'");
        }
        System.out.println("companyAdjSeq='" + pCompanyAdjustmentSubmission.getId() +"' isValid='"+isValid+ "' PSID='" + sourceCompanyId + "'" + " amount='" + pCompanyAdjustmentSubmission.getAmount() + "' Law='"+law.getLawId()+"' AgencyId='"+law.getPaymentTemplate().getAgency().getAgencyId()+"'") ;

    }

    /**
     * @param pAmount
     * @param defaultValue
     * @return
     */
    public static SpcfMoney getSpcfMoneyFromString(String pAmount, SpcfMoney defaultValue) {
        if (StringUtils.isEmpty(pAmount)) {
            return defaultValue;
        }
        return new SpcfMoney(pAmount);
    }

    public static boolean isNumber(String string) {
        try {
            Long.parseLong(string);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

