package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.factory;

import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.StatementType;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.executor.StatementExecutor;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.mapper.PlSqlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class StatementExecutorFactory {

    @Autowired
    private PlSqlMapper plSqlMapper;

    private Map<StatementType, StatementExecutor> parserMap;

    @Autowired
    public StatementExecutorFactory(List<StatementExecutor> statementExecutorList) {
        parserMap = statementExecutorList.stream().collect(Collectors.toMap(StatementExecutor::getType, Function.identity()));
    }

    public StatementExecutor getStatementExecutor(String batchJobName) {

        return parserMap.get(getStatementTypeFromBatchJob(batchJobName));
    }

    private StatementType getStatementTypeFromBatchJob(String batchJobName) {

        return plSqlMapper.plsqlStatementTypeMap.get(batchJobName);

    }


}
