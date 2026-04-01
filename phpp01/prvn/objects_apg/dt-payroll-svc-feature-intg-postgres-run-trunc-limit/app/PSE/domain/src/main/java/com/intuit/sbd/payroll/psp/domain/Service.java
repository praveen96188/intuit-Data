package com.intuit.sbd.payroll.psp.domain;


/**
 * Hand-written business logic
 */
public class Service extends BaseService {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Service()
	{
		super();
	}

    //PSP moves money for this service if there are financial transactions associated with this service
    public boolean doesPSPMoveMoneyForService() {
       if (getTransactionTypeCollection() !=null && getTransactionTypeCollection().size()>0) {
           return true;
       } else {
           return false;
       }
    }

    public String toString() {
        return "Service: " + getServiceCd().name();        
    }
}