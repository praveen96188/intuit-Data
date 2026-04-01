package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Outputs Massachusetts's coupon file
 *
 * Example output lines:<br>
 * Monthly:<br>
 * M00000103312011010462625  STATION CLASS CONSTRUCTORS    Y0000000003000000010398000000000000 000000000000000000010398 000000010398000000000000000000010398000000000000  NN                                                                                                               E<br>
 * Quarterly:<br>
 * Q00000103312011010558853  RILEY BROTHERS, INC.          Y0000000142000006928589000006928589 000000000000000006928589 000000000000000000000000000000000000000000000000  NN                                                                                                               E<br>
 */
public class MA_WH extends StateReportBase {
    public MA_WH() {
        reportNamesList = new String[] {"MA-M941-PAYMENT"};
    }

    @Override
    public void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERMONTHLY)) {
            // Skip QUARTERMONTHLY as it is done along with quarterly
            return;
        } else if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) &&
                !paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            // Unimplemented frequency, skip
            logger.info("Skipping processing of " + paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev()
                    + " for frequency " + paymentTemplateFrequency.getPaymentFrequencyId().toString());
            return;
        }

        SpcfCalendar[] dates = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency);

        SpcfCalendar startDate = dates[0];
        SpcfCalendar endDate = dates[1];

        StringBuilder individualRecords = new StringBuilder();

        ArrayList<PaymentTemplateFrequency> frequencies = new ArrayList<PaymentTemplateFrequency>();
        frequencies.add(paymentTemplateFrequency);

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions =
                getMoneyMovementTransactions(paymentTemplateFrequency, startDate, endDate);

        logger.info("Running report " + paymentTemplateFrequency.getPaymentFrequencyId().toString() + " for class " +
                getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end " + endDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " MMTs");

        int sequenceNumber = createIndividualRecords(companyToMoneyMovementTransactions, individualRecords, startDate, endDate, 1);

        if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            // Quarterly file includes companies with a QUARTERMONTHLY deposit frequency
            PaymentTemplateFrequency weeklyMA = PaymentTemplateFrequency.getPaymentTemplateFrequency(reportNamesList[0],
                    DepositFrequencyCode.QUARTERMONTHLY);
            HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> weeklyCompanyToMoneyMovementTransactions =
                getMoneyMovementTransactions(weeklyMA, startDate, endDate);

            sequenceNumber = createIndividualRecords(weeklyCompanyToMoneyMovementTransactions, individualRecords, startDate, endDate, sequenceNumber);
            frequencies.add(weeklyMA);

            logger.info("Running report " + weeklyMA.getPaymentFrequencyId().toString() + " for class " +
                getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end " + endDate.format("yyyy/MM/dd") +
                " with " + weeklyCompanyToMoneyMovementTransactions.size() + " MMTs");
        }

        StringBuilder headerBuilder = createHeaderRecord(sequenceNumber);

        // Append the individual reports
        headerBuilder.append(individualRecords);

        PaymentTemplateFrequency[] frequenciesArray = frequencies.toArray(new PaymentTemplateFrequency[frequencies.size()]);
        saveStateCoupon(headerBuilder, startDate, endDate, StateReportType.Recon, frequenciesArray);
        BatchUtils.createStateReportEmail(headerBuilder, sequenceNumber -1, startDate, endDate, StateReportType.Recon, frequenciesArray); //Total Records processed = SequenceNumber - 1; as sequence Number is for next record.
    }

    /**
     * Creates the MA header
     * @param sequenceNumber The number of individual records output
     * @return The MA header
     */
    private StringBuilder createHeaderRecord(int sequenceNumber) {
        StringBuilder headerBuilder = new StringBuilder();

        // File Identifier
        headerBuilder.append("MA941X");
        // Total return count
        // Subtract one because sequenceNumber starts at 1
        headerBuilder.append(getPaddedWholeNumber((sequenceNumber - 1), 6));
        // Transmitter FEIN
        SourcePayrollParameter einParm = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterFEIN);
        String einStr = einParm.getParameterValue();
        headerBuilder.append(einStr);
        // Transmitter Name
        SourcePayrollParameter nameParm = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterName);
        String nameStr = nameParm.getParameterValue();
        headerBuilder.append(cropOrPad(nameStr, 30));
        // Transmitter Address
        SourcePayrollParameter addressParm = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterAddress);
        String addressStr = addressParm.getParameterValue();
        headerBuilder.append(cropOrPad(addressStr, 30));
        // Transmitter City
        SourcePayrollParameter cityParm = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterCity);
        String cityStr = cityParm.getParameterValue();
        headerBuilder.append(cropOrPad(cityStr, 30));
        // Transmitter State
        SourcePayrollParameter stateParm = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterState);
        String stateStr = stateParm.getParameterValue();
        headerBuilder.append(stateStr);
        // Transmitter Zip Code
        SourcePayrollParameter zipParm = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterZip);
        String zipStr = zipParm.getParameterValue();
        headerBuilder.append(zipStr);
        // Transmitter Zip Extension
        // It's 5 digits according to the spec even though it is 4 digits for the Post Office
        headerBuilder.append("     ");
        // 157 blank reserved characters
        headerBuilder.append("                                                                                                                                                             \n");
        return headerBuilder;
    }

    /**
     * Creates the individual records for the MoneyMovementTransaction
     * @param companyToMoneyMovementTransactions The MoneyMovementTransactions to output
     * @param builder The StringBuilder object to put the individual records
     * @param sequenceNumber The sequence number to start at
     * @return The number of individual records output
     */
    private int createIndividualRecords(HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions,
            StringBuilder builder, SpcfCalendar startDate, SpcfCalendar endDate, int sequenceNumber) {
        boolean isFirst = sequenceNumber == 1;

        DomainEntitySet<Law> laws = Law.findWithholdingLawForTemplate(reportNamesList[0]);
        if (laws.size() != 1) {
            logger.fatal("Report=MA_Report_Error NoLaw. Could not find law for MA Withholding.  Aborting!");
            return sequenceNumber;
        }
        Law law = laws.get(0);

        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            try {
                pspRequestContextManager.setRequestContextCompany(company);
                ArrayList<MoneyMovementTransaction> mmts = companyToMoneyMovementTransactions.get(company).getValueItem();
                DepositFrequencyCode companyFreq = companyToMoneyMovementTransactions.get(company).getKeyItem();
                MoneyMovementTransaction mmt = mmts.get(0);
                SpcfMoney totalPayments = getTotalPayments(mmts);
                SpcfMoney totalLiabilities = getTotalLiabilities(mmts);

                if (totalPayments.isLessThan(SpcfMoney.ZERO) || totalLiabilities.isLessThan(SpcfMoney.ZERO)) {
                    logger.error("Report=MA_Report_Error " + company.getSourceCompanyId() + "negetiveValues_totalPayments:" + totalPayments + "_liabilities:" + totalLiabilities + " Company has a total payment or total liability that is negative, skipping.  Total Payments:"
                            + totalPayments.toString() + " Total Liabilities:" + totalLiabilities.toString());
                    continue;
                }

                String ein = padAndSizeCheck(mmt.getCompany().getFedTaxId(), 9);

                if (ein == null) {
                    logger.error("Report=MA_Report_Error " + company.getSourceCompanyId() + "einIsNull .EIN is too long. Skipping output");
                    continue;
                }

                // Comment from AS400 code
                // If there is an adjustment entered from screen pgm, then the amount of tax is reduced by the adjustment,
                // but not to less than zero.  If the adjustment is greater than the payment due then the difference is the
                // Over Payed amount.
                SpcfMoney totalTaxWithheld = SpcfMoney.ZERO;
                SpcfMoney totalPayment;
                SpcfMoney priorPeriodAdjustment = SpcfMoney.ZERO;
                SpcfMoney totalTax = SpcfMoney.ZERO;
                SpcfMoney amountDue;
                SpcfMoney previousOverpayment = SpcfMoney.ZERO;
                SpcfMoney balanceDue;
                SpcfMoney overpayment = SpcfMoney.ZERO;
                String creditOrRefund = " ";

                if (companyFreq.equals(DepositFrequencyCode.QUARTERLY)
                        || companyFreq.equals(DepositFrequencyCode.QUARTERMONTHLY)) {
                    // Total Payment field is a total of all payments and refunds
                    totalPayment = totalPayments;
                    //If some ATO is created that should be subtracted from totalLiabilities
                    overpayment = getAgencyTaxRefund(startDate, company);
                    totalTaxWithheld = (SpcfMoney) totalLiabilities.subtract(overpayment);
                    //amountDue=totalTax less payment
                    amountDue = (SpcfMoney) totalTaxWithheld.subtract(totalPayment);
                    //since previousOverPayment will be always zero so balanceDue=amountDue
                    if (amountDue.isGreaterThan(SpcfMoney.ZERO)) {
                        balanceDue = amountDue;
                    } else {
                        balanceDue = SpcfMoney.ZERO;
                    }
                    creditOrRefund = amountDue.equals(SpcfMoney.ZERO) ? " " : amountDue.getSign() == -1 ? "R" : "C";
                } else {
                    // Monthly
                    // Always zero for monthly
                    totalPayment = SpcfMoney.ZERO;
                    // Amount Due field is the total of all liabilities
                    amountDue = totalLiabilities;
                    balanceDue = totalLiabilities;
                    totalTaxWithheld = totalLiabilities;
                }
                totalTax = totalTaxWithheld;
                if (!isFirst) {
                    builder.append("\n");
                }

                // Record identifier (D- QUARTERMONTHLY/M - Monthly/Q - Quarterly/D - Depository Quarterly/A - Annual
                if (companyFreq.equals(DepositFrequencyCode.QUARTERLY)) {
                    builder.append("Q");
                } else if (companyFreq.equals(DepositFrequencyCode.QUARTERMONTHLY)) {
                    builder.append("D");
                } else if (companyFreq.equals(DepositFrequencyCode.ANNUAL)) {
                    builder.append("A");
                } else {
                    builder.append("M");
                }
                // Sequence number - incrementing number starting at 1
                builder.append(getPaddedWholeNumber(sequenceNumber, 6));
                // Period end date (MMDDYYYY)
                builder.append(endDate.format("MMddyyyy"));
                // FEIN
                builder.append(ein);
                // Filing entity code leave blank
                builder.append("  ");
                // Business name
                builder.append(cropOrPad(company.getLegalName(), 30));
                // Return record
                builder.append("Y");
                // Number of employees
                int employees = Paycheck.findEmployeeCount(company, startDate, endDate, law);
                builder.append(getPaddedWholeNumber(employees, 10));
                // Total tax withheld
                builder.append(getPaddedMoney(totalTaxWithheld, 10, 2));
                // Total payment
                builder.append(getPaddedMoney(totalPayment, 10, 2));
                // Prior period adjustment - "SDDDDDDDDDDCC" Use minus sign if amount is negative
                builder.append(getPaddedMoneyWithSign(priorPeriodAdjustment, 10, 2));
                // Total tax (totaltaxwithheld - adjustment)
                builder.append(getPaddedMoney(totalTax, 10, 2));
                // Amount due - "SDDDDDDDDDDCC" Use minus sign if amount is negative
                builder.append(getPaddedMoneyWithSign(amountDue, 10, 2));
                // Previous overpayment
                builder.append(getPaddedMoney(previousOverpayment, 10, 2));
                // Balance due
                builder.append(getPaddedMoney(balanceDue, 10, 2));
                // Overpayment
                builder.append(getPaddedMoney(overpayment, 10, 2));
                // Credit or Refund, space for n/a
                builder.append(creditOrRefund);
                // Amended, not implemented by MA, use space
                builder.append(" ");
                // Final return (Y/N) - Always a no
                builder.append("N");
                // Payment record (Y/N) - Always a no
                builder.append("N");
                // Various fields related to payment information.  We are paying via ACH so ignore by filling with 111 spaces
                builder.append("                                                                                                               ");

                sequenceNumber++;
                isFirst = false;
                if (!totalTaxWithheld.add(overpayment).equals(totalPayment)) {
                    logger.warn("Report=MA_Report_Error " + company.getSourceCompanyId() + "_" + SpcfCalendar.getNow() + "_overpayment:" + overpayment + "_totalPayment:" + totalPayment);
                }
            }finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        return sequenceNumber;
    }

    private SpcfMoney getAgencyTaxRefund(SpcfCalendar pStartDate, Company pCompany) {
        Map<Law, SpcfMoney> lawVsMoneyMap = LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, PaymentTemplate.findPaymentTemplate("MA-M941-PAYMENT"), pCompany, pStartDate);
        SpcfMoney totalMoney = SpcfMoney.ZERO;
        for (SpcfMoney money : lawVsMoneyMap.values()) {
            totalMoney = (SpcfMoney) totalMoney.add(money);
        }
        return totalMoney;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        // Skip weekly (QUARTERMONTHLY) as it is done along with quarterly, Generate report only for MONTHLY and QUARTERLY

        if(paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) || paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            SpcfCalendar endDate = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency)[1]; // this will give previous period end date

            if(paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
                if(endDate.getMonth() == CalendarUtils.getLastDayOfQuarter(endDate).getMonth()) {
                    return checkDay(paymentTemplateFrequency, passedInDate.getDaysInMonth(), 3, true);
                } else {
                    return checkDay(paymentTemplateFrequency, 15, 3, true);
                }
            } else if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY) && passedInDate.getMonth() == (endDate.getMonth()+1) % 12 ){
                return checkDay(paymentTemplateFrequency, passedInDate.getDaysInMonth(), 3, true);
            }
        }
        return false;
    }

    /**
     * Gets the total payments for all MoneyMovementTransaction passed in
     *
     * @param moneyMovementTransactions The MoneyMovementTransactions to total
     * @return The total payments for all MoneyMovementTransaction passed in
     */
    public SpcfMoney getTotalPayments(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        SpcfMoney total = SpcfMoney.ZERO;

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            if (moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.SentToAgency) || moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.AcknowledgedByAgency)) {
                total = (SpcfMoney) total.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());
            }
        }

        return total;
    }
}
