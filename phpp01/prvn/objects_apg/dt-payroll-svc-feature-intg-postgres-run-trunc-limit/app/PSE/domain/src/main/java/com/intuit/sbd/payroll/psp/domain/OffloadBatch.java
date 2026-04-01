package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class OffloadBatch extends BaseOffloadBatch {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @return
     */
    public static DomainEntitySet<OffloadBatch> getOffloadBatchesForOffloadEventCreation() {
        Expression<OffloadBatch> query =
                new Query<OffloadBatch>()
                       .Where(IsOffloadedTransactionsEventCreationComplete().equalTo(false)
                              .And(StatusCd().equalTo(OffloadBatchStatus.Completed)))
                       .OrderBy(CreatedDate());

        return Application.find(OffloadBatch.class, query);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public OffloadBatch()
	{
		super();
	}

    public static final String DATE_FORMAT = "yyyyMMdd";

    // PSRV001272 - Optimizing ach offload
    // We added a foreign key in the NACHAFile table (pointing to the OffloadBatch table)
    // Note: For reference, this replaces the old named query 'findNACHAFilesForOffloadBatch'
    /**
     * This method returns the NACHAFile collection for a given offload batch.
     * @return The collection of NACHAFile records for the given offload batch.
     */
    public DomainEntitySet<NACHAFile> getNACHAFilesForOffloadBatch(NACHAFileStatus nachaFileStatus) {
        return getNACHAFileCollection().find(NACHAFile.Status().equalTo(nachaFileStatus)).sort(NACHAFile.FileType());
    }


    public static String findPendingOffloadBatch(SpcfCalendar offloadDate, String offloadGroupCd, Boolean disregardCutoffTime) {
        OffloadGroup offloadGroup = Application.find(OffloadGroup.class, OffloadGroup.OffloadGroupCd().equalTo(offloadGroupCd)).get(0);

        DomainEntitySet<OffloadBatch> offloadBatches =
                Application.find(OffloadBatch.class,
                        new Query<OffloadBatch>()
                              .Where(OffloadBatch.OffloadGroup().equalTo(offloadGroup)
                                     .And(OffloadBatch.OffloadDate().equalTo(offloadDate))
                                     .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.InProcess)))
                              .OrderBy(OffloadBatch.CreatedDate()));

        // Look for pre-populated batch for the given date, offload group, and status if one of the conditions below is true:
        //    a. we are disregarding the cutoff time
        //    b. we are before the cutoff time for the first offload
        //    c. we are running with the CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY parameter set to true
        SpcfCalendar cutoffTime = offloadGroup.getCalendarForCutoffTime(offloadDate);  // Does not consider second offload
        if (disregardCutoffTime || PSPDate.getPSPTime().before(cutoffTime) || SystemParameter.findBooleanValue(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY)) {
            DomainEntitySet<OffloadBatch> offloadBatchesFiltered = offloadBatches.find(OffloadBatch.<OffloadBatch>CreatorId().equalTo("System"));
            if (offloadBatchesFiltered.size() > 0) {
                return offloadBatchesFiltered.get(0).getId().toString();
            }
        }

        // We always look for a custom (non pre-populated) batch if we haven't found a pre-populated one
        DomainEntitySet<OffloadBatch> offloadBatchesFiltered = offloadBatches.find(OffloadBatch.<OffloadBatch>CreatorId().notEqualTo("System"));
        if (offloadBatchesFiltered.size() > 0) return offloadBatchesFiltered.get(0).getId().toString();

        // No offload batch found
        return null;
    }

    public static List<String>  findPendingOffloadBatchesForOffloadGroups(List<String> offloadGroupCds,SpcfCalendar offloadDate,Boolean disregardCutoffTime){
        List<String> offloadBtachIds= new ArrayList<>();
        for(String code: offloadGroupCds){
            String offloadBatchId = findPendingOffloadBatch(offloadDate,code, disregardCutoffTime);
            if(offloadBatchId!=null)
                offloadBtachIds.add(offloadBatchId);
        }
        return offloadBtachIds;
    }

    public static OffloadBatch findPendingOffloadBatch(OffloadGroup offloadGroup, SpcfCalendar initiationDate) {
        String offloadBatchId = findPendingOffloadBatch(initiationDate, offloadGroup.getOffloadGroupCd(), false);
        if (offloadBatchId == null) {
            return null;
        }
        else {
            return Application.findById(OffloadBatch.class,  SpcfUniqueId.createInstance(offloadBatchId));
        }
    }

    @Override
    public String toString() {
        return "offload group: " + getOffloadGroup().getOffloadGroupCd().toString() + " offload date: " + getOffloadDate().toString();
    }

    public static SpcfLevel getOffloadBatchChangeLogLevel() {
        if (SystemParameter.findBooleanValue(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY)) {
            return SpcfLevel.Warn;
        } else {
            return SpcfLevel.Error;
        }
    }

    public static String getOffloadBatchIdForOffloadGroup(SpcfCalendar pNormalizedOffloadDate, String offloadGroupCd, OffloadBatchStatus status) {
        OffloadGroup offloadGroup =  Application.find(OffloadGroup.class, OffloadGroup.OffloadGroupCd().equalTo(offloadGroupCd)).get(0);

        // get the most recent offload batch in a Completed state
        Expression<OffloadBatch> query = new Query<OffloadBatch>()
                .Where(OffloadBatch.OffloadDate().equalTo(pNormalizedOffloadDate)
                        .And(OffloadBatch.OffloadGroup().equalTo(offloadGroup))
                        .And(OffloadBatch.StatusCd().equalTo(status))
                        .And(OffloadBatch.IsOffloadedTransactionsEventCreationComplete().equalTo(false)))
                .OrderBy(OffloadBatch.CreatedDate().Descending()); // sort descending

        // get offload batches for the given offload date in descending order
        DomainEntitySet<OffloadBatch> batchSet = Application.find(OffloadBatch.class, query);

        if (batchSet.isEmpty()) {
            throw new RuntimeException("Unable to locate offload batch for offload date: " +
                    pNormalizedOffloadDate.format(DATE_FORMAT));
        }

        return batchSet.get(0).getId().toString();
    }
}