/**
 * ops/eftps/EdiFile.java
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

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpCommonEncryptedWriter;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordManager;
import com.paycycle.fixedlen.RecordTemplate;
import com.paycycle.util.PgpUtils;
import com.paycycle.util.XmlResourcePool;

import java.io.*;
import java.util.Date;

/**
 * This EdiFile class represents the business-specific EFTPS file.
 */
abstract public class EdiFile extends RecordManager {
    private XmlResourcePool mXmlResourcePool = new XmlResourcePool();
    protected EdiFileRecord mEdiFileRecord = null;
    private Writer mWriter = null;
    private Reader mReader = null;
    private Integer mFileControlNumber = null;
    private int mTransactionSetCtrlNum = 0; // ST segment control number
    private int mTransactionSetCount = 0; // the number of ST segments
    protected int mTransactionCount = 0; // the number of transactions within an ST segment

    protected String mFileName = null;
    protected Date mCreateDate = CalendarUtils.convertToDate(PSPDate.getPSPTime());
    protected EDIRecordTemplate mISAHeaderTemplate;
    protected EDIRecordTemplate mIEATrailerTemplate;
    protected EDIRecordTemplate mGSHeaderTemplate;
    protected EDIRecordTemplate mGETrailerTemplate;
    protected EDIRecordTemplate mSTHeaderTemplate;
    protected EDIRecordTemplate mSETrailerTemplate;

    /**
     * This is a simple listener class to allow the file control number to be set when reading an EDI file.
     */
    class EdiFileDefaultSegmentListener implements RecordListener {
        public void recordCreated(final RecordTemplate pRecordTemplate) {
            if (pRecordTemplate == mISAHeaderTemplate) {
                setFileControlNumber(pRecordTemplate.getFieldInt(FieldId.EDI_SEG_ISA13));
            } else if (pRecordTemplate == mGSHeaderTemplate) {
                //
                // Use GS06 as the file control number since the AS400 has different values for ISA13 and GS06
                // (this will allow 997/813 files from the TFA to be processed correctly for the AS400)
                // (once the AS400 is retired, we can go back to using ISA13 as the file control number)
                // (this won't interfere with PSP since it uses the same values for ISA13 and GS06)
                //
                setFileControlNumber(pRecordTemplate.getFieldInt(FieldId.EDI_SEG_GS06));
            }
        }
    }

    public EdiFile() {
        mXmlResourcePool.addResource("/eftps-edi-field-def.xml");

        mISAHeaderTemplate = getRecordTemplate(RecordId.EDI_SEG_ISA);
        mIEATrailerTemplate = getRecordTemplate(RecordId.EDI_SEG_IEA);
        mGSHeaderTemplate = getRecordTemplate(RecordId.EDI_SEG_GS);
        mGETrailerTemplate = getRecordTemplate(RecordId.EDI_SEG_GE);
        mSTHeaderTemplate = getRecordTemplate(RecordId.EDI_SEG_ST);
        mSETrailerTemplate = getRecordTemplate(RecordId.EDI_SEG_SE);
    }

    public EDIRecordTemplate getRecordTemplate(int pTemplateId) {
        return getRecordTemplate(pTemplateId, false);
    }

    public EDIRecordTemplate getRecordTemplate(int pTemplateId, boolean pCreateNew) {
        return (EDIRecordTemplate) getXmlResourcePool().get(pTemplateId, pCreateNew);
    }

    @Override
    protected void setDefaultListeners() {
        addReadRecordListener(new EdiFileDefaultSegmentListener());
    }

    protected XmlResourcePool getXmlResourcePool() {
        return mXmlResourcePool;
    }

    protected void setWriter(final Writer pWriter) {
        mWriter = pWriter;
    }

    protected void setReader(final Reader pReader) {
        mReader = pReader;
    }

    public Date getCreateDate() {
        return mCreateDate;
    }

    public void configureForTransmit() {
        createFileName(getEftpsFileType().toString(), getEdiFileType().toString(), EftpsUtil.getWorkDir());
        getEdiFileRecord().setCompletionStatus(EdiFileStatus.PendingTransmission);
    }

    /**
     * The file name for this EDI file.  If the file name has not yet been assigned, generate a default.
     * <br>
     * The default file name is: [EdiFileType][FileControlNumber].[EdiFileType]
     *
     * @return The file name for this EDI file.
     */
    public String getFileName() {
        if (mFileName == null) {
            createFileName(getEftpsFileType().toString(), getEdiFileType().toString(), null);
        }

        return mFileName;
    }

