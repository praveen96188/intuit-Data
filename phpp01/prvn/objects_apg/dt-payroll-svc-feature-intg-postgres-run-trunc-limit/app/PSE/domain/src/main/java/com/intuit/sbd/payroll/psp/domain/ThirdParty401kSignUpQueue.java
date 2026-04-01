package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import java.util.List;


/**
 * Hand-written business logic
 */
public class ThirdParty401kSignUpQueue extends BaseThirdParty401kSignUpQueue {
    public static String FedTaxIdKeyName="TP401KSignUpQ_EIN";

	/**
	 * Default constructor.
	 */
	public ThirdParty401kSignUpQueue()
	{
		super();
	}

    public static ThirdParty401kSignUpQueue findThirdParty401kSignUpQueue(String pFedTaxId) {
        Criterion<ThirdParty401kSignUpQueue> criterion = null;
        if(pFedTaxId == null){
            criterion = ThirdParty401kSignUpQueue.FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(ThirdParty401kSignUpQueue.FedTaxIdKeyName,pFedTaxId);
            criterion = ThirdParty401kSignUpQueue.FedTaxIdEnc().in(fedTaxIdEncList);
        }
        Expression<ThirdParty401kSignUpQueue> query =
                new Query<ThirdParty401kSignUpQueue>()
                        .Where(criterion);
        DomainEntitySet<ThirdParty401kSignUpQueue> batchSet = Application.find(ThirdParty401kSignUpQueue.class, query);

        return batchSet.isEmpty() ? null : batchSet.get(0);
    }

    public static void addThirdParty401kSignUpQueue(String pFedTaxId,
                                                    String pCustodialId,
                                                    String pEffectiveDate,
                                                    String LegalName,
                                                    Boolean pHasSafeHarbor) throws Exception {
        if (findThirdParty401kSignUpQueue(pFedTaxId) != null) {
            throw new RuntimeException("ThirdParty401kSignUpQueue already contains FedTaxId: " + pFedTaxId);
        }

        ThirdParty401kSignUpQueue tp401kSignUpQueue = new ThirdParty401kSignUpQueue();
        tp401kSignUpQueue.setStatus(ThirdParty401kSignUpQueueStatusCode.Pending);
        tp401kSignUpQueue.setFedTaxId(pFedTaxId);
        tp401kSignUpQueue.setCustodialId(pCustodialId);
        tp401kSignUpQueue.setLegalName(LegalName);
        tp401kSignUpQueue.setEffectiveDate(SpcfCalendar.parse("mm/DD/yyyy", pEffectiveDate));
        tp401kSignUpQueue.setHasSafeHarbor(pHasSafeHarbor);

        Application.save(tp401kSignUpQueue);
    }

    public static void updateThirdParty401kSignUpQueue(String pFedTaxId,
                                                       String pCustodialId,
                                                       String pEffectiveDate,
                                                       String LegalName,
                                                       Boolean pHasSafeHarbor) throws Exception {
        ThirdParty401kSignUpQueue tp401kSignUpQueue = findThirdParty401kSignUpQueue(pFedTaxId);

        if (tp401kSignUpQueue == null) {
            throw new RuntimeException("ThirdParty401kSignUpQueue does not contain FedTaxId: " + pFedTaxId);
        }

        tp401kSignUpQueue.setCustodialId(pCustodialId);
        tp401kSignUpQueue.setLegalName(LegalName);
        tp401kSignUpQueue.setEffectiveDate(SpcfCalendar.parse("mm/DD/yyyy", pEffectiveDate));
        tp401kSignUpQueue.setHasSafeHarbor(pHasSafeHarbor);

        Application.save(tp401kSignUpQueue);
    }

    public static void updateThirdParty401kSignUpQueueStatus(String pFedTaxId, ThirdParty401kSignUpQueueStatusCode pStatus) {
        ThirdParty401kSignUpQueue tp401kSignUpQueue = findThirdParty401kSignUpQueue(pFedTaxId);

        if (tp401kSignUpQueue != null) {
            tp401kSignUpQueue.setStatus(pStatus);
            Application.save(tp401kSignUpQueue);
        }
    }

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }
}