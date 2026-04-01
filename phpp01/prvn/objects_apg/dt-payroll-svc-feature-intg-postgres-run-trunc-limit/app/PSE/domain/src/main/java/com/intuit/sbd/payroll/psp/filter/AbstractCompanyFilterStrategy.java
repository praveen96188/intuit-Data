package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCompanyFilterStrategy<U, V> implements CompanyFilterStrategy<U, V> {

    private PSPRequestContextManager pspRequestContextManager;

    protected SpcfUniqueId getCompanySequence() {
        RequestContext requestContext = getPSPRequestContextManager().getRequestContext();
        if(Objects.isNull(requestContext) || Objects.isNull(requestContext.getCompanyInfo()) || Objects.isNull(requestContext.getCompanyInfo().getCompanySequence())) {
            return null;
        }

        return requestContext.getCompanyInfo().getCompanySequence();
    }

    protected PSPRequestContextManager getPSPRequestContextManager(){
        if(Objects.isNull(pspRequestContextManager)){
            pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
        }
        return pspRequestContextManager;
    }

    protected boolean isFilterRequired(){
        RequestContext requestContext = getPSPRequestContextManager().getRequestContext();
        if(Objects.isNull(requestContext) || Objects.isNull(requestContext.getCompanyInfo()) || Objects.isNull(requestContext.getCompanyInfo().getCompanySequence())) {
            return false;
        }

        //TODO: Convert Request Type to ENUM
        /*
        Possible Values for Request Type: SOAP, OFX, PAPI, GRAPHQL, SAP
         */
        RequestType requestType = requestContext.getRequestType();
        /*
        Request Operation contains the Operation Name for SOAP, SAP.
        For PAPI, it contains the endpoint URL.
        For OFX, it is QBDT.
        For GRAPHQL, it is Workforce
         */
        String requestOperation = requestContext.getRequestOperation();

        return isFilterAllowed(requestType, requestOperation);
    }


    protected boolean isFilterAllowed(RequestType requestType, String requestOperation) {
        boolean hibernateFilterEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_HIBERNATE_COMPANY_FILTER, false);
        if(!hibernateFilterEnabled)
            return false;

        String allowedTypes = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.FILTER_ENABLED_REQUEST_TYPES);
        String allowedOperations = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.FILTER_ENABLED_REQUEST_OPERATIONS);
        String disabledOperations = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.FILTER_DISABLED_REQUEST_OPERATIONS);

        List<String> disabledOperationsList = new ArrayList<>();
        disabledOperationsList.addAll(Arrays.asList(StringUtils.stripAll(disabledOperations.split(","))));

        List<String> allowedTypesList = new ArrayList<>();
        allowedTypesList.addAll(Arrays.asList(StringUtils.stripAll(allowedTypes.split(","))));

        List<String> allowedOperationsList = new ArrayList<>();
        allowedOperationsList.addAll(Arrays.asList(StringUtils.stripAll(allowedOperations.split(","))));

        if(disabledOperationsList.contains(requestOperation)) {
            return false;
        }

        if(allowedTypesList.contains(requestType.toString())) {
            return true;
        }

        if(allowedOperationsList.contains(requestOperation)) {
            return true;
        }

        return false;
    }



}