    public void setFileName(String pFileName) {
        mFileName = pFileName;
    }

    public String getDetailedFileName() {
        return String.format("%s (%s) file %s", getEftpsFileType(), getEdiFileType(), getFileName());
    }

    public abstract EdiFileRecord getEdiFileRecord();

    public EDIRecordTemplate getISAHeaderTemplate() {
        return mISAHeaderTemplate;
    }

    public EDIRecordTemplate getIEATrailerTemplate() {
        return mIEATrailerTemplate;
    }

    public EDIRecordTemplate getGSHeaderTemplate() {
        return mGSHeaderTemplate;
    }

    public EDIRecordTemplate getGETrailerTemplate() {
        return mGETrailerTemplate;
    }

    public EDIRecordTemplate getSTHeaderTemplate() {
        return mSTHeaderTemplate;
    }

    public EDIRecordTemplate getSETrailerTemplate() {
        return mSETrailerTemplate;
    }

    /**
     * This method gets the ISA/GS File control number.
     * If it has not been set yet, it retrieves the next file number from
     * EFTPS_FILE_SEQUENCE.  All file types that we send share the same
     * sequence, since the ISA/GS control numbers need to be unique among
     * all the files we send.
     *
     * @return The next unique file control number
     */
    synchronized public int getFileControlNumber() {
        if (mFileControlNumber == null) {
            mFileControlNumber = EftpsUtil.getNewFileIdModifier(mCreateDate);
        }

        return mFileControlNumber;
    }

    synchronized protected void setFileControlNumber(int pFileControlNumber) {
        mFileControlNumber = pFileControlNumber;
    }

    /**
     * ISA control numbers are set to the file control number.
     *
     * @return The common ISA/GS segment control number
     */
    public int getIsaControlNumber() {
        return getFileControlNumber();
    }

    /**
     * Our files only contain one type of EDI Transaction set.  So there
     * will only be one GS segment.  So it can just use the same control
     * number as the enclosing ISA segment.
     *
     * @return The common ISA/GS segment control number
     */
    public int getGsControlNumber() {
        return getFileControlNumber();
    }

    public int getCurrentStControlNumber() {
        return mTransactionSetCtrlNum;
    }

    protected void setCurrentStControlNumber(int pTransactionSetCtrlNum) {
        mTransactionSetCtrlNum = pTransactionSetCtrlNum;
    }

    public int getCurrentTransactionSetCount() {
        return mTransactionSetCount;
    }

    protected String peekNextSegmentCode() {
        try {
            return EDIRecordTemplate.peekNextSegCode(mReader);
        } catch (Throwable t) {
            String err = String.format("Error peeking next segment from %s ", getDetailedFileName());
            throw new EftpsBpRuntimeException(err, t);
        }
    }

    protected int readRecord(RecordTemplate pRecord) {
        try {
            pRecord.reset();

            int bytesRead = pRecord.read(mReader);

            if (bytesRead > 0) {
                notifyReadRecordCreated(pRecord);
            }

            return bytesRead;
        } catch (Throwable t) {
            String err = String.format("Error reading %s record from %s ", pRecord.getName(), getDetailedFileName());
            throw new EftpsBpRuntimeException(err, t);
        }
    }

    protected int writeRecord(RecordTemplate pRecord) {
        try {
            int bytesWritten = pRecord.write(mWriter);

            if (bytesWritten > 0) {
                notifyWriteRecordCreated(pRecord);
            }

            return bytesWritten;
        } catch (Throwable t) {
            String err = String.format("Error writing %s record to %s ", pRecord.getName(), getDetailedFileName());
            throw new EftpsBpRuntimeException(err, t);
        }
    }

    protected int writeTransaction(RecordTemplate pRecord) {
        int bytesWritten = writeRecord(pRecord);

        if (bytesWritten > 0) {
            ++mTransactionCount;
        }

        return bytesWritten;
    }

    public abstract int writeISAHeader();

