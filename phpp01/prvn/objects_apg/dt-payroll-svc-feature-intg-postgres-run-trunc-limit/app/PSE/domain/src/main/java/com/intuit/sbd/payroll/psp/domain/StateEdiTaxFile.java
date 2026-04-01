package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import java.util.List;
import java.util.Vector;

/**
 * Hand-written business logic
 */
public class StateEdiTaxFile extends BaseStateEdiTaxFile {

	/**
	 * Default constructor.
	 */
	public StateEdiTaxFile()
	{
		super();
	}



    public void setAcknowledgementFile(EdiTaxFile pAckFile) {
        setAckFile((StateEdiTaxFile)pAckFile);
        Application.save(this);
    }

    public static StateEdiTaxFile getEdiFileByFileId(int pFileId) {
        DomainEntitySet<StateEdiTaxFile> ediFileSet = Application.find(StateEdiTaxFile.class, FileId().equalTo(pFileId));

        if (ediFileSet.isEmpty()) {
            return null;
        }
        if (ediFileSet.size() > 1) {
            throw new RuntimeException(String.format("More than one StateEdiTaxFile record was found for FileId %d", pFileId));
        }

        return ediFileSet.get(0);
    }

    public boolean canDeletePaymentResponseRecord(StateEdiTaxFile pExemptParentFile) {
        Criterion<EdiPaymentDetail> where = EdiPaymentDetail.ResponseFile().equalTo(this);

        if (pExemptParentFile != null) {
            where = where.And(EdiPaymentDetail.ParentFile().notEqualTo(pExemptParentFile));
        }

        return Application.find(EdiPaymentDetail.class, where).isEmpty();
    }

    public void deletePaymentDetails() {
        List<StateEdiTaxFile> deleteList = new Vector<StateEdiTaxFile>(); // StateEdiTaxFile we know we can delete
        List<StateEdiTaxFile> skipList = new Vector<StateEdiTaxFile>(); // StateEdiTaxFile we know we cannot delete
        DomainEntitySet<EdiPaymentDetail> detailSet =
                Application.find(EdiPaymentDetail.class, EdiPaymentDetail.ParentFile().equalTo(this));

        for (EdiPaymentDetail detail : detailSet) {
            //
            // Check to see if we need to delete the response file record
            //
            StateEdiTaxFile responseFile = detail.getResponseFile();
            if ((responseFile != null) && !deleteList.contains(responseFile) && !skipList.contains(responseFile)) {
                if (responseFile.canDeletePaymentResponseRecord(this)) {
                    deleteList.add(responseFile);
                } else {
                    skipList.add(responseFile);
                }
            }

            //
            // Delete the detail
            //
            Application.delete(detail);
        }

        // Finally, delete all affected StateEdiTaxFiles that are no longer associated to anything
        for (StateEdiTaxFile stateEdiTaxFile : deleteList) {
            stateEdiTaxFile.cascadeDelete();
        }
    }

    public boolean canDeleteAckFileRecord() {
        return Application.find(StateEdiTaxFile.class, AckFile().equalTo(this)).isEmpty();
    }

    public void cascadeDelete() {
        switch (getFileType()) {
            case StateEdiPayment:
                deletePaymentDetails();
                break;
            case StateEdiPaymentResponse:
                if (!canDeletePaymentResponseRecord(null)) {
                    throw new RuntimeException("Cannot delete, record in use by EdiPaymentDetail record(s).");
                }
                break;
            case StateEdiPaymentAck:
                if (!canDeleteAckFileRecord()) {
                    throw new RuntimeException("Cannot delete, record in use by StateEdiTaxFile record(s).");
                }
                break;
        }

        if (getAckFile() != null) {
            Application.delete(getAckFile());
        }

        Application.delete(this);
    }

    /**
     * When searching by file id, there should be exactly one match since file id is (should be) unique.
     *
     * @param pFileId The file id to locate.
     * @return The StateEdiTaxFile record matching the file id.
     */
    public static StateEdiTaxFile getStateEdiTaxFileByFileId(int pFileId) {
        DomainEntitySet<StateEdiTaxFile> stateEdiFileSet = Application.find(StateEdiTaxFile.class, FileId().equalTo(pFileId));

        if (stateEdiFileSet.isEmpty()) {
            return null;
        } else if (stateEdiFileSet.size() > 1) {
            throw new RuntimeException(String.format("More than one StateEdiTaxFile record was found for FileId %d", pFileId));
        }

        return stateEdiFileSet.get(0);
    }

    public static DomainEntitySet<StateEdiTaxFile> getCompletedStateEdiFiles() {
        return getStateEdiFilesByStatus(EdiFileStatus.Completed);
    }

    public static DomainEntitySet<StateEdiTaxFile> getPendingTransmissionStateEdiFiles() {
        return getStateEdiFilesByStatus(EdiFileStatus.PendingTransmission);
    }

    public static DomainEntitySet<StateEdiTaxFile> getSendToAS400StateEdiFiles() {
        return getStateEdiFilesByStatus(EdiFileStatus.SendToAS400);
    }

    public static DomainEntitySet<StateEdiTaxFile> getStateEdiFilesByStatus(EdiFileStatus... pStatus) {
        Expression<StateEdiTaxFile> query = new Query<StateEdiTaxFile>().Where(StatusCd().in(pStatus)).OrderBy(CreatedDate());
        return Application.find(StateEdiTaxFile.class, query);
    }    

}