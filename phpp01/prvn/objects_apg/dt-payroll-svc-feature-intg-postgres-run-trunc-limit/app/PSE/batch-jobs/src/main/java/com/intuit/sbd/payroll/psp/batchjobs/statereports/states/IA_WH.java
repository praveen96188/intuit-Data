package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Outputs Iowa's coupon file<br>
 *
 * Example output line:<br>
 * 1008410120147172300104/15/20110000019010000000000000000190100000000000000000000000000190100A
 */
public class IA_WH extends StateReportBase {
    public IA_WH() {
        reportNamesList = new String[] {"IA-44105-PAYMENT"};
    }

    @Override
    public void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) &&
                !paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIMONTHLY)) {
            // ATF creates quarterly reports.  Only do monthly and semiweekly reports.
            logger.info("Skipping processing of " + paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev()
                    + " for frequency " + paymentTemplateFrequency.getPaymentFrequencyId().toString());
            return;
        }

        SpcfCalendar[] dates = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency);

        SpcfCalendar startDate = dates[0];
        SpcfCalendar endDate = dates[1];

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions =
                getMoneyMovementTransactions(paymentTemplateFrequency, startDate, endDate);

        logger.info("Running report " + paymentTemplateFrequency.getPaymentFrequencyId().toString() + " for class " +
                getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end " + endDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " MMTs");

        boolean isFirst = true;
        StringBuilder builder = new StringBuilder();

        String zeroFormatted = "         0";
        String tenSpaces = "          ";

        int totalProcessed = 0;

        HashMap<String, String> iaBENNumber = getIABENNumber();

        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            try {
                pspRequestContextManager.setRequestContextCompany(company);
                ArrayList<MoneyMovementTransaction> mmts = companyToMoneyMovementTransactions.get(company).getValueItem();
                MoneyMovementTransaction mmt = mmts.get(0);
                SpcfMoney totalPayments = getTotalPayments(mmts);
                SpcfMoney totalLiabilities = getTotalLiabilities(mmts);

                if (totalPayments.isLessThan(SpcfMoney.ZERO) || totalLiabilities.isLessThan(SpcfMoney.ZERO)) {
                    logger.error("Company " + company.getSourceCompanyId() + " has a total payment or total liability that is negative, skipping.  Total Payments:" +
                            totalPayments.toString() + " Total Liabilities:" + totalLiabilities.toString());
                    continue;
                }

                String businessEfileNumber = iaBENNumber.get(company.getSourceCompanyId());

                if (businessEfileNumber == null) {
                    logger.warn("Company " + company.getSourceCompanyId() + " IA BEN Number wrong.  Skipping output");
                    continue;
                }

                String stateTaxpayerId = prepareStateAgencyId(mmt, 12);

                if (stateTaxpayerId == null) {
                    logger.error("Company " + company.getSourceCompanyId() + " state tax id too long.  The state tax id is \"" +
                            mmt.getAgencyTaxpayerId() + "\".  Skipping output");
                    continue;
                }

                totalProcessed++;

                if (!isFirst) {
                    builder.append("\n");
                }

                // Client business efile number - 8 chars - IOWA bin number from payment template
                builder.append(businessEfileNumber);
                // Client permit number - state taxpayer id
                builder.append(stateTaxpayerId);
                // Period end date (MM/DD/YYYY)
                builder.append(endDate.format("MM/dd/yyyy"));
                // Withholding this period
                builder.append(getPaddedMoney(totalLiabilities, 9, 0));
                // Less credits due
                builder.append(zeroFormatted);
                // Balance due
                builder.append(getPaddedMoney(totalLiabilities, 10, 0));
                // Penalty
                builder.append(tenSpaces);
                // Interest
                builder.append(tenSpaces);
                // Total amount due
                builder.append(getPaddedMoney(totalLiabilities, 10, 2));
                // Payment method (Z for zero payment/A for anything else)
                String paymentMethod;

                if (totalLiabilities.equals(SpcfMoney.ZERO)) {
                    paymentMethod = "Z";
                } else {
                    paymentMethod = "A";
                }
                builder.append(paymentMethod);

                isFirst = false;
            }finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        saveStateCoupon(builder, startDate, endDate, StateReportType.Recon, paymentTemplateFrequency);
        BatchUtils.createStateReportEmail(builder, totalProcessed, startDate, endDate, StateReportType.Recon, paymentTemplateFrequency);
    }

    @Override
    public boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) &&
                !paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIMONTHLY)) {
            // ATF creates quarterly reports.  Only do monthly and semiweekly reports.
            return false;
        }

        boolean isScheduled;

        if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            isScheduled = checkDay(paymentTemplateFrequency, 15, 3, true);
        } else {
            // Schedules for semimonthly
            isScheduled = checkDay(paymentTemplateFrequency, 10, 3, true) || checkDay(paymentTemplateFrequency, 25, 3, true);
        }

        return isScheduled;
    }
}
