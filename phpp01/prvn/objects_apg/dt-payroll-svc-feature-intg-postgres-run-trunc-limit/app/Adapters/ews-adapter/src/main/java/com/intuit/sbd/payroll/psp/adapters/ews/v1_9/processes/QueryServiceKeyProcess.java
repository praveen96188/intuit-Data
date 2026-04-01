package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.Collection;

/**
 * @author Jeff Jones
 */
public class QueryServiceKeyProcess extends BaseProcess {

    private Collection<Company> mCompanies;
    private EwsQueryServiceKey mRequest;
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(QueryServiceKeyProcess.class);
    }

    public QueryServiceKeyProcess(EwsQueryServiceKey pRequest) {
        mRequest = pRequest;
        mEIN = pRequest.getEin();

        logger.info("Processing Query Service Key Request / AuthId: " + this.mRequest.getAuthId() + " PSID: " + this.mPSID);
    }

    @Override
    public EwsQueryServiceKeyResponse execute() {
        EwsQueryServiceKeyResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsQueryServiceKeyResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsQueryServiceKeyResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();

        mCompanies = PspFactory.findCompanyByEinAndAuthId(mEIN, mRequest.getAuthId());
    }

    @Override
    protected EwsQueryServiceKeyResponse process() throws Exception {
        EwsQueryServiceKeyResponse response = new EwsQueryServiceKeyResponse();

        for (Company company : mCompanies) {
            EwsQueryServiceKeyCompany skCompany = new EwsQueryServiceKeyCompany();
            skCompany.setLegalName(company.getLegalName());

            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                EwsQueryServiceKeyItem skItem = new EwsQueryServiceKeyItem();
                skItem.setServiceKey(entitlementUnit.getServiceKey());

                EwsEntitlementUnitStatusCode status = EwsFactory.convertEntitlementUnitStatus(entitlementUnit.getEntitlementUnitStatus());
                skItem.setStatus(status.toString());

                skCompany.getServiceKeys().add(skItem);
            }

            response.getCompanies().add(skCompany);
        }

        return response;
    }
}
