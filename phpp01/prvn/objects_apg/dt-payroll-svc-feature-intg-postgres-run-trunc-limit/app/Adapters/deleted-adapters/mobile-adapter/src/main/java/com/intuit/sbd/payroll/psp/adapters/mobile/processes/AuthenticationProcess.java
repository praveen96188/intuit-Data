package com.intuit.sbd.payroll.psp.adapters.mobile.processes;

import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSCompany;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyFinder;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Jeff Jones
 */
public class AuthenticationProcess {

    private String mEIN;
    private String mPIN;

    private Company company;

    private static final SpcfLogger logger = PayrollServices.getLogger(AuthenticationProcess.class);


    public AuthenticationProcess(String pEIN, String pPIN) {
        this.mEIN = pEIN;
        this.mPIN = pPIN;
    }

    public void execute() throws Exception {
        validate();
        process();
    }

    private void validate() {
        if (mEIN == null || mEIN.length() == 0)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        if (mPIN == null || mPIN.length() == 0)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    private void process() throws Exception {
        try {

            try {
                company = CompanyFinder.findCompany(mEIN);
            } catch (WebApplicationException e) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            ProcessResult<Company> authenticationPR = PayrollServices.subscriptionManager.verifyCompanyPIN
                    (SourceSystemCode.QBDT, company.getSourceCompanyId(), mPIN);
            if (!authenticationPR.isSuccess()) {
                //Save core events
                PayrollServices.commitUnitOfWork();
                PayrollServices.beginUnitOfWork();

                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.warn(e);
            throw e;
        }
    }

    public Company getCompany() {
        return company;
    }
}
