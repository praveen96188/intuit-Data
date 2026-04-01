package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * @author Jeff Jones
 */
public class PayeeFinder {

    public static DomainEntitySet<Payee> findPayees(Company pCompany) {
        return pCompany.getPayeeCollection();
    }

    public static Payee findPayee(Company pCompany, String pId) {
        return Application.findById(Payee.class, SpcfUniqueId.createInstance(pId));
    }

}