    public int writeIEATrailer() {
        try {
            mIEATrailerTemplate.setFieldValue(FieldId.EDI_SEG_IEA01, 1);
            mIEATrailerTemplate.setFieldValue(FieldId.EDI_SEG_IEA02, EftpsUtil.formatCtrlNum(getIsaControlNumber()));

            return writeRecord(mIEATrailerTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write IEA trailer. ", t);
        }
    }

    public abstract int writeGSHeader();

    public int writeGETrailer() {
        try {
            mGETrailerTemplate.setFieldValue(FieldId.EDI_SEG_GE01, mTransactionSetCount);
            mGETrailerTemplate.setFieldValue(FieldId.EDI_SEG_GE02, EftpsUtil.formatCtrlNum(getGsControlNumber()));

            return writeRecord(mGETrailerTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write GE trailer. ", t);
        }
    }

    public int writeSTHeader() {
        try {
            mSTHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ST01, getEdiFileType().toString());
            mSTHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ST02, EftpsUtil.formatCtrlNum(mTransactionSetCtrlNum));

            return writeTransaction(mSTHeaderTemplate); // The ST header record counts as a transaction within the ST segment
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write ST header. ", t);
        }
    }

    public int writeSETrailer() {
        try {
            // Increment mTransactionCount since the SE trailer is included in the transaction count for the ST segment
            mSETrailerTemplate.setFieldValue(FieldId.EDI_SEG_SE01, ++mTransactionCount);
            mSETrailerTemplate.setFieldValue(FieldId.EDI_SEG_SE02, EftpsUtil.formatCtrlNum(mTransactionSetCtrlNum));

            return writeRecord(mSETrailerTemplate); // no need to call writeTransaction since we're ending the ST segment
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write SE trailer. ", t);
        }
    }

    protected int startFile() {
        mTransactionSetCount = 0;

        return writeISAHeader() + writeGSHeader();
    }

    protected int endFile() {
        return writeGETrailer() + writeIEATrailer();
    }

    protected int startTransactionSet() {
        return startTransactionSet(EftpsUtil.getNewSegmentControlNumber());
    }

    protected int startTransactionSet(int pTransactionSetCtrlNum) {
        // increment the transaction set count since we're starting a new ST segment
        ++mTransactionSetCount;

        // reset the transaction count for this new ST segment
        mTransactionCount = 0;

        // set this ST segment's control number
        mTransactionSetCtrlNum = pTransactionSetCtrlNum;

        // write the new ST header
        return writeSTHeader();
    }

    protected int endTransactionSet() {
        return writeSETrailer();
    }

    protected void createFileName(String pFilePrefix, String pFileExtension, String pDirectory) {
        String prefix = (pFilePrefix == null) ? "" : pFilePrefix;
        String ext = (pFileExtension == null) ? "" : pFileExtension;

        // remove leading '.' from file extension if present.
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }

        String fileName = String.format("%s%d.%s.pgp", prefix, getFileControlNumber(), ext);
        boolean validDir = ((pDirectory != null) && (pDirectory.length() > 0));

