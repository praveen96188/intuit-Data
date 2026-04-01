package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.AgencyIdRequirementDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.PaymentMethodAgencyIdRequirementsDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.UpdateCompanyAgencyIdRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.UpdateCompanyAgencyIdResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyIdRequirement;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyAgencyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentMethodAgencyIdRequirements;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPUpdateCompanyAgencyId.java $
 * $Revision: #1 $
 * $DateTime: 2012/08/30 22:27:03 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPUpdateCompanyAgencyId extends DISProcessInterface {
    private static final SpcfLogger logger;
    public static final String NO_PAYMENT_TEMPLATE_FOUND_ERROR_MESSAGE = "Agency ID UPDATED!  Error finding SAPCompanyAgencyPaymentTemplateAgencyId for payment template '%1$s' for company '%2$s'.";

    static {
        logger = PayrollServices.getLogger(PSPUpdateCompanyAgencyId.class);
    }

    public static final String UPDATE_OPERATION_CLASS_NAME = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter";
    public static final String UPDATE_OPERATION_METHOD_NAME = "updateAgencyIDs";

    public static Class updateAdapterClass;

    static {
        try {
            updateAdapterClass = Class.forName(UPDATE_OPERATION_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.fatal("PSPUpdateCompanyAgencyId static loader failed.");
        }
    }

    private UpdateCompanyAgencyIdRequestDISDTO requestDISDTO;
    private ResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pUpdateCompanyAgencyIdDISDTO
     */
    public PSPUpdateCompanyAgencyId(UpdateCompanyAgencyIdRequestDISDTO pUpdateCompanyAgencyIdDISDTO) {
        requestDISDTO = pUpdateCompanyAgencyIdDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPUpdateCompanyAgencyIds.process()");

        responseDISDTO = new UpdateCompanyAgencyIdResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();

        String paymentTemplateCd = requestDISDTO.getPaymentTemplateCd();
        String noteToAttachToEvent = requestDISDTO.getNoteToAttachToEvent();
        String token = requestDISDTO.getToken();
        String newAID = requestDISDTO.getAgencyId();
        String corpId = requestDISDTO.getCorpId();

        doWork(sourceSystem,
               sourceCompanyId,
               paymentTemplateCd,
               token,
               corpId,
               newAID,
               noteToAttachToEvent);
        logger.debug("Leaving PSPUpdateCompanyAgencyIds.process()");
        return responseDISDTO;
    }

    private void doWork(SourceSystemEnum pSourceSystemCd,
                        String pSourceCompanyId,
                        String pPaymentTemplateCd,
                        String pToken,
                        String pCorpId,
                        String pNewAID,
                        String pNoteToAttachToEvent) throws Throwable {
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

            try {
                PSPHelper.validateUserHasPermissionsInSAP(pCorpId, pToken, updateAdapterClass, UPDATE_OPERATION_METHOD_NAME);
            } catch (Exception e) {
                responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                return;
            }

            TaxAdapter taxAdapter = new TaxAdapter();

            SAPCompanyAgencyPaymentTemplateAgencyId paymentTemplateToUpdate = getSAPCompanyAgencyPaymentTemplateAgencyId(
                    taxAdapter,
                    pSourceSystemCd,
                    pSourceCompanyId,
                    pPaymentTemplateCd
            );

            if (paymentTemplateToUpdate == null) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.couldNotFindDefaultPaymentTemplateForCompany(pPaymentTemplateCd, pSourceCompanyId);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            if (paymentTemplateToUpdate.getId().equals(pNewAID)) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.existingAIDMatchesUpdateAID(pPaymentTemplateCd, pNewAID);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            paymentTemplateToUpdate.setId(pNewAID);
            List<SAPCompanyAgencyPaymentTemplateAgencyId> paymentTemplatesToUpdate = new ArrayList<SAPCompanyAgencyPaymentTemplateAgencyId>();
            paymentTemplatesToUpdate.add(paymentTemplateToUpdate);

            taxAdapter.updateAgencyIDs(pSourceSystemCd.toString(), pSourceCompanyId, pPaymentTemplateCd, paymentTemplatesToUpdate);

            CompanyEvent coEvent = findUpdatedEvent(company, pPaymentTemplateCd, pNewAID);
            PSPHelper.addCompanyNote(company, pCorpId, coEvent.getId().toString(), pNoteToAttachToEvent);

            List<SAPCompanyAgencyPaymentTemplateAgencyId> checkAgencyIds = taxAdapter.checkAgencyIDs(pSourceSystemCd.toString(), pSourceCompanyId, pPaymentTemplateCd, paymentTemplatesToUpdate);
            // Since we only passed in one payment template in paymentTemplatesToUpdate, we will only get one back.
            if (checkAgencyIds.size() != 1) {
                PayrollServices.rollbackUnitOfWork();
                String errMsg = NO_PAYMENT_TEMPLATE_FOUND_ERROR_MESSAGE;
                errMsg = String.format(errMsg, pPaymentTemplateCd, pSourceCompanyId);
                DISMessage disMessage = DISMessages.systemError(errMsg);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            SAPCompanyAgencyPaymentTemplateAgencyId sapCompanyAgencyPaymentTemplateAgencyId = checkAgencyIds.get(0);
            List<PaymentMethodAgencyIdRequirementsDISDTO> paymentMethodAIDRequirementsList = new ArrayList<PaymentMethodAgencyIdRequirementsDISDTO>();
            for (SAPPaymentMethodAgencyIdRequirements sapPaymentMethodAgencyIdRequirements : sapCompanyAgencyPaymentTemplateAgencyId.getPaymentMethodRequirements()) {
                paymentMethodAIDRequirementsList.add(new PaymentMethodAgencyIdRequirementsDISDTO(sapPaymentMethodAgencyIdRequirements));
            }

            ((UpdateCompanyAgencyIdResponseDISDTO) responseDISDTO).setPaymentMethodAgencyIdRequirements(paymentMethodAIDRequirementsList);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private SAPCompanyAgencyPaymentTemplateAgencyId getSAPCompanyAgencyPaymentTemplateAgencyId(TaxAdapter pTaxAdapter,
                                                                                               SourceSystemEnum pSourceSystemCd,
                                                                                               String pSourceCompanyId,
                                                                                               String pPaymentTemplateCd) throws Throwable {
        List<SAPCompanyAgencyPaymentTemplateAgencyId> currentSAPCompanyAgencyPaymentTemplateAgencyIds =
                pTaxAdapter.findAgencyIDs(
                        pSourceSystemCd.toString(),
                        pSourceCompanyId,
                        pPaymentTemplateCd
                );

        SAPCompanyAgencyPaymentTemplateAgencyId paymentTemplateToUpdate = null;
        if (currentSAPCompanyAgencyPaymentTemplateAgencyIds.size() == 1) {
            paymentTemplateToUpdate = currentSAPCompanyAgencyPaymentTemplateAgencyIds.get(0);
        } else {
            if (currentSAPCompanyAgencyPaymentTemplateAgencyIds.size() > 1) {
                for (SAPCompanyAgencyPaymentTemplateAgencyId sapCompanyAgencyPaymentTemplateAgencyId : currentSAPCompanyAgencyPaymentTemplateAgencyIds) {
                    if (sapCompanyAgencyPaymentTemplateAgencyId.getName() == null) {
                        paymentTemplateToUpdate = currentSAPCompanyAgencyPaymentTemplateAgencyIds.get(0);
                    }
                }
            }
        }
        return paymentTemplateToUpdate;
    }

    public static CompanyEvent findUpdatedEvent(Company pCompany, String pPaymentTemplateCd, String pNewAid) throws Exception {

        // Using this find method because it get the events in order
        // Limiting to last hour.
        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        spcfCalendar.addHours(-1);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventTypeCode.StateIdModified,null,spcfCalendar,null,true);
        if (companyEvents.size() > 0) {
            for (int cnt=companyEvents.size()-1;cnt>=0;cnt--) {
                CompanyEvent companyEvent = companyEvents.get(cnt);
                DomainEntitySet<CompanyEventDetail> paymentTemplateEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.PaymentTemplate);
                if (paymentTemplateEventDetails.size() == 1 && paymentTemplateEventDetails.get(0).getValue().equals(pPaymentTemplateCd)) {
                    DomainEntitySet<CompanyEventDetail> newValueEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewStringValue);
                    if (newValueEventDetails.size() == 1 && newValueEventDetails.get(0).getValue().equals(pNewAid)) {
                        return companyEvent;
                    }
                }
            }
        }
        return null;
    }


    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

}
