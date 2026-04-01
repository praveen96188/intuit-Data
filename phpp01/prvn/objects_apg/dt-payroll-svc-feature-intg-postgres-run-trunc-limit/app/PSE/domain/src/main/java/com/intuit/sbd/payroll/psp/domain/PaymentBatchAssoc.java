package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class PaymentBatchAssoc extends BasePaymentBatchAssoc {

	/**
	 * Default constructor.
	 */
	public PaymentBatchAssoc()
	{
		super();
	}

    public static List<PaymentBatchAssoc> findPaymentBatchAssocsByBatch(AgencyCheckBatch pAgencyCheckBatch, boolean pFetchFinancialTransactions) {
        String[] paramNames = new String[1];
        paramNames[0] = "agencyCheckBatch";
        Object[] paramValues = new Object[1];
        paramValues[0] = pAgencyCheckBatch;

        if(pFetchFinancialTransactions) {
            return new ArrayList<PaymentBatchAssoc>(Application.findByNamedQuery("findPaymentBatchAssocsByBatchFetchFinancialTransactions", paramNames, paramValues));
        }

        return new ArrayList<PaymentBatchAssoc>(Application.findByNamedQuery("findPaymentBatchAssocsByBatch", paramNames, paramValues));
    }

}