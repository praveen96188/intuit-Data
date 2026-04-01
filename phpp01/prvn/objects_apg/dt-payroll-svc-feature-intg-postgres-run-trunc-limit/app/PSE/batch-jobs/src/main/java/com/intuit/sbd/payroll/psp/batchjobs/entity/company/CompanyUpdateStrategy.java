package com.intuit.sbd.payroll.psp.batchjobs.entity.company;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.entity.UpdateStatusStrategy;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CompanyUpdateStrategy<T> implements UpdateStatusStrategy<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyUpdateStrategy.class);
    private CompanyPublishStatusWorkflows companyPublishStatusWorkflows;

    public CompanyUpdateStrategy(CompanyPublishStatusWorkflows companyPublishStatusWorkflows) {
        this.companyPublishStatusWorkflows = companyPublishStatusWorkflows;
    }

    public void updateCompanyStatus(List<T> chunk, PublishStatusWorkflowState publishStatusWorkflowState) {
        LOGGER.info("job=initial_load,action=update_company_status_started");

        ArrayList<SpcfUniqueId> companySeqs = new ArrayList<>();
        chunk.stream().forEach(entry -> companySeqs.add(SpcfUniqueId.createInstance(((Object[])entry)[1].toString())));
        DomainEntitySet<Company> entitySet = Company.findCompaniesByCompanyIdList(companySeqs);
        for (Company company : entitySet) {
            company.setPublishStatusWorkflowState(companyPublishStatusWorkflows, publishStatusWorkflowState);
            Application.save(company);
        }
        LOGGER.info("job=initial_load,action=update_company_status_completedWorkflowState={},psIds={}", publishStatusWorkflowState.name(),getPsIdsFromChunk(chunk));
    }

    @Override
    public void handlePublishChunkFailure(List<T> chunk) {
        LOGGER.info("job=initial_load,action=handle_publish_chunk_failure,targetedWorkFlow={},chunkSize={},chunk={}", companyPublishStatusWorkflows.name(), chunk.size(), getPsIdsFromChunk(chunk));
        updateCompanyStatus(chunk, PublishStatusWorkflowState.ERROR);
    }

    @Override
    public void handlePublishChunkSuccess(List<T> chunk) {
        LOGGER.info("job=initial_load,action=handle_publish_chunk_success_started,targetedWorkFlow={},chunkSize={},chunk={}", companyPublishStatusWorkflows.name(), chunk.size(), getPsIdsFromChunk(chunk));
        updateCompanyStatus(chunk, PublishStatusWorkflowState.PUBLISHED_INTERNAL);
    }


}