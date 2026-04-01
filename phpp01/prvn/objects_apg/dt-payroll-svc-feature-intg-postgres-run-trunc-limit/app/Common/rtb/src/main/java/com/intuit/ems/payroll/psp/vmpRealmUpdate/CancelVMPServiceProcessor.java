package com.intuit.ems.payroll.psp.vmpRealmUpdate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class CancelVMPServiceProcessor {

    private FileParser fileParser;

    private int total= 0;
    private int success= 0;
    private int failed= 0;

    @Autowired
    public CancelVMPServiceProcessor(FileParser fileParser) {
        this.fileParser = fileParser;
    }

    public void process(String jiraId, String filePath) throws IOException {
        List<String> psidList= fileParser.parse(filePath);
        total= psidList.size();
        for (String psid:psidList
             ) {
            cancelVMPforPSID(psid, jiraId);
        }
        log.info("Event=CancelVMP status=Completed total="+total+" success="+success+" failed="+failed);
    }

    private void cancelVMPforPSID(String psid, String jiraId) {
        log.info("Event=CancelVMP status=start psid="+ psid);
        try {
            PayrollServices.beginUnitOfWork();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RTBAutomationBatchJob, jiraId));
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            CompanyService vmpService = CompanyService.findCompanyService(company, ServiceCode.ViewMyPaycheck);
            vmpService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
            addCompanyNote(company, "Cancelling VMP service activated incorrectly via VMPAutoEnabled");
            log.info("Event=CancelVMP status=success psid="+ psid);
            success++;
            PayrollServices.commitUnitOfWork();
        }catch(Exception e){
            log.error("Event=CancelVMP status=failed psid="+ psid, e);
            failed++;
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void addCompanyNote(Company company,String note){
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
        ProcessResult result = PayrollServices.companyManager.addCompanyNote(company.getSourceSystemCd(),
                company.getSourceCompanyId(), null,
                principal.getId(), note, false);
    }

}
