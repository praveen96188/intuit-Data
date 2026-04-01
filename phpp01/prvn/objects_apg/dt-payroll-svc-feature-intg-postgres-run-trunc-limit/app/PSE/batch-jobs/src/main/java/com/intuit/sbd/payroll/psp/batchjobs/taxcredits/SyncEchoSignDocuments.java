package com.intuit.sbd.payroll.psp.batchjobs.taxcredits;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.TaxCreditsApplication;
import com.intuit.sbd.payroll.psp.gateways.echosign.DocumentSigners;
import com.intuit.sbd.payroll.psp.gateways.echosign.EchoSignGateway;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: dweinberg
 * Date: Sep 22, 2010
 * Time: 4:29:05 PM
 */
public class SyncEchoSignDocuments {

    private static SpcfLogger logger = Application.getLogger(SyncEchoSignDocuments.class);

    

    public void sync() {
        logger.info("Beginning EchoSign Sync");

        List<String> ids = new ArrayList<String>();

        int daysBack = SystemParameter.findIntValue(SystemParameter.Code.TAX_CREDITS_ECHOSIGN_SYNC_WINDOW, 45);
        SpcfCalendar daysBackFromToday = PSPDate.getPSPTime();
        daysBackFromToday.addDays(-1 * daysBack);

        DomainEntitySet<TaxCreditsApplication> applications = Application.find(TaxCreditsApplication.class,
                TaxCreditsApplication.SignersRemaining().isNotNull()
                        .And(TaxCreditsApplication.CreatedDate().greaterOrEqualThan(daysBackFromToday)));

        for (TaxCreditsApplication application : applications) {
            ids.add(application.getDocumentKey());
        }
        
        logger.info("Syncing " + ids.size() + " echosign docs");

        Map<String, DocumentSigners> documentSignersMap = EchoSignGateway.getDocumentUpdates(ids);

        for (TaxCreditsApplication application : applications) {
            DocumentSigners documentSigners = documentSignersMap.get(application.getDocumentKey());
            String signersRemaining = getSignersRemainingString(application, documentSigners);
            if (! application.getSignersRemaining().equals(signersRemaining)) {
                logger.info("Updating echosign packet " + application.getId().toString());
                ProcessResult pr = PayrollServices.taxCreditsManager.updatePacket(application.getDocumentKey(),
                        EchoSignGateway.getLatestSignedDocument(application.getDocumentKey()),
                        signersRemaining);
                if (!pr.isSuccess()) {
                    throw new RuntimeException("Could not update packet: " + pr.getMessages().toString());
                }
            }
        }

        logger.info("Finished echosign syncing");
    }

    private String getSignersRemainingString(TaxCreditsApplication application, DocumentSigners documentSigners) {
        ArrayList<String> signersRemaining = new ArrayList<String>();
        for (String unsignedEmail : documentSigners.getSignersRemaining()) {
            if (application.getEmployerEmail().equals(unsignedEmail)) {
                signersRemaining.add("ER");
            } else if (application.getEmployeeEmail().equals(unsignedEmail)) {
                signersRemaining.add("EE");
            }
        }
        if (signersRemaining.size() > 0) {
            //always want ER before EE
            if (signersRemaining.size() == 2 && signersRemaining.get(0).equals("EE")) {
                signersRemaining.set(0, "ER");
                signersRemaining.set(1, "EE");
            }
            return StringUtils.join(signersRemaining.iterator(), ", ");
        } else {
            return null;
        }        
    }


    public static void main(String[] args) {
        PayrollServices.beginUnitOfWork();
        new SyncEchoSignDocuments().sync();
        PayrollServices.commitUnitOfWork();
    }
}
