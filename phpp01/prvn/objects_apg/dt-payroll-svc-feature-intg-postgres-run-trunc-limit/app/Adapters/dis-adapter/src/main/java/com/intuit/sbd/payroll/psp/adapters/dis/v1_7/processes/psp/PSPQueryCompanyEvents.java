package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyEventDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyEventsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyEventsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryCompanyEvents.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Query company event Process
 *
 */
public class PSPQueryCompanyEvents extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyEvents.class);
    }

    private QueryCompanyEventsRequestDISDTO mQueryCompanyEventsRequestDISDTO;
    private QueryCompanyEventsResponseDISDTO mQueryCompanyEventsResponseDISDTO;

    /***
     * Constructor
     * @param pQueryCompanyEventsRequestDISDTO
     */
    public PSPQueryCompanyEvents(QueryCompanyEventsRequestDISDTO pQueryCompanyEventsRequestDISDTO) {
        mQueryCompanyEventsRequestDISDTO = pQueryCompanyEventsRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyEvents.process()");
        mQueryCompanyEventsResponseDISDTO = new QueryCompanyEventsResponseDISDTO();

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(mQueryCompanyEventsRequestDISDTO.getSourceCompanyId(),translateSourceSystemCode(mQueryCompanyEventsRequestDISDTO.getSourceSystem()));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(mQueryCompanyEventsRequestDISDTO.getSourceCompanyId()));
            }
            List<CompanyEventDISDTO> companyEventList = new ArrayList<CompanyEventDISDTO>();
            String eventTypeCode = mQueryCompanyEventsRequestDISDTO.getEventTypeCode();
            DomainEntitySet<CompanyEvent> coEvents = null;
            if (eventTypeCode != null) {

                coEvents = CompanyEvent.findCompanyEvents(company,EventTypeCode.valueOf(eventTypeCode));
            } else {
                coEvents = CompanyEvent.findCompanyEvents(company);
            }
            for (CompanyEvent coEvent : coEvents) {
                companyEventList.add(PSPToDISTransformer.createCompanyEventDISDTO(coEvent));
            }
            mQueryCompanyEventsResponseDISDTO.setCompanyEvents(companyEventList);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.debug("Leaving PSPQueryCompanyEvents.process()");
        return mQueryCompanyEventsResponseDISDTO;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return mQueryCompanyEventsResponseDISDTO;
    }

}
