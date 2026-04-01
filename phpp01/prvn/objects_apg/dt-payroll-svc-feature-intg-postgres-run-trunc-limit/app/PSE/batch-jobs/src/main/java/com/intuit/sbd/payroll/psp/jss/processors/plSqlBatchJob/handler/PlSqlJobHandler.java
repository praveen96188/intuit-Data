package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.handler;

import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.executor.StatementExecutor;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.factory.StatementExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PlSqlJobHandler {

    private StatementExecutorFactory statementExecutorFactory;
    private OutputHandler outputHandler;

    @Autowired
    public PlSqlJobHandler(StatementExecutorFactory statementExecutorFactory, OutputHandler outputHandler) {
        this.statementExecutorFactory = statementExecutorFactory;
        this.outputHandler = outputHandler;
    }

    public String process(String batchJobName){
        log.info("Event={} SubEvent=ExecuteBatchJob Status=Started", batchJobName);
        StatementExecutor statementExecutor = statementExecutorFactory.getStatementExecutor(batchJobName);
        Map<String, Object> returnValues = statementExecutor.execute(batchJobName);
        String output = outputHandler.getOutputString(returnValues);
        log.info("Event={} SubEvent=ExecuteBatchJob Status=Completed", batchJobName);
        return output;
    }


}
