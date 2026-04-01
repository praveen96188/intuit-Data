/**
 * EnrollmentResponseFile.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.*;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * The EnrollmentResponseFile class represents the business-specific EFTPS
 * file with enrollment specifics added
 */
public class EnrollmentResponseFile extends EftpsEDIFile implements RecordListener {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(EnrollmentResponseFile.class);

    protected EDIRecordTemplate mBgnRecordTemplate;
    protected EDIRecordTemplate mRefRecordTemplate;
    protected EDIRecordTemplate mOtiRecordTemplate;
    protected int mAcceptCount;
    protected int mRejectCount;
    protected int mRecordCount;
    protected int mTransactionSetId;
    protected int mTransactionId;
    protected String mResponseCode;
    protected AckFile mAckFile = null;

    public EnrollmentResponseFile() {
        mBgnRecordTemplate = getRecordTemplate(RecordId.EDI_824_SEG_BGN);
        mOtiRecordTemplate = getRecordTemplate(RecordId.EDI_824_SEG_OTI);
        mRefRecordTemplate = getRecordTemplate(RecordId.EDI_824_SEG_REF);

        addReadRecordListener(this);
    }

    public int getAcceptCount() {
        return mAcceptCount;
    }

    public int getRejectCount() {
        return mRejectCount;
    }

    public int getRecordCount() {
        return mRecordCount;
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI824;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.EftpsEnrollmentResponse;
    }

    public AckFile getAckFile() {
        return mAckFile;
    }

    @Override
    protected int readContent() {
        int count = 0;

        mAcceptCount = 0;
        mRejectCount = 0;
        mRecordCount = 0;

        // read the ISA header record
        readRecord(getISAHeaderTemplate());

        // read the GS header record
        readRecord(getGSHeaderTemplate());

        mAckFile = new AckFile(getEdiFileType(), getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06));

        while (true) {
            String stSeg = peekNextSegmentCode();

            if ((stSeg == null) || (stSeg.startsWith("GE"))) {
                // There are no more transaction sets in the file.
                break;
            }

            if (!stSeg.startsWith("ST")) {
                throw new EftpsBpRuntimeException("Expecting ST segment, found: " + stSeg);
            }

            // read the ST header record
            readRecord(getSTHeaderTemplate());

            // read the BGN record
            readRecord(mBgnRecordTemplate);

            while (true) {
                String otiSeg = peekNextSegmentCode();

                if (otiSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Enrollment Response file. Expected OTI or SE segment, found EOF.");
                }

                if (otiSeg.startsWith("SE")) {
                    // read the SE trailer record (also writes the ack info in notify handler)
                    readRecord(getSETrailerTemplate());
                    break;
                }

                // read the OTI record
                count += readRecord(mOtiRecordTemplate);

                // read the REF record
                count += readRecord(mRefRecordTemplate);
            }
        }

