package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyAgencyYearInfoDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.LawRateDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.PaymentTemplateQuarterPaymentDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.PaymentTemplateYearPaymentDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryCompanyAgenciesYearInfoRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryCompanyAgenciesYearInfoResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Agency;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAgency;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
public class PSPQueryCompanyAgenciesYearInfo extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyAgenciesYearInfo.class);
    }

    private QueryCompanyAgenciesYearInfoRequestDISDTO queryCompanyPayrollDatesRequestDISDTO;
    private QueryCompanyAgenciesYearInfoResponseDISDTO queryCompanyPayrollDatesResponseDISDTO;

    /**
     * Constructor
     *
     * @param pQueryCompanyAgencyYearInfoRequestDISDTO
     *
     */
    public PSPQueryCompanyAgenciesYearInfo(QueryCompanyAgenciesYearInfoRequestDISDTO pQueryCompanyAgencyYearInfoRequestDISDTO) {
        queryCompanyPayrollDatesRequestDISDTO = pQueryCompanyAgencyYearInfoRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyAgenciesYearInfoTests.process()");
        queryCompanyPayrollDatesResponseDISDTO = new QueryCompanyAgenciesYearInfoResponseDISDTO();

        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany(queryCompanyPayrollDatesRequestDISDTO.getSourceCompanyId(), translateSourceSystemCode(queryCompanyPayrollDatesRequestDISDTO.getSourceSystem()));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(queryCompanyPayrollDatesRequestDISDTO.getSourceCompanyId()));
            }
            PayrollServices.rollbackUnitOfWork();

            List<CompanyAgencyYearInfoDISDTO> companyAgencyYearInfoItems = new ArrayList<CompanyAgencyYearInfoDISDTO>();

            TaxAdapter taxAdapter = new TaxAdapter();
            // There is a known bug in SAP where legacy companies that were cancelled before PSP started supporting RAF throw an exception
            //   because of bad data.  The PSP team gets alerts from Producion when these companies are accessed from SAP.  We are checking here
            //   to proactively prevent these error so we don't litter our logs with artificial errors.
            if (isLegacyRAFEnrolledMissing(company)) {
                queryCompanyPayrollDatesResponseDISDTO.setCompanyAgencyYearInfoDISDTO(companyAgencyYearInfoItems);
                return queryCompanyPayrollDatesResponseDISDTO;
            }

            List<SAPAgencyInfoDTO> sapAgencies = taxAdapter.getAgencyInfoArray(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
            for (SAPAgencyInfoDTO sapCompanyAgencyDetails : sapAgencies) {
                CompanyAgencyYearInfoDISDTO companyAgencyYearInfoDISDTO = new CompanyAgencyYearInfoDISDTO();
                companyAgencyYearInfoDISDTO.setAgencyId(sapCompanyAgencyDetails.getAgency().getAgencyId());
                companyAgencyYearInfoDISDTO.setAgencyName(sapCompanyAgencyDetails.getAgency().getAgencyName());
                List<PaymentTemplateYearPaymentDISDTO> paymentTemplateYearPaymentDISDTOItems = new ArrayList<PaymentTemplateYearPaymentDISDTO>();
                for (SAPCompanyPaymentTemplate sapPCompanyPaymentTemplate : sapCompanyAgencyDetails.getCompanyPaymentTemplates()) {
                    PaymentTemplateYearPaymentDISDTO paymentTemplateYearPaymentDISDTO = new PaymentTemplateYearPaymentDISDTO();
                    SAPPaymentTemplate sapPaymentTemplate = sapPCompanyPaymentTemplate.getPaymentTemplate();
                    if (sapPaymentTemplate != null) {
                        SAPPaymentTemplateYearPayment sapPaymentTemplateYearPayment = taxAdapter.getTemplateYearPayment(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), queryCompanyPayrollDatesRequestDISDTO.getTaxYear()+"", sapPaymentTemplate.getPaymentTemplateCd());
                        paymentTemplateYearPaymentDISDTO.setAgencyTaxPayerId(sapPCompanyPaymentTemplate.getAgencyTaxpayerId()); //todo
                        List<LawRateDISDTO> lawRates = new ArrayList<LawRateDISDTO>();
                        for (SAPCompanyLawRateDetail sapCompanyLawRateDetail : sapPCompanyPaymentTemplate.getLawRates()) {
                            LawRateDISDTO lawRateDISDTO = new LawRateDISDTO();
                            lawRateDISDTO.setActive(!sapCompanyLawRateDetail.getInactive());
                            lawRateDISDTO.setEffectiveDate(sapCompanyLawRateDetail.getEffectiveQuarter() == null ? null : CalendarUtils.convertToDate(sapCompanyLawRateDetail.getEffectiveQuarter().getFirstDayOfQuarter()));
                            lawRateDISDTO.setLawName(sapCompanyLawRateDetail.getLawName());
                            lawRateDISDTO.setRate(sapCompanyLawRateDetail.getRate());
                            lawRates.add(lawRateDISDTO);
                        }
                        paymentTemplateYearPaymentDISDTO.setLawRates(lawRates);
                        List<PaymentTemplateQuarterPaymentDISDTO> quarterPayrments = new ArrayList<PaymentTemplateQuarterPaymentDISDTO>();
                        for (SAPPaymentTemplateQuarterPayment sapPaymentTemplateQuarterPayment : sapPaymentTemplateYearPayment.getTemplateQuarterPayments()) {
                            PaymentTemplateQuarterPaymentDISDTO paymentTemplateQuarterPaymentDISDTO = new PaymentTemplateQuarterPaymentDISDTO();
                            paymentTemplateQuarterPaymentDISDTO.setNotStarted(sapPaymentTemplateQuarterPayment.getNotStarted());
                            paymentTemplateQuarterPaymentDISDTO.setPaymentsMadeTotal(sapPaymentTemplateQuarterPayment.getPaymentsMadeTotal());
                            paymentTemplateQuarterPaymentDISDTO.setPaymentTemplateCd(sapPaymentTemplateQuarterPayment.getPaymentTemplateCd());
                            paymentTemplateQuarterPaymentDISDTO.setPaymentTemplateName(sapPaymentTemplateQuarterPayment.getPaymentTemplateName());
                            paymentTemplateQuarterPaymentDISDTO.setPendingPaymentsTotal(sapPaymentTemplateQuarterPayment.getPendingPaymentsTotal());
                            paymentTemplateQuarterPaymentDISDTO.setQuarter(sapPaymentTemplateQuarterPayment.getQuarter());
                            paymentTemplateQuarterPaymentDISDTO.setQuarterPaymentsTotal(sapPaymentTemplateQuarterPayment.getQuarterPaymentsTotal());
                            paymentTemplateQuarterPaymentDISDTO.setYear(sapPaymentTemplateQuarterPayment.getYear());
                            quarterPayrments.add(paymentTemplateQuarterPaymentDISDTO);
                        }
                        paymentTemplateYearPaymentDISDTO.setTemplateQuarterPayments(quarterPayrments);
                        paymentTemplateYearPaymentDISDTO.setPendingPaymentsTotal(sapPaymentTemplateYearPayment.getPendingPaymentsTotal());
                        paymentTemplateYearPaymentDISDTO.setPaymentsMadeTotal(sapPaymentTemplateYearPayment.getPaymentsMadeTotal());
                        paymentTemplateYearPaymentDISDTO.setPaymentTemplateCd(sapPaymentTemplateYearPayment.getPaymentTemplateCd());
                        paymentTemplateYearPaymentDISDTO.setTaxYear(sapPaymentTemplateYearPayment.getTaxYear());
                        paymentTemplateYearPaymentDISDTO.setYearPaymentsTotal(sapPaymentTemplateYearPayment.getYearPaymentsTotal());
                        paymentTemplateYearPaymentDISDTOItems.add(paymentTemplateYearPaymentDISDTO);
                    }
                }
                companyAgencyYearInfoDISDTO.setPaymentTemplateYearPayment(paymentTemplateYearPaymentDISDTOItems);
                companyAgencyYearInfoItems.add(companyAgencyYearInfoDISDTO);
            }
            queryCompanyPayrollDatesResponseDISDTO.setCompanyAgencyYearInfoDISDTO(companyAgencyYearInfoItems);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return queryCompanyPayrollDatesResponseDISDTO;
    }

    private boolean isLegacyRAFEnrolledMissing(Company pCompany) {
        CompanyAgency irsAgency = CompanyAgency.findCompanyAgency(pCompany, Agency.IRS);
        return irsAgency == null;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryCompanyPayrollDatesResponseDISDTO;
    }
}
