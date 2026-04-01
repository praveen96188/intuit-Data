package com.intuit.ems.payroll.psp.jss.monitoring;

import com.intuit.ems.payroll.psp.jss.monitoring.model.BatchJob;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.util.CustomCronExpression;
import com.intuit.sbd.payroll.psp.jss.util.QuartzUtils;
import com.intuit.sbd.payroll.psp.jss.util.TimeExpressionConverter;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author kmuthurangam
 *
 */
public class LogMonitor {

	public static String MONITOR_WORK_DIR_COMMAND = "monitor-work-dir";

	public static String LOG_SEARCH_CONFIG_FILE = "batchjob-log-search-config.json";

	public File logConfigFile;

	private List<BatchJobSetup> batchJobSetupList;

	private LogSearchService logSearchService;

	public LogMonitor() {
		BatchUtil.print("INFO: Initializing LogMonitor started");
		init();
	}

	private void init() {
		logConfigFile = new File(System.getProperty(MONITOR_WORK_DIR_COMMAND), LOG_SEARCH_CONFIG_FILE);
		setBatchJobSetupList();
		BatchUtil.print("INFO: Batch Job Setup Succesfully loaded " + getBatchJobSetupList());
		generateLogSearchConfig();
		logSearchService = new SplunkSearchService();
		BatchUtil.print("INFO: Initializing LogMonitor completed");
	}

	public List<BatchJobSetup> getBatchJobSetupList() {
		return batchJobSetupList;
	}

	public void setBatchJobSetupList() {
		DomainEntitySet<BatchJobSetup> batchJobSetups = loadBatchJobSetup();
		Set<BatchJobSetup> batchJobSet = batchJobSetups.toNative();
		this.batchJobSetupList = new ArrayList<BatchJobSetup>(batchJobSet);
	}

	public void monitorLog() {
		List<BatchJob> batchJobs = null;
		try {
			batchJobs = new ArrayList<BatchJob>(BatchUtil.readJson(logConfigFile));
		} catch (IOException e) {
			batchJobs = new ArrayList<BatchJob>();
			e.printStackTrace();
		}
		Date now = new Date();
		for (BatchJobSetup batchJobSetup : getBatchJobSetupList()) {
			if (!batchJobSetup.getIsAutomaticallyScheduled()) {
				BatchUtil.print("INFO: Skipping the log monitor for not automatically scheduled batch job %s",
						batchJobSetup.getJobType().name());
				continue;
			}

			int index = batchJobs.indexOf(new BatchJob(batchJobSetup.getJobType().name()));
			if (index == -1) {
				BatchUtil.print("ERROR: %s", "Failed to load batch job next schedule configuration");
				continue;
			}
			BatchJob batchJob = batchJobs.get(index);
			if (batchJob.getNextSplunkSearchTime().after(now)) {
				BatchUtil.print("INFO: Next log search for batch job %s will happen only after %s", batchJob.getName(),
						batchJob.getNextSplunkSearchTime().toString());
				continue;
			}

			Date startTime = getPreviousSchedule(batchJobSetup.getJobTimerExpression());
			Date endTime = addMinutes(startTime, 1);

			String query = String.format("search index=\"psp\" %s - Starting * batch job",
					StringUtils.substringAfterLast(batchJobSetup.getJobProcessorClassName(), "."));
			int eventCount = logSearchService.execute(query, startTime, endTime);
			BatchUtil.print("INFO: %s", parseResults(batchJobSetup, eventCount, startTime, endTime));

			batchJob.setNextSplunkSearchTime(getNextSchedule(batchJobSetup.getJobTimerExpression()));
		}
		BatchUtil.writeJson(logConfigFile, batchJobs);
	}

	private String parseResults(BatchJobSetup batchJobSetup, int eventCount, Date startTime, Date endTime) {
		StringBuffer messages = new StringBuffer();
		messages.append(batchJobSetup.getJobType().name()).append(" ");
		switch (eventCount) {
		case 0:
			BatchJobAuditLog batchJobAuditLog = findLastMessage(batchJobSetup.getJobType(), startTime, endTime);
			if ((batchJobAuditLog == null) || (!StringUtils.equals(batchJobAuditLog.getMessage(), "Started"))) {
				messages.append("missed to run between ");
				messages.append(startTime).append(" and ").append(endTime);
			} else {
				messages.append(" started at ").append(batchJobAuditLog.getCreatedDate()).append(" with ")
						.append(batchJobAuditLog.getJobNamespace()).append(" is still running or failed");
			}
			break;
		case 1:
			messages.append("ran succesfully once between ");
			messages.append(startTime).append(" and ").append(endTime);
			break;
		default:
			messages.append("ran ").append(eventCount).append(" times between ");
			messages.append(startTime).append(" and ").append(endTime);
			break;
		}
		return messages.toString();
	}

	private void generateLogSearchConfig() {
		if (!logConfigFile.exists()) {
			List<BatchJob> batchJobs = new ArrayList<BatchJob>();
			for (BatchJobSetup batchJobSetup : getBatchJobSetupList()) {
				BatchJob batchJob = new BatchJob(batchJobSetup.getJobType().name());
				batchJob.setNextSplunkSearchTime(getNextSchedule(batchJobSetup.getJobTimerExpression()));
				batchJobs.add(batchJob);
			}
			BatchUtil.print("INFO: Log search configurationo created " + batchJobs);
			BatchUtil.writeJson(logConfigFile, batchJobs);
		}
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

	private Date getPreviousSchedule(String fluxExpression) {
		return getPreviousSchedule(fluxExpression, new Date());
	}

	private Date getPreviousSchedule(String fluxExpression, Date date) {
		String quartzSchedule = TimeExpressionConverter.convertFluxToQuartz(fluxExpression);
		CronExpression cronExpression = null;
		try {
			cronExpression = new CustomCronExpression(quartzSchedule);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cronExpression.getTimeBefore(date);
	}

	private Date getNextSchedule(String fluxExpression) {
		return getNextSchedule(fluxExpression, new Date());
	}

	private Date getNextSchedule(String fluxExpression, Date date) {
		String quartzSchedule = TimeExpressionConverter.convertFluxToQuartz(fluxExpression);
		return QuartzUtils.getNextValidTimeAfter(date, quartzSchedule);
	}

	private Date addMinutes(Date date, int minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}

	private static BatchJobAuditLog findLastMessage(BatchJobType pJobType, Date startTime, Date endTime) {
		boolean manageTransaction = !Application.hasActiveTransaction();
		try {
			if (manageTransaction) {
				PayrollServices.beginUnitOfWork();
			}

			Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like("%" + pJobType.name() + "%");

			return Application
					.find(BatchJobAuditLog.class,
							new Query<BatchJobAuditLog>().Where(where)
									.OrderBy(BatchJobAuditLog.CreatedDate().Descending()).LimitResults(0, 1))
					.getFirst();
		} finally {
			if (manageTransaction) {
				PayrollServices.rollbackUnitOfWork();
			}
		}

	}

	public static void main(String[] args) {
		LogMonitor logMonitor = new LogMonitor();
		logMonitor.monitorLog();
	}

}
