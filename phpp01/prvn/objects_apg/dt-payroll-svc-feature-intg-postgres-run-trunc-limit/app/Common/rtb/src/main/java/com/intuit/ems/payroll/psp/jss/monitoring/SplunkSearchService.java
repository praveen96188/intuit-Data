package com.intuit.ems.payroll.psp.jss.monitoring;

import com.intuit.sbd.payroll.psp.Application;
import com.splunk.HttpService;
import com.splunk.Job;
import com.splunk.JobArgs;
import com.splunk.ResultsReaderXml;
import com.splunk.SSLSecurityProtocol;
import com.splunk.Service;
import com.splunk.ServiceArgs;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author kmuthurangam
 *
 */
public class SplunkSearchService implements LogSearchService {

	public static Properties splunkConfigProperties;

	static {
		splunkConfigProperties = new Properties();
		try {

			String splunkProperties = Application.isProdEnvironment() ? "splunk-prod.properties"
					: "splunk-pre-prod.properties";
			splunkConfigProperties
					.load(new FileInputStream(new File(System.getProperty("monitor-work-dir"), splunkProperties)));
		} catch (IOException e) {
			BatchUtil.print("ERROR: Error loading the Splunk configuration property file");
		}
	}

	private Service service;

	public SplunkSearchService() {
		connectToSplunk(splunkConfigProperties);
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public void connectToSplunk(Properties properties) {
		HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);

		ServiceArgs loginArgs = new ServiceArgs();

		loginArgs.setUsername(properties.getProperty("username"));
		loginArgs.setPassword(properties.getProperty("password"));
		loginArgs.setHost(properties.getProperty("host"));
		loginArgs.setPort(Integer.parseInt(properties.getProperty("port")));
		loginArgs.setScheme(properties.getProperty("scheme"));
		loginArgs.setApp(properties.getProperty("app"));

		Service service = Service.connect(loginArgs);
		BatchUtil.print("INFO: Succesfully connected to the Splunk service");
		setService(service);
	}

	@Override
	public int execute(String query, Date startTime, Date endTime) {

		String startTimeString = getTimeString(startTime);
		String endTimeString = getTimeString(endTime);

		BatchUtil.print("INFO: %s",
				String.format("Splunk Query - %s earliest=%s latest=%s ", query, startTimeString, endTimeString));

		Job job = executeQuery(query, startTimeString, endTimeString);
		int eventCount = parseResults(job);
		return eventCount;
	}

	public Job executeQuery(String query, String startTime, String endTime) {

		JobArgs jobargs = new JobArgs();
		jobargs.setEarliestTime(startTime);
		jobargs.setLatestTime(endTime);
		jobargs.setExecutionMode(JobArgs.ExecutionMode.NORMAL);
		Job job = getService().getJobs().create(query, jobargs);
		while (!job.isDone()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				BatchUtil.print("ERROR: Thread interrupted while waiting for the Splunk search job to be completed");
			}
		}
		return job;
	}

	private int parseResults(Job job) {
		Map<String, String> events = new HashMap<String, String>();

		InputStream resultsNormalSearch = job.getResults();

		ResultsReaderXml resultsReaderNormalSearch;

		try {
			resultsReaderNormalSearch = new ResultsReaderXml(resultsNormalSearch);
			HashMap<String, String> event;
			while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
				String eventLog = event.get("_raw");
				String threadName = StringUtils.substringBetween(eventLog, "INFO  [", "]");
				if (!events.containsKey(threadName)) {
					events.put(threadName, eventLog);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return events.size();
	}

	private String getTimeString(Date date) {
		if (date == null) {
			return null;
		}
		return getTimeString(date.getTime());
	}

	private String getTimeString(Long milliSeconds) {
		if (milliSeconds == null) {
			return null;
		}
		return Long.toString(milliSeconds).substring(0, 10);
	}

}
