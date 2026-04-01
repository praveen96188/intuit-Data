package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Outputs Alabama's coupon file<br>
 * Example output line:<br>
 * R00072202720110430MA60005100000000652100000000065210V0000000000000448030015
 */
public class AL_WH extends StateReportBase {

    public AL_WH() {
        reportNamesList = new String[] {"AL-CR4WH-PAYMENT"};
    }

    @Override
    public void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            // ATF creates quarterly reports.  Only do monthly reports.
            logger.info("Skipping processing of " + paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev()
                    + " for frequency " + paymentTemplateFrequency.getPaymentFrequencyId().toString());
            return;
        }

        SpcfCalendar[] dates = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency);

        SpcfCalendar startDate = dates[0];
        SpcfCalendar endDate = dates[1];

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions =
                getMoneyMovementTransactions(paymentTemplateFrequency, passedInDate);

        logger.info("Running report " + paymentTemplateFrequency.getPaymentFrequencyId().toString() + " for class " +
                getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end " + endDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " MMTs");

        boolean isFirst = true;
        StringBuilder builder = new StringBuilder();
        StringBuilder secondary = new StringBuilder();

        DomainEntitySet<Law> laws = Law.findWithholdingLawForTemplate(reportNamesList[0]);
        if (laws.size() != 1) {
            logger.fatal("Could not find law for AL Withholding.  Aborting!");
            return;
        }
        Law law = laws.get(0);

        int totalProcessed = 0;

        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            try {
                pspRequestContextManager.setRequestContextCompany(company);
                ArrayList<MoneyMovementTransaction> mmts = companyToMoneyMovementTransactions.get(company).getValueItem();
                MoneyMovementTransaction mmt = mmts.get(0);
                SpcfMoney totalPayments = getTotalPayments(mmts);
                SpcfMoney totalLiabilities = getTotalLiabilities(mmts);

                if (totalLiabilities.equals(SpcfMoney.ZERO)) {
                    // Companies with zero liabilities for the monthly period are not required to be included in the file.
                    continue;
                }

                if (totalPayments.isLessThan(SpcfMoney.ZERO) || totalLiabilities.isLessThan(SpcfMoney.ZERO)) {
                    logger.error("Company " + company.getSourceCompanyId() + " has a total payment or total liability that is negative, skipping.  Total Payments:" +
                            totalPayments.toString() + " Total Liabilities:" + totalLiabilities.toString());
                    continue;
                }

                String stateTaxId = prepareStateAgencyId(mmt, 10);

                if (stateTaxId == null) {
                    logger.error("Company " + company.getSourceCompanyId() + " state tax id too long.  The id is \"" +
                            mmt.getAgencyTaxpayerId() + "\".  Skipping output");
                    continue;
                }

                totalProcessed++;

                String totalPaymentsFormatted = getPaddedMoney(totalPayments, 10, 2, true);
                String totalLiabilitiesFormatted = getPaddedMoney(totalLiabilities, 10, 2, true);
                String zeroValue = getPaddedMoney(SpcfMoney.ZERO, 10, 2, true);

                StringBuilder temp = new StringBuilder();

                if (!isFirst) {
                    builder.append("\n");
                    secondary.append("\n");
                }

                // 1. State Tax Id number
                temp.append(stateTaxId).append(CSV_SEPARATOR);
                // 2. YYYYMMDD Period End
                temp.append(endDate.format("yyyyMMdd")).append(CSV_SEPARATOR);
                // 3. Period (M "Monthly"/Q "Quarterly")
                temp.append("M").append(CSV_SEPARATOR);
                // 4. Return type (A6 Monthly/ A1 Quarterly)
                temp.append("A6").append(CSV_SEPARATOR);
                // 5. Number of employees create list of employees that were not voided and paid during this time
                int employees = Paycheck.findEmployeeCount(company, startDate, endDate, law);
                temp.append(getPaddedWholeNumber(employees, 5)).append(CSV_SEPARATOR);
                // 6. Tax amount withheld
                temp.append(totalLiabilitiesFormatted).append(CSV_SEPARATOR);
                // 7. Tax remitted first 2 months only required
                temp.append(cropOrPad("", 13)).append(CSV_SEPARATOR);
                // 8. Credit for overpayment
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 9. Tax amount due
                temp.append(totalPaymentsFormatted).append(CSV_SEPARATOR);
                // 10. Final return default "N"
                temp.append("N").append(CSV_SEPARATOR);
                // 11. Final return Last payment
                temp.append(cropOrPad("", 8)).append(CSV_SEPARATOR);
                // 12. Prior Year Credit
                temp.append(cropOrPad("", 4)).append(CSV_SEPARATOR);
                // 13. Prior Year withheld amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 14. Prior Year paid Amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 15. Qtr 1 Current Year Credit
                temp.append(cropOrPad("", 6)).append(CSV_SEPARATOR);
                // 16. Qtr 1 Current year withheld amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 17. Qtr 1 Current year paid amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 18. Qtr 2 Current Year Credit
                temp.append(cropOrPad("", 6)).append(CSV_SEPARATOR);
                // 19. Qtr 2 Current year withheld amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 20. Qtr 2 Current year paid amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 21. Qtr 3 Current Year Credit
                temp.append(cropOrPad("", 6)).append(CSV_SEPARATOR);
                // 22. Qtr 3 Current year withheld amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 23. Qtr 3 Current year paid amt
                temp.append(zeroValue).append(CSV_SEPARATOR);
                // 24. Payment Method (E "EFT DEBIT"/V "EFT CREDIT"/Z "ZERO TAX DUE")
                temp.append("V").append(CSV_SEPARATOR);
                // 25. Funding Source
                temp.append("N").append(CSV_SEPARATOR);
                // 26. Address
                temp.append(cropOrPad("", 35)).append(CSV_SEPARATOR);
                // 27. City
                temp.append(cropOrPad("", 33)).append(CSV_SEPARATOR);
                // 28. State
                temp.append(cropOrPad("", 2)).append(CSV_SEPARATOR);
                // 29. Zip Code
                temp.append(cropOrPad("", 5)).append(CSV_SEPARATOR);
                // 30. Zip Plus 4
                temp.append(cropOrPad("", 4)).append(CSV_SEPARATOR);
                // 31. Payment Date
                temp.append(cropOrPad("", 8)).append(CSV_SEPARATOR);
                // 32. Bank Acct Type
                temp.append(cropOrPad("", 1)).append(CSV_SEPARATOR);
                // 33. Bank routing number
                temp.append(cropOrPad("", 9)).append(CSV_SEPARATOR);
                // 34. Bank acct number
                temp.append(cropOrPad("", 18));

                builder.append(temp);

                //PSP-20545  creating secondary report with PSID and Company Name
                secondary.append(company.getSourceCompanyId()).append(CSV_SEPARATOR);
                secondary.append(company.getLegalName()).append(CSV_SEPARATOR);
                secondary.append(temp);

                isFirst = false;
            }finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }


        saveStateCoupon(builder, startDate, endDate, StateReportType.Recon, paymentTemplateFrequency);
        BatchUtils.createStateReportEmail(builder, ".csv", totalProcessed, startDate, endDate, StateReportType.Recon, paymentTemplateFrequency);
        BatchUtils.createStateReportEmail(secondary, ".csv", totalProcessed, startDate, endDate, StateReportType.Recon, paymentTemplateFrequency);
    }

    /**
     * returns int value of day of initiationDate
     * @return int value of day of initiationDate
     */
    private int getDayOfInitiationDate(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {

        SpcfCalendar[] dates = getStartEndDateByPassedInDate(passedInDate, 3);

        SpcfCalendar startDate = dates[0];

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = getMMTsByInitiationDate(paymentTemplateFrequency, passedInDate);

        if (moneyMovementTransactions.size() == 0) {
            logger.info("Will not run the report for class AL_WH today as no transactions present for initiation date: " + startDate.format("yyyy/MM/dd"));
            return 0;
        } else {
            return moneyMovementTransactions.getFirst().getInitiationDate().getDay();
        }
    }


    /**
     * returns all MMTs baseed on initiationDate and PaymentTemplateFrequency
     * @return all MMTs baseed on initiationDate and PaymentTemplateFrequency
     */
    public DomainEntitySet<MoneyMovementTransaction> getMMTsByInitiationDate(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {

        SpcfCalendar[] dates = getStartEndDateByPassedInDate(passedInDate, 3);

        SpcfCalendar startDate = dates[0];
        SpcfCalendar endDate = dates[1];

        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.InitiationDate().between(startDate, endDate)
                .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHCredit, PaymentMethod.CheckPayment, PaymentMethod.ACHDirectDeposit))
                .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.Ignore))
                .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplateFrequency.getPaymentTemplate()));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                        .OrderBy(MoneyMovementTransaction.InitiationDate().Descending())
                        .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));

        logger.info("Retrieved " + moneyMovementTransactions.size() + " MMTs");

        return moneyMovementTransactions;
    }

    /**
     * returns int value of day of initiationDate
     *
     * @return int value of day of initiationDate
     */
    public HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> getMoneyMovementTransactions(PaymentTemplateFrequency paymentTemplateFrequency,
                                                                                                                              SpcfCalendar passedInDate) {
        SpcfCalendar[] dates = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency);

        SpcfCalendar endDate = dates[1];

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = getMMTsByInitiationDate(paymentTemplateFrequency, passedInDate);

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions = new
                HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>>();
        HashSet<Company> badCompanies = new HashSet<Company>();

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            Company theCompany = moneyMovementTransaction.getCompany();
            if (!badCompanies.contains(theCompany)) {
                if (companyToMoneyMovementTransactions.containsKey(theCompany)) {
                    companyToMoneyMovementTransactions.get(theCompany).getValueItem().add(moneyMovementTransaction);
                } else {
                    EffectiveDepositFrequency companyEffectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(theCompany, paymentTemplateFrequency.getPaymentTemplate(), endDate);
                    if (companyEffectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId() != paymentTemplateFrequency.getPaymentFrequencyId()) {
                        badCompanies.add(theCompany);
                    } else {
                        SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>> pair = new SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>();
                        pair.setKeyItem(paymentTemplateFrequency.getPaymentFrequencyId());
                        pair.setValueItem(new ArrayList<MoneyMovementTransaction>());
                        pair.getValueItem().add(moneyMovementTransaction);
                        companyToMoneyMovementTransactions.put(theCompany, pair);
                    }
                }
            }
        }

        return companyToMoneyMovementTransactions;
    }

    @Override
    public boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        int day = getDayOfInitiationDate(paymentTemplateFrequency, passedInDate);
        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) || day == 0) {
            // ATF creates quarterly reports.  Only do monthly reports.
            return false;
        } else {
            return checkDay(paymentTemplateFrequency, day, 3, true);
        }
    }
}
