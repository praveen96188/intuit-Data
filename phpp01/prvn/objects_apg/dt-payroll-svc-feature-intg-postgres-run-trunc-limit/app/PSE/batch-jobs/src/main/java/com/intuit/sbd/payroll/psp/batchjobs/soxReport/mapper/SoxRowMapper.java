package com.intuit.sbd.payroll.psp.batchjobs.soxReport.mapper;

import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxResultSet;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserAdditionalPropertiesModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataModel;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import org.hibernate.ScrollableResults;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SoxRowMapper {
    public List<SoxUserDataModel> parseQueryResults(ScrollableResults results, String dbName, String dmName, String accessType)
    {
        List<SoxUserDataModel> userDataList = new ArrayList();
        while (results.next()) {

            SoxUserDataModel dbUserData = new SoxUserDataModel();
            dbUserData.setAccessType(accessType);
            dbUserData.setBatchId(null);
            dbUserData.setCreatedDate((Timestamp) results.get(SoxResultSet.CREATED.getIndex()));
            dbUserData.setUsername((String) results.get(SoxResultSet.USERNAME.getIndex()));
            dbUserData.setDatabaseName(dbName);
            dbUserData.setDataManagerName(dmName);
            dbUserData.setTransactionId(UUID.randomUUID().toString());
            SoxUserAdditionalPropertiesModel additionalProp = new SoxUserAdditionalPropertiesModel();
            additionalProp.setAccess((String) results.get(SoxResultSet.ACCESS.getIndex()));
            additionalProp.setProfile((String) results.get(SoxResultSet.PROFILE.getIndex()));
            additionalProp.setStatus((String) results.get(SoxResultSet.ACCOUNT_STATUS.getIndex()));
            dbUserData.setSoxUserAdditionalPropertiesModel(additionalProp);

            userDataList.add(dbUserData);
        }
        return userDataList;
    }
}
