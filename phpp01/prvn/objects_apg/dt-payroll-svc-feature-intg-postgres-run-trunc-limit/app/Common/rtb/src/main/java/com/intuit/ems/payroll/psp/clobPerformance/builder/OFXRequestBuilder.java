package com.intuit.ems.payroll.psp.clobPerformance.builder;

import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.domain.Company;

import java.util.List;

public class OFXRequestBuilder {

    public static OFX generateOFX(Company company, List<IEMP> iemps) {

        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(generateSignOnMessage(company.getSourceCompanyId()));

        IPAYROLLTRNRQ ipayrolltrnrq = EmployeeBuilder.generatePayrollRequest(false,
                null,
                null,
                iemps,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        ofx.setIPAYROLLMSGSRQV1(EmployeeBuilder.generatePayrollMessage(company, true, ipayrolltrnrq));
        return ofx;
    }

    private static SIGNONMSGSRQV1 generateSignOnMessage(String pUserId) {
        SIGNONMSGSRQV1 signonmsgsrqv1 = new SIGNONMSGSRQV1();
        SONRQ sonrq = new SONRQ();
        sonrq.setAPPID("QBWPRO");
        sonrq.setAPPVER("31.00.R.6/22211#pro");
        sonrq.setDTCLIENT("20220527055048");
        sonrq.setIIPADDRESS("FileInfo:QB_data_engine_31:10.72.220.42#0");
        sonrq.setIQBFILEID("36e9bc499ba34662ab0f2d208040c020");
        sonrq.setIQBFILENAME("C:\\Users\\Public\\Documents\\Intuit\\QuickBooks\\Company Files\\VBD_Symphony_ParallelTesting-Comp-01.qbw");
        sonrq.setIQBUSERNAME("Admin");
        sonrq.setLANGUAGE("ENG");
        sonrq.setUSERID(pUserId);
        sonrq.setUSERPASS("test1234");
        sonrq.setIRQEIN("04-7120202");
        sonrq.setISUBSCRIPTIONNUM("8474183");
        signonmsgsrqv1.setSONRQ(sonrq);
        return signonmsgsrqv1;
    }
}
