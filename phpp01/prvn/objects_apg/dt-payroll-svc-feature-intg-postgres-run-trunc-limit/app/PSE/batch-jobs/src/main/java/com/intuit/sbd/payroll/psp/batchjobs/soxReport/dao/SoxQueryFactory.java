package com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao;

import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.configuration.Database;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SoxQueryFactory {

    public String getSoxDataQuery(SoxDataManager dataManager){
        String dbToken= StringUtils.EMPTY;
        switch (dataManager){
            case DATA_MANAGER_APP:
                return BatchJobConstants.SOX_APP_QUERY;
            case DATA_MANAGER_DB_MONOLITH:
                dbToken = DatabaseConfigManager.MonolithDbToken;
                break;
            case DATA_MANAGER_DB_AUDIT:
                dbToken = DatabaseConfigManager.AuditDbToken;
                break;
            default:
                throw new RuntimeException("SoxReportBatchJobError: invalid dataManager");
        }

        return dbToken.contains(DatabaseType.ORACLE.toString()) ? BatchJobConstants.SOX_DB_QUERY_ORACLE : BatchJobConstants.SOX_DB_QUERY_POSTGRES;
    }

    public String getDBNameQuery(Database database){
        String dbToken=StringUtils.EMPTY;
        switch(database){
            case MONOLITH:
                dbToken=DatabaseConfigManager.MonolithDbToken;
                break;
            case AUDIT:
                dbToken=DatabaseConfigManager.AuditDbToken;
                break;
            default:
                throw new RuntimeException("SoxReportBatchJobError: invalid database");
        }

        return dbToken.contains(DatabaseType.ORACLE.toString()) ? BatchJobConstants.ORACLE_DB_NAME_QUERY : BatchJobConstants.POSTGRES_DB_NAME_QUERY;
    }
}
