package com.intuit.ems.payroll.psp.vmpRealmUpdate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
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
public class DeleteCompanyRealmProcessor {

    private FileParser fileParser;

    private int total= 0;
    private int success= 0;
    private int failed= 0;

    @Autowired
    public DeleteCompanyRealmProcessor(FileParser fileParser) {
        this.fileParser = fileParser;
    }

    public void process(String jiraId, String filePath) throws IOException {
        List<String> psidList= fileParser.parse(filePath);
        total= psidList.size();
        for (String psid:psidList
        ) {
            deleteCompanyRealm(psid, jiraId);
        }
        log.info("Event=DeleteCompanyRealm status=Completed total="+total+" success="+success+" failed="+failed);
    }


    private void deleteCompanyRealm(String psid, String jiraId) {
        log.info("Event=DeleteCompanyRealm status=start psid="+ psid);
        try {
            PayrollServices.beginUnitOfWork();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RTBAutomationBatchJob, jiraId));
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            CompanyEvent companyEvent = CompanyEvent.createCompanyInfoChangeEvent(company, company.getIAMRealmId(),
                    null, EventTypeCode.RealmIdUpdated);
            company.setIAMRealmId(null);
            //QBInfo Realm is not updated in AutoEnabledVMP workflow that's why not taking any action on QBInfo realm
            addCompanyNote(company, "Deleting RealmId added incorrectly via VMPAutoEnabled");
            log.info("Event=DeleteCompanyRealm status=success psid="+ psid);
            success++;
            PayrollServices.commitUnitOfWork();
        }catch(Exception e){
            log.error("Event=DeleteCompanyRealm status=failed psid="+ psid, e);
            failed++;
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    private static void addCompanyNote(Company company, String note){
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
        ProcessResult result = PayrollServices.companyManager.addCompanyNote(company.getSourceSystemCd(),
                company.getSourceCompanyId(), null,
                principal.getId(), note, false);
    }

}
