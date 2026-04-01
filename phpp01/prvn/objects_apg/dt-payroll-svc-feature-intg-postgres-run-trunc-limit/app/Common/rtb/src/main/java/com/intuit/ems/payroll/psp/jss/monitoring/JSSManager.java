package com.intuit.ems.payroll.psp.jss.monitoring;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.intuit.ems.payroll.psp.jss.monitoring.model.BatchJob;
import com.intuit.jss.client.model.Job;
import com.intuit.jss.client.model.Jobs;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbg.shared.batchjob.BatchJobConfig;
import com.intuit.sbg.shared.batchjob.BatchJobConfigFactory;
import com.intuit.sbg.shared.batchjob.jss.client.JSSClientWrapper;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author kmuthurangam
 *
 */
public class JSSManager {

	public static String BATCH_JOB_DEPENDENCY_CONFIG = "batchjob-dependency-config.json";
	public static String MONITOR_WORK_DIR_COMMAND = "monitor-work-dir";

	private SpcfLogger logger = Application.getLogger(JSSManager.class);

	private String monitorWorkDir;

	private Set<BatchJob> jssScheduledBatchJobs;
	private Set<BatchJob> fluxScheduledBatchJobs;
	private MutableGraph<BatchJob> batchJobDependencyGraph;
	private List<BatchJobSetup> batchJobSetupList;

	private LogMonitor logMonitor;

	private static BatchJobConfig batchJobConfig;
	static {

		try {
			batchJobConfig = BatchJobConfigFactory.createInstance();

		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}

	}

	public JSSManager() {
		validate();
		init();
	}

	private void validate() {
		setMonitorWorkDir(System.getProperty(MONITOR_WORK_DIR_COMMAND));
		if (getMonitorWorkDir() == null) {
			BatchUtil.print("ERROR: System property " + MONITOR_WORK_DIR_COMMAND + " not found");
			throw new RuntimeException("System property " + MONITOR_WORK_DIR_COMMAND + " not found");
		}
	}

	private void init() {
		setFluxScheduledBatchJobs(findAllFluxJobSchedules());
		BatchUtil.print("INFO: Successfully loaded Flux scheduled batch jobs");

		setJssScheduledBatchJobs(findAllJSSJobSchedules());

		if (getJssScheduledBatchJobs().isEmpty()) {
			BatchUtil.print("INFO: No batch jobs are scheduled in JSS");
		} else {
			BatchUtil.print("INFO: Following batch jobs are scheduled in JSS - %s",
					formatList(getJssScheduledBatchJobs()));
		}

		if (getFluxScheduledBatchJobs().isEmpty()) {
			BatchUtil.print("INFO: No batch jobs are scheduled in Flux");
		} else {
			BatchUtil.print("INFO: Following batch jobs are scheduled in Flux - %s",
					formatList(getFluxScheduledBatchJobs()));
		}

		setBatchJobDependencyGraph(buildDependencyGraph(BATCH_JOB_DEPENDENCY_CONFIG));
		// BatchUtil.print("INFO: Batch Job dependency graph - %s", getBatchJobDependencyGraph());

		setBatchJobSetupList();
		BatchUtil.print("INFO: Successfully loaded Batch Jobs Setup");

		logMonitor = new LogMonitor();
	}

	public String getMonitorWorkDir() {
		return monitorWorkDir;
	}

	public void setMonitorWorkDir(String monitorWorkDir) {
		this.monitorWorkDir = monitorWorkDir;
	}

	public Set<BatchJob> getJssScheduledBatchJobs() {
		return jssScheduledBatchJobs;
	}

	public void setJssScheduledBatchJobs(Set<BatchJob> jssScheduledBatchJobs) {
		this.jssScheduledBatchJobs = jssScheduledBatchJobs;
	}

	public Set<BatchJob> getFluxScheduledBatchJobs() {
		return fluxScheduledBatchJobs;
	}

	public void setFluxScheduledBatchJobs(Set<BatchJob> fluxScheduledBatchJobs) {
		this.fluxScheduledBatchJobs = fluxScheduledBatchJobs;
	}

	public MutableGraph<BatchJob> getBatchJobDependencyGraph() {
		return batchJobDependencyGraph;
	}

	public void setBatchJobDependencyGraph(MutableGraph<BatchJob> batchJobDependencyGraph) {
		this.batchJobDependencyGraph = batchJobDependencyGraph;
	}

	public List<BatchJobSetup> getBatchJobSetupList() {
		return batchJobSetupList;
	}

	public void setBatchJobSetupList() {
		DomainEntitySet<BatchJobSetup> batchJobSetups = loadBatchJobSetup();
		Set<BatchJobSetup> batchJobSet = batchJobSetups.toNative();
		this.batchJobSetupList = new ArrayList<BatchJobSetup>(batchJobSet);
	}

