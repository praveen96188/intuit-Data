package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: ankitaa186
 * Date: 9/30/13
 * Time: 10:29 PM
 * <p/>
 * Tests for business login written in AgencyIdRequirement
 */
public class AgencyIdRequirementTests {

    @Test
    public void testDefaultRequirementFunctionLogic() {
        PayrollServices.beginUnitOfWork();
        AgencyIdRequirement aidRequirement = new AgencyIdRequirement();
        aidRequirement.setPattern("\\d{3}-\\d{3}-\\d{3}");
        aidRequirement.setExample("123-456-789");
        aidRequirement.setRequired(true);
        //Test valid non default id
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertTrue(aidRequirement.isNotADefaultId("122-454-567", "12-3456789"));
        //Test id in sequence
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("123-456-789", "12-3456789"));
        //Test id in repetition
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("111-111-111", "12-3456789"));
        //Test id is alphanumeric
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertTrue(aidRequirement.isNotADefaultId("111-111-ABC", "12-3456789"));
        //Test id start at 2 and in sequence, it should be invalid
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("234-567-890", "12-3456789"));
        //Test id matches fed id
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("122-456-789", "12-2456789"));
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.MustNotFollowFedTaxId);
        assertFalse(aidRequirement.isNotADefaultId("122-456-789", "12-2456789"));
        //Test id matches fed id but requirement states it should
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.MustFollowFedTaxId);
        assertTrue(aidRequirement.isNotADefaultId("122-456-789", "12-2456789"));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDefaultRequirementFunctionLogicForMN() {
        PayrollServices.beginUnitOfWork();
        AgencyIdRequirement aidRequirement = new AgencyIdRequirement();
        //Pattern for MN-DEED1-PAYMENT ^\d{8}-?\d{4}$|^\d{8}$
        //Example 99999999-9999 or 99999999
        aidRequirement.setPattern("^\\d{8}-?\\d{4}$|^\\d{8}$");
        aidRequirement.setExample("99999999-9999");
        aidRequirement.setRequired(true);

        String aid1 = "12245994-8567";
        String aid2 = "59912244";
        String fedtaxid = "12-3456789";

        assertTrue(aidRequirement.matchesPattern(aid1));
        assertTrue(aidRequirement.matchesPattern(aid2));
        assertFalse(aidRequirement.matchesPattern("12345678-ABC"));


        //Test valid non default id
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertTrue(aidRequirement.isNotADefaultId(aid1, fedtaxid));
        assertTrue(aidRequirement.isNotADefaultId(aid2, fedtaxid));

        //Test id in sequence
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("12345678", fedtaxid));
        assertFalse(aidRequirement.isNotADefaultId("12345678-9012", fedtaxid));

        //Test id in repetition
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("11111111", fedtaxid));
        assertFalse(aidRequirement.isNotADefaultId("11111111-1111", fedtaxid));

        //Test id start at 2 and in sequence, it should be invalid
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("12345678", fedtaxid));

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testDefaultRequirementFunctionLogicForAR() {
        PayrollServices.beginUnitOfWork();
        AgencyIdRequirement aidRequirement = new AgencyIdRequirement();
        //Pattern for MN-DEED1-PAYMENT ^\d{8}-?([a-zA-Z]{3})?$
        //Example 12345678-ABC
        aidRequirement.setPattern("^\\d{8}-?([a-zA-Z]{3})?$");
        aidRequirement.setExample("12345678-ABC");
        aidRequirement.setRequired(true);

        String aid = "12245994-WTH";

        String fedtaxid = "12-3456789";

        assertTrue(aidRequirement.matchesPattern(aid));
        assertFalse(aidRequirement.matchesPattern("WAR-12345678"));

        //Test valid non default id
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertTrue(aidRequirement.isNotADefaultId(aid, fedtaxid));
        //Test id in sequence
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("12345678", fedtaxid));
        //Test id in repetition
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("11111111", fedtaxid));
        //Test id is alphanumeric
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertTrue(aidRequirement.isNotADefaultId("11111111-ABC", fedtaxid));
        //Test id start at 2 and in sequence, it should be invalid
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("23456789", fedtaxid));
        //Test id matches fed id
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.None);
        assertFalse(aidRequirement.isNotADefaultId("12345678", fedtaxid));
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.MustNotFollowFedTaxId);
        assertFalse(aidRequirement.isNotADefaultId("12345678", fedtaxid));
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testDefaultWAPFMLAgencyId() {
        PayrollServices.beginUnitOfWork();
        AgencyIdRequirement aidRequirement = new AgencyIdRequirement();
        aidRequirement.setPattern("\\d{3}-\\d{3}-\\d{3}");
        aidRequirement.setExample("123-456-789");
        aidRequirement.setRequired(true);
        //Test valid non default id
        aidRequirement.setCustomRequirement(AgencyIdCustomRequirement.IFNotPatternMustFollowFedTaxId);
        Assert.assertTrue(aidRequirement.isNotADefaultId("122-454-567", "12-3456789"));
        //Test id in sequence
        String aid = "122-459-941";
        Assert.assertTrue(aidRequirement.matchesPattern(aid));
        aid = "122-459941";
        Assert.assertFalse(aidRequirement.matchesPattern(aid));

        aidRequirement.setPattern("\\d{3}\\s\\d{3}\\s\\d{3}");
        aidRequirement.setExample("123 456 789");
        aidRequirement.setRequired(true);

        Assert.assertTrue(aidRequirement.isNotADefaultId("122 454 567", "12-3456789"));
        //Test id in sequence
        aid = "122 459 941";
        Assert.assertTrue(aidRequirement.matchesPattern(aid));
        aid = "122 459941";
        Assert.assertFalse(aidRequirement.matchesPattern(aid));
        PayrollServices.rollbackUnitOfWork();
    }
}
