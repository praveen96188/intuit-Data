package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.mapper;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.StatementType;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.constants.PlSqlConstants;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@Component
public class PlSqlMapper {


    public HashMap<String, StatementType> plsqlStatementTypeMap;
    private HashMap<String, String> plsqlNamedQueryMap;
    public HashMap<String, String> plsqlOutputMap;
    public HashMap<String, StoredProcedures> plsqlProcedureMap;
    private HashMap<String, Class> prcCostCoPlsqlOutParamType;
    private HashMap<String, String> prcCostCoPlsqlOutputTemplate;
    private HashMap<String, Class> prcPayrollFraudBatchOutParamType;
    private HashMap<String, String> prcPayrollFraudBatchOutputTemplate;
    public HashMap<String, HashMap<String, Class>> procedureOutputParamTypeMap;
    private HashMap<String, HashMap<String, String>> procedureOutputTemplate;

    @PostConstruct
    public void init() {

        populatePlSqlStatementTypeMap();
        populatePlSqlNamedQueryMap();
        populatePlSqlOutputMap();
        populatePlSqlProcedureMap();
        populateCostCoPlSqlOutParamType();
        populateCostCoPlSqlOutputTemplate();
        populatePayrollFraudBatchOutParamType();
        populatePayrollFraudBatchOutputTemplate();
        populateProcedureOutputParamTypeMap();
        populateProcedureOutputTemplate();

    }

    /**
     * map containing batchjob and it's StatementType
     */
    private void populatePlSqlStatementTypeMap() {

        plsqlStatementTypeMap = new HashMap<>();
        plsqlStatementTypeMap.put(PlSqlConstants.EFTPS_ONHOLD_PAYMENT_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.FAILED_PAYROLL_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.EDR_ASSOCIATION_FIX_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.NCD_FIX_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.VALIDATE_EMPLOYEE_WAGE_PLANS_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.RETRY_ENTITLEMENT_ACTIVATION_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.NCD_FIX_ALL_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.OFFERING_UPDATE_USAGE_BILLING_PLSQL_JOBS_PROCESSOR, StatementType.NamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.PAYROLL_FRAUD_BATCH_PURGE_PLSQL_JOBS_PROCESSOR, StatementType.ProcedureAndNamedQuery);
        plsqlStatementTypeMap.put(PlSqlConstants.COST_CO_PLSQL_JOBS_PROCESSOR, StatementType.Procedure);
    }
    /**
     * map storing batchjob and it's named query
     */
    private void populatePlSqlNamedQueryMap() {
        plsqlNamedQueryMap = new HashMap<>();
        plsqlNamedQueryMap.put(PlSqlConstants.EFTPS_ONHOLD_PAYMENT_PLSQL_JOBS_PROCESSOR, "updateMoneyMovementStatus");
        plsqlNamedQueryMap.put(PlSqlConstants.FAILED_PAYROLL_PLSQL_JOBS_PROCESSOR, "updateFailedPayrollStatus");
        plsqlNamedQueryMap.put(PlSqlConstants.EDR_ASSOCIATION_FIX_PLSQL_JOBS_PROCESSOR, "updateEntryDetail");
        plsqlNamedQueryMap.put(PlSqlConstants.NCD_FIX_PLSQL_JOBS_PROCESSOR, "updateEntitlementStartDate");
        plsqlNamedQueryMap.put(PlSqlConstants.VALIDATE_EMPLOYEE_WAGE_PLANS_PLSQL_JOBS_PROCESSOR, "updateEmployeeWageInvalidDate");
        plsqlNamedQueryMap.put(PlSqlConstants.RETRY_ENTITLEMENT_ACTIVATION_PLSQL_JOBS_PROCESSOR, "updateEntitlementUnitStatus");
        plsqlNamedQueryMap.put(PlSqlConstants.NCD_FIX_ALL_PLSQL_JOBS_PROCESSOR, "updateEntitlementChargeDate");
        plsqlNamedQueryMap.put(PlSqlConstants.OFFERING_UPDATE_USAGE_BILLING_PLSQL_JOBS_PROCESSOR, "updateCompanyOfferingFK");
        plsqlNamedQueryMap.put(PlSqlConstants.PAYROLL_FRAUD_BATCH_PURGE_PLSQL_JOBS_PROCESSOR, "deletePayrollFraudBatch");
    }

