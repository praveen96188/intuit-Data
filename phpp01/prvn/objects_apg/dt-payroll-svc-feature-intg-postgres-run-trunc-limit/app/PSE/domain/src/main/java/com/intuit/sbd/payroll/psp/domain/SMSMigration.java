package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;

import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.BaseSMSMigration;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class SMSMigration extends BaseSMSMigration {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(SMSMigration.class);


    /**
     * Default constructor.
     */
    public SMSMigration() {
        super();
    }


    public static void saveSMSValidationResult(String psid, String smsValidationResult, SMSMigrationStatus smsMigrationStatus) {
               Company company = Company.findCompany(psid, com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT);
               DomainEntitySet<SMSMigration> migrationRows = getSmsMigrationByCompany(company);
                if (migrationRows.isEmpty()) {

                    logger.info("persisting new row in migration table for psid "+psid);
                    BaseSMSMigration smsMigration = new SMSMigration();
                    smsMigration.setSourceCompanyId(psid);
                    smsMigration.setMigrationStatus(smsMigrationStatus);
                    smsMigration.setValidationErrorResult(smsValidationResult);
                    smsMigration.setCompany(company);
                    Application.save(smsMigration);

                } else {
                    logger.info("Updating old row in migration table for psid "+psid);
                    SMSMigration smsMigration = migrationRows.get(0);
                    smsMigration.setMigrationStatus(smsMigrationStatus);
                    smsMigration.setValidationErrorResult(smsValidationResult);
                    Application.save(smsMigration);
                }



    }


    public static DomainEntitySet<SMSMigration> getSmsMigrationBySourceCompanyId(String sourceCOmpanyId) {

        Criterion<SMSMigration> query = SMSMigration.SourceCompanyId().equalTo(sourceCOmpanyId);
        return Application.find(SMSMigration.class, query);

    }

    public static DomainEntitySet<SMSMigration> getSmsMigrationByCompany(Company company) {
        Criterion<SMSMigration> query = SMSMigration.Company().equalTo(company);
        return Application.find(SMSMigration.class, query);
    }

    public static SMSMigrationStatus getSMSMigrationStatusByCompany(Company company) {
        DomainEntitySet<SMSMigration> migrationRows = getSmsMigrationByCompany(company);
        if(Objects.isNull(migrationRows) || migrationRows.isEmpty() || Objects.isNull(migrationRows.get(0)) ) {
            return null;
        }
        return migrationRows.get(0).getMigrationStatus();
    }

    public static List<String> getSMSMigrationCompanyIds(int getMaxCompaniesPerRun, SMSMigrationStatus status) {
        String namedQuery = "findCompaniesTermedForMigartion";
        String[] paramNames = new String[]{"migartionStatus"};
        Object[] paramValues = new Object[]{status};
        return Application.executeNamedQuery(namedQuery, paramNames, paramValues, -1, getMaxCompaniesPerRun);
    }

    public static void setSMSMigrationStatus(String psId, Company company, SMSMigrationStatus smsMigrationStatus) {
        DomainEntitySet<SMSMigration> migrationRows = getSmsMigrationByCompany(company);
        if (!migrationRows.isEmpty()) {
            logger.info("Updating old row in migration table for psid "+psId);
            SMSMigration smsMigration = migrationRows.get(0);
            smsMigration.setMigrationStatus(smsMigrationStatus);
            smsMigration.setValidationErrorResult(StringUtils.EMPTY);
            Application.save(smsMigration);
        } else {
            logger.error("Updating row in migration table that doesn't exist for psid "+psId);
        }
    }
}