package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.LawCategoryCode;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 2/4/14
 * Time: 12:23 AM
 * To change this template use File | Settings | File Templates.
 * This is added to get the base law rate only. Since supplement laws are sent as part of payload,we dont need to add them here
 */
public class AdeCepDefaultRateConverter implements IRateConverter {

    // Exclude the following Law IDs from having a default 0.0% rate created when they are not specified in the JSON file.
    private static String[] excludeDefaultZeroRateLawIds = {"168", "170", "174", "176"};

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Pre-load the map with 0.0% rates for all SUI laws for this state.
        DomainEntitySet<Law> laws = Application.find(Law.class, Law.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI)
                                                                   .And(Law.PaymentTemplate().PaymentTemplateCd().like(state + "-%"))
                                                                   .And(Law.LawCategoryCode().in(LawCategoryCode.UnemploymentEmployer))
                                                                   .And(Law.LawId().notIn(excludeDefaultZeroRateLawIds))
                                                                   .And(Law.LawId().notIn(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE)));
        for (Law law : laws) {
            rates.put(law, BigDecimal.ZERO);
        }

        // Store the base rate.
        Law law;
        try {
            law = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        } catch (Exception ex) {
            law = laws.findEntity(Law.LawCategoryCode().in(LawCategoryCode.UnemploymentEmployer));
        }
        rates.put(law, baseRate);


        return rates;
    }

    public static String[] getExcludeDefaultZeroRateLawIds() {
        return excludeDefaultZeroRateLawIds;
    }
}