    /**
     * map storing batchjob and its output template
     */
    private void populatePlSqlOutputMap() {
        plsqlOutputMap = new HashMap<>();
        plsqlOutputMap.put(PlSqlConstants.EFTPS_ONHOLD_PAYMENT_PLSQL_JOBS_PROCESSOR, PlSqlConstants.EFTPS_ONHOLD_PAYMENT);
        plsqlOutputMap.put(PlSqlConstants.FAILED_PAYROLL_PLSQL_JOBS_PROCESSOR, PlSqlConstants.FAILED_PAYROLL);
        plsqlOutputMap.put(PlSqlConstants.EDR_ASSOCIATION_FIX_PLSQL_JOBS_PROCESSOR, PlSqlConstants.EDR_ASSOCIATION_FIX);
        plsqlOutputMap.put(PlSqlConstants.NCD_FIX_PLSQL_JOBS_PROCESSOR, PlSqlConstants.NCD_FIX);
        plsqlOutputMap.put(PlSqlConstants.VALIDATE_EMPLOYEE_WAGE_PLANS_PLSQL_JOBS_PROCESSOR, PlSqlConstants.VALIDATE_EMPLOYEE_WAGE_PLANS);
        plsqlOutputMap.put(PlSqlConstants.RETRY_ENTITLEMENT_ACTIVATION_PLSQL_JOBS_PROCESSOR, PlSqlConstants.RETRY_ENTITLEMENT_ACTIVATION);
        plsqlOutputMap.put(PlSqlConstants.NCD_FIX_ALL_PLSQL_JOBS_PROCESSOR, PlSqlConstants.NCD_FIX_ALL);
        plsqlOutputMap.put(PlSqlConstants.OFFERING_UPDATE_USAGE_BILLING_PLSQL_JOBS_PROCESSOR, PlSqlConstants.OFFERING_UPDATE_USAGE_BILLING);
        plsqlOutputMap.put(PlSqlConstants.PAYROLL_FRAUD_BATCH_PURGE_PLSQL_JOBS_PROCESSOR, PlSqlConstants.PAYROLL_FRAUD_BATCH_PURGE);
    }
    /**
     * map containing batchjob and it's procedure
     */
    private void populatePlSqlProcedureMap() {
        plsqlProcedureMap = new HashMap<>();
        plsqlProcedureMap.put(PlSqlConstants.PAYROLL_FRAUD_BATCH_PURGE_PLSQL_JOBS_PROCESSOR, StoredProcedures.PRC_PAYROLL_FRAUDBATCH_PURGE_DBUPGRADE_PLSQL_JOBS_PROCESSOR
        );
        plsqlProcedureMap.put(PlSqlConstants.COST_CO_PLSQL_JOBS_PROCESSOR, StoredProcedures.PRC_COST_CO_PLSQL_JOBS_PROCESSOR);

    }
    /**
     * CostCoPlSqlJobsProcessor
     * map containing outputparam's name and its type
     */
    private void populateCostCoPlSqlOutParamType() {
        prcCostCoPlsqlOutParamType = new LinkedHashMap<>();
        prcCostCoPlsqlOutParamType.put("v_ce_ins_cnt", Integer.class);
        prcCostCoPlsqlOutParamType.put("v_ced_ins_cnt", Integer.class);
    }