	public void monitor() {
		Set<BatchJob> duplicateBatchJobs = findDuplicateScheduledBatchJobs();
		if (duplicateBatchJobs.isEmpty()) {
			BatchUtil.print("INFO: No duplicate batch jobs scheduled in both Flux and JSS ");
		} else {
			BatchUtil.print("INFO: Following are the duplicate batch jobs scheduled in both Flux and JSS - %s",
					formatList(duplicateBatchJobs));
		}

		ListMultimap<String, String> fluxMissedToScheduleDependentBatchJobs = findMissedToScheduleDependentBatchJobs(
				ScheduleMode.FLUX);
		printListMultimap(fluxMissedToScheduleDependentBatchJobs, ScheduleMode.FLUX);

		ListMultimap<String, String> jssMissedToScheduleDependentBatchJobs = findMissedToScheduleDependentBatchJobs(
				ScheduleMode.JSS);
		printListMultimap(jssMissedToScheduleDependentBatchJobs, ScheduleMode.JSS);

		Set<BatchJob> missedToScheduleBatchJobs = findMissedToScheduleBatchJobs();
		if (missedToScheduleBatchJobs.isEmpty()) {
			BatchUtil.print("INFO: All the automatic batch jobs are scheduled either in Flux or JSS ");
		} else {
			BatchUtil.print("INFO: Following automatic batch jobs are missed to be schedule either in Flux or JSS - %s",
					missedToScheduleBatchJobs);
		}

		logMonitor.monitorLog();
	}

	public Set<BatchJob> findAllFluxJobSchedules() {
		Set<BatchJob> scheduledBatchJobs = new HashSet<BatchJob>();
		try {
			scheduledBatchJobs = BatchUtil.readJson(new File(getMonitorWorkDir(), "flux-scheduled-jobs.json"));
		} catch (IOException e) {
			BatchUtil.print("ERROR: Unable to find the jobs scheduled in Flux");
			throw new RuntimeException("Unable to find the jobs scheduled in Flux", e);
		}

		return scheduledBatchJobs;
	}

	public Set<BatchJob> findAllJSSJobSchedules() {
		Set<BatchJob> scheduledBatchJobs = new HashSet<BatchJob>();

		Jobs scheduledJobs = JSSClientWrapper.findAllJobSchedulesInGroup(batchJobConfig.getGroupName());
		if (scheduledJobs == null || scheduledJobs.getJobs() == null || scheduledJobs.getJobs().isEmpty()) {
			return scheduledBatchJobs;
		}

		for (Job job : scheduledJobs.getJobs()) {
			scheduledBatchJobs.add(new BatchJob(job.getName()));
		}

		return scheduledBatchJobs;
	}

	public MutableGraph<BatchJob> buildDependencyGraph(String configFile) {
		Set<BatchJob> batchJobdependencyDefSet = null;
		try {
			batchJobdependencyDefSet = BatchUtil.readJson(new File(getMonitorWorkDir(), configFile));
		} catch (IOException e) {
			BatchUtil.print("ERROR: Unable to read the dependent batch job from config file");
			throw new RuntimeException("Unable to read the dependent batch job from config file", e);
		}
		MutableGraph<BatchJob> dependencyGraph = GraphBuilder.undirected().build();
		for (BatchJob batchJob : batchJobdependencyDefSet) {
			dependencyGraph.addNode(batchJob);
			for (BatchJob dependentBatchJob : batchJob.getDependsOn()) {
				dependencyGraph.putEdge(batchJob, dependentBatchJob);
			}
		}
		return dependencyGraph;
	}

	public Set<BatchJob> findDuplicateScheduledBatchJobs() {
		Set<BatchJob> duplicateBatchJobs = new HashSet<BatchJob>();
		if (getJssScheduledBatchJobs().isEmpty()) {
			return duplicateBatchJobs;
		}

		if (getFluxScheduledBatchJobs().isEmpty()) {
			return duplicateBatchJobs;
		}

		duplicateBatchJobs = SetUtils.intersection(getFluxScheduledBatchJobs(), getJssScheduledBatchJobs());
		return duplicateBatchJobs;
	}

