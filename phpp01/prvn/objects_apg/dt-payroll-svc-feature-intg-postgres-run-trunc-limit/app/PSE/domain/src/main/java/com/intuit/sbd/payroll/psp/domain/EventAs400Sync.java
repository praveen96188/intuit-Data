package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class EventAs400Sync extends BaseEventAs400Sync {

	/**
	 * Default constructor.
	 */
	public EventAs400Sync()
	{
		super();
	}

    public static DomainEntitySet<EventAs400Sync> findEventSyncsToProcess(int maxBatch, int maxRetries) {
        return Application.find(EventAs400Sync.class,
                new Query<EventAs400Sync>()
                    .Where(EventAs400Sync.StatusCd().equalTo(SyncStatus.Pending)
                        .Or(EventAs400Sync.StatusCd().equalTo(SyncStatus.Error).And(EventAs400Sync.RetryCount().lessThan(maxRetries))))
                            .OrderBy(EventAs400Sync.CreatedDate())
                            .EagerLoad(
                                EventAs400Sync.CompanyEvent(),
                                EventAs400Sync.CompanyEvent().Company())
                            .LimitResults(0, maxBatch));

    }



}