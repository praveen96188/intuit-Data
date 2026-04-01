package com.intuit.sbd.payroll.psp.filter.range;

import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.filter.CommonFilterStrategy;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Objects;

public abstract class AbstractDateFilterStrategy<U, V> implements CommonFilterStrategy<U, V> {

    protected SpcfCalendar getCreatedDate() {
        return RequestAttributesUtils.getAttribute(CommonConstants.CREATED_DATE_ATTRIBUTE, SpcfCalendar.class);
    }

    protected boolean isDateFilterRequired(){
        boolean hibernateFilterEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_HIBERNATE_DATE_FILTER, false);
        if(!hibernateFilterEnabled) {
            return false;
        }
        if(Objects.isNull(getCreatedDate())) {
            return false;
        }
        return true;
    }

}
