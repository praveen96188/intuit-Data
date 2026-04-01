package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.executor;

import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.StatementType;

import java.util.Map;

public interface StatementExecutor {

     StatementType getType();
     Map<String, Object> execute(String BatchJobName);

}
