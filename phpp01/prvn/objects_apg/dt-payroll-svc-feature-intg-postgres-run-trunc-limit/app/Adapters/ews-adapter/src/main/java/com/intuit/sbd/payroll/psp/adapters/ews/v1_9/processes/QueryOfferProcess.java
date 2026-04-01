package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsQueryOffer;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsQueryOfferResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Marcela Villani
 */
public class QueryOfferProcess extends BaseProcess {
    private EwsQueryOffer mRequest;
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(QueryOfferProcess.class);
    }

    public QueryOfferProcess(EwsQueryOffer pRequest) {
        this.mRequest = pRequest;

        logger.info("Processing Query_Offer Request / Offer: " + pRequest.getOfferCode()); 
    }

    @Override
    public EwsQueryOfferResponse execute() {
        EwsQueryOfferResponse response = new EwsQueryOfferResponse();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsQueryOfferResponse();
            processEwsException(e, response);
        } catch (Throwable t) {
            response = new EwsQueryOfferResponse();
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
    protected EwsQueryOfferResponse process() throws Exception {
        EwsQueryOfferResponse response = new EwsQueryOfferResponse();
        Offer offer = Offer.findOfferByOfferCode(mRequest.getOfferCode());
        if (offer == null) {
            throw new EwsException(EwsMessages.offerCodeDoesNotExist());
        }
        response.setOfferCode(offer.getOfferCd());
        response.setDescription(offer.getDescription());

        return response;
    }
}