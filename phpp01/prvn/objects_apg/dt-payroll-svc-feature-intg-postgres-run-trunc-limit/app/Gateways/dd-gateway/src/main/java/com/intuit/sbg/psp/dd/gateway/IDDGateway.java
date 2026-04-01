package com.intuit.sbg.psp.dd.gateway;

import com.intuit.pmo.client.model.PayrollCheckResponse;

/**
 * 
 * @author dchoudhary1
 *
 */
public interface IDDGateway {
	PayrollCheckResponse checkLimit(Object ddRequestEntity ) throws Exception;
	String getSubmissionId(String ownerId, String detailTransactionTypes, String paycheckId) throws Exception;
}