	public Set<BatchJob> findMissedToScheduleBatchJobs() {
		Set<BatchJob> missedToScheduledBatchJobs = new HashSet<BatchJob>();
		if (getJssScheduledBatchJobs().isEmpty()) {
			return missedToScheduledBatchJobs;
		}

		if (getFluxScheduledBatchJobs().isEmpty()) {
			return missedToScheduledBatchJobs;
		}

		Set<BatchJob> allScheduledBatchJobs = SetUtils.union(getFluxScheduledBatchJobs(), getJssScheduledBatchJobs());
		for (BatchJobSetup batchJobSetup : getBatchJobSetupList()) {
			if (!batchJobSetup.getIsAutomaticallyScheduled()) {
				continue;
			}

			BatchJob batchJob = new BatchJob(batchJobSetup.getJobType().name());
			if (!allScheduledBatchJobs.contains(batchJob)) {
				missedToScheduledBatchJobs.add(batchJob);
			}
		}
		return missedToScheduledBatchJobs;
	}

	public ListMultimap<String, String> findMissedToScheduleDependentBatchJobs(ScheduleMode scheduleMode) {
		ListMultimap<String, String> missedToScheduleBatchJobMap = ArrayListMultimap.create();
		MutableGraph<BatchJob> dependencyGraph = getBatchJobDependencyGraph();

		Set<BatchJob> scheduledBatchJobs = null;
		switch (scheduleMode) {
		case FLUX:
			scheduledBatchJobs = getFluxScheduledBatchJobs();
			break;

		case JSS:
			scheduledBatchJobs = getJssScheduledBatchJobs();
			break;
		}

		for (BatchJob scheduledBatchJob : scheduledBatchJobs) {
			if (!dependencyGraph.nodes().contains(scheduledBatchJob)) {
				BatchUtil.print("WARN: Graph does not contain the node - %s", scheduledBatchJob);
				continue;
			}
			Set<BatchJob> dependentBatchJobs = dependencyGraph.successors(scheduledBatchJob);
			// info("Successor node(s) for " + scheduledBatchJob.getName() + " is/are " + dependentBatchJobs);
			for (BatchJob dependentBatchJob : dependentBatchJobs) {
				if (!scheduledBatchJobs.contains(dependentBatchJob)) {
					if (isBatchJobAutoScheduled(dependentBatchJob.getName())) {
						missedToScheduleBatchJobMap.put(scheduledBatchJob.getName(), dependentBatchJob.getName());
					}
				}
			}
		}
		return missedToScheduleBatchJobMap;
	}

	public void printListMultimap(ListMultimap<String, String> listMultimap, ScheduleMode scheduleMode) {
		if (listMultimap.isEmpty()) {
			BatchUtil.print("INFO: All the dependent batch jobs are scheduled properly on -%s", scheduleMode.name());
			return;
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("INFO: Following dependent batch jobs are not scheduled on - " + scheduleMode.name())
				.append("\n");
		for (Entry<String, String> missedDependencySet : listMultimap.entries()) {
			buffer.append(missedDependencySet.getKey() + " -> " + missedDependencySet.getValue()).append(",");
		}
		BatchUtil.print(buffer.toString());
	}

	private DomainEntitySet<BatchJobSetup> loadBatchJobSetup() {
		boolean manageTransaction = !Application.hasActiveTransaction();

		DomainEntitySet<BatchJobSetup> batchJobSetups = null;
		try {
			if (manageTransaction) {
				PayrollServices.beginUnitOfWork();
			}

			batchJobSetups = PayrollServices.entityFinder.<BatchJobSetup> findObjects(BatchJobSetup.class);
		} finally {
			if (manageTransaction) {
				PayrollServices.commitUnitOfWork();
			}
		}
		return batchJobSetups;

	}

	private boolean isBatchJobAutoScheduled(String batchJobName) {
		BatchJobSetup batchJobSetup = getBatchJobSetup(batchJobName);
		if (batchJobSetup == null) {
			return false;
		}
		return batchJobSetup.getIsAutomaticallyScheduled();
	}

	private BatchJobSetup getBatchJobSetup(String batchJobName) {
		BatchJobSetup batchJobSetup = null;
		for (BatchJobSetup batchJob : getBatchJobSetupList()) {
			if (batchJob.getJobType() == BatchJobType.valueOf(batchJobName)) {
				return batchJob;
			}
		}
		return batchJobSetup;
	}

	public static String formatList(Set<?> set) {
		return StringUtils.join(set, ",");
	}

	public static void main(String[] args) throws IOException {
		String tomcatStatusFile = System.getProperty(BatchUtil.TOMCAT_STATUS_FILE);
		if (tomcatStatusFile != null && !BatchUtil.canRun(tomcatStatusFile)) {
			BatchUtil.print("INFO: Tomcat service is not active. Skipping the job");
			System.exit(0);
		}

		BatchUtil.print("INFO: Starting alert monitor ");

		JSSManager helper = new JSSManager();
		helper.monitor();

		BatchUtil.print("INFO: Completed alert monitor successfully");
	}

}
