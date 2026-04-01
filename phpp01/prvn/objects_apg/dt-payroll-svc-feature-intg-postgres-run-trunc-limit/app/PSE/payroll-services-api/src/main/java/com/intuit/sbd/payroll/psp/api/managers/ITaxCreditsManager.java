package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.TaxCredits9061DTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * User: dweinberg
 * Date: Jan 25, 2010
 * Time: 1:13:33 PM
 */
public interface ITaxCreditsManager {
    public ProcessResult add9061Form(TaxCredits9061DTO dto);
    public ProcessResult updatePacket(String documentKey, byte[] signedPacket, String signersRemaining);
}
