package com.intuit.sbd.payroll.psp.processes.iam;

import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.processes.ConstantValues;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Objects;

/**
 *   AddOrUpdateVMPGrantProcessor
 *
 *   Creates VMP Grant if the request has a specific header i.e intuit_autovmpsource
 *
 */

public class AddOrUpdateVMPGrantProcessor {

    private static final SpcfLogger logger = Application.getLogger(AddOrUpdateVMPGrantProcessor.class);

    private RealmManager realmManager;
    private String sourceName;
    private Company company;

    public AddOrUpdateVMPGrantProcessor(Company domainCompany) {
        this.company = domainCompany;
        this.realmManager = new RealmManager();
        this.sourceName = RequestAttributesUtils.getAttribute(ConstantValues.HEADER_INTUIT_AUTO_VMP_SOURCE, String.class);
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (Objects.isNull(company)) {
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany");
            return validationResult;
        }

        //Realm id can be null for legacy company. So do realm validation only for SMS onboarded companies
        if (Objects.nonNull(sourceName) && Objects.isNull(company.getIAMRealmId())) {
            validationResult.getMessages()
                    .BadProcessArgument("IAMRealmId");
            return validationResult;
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();

        //Return the request coming from EWS
        if(Objects.isNull(sourceName)) {
            logger.info("Add or update VMP Grant initiated from EWS, skipping VMP grant addition. PSID=" + company.getSourceCompanyId());
            return processResult;
        }

        try {
            Grant grant = realmManager.addVMPGrant(company);

            //if grant is added return success else return error message
            if(Objects.nonNull(grant)) {
                logger.info(String.format("VMP Grant added for the realmId=%s", company.getIAMRealmId()));
                processResult.setResult(grant);
            } else {
                logger.error(String.format("Unable to add VMP grant realmId=%s", company.getIAMRealmId()));
                processResult.getMessages().VMPGrantAdditionError(company.getIAMRealmId(), "Unable to add VMP grant");
            }
        } catch (RuntimeException e) {
            logger.error(String.format("Unable to add VMP grant realmId=%s \n "+e, company.getIAMRealmId()));
            processResult.getMessages().VMPGrantAdditionError(company.getIAMRealmId(), "Unable to add VMP grant");
        }
        return processResult;
    }
}