        // read the GE trailer record
        readRecord(getGETrailerTemplate());

        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());

        // finally, write the Ack file
        mAckFile.write();

        // set the parent of the Ack EdiFileRecord to this Response file's EdiFileRecord
        getEdiFileRecord().setAckFile(mAckFile.getEdiFileRecord().getEftpsFile());

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();

        return count;
    }

    /**
     * Process the records as they are created.
     * If the enrollment is successful then save the enrollment id to the company.
     * If the enrollment was rejected, then update the company with the reject reason.
     */
    public void recordCreated(RecordTemplate template) {
        if (template == mBgnRecordTemplate) {
            mTransactionSetId = template.getFieldInt(FieldId.EDI_824_SEG_BGN06);
        } else if (template == mOtiRecordTemplate) {
            mResponseCode = template.getFieldValue(FieldId.EDI_824_SEG_OTI01);
            mTransactionId = template.getFieldInt(FieldId.EDI_824_SEG_OTI03);
        } else if (template == mRefRecordTemplate) {
            updateEnrollmentStatus();
        } else if (template == getSETrailerTemplate()) {
            // since we're being notified about the SE record, we know the ST record has already been read.
            mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
        }
    }

    private void updateEnrollmentStatus() {
        logger.info(String.format("Looking up EftpsEnrollmentDetail for Enrollment Response transaction " +
                                  "(TransactionSetId: %d, TransactionId: %d)", mTransactionSetId, mTransactionId));

        try {
            boolean enrollmentAccepted = "IA".equals(mResponseCode);
            EftpsEnrollmentDetail enrollmentDetail = updateEnrollmentDetail(enrollmentAccepted);
            EftpsEnrollment enrollment = enrollmentDetail.getEftpsEnrollment();
            EftpsEnrollmentStatus oldStatus = enrollment.getStatusCd();
            CompanyAgency companyAgency = enrollment.getCompanyAgency();
            Company company = companyAgency.getCompany();

            if (Company.isDGDeleteFeatureEnabled() && company.getIsDgDisassociated()) {
                String msg = String.format("Error updating EFTPS Enrollment status (response) for company %s:%s. Error message: As Company is DG Disassociated",
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId());
                logger.error(msg);
                return;
            }

            ++mRecordCount;

            try {
                //
                // only update the EftpsEnrollment record if it's still in a PendingAcceptance state
                //
                if (!EftpsEnrollmentStatus.PendingAcceptance.equals(oldStatus)) {
                    String msg = "Skipping EFTPS enrollment status update from Enrollment Response (%s) file for " +
                                 "company %s:%s since their associated EftpsEnrollment record status is no longer " +
                                 "in a PendingAcceptance state (current state is: %s, new state would have been %s).";

                    EftpsEnrollmentStatus skippedStatus = enrollmentAccepted ? EftpsEnrollmentStatus.Enrolled : EftpsEnrollmentStatus.Rejected;

                    logger.warn(String.format(msg, getEdiFileType().name(), company.getSourceSystemCd(), company.getSourceCompanyId(), oldStatus, skippedStatus));
                } else {
                    if (enrollmentAccepted) { // EFTPS enrollment accepted (success)
                        //
                        // On success, REF02 holds the eftps enrollment id
                        //
                        enrollment.setEftpsEnrollmentId(mRefRecordTemplate.getFieldValue(FieldId.EDI_824_SEG_REF02));

                        Application.save(enrollment);

                        logger.info(String.format("EFTPS enrollment successful for company %s:%s", company.getSourceSystemCd(), company.getSourceCompanyId()));
                    } else { // EFTPS enrollment rejected
                        logger.warn(String.format("EFTPS enrollment rejected by IRS for company %s:%s (reason: %s)",
                                                  company.getSourceSystemCd(), company.getSourceCompanyId(), enrollmentDetail.getRejectReason()));
                    }

                    //
                    // Update the eftps enrollment status for the company
                    //
                    ProcessResult result = PayrollServices.companyManager.updateEftpsEnrollment(enrollment,
                                                                                                enrollmentDetail.getStatusCd());

                    if (!result.isSuccess()) {
                        String msg = String.format("Error updating EFTPS Enrollment status (response) for company %s:%s. Error message: %s",
                                                   company.getSourceSystemCd().toString(),
                                                   company.getSourceCompanyId(),
                                                   result.toString());
                        logger.error(msg);
                    }
                }
            } finally {
                Application.evict(company);
                Application.evict(companyAgency);
            }
        } catch (Throwable t) {
            // This transaction could not be found
            // Log it and keep going since we don't want to interfere with other company responses.
            String txnInfo = String.format("TransactionSetId: %d, TransactionId: %d", mTransactionSetId, mTransactionId);
            logger.error(String.format("Error processing EFTPS Enrollment Response (%s) from %s ", txnInfo, getDetailedFileName()), t);
        }
    }

    private EftpsEnrollmentDetail updateEnrollmentDetail(boolean pEnrollmentAccepted) {
        EftpsEnrollmentDetail enrollmentDetail = EftpsEnrollmentDetail.findEnrollmentDetailByTransactionSetId(mTransactionSetId, mTransactionId);
        EftpsEnrollmentStatus newStatus;
        SpcfCalendar now = PSPDate.getPSPTime();

        //
        // Update the EftpsEnrollmentDetail record with the response
        //

        if (pEnrollmentAccepted) { // EFTPS enrollment accepted (success)
            ++mAcceptCount;
            newStatus = EftpsEnrollmentStatus.Enrolled;
        } else { // EFTPS enrollment rejected
            ++mRejectCount;
            newStatus = EftpsEnrollmentStatus.Rejected;

            //
            // In a rejection, REF02 holds the return error code
            //

            enrollmentDetail.setRejectCd(mRefRecordTemplate.getFieldValue(FieldId.EDI_824_SEG_REF02));
            enrollmentDetail.setRejectReason(getRejectReasonMessage(mRefRecordTemplate.getFieldInt(FieldId.EDI_824_SEG_REF02)));
        }

        enrollmentDetail.setStatusCd(newStatus);
        enrollmentDetail.setStatusEffectiveDate(now);
        enrollmentDetail.setResponseFile((EftpsFile)getEdiFileRecord().getEftpsFile());
        enrollmentDetail.setResponseDate(now);

        return Application.save(enrollmentDetail);
    }

    private String getRejectReasonMessage(int pReasonCode) {
        String message;

        switch (pReasonCode) {
            case EftpsBpConstants.ENROLLMENT_REJECTED_NAME_MISMATCH:
                message = "Name Mismatch";
                break;
            case EftpsBpConstants.ENROLLMENT_REJECTED_TIN_MISMATCH:
                message = "TIN Mismatch";
                break;
            case EftpsBpConstants.ENROLLMENT_REJECTED_TIN_BLANK_OR_INVALID:
                message = "TIN Invalid";
                break;
            default:
                message = "Unknown";
                break;
        }

        return String.format("Return Code: %d (%s)", pReasonCode, message);
    }

}
