package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeatureFlagWorkflowTypeAssessor implements WorkflowTypeAssessor {

    private static final String comma = ",";
    private static final List<String> readOnlyWorkflows = new ArrayList<>();
    private static final List<String> unitTestReadWorkflows = Arrays.asList("com.intuit.sbd.payroll.psp.hibernate.multitenancy.ConfigurableMultiTenantConnectionProviderTests.testReadOnlyTenant");
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public FeatureFlagWorkflowTypeAssessor() {
        log.info("Scheduling FeatureFlag WorkflowType Assessor");
        this.scheduler.scheduleAtFixedRate(this.getRunnable(), 5, 3, TimeUnit.MINUTES);
        log.info("Successfully scheduled FeatureFlag WorkflowType Assessor");
    }

    @Override
    public List<String> getAllReadWorkflows() {
        return readOnlyWorkflows;
    }

    private Runnable getRunnable() {
        return new Runnable() {
            public void run() {
                try {
                    prepareReadOnlyWorkflows();
                    log.info("Successfully loaded FeatureFlag WorkflowType Assessor. Enabled ReadOnlyWorkflows={}", readOnlyWorkflows);
                } catch (Exception var2) {
                    log.warn("Unable to refresh the FeatureFlags");
                }
            }
        };
    }

    private void prepareReadOnlyWorkflows() {
        String readOnlyDatabaseWorkflows = FeatureFlags.get().stringValue(FeatureFlags.Key.READ_ONLY_DB_WORKFLOWS, StringUtils.EMPTY);
        readOnlyWorkflows.clear();
        if (StringUtils.isNotEmpty(readOnlyDatabaseWorkflows)) {
            readOnlyWorkflows.addAll(Arrays.asList(StringUtils.stripAll(readOnlyDatabaseWorkflows.split(comma))));
            readOnlyWorkflows.addAll(getUnitTestReadWorkflows());
        }
    }

    private List<String> getUnitTestReadWorkflows() {
        return unitTestReadWorkflows;
    }
}
