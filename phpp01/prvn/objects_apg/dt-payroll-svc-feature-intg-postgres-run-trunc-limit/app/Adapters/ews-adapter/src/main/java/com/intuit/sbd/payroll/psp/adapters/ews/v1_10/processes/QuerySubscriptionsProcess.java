package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQuerySubscriptions;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQuerySubscriptionsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class QuerySubscriptionsProcess extends BaseProcess {
    private EwsQuerySubscriptions mRequest;
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(QueryAccountProcess.class);
    }

    public QuerySubscriptionsProcess(EwsQuerySubscriptions pRequest) {
        this.mRequest = pRequest;

        logger.info("Processing Query_Subscriptions Request / SubscriptionNumber: " + pRequest.getSubscriptionNumber());
    }

    @Override
    public EwsQuerySubscriptionsResponse execute() {
        EwsQuerySubscriptionsResponse response;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsQuerySubscriptionsResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsQuerySubscriptionsResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();
    }

    @Override
    protected EwsQuerySubscriptionsResponse process() throws Exception {
        DomainEntitySet<EntitlementUnit> entitlementUnits =
                PspFactory.findEntitlementUnitsBySubscriptionNumber(mRequest.getSubscriptionNumber());

        return EwsFactory.createEwsQuerySubscriptionsResponse(entitlementUnits);
    }
}
