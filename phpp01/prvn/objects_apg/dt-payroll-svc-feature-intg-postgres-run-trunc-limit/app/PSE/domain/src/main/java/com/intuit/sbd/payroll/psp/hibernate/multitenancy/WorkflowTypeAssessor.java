package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

import com.intuit.sbd.payroll.psp.context.constants.MultiTenantContextConstants;
import com.intuit.sbg.psp.dbtelemetry.utils.StackTraceUtils;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public interface WorkflowTypeAssessor {

    Logger log = LoggerFactory.getLogger(WorkflowTypeAssessor.class);

    default boolean isReadOnly(boolean readOnly) {
        String stackTrace = StackTraceUtils.getStackTrace(new Throwable(), null, -1);
        return isReadOnly(readOnly, stackTrace);
    }

    default boolean isReadOnly(boolean readOnly, String stackTrace) {
        return readOnly && isReadOnlyWorkflow(stackTrace);
    }

    default boolean isReadOnlyWorkflow(String stackTrace) {
        if (Objects.isNull(stackTrace)) {
            return false;
        }

        if(checkIfNeedToBeServedExplicitlyFromReadWrite()) {
            log.info("Explicit fallback to READ_WRITE requested");
            return false;
        }
        for (String allowedWorkflow : getAllReadWorkflows()) {
            if (StringUtils.contains(stackTrace, allowedWorkflow)) {
                //TODO Remove the log before going live in production
                log.info("Tenant=Read, WorkflowName={}", allowedWorkflow);
                return true;
            }
        }
        return false;
    }

    default boolean checkIfNeedToBeServedExplicitlyFromReadWrite() {
        //finally we will have a single unique flag - ExplicitServeFromReadWriteOnly but for the timebeing keeping it at workforce level
        if(Boolean.TRUE.equals(RequestAttributesUtils.getAttribute(MultiTenantContextConstants.WORKFORCE_EXPLICIT_SERVE_FROM_READ_WRITE, Boolean.class))) {
            return true;
        }
        return false;
    }



    default boolean isReadWrite() {
        String stackTrace = StackTraceUtils.getStackTrace(new Throwable(), null, -1);
        return isReadWrite(stackTrace);
    }

    default boolean isReadWrite(String stackTrace) {
        return !isReadOnly(false, stackTrace);
    }

    List<String> getAllReadWorkflows();
}
