package com.intuit.sbd.payroll.psp.batchjobs.entity.company;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.entity.EntityFindStrategy;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompanyHourlyFindStrategy<T> implements EntityFindStrategy<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyHourlyFindStrategy.class);

    private SpcfCalendar lastProcessedTime;
    private SpcfCalendar batchStartTime;
    private List<String> sourceCompanyIds;

    public CompanyHourlyFindStrategy(SpcfCalendar lastProcessedTime, SpcfCalendar batchStartTime, List<String> sourceCompanyIds) {
        this.lastProcessedTime = lastProcessedTime;
        this.batchStartTime = batchStartTime;
        this.sourceCompanyIds = sourceCompanyIds;
    }

    public List<T> getFixedCompanyList() {
        LOGGER.info("job=initial_load_evs_hourly,action=get_fixed_company_list_started ");
        DomainEntitySet<Company> companies = Company.findCompaniesBySourceCompanyIds(SourceSystemCode.QBDT, sourceCompanyIds);

        List<T> data = new ArrayList<>();
        for (Company company : companies) {
            data.add((T) new Object[]{company.getSourceCompanyId(), company.getId()});
            LOGGER.info("job=initial_load_evs_hourly,action=get_fixed_company_list_iterating,companyId=", company.getId() + ", SourceCompanyId=" + company.getSourceCompanyId());
        }
        return data;
    }

    public List<T> getQueriedCompanyList() {
        LOGGER.info("job=initial_load_evs_hourly,action=get_queried_company_list_started");
        String[] paramNames = {"lastProcessedTime", "batchStartTime"};
        Object[] paramValues = {lastProcessedTime, batchStartTime};

        return Application.executeNamedQuery("findCompaniesForRealTimePublish", paramNames, paramValues);
    }

    @Override
    public List<T> getCompanyList() {
        LOGGER.info("job=initial_load_evs_hourly,action=get_company_list_started");
        List<T> entityList;

        if (Objects.nonNull(sourceCompanyIds) && sourceCompanyIds.size() > 0) {
            entityList = getFixedCompanyList();
        } else {
            entityList = getQueriedCompanyList();
        }
        LOGGER.info("job=initial_load_evs_hourly,action=get_company_list_started,workflow=EVS_HOURLY,entity_list_size={}", entityList.size());
        return entityList;
    }
}