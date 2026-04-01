package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyPaymentMethodDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryPaymentMethodHistoryRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryPaymentMethodHistoryResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentMethod;
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
public class PSPQueryPaymentMethodHistory extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPQueryPaymentMethodHistory.class);
    }

    private QueryPaymentMethodHistoryRequestDISDTO queryTaxPaymentHistoryRequestDISDTO;
    private QueryPaymentMethodHistoryResponseDISDTO queryTaxPaymentHistoryResponseDISDTO;

    /***
     * Constructor
     * @param pQueryTaxPaymentHistoryRequestDISDTO
     */
    public PSPQueryPaymentMethodHistory(QueryPaymentMethodHistoryRequestDISDTO pQueryTaxPaymentHistoryRequestDISDTO) {
        queryTaxPaymentHistoryRequestDISDTO = pQueryTaxPaymentHistoryRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryTaxPaymentHistory.process()");
        queryTaxPaymentHistoryResponseDISDTO = new QueryPaymentMethodHistoryResponseDISDTO();

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(queryTaxPaymentHistoryRequestDISDTO.getSourceCompanyId(),translateSourceSystemCode(queryTaxPaymentHistoryRequestDISDTO.getSourceSystem()));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(queryTaxPaymentHistoryRequestDISDTO.getSourceCompanyId()));
            }
            PayrollServices.rollbackUnitOfWork();
            List<CompanyPaymentMethodDISDTO> companyTaxPaymentHistoryItems = getPaymentMethodHistory(company, queryTaxPaymentHistoryRequestDISDTO.getPaymentTemplateCd());
            queryTaxPaymentHistoryResponseDISDTO.setCompanyPaymentMethods(companyTaxPaymentHistoryItems);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.debug("Leaving PSPQueryTaxPaymentHistory.process()");
        return queryTaxPaymentHistoryResponseDISDTO;
    }

    public List<CompanyPaymentMethodDISDTO> getPaymentMethodHistory(Company company, String pPaymentTemplateCd) throws Throwable {
        List<CompanyPaymentMethodDISDTO> taxPaymentHistoryItems = new ArrayList<CompanyPaymentMethodDISDTO>();
        try {

            TaxAdapter taxAdapter = new TaxAdapter();
//            ArrayList<SAPCompanyAgencyDetails> sapCompanyAgencies = taxAdapter.getAgencyDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
            List<SAPPaymentMethod> sapPaymentMethods = taxAdapter.getPaymentMethodsHistory(company.getSourceSystemCd().toString(), company.getSourceCompanyId(),pPaymentTemplateCd,"Enabled");
            for (SAPPaymentMethod sapPaymentMethod : sapPaymentMethods) {

                CompanyPaymentMethodDISDTO companyPaymentMethodDISDTO = new CompanyPaymentMethodDISDTO();
                companyPaymentMethodDISDTO.setEnabled(sapPaymentMethod.getIsEnabled());
                companyPaymentMethodDISDTO.setPaymentMethodName(sapPaymentMethod.getPaymentMethodName());
                companyPaymentMethodDISDTO.setChangedBy(sapPaymentMethod.getChangedBy());
                companyPaymentMethodDISDTO.setRequirements(sapPaymentMethod.getRequirements());
                if (sapPaymentMethod.getModifiedDate() != null) {
                    Calendar modifiedDate = Calendar.getInstance();
                    modifiedDate.setTime(sapPaymentMethod.getModifiedDate());
                    companyPaymentMethodDISDTO.setModifiedDate(modifiedDate);
                }
                taxPaymentHistoryItems.add(companyPaymentMethodDISDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return taxPaymentHistoryItems;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryTaxPaymentHistoryResponseDISDTO;
    }

}
