package com.intuit.sbd.payroll.psp.adapters.mobile.processes;

import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSCompany;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyFinder;
import com.intuit.sbd.payroll.psp.domain.Company;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 25, 2011
 * Time: 3:15:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyProcess {

    String mEIN;

    public CompanyProcess(String pEIN) {
        this.mEIN = pEIN;
    }

    public RSCompany execute() {
        validate();
        return process();
    }

    private void validate() {
        
    }

    private RSCompany process() {
        Company company = CompanyFinder.findCompany(mEIN);
        return MobileFactory.createRSCompany(company);
    }
}
