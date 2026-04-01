package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.List;
import java.util.Vector;

/**
 * Hand-written business logic
 */
public class EftpsFile extends BaseEftpsFile {



    public void setAcknowledgementFile(EdiTaxFile pAckFile) {
        setAckFile((EftpsFile)pAckFile);
        Application.save(this);
    }

    //abstract
    public void setEftpsFileSubtype(EftpsFileSubtype pEftpsFileSubtype) {
        setFileSubtype(pEftpsFileSubtype);
    }
    //
    // Static methods
    //

    /**
     * When searching by file id, there should be exactly one match since file id is (should be) unique.
     *
     * @param pFileId The file id to locate.
     * @return The EftpsFile record matching the file id.
     */
    public static EftpsFile getEftpsFileByFileId(int pFileId) {
        DomainEntitySet<EftpsFile> eftpsFileSet = Application.find(EftpsFile.class, FileId().equalTo(pFileId));

        if (eftpsFileSet.isEmpty()) {
            throw new RuntimeException(String.format("EftpsFile record could not be found for FileId %d", pFileId));
        } else if (eftpsFileSet.size() > 1) {
            throw new RuntimeException(String.format("More than one EftpsFile record was found for FileId %d", pFileId));
        }

        return eftpsFileSet.get(0);
    }

    public static DomainEntitySet<EftpsFile> getSubmittedFilesByTypeAndDate(EdiFileType pFileType, SpcfCalendar pDate) {
        //
        // Date range should be (for example date 01/20/2011): '01/20/2011 00:00:00.000' and '01/20/2011 23:59:59.999'
        //

        SpcfCalendar day = pDate.copy();
        CalendarUtils.clearTime(day);  // clear time

        SpcfCalendar nextDay = day.copy();
        nextDay.addDays(1);
        nextDay.addMilliseconds(-1);

        return Application.find(EftpsFile.class, FileType().equalTo(pFileType).And(SubmitDate().between(day, nextDay)));
    }

    public static boolean hasPaymentFileBeenSubmittedForDate(SpcfCalendar pDate) {
        return !getSubmittedFilesByTypeAndDate(EdiFileType.EftpsPayment, pDate).isEmpty();
    }

    public static EftpsFile updateErrorStatus(SpcfUniqueId pUniqueId, String pFileName) {
        EftpsFile eftpsFile = Application.findById(EftpsFile.class, pUniqueId);
        return (eftpsFile != null) ? (EftpsFile) eftpsFile.updateErrorStatus(pFileName) : null;
    }

    public static EftpsFile updateErrorStatus(int pFileId, String pFileName) {
        return (EftpsFile) getEftpsFileByFileId(pFileId).updateErrorStatus(pFileName);
    }

    public static DomainEntitySet<EftpsFile> getEftpsFilesByTypeAndStatus(EdiFileType pFileType,
                                                                          EdiFileStatus... pStatus) {
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(FileType().equalTo(pFileType)
                        .And(StatusCd().in(pStatus)))
                .OrderBy(CreatedDate());
        return Application.find(EftpsFile.class, query);
    }

    public static DomainEntitySet<EftpsFile> getEftpsFilesByStatus(EdiFileStatus... pStatus) {
        Expression<EftpsFile> query = new Query<EftpsFile>().Where(StatusCd().in(pStatus)).OrderBy(CreatedDate());
        return Application.find(EftpsFile.class, query);
    }

    public static DomainEntitySet<EftpsFile> getArchivedEftpsFiles() {
        return getEftpsFilesByStatus(EdiFileStatus.Archived);
    }

    public static DomainEntitySet<EftpsFile> getCompletedEftpsFiles() {
        return getEftpsFilesByStatus(EdiFileStatus.Completed);
    }

    public static DomainEntitySet<EftpsFile> getPendingTransmissionEftpsFiles() {
        return getEftpsFilesByStatus(EdiFileStatus.PendingTransmission);
    }

    public static DomainEntitySet<EftpsFile> getSendToAS400EftpsFiles() {
        return getEftpsFilesByStatus(EdiFileStatus.SendToAS400);
    }

    //
    // Instance methods
    //

	/**
	 * Default constructor.
	 */
	public EftpsFile()
	{
		super();
	}

    public boolean canDeleteAckFileRecord() {
        return Application.find(EftpsFile.class, AckFile().equalTo(this)).isEmpty();
    }

    public boolean canDeleteEnrollmentResponseRecord(EftpsFile pExemptParentFile) {
        Criterion<EftpsEnrollmentDetail> where = EftpsEnrollmentDetail.ResponseFile().equalTo(this);

        if (pExemptParentFile != null) {
            where = where.And(EftpsEnrollmentDetail.ParentFile().notEqualTo(pExemptParentFile));
        }

        return Application.find(EftpsEnrollmentDetail.class, where).isEmpty();
    }

