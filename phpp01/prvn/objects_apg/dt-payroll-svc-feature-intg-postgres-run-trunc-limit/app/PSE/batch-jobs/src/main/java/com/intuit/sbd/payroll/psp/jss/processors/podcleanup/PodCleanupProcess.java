package com.intuit.sbd.payroll.psp.jss.processors.podcleanup;

import com.intuit.jss.client.model.JobStatus;
import com.intuit.jss.client.model.JobStatuses;
import com.intuit.sbg.shared.batchjob.BatchJobConfigFactory;
import com.intuit.sbg.shared.batchjob.jss.client.JSSClientWrapper;
import com.intuit.sbg.shared.batchjob.jss.client.enumerations.JobStatusCode;
import com.sun.jersey.api.client.ClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PodCleanupProcess {

    private String group;
    // this list is being picked from batch-jobs.yml
    // the list should be comma separated with no space before or after the comma
    // batchjobs.eligible.for.autocompletion.list: EntityEvent,BulkWorkforceInviteProcessor
    private Set<String> jobsEligibleForAutoCompletion;
    private String offeringId;

    @Autowired
    public PodCleanupProcess(@Value("${batchjobs.eligible.for.autocompletion.list}") String jobsEligibleForAutoCompletionStr,
                             @Value("${security.intuit.appId}") String offeringId) {
        this.jobsEligibleForAutoCompletion = new HashSet<>(Arrays.asList(jobsEligibleForAutoCompletionStr.trim().split(",")));
        this.group = BatchJobConfigFactory.createInstance().getGroupName();
        this.offeringId = offeringId;

        log.info("PodCleanupProcess bean initialized jobsEligibleForAutoCompletion={} group={} offeringId={}",
                this.jobsEligibleForAutoCompletion, this.group, this.offeringId);
    }

    /***
     *
     * @param podName podName
     * This function performs cleanup activities for a pod
     * Currently, it will complete all the running batch jobs on the pod
     * which are eligible for auto-completion (picked from properties)
     */
    public void cleanupPod(String podName) {
        markEligibleRunningJobsAsCompleted(podName);
    }

    /***
     * @param podName podName for which we want to mark eligible jobs as completed
     * @return List<JobStatus> List of jobs successfully marked as completed
     */
    private List<JobStatus> markEligibleRunningJobsAsCompleted(String podName) {
        try {
            List<JobStatus> markAsCompletedSuccessList = new ArrayList<>();
            //1. find jobs running on the pod
            List<JobStatus> jobsRunningOnPod = getRunningJobsOnPod(podName);

            //2. Loop over the jobs and attempt to mark them as completed
            for(JobStatus jobStatus: jobsRunningOnPod) {
                if(jobsEligibleForAutoCompletion.contains(jobStatus.getJobName())) {
                    boolean jobSuccessfullyMarkedAsCompleted = markJobAsCompleted(jobStatus);
                    if(jobSuccessfullyMarkedAsCompleted) {
                        markAsCompletedSuccessList.add(jobStatus);
                    }

                }
            }
            logJobStatusOnPodCleanup(mapJobStatusToJobName(jobsRunningOnPod), mapJobStatusToJobName(markAsCompletedSuccessList), podName);
            return markAsCompletedSuccessList;
        } catch (IllegalArgumentException e) {
            log.error("error retrieving running jobs for the pod={}, errorMessage={}", podName, e.getMessage(), e);
            throw e;
        }
    }

    private void logJobStatusOnPodCleanup(List<String> jobsRunning, List<String> jobsMarkedAsCompleted, String podName) {
        List<String> jobsNotMarkedAsCompleted = new ArrayList<>(jobsRunning);
        jobsNotMarkedAsCompleted.removeAll(jobsMarkedAsCompleted);
        log.info("shutdownPodName={} jobsRunningOnPod={} jobSuccessfullyMarkedAsCompleted={} jobsNotMarkedAsCompleted={}",
                podName, jobsRunning, jobsMarkedAsCompleted, jobsNotMarkedAsCompleted);
    }

    private List<String> mapJobStatusToJobName(List<JobStatus> jobStatusList) {
        if(Objects.isNull(jobStatusList) || jobStatusList.isEmpty()) {
            return Collections.emptyList();
        }
        return jobStatusList.stream().map(JobStatus::getJobName).collect(Collectors.toList());
    }

    /***
     * This function returns all the running jobs on the pod
     * @param podName
     * If authorization is not present , we create an offline ticket
     * Implementation - We get all the active jobs in the current group
     * The shutdownUrl for a job contains the podName on which it is running, based on this we filter the jobs
     * @return
     */
    public List<JobStatus> getRunningJobsOnPod(String podName) {
        if(StringUtils.isEmpty(podName)) {
            throw new IllegalArgumentException("PodName can not be null or empty");
        }
        try {
            JobStatuses response = JSSClientWrapper.findActiveJobsInGroup(group);

            List<JobStatus> jobsRunningOnPod = new ArrayList<>();
            if(Objects.isNull(response)) {
                log.info("received null response from findActiveJobsInGroup, this also happens when unschedule hook is called");
                return jobsRunningOnPod;
            }

            for (JobStatus jobStatus : response.getJobStatuses()) {
                if (Objects.nonNull(jobStatus.getShutdownUrl()) && jobStatus.getShutdownUrl().contains(podName)) {
                    jobsRunningOnPod.add(jobStatus);
                }
            }
            return jobsRunningOnPod;

        } catch (Exception ex) {
            log.error("encountered exception message={}", ex.getMessage(),ex);
            throw ex;
        }
    }

    private boolean markJobAsCompleted(JobStatus jobStatus) {
        boolean markSuccess = false;
        try {
            Future<Pair<ClientResponse, JobStatus>> result = JSSClientWrapper.updateJobStatus(jobStatus.getJobGroup(), jobStatus.getJobName(), jobStatus.getInstanceId(), JobStatusCode.Completed, jobStatus.getDetails());
            jobStatus = getJobStatusFromResult(result);
            if(Objects.nonNull(jobStatus.getCode()) && jobStatus.getCode().equals(JobStatusCode.Completed.toString())) {
                markSuccess = true;
            }
            log.info("action=markJobAsCompleted success={} jobStatus={}", markSuccess, jobStatus);
        } catch (Exception e){
            log.error("action=markJobAsCompleted success=false jobStatus={} errorMessage={}", jobStatus.toString(), e.getMessage(),e);
        }
        return markSuccess;
    }

    private static JobStatus getJobStatusFromResult(Future<Pair<ClientResponse, JobStatus>> result) throws InterruptedException, ExecutionException {
        if (Objects.isNull(result) || Objects.isNull(result.get()) || Objects.isNull(result.get().getValue())){
            return null;
        }
        return result.get().getValue();
    }


}
