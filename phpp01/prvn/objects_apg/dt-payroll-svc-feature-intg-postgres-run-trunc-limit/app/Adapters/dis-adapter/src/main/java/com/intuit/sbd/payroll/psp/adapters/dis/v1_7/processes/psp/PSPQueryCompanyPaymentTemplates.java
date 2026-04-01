package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.*;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyPaymentTemplatesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyPaymentTemplatesResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryCompanyPaymentTemplates.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPQueryCompanyPaymentTemplates extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyPaymentTemplates.class);
    }

    private QueryCompanyPaymentTemplatesRequestDISDTO queryCompanyPaymentTemplatesRequestDISDTO;
    private QueryCompanyPaymentTemplatesResponseDISDTO queryCompanyPaymentTemplatesResponseDISDTO;

    /**
     * Constructor
     *
     * @param pQueryCompanyPaymentTemplatesRequestDISDTO
     *
     */
    public PSPQueryCompanyPaymentTemplates(QueryCompanyPaymentTemplatesRequestDISDTO pQueryCompanyPaymentTemplatesRequestDISDTO) {
        queryCompanyPaymentTemplatesRequestDISDTO = pQueryCompanyPaymentTemplatesRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyPaymentTemplates.process()");
        queryCompanyPaymentTemplatesResponseDISDTO = new QueryCompanyPaymentTemplatesResponseDISDTO();

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(queryCompanyPaymentTemplatesRequestDISDTO.getSourceCompanyId(), translateSourceSystemCode(queryCompanyPaymentTemplatesRequestDISDTO.getSourceSystem()));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(queryCompanyPaymentTemplatesRequestDISDTO.getSourceCompanyId()));
            }
            PayrollServices.rollbackUnitOfWork();
            List<CompanyPaymentTemplateDISDTO> companyPmtTemplates = getCompanyPaymentTemplates(company, queryCompanyPaymentTemplatesRequestDISDTO.getPaymentTemplateCd());
            queryCompanyPaymentTemplatesResponseDISDTO.setCompanyPaymentTemplates(companyPmtTemplates);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.debug("Leaving PSPQueryCompanyPaymentTemplates.process()");
        return queryCompanyPaymentTemplatesResponseDISDTO;
    }

    public List<CompanyPaymentTemplateDISDTO> getCompanyPaymentTemplates(Company company, String pPaymentTemplateCd) throws Throwable {
        List<CompanyPaymentTemplateDISDTO> companyLawDISDTOs = new ArrayList<CompanyPaymentTemplateDISDTO>();
        try {

            TaxAdapter taxAdapter = new TaxAdapter();
//            ArrayList<SAPCompanyAgencyDetails> sapCompanyAgencies = taxAdapter.getAgencyDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
            List<SAPAgencyInfoDTO> sapAgencyInfoDTOs = taxAdapter.getAgencyInfoArray(company.getSourceSystemCd().toString(), company.getSourceCompanyId());

            // CF - If a blank payment template is passed in the set it to null so all payment templates are returned
            if (pPaymentTemplateCd != null && pPaymentTemplateCd.length() == 0) {
                pPaymentTemplateCd = null;
            }

            for (SAPAgencyInfoDTO sapAgencyInfoDTO : sapAgencyInfoDTOs) {
                for (SAPCompanyPaymentTemplate sapCompanyPaymentTemplate : sapAgencyInfoDTO.getCompanyPaymentTemplates()) {
                    // SAP returns all payment templates.  If only one requested, skip all others.
                    // Only return the requested item.
                    if (pPaymentTemplateCd != null) {
                        if (sapCompanyPaymentTemplate.getPaymentTemplate() == null
                                || sapCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd() == null
                                || sapCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().compareTo(pPaymentTemplateCd) != 0) {
                            continue;
                        }
                    }
                    CompanyPaymentTemplateDISDTO companyPaymentTemplateDISDTO = new CompanyPaymentTemplateDISDTO();
                    companyPaymentTemplateDISDTO.setAgencyID(sapAgencyInfoDTO.getAgency().getAgencyId());
                    companyPaymentTemplateDISDTO.setAgencyName(sapAgencyInfoDTO.getAgency().getAgencyName());
                    companyPaymentTemplateDISDTO.setAgencyTaxpayerId(sapCompanyPaymentTemplate.getAgencyTaxpayerId());

                    List<CompanyLawRateDetailDISDTO> companyLawRateDetailList = new ArrayList<CompanyLawRateDetailDISDTO>();
                    for (SAPCompanyLawRateDetail sapCompanyLawRateDetail : sapCompanyPaymentTemplate.getLawRates()) {
                        CompanyLawRateDetailDISDTO companyLawRateDetailDISDTO = new CompanyLawRateDetailDISDTO();
                        if (sapCompanyLawRateDetail.getEffectiveQuarter() != null) {
                            Calendar effectiveDate = CalendarUtils.convertToCalendar(sapCompanyLawRateDetail.getEffectiveQuarter().getFirstDayOfQuarter());
                            companyLawRateDetailDISDTO.setEffectiveDate(effectiveDate);
                        }
                        companyLawRateDetailDISDTO.setRate(sapCompanyLawRateDetail.getRate());
                        companyLawRateDetailDISDTO.setExempt(sapCompanyLawRateDetail.getExempt());
                        companyLawRateDetailDISDTO.setSourceLawDescription(sapCompanyLawRateDetail.getSourceLawDescription());
                        companyLawRateDetailDISDTO.setSourceLawId(sapCompanyLawRateDetail.getSourceLawID());
                        companyLawRateDetailDISDTO.setActive(!sapCompanyLawRateDetail.getInactive());
                        companyLawRateDetailDISDTO.setAgencyId(sapCompanyLawRateDetail.getAgencyId());
                        companyLawRateDetailDISDTO.setLawName(sapCompanyLawRateDetail.getLawName());
                        companyLawRateDetailDISDTO.setLawId(sapCompanyLawRateDetail.getLawId());
                        companyLawRateDetailDISDTO.setExempt(sapCompanyLawRateDetail.getExempt());
                        companyLawRateDetailList.add(companyLawRateDetailDISDTO);
                    }
                    companyPaymentTemplateDISDTO.setCompanyLawRatesDetails(companyLawRateDetailList);

                    List<CompanyLawItemDISDTO> companyLawItemList = new ArrayList<CompanyLawItemDISDTO>();
                    //todo joe this is old gemini code and has been removed (was never populated in prod)
                    /*if (sapCompanyPaymentTemplate.getLawItems() != null) {
                        for (SAPCompanyLawItem sapCompanyLawItem : sapCompanyPaymentTemplate.getLawItems()) {
                            CompanyLawItemDISDTO companyLawItemDISDTO = new CompanyLawItemDISDTO();
                            SAPLawItem lawItem = sapCompanyLawItem.getLawItem();
                            if (lawItem != null) {
                                companyLawItemDISDTO.setPaymentTemplateCd(lawItem.getPaymentTemplateCd());
                                companyLawItemDISDTO.setSourceLawDescription(lawItem.getDescription());
                                companyLawItemDISDTO.setLawName(lawItem.getName());
                            }

                            SAPLawItemRate currentSAPLawItemRate = sapCompanyLawItem.getCurrentLawItemRate();
                            CompanyLawRateDetailDISDTO currentLawRateDetail = new CompanyLawRateDetailDISDTO();
                            if (currentSAPLawItemRate.getEffectiveDate() != null) {
                                Calendar effectiveDate = Calendar.getInstance();
                                effectiveDate.setTime(currentSAPLawItemRate.getEffectiveDate());
                                currentLawRateDetail.setEffectiveDate(effectiveDate);
                            }
                            currentLawRateDetail.setRate(currentSAPLawItemRate.getRate());
                            companyLawItemDISDTO.setCurrentLawRate(currentLawRateDetail);

                            SAPLawItemRate futureSAPLawItemRate = sapCompanyLawItem.getFutureLawItemRate();
                            CompanyLawRateDetailDISDTO futureLawRateDetail = new CompanyLawRateDetailDISDTO();
                            if (futureSAPLawItemRate.getEffectiveDate() != null) {
                                Calendar effectiveDate = Calendar.getInstance();
                                effectiveDate.setTime(futureSAPLawItemRate.getEffectiveDate());
                                futureLawRateDetail.setEffectiveDate(effectiveDate);
                            }
                            futureLawRateDetail.setRate(futureSAPLawItemRate.getRate());
                            companyLawItemDISDTO.setFutureLawRate(futureLawRateDetail);


                            companyLawItemList.add(companyLawItemDISDTO);
                        }
                    }*/

                    companyPaymentTemplateDISDTO.setCompanyLawItems(companyLawItemList);

                    SAPDepositFrequency currentSAPDepositFrequency = sapCompanyPaymentTemplate.getCurrentDepositFrequency();
                    CompanyDepositFrequencyDISDTO currentCompanyDepositFrequencyDISDTO = new CompanyDepositFrequencyDISDTO();
                    currentCompanyDepositFrequencyDISDTO.setDepositFrequency(currentSAPDepositFrequency.getDepositFrequency());

                    if (currentSAPDepositFrequency.getEffectiveDate() != null) {
                        Calendar effectiveDate = Calendar.getInstance();
                        effectiveDate.setTime(currentSAPDepositFrequency.getEffectiveDate());
                        currentCompanyDepositFrequencyDISDTO.setEffectiveDate(effectiveDate);
                    }
                    companyPaymentTemplateDISDTO.setCurrentDepositFrequency(currentCompanyDepositFrequencyDISDTO);

                    SAPDepositFrequency futureSAPDepositFrequency = sapCompanyPaymentTemplate.getFutureDepositFrequency();
                    if (futureSAPDepositFrequency != null) {
                        CompanyDepositFrequencyDISDTO futureCompanyDepositFrequencyDISDTO = new CompanyDepositFrequencyDISDTO();
                        futureCompanyDepositFrequencyDISDTO.setDepositFrequency(futureSAPDepositFrequency.getDepositFrequency());

                        if (futureSAPDepositFrequency.getEffectiveDate() != null) {
                            Calendar effectiveDate = Calendar.getInstance();
                            effectiveDate.setTime(futureSAPDepositFrequency.getEffectiveDate());
                            futureCompanyDepositFrequencyDISDTO.setEffectiveDate(effectiveDate);
                        }
                        companyPaymentTemplateDISDTO.setFutureDepositFrequency(futureCompanyDepositFrequencyDISDTO);
                    }

                    List<CompanyPaymentMethodDISDTO> companyPaymentMethods = new ArrayList<CompanyPaymentMethodDISDTO>();
                    List<SAPPaymentMethod> sapPaymentMethods = sapCompanyPaymentTemplate.getPaymentMethods();
                    if (sapPaymentMethods != null) {
                        for (SAPPaymentMethod sapPaymentMethod : sapPaymentMethods) {
                            CompanyPaymentMethodDISDTO companyPaymentMethodDISDTO = new CompanyPaymentMethodDISDTO();
                            companyPaymentMethodDISDTO.setEnabled(sapPaymentMethod.getIsEnabled());
                            companyPaymentMethodDISDTO.setPaymentMethodName(sapPaymentMethod.getPaymentMethodName());
                            companyPaymentMethodDISDTO.setRequirements(sapPaymentMethod.getRequirements());
                            companyPaymentMethods.add(companyPaymentMethodDISDTO);
                        }
                    }
                    companyPaymentTemplateDISDTO.setCompanyPaymentMethods(companyPaymentMethods);

                    companyPaymentTemplateDISDTO.setIs944Filer(sapCompanyPaymentTemplate.isIs944Filer());
                    companyPaymentTemplateDISDTO.setRegisteredForACH(sapCompanyPaymentTemplate.isRegisteredForACH());
                    SAPPaymentTemplate sapPaymentTemplate = sapCompanyPaymentTemplate.getPaymentTemplate();
                    if (sapPaymentTemplate != null) {
                        companyPaymentTemplateDISDTO.setPaymentTemplateCd(sapPaymentTemplate.getPaymentTemplateCd());
                        companyPaymentTemplateDISDTO.setPaymentTemplateName(sapPaymentTemplate.getPaymentTemplateName());
                    }
                    companyLawDISDTOs.add(companyPaymentTemplateDISDTO);
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return companyLawDISDTOs;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryCompanyPaymentTemplatesResponseDISDTO;
    }

}
