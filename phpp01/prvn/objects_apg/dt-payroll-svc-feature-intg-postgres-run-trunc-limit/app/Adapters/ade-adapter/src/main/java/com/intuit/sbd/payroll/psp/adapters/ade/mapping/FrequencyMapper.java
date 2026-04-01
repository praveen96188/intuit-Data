package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

/**
 * User: shivanandad069
 * Date: 9/18/13
 * Time: 1:19 AM
 */
import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.schema.payroll.v3.common.FrequencyEnum;

import java.util.HashMap;
import java.util.Map;

public class FrequencyMapper {


    private static Map<FrequencyEnum, DepositFrequencyCode> frequencyToDepositFrequencyCodeConstantMap = new HashMap<FrequencyEnum, DepositFrequencyCode>();
    private static Map<DepositFrequencyCode, FrequencyEnum> depositFrequencyCodeConstantToFrequencyMap = new HashMap<DepositFrequencyCode, FrequencyEnum>();


    static {
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.ANNUAL, DepositFrequencyCode.ANNUAL);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.EARLY_FILER, DepositFrequencyCode.EARLYFILER);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.EIGHT_MONTHLY, DepositFrequencyCode.EIGHTHMONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.FIVE_BANKING_DAY, DepositFrequencyCode.FIVEBANKINGDAY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.MONTHLY, DepositFrequencyCode.MONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.MONTHLY_ACCELERATED, DepositFrequencyCode.MONTHLYACCELERATED);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.NEXT_BANKING_DAY, DepositFrequencyCode.NEXTBANKINGDAY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.QUAD_MONTHLY, DepositFrequencyCode.QUADMONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.QUARTERLY, DepositFrequencyCode.QUARTERLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.QUARTER_MONTHLY, DepositFrequencyCode.QUARTERMONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.SEMI_ANNUAL, DepositFrequencyCode.SEMIANNUAL);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.SEMI_MONTHLY, DepositFrequencyCode.SEMIMONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.SEMI_WEEKLY, DepositFrequencyCode.SEMIWEEKLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.SPLIT_MONTHLY, DepositFrequencyCode.SPLITMONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.THREE_BANKING_DAY, DepositFrequencyCode.THREEBANKINGDAY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.TWICE_MONTHLY, DepositFrequencyCode.TWICEMONTHLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.WEEKLY, DepositFrequencyCode.WEEKLY);
        frequencyToDepositFrequencyCodeConstantMap.put(FrequencyEnum.ACCELERATED, DepositFrequencyCode.ACCELERATED);

        // reverse map
        for (FrequencyEnum frequency : frequencyToDepositFrequencyCodeConstantMap.keySet()) {
            depositFrequencyCodeConstantToFrequencyMap.put(frequencyToDepositFrequencyCodeConstantMap.get(frequency), frequency);
        }
    }

    public static FrequencyEnum getComplainceFrequencyByDepositFrequencyCode(DepositFrequencyCode pPSPSDepositFrequencyCode) {
        return depositFrequencyCodeConstantToFrequencyMap.get((DepositFrequencyCode) pPSPSDepositFrequencyCode);
    }

    public static DepositFrequencyCode getPSPDepositFrequencyCodeByCDMFrequency(FrequencyEnum pCDMFrequency) {
        DepositFrequencyCode pspDepositFrequencyCode = frequencyToDepositFrequencyCodeConstantMap.get(pCDMFrequency);
        if(pspDepositFrequencyCode != null) {
            return (DepositFrequencyCode) pspDepositFrequencyCode;
        }

        return null;
    }
}
