package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class StaticWorkflowTypeAssessor implements WorkflowTypeAssessor {

    private static final List<String> allReadOnlyWorkflows = new ArrayList<>();

    static {
        initAllReadWorkflows();
    }

    private static void initAllReadWorkflows() {
        allReadOnlyWorkflows.addAll(getFunctionalReadWorkflows());
        allReadOnlyWorkflows.addAll(getUnitTestReadWorkflows());
    }

    private static List<String> getFunctionalReadWorkflows() {
        List<String> allowedFunctionalWorkflows = new ArrayList<>();
        allowedFunctionalWorkflows.add("com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdministrationAdapter.getAllSystemParameters");
        allowedFunctionalWorkflows.add("com.intuit.sbd.payroll.psp.MainClass.main");
        return allowedFunctionalWorkflows;
    }

    private static List<String> getUnitTestReadWorkflows() {
        return Arrays.asList("com.intuit.sbd.payroll.psp.hibernate.multitenancy.ConfigurableMultiTenantConnectionProviderTests.testReadOnlyTenant");
    }

    //TODO: Configure the workflows dynamically
    public List<String> getAllReadWorkflows() {
        return allReadOnlyWorkflows;
    }
}
