package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.executor;

import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.StatementType;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.service.PlSqlExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class NamedQueryExecutor implements StatementExecutor {

    @Autowired
    private PlSqlExecutorService plSqlExecutorService;

    @Override
    public StatementType getType() {
        return StatementType.NamedQuery;
    }

    @Override
    public Map<String, Object> execute(String batchJobName) {

        log.info("Event={} Executor=NamedQueryExecutor", batchJobName);
        return plSqlExecutorService.executeNamedQuery(batchJobName);
    }
}
