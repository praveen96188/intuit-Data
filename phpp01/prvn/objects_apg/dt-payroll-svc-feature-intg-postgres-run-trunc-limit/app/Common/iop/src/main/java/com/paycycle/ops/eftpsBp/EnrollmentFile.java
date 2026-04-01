/**
 * EnrollmentFile.java
 *
 * Copyright(c) 2007 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc.("Confidential Information").  You shall not
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

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsFileBoundaryException;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Map;

public class EnrollmentFile extends EftpsEDIFile {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(EnrollmentFile.class);
    private static final String LEGALNAME = "legalname";
    private static final String LEGALZIP = "legalzip";
    private static final String FEDTAXID = "fedtaxid";

    private int mLxIndex = 0;
    private Company mCompany;
    private EftpsEnrollment mEftpsEnrollment;

    // retrieve established file/segment boundaries from system parameter table
    protected final int mMaxSegmentsPerFile = EftpsUtil.getEdi838MaxSegmentsPerFile();
    protected final int mMaxTransactionsPerSegment = EftpsUtil.getEdi838MaxTransactionsPerSegment();

    protected EDIRecordTemplate mBtpRecordTemplate;
    protected EDIRecordTemplate mPerRecordTemplate;
    protected EDIRecordTemplate mLxRecordTemplate;
    protected EDIRecordTemplate mN1RecordTemplate;
    protected EDIRecordTemplate mN3RecordTemplate; // not used for writing, but maybe for reading
    protected EDIRecordTemplate mN4RecordTemplate;
    protected EDIRecordTemplate mN9RecordTemplate;
    protected int mSuccessfulEnrollmentCount = 0;
    protected int mInvalidEnrollmentCount = 0;
    protected int mRecordCount = 0;

    public EnrollmentFile() {
        mBtpRecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_BTP);
        mPerRecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_PER);
        mLxRecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_LX);
        mN1RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N1);
        mN3RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N3);
        mN4RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N4);
        mN9RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N9);

        configureForTransmit();
    }

    public EDIRecordTemplate getBtpRecordTemplate() {
        return mBtpRecordTemplate;
    }

    public EDIRecordTemplate getPerRecordTemplate() {
        return mPerRecordTemplate;
    }

    public int getSuccessfulEnrollmentCount() {
        return mSuccessfulEnrollmentCount;
    }

    public int getInvalidEnrollmentCount() {
        return mInvalidEnrollmentCount;
    }

    public int getRecordCount() {
        return mRecordCount;
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI838;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.EftpsEnrollment;
    }

    @Override
    protected int startTransactionSet() {
        if (getCurrentTransactionSetCount() >= mMaxSegmentsPerFile) {
            throw new EftpsFileBoundaryException(String.format("EFTPS spec allows no more than %d ST Segments per %s file.",
                                                              mMaxSegmentsPerFile, getEdiFileType().name()));
        }

        return super.startTransactionSet() + writeBTPHeader() + writePERHeader();
    }

    protected int writeBTPHeader() {
        try {
            mBtpRecordTemplate.setFieldValue(FieldId.EDI_838_SEG_BTP02, EftpsUtil.getConfigString("psp_eftps_sender_id"));
            mBtpRecordTemplate.setFieldValue(FieldId.EDI_838_SEG_BTP03, new SimpleDateFormat("yyMMdd").format(getCreateDate()));
            mBtpRecordTemplate.setFieldValue(FieldId.EDI_838_SEG_BTP04, new SimpleDateFormat("HHmm").format(getCreateDate()));

            return writeTransaction(mBtpRecordTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write BTP header. ", t);
        }
    }

    protected int writePERHeader() {
        try {
            return writeTransaction(mPerRecordTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write PER header. ", t);
        }
    }

    protected void writeEnrollmentData() {
        //
        // Each LX loop consists of 4 transactions: LX, N1, N4, N9 (N3 is optional so we choose not to use it)
        //
        writeTransaction(mLxRecordTemplate);
        writeTransaction(mN1RecordTemplate);
        writeTransaction(mN4RecordTemplate);
        writeTransaction(mN9RecordTemplate);
    }

    private boolean validateEnrollmentData(Map<String, String> pDataFields) {
        StringBuffer errors = new StringBuffer();
        String data;

        String fedTaxId = mCompany.getFedTaxId();

        if ((fedTaxId == null) || (fedTaxId.length() == 0)) {
            data = "Invalid FedTaxId (missing)";
            errors.append(String.format("%s%n", data));
        } else {
            fedTaxId = StringUtil.stripNonNumeric(fedTaxId);

            if (fedTaxId.length() == 9) {
                data = fedTaxId;
            } else {
                data = "Invalid FedTaxId (length)";
                errors.append(String.format("%s%n", data));
            }
        }

        pDataFields.put(FEDTAXID, data);

        String legalName = mCompany.getLegalName();

        if ((legalName == null) || (legalName.length() == 0)) {
            data = "Invalid LegalName (missing)";
            errors.append(String.format("%s%n", data));
        } else {
            legalName = legalName.replace('~', '-').replace('\\', '/').toUpperCase().trim();

            if (legalName.length() > 35) {
                legalName = legalName.substring(0, 35); // max 35 chars for this field
            }

            if (legalName.length() > 0) {
                data = legalName;
            } else {
                data = "Invalid LegalName (length)";
                errors.append(String.format("%s%n", data));
            }
        }

        pDataFields.put(LEGALNAME, data);

        Address legalAddress = mCompany.getLegalAddress();

        if (legalAddress == null) {
            data = "Invalid LegalAddress (missing)";
            errors.append(String.format("%s%n", data));
        } else {
            String legalZip = legalAddress.getZipCode();

            if ((legalZip == null) || (legalZip.length() == 0)) {
                data = "Invalid LegalZip (missing)";
                errors.append(String.format("%s%n", data));
            } else {
                legalZip = StringUtil.stripNonNumeric(legalZip);

                if (legalZip.length() > 5) {
                    legalZip = legalZip.substring(0, 5); // zip must be 5 chars
                }

                if (legalZip.length() == 5) {
                    data = legalZip;
                } else {
                    data = "Invalid LegalZip (length)";
                    errors.append(String.format("%s%n", data));
                }
            }
        }

        pDataFields.put(LEGALZIP, data);

        //Check DGDeleted flag
        if(Company.isDGDeleteFeatureEnabled() && mCompany.getIsDgDisassociated()){
            data = "The Company is Deleted through a DG request. PSID";
            errors.append(String.format("%s:%n", data, mCompany.getSourceCompanyId()));
        }

        if (errors.length() > 0) {
            errors.insert(0, String.format("The following errors were encountered while processing EFTPS Enrollment for company %s:%s%n",
                                           mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId()));
            logger.error(errors.toString());
        }

        return errors.length() == 0;
    }

    private void updateEftpsEnrollmentStatus(EftpsEnrollmentStatus pStatus) {
        ProcessResult<EftpsEnrollment> result = PayrollServices.companyManager.updateEftpsEnrollment(mEftpsEnrollment, pStatus);

        if (!result.isSuccess()) {
            String msg = String.format("Error updating EFTPS Enrollment status for company %s:%s. Error message: %s",
                                       mCompany.getSourceSystemCd().toString(),
                                       mCompany.getSourceCompanyId(),
                                       result.toString());
            logger.error(msg);
        } else {
            mEftpsEnrollment = result.getResult();

            if (EftpsEnrollmentStatus.PendingAcceptance.equals(pStatus)) {
                getEftpsEdiFileRecord().addOrUpdateEnrollmentDetailRecord(mEftpsEnrollment, mLxIndex);
            }
        }
    }
    /**
     * Generate employer enrollment records.  Provided DomainEntitySet must be a set of EftpsEnrollment.
     */
    @Override
    protected <T extends DataObject> int writeContent(DomainEntitySet<T> pData) {
        mSuccessfulEnrollmentCount = 0;
        mInvalidEnrollmentCount = 0;
        mRecordCount = 0;

        if ((pData != null) && !pData.isEmpty()) {
            Map<String, String> dataFields = new Hashtable<String, String>();

            mLxIndex = 0; // mLxIndex must start at 0.

            for (DataObject obj : pData) {
                mEftpsEnrollment = (EftpsEnrollment) obj;

                mCompany = mEftpsEnrollment.getCompanyAgency().getCompany();

                dataFields.clear();

                if (!validateEnrollmentData(dataFields)) {
                    updateEftpsEnrollmentStatus(EftpsEnrollmentStatus.Invalid);
                    ++mInvalidEnrollmentCount;
                } else {
                    if ((mLxIndex % mMaxTransactionsPerSegment) == 0) {
                        // If we haven't processed any companies yet, start the file and start a new transaction set.
                        // Else we just need to end the previous transaction set and start a new one.
                        if (mSuccessfulEnrollmentCount == 0) {
                            startFile();
                        } else {
                            endTransactionSet();
                        }

                        startTransactionSet();

                        mLxIndex = 0; // reset lx loop index
                    }

                    mLxRecordTemplate.setFieldValue(FieldId.EDI_838_SEG_LX01, ++mLxIndex);
                    mN1RecordTemplate.setFieldValue(FieldId.EDI_838_SEG_N102, dataFields.get(LEGALNAME));
                    mN1RecordTemplate.setFieldValue(FieldId.EDI_838_SEG_N104, dataFields.get(FEDTAXID));
                    mN4RecordTemplate.setFieldValue(FieldId.EDI_838_SEG_N403, dataFields.get(LEGALZIP));

                    writeEnrollmentData();

                    updateEftpsEnrollmentStatus(EftpsEnrollmentStatus.PendingAcceptance);

                    ++mSuccessfulEnrollmentCount;
                }

                ++mRecordCount;
            }

            if (mSuccessfulEnrollmentCount > 0) {
                if (mLxIndex > 0) {
                    endTransactionSet();
                }

                endFile();
            }
        }

        return mSuccessfulEnrollmentCount;
    }
}
