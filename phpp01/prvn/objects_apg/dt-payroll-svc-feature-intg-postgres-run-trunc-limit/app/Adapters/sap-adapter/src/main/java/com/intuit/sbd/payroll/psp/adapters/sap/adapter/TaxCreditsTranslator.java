package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxCredits9061;
import com.intuit.sbd.payroll.psp.domain.TaxCredits9061;
import com.intuit.sbd.payroll.psp.domain.TaxCreditsApplication;

/**
 * User: dweinberg
 * Date: Jan 27, 2010
 * Time: 5:19:12 PM
 */
public class TaxCreditsTranslator {

    public static SAPTaxCredits9061 get9061FromDomainEntity(TaxCredits9061 form) {
        SAPTaxCredits9061 sapForm = new SAPTaxCredits9061();

        sapForm.setEin(form.getFedTaxId());
        sapForm.setSsn(form.getSSN());
        sapForm.setFormId(form.getId().toString());
        sapForm.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(form.getCreatedDate()));

        TaxCreditsApplication application = form.getTaxCreditsApplication();
        if (application != null) {
            sapForm.setApplicationId(application.getId().toString());
            sapForm.setApplicationPassword(application.getDocumentPassword());
            sapForm.setEmployeeEmail(application.getEmployeeEmail());
            sapForm.setEmployerEmail(application.getEmployerEmail());
            sapForm.setSignersRemaining(application.getSignersRemaining());
        }

        return sapForm;
    }

}
