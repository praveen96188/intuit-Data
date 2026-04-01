package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.*;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryUpdatedCompaniesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryUpdatedCompaniesResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 * <p/>
 * Query company event Process
 */
public class PSPQueryUpdatedCompanies extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryUpdatedCompanies.class);
    }

    public static final int MAX_RESULT_CNT = 1000;

    private QueryUpdatedCompaniesRequestDISDTO queryUpdatedCompaniesRequestDISDTO;
    private QueryUpdatedCompaniesResponseDISDTO queryUpdatedCompaniesResponseDISDTO;

    /**
     * Constructor
     *
     * @param pQueryUpdatedCompaniesRequestDISDTO
     *
     */
    public PSPQueryUpdatedCompanies(QueryUpdatedCompaniesRequestDISDTO pQueryUpdatedCompaniesRequestDISDTO) {
        queryUpdatedCompaniesRequestDISDTO = pQueryUpdatedCompaniesRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryUpdatedCompanies.process()");
        queryUpdatedCompaniesResponseDISDTO = new QueryUpdatedCompaniesResponseDISDTO();

        Calendar startDate = queryUpdatedCompaniesRequestDISDTO.getStartDate();
        Calendar endDate = queryUpdatedCompaniesRequestDISDTO.getEndDate();
        ServiceCode serviceCode = queryUpdatedCompaniesRequestDISDTO.getServiceCode();
        List<CompanyServiceUpdatedDISDTO> companies = doWork(startDate, endDate, serviceCode);
        queryUpdatedCompaniesResponseDISDTO.setCompanies(companies);
        logger.debug("Leaving PSPQueryUpdatedCompanies.process()");
        return queryUpdatedCompaniesResponseDISDTO;
    }

    private List<CompanyServiceUpdatedDISDTO> doWork(Calendar pStartDate, Calendar pEndDate,ServiceCode pServiceCode) throws Throwable {
        List<CompanyServiceUpdatedDISDTO> companyServices = new ArrayList<CompanyServiceUpdatedDISDTO>();
        PayrollServices.beginUnitOfWork();
        try {
            DomainEntitySet<CompanyService> pspCompanyServices = queryPSPForNewCompanies(pStartDate, pEndDate, pServiceCode);
            for (CompanyService companyService : pspCompanyServices) {
                CompanyServiceUpdatedDISDTO companyServiceUpdatedDISDTO = new CompanyServiceUpdatedDISDTO();
                companyServiceUpdatedDISDTO.setEin(companyService.getCompany().getFedTaxId());
                companyServiceUpdatedDISDTO.setPsid(companyService.getCompany().getSourceCompanyId());
                SpcfCalendar createdDateSpcfCal = companyService.getCreatedDate();
                Calendar createdDateCal = CalendarUtils.convertToCalendar(createdDateSpcfCal);
                companyServiceUpdatedDISDTO.setDateCreated(createdDateCal);
                companyServices.add(companyServiceUpdatedDISDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return companyServices;
    }

    private DomainEntitySet<CompanyService> queryPSPForNewCompanies(Calendar pStartDate, Calendar pEndDate, ServiceCode pServiceCode) {
        SpcfCalendar startDateSpcfCal = CalendarUtils.convertToSpcfCalendar(pStartDate);

        DomainEntitySet<CompanyService> companyServices = null;

        Criterion<CompanyService> companyserviceCriterion = CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                .And(CompanyService.CreatedDate().greaterOrEqualThan(startDateSpcfCal));
        if(!AuthUser.hasSAPAdminAccess()){
            companyserviceCriterion = companyserviceCriterion.And(CompanyService.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        if (pEndDate != null) {
            SpcfCalendar endDateSpcfCal = CalendarUtils.convertToSpcfCalendar(pEndDate);
            
            companyserviceCriterion = companyserviceCriterion
                    .And(CompanyService.CreatedDate().lessOrEqualThan(endDateSpcfCal));

            companyServices = Application.find(
                    CompanyService.class,
                    new Query<CompanyService>().Where(companyserviceCriterion)
                    .OrderBy(CompanyService.CreatedDate()).LimitResults(0,MAX_RESULT_CNT));
        } else {
            companyServices = Application.find(
                    CompanyService.class,
                    new Query<CompanyService>().Where(companyserviceCriterion
                    ).OrderBy(CompanyService.CreatedDate()).LimitResults(0,MAX_RESULT_CNT));
        }
        return companyServices;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryUpdatedCompaniesResponseDISDTO;
    }

}
