package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxCredits9061;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.TaxCredits9061;
import com.intuit.sbd.payroll.psp.domain.TaxCreditsApplication;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: dweinberg
 * Date: Jan 26, 2010
 * Time: 3:58:50 PM
 */
public class TaxCreditsAdapter {

    @FlexMethod
    public ArrayList<SAPTaxCredits9061> find9061Forms(String ein, String ssn, Date startDate, Date endDate) throws Exception {

        ArrayList<SAPTaxCredits9061> sapForms = new ArrayList<SAPTaxCredits9061>();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Criterion<TaxCredits9061> whereClause = TaxCredits9061.SSNEnc().isNotNull();

            if (ein != null) {
                List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(TaxCredits9061.FedTaxIdKeyName,ein);
                whereClause = whereClause.And(TaxCredits9061.FedTaxIdEnc().in(fedTaxIdEncList));
             }
            if (ssn != null) {
                List<String> ssnEncList = EncryptionUtils.deterministicEncryptWithAllKeys(TaxCredits9061.TaxCredit9061KeyName,ssn);
                whereClause = whereClause.And(TaxCredits9061.SSNEnc().in(ssnEncList));
            }
            if (startDate != null) {
                SpcfCalendar startDateCal = SAPTranslator.getSpcfCalendarFromDate_BeginDay(startDate);
                whereClause = whereClause.And(TaxCredits9061.CreatedDate().greaterOrEqualThan(startDateCal));
            }
            if (endDate != null) {
                SpcfCalendar endDateCal = SAPTranslator.getSpcfCalendarFromDate_EndDay(endDate);
                whereClause = whereClause.And(TaxCredits9061.CreatedDate().lessOrEqualThan(endDateCal));
            }

            DomainEntitySet<TaxCredits9061> forms = PayrollServices.entityFinder.find(TaxCredits9061.class,
                    new Query<TaxCredits9061>().Where(whereClause)
                            .OrderBy(TaxCredits9061.CreatedDate().Descending())
                            .LimitResults(0,100));

            for (TaxCredits9061 form : forms) {
                SAPTaxCredits9061 sapForm = TaxCreditsTranslator.get9061FromDomainEntity(form);
                sapForms.add(sapForm);
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapForms;
    }

    public byte[] findTaxFormBytes(String formId) throws Exception {
        TaxCredits9061 form = PayrollServices.entityFinder.findById(TaxCredits9061.class, SpcfUniqueId.createInstance(formId));

        if (form == null) {
            throw new Exception("Could not find form");
        }

        return form.get9061Bytes();
    }

    public byte[] findUnsignedApplicationBytes(String applicationId) throws Exception {
        return getApplication(applicationId).getUnsignedDocumentBytes();
    }

    public byte[] findSignedApplicationBytes(String applicationId) throws Exception {
        return getApplication(applicationId).getSignedDocumentBytes();
    }

    private TaxCreditsApplication getApplication(String applicationId) throws Exception {
        TaxCreditsApplication application = PayrollServices.entityFinder.findById(TaxCreditsApplication.class,
                SpcfUniqueId.createInstance(applicationId));

        if (application == null) {
            throw new Exception("Could not find application");
        }
        return application;
    }

}
