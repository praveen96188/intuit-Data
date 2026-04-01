package com.intuit.sbd.payroll.psp.adapters.ade.json;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.AdeLawMap;

import java.util.HashMap;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 2/5/13
 */
public class TaxItemLawMap {

    public static final String BASE_RATE_NAME = "uiRate";

    private static Map<String, Map<String, String>> mapping = null;

    private static void initialize() {
        mapping = new HashMap<String, Map<String, String>>();

        // Find all of the ADE Law Map objects.
        DomainEntitySet<AdeLawMap> adeLaws = Application.findObjects(AdeLawMap.class);
        for (AdeLawMap adeLaw : adeLaws) {

            // Determine the state from the parent law and template.
            String templateName = adeLaw.getLaw().getPaymentTemplate().getPaymentTemplateCd();
            String state = templateName.substring(0,2);

            // Find/Create the map of laws for this state.
            Map<String, String> lawMap = mapping.get(state);
            if (lawMap == null) {
                mapping.put(state, new HashMap<String, String>());
                lawMap = mapping.get(state);
            }

            // If this law is combined with a different law, store that law id.
            if (adeLaw.getAdeLawMap() != null) {
                lawMap.put(adeLaw.getAdeName(), adeLaw.getAdeLawMap().getLaw().getLawId());
            } else {
                lawMap.put(adeLaw.getAdeName(), adeLaw.getLaw().getLawId());
            }
        }

    }

    public synchronized static String getLawId( String state, String itemName ) {

        if (mapping == null) {
            initialize();
        }

        Map<String, String> stateLaws = mapping.get(state);
        if (stateLaws != null) {
            return stateLaws.get(itemName);
        }

        return null;
    }
}
