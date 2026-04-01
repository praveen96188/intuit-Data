package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchReturnsFileDownload;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnBatch;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnBatchStatusCode;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.File;
import java.util.List;
import java.util.Vector;

@ScheduledJob(name = "NightlyBatchJobs", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class NightlyBatchJobsProcessor extends JSSBatchJob {

	public enum Commands {
		all, achret, achtrans, tp401k
	}

	private Commands mCommand = null;
	private SpcfCalendar mProcessingDate;
	private String mReturnsFileName;
	private boolean mValidProcessingDate = false;

	public NightlyBatchJobsProcessor(String[] pArguments) {
		super(pArguments);
	}

	public NightlyBatchJobsProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	private String getUsage() {
		return "Usage: all | achret | achtrans <yyyyMMdd> ";
	}

	protected Commands getCommand() {
		return mCommand;
	}

	protected SpcfCalendar getProcessingDate() {
		return mProcessingDate;
	}

	protected String getReturnsFileName() {
		return mReturnsFileName;
	}

	protected boolean isValidProcessingDate() {
		return mValidProcessingDate;
	}

	protected void validateRuntimeParameters() {
		// getJobInstanceParameters() will never return null
		String commandLine = getJobInstanceParameters().trim();

		// always set processing date to check for weekends/holidays
		try {
			Application.beginUnitOfWork();

			mProcessingDate = PSPDate.getPSPTime();
			mValidProcessingDate = !BatchUtils.isWeekendOrHoliday(mProcessingDate);
		} finally {
			Application.rollbackUnitOfWork();
		}

		if (commandLine.isEmpty()) {
			mCommand = Commands.all;
		} else {
			getLogger().info("Validating NightlyBatchJobsProcessor command line: " + commandLine);

			String[] args = commandLine.split(" ");

			if (args.length == 0) {
				throw new RuntimeException("Wrong number of parameters. " + getUsage());
			}

			try {
				mCommand = Commands.valueOf(args[0]);
			} catch (Exception e) {
				throw new RuntimeException("Invalid command specified. " + getUsage());
			}

			if (mCommand.equals(Commands.achtrans)) {
				if (args.length != 2) {
					throw new RuntimeException("Wrong number of parameters. " + getUsage());
				}

				// next arg must be processing date formatted as yyyyMMdd
				if (!args[1].matches(BatchUtils.VALIDYYYYMMDD)) {
					throw new RuntimeException("Invalid processing date specified. " + getUsage());
				}

				mProcessingDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[1]);
			} else if (mCommand.equals(Commands.achret)) {
				if (args.length == 2) {
					mReturnsFileName = args[1];
				}
			}
		}
	}

	protected void validateStepRuntimeParameters(String stepName) {
		if (DownloadAchReturnsFile.class.getSimpleName().equals(stepName)) {
			// no validation
		} else if (ProcessAchReturnsBatch.class.getSimpleName().equals(stepName)) {
			// no validation
		} else if (PersistReturnsFile.class.getSimpleName().equals(stepName)) {
			// no validation
		} else if (NotifyReturnProcessingResults.class.getSimpleName().equals(stepName)) {
			// no validation
		} else if (DownloadNOCReturnsFile.class.getSimpleName().equals(stepName)) {
			// no validation
		} else if (CreateReturnBatch.class.getSimpleName().equals(stepName)
				|| ProcessAchReturnsFile.class.getSimpleName().equals(stepName)) {
			String[] returnsFile = getArguments();

			if (returnsFile.length == 0) {
				throw new RuntimeException("Invalid returns file specified.");
			}

			mReturnsFileName = returnsFile[0];
		} else if (ProcessAchTransactions.class.getSimpleName().equals(stepName)) {
			String[] arguments = getArguments();
			String processingDate = arguments[0];

			// next arg must be processing date formatted as yyyyMMdd
			if (!processingDate.matches(BatchUtils.VALIDYYYYMMDD)) {
				throw new RuntimeException("Invalid processing date specified. Valid format is yyyyMMdd.");
			}

			mProcessingDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, processingDate);
			mValidProcessingDate = !BatchUtils.isWeekendOrHoliday(mProcessingDate);
		} else {
			StringBuilder err = new StringBuilder();
			err.append("The specified job step \"").append(stepName).append("\" does not exist in batch processor ")
					.append(this.getClass().getSimpleName())
					.append(". The valid steps (with optional arguments) that can be executed are {")
					.append(DownloadAchReturnsFile.class.getSimpleName()).append(", ")
					.append(CreateReturnBatch.class.getSimpleName()).append(" <returns-file>, ")
					.append(PersistReturnsFile.class.getSimpleName()).append(", ")
					.append(ProcessAchReturnsBatch.class.getSimpleName()).append(", ")
					.append(NotifyReturnProcessingResults.class.getSimpleName()).append(", ")
					.append(ProcessAchReturnsFile.class.getSimpleName()).append(" <returns-file>, ")
					.append(ProcessAchTransactions.class.getSimpleName()).append(" <yyyyMMdd>}");

			throw new RuntimeException(err.toString());
		}
	}

	protected void execute() {
		if (!isValidProcessingDate()) {
			getLogger().warn("Nightly batch jobs skipped (processing date is bank holiday: "
					+ getProcessingDate().format(BatchUtils.DATE_FORMAT) + ")");
			return;
		}

		getLogger().info("Starting nightly batch jobs (command= " + getCommand().toString() + ")");
		StopWatch timer = StopWatch.startTimer();

		switch (getCommand()) {
		case achret:
			if (getReturnsFileName() == null && !TransactionReturnBatch.fileAlreadyDownloaded(PSPDate.getPSPTime())) {
				executeStep(DownloadAchReturnsFile.class);
			} else if (getReturnsFileName() != null) {
				executeStep(CreateReturnBatch.class);
			}
			executeStep(PersistReturnsFile.class);
			executeStep(ProcessAchReturnsBatch.class);
			executeStep(NotifyReturnProcessingResults.class);
			executeStep(DownloadNOCReturnsFile.class);
			break;

		case achtrans:
			executeStep(ProcessAchTransactions.class);
			break;

		default: // all
			if (getReturnsFileName() == null && !TransactionReturnBatch.fileAlreadyDownloaded(PSPDate.getPSPTime())) {
				executeStep(DownloadAchReturnsFile.class);
			} else if (getReturnsFileName() != null) {
				executeStep(CreateReturnBatch.class);
			}
			executeStep(PersistReturnsFile.class);
			executeStep(ProcessAchReturnsBatch.class);
			executeStep(NotifyReturnProcessingResults.class);
			executeStep(DownloadNOCReturnsFile.class);
			executeStep(ProcessAchTransactions.class);
			break;
		}

		getLogger().info("Completed nightly batch jobs. Elapsed time: " + timer.stop().getElapsedTimeString());
	}

	abstract class AchProcessorStep extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		private StopWatch sw;

		protected AchProcessorStep() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);
		}

		@Override
		protected void logStepStarted() {
			super.logStepStarted();
			sw = StopWatch.startTimer();
		}

		@Override
		protected void logStepFinished() {
			super.logStepFinished();
			getLogger().info(getClass().getSimpleName() + " step finished in " + sw.getElapsedTimeString());
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Job Steps
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class DownloadAchReturnsFile extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);
			new SftpAchReturnsFileDownload().download();
			confirmDownload();
		}

		private void confirmDownload() {
			try {
				PayrollServices.beginUnitOfWork();

				DomainEntitySet<TransactionReturnBatch> batchList = TransactionReturnBatch
						.getTransactionReturnBatchByStatus(TransactionReturnBatchStatusCode.Received);

				if (batchList.isEmpty()) {
					throw new RuntimeException("Failed to download ACH Returns file from bank.");
				}
			} finally {
				PayrollServices.rollbackUnitOfWork();
			}
		}
	}

	public static class DownloadNOCReturnsFile extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);
			// new SftpNocReturnsFileDownload().download();
			try{
				BatchUtils.downloadNocReturnAccountingFileAndEmail();
			}catch (Throwable t){
				throw new RuntimeException("Exception in job step DownloadNOCReturnsFile ", t);
			}
			// confirmDownload();
		}

	}

	/**
	 * this is not part of the standard workflow
	 *
	 * this step is used to manually load an ACH file into the TransactionReturnBatch table; generally, this is used when
	 * manual intervention was required and a new file created or for testing (i.e. the file is not downloaded form the bank)
	 *
	 * TO MANUALLY LOAD A FILE IN PRODUCTION: ./engBatchJobManager.sh run DailyAchProcessor achret <file-name>
	 */
	public static class CreateReturnBatch extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);

			File file = new File(batchJobProcessor.getReturnsFileName());
			try {
				PayrollServices.beginUnitOfWork();

				TransactionReturnBatch.createBatch(file);
				PayrollServices.commitUnitOfWork();

			} finally {
				PayrollServices.rollbackUnitOfWork();
			}
		}
	}

	public static class PersistReturnsFile extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);

			List<SpcfUniqueId> batchIdList = new Vector<SpcfUniqueId>();
			try {
				PayrollServices.beginUnitOfWork();
				DomainEntitySet<TransactionReturnBatch> batchList = TransactionReturnBatch.getReceivedBatches();
				for (TransactionReturnBatch batch : batchList) {
					batchIdList.add(batch.getId());
				}
			} finally {
				PayrollServices.rollbackUnitOfWork();
			}

			if (batchIdList.isEmpty()) {
				getLogger().warn("There are no ACH Returns batches in a Received state to be processed.");
				return;
			}

			for (SpcfUniqueId batchId : batchIdList) {
				// process the file and create TransactionReturn records based
				// on file contents
				try {
					PayrollServices.beginUnitOfWork();
					ReturnFileParser returnFileParser = new ReturnFileParser();
					TransactionReturnBatch returnBatch = returnFileParser.parseAndPersist(batchId);

					String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
					String batchJobName = BatchJobType.NightlyBatchJobs.name();

					returnBatch.setACHReturnFileName(S3UploadUtils.archive(batchJobName,archiveDir,returnBatch.getACHReturnFileName()));

					PayrollServices.commitUnitOfWork();
				} catch (Throwable t){
					getLogger().error("Error in executing job step PersistReturnsFile",t);
				} finally {
					PayrollServices.rollbackUnitOfWork();
				}
			}
		}
	}

	/**
	 * process the returns - all returns should move to Open or Resolved
	 */
	public static class ProcessAchReturnsBatch extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);

			List<SpcfUniqueId> batchIdList = new Vector<SpcfUniqueId>();
			try {
				PayrollServices.beginUnitOfWork();
				DomainEntitySet<TransactionReturnBatch> batchList = TransactionReturnBatch.getReadyToProcessBatches();
				for (TransactionReturnBatch batch : batchList) {
					batchIdList.add(batch.getId());
				}
			} finally {
				PayrollServices.rollbackUnitOfWork();
			}

			if (batchIdList.isEmpty()) {
				getLogger().warn("There are no ACH Returns batches to be processed.");
				return;
			}

			for (SpcfUniqueId batchId : batchIdList) {
				try {
					ReturnFileParser returnFileParser = new ReturnFileParser();
					returnFileParser.processReturnBatch(batchId);

					PayrollServices.beginUnitOfWork();
					TransactionReturnBatch returnBatch = Application.findById(TransactionReturnBatch.class, batchId);
					if (assertAllReturnsProcessed(returnBatch)) {
						returnBatch.setStatusCd(TransactionReturnBatchStatusCode.Processed);
						PayrollServices.commitUnitOfWork();
					}
					PayrollServices.rollbackUnitOfWork();
				} finally {
					PayrollServices.rollbackUnitOfWork();
				}
			}
		}

		private boolean assertAllReturnsProcessed(TransactionReturnBatch pReturnBatch) {
			DomainEntitySet<TransactionReturn> unprocessedReturns = TransactionReturnBatch
					.getUnprocessedTransactionReturns(pReturnBatch.getId());
			if (unprocessedReturns.size() > 0) {
				StringBuilder msg = new StringBuilder(unprocessedReturns.size() * 128);
				msg.append("ACH return processing completed processing but all returns not processed").append("\n\t")
						.append(pReturnBatch)
						.append("\n-------------------------[ unprocessed returns ]-------------------------\n");
				for (TransactionReturn aReturn : unprocessedReturns) {
					msg.append(aReturn).append("\n");
				}

				// throw an exception to terminate workflow - we don't want ACH
				// Transaction processor to run
				throw new RuntimeException(msg.toString());
			}

			return unprocessedReturns.size() == 0;
		}
	}

	public static class NotifyReturnProcessingResults extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		@Override
		public void execute() {
			PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);

			try {
				PayrollServices.beginUnitOfWork();
				DomainEntitySet<TransactionReturnBatch> processedBatches = TransactionReturnBatch.getProcessedBatches();
				PayrollServices.rollbackUnitOfWork();

				if (processedBatches.isEmpty()) {
					getLogger().warn("There are no ACH Returns batches in the "
							+ TransactionReturnBatchStatusCode.Processed + " state to generate notifications for.");
					return;
				}

				for (TransactionReturnBatch processedBatch : processedBatches) {
					try {
						PayrollServices.beginUnitOfWork();
						processedBatch = Application.refresh(processedBatch);
						BatchUtils.createAchReturnAccountingFileAndEmail(processedBatch.getId());
						processedBatch.setStatusCd(TransactionReturnBatchStatusCode.Completed);
						PayrollServices.commitUnitOfWork();
					} catch (Throwable t) {
						getLogger().error("failed to create accounting file and email for " + processedBatch, t);
					} finally {
						PayrollServices.rollbackUnitOfWork();
					}
				}
			} finally {
				PayrollServices.rollbackUnitOfWork();
			}
		}
	}

	/**
	 * THIS SHOULD ONLY BE USED FOR TEST - TO MANUALLY LOAD A FILE IN PRODUCTION: 1 - ./engBatchJobManager.sh runstep
	 * DailyAchProcessor CreateReturnBatch <file-name> 2 - ./engBatchJobManager.sh run DailyAchProcessor achret
	 */
	public static class ProcessAchReturnsFile extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			try {
				PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);

				try {
					File file = new File(getBatchJobProcessor().getReturnsFileName());

					SpcfUniqueId batchId = new ReturnFileParser().processFile(file);

					PayrollServices.beginUnitOfWork();
					TransactionReturnBatch returnBatch = Application.findById(TransactionReturnBatch.class, batchId);
					PayrollServices.rollbackUnitOfWork();

					// Create NSF return file and email for accounting
					if (returnBatch.getStatusCd() == TransactionReturnBatchStatusCode.Completed) {
						PayrollServices.beginUnitOfWork();
						BatchUtils.createAchReturnAccountingFileAndEmail(batchId);
						PayrollServices.rollbackUnitOfWork();
					}
				} finally {
					PayrollServices.rollbackUnitOfWork();
				}
			} catch (Throwable t) {
				throw new RuntimeException("Exception in job step ProcessAchReturnsFile ", t);
			}
		}
	}

	public static class ProcessAchTransactions extends JSSBatchJobStep<NightlyBatchJobsProcessor> {
		public void execute() {
			try {
				PayrollServices.setCurrentPrincipal(SystemPrincipal.AchTransactionsBatchJob);

				// ProcessACHTransactions will manage its own transaction(s)
				new ProcessACHTransactions().process(getBatchJobProcessor().getProcessingDate());
			} catch (Throwable t) {
				throw new RuntimeException("Exception in job step ProcessAchTransactions ", t);
			}
		}
	}

}
