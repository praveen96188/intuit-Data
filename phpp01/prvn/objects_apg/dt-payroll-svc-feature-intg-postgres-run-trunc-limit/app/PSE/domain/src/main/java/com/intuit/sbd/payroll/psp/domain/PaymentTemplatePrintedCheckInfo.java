package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class PaymentTemplatePrintedCheckInfo extends BasePaymentTemplatePrintedCheckInfo {
    private static String cacheMapName = "PrintedCheckInfoMap";

	/**
	 * Default constructor.
	 */
	public PaymentTemplatePrintedCheckInfo()
	{
		super();
	}

    public static Map<String, PaymentTemplatePrintedCheckInfo> cacheAllPaymentTemplatePrintedCheckInfo() {
        Expression<PaymentTemplateCheckInfoAssoc> query =
                new Query<PaymentTemplateCheckInfoAssoc>().EagerLoad(PaymentTemplateCheckInfoAssoc.PaymentTemplatePrintedCheckInfo(), PaymentTemplateCheckInfoAssoc.PaymentTemplatePrintedCheckInfo().Address(), PaymentTemplateCheckInfoAssoc.PaymentTemplate());
        DomainEntitySet<PaymentTemplateCheckInfoAssoc> paymentTemplateCheckInfoAssocs = Application.find(PaymentTemplateCheckInfoAssoc.class, query);

        HashMap<String,PaymentTemplatePrintedCheckInfo> map = new HashMap<String,PaymentTemplatePrintedCheckInfo>();
        for (PaymentTemplateCheckInfoAssoc paymentTemplateCheckInfoAssoc : paymentTemplateCheckInfoAssocs) {
            PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo = paymentTemplateCheckInfoAssoc.getPaymentTemplatePrintedCheckInfo();
            map.put(paymentTemplateCheckInfoAssoc.getPaymentTemplate().getPaymentTemplateCd(), paymentTemplatePrintedCheckInfo);
        }
        Application.getSessionCache().addNonHibernateObject(cacheMapName, map);
        return map;
    }

    public static PaymentTemplatePrintedCheckInfo findPaymentTemplatePrintedCheckInfo(PaymentTemplate pPaymentTemplate) {
        String paymentTemplateCd = pPaymentTemplate.getPaymentTemplateCd();
        Map<String, PaymentTemplatePrintedCheckInfo> rulesMap = Application.getSessionCache().getNonHibernateObject(cacheMapName);
        if (rulesMap == null) {
            rulesMap = cacheAllPaymentTemplatePrintedCheckInfo();
        }

        return rulesMap.get(paymentTemplateCd);
    }

    public static boolean paymentTemplateHasPrintedCheckInfo(PaymentTemplate pPaymentTemplate) {
        return  findPaymentTemplatePrintedCheckInfo(pPaymentTemplate) != null;
    }

}