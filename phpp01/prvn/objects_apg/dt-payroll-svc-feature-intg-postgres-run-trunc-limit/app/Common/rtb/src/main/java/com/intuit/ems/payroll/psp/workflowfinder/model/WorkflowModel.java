package com.intuit.ems.payroll.psp.workflowfinder.model;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowModel {
	
    private String outputFile;
    private String packageName;
    private List<String> classAnnotation;
    private List<String> methodAnnotation;
}
