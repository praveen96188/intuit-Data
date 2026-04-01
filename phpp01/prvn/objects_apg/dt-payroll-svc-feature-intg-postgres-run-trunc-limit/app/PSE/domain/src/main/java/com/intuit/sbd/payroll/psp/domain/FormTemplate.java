package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class FormTemplate extends BaseFormTemplate {

    public static String IRS_941 = "IRS-941-FILING";
    public static String IRS_944 = "IRS-944-FILING";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public FormTemplate()
	{
		super();
	}

    public static FormTemplate findFormTemplateByCd(String pFormTemplateCd) {
        Expression<FormTemplate> query = new Query<FormTemplate>().Where(FormTemplate.FormTemplateCd().equalTo(pFormTemplateCd));
        DomainEntitySet<FormTemplate> formTemplates = Application.find(FormTemplate.class, query);

        if (formTemplates.size() == 0) {
            return null;
        }
        if (formTemplates.size() > 1) {
            throw new RuntimeException(String.format("Found more than one Form templates with FormTemplateCd = %s", pFormTemplateCd));
        }
        
        return formTemplates.get(0);
    }
}