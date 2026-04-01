package com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS;

import com.intuit.ems.tfs.messages.v1.FilingTypeType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: dhaddan
 * Date: 9/03/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class TFSW2AnnualSendJob {
    private static SpcfLogger logger = Application.getLogger(TFSW2AnnualSendJob.class);

    public ProcessResult main(String args[]) {
        ProcessResult processResult = new ProcessResult();

        try {
            logger.debug("Beginning TFS W2 Annual Send Job");
            SendW2DataToTFS sendW2DataToTFS = new SendW2DataToTFS(args, FilingTypeType.UnmodifiableAnnualData);
            processResult = sendW2DataToTFS.process();
            logger.debug("Completed TFS W2 Annual Send Job");
        } catch (Throwable t) {
            logger.error(t);
        }

        return processResult;
    }

}
