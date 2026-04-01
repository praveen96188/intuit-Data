package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.TaxRateUpdateDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.UpdateCompanyTaxRateRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.UpdateCompanyTaxRateResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawItem;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawRate;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarterLawRates;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPUpdateCompanyTaxRate.java $
 * $Revision: #1 $
 * $DateTime: 2012/08/30 22:27:03 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPUpdateCompanyTaxRate extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPUpdateCompanyTaxRate.class);
    }

    public static final String UPDATE_OPERATION_CLASS_NAME = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter";
    public static final String UPDATE_OPERATION_METHOD_NAME = "updateRates";

    public static Class updateAdapterClass;

    static {
        try {
            updateAdapterClass = Class.forName(UPDATE_OPERATION_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.fatal("PSPUpdateCompanyTaxRate static loader failed.");
        }
    }

    private UpdateCompanyTaxRateRequestDISDTO requestDISDTO;
    private ResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pUpdateCompanyTaxRateDISDTO
     */
    public PSPUpdateCompanyTaxRate(UpdateCompanyTaxRateRequestDISDTO pUpdateCompanyTaxRateDISDTO) {
        requestDISDTO = pUpdateCompanyTaxRateDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPUpdateCompanyTaxRates.process()");

        responseDISDTO = new UpdateCompanyTaxRateResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();
        Calendar effectiveDate = requestDISDTO.getEffectiveDate();
        String paymentTemplateCd = requestDISDTO.getPaymentTemplateCd();

        List<TaxRateUpdateDISDTO> taxRates = requestDISDTO.getTaxRates();

        Integer quarter = (effectiveDate.get(Calendar.MONTH) / 3) + 1;
        Integer year = effectiveDate.get(Calendar.YEAR);
        Boolean pushToQuickbooksObj = requestDISDTO.getPushToQuickbooks();
        boolean pushToQuickbooks = false;
        if (pushToQuickbooksObj != null) {
            pushToQuickbooks = pushToQuickbooksObj.booleanValue();
        }
        Boolean overrideBlackoutObj = requestDISDTO.getOverrideBlackout();
        boolean overrideBlackout = false;
        if (pushToQuickbooksObj != null) {
            overrideBlackout = overrideBlackoutObj.booleanValue();
        }
        Boolean supportRatesOutsideBoundaries = requestDISDTO.getSupportRatesOutsideBoundaries();
        String noteToAttachToEvent = requestDISDTO.getNoteToAttachToEvent();
        String corpId = requestDISDTO.getCorpId();

        doWork(sourceSystem,
               sourceCompanyId,
               paymentTemplateCd,
               quarter,
               year,
               taxRates,
               pushToQuickbooks,
               overrideBlackout,
               noteToAttachToEvent,
               corpId
        );
        logger.debug("Leaving PSPUpdateCompanyTaxRates.process()");
        return responseDISDTO;
    }

    private void doWork(SourceSystemEnum pSourceSystemCd,
                        String pSourceCompanyId,
                        String pPaymentTemplateCd,
                        Integer pQuarter,
                        Integer pYear,
                        List<TaxRateUpdateDISDTO> pTaxRates,
                        boolean pPushToQuickbooks,
                        boolean pOverrideBlackout,
                        String pNoteToAttachToEvent,
                        String pCorpId
    ) throws Throwable {
        try {

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, translateSourceSystemCode(pSourceSystemCd));
            if (company == null) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.companyDoesNotExist(pSourceCompanyId);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            PayrollServices.rollbackUnitOfWork();

            String token = requestDISDTO.getToken();
            String corpId = requestDISDTO.getCorpId();

            try {
                PSPHelper.validateUserHasPermissionsInSAP(corpId, token, updateAdapterClass, UPDATE_OPERATION_METHOD_NAME);
            } catch (Exception e) {
                responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                return;
            }

            TaxAdapter taxAdapter = new TaxAdapter();

            SAPQuarterLawRates rates = populateSAPQuarterLawRates(
                    pQuarter,
                    pYear,
                    pTaxRates,
                    pOverrideBlackout
            );

            try {
                taxAdapter.updateRates(
                        pSourceSystemCd.toString(),
                        pSourceCompanyId,
                        pPaymentTemplateCd,
                        rates,
                        pPushToQuickbooks
                );
            } catch (Exception e) {
                // In order to prevent the quarter out of range exception from logging an error message,
                //    we have to compare the exception message.  The updateRates method returns
                //    an SAP exception for all failures, so the only mechanism we have to determine
                //    a specific error is to compare the message text.
                if (TaxAdapter.QUARTER_OUT_OF_RANGE_EXCEPTION_STRING.equals(e.getMessage())) {
                    responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                    return;
                } else {
                    throw e;
                }
            }

            if (pNoteToAttachToEvent != null) {
                updateEventsWithCaseNote(company, pTaxRates, pNoteToAttachToEvent, pCorpId);
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void updateEventsWithCaseNote(
            Company pCompany,
            List<TaxRateUpdateDISDTO> pTaxRates,
            String pNoteToAttachToEvent,
            String pCorpId
    ) throws Throwable {
        for (TaxRateUpdateDISDTO taxRateUpdateDISDTO : pTaxRates) {
            CompanyEvent companyEvent = findRateUpdateEvent(pCompany,taxRateUpdateDISDTO.getLawId().toString());
            if (companyEvent != null) {
                PSPHelper.addCompanyNote(pCompany,pCorpId,companyEvent.getId().toString(),pNoteToAttachToEvent);
            }
        }
    }

    public static CompanyEvent findRateUpdateEvent(Company pCompany, String pLawId) throws Exception {

        // Using this find method because it get the events in order
        // Limiting to last hour.
        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        spcfCalendar.addHours(-1);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventTypeCode.CompanyLawUpdated, null, spcfCalendar, null, true);
        if (companyEvents.size() > 0) {
            for (int cnt = companyEvents.size() - 1; cnt >= 0; cnt--) {
                CompanyEvent companyEvent = companyEvents.get(cnt);
                DomainEntitySet<CompanyEventDetail> paymentTemplateEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Law);
                if (paymentTemplateEventDetails.size() == 1 && paymentTemplateEventDetails.get(0).getValue().equals(pLawId)) {
                    return companyEvent;
                }
            }
        }
        return null;
    }

    private SAPQuarterLawRates populateSAPQuarterLawRates(
            Integer pQuarter,
            Integer pYear,
            List<TaxRateUpdateDISDTO> pTaxRates,
            boolean pOverrideBlackout
    ) {
        SAPQuarterLawRates sapQuarterLawRates = new SAPQuarterLawRates();

        List<SAPLawRate> sapLawRates = new ArrayList<SAPLawRate>();
        for (TaxRateUpdateDISDTO taxRateUpdate : pTaxRates) {
            SAPLawRate sapLawRate = new SAPLawRate();
            sapLawRate.setNewPercentage(taxRateUpdate.getRate().doubleValue());
            SAPLawItem sapLawItem = new SAPLawItem();
            sapLawItem.setLawId(taxRateUpdate.getLawId().toString());
            sapLawRate.setLaw(sapLawItem);
            sapLawRates.add(sapLawRate);
        }

        sapQuarterLawRates.setLawRates(sapLawRates);

        SAPQuarter sapQuarter = new SAPQuarter();
        sapQuarter.setQuarter(pQuarter);
        sapQuarter.setYear(pYear);
        sapQuarterLawRates.setQuarter(sapQuarter);
        sapQuarterLawRates.setUnderBlackout(pOverrideBlackout);

        return sapQuarterLawRates;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

}
