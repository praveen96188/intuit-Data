package com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess;

import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.WorkflowFinderConstants;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowDigestHelper {
    public Map<Integer, String> preProcess(List<String> lines) {
        Map<Integer, String> digest = new HashMap<>();
        String defaultClassName = StringUtils.EMPTY;
        for(String line : lines) {
            String[] classAndMethodName = line.split(String.valueOf(WorkflowFinderConstants.CSV_COLUMN_SEPARATOR));
            if(StringUtils.isNotBlank(classAndMethodName[0])) {
                defaultClassName = classAndMethodName[0].trim();
            }
            line = defaultClassName + WorkflowFinderConstants.CLASS_METHOD_SEPARATOR + classAndMethodName[1].trim();
            digest.put(line.hashCode(), line);
        }
        return digest;
    }
}