    public boolean canDeletePaymentResponseRecord(EftpsFile pExemptParentFile) {
        Criterion<EftpsPaymentDetail> where = EftpsPaymentDetail.ResponseFile().equalTo(this);

        if (pExemptParentFile != null) {
            where = where.And(EftpsPaymentDetail.ParentFile().notEqualTo(pExemptParentFile));
        }

        return Application.find(EftpsPaymentDetail.class, where).isEmpty();
    }

    public boolean canDeletePaymentReturnRecord(EftpsFile pExemptParentFile) {
        Criterion<EftpsPaymentDetail> where = EftpsPaymentDetail.ReturnFile().equalTo(this);

        if (pExemptParentFile != null) {
            where = where.And(EftpsPaymentDetail.ParentFile().notEqualTo(pExemptParentFile));
        }

        return Application.find(EftpsPaymentDetail.class, where).isEmpty();
    }

    public void deleteEnrollmentDetails() {
        List<EftpsFile> deleteList = new Vector<EftpsFile>(); // EftpsFiles we know we can delete
        List<EftpsFile> skipList = new Vector<EftpsFile>(); // EftpsFiles we know we cannot delete
        DomainEntitySet<EftpsEnrollmentDetail> detailSet =
                Application.find(EftpsEnrollmentDetail.class, EftpsEnrollmentDetail.ParentFile().equalTo(this));

        for (EftpsEnrollmentDetail detail : detailSet) {
            //
            // Check to see if we need to delete the response file record
            //
            EftpsFile responseFile = detail.getResponseFile();
            if ((responseFile != null) && !deleteList.contains(responseFile) && !skipList.contains(responseFile)) {
                if (responseFile.canDeleteEnrollmentResponseRecord(this)) {
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

        // Finally, delete all affected EftpsFiles that are no longer associated to anything
        for (EftpsFile eftpsFile : deleteList) {
            eftpsFile.cascadeDelete();
        }
    }

    public void deletePaymentDetails() {
        List<EftpsFile> deleteList = new Vector<EftpsFile>(); // EftpsFiles we know we can delete
        List<EftpsFile> skipList = new Vector<EftpsFile>(); // EftpsFiles we know we cannot delete
        DomainEntitySet<EftpsPaymentDetail> detailSet =
                Application.find(EftpsPaymentDetail.class, EftpsPaymentDetail.ParentFile().equalTo(this));

        for (EftpsPaymentDetail detail : detailSet) {
            //
            // Check to see if we need to delete the response file record
            //
            EftpsFile responseFile = detail.getResponseFile();
            if ((responseFile != null) && !deleteList.contains(responseFile) && !skipList.contains(responseFile)) {
                if (responseFile.canDeletePaymentResponseRecord(this)) {
                    deleteList.add(responseFile);
                } else {
                    skipList.add(responseFile);
                }
            }

            //
            // Check to see if we need to delete the return file record
            //
            EftpsFile returnFile = detail.getReturnFile();
            if ((returnFile != null) && !deleteList.contains(returnFile) && !skipList.contains(returnFile)) {
                if (returnFile.canDeletePaymentReturnRecord(this)) {
                    deleteList.add(returnFile);
                } else {
                    skipList.add(returnFile);
                }
            }

            //
            // Delete the detail
            //
            Application.delete(detail);
        }

        // Finally, delete all affected EftpsFiles that are no longer associated to anything
        for (EftpsFile eftpsFile : deleteList) {
            eftpsFile.cascadeDelete();
        }
    }

    public void cascadeDelete() {
        switch (getFileType()) {
            case EftpsPayment:
                deletePaymentDetails();
                break;
            case EftpsPaymentResponse:
                if (!canDeletePaymentResponseRecord(null)) {
                    throw new RuntimeException("Cannot delete, record in use by EftpsPaymentDetail record(s).");
                }
                break;
            case EftpsPaymentReturn:
                if (!canDeletePaymentReturnRecord(null)) {
                    throw new RuntimeException("Cannot delete, record in use by EftpsPaymentDetail record(s).");
                }
                break;
            case EftpsEnrollment:
                deleteEnrollmentDetails();
                break;
            case EftpsEnrollmentResponse:
                if (!canDeleteEnrollmentResponseRecord(null)) {
                    throw new RuntimeException("Cannot delete, record in use by EftpsEnrollmentDetail record(s).");
                }
                break;
            case EftpsForecast:
                // support TBD
                break;
            case EftpsPaymentConfirmation:
                // no extra checks to make
                break;
            case EftpsEnrollmentAck:
            case EftpsEnrollmentResponseAck:
            case EftpsForecastAck:
            case EftpsPaymentAck:
            case EftpsPaymentConfirmationAck:
            case EftpsPaymentResponseAck:
            case EftpsPaymentReturnAck:
                if (!canDeleteAckFileRecord()) {
                    throw new RuntimeException("Cannot delete, record in use by EftpsFile record(s).");
                }
                break;
        }

        if (getAckFile() != null) {
            Application.delete(getAckFile());
        }

        Application.delete(this);
    }

    public boolean isAS400File(){
        if(getSystemOwner().equals(SystemOwnerType.AS400)) {
            return true;
        }
        return false;
    }
}
