package com.intuit.sbd.payroll.psp.batchjobs.entity.company;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.entity.EntityFindStrategy;
import com.intuit.sbd.payroll.psp.batchjobs.entity.NamedQueryEnum;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompanyFindStrategy<T> implements EntityFindStrategy<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyFindStrategy.class);
    private int batchSize;
    private PublishStatusWorkflowState publishStatusWorkflowState;
    private CompanyPublishStatusWorkflows companyPublishStatusWorkflows;
    private List<String> sourceCompanyIds;
    private NamedQueryEnum namedQueryEnum;

    public CompanyFindStrategy(int batchSize, CompanyPublishStatusWorkflows companyPublishStatusWorkflows, PublishStatusWorkflowState publishStatusWorkflowState, List<String> sourceCompanyIds, NamedQueryEnum namedQueryEnum) {
        this.batchSize = batchSize;
        this.publishStatusWorkflowState = publishStatusWorkflowState;
        this.companyPublishStatusWorkflows = companyPublishStatusWorkflows;
        this.sourceCompanyIds = sourceCompanyIds;
        this.namedQueryEnum = namedQueryEnum;
    }

    public List<T> getFixedCompanyList() {
        LOGGER.info("job=initial_load,action=get_fixed_company_list_started");
        DomainEntitySet<Company> companies = Company.findCompaniesBySourceCompanyIds(SourceSystemCode.QBDT, sourceCompanyIds);

        List<T> data = new ArrayList<>();
        for (Company company : companies) {
            data.add((T) new Object[]{company.getSourceCompanyId(), company.getId()});
            LOGGER.info("job=initial_load,action=get_fixed_company_list_iterating,companyId=", company.getId() + ", SourceCompanyId=" + company.getSourceCompanyId());
        }
        return data;
    }

    public List<T> getQueriedCompanyList() {
        LOGGER.info("job=initial_load,action=get_queried_company_list_started");
        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "fieldIndex";
        paramNames[1] = "companyPublishState";

        // Index- 0 for EMS, 1 for EVS, 3 for SANCTIONS_EMPLOYER, 4 for SANCTIONS_EMPLOYEE, 5 for SANCTIONS_CONTRACTOR
        paramValues[0] = companyPublishStatusWorkflows.getValue();

        //Compare Index
        paramValues[1] = String.valueOf(publishStatusWorkflowState.getValue());

        if(namedQueryEnum == NamedQueryEnum.FIND_INACTIVE_COMPANIES_WITH_REALM){
           return Application.executeNamedQuery(Application.getQueryName(namedQueryEnum.value()), paramNames, paramValues, 0, batchSize);
        }

        return Application.executeNamedQuery(namedQueryEnum.value(), paramNames, paramValues, 0, batchSize);
    }

    @Override
    public List<T> getCompanyList() {
        LOGGER.info("job=initial_load,action=get_company_list_started");
        List<T> entityList;
        if (Objects.nonNull(sourceCompanyIds) && sourceCompanyIds.size() > 0) {
            entityList = getFixedCompanyList();
        } else {
            entityList = getQueriedCompanyList();
        }
        LOGGER.info("job=initial_load,action=get_company_list_completed,workflow={},entity_list_size={}", companyPublishStatusWorkflows.name(), entityList.size());
        return entityList;
    }
}
