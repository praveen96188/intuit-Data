package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class ReturnReasonDesc extends BaseReturnReasonDesc {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ReturnReasonDesc()
	{
		super();
	}
    
    public static String findReturnDescription(String pACHReturnReason) {
        ACHReturnReason achReturnReason;
        try {
             achReturnReason = ACHReturnReason.valueOf(pACHReturnReason);
        } catch (IllegalArgumentException e) {
            return null;
        }

        DomainEntitySet<ReturnReasonDesc> returnReasonDescs = Application.find(ReturnReasonDesc.class, ReturnReasonDesc.ReasonCd().equalTo(achReturnReason));
        if(!returnReasonDescs.isEmpty()) {
            return returnReasonDescs.get(0).getDescription();
        } else {
            return null;
        }
    }

}