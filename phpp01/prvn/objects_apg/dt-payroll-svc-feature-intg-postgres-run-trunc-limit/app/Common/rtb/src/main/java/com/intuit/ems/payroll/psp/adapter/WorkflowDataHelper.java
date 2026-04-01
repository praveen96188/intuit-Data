package com.intuit.ems.payroll.psp.adapter;

import com.intuit.ems.payroll.psp.csv.model.FileRecord;
import com.intuit.sbg.workflowfinder.model.MethodInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WorkflowDataHelper {

    private static final String CLASS_NAME = "Class";
    private static final String METHOD_NAME = "Method";

    public FileRecord getCsvData(Map<String, Set<MethodInfo>> classNameMethodInfoMap) {
        List<String> header = getHeader();
        List<List<String>> data = getData(classNameMethodInfoMap);
        return new FileRecord(header, data);
    }

    private List<List<String>> getData(Map<String, Set<MethodInfo>> classNameMethodInfoMap) {
        List<List<String>> data = new ArrayList<>();
        for(Map.Entry<String, Set<MethodInfo>> entry : classNameMethodInfoMap.entrySet()) {
            int index = 0;
            for(MethodInfo methodInfo : entry.getValue()) {
                List<String> row = new ArrayList<>();
                String className = index++ == 0? methodInfo.getClassName() : StringUtils.EMPTY;
                row.add(className);
                row.add(methodInfo.getMethodName());
                data.add(row);
            }
        }
        return data;
    }

    private List<String> getHeader() {
        List<String> headerNames = new ArrayList<>();
        headerNames.add(CLASS_NAME);
        headerNames.add(METHOD_NAME);
        return headerNames;
    }
}
