package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class QuickbooksInfo extends BaseQuickbooksInfo implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public QuickbooksInfo()
	{
		super();
	}

    @Override
    public void setProcessTransmissions(boolean pProcessTransmissions) {
        if(pProcessTransmissions != getProcessTransmissions()) {
            CompanyEvent.createRequestProcessingFlagChangedEvent(getCompany(), getProcessTransmissions(), pProcessTransmissions);
            if(pProcessTransmissions) {
                // find any requests in error state and reprocess them
                DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests = Application.find(QbdtUnprocessedRequest.class, QbdtUnprocessedRequest.Company().equalTo(getCompany())
                                                                                                                                                       .And(QbdtUnprocessedRequest.Status().equalTo(QbdtRequestStatus.Error)));
                if(qbdtUnprocessedRequests.size() > 0) {
                    // don't allow reprocessing within 1hr of offload
                    SpcfCalendar today = PSPDate.getPSPTime().copy();
                    CalendarUtils.clearTime(today);
                    OffloadGroup offloadGroup = getCompany().getOffloadGroup();
                    SpcfCalendar limitCalendar = offloadGroup.getCalendarForCutoffTime(today);
                    limitCalendar.addHours(-1);
                    if(PSPDate.getPSPTime().after(limitCalendar)) {
                        throw new RuntimeException("Cannot update processing flag anytime after 1hr before the company's offload time (i.e. 4:15pm if the offload is 5:15pm).");
                    }

                    for (QbdtUnprocessedRequest qbdtUnprocessedRequest : qbdtUnprocessedRequests) {
                        qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Queued);
                        Application.save(qbdtUnprocessedRequest);
                    }
                }
            }
        }

        super.setProcessTransmissions(pProcessTransmissions);
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    public void onUpdate() {
        if(getCompany() != null) {
            setToken(getCompany().getNextToken());
        }
    }

    public String getReleaseNumber() {
        return getApplicationVersionDetail("SLIPSTREAM_VERSION");
    }

    public String getVersionNumber() {
        int firstVersion = 1990;

        String quickbooksVersionString = getApplicationVersionDetail("MAJOR_VERSION");
        int quickbooksVersion = NumberUtils.toInt(quickbooksVersionString, 0);

        if(quickbooksVersion == 0) {
            return null;
        }

        return String.valueOf(firstVersion+quickbooksVersion);
    }

    private String getApplicationVersionDetail(String name) {
        Map<String, String> applicationVersionMap = getApplicationVersionDetails();
        if(MapUtils.isEmpty(applicationVersionMap)) {
            return null;
        }
        return applicationVersionMap.get(name);
    }

    private Map<String, String> getApplicationVersionDetails() {
        String applicationVersion = getApplicationVersion();
        if(StringUtils.isEmpty(applicationVersion)) {
            return null;
        }

        String[] applicationVersionDetails = applicationVersion.split("\\.");

        if(applicationVersionDetails.length != 4) {
            return null;
        }

        Map<String, String> applicationVersionMap = new HashMap<>();
        applicationVersionMap.put("MAJOR_VERSION", applicationVersionDetails[0]);
        applicationVersionMap.put("MINOR_VERSION", applicationVersionDetails[1]);
        applicationVersionMap.put("SLIPSTREAM_VERSION", applicationVersionDetails[3]);

        return applicationVersionMap;
    }
}
