package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyLawRateDetailDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyLawRatesHistoryDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryLawRateHistoryRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryLawRateHistoryResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLawRateDetail;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLawRatesHistory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryLawRateHistory.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Query company event Process
 *
 */
public class PSPQueryLawRateHistory extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPQueryLawRateHistory.class);
    }

    private QueryLawRateHistoryRequestDISDTO queryLawRateHistoryRequestDISDTO;
    private QueryLawRateHistoryResponseDISDTO queryLawRateHistoryResponseDISDTO;

    /***
     * Constructor
     * @param pQueryLawRateHistoryRequestDISDTO
     */
    public PSPQueryLawRateHistory(QueryLawRateHistoryRequestDISDTO pQueryLawRateHistoryRequestDISDTO) {
        queryLawRateHistoryRequestDISDTO = pQueryLawRateHistoryRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryLawRateHistory.process()");
        queryLawRateHistoryResponseDISDTO = new QueryLawRateHistoryResponseDISDTO();

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(queryLawRateHistoryRequestDISDTO.getSourceCompanyId(),translateSourceSystemCode(queryLawRateHistoryRequestDISDTO.getSourceSystem()));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(queryLawRateHistoryRequestDISDTO.getSourceCompanyId()));
            }
            PayrollServices.rollbackUnitOfWork();
            CompanyLawRatesHistoryDISDTO companyLawRatesHistoryDISDTO = getLawRateHistory(company, queryLawRateHistoryRequestDISDTO.getPaymentTemplateCd());
            queryLawRateHistoryResponseDISDTO.setCompanyLawRateHistoryDISDTO(companyLawRatesHistoryDISDTO);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.debug("Leaving PSPQueryLawRateHistory.process()");
        return queryLawRateHistoryResponseDISDTO;
    }

    public CompanyLawRatesHistoryDISDTO getLawRateHistory(Company company, String pPaymentTemplateCd) throws Throwable {
        CompanyLawRatesHistoryDISDTO companyLawRatesHistoryDISDTO = new CompanyLawRatesHistoryDISDTO();
        try {

            TaxAdapter taxAdapter = new TaxAdapter();
//            ArrayList<SAPCompanyAgencyDetails> sapCompanyAgencies = taxAdapter.getAgencyDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
            SAPCompanyLawRatesHistory sapCompanyLawRatesHistories = taxAdapter.getCompanyLawRatesHistory(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), pPaymentTemplateCd);
            companyLawRatesHistoryDISDTO.setCompanyLawNames(sapCompanyLawRatesHistories.getCompanyLawNames());
            companyLawRatesHistoryDISDTO.setCompanyLawRateDetails(new ArrayList<CompanyLawRateDetailDISDTO>());
            if (sapCompanyLawRatesHistories.getCompanyLawRateDetails() != null) {
                for (SAPCompanyLawRateDetail sapCompanyLawRateDetail : sapCompanyLawRatesHistories.getCompanyLawRateDetails()) {
                    CompanyLawRateDetailDISDTO companyLawRateDetailDISDTO = new CompanyLawRateDetailDISDTO();
                    companyLawRateDetailDISDTO.setLawName(sapCompanyLawRateDetail.getLawName());
                    companyLawRateDetailDISDTO.setLawId(sapCompanyLawRateDetail.getLawId());
                    companyLawRateDetailDISDTO.setRate(sapCompanyLawRateDetail.getRate());
                    companyLawRateDetailDISDTO.setModifiedBy(sapCompanyLawRateDetail.getChangedBy());
                    if (sapCompanyLawRateDetail.getChangeDate() != null) {
                        Calendar modifiedDate = Calendar.getInstance();
                        modifiedDate.setTime(sapCompanyLawRateDetail.getChangeDate());
                        companyLawRateDetailDISDTO.setModifiedDate(modifiedDate);
                    }
                    if (sapCompanyLawRateDetail.getEffectiveQuarter() != null) {
                        Calendar effectiveDate = CalendarUtils.convertToCalendar(sapCompanyLawRateDetail.getEffectiveQuarter().getFirstDayOfQuarter());
                        companyLawRateDetailDISDTO.setEffectiveDate(effectiveDate);
                    }
                    companyLawRatesHistoryDISDTO.getCompanyLawRateDetails().add(companyLawRateDetailDISDTO);
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return companyLawRatesHistoryDISDTO;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryLawRateHistoryResponseDISDTO;
    }

}
