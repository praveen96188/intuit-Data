package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.service;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.mapper.PlSqlMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import javax.persistence.StoredProcedureQuery;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class PlSqlExecutorService {

    @Autowired
    private PlSqlMapper plSqlMapper;


    /**
     * Executes the stored procedure and returns the final output.
     *
     * @param batchJobName name
     * @return OutputParameter name-value map
     */
    public Map<String, Object> executeStoredProcedure(String batchJobName) {
        Map<String, Object> outParamValue = new HashMap<>();
        String procedure = plSqlMapper.plsqlProcedureMap.get(batchJobName).getStoredProcedureName();

        log.info("Event={} SubEvent=ExecuteStoredProcedure Status=Started Procedure={}", batchJobName, procedure);
        HashMap<String, Class> procedureOutParamMap = plSqlMapper.procedureOutputParamTypeMap.get(procedure);
        Session session;
        try {
            session = Application.getHibernateSession();
            StoredProcedureQuery query = session.createStoredProcedureQuery(procedure);
            for (Map.Entry<String, Class> procedureOutParam : procedureOutParamMap.entrySet()) {
                query.registerStoredProcedureParameter(procedureOutParam.getKey(), procedureOutParam.getValue(), ParameterMode.OUT);
            }
            query.execute();
            for (Map.Entry<String, Class> procedureOutParam : procedureOutParamMap.entrySet()) {
                outParamValue.put(procedureOutParam.getKey(), query.getOutputParameterValue(procedureOutParam.getKey()));
            }
            log.info("Event={} SubEvent=ExecuteStoredProcedure Status=Completed Procedure={} ", batchJobName, procedure);
        } catch (Exception exception) {
            log.error("Event={} SubEvent=ExecuteStoredProcedure Status=Error Procedure={} ", batchJobName, procedure, exception);
            throw new RuntimeException("Problem executing Procedure: procedureName=" + procedure, exception);
        }
        return plSqlMapper.getOutput(outParamValue, procedure);
    }

    /**
     *  Executes named query and returns output template and param value
     * @param batchJobName
     * @return
     */
    public Map<String, Object> executeNamedQuery(String batchJobName) {

        HashMap<String, Object> namedQueryOutput = new LinkedHashMap<>();
        String namedQuery = plSqlMapper.getNamedQuery(batchJobName);
        log.info("Event={} SubEvent=ExecuteNamedQuery Status=Started NamedQuery={}", batchJobName, namedQuery);
        Query queryObject = Application.getNamedQuery(namedQuery);
        try {
            namedQueryOutput.put("Start time: %s", plSqlMapper.getCurrentTime());
            int rowsUpdated = queryObject.executeUpdate();
            namedQueryOutput.put(plSqlMapper.plsqlOutputMap.get(batchJobName), rowsUpdated);
            log.info("Event={} SubEvent=ExecuteNamedQuery Status=Completed NamedQuery={}", batchJobName, namedQuery);
        } catch (PersistenceException e) {
            namedQueryOutput.put("Error message: %s", e.getMessage());
            log.error("Event={} SubEvent=ExecuteNamedQuery Status=Error NamedQuery={}", batchJobName, namedQuery, e);
        } catch (Exception e) {
            throw new RuntimeException("Problem executing NamedQuery: namedQuery=" + namedQuery, e);
        }
        namedQueryOutput.put("End time: %s", plSqlMapper.getCurrentTime());
        return namedQueryOutput;
    }

}
