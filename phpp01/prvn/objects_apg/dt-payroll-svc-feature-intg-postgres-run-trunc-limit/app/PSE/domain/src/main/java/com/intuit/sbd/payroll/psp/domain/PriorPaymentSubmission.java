package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Hand-written business logic
 */
public class PriorPaymentSubmission extends BasePriorPaymentSubmission implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public PriorPaymentSubmission()
	{
		super();
	}

    public static PriorPaymentSubmission findPriorPaymentSubmissionByCompanyAndSourceId(Company company, String sourceId) {
        PriorPaymentSubmission foundPriorPaymentSubmission = null;

        NaturalKey naturalKey = new NaturalKey(PriorPaymentSubmission.class, company.getId(), sourceId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundPriorPaymentSubmission = Application.findById(PriorPaymentSubmission.class, primaryKey);
        } else {
            DomainEntitySet<PriorPaymentSubmission> priorPaymentSubmissions = Application.find(PriorPaymentSubmission.class,
                                                                                               PriorPaymentSubmission.Company().equalTo(company)
                                                                                                       .And(PriorPaymentSubmission.SourceId().equalTo(sourceId)));
            if (priorPaymentSubmissions.size() > 1) {
                throw new RuntimeException("Multiple PriorPaymentSubmissions for company/source Id");
            } else if (priorPaymentSubmissions.size() == 1) {
                foundPriorPaymentSubmission = priorPaymentSubmissions.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundPriorPaymentSubmission.getId());
            }
        }

        return foundPriorPaymentSubmission;
    }

    public static PriorPaymentSubmission createNewPaymentSubmission(Company company, String sourceId) {
        PriorPaymentSubmission pps = new PriorPaymentSubmission();
        pps.setCompany(company);
        pps.setSourceId(sourceId);
        company.usedPayrollTransactionId(sourceId);
        Application.save(pps);

        NaturalKey naturalKey = new NaturalKey(PriorPaymentSubmission.class, company.getId(), sourceId);
        Application.getSessionCache().addPrimaryKey(naturalKey, pps.getId());

        return pps;
    }

    @Override
    public void setSourceId(String pSourceId) {
        super.setSourceId(pSourceId);

        if(getCompany() != null && getSourceId() != null) {
            getCompany().usedPayrollTransactionId(getSourceId());
        }
    }

    @Override
    public void setCompany(Company pCompany) {
        super.setCompany(pCompany);

        if(getCompany() != null && getSourceId() != null) {
            getCompany().usedPayrollTransactionId(getSourceId());
        }
    }

    public void onUpdate() {
        for (QbdtTransactionInfo qbdtTransactionInfo : getQbdtTransactionInfoCollection()) {
            qbdtTransactionInfo.update();
        }

        if(getQbdtPayrollTransaction() != null) {
            getQbdtPayrollTransaction().update();
        }
    }
}