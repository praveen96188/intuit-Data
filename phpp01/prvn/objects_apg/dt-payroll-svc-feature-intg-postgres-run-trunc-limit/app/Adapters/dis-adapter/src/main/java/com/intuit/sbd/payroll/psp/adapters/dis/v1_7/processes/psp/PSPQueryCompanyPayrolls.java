package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyPayrollDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyPayrollsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyPayrollsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollRun;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryCompanyPayrolls.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPQueryCompanyPayrolls extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyPayrolls.class);
    }

    public static final int MAX_RESULT_CNT = 1000;

    private QueryCompanyPayrollsRequestDISDTO requestDISDTO;
    private QueryCompanyPayrollsResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pQueryCompanyPayrollsDISDTO
     *
     */
    public PSPQueryCompanyPayrolls(QueryCompanyPayrollsRequestDISDTO pQueryCompanyPayrollsDISDTO) {
        requestDISDTO = pQueryCompanyPayrollsDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyPayrolls.process()");
        responseDISDTO = new QueryCompanyPayrollsResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();
        Calendar fromDate = requestDISDTO.getFromDate();
        Calendar toDate = requestDISDTO.getToDate();

//        Calendar startDate = requestDISDTO.getStartDate();
//        Calendar endDate = requestDISDTO.getEndDate();
//        ServiceCode serviceCode = requestDISDTO.getServiceCode();
        List<CompanyPayrollDISDTO> companyPayrolls = doWork(sourceSystem,sourceCompanyId,fromDate,toDate);
        responseDISDTO.setCompanyPayrollDISDTOs(companyPayrolls);
        logger.debug("Leaving PSPQueryCompanyPayrolls.process()");
        return responseDISDTO;
    }

    private List<CompanyPayrollDISDTO> doWork(SourceSystemEnum pSourceSystemCd,String pSourceCompanyId,Calendar pFromDate,Calendar pToDate) throws Throwable {

        List<CompanyPayrollDISDTO> companyPayrollDISDTOs = new ArrayList<CompanyPayrollDISDTO>();
        try {
            PayrollServices.beginUnitOfWork();
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            Company company = Company.findCompany(pSourceCompanyId, translateSourceSystemCode(pSourceSystemCd));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(pSourceCompanyId));
            }

            PayrollServices.rollbackUnitOfWork();

            ArrayList<SAPPayrollRun> payrollRuns = payrollRunAdapter.findPayrollRunsByDate(
                    company.getSourceCompanyId()
                    ,company.getSourceSystemCd().toString()
                    ,null, null, null);

            Date fromDate = null;
            if (pFromDate != null) {
                fromDate = pFromDate.getTime();
            }

            Date toDate = null;
            if (pToDate!= null) {
                toDate = pToDate.getTime();
            }

            for (SAPPayrollRun sapPayrollRun : payrollRuns) {
                if (fromDate != null) {
                    if (sapPayrollRun.getPaycheckDate().before(fromDate)) {
                        continue;
                    }
                }
                if (toDate != null) {
                    if (sapPayrollRun.getPaycheckDate().after(toDate)) {
                        continue;
                    }
                }
                CompanyPayrollDISDTO companyPayrollDISDTO = new CompanyPayrollDISDTO(sapPayrollRun);
                companyPayrollDISDTOs.add(companyPayrollDISDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return companyPayrollDISDTOs;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }


//    // Copied from PayrollRunAdapter.  I want to search by paycheck date and not payroll run date.
//    private DomainEntitySet<PayrollRun> findPayrollRuns(Company pCompany, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
//        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany);
//
//        if (pFromDate != null) {
//            where = where.And(PayrollRun.PaycheckDate().greaterOrEqualThan(pFromDate));
//        }
//
//        if (pToDate != null) {
//            where = where.And(PayrollRun.PaycheckDate().lessOrEqualThan(pToDate));
//        }
//
//        Expression<PayrollRun> query =
//                new Query<PayrollRun>()
//                       .Where(where)
//                       .OrderBy(PayrollRun.PayrollRunDate())
//                       .EagerLoad(PayrollRun.FinancialTransactionSet());
//
//        return Application.find(PayrollRun.class, query);
//    }

}
