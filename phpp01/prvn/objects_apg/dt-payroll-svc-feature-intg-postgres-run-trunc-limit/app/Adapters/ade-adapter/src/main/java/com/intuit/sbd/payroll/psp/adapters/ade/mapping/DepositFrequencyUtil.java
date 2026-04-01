package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 1/28/14
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class DepositFrequencyUtil {

    /**
     * @param df
     * @return weight value based order of DF.Weight will be assinged in ascednig order of DF
     *         order of the deposit frequencies as  below
     *         NEXTBANKINGDAY
     *         THREEBANKINGDAY
     *         FIVEBANKINGDAY
     *         SEMIWEEKLY
     *         EIGHTHMONTHLY
     *         WEEKLY
     *         QUARTERMONTHLY
     *           QUADMONTHLY
     *         SEMIMONTHLY
     *         TWICEMONTHLY
     *         SPLITMONTHLY
     *         MONTHLYACCELERATED
     *         MONTHLY
     *         ACCELERATED
     *         QUARTERLY
     *         SEMIANNUAL
     *         EARLYFILER
     *         ANNUAL
     */
    public static int getOrderOfDepositFrequencyCode(DepositFrequencyCode df) {
        int weight = 100;
        switch (df) {
            case NEXTBANKINGDAY:
                weight = 0;
                break;

            case THREEBANKINGDAY:
                weight = 1;
                break;
            case FIVEBANKINGDAY:
                weight = 2;
                break;
            case SEMIWEEKLY:
                weight = 3;
                break;
            case EIGHTHMONTHLY:
                weight = 4;
                break;
            case WEEKLY:
                weight = 5;
                break;
            case QUARTERMONTHLY:
                weight = 6;
                break;
            case QUADMONTHLY:
                weight = 7;
                break;
            case SEMIMONTHLY:
                weight = 8;
                break;
            case TWICEMONTHLY:
                weight = 9;
                break;
            case SPLITMONTHLY:
                weight = 10;
                break;
            case MONTHLYACCELERATED:
                weight = 11;
                break;
            case MONTHLY:
                weight = 12;
                break;
            case QUARTERLY:
                weight = 13;
                break;
            case SEMIANNUAL:
                weight = 14;
                break;
            case EARLYFILER:
                weight = 15;
                break;
            case ANNUAL:
                weight = 16;
                break;
            case ACCELERATED:
                weight = 17;
                break;
            default:
                weight = 18;
                break;
        }
        return weight;
    }

    /**
     *
     * @param currrentDf
     * @param newDF
     * @return
     */
    public static int compareDepositFrequencyCodes(DepositFrequencyCode newDF, DepositFrequencyCode currrentDf) {
        if (currrentDf == null && newDF == null) {
            return 0;
        }
        //In this case dont check for threshold. So returning positive value
        if (currrentDf == null) {
            return getOrderOfDepositFrequencyCode(newDF);
        }
        //In this case dont check for threshold. So returning positive value
        if (newDF == null) {
            return  getOrderOfDepositFrequencyCode(currrentDf);
        }
        return getOrderOfDepositFrequencyCode(currrentDf) - getOrderOfDepositFrequencyCode(newDF);

    }
}