        mFileName = (validDir ? new File(new File(pDirectory), fileName).getPath() : fileName);
    }

    /**
     * This method is intended to be overridden to return the EDI file type (838, 997, etc.)
     *
     * @return Returns the EDI file type (see EftpsEdiType)
     */
    abstract public EftpsEdiType getEdiFileType();

    /**
     * This method is intended to be overridden to return the EFTPS file type (Enrollment, Payment, etc.)
     *
     * @return Returns the EFTPS file type (see EdiFileType)
     */
    abstract public EdiFileType getEftpsFileType();

    public void cleanup() {
        //
        // If the EftpsFile record is present, delete it and any associated detail records belonging to it
        //
        if (mEdiFileRecord != null) {
            mEdiFileRecord.delete();
        }
    }

    /**
     * Method to force write content to File.
     * Override if you want to force the writing of the file even if no data was processed in writeContent()
     * @return
     */
    protected boolean forceWriteToFile() {
        return false;
    }

    /**
     * Finalize the write operation (flush data to physical file and handle EftpsFile record finalization details)
     *
     * @param pWriteFile Flag to indicate whether the cached data should be written to a physical file.
     * @throws IOException
     */
    protected void finalizeWrite(boolean pWriteFile) throws IOException {
        boolean writeFile = (pWriteFile || forceWriteToFile());

        //
        // If requested, write the cached data to the specified physical file
        //
        if (writeFile) {
            writeContentToFile();
        }

        //
        // If the EftpsFile record is present, deal with its finalization details.
        //
        if (mEdiFileRecord != null) {
            if (writeFile) {
                mEdiFileRecord.completeRecord(); // complete the EftpsFile record
            } else {
                mEdiFileRecord.delete(); // clean up the EftpsFile record if it was allocated
            }
        }
    }

    /**
     * Write the generated content to the configured (or default) file.
     *
     * @throws IOException Error writing content to file.
     */
    protected void writeContentToFile() throws IOException {
        PgpWriter pw ;
        try {
            pw = new PgpCommonEncryptedWriter(PgpUtils.getTfaPgpKeys());
            pw.open(getFileName());

            try {
                pw.write(mWriter.toString());
            } finally {
                pw.flush();
                pw.close();
            }
        } catch (IOException e){
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Generic method to read EDI records from the configured Reader.
     * Override to read EDI records in whatever manner is required.
     *
     * @return : The number of EDI records read.
     */
    protected int readContent() {
        return 0;
    }

    /**
     * Generic method to write EDI records to the configured Writer.
     * Override to write EDI records in whatever manner is required.
     *
     * @return : The number of EDI records written.
     */
    protected int writeContent() {
        return 0;
    }

    /**
     * Process the given DomainEntitySet into the intended EDI records for this file.
     * Return the number of records written.
     *
     * @param pData : A DomainEntitySet containing the appropriate source data used to generate the EDI records.
     * @param <T>   : T extends DataObject.
     * @return : The number of EDI records created.
     */
    protected <T extends DataObject> int writeContent(DomainEntitySet<T> pData) {
        return 0;
    }

    /**
     * Generate and write out the content for this EDI file to the default EDIWrappedStringWriter and save the
     * Writer content to a file.
     * Call setFileName() prior to this method to set the file to which the content is written.
     *
     * @param pData : The domain entity list to use as the source of EDI field data.
     * @param <T>   : T extends DataObject
     * @return : The number of processed domain entities.
     */
    public abstract <T extends DataObject>  int write(DomainEntitySet<T> pData);

    /**
     * Generate and write out the content for this EDI file to the given Writer and save the Writer content to a file.
     * Call setFileName() prior to this method to set the file to which the content is written.
     * This method closes the given Writer upon completion.
     *
     * @param pData   : The domain entity list to use as the source of EDI field data.
     * @param pWriter : The Writer to which the EDI contents are written.
     * @param <T>     : T extends DataObject
     * @return : The number of processed domain entities.
     */
    public <T extends DataObject> int write(DomainEntitySet<T> pData, Writer pWriter) {
        int count = 0;

        try {
            setWriter(pWriter);

            try {
                count = writeContent(pData);
                finalizeWrite(count > 0);
            } finally {
                pWriter.close();
            }
        } catch (Throwable t) {
            cleanup();
            throw new EftpsBpRuntimeException(String.format("Error writing %s ", getDetailedFileName()), t);
        }

        return count;
    }

    /**
     * Generate and write out the content for this EDI file to the default EDIWrappedStringWriter and save the
     * Writer content to a file.
     * Call setFileName() prior to this method to set the file to which the content is written.
     *
     * @return : The number of EDI records written.
     */
    public abstract int write();

    /**
     * Generate and write out the content for this EDI file to the given Writer and save the Writer content to a file.
     * Call setFileName() prior to this method to set the file to which the content is written.
     * This method closes the given Writer upon completion.
     *
     * @param pWriter : The Writer to which the EDI contents are written.
     * @return : The number of EDI records written.
     */
    public int write(Writer pWriter) {
        int count = 0;

        try {
            setWriter(pWriter);

            try {
                count = writeContent();
                finalizeWrite(count > 0);
            } finally {
                pWriter.close();
            }
        } catch (Throwable t) {
            cleanup();
            throw new EftpsBpRuntimeException(String.format("Error writing %s ", getDetailedFileName()), t);
        }

        return count;
    }

    /**
     * Reads the EDI contents from the configured file.
     * Call setFileName() prior to this method to set the file from which the content is read.
     *
     * @return : The number of EDI records read.
     * @throws IOException : Error reading data from Reader
     */
    public int read() throws IOException {
        return read(new BufferedReader(new FileReader(getFileName())));
    }

    /**
     * Reads the EDI contents from the given Reader.
     *
     * @param pReader : The Reader from which to read the EDI content.
     * @return : The number of EDI records read.
     * @throws IOException : Error reading data from Reader
     */
    public int read(Reader pReader) throws IOException {
        int count = 0;

        try {
            setReader(pReader);

            try {
                count = readContent();
            } finally {
                pReader.close();
            }
        } catch (Throwable t) {
            cleanup();
            throw new EftpsBpRuntimeException(String.format("Error reading %s ", getDetailedFileName()), t);
        }

        return count;
    }
}
