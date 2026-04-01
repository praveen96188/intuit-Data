package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.edi813.Edi813SegmentList;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur, kpaul
 * Date: Dec 28, 2010
 * Time: 9:06:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentFileReader extends EftpsEDIFile implements RecordListener {
    protected EDIRecordTemplate mBtiRecordTemplate;
    protected EDIRecordTemplate mDtmRecordTemplate;
    protected EDIRecordTemplate mBprRecordTemplate;
    protected EDIRecordTemplate mTfsRecordTemplate;
    protected EDIRecordTemplate mFgsRecordTemplate;
    protected EDIRecordTemplate mTiaOuterRecordTemplate;
    protected EDIRecordTemplate mTiaInnerRecordTemplate;
    protected EDIRecordTemplate mRefOuterRecordTemplate;
    protected EDIRecordTemplate mRefInnerRecordTemplate;
    protected Edi813SegmentList mSegmentList;

    public PaymentFileReader() {
        mBtiRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_BTI);
        mDtmRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_DTM);
        mBprRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_BPR);
        mTfsRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_TFS);
        mFgsRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_FGS);
        mTiaOuterRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_OUTER_TIA);
        mTiaInnerRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_INNER_TIA);
        mRefOuterRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_OUTER_REF);
        mRefInnerRecordTemplate = getRecordTemplate(RecordId.EDI_813_SEG_INNER_REF);

        addReadRecordListener(this);
    }

    public Edi813SegmentList getSegmentList() {
        return mSegmentList;
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI813;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        return EdiFileType.EftpsPayment;
    }

    @Override
    public void configureForTransmit() {
    }

    @Override
    public String getFileName() {
        return mFileName;
    }

    @Override
    protected int readContent() {
        int count = 0;
        String peekSeg;

        mSegmentList = new Edi813SegmentList();

        // read the ISA header record
        readRecord(getISAHeaderTemplate());

        // read the GS header record
        readRecord(getGSHeaderTemplate());

        while (true) {
            peekSeg = peekNextSegmentCode();

            if ((peekSeg == null) || peekSeg.startsWith("GE")) {
                // There are no more transaction sets in the file.
                break;
            }

            if (!peekSeg.startsWith("ST")) {
                throw new EftpsBpRuntimeException("Expecting ST segment, found: " + peekSeg);
            }

            // read the ST header record
            readRecord(getSTHeaderTemplate());

            // read the BTI header record
            readRecord(mBtiRecordTemplate);

            // read the DTM header record
            readRecord(mDtmRecordTemplate);

            // read the TIA header record
            readRecord(mTiaOuterRecordTemplate);

            // read the outer REF segment (if any)
            peekSeg = peekNextSegmentCode();
            if ((peekSeg != null) && peekSeg.startsWith("REF")) {
                readRecord(mRefOuterRecordTemplate);
            }

            // read the BPR/REF segment (if any)
            peekSeg = peekNextSegmentCode();
            if (peekSeg != null && peekSeg.startsWith("REF")) { // AS same day will have two REF'S
               readRecord(mRefOuterRecordTemplate);
            }

            // PREVIOUS ONE IS NOT REF IT SHOULD BE BPR
            peekSeg = peekNextSegmentCode();
            if ((peekSeg != null) && peekSeg.startsWith("BPR")) {
                readRecord(mBprRecordTemplate);
            }

            while (true) {
                peekSeg = peekNextSegmentCode();

                if (peekSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Payments file. Expected TFS or SE segment.");
                }

                if (peekSeg.startsWith("SE")) {
                    // read the SE trailer record (also writes the ack info in notify handler)
                    readRecord(getSETrailerTemplate());
                    break;
                }

                // read the TFS Loop
                count += readRecord(mTfsRecordTemplate);

                //read REF segment
                count += readRecord(mRefInnerRecordTemplate);

                //read FGS segment
                count += readRecord(mFgsRecordTemplate);

                while (peekNextSegmentCode().startsWith("TIA")) {
                    count += readRecord(mTiaInnerRecordTemplate);
                }
            }
        }

        // read the GE trailer record
        readRecord(getGETrailerTemplate());

        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());

        return count;
    }

    public void recordCreated(RecordTemplate template) {
        if (template == getSTHeaderTemplate()) {
            mSegmentList.addStSegment(template);
        } else if (template == mBtiRecordTemplate) {
            mSegmentList.setBtiSegment(template);
        } else if (template == mDtmRecordTemplate) {
            mSegmentList.setDtmSegment(template);
        } else if (template == mTiaOuterRecordTemplate) {
            mSegmentList.setTiaSegment(template);
        } else if (template == mRefOuterRecordTemplate) {
            mSegmentList.setRefSegment(template);
        } else if (template == mBprRecordTemplate) {
            mSegmentList.setBprSegment(template);
        } else if (template == mTfsRecordTemplate) {
            mSegmentList.addTfsSegment(template);
        } else if (template == mRefInnerRecordTemplate) {
            mSegmentList.setTfsRefSegment(template);
        } else if (template == mTiaInnerRecordTemplate) {
            mSegmentList.addTfsTiaSegment(template);
        }
    }
}
