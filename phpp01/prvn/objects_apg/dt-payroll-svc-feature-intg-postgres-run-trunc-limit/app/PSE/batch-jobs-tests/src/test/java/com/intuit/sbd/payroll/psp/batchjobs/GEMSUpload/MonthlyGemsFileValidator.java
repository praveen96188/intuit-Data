package com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: ihannur
 * Date: 5/22/12
 * Time: 6:03 PM
 */
public class MonthlyGemsFileValidator {

    public static void validateData(GemsUploadBatch pUploadBatch, String pReportingPeriod, String pPreviousPeriod) {
        HashMap<LedgerAccountCode, HashMap<ReportingType, SpcfDecimal>> toDateBalanceMap = new HashMap<LedgerAccountCode, HashMap<ReportingType, SpcfDecimal>>();
        HashMap<LedgerAccountCode, HashMap<ReportingType, SpcfDecimal>> periodBalanceMap = new HashMap<LedgerAccountCode, HashMap<ReportingType, SpcfDecimal>>();
        DomainEntitySet<LedgerBalance> retList = getCurrentPeriodLedgerBalance(pReportingPeriod);

        //Get the Previous Period Ledger Balance for each ledger Account Code and create maps for toDateBalance &
        //PeriodBalance to use them for asserting the values.
        DomainEntitySet<GemsLedgerPostingRule> gemsLedgerPostingRules = Application.findObjects(GemsLedgerPostingRule.class).find(GemsLedgerPostingRule.LedgerAccount().ReportingFrequency().equalTo(ReportingFrequency.Monthly));

        for (GemsLedgerPostingRule gemsLedgerPostingRule : gemsLedgerPostingRules) {
            if (!toDateBalanceMap.containsKey(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd())) {
                toDateBalanceMap.put(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd(), new HashMap<ReportingType, SpcfDecimal>());
                toDateBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(ReportingType.Tax, SpcfMoney.ZERO);
                toDateBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(ReportingType.DirectDeposit, SpcfMoney.ZERO);
            }

            if (!periodBalanceMap.containsKey(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd())) {
                periodBalanceMap.put(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd(), new HashMap<ReportingType, SpcfDecimal>());
                periodBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(ReportingType.Tax, SpcfMoney.ZERO);
                periodBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(ReportingType.DirectDeposit, SpcfMoney.ZERO);
            }

            Map<ReportingType, SpcfMoney> toDateBalances = getPreviousPeriodGemsMonthlyBalance(pPreviousPeriod, gemsLedgerPostingRule);

            SpcfDecimal amount = toDateBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).get(gemsLedgerPostingRule.getReportingType());
            amount = new SpcfMoney(amount.subtract(toDateBalances.get(gemsLedgerPostingRule.getReportingType())));
            periodBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(gemsLedgerPostingRule.getReportingType(), amount);
        }

        for (LedgerBalance ledgerBalance : retList) {

            SpcfDecimal ledgerBalanceAmount = ledgerBalance.getBalanceAmount();
//            ledgerBalanceAmount = ledgerBalanceAmount.subtract(getPreviousPeriodCompanyLedgerBalance(ledgerBalance.getLedgerAccount().getLedgerAccountCd(), pPreviousPeriod, ledgerBalance.getCompany()));

//            ReportingType reportingType = GemsLedgerPostingRule.getGemsAccountServiceType(ledgerBalance.getCompany());
//            GemsLedgerPostingRule gemsLedgerPostingRule = GemsLedgerPostingRule.findGemsLedgerPostingRule(ledgerBalance.getLedgerAccount().getLedgerAccountCd(), reportingType);
            GemsLedgerPostingRule gemsLedgerPostingRule = GemsLedgerPostingRule.findGemsLedgerPostingRule(ledgerBalance.getLedgerAccount().getLedgerAccountCd(), ledgerBalance.getReportingType());

            if(gemsLedgerPostingRule != null) {
                SpcfDecimal toDateBalanceMapAmount = toDateBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).get(gemsLedgerPostingRule.getReportingType());
                toDateBalanceMapAmount = toDateBalanceMapAmount.add(ledgerBalanceAmount);

                toDateBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(gemsLedgerPostingRule.getReportingType(), toDateBalanceMapAmount);

                SpcfDecimal periodBalanceMapAmount = periodBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).get(gemsLedgerPostingRule.getReportingType());
                periodBalanceMapAmount = periodBalanceMapAmount.add(ledgerBalanceAmount);

                periodBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).put(gemsLedgerPostingRule.getReportingType(), periodBalanceMapAmount);
            } else {
//                assertTrue(String.format("GemsLedgerPostingRule is not found for Company: %s, ReportingType: %s, LedgerAccountCd: %s",
//                                         ledgerBalance.getCompany(), reportingType != null ? reportingType.toString() : null, ledgerBalance.getLedgerAccount().getLedgerAccountCd().toString()), false);
                assertTrue(String.format("GemsLedgerPostingRule is not found for Company: %s, LedgerAccountCd: %s",
                        ledgerBalance.getCompany(), ledgerBalance.getLedgerAccount().getLedgerAccountCd().toString()), false);
            }
        }

        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalanceList = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ReportingPeriod().equalTo(pReportingPeriod));
        for (GemsMonthlyBalance monthlyBalance : gemsMonthlyBalanceList) {
            assertEquals("Gems Upload Batch Id", pUploadBatch.getBatchId(), monthlyBalance.getGemsUploadBatch().getBatchId());
            GemsLedgerPostingRule gemsLedgerPostingRule = monthlyBalance.getGemsLedgerPostingRule();
            SpcfMoney toDateBalanceAmt = (SpcfMoney) toDateBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).get(gemsLedgerPostingRule.getReportingType());
            SpcfMoney periodBalanceAmt = (SpcfMoney) periodBalanceMap.get(gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd()).get(gemsLedgerPostingRule.getReportingType());
            assertEquals("Todate Balance for Ledger AccountCd: "+ gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd().toString(), //+ " ReportingType:"+ gemsLedgerPostingRule.getReportingType().toString(),
                    toDateBalanceAmt, monthlyBalance.getToDateBalance() );
            assertEquals("Period Balance for Ledger AccountCd: "+ gemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd().toString(), //+ " ReportingType:"+ gemsLedgerPostingRule.getReportingType().toString(),
                    periodBalanceAmt, monthlyBalance.getPeriodBalance() );
        }
    }

    private static Map<ReportingType, SpcfMoney> getPreviousPeriodGemsMonthlyBalance(String pPreviousPeriod, GemsLedgerPostingRule pGemsLedgerPostingRule) {

        Map<ReportingType, SpcfMoney> toDateBalances = new HashMap<ReportingType, SpcfMoney>();
        toDateBalances.put(ReportingType.Tax, SpcfMoney.ZERO);
        toDateBalances.put(ReportingType.DirectDeposit, SpcfMoney.ZERO);

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "reportingPeriod";
        paramNames[1] = "ledgerAccountCode";
//        paramNames[1] = "gemsPostingRule";

        paramValues[0] = pPreviousPeriod;
        paramValues[1] = pGemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd();
//        paramValues[1] = pGemsLedgerPostingRule;

        DomainEntitySet<GemsMonthlyBalance> monthlyBalanceList =
                Application.findByNamedQuery("findPreviousPeriodGemsMonthlyBalance", paramNames, paramValues);

        if (monthlyBalanceList.size() > 0) {
            for (GemsMonthlyBalance gemsMonthlyBalance : monthlyBalanceList) {
                ReportingType reportingType = gemsMonthlyBalance.getGemsLedgerPostingRule().getReportingType();

                SpcfMoney amount = toDateBalances.get(reportingType);
                toDateBalances.put(reportingType, new SpcfMoney(amount.add(gemsMonthlyBalance.getToDateBalance())));
            }
        } else {
            toDateBalances = getPreviousPeriodLedgerBalance(pGemsLedgerPostingRule, pPreviousPeriod);
        }

        return toDateBalances;
    }


    private static SpcfDecimal getPreviousPeriodCompanyLedgerBalance(LedgerAccountCode pLedgerAccountCode, String pPreviousPeriod, Company pCompany) {

        int year = Integer.parseInt(pPreviousPeriod.substring(0, 4));
        int month = Integer.parseInt(pPreviousPeriod.substring(4, 6));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, 0);
        String lastDayOfMonth = Integer.toString(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        SpcfDecimal previousBalance = new SpcfMoney("0.00");

        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];

        paramNames[0] = "toDate";
        paramNames[1] = "ledgerAccountCode";
        paramNames[2] = "company";

        paramValues[0] = pPreviousPeriod + lastDayOfMonth;
        paramValues[1] = pLedgerAccountCode;
        paramValues[2] = pCompany;

        List<SpcfMoney> retList =
                Application.executeNamedQuery("findPreviousPeriodCompanyLedgerAccountBalance", paramNames, paramValues);

        if (retList.size() > 0) {
            if (retList.get(0) != null) {
                previousBalance = retList.get(0);
            }
        }

        return previousBalance;
    }

    private static DomainEntitySet<LedgerBalance> getCurrentPeriodLedgerBalance(String pProcessingDate) {
        int year = Integer.parseInt(pProcessingDate.substring(0, 4));
        int month = Integer.parseInt(pProcessingDate.substring(4, 6));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, 0);
        String lastDayOfMonth = Integer.toString(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "toDate";
        paramNames[1] = "reportingFrequency";

        paramValues[0] = pProcessingDate + lastDayOfMonth;
        paramValues[1] = ReportingFrequency.Monthly;

        return Application.findByNamedQuery(
                Application.getQueryName("findCurrentPeriodLedgerAccountBalance"), paramNames, paramValues);
    }

    private static Map<ReportingType, SpcfMoney> getPreviousPeriodLedgerBalance(GemsLedgerPostingRule pGemsLedgerPostingRule, String pPreviousPeriod) {
        int year = Integer.parseInt(pPreviousPeriod.substring(0, 4));
        int month = Integer.parseInt(pPreviousPeriod.substring(4, 6));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, 0);
        String lastDayOfMonth = Integer.toString(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        DomainEntitySet<LedgerAccount> ledgerAccounts = Application.find(LedgerAccount.class, LedgerAccount.ReportingFrequency().equalTo(ReportingFrequency.Monthly));

        Map<LedgerAccountCode, Map<ReportingType, SpcfMoney>> toDateBalances = new HashMap<LedgerAccountCode, Map<ReportingType, SpcfMoney>>();
        for (LedgerAccount ledgerAccount : ledgerAccounts) {
            toDateBalances.put(ledgerAccount.getLedgerAccountCd(), new HashMap<ReportingType, SpcfMoney>());
            toDateBalances.get(ledgerAccount.getLedgerAccountCd()).put(ReportingType.Tax, SpcfMoney.ZERO);
            toDateBalances.get(ledgerAccount.getLedgerAccountCd()).put(ReportingType.DirectDeposit, SpcfMoney.ZERO);
        }

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "toDate";
        paramNames[1] = "reportingFrequency";
//        paramNames[1] = "gemsPostingRule";

        paramValues[0] = pPreviousPeriod + lastDayOfMonth;
        paramValues[1] = ReportingFrequency.Monthly;
//        paramValues[1] = pGemsLedgerPostingRule;

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

        return toDateBalances.get(pGemsLedgerPostingRule.getLedgerAccount().getLedgerAccountCd());
    }

}
