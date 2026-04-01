package com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.configuration.Database;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.mapper.SoxRowMapper;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataModel;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SoxUserDAO {
    private SoxRowMapper soxRowMapper;
    private SoxQueryFactory soxQueryFactory;

    @Autowired
    public SoxUserDAO(SoxRowMapper soxRowMapper, SoxQueryFactory soxQueryFactory)
    {
        this.soxRowMapper = soxRowMapper;
        this.soxQueryFactory = soxQueryFactory;
    }

    public List<SoxUserDataModel> queryDatabase(SoxDataManager dataManager, String accessType){
        String query = soxQueryFactory.getSoxDataQuery(dataManager);
        switch(dataManager){
            case DATA_MANAGER_APP:
            case DATA_MANAGER_DB_MONOLITH:
                return queryPrimaryDatabase(query, dataManager.value(), accessType);
            case DATA_MANAGER_DB_AUDIT:
                return querySecondaryDatabase(query, dataManager.value(), accessType);
        }
        throw new RuntimeException("Event=SoxReportBatchJobError: invalid dataManager");
    }

    private List<SoxUserDataModel> queryPrimaryDatabase(String query, String dmName, String accessType) {
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL);

            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.SoxReportBatchJob));
            Session session = Application.getHibernateSession();

            return fetchUserList(query, dmName, accessType, session, Database.MONOLITH);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private List<SoxUserDataModel> querySecondaryDatabase(String query, String dmName, String accessType) {
        try {
            ApplicationSecondary.beginUnitOfWork(FlushMode.MANUAL);
            Session session = ApplicationSecondary.getHibernateSession();

            return fetchUserList(query, dmName, accessType, session, Database.AUDIT);

        } finally {
            ApplicationSecondary.rollbackUnitOfWork();
        }
    }

    private List<SoxUserDataModel> fetchUserList(String query, String dmName, String accessType, Session session, Database database){
        String dbName = getDBName(session, database);
        ScrollableResults queryResult = getQueryResults(query, session);
        return soxRowMapper.parseQueryResults(queryResult, dbName, dmName, accessType);
    }

    private String getDBName(Session session, Database database) {
        String dbName = null;

        ScrollableResults queryResult = getQueryResults(soxQueryFactory.getDBNameQuery(database), session);
        if(queryResult.next()) {
            dbName = (String) queryResult.get(0);
        }

        return dbName;
    }

    private ScrollableResults getQueryResults(String query, Session session) {
        Query dbQuery = session.createSQLQuery(query);
        ScrollableResults queryResult = dbQuery.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
        return queryResult;
    }
}
