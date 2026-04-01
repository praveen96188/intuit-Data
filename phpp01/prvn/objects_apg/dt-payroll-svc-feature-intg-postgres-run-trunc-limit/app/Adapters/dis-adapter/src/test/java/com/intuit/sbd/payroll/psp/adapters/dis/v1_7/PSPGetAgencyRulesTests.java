package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.AgencyRulesAgencyDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.AgencyRulesPaymentTemplateDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.GetAgencyRulesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.GetAgencyRulesResponseDISDTO;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPGetAgencyRulesTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPGetAgencyRulesTests {
    @Test
    public void testGetAgencyRulesHappyPath() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            GetAgencyRulesRequestDISDTO getAgencyRulesRequestDISDTO = new GetAgencyRulesRequestDISDTO();

            GetAgencyRulesResponseDISDTO getAgencyRulesResponseDISDTO = disAdapter.Query_GetAgencyRules(getAgencyRulesRequestDISDTO);
            TestHelper.verifySuccess(getAgencyRulesResponseDISDTO.getDisResponse());
            TestCase.assertTrue(getAgencyRulesResponseDISDTO.getAgencies().size() > 1);
            AgencyRulesAgencyDISDTO irsAgency = null;

            for(AgencyRulesAgencyDISDTO agencyRulesAgencyDISDTO : getAgencyRulesResponseDISDTO.getAgencies()) {
                if (agencyRulesAgencyDISDTO.getAgencyId().equals("IRS")) {
                    irsAgency=agencyRulesAgencyDISDTO;
                }
            }
            TestCase.assertNotNull(irsAgency);

            AgencyRulesPaymentTemplateDISDTO irs940PaymentTemplate = null;
            for(AgencyRulesPaymentTemplateDISDTO agencyRulesPaymentTemplateDISDTO : irsAgency.getPaymentTemplates()) {
                if (agencyRulesPaymentTemplateDISDTO.getPaymentTemplateId().equals("IRS-940-PAYMENT")) {
                    irs940PaymentTemplate=agencyRulesPaymentTemplateDISDTO;
                }
            }
            TestCase.assertNotNull(irs940PaymentTemplate);
            //@TODO Change this once real rules are implemented
//            TestCase.assertEquals(irs940PaymentTemplate.getAgencyIDFormats().get(0),"99-9999999");
            TestCase.assertTrue(irs940PaymentTemplate.getLaws().size()>0);
            TestCase.assertTrue(irs940PaymentTemplate.getPaymentFrequencies().size()>0);

        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }

}
