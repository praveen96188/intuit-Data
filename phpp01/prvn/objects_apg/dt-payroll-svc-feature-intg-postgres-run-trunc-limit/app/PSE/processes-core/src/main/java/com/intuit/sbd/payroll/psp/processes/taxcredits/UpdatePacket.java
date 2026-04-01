package com.intuit.sbd.payroll.psp.processes.taxcredits;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.TaxCreditsApplication;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: dweinberg
 * Date: Sep 22, 2010
 * Time: 3:12:26 PM
 */
public class UpdatePacket extends Process {

    private String documentKey;
    private byte[] signedPacket;
    private String signersRemaining;

    private TaxCreditsApplication mApplication;

    public UpdatePacket(String documentKey, byte[] signedPacket, String signersRemaining) {
        this.documentKey = documentKey;
        this.signedPacket = signedPacket;
        this.signersRemaining = signersRemaining;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (documentKey == null) {
            validationResult.getMessages().GenericError(EntityName.TaxCreditsPacket, "unknown", "Document key not specified");
            return validationResult;
        }

        if (signedPacket == null || signedPacket.length == 0) {
            validationResult.getMessages().GenericError(EntityName.TaxCreditsPacket, documentKey, "Signed packet not specified");
            return validationResult;
        }

        DomainEntitySet<TaxCreditsApplication> applications = Application.find(TaxCreditsApplication.class, TaxCreditsApplication.DocumentKey().equalTo(documentKey));
        if (applications.size() != 1) {
            validationResult.getMessages().GenericError(EntityName.TaxCreditsPacket, documentKey, "Exactly 1 packet must contain this document key; was " + applications.size());
            return validationResult;
        }
        mApplication = applications.get(0);

        return validationResult;

    }

    @Override
    public ProcessResult<TaxCreditsApplication> process() {
        ProcessResult<TaxCreditsApplication> pr = new ProcessResult<TaxCreditsApplication>();

        mApplication.setSignedDocumentBytes(signedPacket);
        mApplication.setSignersRemaining(signersRemaining);

        Application.save(mApplication);

        pr.setResult(mApplication);

        return pr;
    }
}
