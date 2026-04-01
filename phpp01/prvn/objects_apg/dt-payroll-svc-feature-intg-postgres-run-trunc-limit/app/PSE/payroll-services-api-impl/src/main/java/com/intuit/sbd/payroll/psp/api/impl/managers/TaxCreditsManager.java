package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.dtos.TaxCredits9061DTO;
import com.intuit.sbd.payroll.psp.api.managers.ITaxCreditsManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.taxcredits.Add9061Form;
import com.intuit.sbd.payroll.psp.processes.taxcredits.UpdatePacket;

/**
 * User: dweinberg
 * Date: Jan 25, 2010
 * Time: 1:15:55 PM
 */
public class TaxCreditsManager implements ITaxCreditsManager {
    public ProcessResult add9061Form(TaxCredits9061DTO dto) {
        return new Add9061Form(dto).execute();
    }

    public ProcessResult updatePacket(String documentKey, byte[] signedPacket, String signersRemaining) {
        return new UpdatePacket(documentKey, signedPacket, signersRemaining).execute();
    }
}
