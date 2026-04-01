package com.intuit.ems.payroll.psp.jss.monitoring;

import com.intuit.ems.payroll.psp.jss.monitoring.model.BatchJob;
import com.intuit.sbd.payroll.psp.batchjobs.util.FluxUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author kmuthurangam
 *
 */
public class FluxManager {

	public static String MONITOR_WORK_DIR_COMMAND = "monitor-work-dir";
	public static final String FLUX_FILE_NAME = "flux-scheduled-jobs.json";

	private String monitorWorkDir;

	public FluxManager() {
		validate();
	}

	private void validate() {
		setMonitorWorkDir(System.getProperty(MONITOR_WORK_DIR_COMMAND));
		if (getMonitorWorkDir() == null) {
			BatchUtil.print("ERROR: System property " + MONITOR_WORK_DIR_COMMAND + " not found");
			throw new RuntimeException("System property " + MONITOR_WORK_DIR_COMMAND + " not found");
		}
	}

	public String getMonitorWorkDir() {
		return monitorWorkDir;
	}

	public void setMonitorWorkDir(String monitorWorkDir) {
		this.monitorWorkDir = monitorWorkDir;
	}

	public Set<BatchJob> findAllJobSchedules() {
		Set<BatchJob> jobList = new HashSet<BatchJob>();
		try {
			Set<String> scheduledJobs = FluxUtils.findAllScheduledJobs();
			for (String scheduledJob : scheduledJobs) {
				String scheduledJobName = StringUtils.substringAfterLast(scheduledJob, "/");
				jobList.add(new BatchJob(scheduledJobName));
			}
		} catch (RuntimeException exception) {
			BatchUtil.print("WARN: Error connecting to Flux clusters");
			return jobList;
		}
		return jobList;
	}

	private void writeToFile(File file, Set<BatchJob> jobList) {
		BatchUtil.writeJson(file, jobList);
	}

	private void writeToFile(Set<BatchJob> jobList) throws IOException {
		File file = new File(getMonitorWorkDir(), FLUX_FILE_NAME);
		writeToFile(file, jobList);
	}

	public static void main(String[] args) throws IOException {
		String tomcatStatusFile = System.getProperty(BatchUtil.TOMCAT_STATUS_FILE);
		if (tomcatStatusFile != null && !BatchUtil.canRun(tomcatStatusFile)) {
			BatchUtil.print("INFO: Tomcat service is not active. Skipping the job");
			System.exit(0);
		}

		FluxManager helper = new FluxManager();
		Set<BatchJob> jobList = helper.findAllJobSchedules();
		if (jobList.isEmpty()) {
			BatchUtil.print("INFO: No batch jobs are scheduled in Flux");
		} else {
			BatchUtil.print("INFO: Following list of batch jobs are scheduled in Flux - %s",
					StringUtils.join(jobList, ","));
		}
		helper.writeToFile(jobList);
		BatchUtil.print("INFO: Completed FluxManager successfully");
	}
}
