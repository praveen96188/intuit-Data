package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.dao.DataStore;
import com.intuit.payroll.agency.dao.LawData;
import com.intuit.payroll.agency.impl.AgencyFormat;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.*;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.GetAgencyRulesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.GetAgencyRulesResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.LawCategoryCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemLawAssoc;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPGetAgencyRules.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Query company event Process
 *
 */
public class PSPGetAgencyRules extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPGetAgencyRules.class);
    }

    private GetAgencyRulesRequestDISDTO getAgencyRulesRequestDISDTO;
    private GetAgencyRulesResponseDISDTO getAgencyRulesResponseDISDTO;

    /***
     * Constructor
     * @param pGetAgencyRulesRequestDISDTO
     */
    public PSPGetAgencyRules(GetAgencyRulesRequestDISDTO pGetAgencyRulesRequestDISDTO) {
        getAgencyRulesRequestDISDTO = pGetAgencyRulesRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPGetAgencyRules.process()");
        getAgencyRulesResponseDISDTO = new GetAgencyRulesResponseDISDTO();

        try {
            List<AgencyRulesAgencyDISDTO> agencyRulesAgencyDISDTOs = getAgencyRules();
            getAgencyRulesResponseDISDTO.setAgencies(agencyRulesAgencyDISDTOs);
        } finally {
        }
        logger.debug("Leaving PSPGetAgencyRules.process()");
        return getAgencyRulesResponseDISDTO;
    }

    private List<AgencyRulesAgencyDISDTO> getAgencyRules() {
        List<AgencyRulesAgencyDISDTO> agencyRulesAgencyDISDTOs = new ArrayList<AgencyRulesAgencyDISDTO>();
        DataStore store = DataStore.getDataStore();

        Map<String,AgencyRulesAgencyDISDTO> agencyMap = new TreeMap<String, AgencyRulesAgencyDISDTO>();

        IRulesList paymentTemplateList = store.getActivePaymentTemplateIDList();
//        List<AgencyRulesPaymentTemplateDISDTO> agencyRulesPaymentTemplateDISDTOs = null;
        PayrollServices.beginUnitOfWork();
        try {
            for (int paymentTemplateCntr = 0; paymentTemplateCntr < paymentTemplateList.getCount(); paymentTemplateCntr++) {
                AgencyRulesPaymentTemplateDISDTO agencyRulesPaymentTemplateDISDTO = new AgencyRulesPaymentTemplateDISDTO();
                IRulesPaymentTemplate paymentTemplate = store.getPaymentTemplate((String) paymentTemplateList.getItem(paymentTemplateCntr));

                AgencyRulesAgencyDISDTO agencyRulesAgencyDISDTO = null;

                if (!agencyMap.containsKey(paymentTemplate.getAgencyID())) {
                    agencyRulesAgencyDISDTO = new AgencyRulesAgencyDISDTO();
                    IAgency agency = store.getAgency(paymentTemplate.getAgencyID());
                    agencyRulesAgencyDISDTO.setAgencyId(agency.getAgencyID());
                    agencyRulesAgencyDISDTO.setAgencyAbbrev(agency.getAgencyAbbrev());
                    agencyRulesAgencyDISDTO.setUiString(agency.getDescription());
                    agencyRulesAgencyDISDTO.setJurisdictionId(agency.getJurisdictionID());
                    agencyRulesAgencyDISDTO.setName(agency.getName());
                    agencyRulesAgencyDISDTO.setPaymentTemplates(new ArrayList<AgencyRulesPaymentTemplateDISDTO>());
                    agencyRulesAgencyDISDTOs.add(agencyRulesAgencyDISDTO);
                    agencyMap.put(agency.getAgencyID(),agencyRulesAgencyDISDTO);
                } else {
                    agencyRulesAgencyDISDTO = agencyMap.get(paymentTemplate.getAgencyID());
                }

                agencyRulesPaymentTemplateDISDTO.setPaymentTemplateAbbrev(paymentTemplate.getPaymentTemplateAbbrev());
                agencyRulesPaymentTemplateDISDTO.setPaymentTemplateId(paymentTemplate.getPaymentTemplateID());
                agencyRulesPaymentTemplateDISDTO.setDescription(paymentTemplate.getDescription());
                agencyRulesAgencyDISDTO.getPaymentTemplates().add(agencyRulesPaymentTemplateDISDTO);

                IRulesList paymentFrequencyList = store.getActivePaymentFrequencyIDList(paymentTemplate.getPaymentTemplateID());
                List<AgencyRulesPaymentFrequencyDISDTO> agencyRulesPaymentFrequencyDISDTOs = new ArrayList<AgencyRulesPaymentFrequencyDISDTO>();
                for (int paymentFrequencyCntr = 0; paymentFrequencyCntr < paymentFrequencyList.getCount(); paymentFrequencyCntr++) {
                    AgencyRulesPaymentFrequencyDISDTO agencyRulesPaymentFrequencyDISDTO = new AgencyRulesPaymentFrequencyDISDTO();
                    IPaymentFrequency paymentFreq = store.getPaymentFrequency(paymentTemplate.getPaymentTemplateID(), (String) paymentFrequencyList.getItem(paymentFrequencyCntr));
                    agencyRulesPaymentFrequencyDISDTO.setPaymentFrequencyId(paymentFreq.getPaymentFrequencyID());
                    agencyRulesPaymentFrequencyDISDTO.setUiDescription(paymentFreq.getDescription());
                    agencyRulesPaymentFrequencyDISDTOs.add(agencyRulesPaymentFrequencyDISDTO);
                }
                agencyRulesPaymentTemplateDISDTO.setPaymentFrequencies(agencyRulesPaymentFrequencyDISDTOs);

                IRulesList lawList = store.getLawIDList(paymentTemplate.getPaymentTemplateID());
                List<AgencyRulesLawDISDTO> agencyRulesLawDISDTOs = new ArrayList<AgencyRulesLawDISDTO>();
                for (int lawCntr = 0; lawCntr < lawList.getCount(); lawCntr++) {
                    AgencyRulesLawDISDTO agencyRulesLawDISDTO = new AgencyRulesLawDISDTO();
                    LawData law = store.getLawFromId((String) ((Integer)lawList.getItem(lawCntr)).toString());
                    agencyRulesLawDISDTO.setLawId(law.getLawID());
                    agencyRulesLawDISDTO.setLawAbbrev(law.getLawAbbrev());
                    agencyRulesLawDISDTO.setDescription(law.getDescription());
                    //@TODO Put in real values once implemented

                    if (law.getLawID() != null) {
                        Expression<Law> lawQuery = new Query<Law>().Where(Law.LawId().equalTo(law.getLawID().toString()));
                        DomainEntitySet<Law> laws = Application.find(Law.class, lawQuery);
                        if (laws.size() == 1) {
                            Law pspLaw = laws.get(0);
                            // FUTA does not have an id
                            if (!pspLaw.isFUTA()) {
                                try {
                                    String sourceId = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(SourceSystemCode.QBDT,pspLaw);
                                    agencyRulesLawDISDTO.setAs400TaxCode(sourceId);
                                } catch (Exception e) {
                                    // If law id not found, log error but do not stop rest of agency rules from loading.
                                    logger.error(law.getLawID() + " not found in PSP Get Agency Rules.");
                                }
                            }
                            if (pspLaw.getLawCategoryCode()== LawCategoryCode.UnemploymentEmployer) {
                                agencyRulesLawDISDTO.setTaxType("UI");
                            }
                            if (pspLaw.getLawCategoryCode()== LawCategoryCode.Withholding) {
                                agencyRulesLawDISDTO.setTaxType("WH");
                            }
                        } else {
                            // If law id not found, log error but do not stop rest of agency rules from loading.
                            logger.error(law.getLawID() + " found twice in PSP Get Agency Rules.");
                        }
                    }
                    agencyRulesLawDISDTOs.add(agencyRulesLawDISDTO);
                }
                agencyRulesPaymentTemplateDISDTO.setLaws(agencyRulesLawDISDTOs);

                //@TODO Put in real values once implemented
                List<AgencyRulesAgencyIDFormatDISDTO> agencyIDFormats = new ArrayList<AgencyRulesAgencyIDFormatDISDTO>();
                IRulesList agencyIdFormats = paymentTemplate.getAgencyFormats();
                for (int aidCntr=0,numberOfAIDs=agencyIdFormats.getCount();aidCntr<numberOfAIDs;aidCntr++) {
                    AgencyFormat agencyFormat = (AgencyFormat)agencyIdFormats.getItem(aidCntr);
                    AgencyRulesAgencyIDFormatDISDTO agencyRulesAgencyIDFormatDISDTO = new AgencyRulesAgencyIDFormatDISDTO();
                    agencyRulesAgencyIDFormatDISDTO.setAgencyIDFormat(agencyFormat.getFormat());
                    agencyRulesAgencyIDFormatDISDTO.setRegularExpression(agencyFormat.getRegularExpression());
                    agencyIDFormats.add(agencyRulesAgencyIDFormatDISDTO);
                }

                agencyRulesPaymentTemplateDISDTO.setAgencyIDFormats(agencyIDFormats);

                //@TODO Move setting of freq to method so code here and code above is centralized
                IPaymentFrequency paymentFreq = store.getPaymentFrequency(paymentTemplate.getPaymentTemplateID(), paymentTemplate.getDefaultPaymentFrequencyID());
                if (paymentFreq != null) {
                    AgencyRulesPaymentFrequencyDISDTO agencyRulesPaymentFrequencyDISDTO = new AgencyRulesPaymentFrequencyDISDTO();
                    agencyRulesPaymentFrequencyDISDTO.setPaymentFrequencyId(paymentFreq.getPaymentFrequencyID());
                    agencyRulesPaymentFrequencyDISDTO.setUiDescription(paymentFreq.getDescription());
                    agencyRulesPaymentTemplateDISDTO.setDefaultPaymentFrequency(agencyRulesPaymentFrequencyDISDTO);
                }
                agencyRulesPaymentTemplateDISDTO.setUsesFrequencyOf(paymentTemplate.getUsesFrequencyOf());
    //            agencyRulesPaymentFrequencyDISDTOs.add(agencyRulesPaymentFrequencyDISDTO);

            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return agencyRulesAgencyDISDTOs;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return getAgencyRulesResponseDISDTO;
    }
}
