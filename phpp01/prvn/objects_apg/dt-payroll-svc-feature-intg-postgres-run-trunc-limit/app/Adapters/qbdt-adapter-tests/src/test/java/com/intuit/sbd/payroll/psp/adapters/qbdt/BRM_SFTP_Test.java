package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.billing.UsageBillingTestsBase;
import com.intuit.sbd.payroll.psp.adapters.qbdt.billing.UsageOFXDataloader;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.BRMUsageErrorFileProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EMSBSToBRMDataSyncProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Created by rvl on 5/8/2016.
 */
public class BRM_SFTP_Test extends UsageBillingTestsBase {

    public static final String mCloudPSID = "8574536";
    static final SpcfLogger logger = SpcfLogManager.getLogger(BRM_SFTP_Test.class);

    @Ignore /* Test in failing at Download step as BRM would not have generated error file yet which we are trying to download */
    @Test
    public void basicTest() throws Exception {
        int NUMBER_OF_RETRIES=2;
        SpcfCalendar curTimeStamp=null;
        boolean isFileFound=false;
        Exception lastException=null;
        SpcfCalendar calendar = SpcfCalendar.createInstance(new Date().getTime(), SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(calendar);

        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchmannn", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        DataLoadServices.setPSPDate(calendar);
        String calDate = calendar.format("yyyyMMddHHmmss");
        curTimeStamp=PSPDate.getPSPTime().toLocal();
        String pspDate = curTimeStamp.format("yyyyMMddHHmmss");
        logger.info("calDate : " + calDate + ", PSPDate : " + pspDate);

        // Testing File Upload
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        // Waiting for AIA to process the uploaded file
        Thread.sleep(5000);

        // Testing Error File Download
        String fileName = "PSP_SymphonyUsage_" + pspDate + "_Error";
        logger.info("Downloading Error file : " + fileName);
        String args = fileName;
        for(int itr=0;itr<NUMBER_OF_RETRIES;itr++) {
            try {
                BRMUsageErrorFileProcessor brmUsageErrorFileProcessor = new BRMUsageErrorFileProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.BRMUsageErrorFileProcessor, "3", args);
                brmUsageErrorFileProcessor.executeJob();
                isFileFound=true;
                break;
            }
            catch(RuntimeException e){
                lastException=e;
                curTimeStamp.addSeconds(1);
                pspDate = curTimeStamp.format("yyyyMMddHHmmss");
                fileName = "PSP_SymphonyUsage_" + pspDate + "_Error";
                args=fileName;
                logger.info("Error downloading BRM file. Retrying: "+Integer.toString(itr+1)+" of "+NUMBER_OF_RETRIES);
            }
        }
        if(!isFileFound){throw lastException;}
        // Waiting for PSP to process downloaded file
        // Thread.sleep(5000);

        File file = new File(BRMFileUploader.LOCAL_RECV_DIR + fileName + BRMFileUploader.FILENAME_EXT);
        Assert.assertTrue(file.exists());

    }
}
