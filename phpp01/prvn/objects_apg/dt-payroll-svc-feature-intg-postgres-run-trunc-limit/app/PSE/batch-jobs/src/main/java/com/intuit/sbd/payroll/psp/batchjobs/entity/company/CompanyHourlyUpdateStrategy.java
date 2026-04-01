package com.intuit.sbd.payroll.psp.batchjobs.entity.company;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.entity.UpdateStatusStrategy;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CompanyHourlyUpdateStrategy<T> implements UpdateStatusStrategy<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyHourlyUpdateStrategy.class);
    private SpcfCalendar batchStartTime;

    public CompanyHourlyUpdateStrategy(SpcfCalendar batchStartTime) {
        this.batchStartTime = batchStartTime;
    }

    @Override
    public void updateLastProcessedTime() {
        try {
            LOGGER.info("job=initial_load_evs_hourly,action=update_last_processed_time_started");
            Application.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EVS_LAST_PROCESSED_TIME, batchStartTime.toISO8601());
            Application.commitUnitOfWork();
            LOGGER.info("job=initial_load_evs_hourly,action=update_last_processed_time_completed,last_processed_time=" + batchStartTime.toISO8601());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
    @Override
    public void handlePublishChunkFailure(List<T> chunk) {
        LOGGER.info("job=initial_load_evs_hourly,action=handle_publish_chunk_failure,targetedWorkFlow=EVS_HOURLY,chunkSize={},chunk={}", chunk.size(), getPsIdsFromChunk(chunk));
    }

    @Override
    public void handlePublishChunkSuccess(List<T> chunk) {
        LOGGER.info("job=initial_load_evs_hourly,action=handle_publish_chunk_success_started,targetedWorkFlow=EVS_HOURLY,chunkSize={},chunk={}", chunk.size(), getPsIdsFromChunk(chunk));
    }
}