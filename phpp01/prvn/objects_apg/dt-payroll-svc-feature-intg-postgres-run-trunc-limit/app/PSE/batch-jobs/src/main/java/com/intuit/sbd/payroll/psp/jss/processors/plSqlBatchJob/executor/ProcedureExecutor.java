package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.executor;

import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.StatementType;

import java.util.Map;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.service.PlSqlExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcedureExecutor implements StatementExecutor {

    @Autowired
    private PlSqlExecutorService plSqlExecutorService;

    @Override
    public StatementType getType() {
        return StatementType.Procedure;
    }

    @Override
    public Map<String, Object> execute(String batchJobName) {

        log.info("Event={} Executor=ProcedureExecutor", batchJobName);
        return plSqlExecutorService.executeStoredProcedure(batchJobName);
    }
}
