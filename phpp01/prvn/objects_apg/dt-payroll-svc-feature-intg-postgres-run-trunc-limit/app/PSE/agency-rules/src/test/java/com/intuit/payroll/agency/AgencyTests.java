package com.intuit.payroll.agency;

import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.impl.RulesInfo;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * User: dweinberg
 * Date: 12/17/12
 * Time: 9:48 AM
 */
public class AgencyTests {
    @Test
    public void testAgencyAbbreviations() {

        Set<String> abbreviations = new HashSet<String>();

        RulesInfo rulesInfo = new RulesInfo();
        IRulesList activeAgencyIDList = rulesInfo.getActiveAgencyIDList();
        for (int i = 0; i < activeAgencyIDList.getCount(); i++) {
            String agencyID = (String) activeAgencyIDList.getItem(i);
            IAgency agency = rulesInfo.getAgency(agencyID);
            if (abbreviations.contains(agency.getAgencyAbbrev())) {
                fail("Non-unique agency abbreviations.");
            }
            abbreviations.add(agency.getAgencyAbbrev());
        }
    }
}
