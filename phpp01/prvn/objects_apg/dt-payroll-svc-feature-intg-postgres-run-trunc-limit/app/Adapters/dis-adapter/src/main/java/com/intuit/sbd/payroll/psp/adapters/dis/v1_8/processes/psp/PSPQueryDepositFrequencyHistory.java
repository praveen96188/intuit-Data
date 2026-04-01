package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyDepositFrequencyDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryDepositFrequencyHistoryRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryDepositFrequencyHistoryResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDepositFrequency;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Query company event Process
 *
 */
public class PSPQueryDepositFrequencyHistory extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPQueryDepositFrequencyHistory.class);
    }

    private QueryDepositFrequencyHistoryRequestDISDTO queryDepositFrequencyHistoryRequestDISDTO;
    private QueryDepositFrequencyHistoryResponseDISDTO queryDepositFrequencyHistoryResponseDISDTO;

    /***
     * Constructor
     * @param pQueryDepositFrequencyHistoryRequestDISDTO
     */
    public PSPQueryDepositFrequencyHistory(QueryDepositFrequencyHistoryRequestDISDTO pQueryDepositFrequencyHistoryRequestDISDTO) {
        queryDepositFrequencyHistoryRequestDISDTO = pQueryDepositFrequencyHistoryRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryDepositFrequencyHistory.process()");
        queryDepositFrequencyHistoryResponseDISDTO = new QueryDepositFrequencyHistoryResponseDISDTO();

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(queryDepositFrequencyHistoryRequestDISDTO.getSourceCompanyId(),translateSourceSystemCode(queryDepositFrequencyHistoryRequestDISDTO.getSourceSystem()));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(queryDepositFrequencyHistoryRequestDISDTO.getSourceCompanyId()));
            }
            PayrollServices.rollbackUnitOfWork();
            List<CompanyDepositFrequencyDISDTO> companyDepositFrequencyHistoryItems = getDepositFrequencyHistory(company, queryDepositFrequencyHistoryRequestDISDTO.getPaymentTemplateCd());
            queryDepositFrequencyHistoryResponseDISDTO.setCompanyDepositFrequencies(companyDepositFrequencyHistoryItems);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.debug("Leaving PSPQueryDepositFrequencyHistory.process()");
        return queryDepositFrequencyHistoryResponseDISDTO;
    }

    public List<CompanyDepositFrequencyDISDTO> getDepositFrequencyHistory(Company company, String pPaymentTemplateCd) throws Throwable {
        List<CompanyDepositFrequencyDISDTO> depositFrequencyHistoryItems = new ArrayList<CompanyDepositFrequencyDISDTO>();
        try {

            TaxAdapter taxAdapter = new TaxAdapter();
//            ArrayList<SAPCompanyAgencyDetails> sapCompanyAgencies = taxAdapter.getAgencyDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
            ArrayList<SAPDepositFrequency> sapDepositFrequencies = taxAdapter.getDepositFrequencyHistory(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), pPaymentTemplateCd);
            for (SAPDepositFrequency sapDepositFrequency : sapDepositFrequencies) {

                CompanyDepositFrequencyDISDTO companyDepositFrequencyDISDTO = new CompanyDepositFrequencyDISDTO();
                companyDepositFrequencyDISDTO.setDepositFrequency(sapDepositFrequency.getDepositFrequency());
                companyDepositFrequencyDISDTO.setModifierId(sapDepositFrequency.getModifierId());
                if (sapDepositFrequency.getModifiedDate() != null) {
                    Calendar modifiedDate = Calendar.getInstance();
                    modifiedDate.setTime(sapDepositFrequency.getModifiedDate());
                    companyDepositFrequencyDISDTO.setModifiedDate(modifiedDate);
                }
                if (sapDepositFrequency.getEffectiveDate() != null) {
                    Calendar effectiveDate = Calendar.getInstance();
                    effectiveDate.setTime(sapDepositFrequency.getEffectiveDate());
                    companyDepositFrequencyDISDTO.setEffectiveDate(effectiveDate);
                }
                depositFrequencyHistoryItems.add(companyDepositFrequencyDISDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return depositFrequencyHistoryItems;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryDepositFrequencyHistoryResponseDISDTO;
    }

}
