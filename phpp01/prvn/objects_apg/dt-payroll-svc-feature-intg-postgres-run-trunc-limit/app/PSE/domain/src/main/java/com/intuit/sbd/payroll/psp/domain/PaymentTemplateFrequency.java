package com.intuit.sbd.payroll.psp.domain;

import com.intuit.payroll.agency.api.*;
import com.intuit.payroll.agency.dao.FrequencyData;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class PaymentTemplateFrequency extends BasePaymentTemplateFrequency {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public PaymentTemplateFrequency() {
        super();
    }


    public static ArrayList<PaymentTemplateFrequency> getZeroPaymentRequiredPaymentFrequencies(SpcfCalendar pDate) {

        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();

        ArrayList<PaymentTemplateFrequency> pmtFrequencyList = new ArrayList<PaymentTemplateFrequency>();

        Criterion<PaymentTemplateFrequency> paymentTemplateFrequencyCriteria = PaymentTemplateFrequency.PaymentTemplate().SupportStartDate().lessOrEqualThan(pDate);
        DomainEntitySet<PaymentTemplateFrequency> paymentTemplateFrequencies =
                Application.find(PaymentTemplateFrequency.class,
                                 new Query<PaymentTemplateFrequency>()
                                 .Where(paymentTemplateFrequencyCriteria)
                                 .OrderBy(PaymentTemplateFrequency.PaymentTemplate().PaymentTemplateCd(),
                                          PaymentTemplateFrequency.PaymentFrequencyId()));

        for (PaymentTemplateFrequency pmtTemplateFrequency : paymentTemplateFrequencies) {
            IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(pmtTemplateFrequency.getPaymentTemplate().getPaymentTemplateCd());
            DepositFrequencyCode depositFrequencyCode = pmtTemplateFrequency.getPaymentFrequencyId();
            if (!depositFrequencyCode.equals(DepositFrequencyCode.NOCALC) && !pmtTemplateFrequency.getObsolete()) {

                IPaymentFrequency paymentFrequency = paymentTemplate.getPaymentFrequency(depositFrequencyCode.toString());

                FrequencyData freq = (FrequencyData) paymentFrequency;
                if (freq != null && freq.isZeroPaymentRequired() && !pmtFrequencyList.contains(pmtTemplateFrequency)) {

                    pmtFrequencyList.add(pmtTemplateFrequency);
                }
            }


        }
        return pmtFrequencyList;
    }


    public static PaymentTemplateFrequency getPaymentTemplateFrequency(String paymentTemplateStr, DepositFrequencyCode frequencyCode) {
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateStr);
        return findPaymentTemplateFrequency(paymentTemplate, frequencyCode);
    }

    public static PaymentTemplateFrequency findPaymentTemplateFrequency(PaymentTemplate paymentTemplate, DepositFrequencyCode frequencyCode) {
        DomainEntitySet<PaymentTemplateFrequency> paymentTemplateFrequencies = Application.find(PaymentTemplateFrequency.class,
                                                                                                PaymentTemplateFrequency.PaymentFrequencyId().equalTo(frequencyCode)
                                                                                                        .And(PaymentTemplateFrequency.PaymentTemplate().equalTo(paymentTemplate)));

        if (paymentTemplateFrequencies.size() != 1) {
            return null;
        } else {
            return paymentTemplateFrequencies.get(0);
        }
    }

    //supported = in agency rules (see below)
    public static List<DepositFrequencyCode> findSupportedFrequencies(PaymentTemplate paymentTemplate) {
        return Application.executeQuery(PaymentTemplateFrequency.class, new Query<PaymentTemplateFrequency>()
                .Select(PaymentTemplateFrequency.PaymentFrequencyId())
                .Where(PaymentTemplateFrequency.PaymentTemplate().equalTo(paymentTemplate)));
    }

    //active = in agency rules and not obsolete
    public static List<DepositFrequencyCode> findActiveFrequencies(PaymentTemplate paymentTemplate) {
        return Application.executeQuery(PaymentTemplateFrequency.class, new Query<PaymentTemplateFrequency>()
                .Select(PaymentTemplateFrequency.PaymentFrequencyId())
                .Where(PaymentTemplateFrequency.PaymentTemplate().equalTo(paymentTemplate)
                                               .And(PaymentTemplateFrequency.Obsolete().equalTo(false))
                                               .And(PaymentTemplateFrequency.AgentDisallowed().equalTo(false))));
    }
}
