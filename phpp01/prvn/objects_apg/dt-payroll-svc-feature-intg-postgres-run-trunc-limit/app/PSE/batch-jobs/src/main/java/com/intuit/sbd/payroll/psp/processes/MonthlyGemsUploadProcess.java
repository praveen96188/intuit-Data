package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.GemsMonthlyBalance;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatch;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatchStatus;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbg.psp.common.gateway.JSSGateway;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Aug 1, 2008
 * Time: 11:12:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class MonthlyGemsUploadProcess extends Process implements IProcess {
    private GemsUploadBatch mUploadBatch;
    private String mReportingPeriod;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public MonthlyGemsUploadProcess() {
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Get the previous month from the PSPDate
        Calendar currentDate = CalendarUtils.convertToCalendar(PSPDate.getPSPTime());
        currentDate.add(Calendar.MONTH, -1);
        mReportingPeriod = new SimpleDateFormat("yyyyMM").format(currentDate.getTime());

        //Get the GemsMonthlyBalance list for the reporting period.
        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalanceList = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ReportingPeriod().equalTo(mReportingPeriod));

        //Validate GemsMonthlyBalance List
        if (gemsMonthlyBalanceList.size() == 0) {
            validationResult.getMessages().MonthlyGemsUploadDataNotGenerated(EntityName.GemsUpload, "MonthlyGemsUpload",
                    mReportingPeriod);

            return validationResult;
        }

        //Get GemsUploadBatch from the GemsMonthly Balance List
        GemsMonthlyBalance monthlyBalance = gemsMonthlyBalanceList.get(0);
        mUploadBatch = monthlyBalance.getGemsUploadBatch();

        //Validation Gems Upload Status
        if (!mUploadBatch.getUploadStatus().equals(GemsUploadBatchStatus.Finalized)) {
            validationResult.getMessages().InvalidUploadStatus(EntityName.GemsUpload, "MonthlyGemsUpload",
                    String.valueOf(mUploadBatch.getBatchId()), mReportingPeriod, mUploadBatch.getUploadStatus().toString());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        try {
            // schedule gems file creation/upload
           
            String jobId = BatchUtils.scheduleJob(BatchJobType.GemsGeneralLedgerUpload.name(), null, "file " + mUploadBatch.getBatchId());
            BatchUtils.scheduleJob(BatchJobType.GemsGeneralLedgerUploadMonitor.name(), null, jobId);
        } catch (RuntimeException e) {
        	logger.error("Failed to create monthly gems upload process ", e.getStackTrace());
            processResult.getMessages().FailedToExecuteMonthlyGemsUploadProcess(EntityName.GemsUpload, "MonthlyGemsUpload", mReportingPeriod);
        } catch (Exception e) {
			// TODO Auto-generated catch block
        		logger.error("Failed to create monthly gems upload process ", e.getStackTrace());
        	  processResult.getMessages().FailedToExecuteMonthlyGemsUploadProcess(EntityName.GemsUpload, "MonthlyGemsUpload", mReportingPeriod);
		}

        return processResult;
    }
}
