package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class NACHAFile extends BaseNACHAFile {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getNextFileIDCounter() {
        //Output: Generates value from A to Z

        int asciiA = 'A';
        // Sequence generates values from 1 to 26
        int uniqueIdCharSeq = Application.nextSequenceValue(SequenceId.SEQ_ACH_FILE_CTR, Long.class).intValue();
        return Character.toString((char)(uniqueIdCharSeq + asciiA - 1));
    }

    public static boolean ifNotEligibleForUpload(NACHAFile nachaFile){
        return (nachaFile.getOffloadBatch().getOffloadGroup().getOffloadGroupCd().equals("PSPO") || nachaFile.getOffloadBatch().getOffloadGroup().getOffloadGroupCd().equals("STD_DDS"));
    }
    
    /**
     * This method returns the NACHAFile collection for a given offload date.
     * @param pOffloadDate The offload date for which you want the associated NACHAFile records.
     * @return The collection of NACHAFile records for the given offload date.
     */
    public static DomainEntitySet<NACHAFile> getNACHAFilesForOffloadDate(SpcfCalendar pOffloadDate, NACHAFileStatus... pNachaFileStatus) {
        Expression<OffloadBatch> query =
                new Query<OffloadBatch>()
                      .Where(OffloadBatch.OffloadDate().equalTo(pOffloadDate)
                             .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed)))
                      .OrderBy(CreatedDate());

        DomainEntitySet<OffloadBatch> batchList = Application.find(OffloadBatch.class, query);

        DomainEntitySet<NACHAFile> nachaFileList = new DomainEntitySet<NACHAFile>();

        // for each batch found, get the related nacha file records
        for (OffloadBatch batch : batchList) {
            nachaFileList.addAll(batch.getNACHAFileCollection().find(NACHAFile.Status().in(pNachaFileStatus)).sort(NACHAFile.FileType()));
        }

        return nachaFileList;
    }

    public static NACHAFile getNACHAFileForOffloadGroupAndDate(SpcfCalendar pOffloadDate, OffloadGroup pOffloadGroup, NACHAFileStatus  status, NACHAFileType fileType){

        DomainEntitySet<OffloadBatch> offloadBatches= Application.find(OffloadBatch.class,OffloadBatch.OffloadGroup().equalTo(pOffloadGroup).And(OffloadBatch.OffloadDate().equalTo(pOffloadDate)));
        if(offloadBatches.isEmpty())
            return null;
        OffloadBatch offloadBatch= offloadBatches.get(0);
        DomainEntitySet<NACHAFile> nachaFiles= Application.find(NACHAFile.class,NACHAFile.FileType().equalTo(fileType).And(NACHAFile.Status().equalTo(status)).And(NACHAFile.OffloadBatch().equalTo(offloadBatch)));
        if(nachaFiles.isEmpty())
            return null;
        return nachaFiles.get(0);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public NACHAFile()
	{
		super();
	}

}