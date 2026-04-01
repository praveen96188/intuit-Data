package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class IOPSyncCompany extends BaseIOPSyncCompany {

	/**
	 * Default constructor.
	 */
	public IOPSyncCompany()
	{
		super();
	}

    /**
     *
     * @param companyId
     * @param hasEmployeePayroll
     * @param hasContractorPayments
     */
    public IOPSyncCompany(Integer companyId,Boolean hasEmployeePayroll, Boolean hasContractorPayments)
    {
        super();
        this.setCompanyId(companyId);
        this.setStatus(IOPSyncStatus.Pending);
        this.setHasEmployeePayroll(hasEmployeePayroll);
        this.setHasContractorPayment(hasContractorPayments);
    }

    /**
     *
     * @return
     */
    public static DomainEntitySet<IOPSyncCompany> findPendingCompanyList() {
        Criterion<IOPSyncCompany> query = Status().in(IOPSyncStatus.Pending,IOPSyncStatus.Failed,IOPSyncStatus.InProcess).And(IOPSyncCompany.RetryCount().lessThan(3));
        return Application.find(IOPSyncCompany.class, query);
    }
}