    /**
     * CostCoPlSqlJobsProcessor
     * map containing outputparam's name and its template
     */
    private void populateCostCoPlSqlOutputTemplate() {
        prcCostCoPlsqlOutputTemplate = new LinkedHashMap<>();
        prcCostCoPlsqlOutputTemplate.put("v_ce_ins_cnt", "inserted %s events");
        prcCostCoPlsqlOutputTemplate.put("v_ced_ins_cnt", "inserted %s event_details");
    }
    /**
     * PayrollFraudBatchPurgePlSqlJobsProcessor
     * map containing outputparam's name and its type
     */
    private void populatePayrollFraudBatchOutParamType() {
        prcPayrollFraudBatchOutParamType = new LinkedHashMap<>();
        prcPayrollFraudBatchOutParamType.put("v_count", Integer.class);
        prcPayrollFraudBatchOutParamType.put("v_iteration_cnt", Integer.class);
        prcPayrollFraudBatchOutParamType.put("v_count_audit_log", Integer.class);
        prcPayrollFraudBatchOutParamType.put("v_count_delete", Integer.class);
        prcPayrollFraudBatchOutParamType.put("v_start_time", Timestamp.class);
        prcPayrollFraudBatchOutParamType.put("v_error", String.class);
    }

    /**
     * PayrollFraudBatchPurgePlSqlJobsProcessor
     * map containing outputparam's name and its template
     */
    private void populatePayrollFraudBatchOutputTemplate() {
        prcPayrollFraudBatchOutputTemplate = new LinkedHashMap<>();
        prcPayrollFraudBatchOutputTemplate.put("v_start_time", "Started at: %s");
        prcPayrollFraudBatchOutputTemplate.put("v_count_audit_log", "Audit log count before purge: %s");
        prcPayrollFraudBatchOutputTemplate.put("v_count", "Total rows to delete : %s");
        prcPayrollFraudBatchOutputTemplate.put("v_count_delete", "Total rows deleted : %s");
        prcPayrollFraudBatchOutputTemplate.put("v_iteration_cnt", "Total update iterations : %s");
        prcPayrollFraudBatchOutputTemplate.put("v_error", "Error : %s");
    }
    /**
     * map containing procedure and output parameters's name and type
     */
    private void populateProcedureOutputParamTypeMap() {
        procedureOutputParamTypeMap = new HashMap<>();
        procedureOutputParamTypeMap.put(PlSqlConstants.PRC_COST_CO_PLSQL_JOBS_PROCESSOR, prcCostCoPlsqlOutParamType);
        procedureOutputParamTypeMap.put(PlSqlConstants.PRC_PAYROLL_FRAUDBATCH_PURGE_DBUPGRADE_PLSQL_JOBS_PROCESSOR, prcPayrollFraudBatchOutParamType);
    }
    /**
     * map containing procedure and output parameters's name and template
     */
    private void populateProcedureOutputTemplate() {
        procedureOutputTemplate = new HashMap<>();
        procedureOutputTemplate.put(PlSqlConstants.PRC_COST_CO_PLSQL_JOBS_PROCESSOR, prcCostCoPlsqlOutputTemplate);
        procedureOutputTemplate.put(PlSqlConstants.PRC_PAYROLL_FRAUDBATCH_PURGE_DBUPGRADE_PLSQL_JOBS_PROCESSOR, prcPayrollFraudBatchOutputTemplate);
    }

    public String getNamedQuery(String batchJobName) {
        return Application.getQueryName(plsqlNamedQueryMap.get(batchJobName));
    }

    /**
     * Returns the final output template and param value
     * @param outParamValue map
     * @param procedure procedure
     * @return map
     */
    public Map<String, Object> getOutput(Map<String, Object> outParamValue, String procedure) {

        HashMap<String, Object> finalOutputMap = new LinkedHashMap<>();
        Map<String, String> procedureOutputTemplate = this.procedureOutputTemplate.get(procedure);
        for (Map.Entry<String, String> procedureOutput : procedureOutputTemplate.entrySet()) {
            if (outParamValue.get(procedureOutput.getKey()) != null) {
                finalOutputMap.put(procedureOutput.getValue(), outParamValue.get(procedureOutput.getKey()));
            }
        }
        return finalOutputMap;
    }

    /**
     * returns current time
     * @return
     */
    public String getCurrentTime() {
        return SpcfCalendar.createInstance(SpcfCalendar.getNow().getTimeInMilliseconds(), SpcfTimeZone.getLocalTimeZone()).format("yyyy-MM-dd HH:mm:ss:SSS");
    }

}
