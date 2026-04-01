package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OutputHandler {

    public String getOutputString(Map<String, Object> returnValues) {

            log.info("Event=GetFinalTemplate Status=Started");
            StringBuilder stringBuilder = new StringBuilder("");
            for (Map.Entry<String, Object> output : returnValues.entrySet()) {
                if (StringUtils.isNotBlank(output.getKey()) && ObjectUtils.isNotEmpty(output.getValue())) {
                    stringBuilder.append(String.format(output.getKey(), output.getValue())).append("\n");
                }
            }
            log.info("Event=GetFinalTemplate Status=Completed");
            return stringBuilder.toString();
    }

}
