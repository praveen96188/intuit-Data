/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/SourcePayrollParameterTranslator.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAutoLimitIncreaseTier;
import com.intuit.sbd.payroll.psp.api.dtos.DDAutoLimitIncreaseTierDTO;
import com.intuit.sbd.payroll.psp.domain.AutoLimitIncreaseTier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SourcePayrollParameterTranslator - Translator class for retrieving SAP DTOs from PSP core domain entities for
 *      source payroll parameters
 *
 * @author Joe Warmelink
 */
public class SourcePayrollParameterTranslator {


    public static List<SAPAutoLimitIncreaseTier> getSAPAutoLimitIncreaseTiers(List<AutoLimitIncreaseTier> tiers) {
        ArrayList<SAPAutoLimitIncreaseTier> sapTiers = new ArrayList<SAPAutoLimitIncreaseTier>(tiers.size());
        for (AutoLimitIncreaseTier tier : tiers) {
            SAPAutoLimitIncreaseTier sapTier = new SAPAutoLimitIncreaseTier();
            sapTier.setLevel(tier.getLevel());
            sapTier.setSourceSystemCd(tier.getSourceSystemCd().toString());
            sapTier.setPayrollsRun(Integer.toString(tier.getPayrollsRun()));
            sapTier.setDaysSinceFirstPayroll(Integer.toString(tier.getDaysSinceFirstPayroll()));
            sapTier.setIncreaseMultiplier(String.format("%.2f", new BigDecimal(tier.getIncreaseMultiplier().toString()).doubleValue()));
            sapTier.setCompanyCap(Integer.toString(new BigDecimal(tier.getCompanyCap().toString()).intValue()));
            sapTier.setEmployeeCap(Integer.toString(new BigDecimal(tier.getPayeeCap().toString()).intValue()));
            sapTiers.add(sapTier);
        }
        return sapTiers;
    }

    public static DDAutoLimitIncreaseTierDTO[] getDDAutoLimitIncreaseTierDTO(List<SAPAutoLimitIncreaseTier> sapTiers) {
        DDAutoLimitIncreaseTierDTO[] autoLimitIncreaseTierDTOs = new DDAutoLimitIncreaseTierDTO[sapTiers.size()];

        for (int i = 0; i < sapTiers.size(); i++) {
            SAPAutoLimitIncreaseTier sapTier = sapTiers.get(i);
            autoLimitIncreaseTierDTOs[i] = new DDAutoLimitIncreaseTierDTO();
            autoLimitIncreaseTierDTOs[i].setSourceSystemCd(sapTier.getSourceSystemCd());
            autoLimitIncreaseTierDTOs[i].setLevel(sapTier.getLevel());
            autoLimitIncreaseTierDTOs[i].setPayrollsRun(sapTier.getPayrollsRun());
            autoLimitIncreaseTierDTOs[i].setDaysSinceFirstPayroll(sapTier.getDaysSinceFirstPayroll());
            autoLimitIncreaseTierDTOs[i].setIncreaseMultiplier(sapTier.getIncreaseMultiplier());
            autoLimitIncreaseTierDTOs[i].setCompanyCap(sapTier.getCompanyCap());
            autoLimitIncreaseTierDTOs[i].setEmployeeCap(sapTier.getEmployeeCap());
        }

        return autoLimitIncreaseTierDTOs;
    }
}