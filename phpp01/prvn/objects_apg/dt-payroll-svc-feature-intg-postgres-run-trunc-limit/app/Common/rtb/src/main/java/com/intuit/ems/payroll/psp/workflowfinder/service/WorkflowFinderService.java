package com.intuit.ems.payroll.psp.workflowfinder.service;

import com.intuit.ems.payroll.psp.adapter.WorkflowDataHelper;
import com.intuit.ems.payroll.psp.csv.CSVFileWriter;
import com.intuit.ems.payroll.psp.csv.model.CSVFile;
import com.intuit.ems.payroll.psp.csv.model.FileRecord;
import com.intuit.ems.payroll.psp.workflowfinder.model.WorkflowModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbg.workflowfinder.model.MethodInfo;
import com.intuit.sbg.workflowfinder.service.MethodMetadataService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

//TODO: Every child class can be made as config.

@Component
public class WorkflowFinderService {

    private WorkflowDataHelper workflowDataHelper;
    private MethodMetadataService methodMetadataService;
    private static final SpcfLogger LOGGER = Application.getLogger(WorkflowFinderService.class);

    public WorkflowFinderService(WorkflowDataHelper workflowDataHelper,
                                 MethodMetadataService methodMetadataService) {
        this.workflowDataHelper = workflowDataHelper;
        this.methodMetadataService = methodMetadataService;
    }

    public void generateReport(List<WorkflowModel> workflowModelList) {
        for (WorkflowModel workflowModel : workflowModelList) {
            try {
                Map<String, Set<MethodInfo>> classNameMethodInfoMap = getMetaData(workflowModel);
                FileRecord fileRecord = workflowDataHelper.getCsvData(classNameMethodInfoMap);
                generateModuleReport(workflowModel.getOutputFile(), fileRecord);
            } catch (IOException e) {
                LOGGER.warn("Unable to generate report. Workflow:" + workflowModel);
            }
        }
    }

    private Map<String, Set<MethodInfo>> getMetaData(WorkflowModel workflowModel) {
        //TODO : Add more logic to scan other classes on demand basis.
        Map<String, Set<MethodInfo>> classNameMethodInfoMap;
        if (workflowModel.getPackageName() != null) {
            if(CollectionUtils.isNotEmpty(workflowModel.getMethodAnnotation())) {
                classNameMethodInfoMap = methodMetadataService.findByPackagesAndMethodAnnotation(workflowModel.getPackageName(), getAnnotationSet(workflowModel.getMethodAnnotation()));
            } else {
                classNameMethodInfoMap = methodMetadataService.findByPackagesAndClassAnnotation(workflowModel.getPackageName(), getAnnotationSet(workflowModel.getClassAnnotation()));
            }
        } else {
            if(CollectionUtils.isNotEmpty(workflowModel.getMethodAnnotation())) {
                classNameMethodInfoMap = methodMetadataService.findByMethodAnnotation(getAnnotationSet(workflowModel.getMethodAnnotation()));
            } else {
                classNameMethodInfoMap = methodMetadataService.findByClassAnnotation(getAnnotationSet(workflowModel.getClassAnnotation()));
            }
        }
        return classNameMethodInfoMap;
    }

    private void generateModuleReport(String fileName, FileRecord fileRecord) throws IOException {
        CSVFile csvFile = new CSVFile(fileName, fileRecord);
        CSVFileWriter csvFileWriter = new CSVFileWriter(csvFile);
        csvFileWriter.write();
    }

    private Set<Class> getAnnotationSet(List<String> classNameSet) {
        Set<Class> classSet = new HashSet<Class>();
        for(String className : classNameSet) {
            try {
                classSet.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Classes Missing. Classname=" + className, e);
            }
        }
        return classSet;
    }
}
