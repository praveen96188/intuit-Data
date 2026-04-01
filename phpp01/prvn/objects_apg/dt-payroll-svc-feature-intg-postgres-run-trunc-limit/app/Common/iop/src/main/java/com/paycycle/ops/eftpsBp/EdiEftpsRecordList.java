package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.FieldTemplate;
import com.paycycle.fixedlen.RecordTemplate;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 3, 2011
 * Time: 8:09:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiEftpsRecordList extends EdiEftpsFileReader {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(EdiEftpsRecordList.class);

    private List<EDIRecordTemplate> mRecordList = new ArrayList<EDIRecordTemplate>();
    private int mMismatchCount = 0;

    public EdiEftpsRecordList(File pEdiFile) {
        super(pEdiFile);
        readFile();
    }

    public EdiEftpsRecordList(String pFileName) {
        super(pFileName);
        readFile();
    }

    public int getMismatchCount() {
        return mMismatchCount;
    }

    public List<EDIRecordTemplate> getRecordList() {
        return mRecordList;
    }

    public boolean isFileValid() {
        return EdiEftpsFileValidator.isValid(getEdiFile());
    }

    public void recordCreated(final RecordTemplate template) {
        //
        // Want a copy since xml resource pool reuses templates (and we don't want to keep reloading the resource)
        //
        mRecordList.add((EDIRecordTemplate) template.clone());
    }

    public boolean equals(EdiEftpsRecordList pRhsRecordList) {
        mMismatchCount = 0;

        if (getEdiFile() == null) {
            ++mMismatchCount;
            logger.warn("EDI file compare error: LHS File is null.");
        } else if (pRhsRecordList.getEdiFile() == null) {
            ++mMismatchCount;
            logger.warn("EDI file compare error: RHS File is null.");
        } else {
            StringBuffer sb = new StringBuffer();

            sb.append(String.format("Comparing EDI files (%s):", getEdiFileType().name())).append(EftpsUtil.NEWLINE);
            sb.append("LHS File: ").append(getEdiFile().getPath()).append(EftpsUtil.NEWLINE);
            sb.append("RHS File: ").append(pRhsRecordList.getEdiFile().getPath()).append(EftpsUtil.NEWLINE);

            logger.info(sb.toString());

            if (getEdiFileType() == null) {
                ++mMismatchCount;
                logger.warn("LHS EftpsEdiType is null.");
            } else if (pRhsRecordList.getEdiFileType() == null) {
                ++mMismatchCount;
                logger.warn("RHS EftpsEdiType is null.");
            } else if (!getEdiFileType().equals(pRhsRecordList.getEdiFileType())) {
                ++mMismatchCount;
                logger.warn(String.format("File type mismatch. LHS: %s, RHS: %s",
                                          getEdiFileType(), pRhsRecordList.getEdiFileType()));
            } else if (mRecordList.size() != pRhsRecordList.mRecordList.size()) {
                ++mMismatchCount;
                logger.warn(String.format("Record count mismatch. LHS: %d, RHS: %d",
                                          mRecordList.size(), pRhsRecordList.mRecordList.size()));
            } else {
                for (int recordIndex = 0; recordIndex < mRecordList.size(); ++recordIndex) {
                    EDIRecordTemplate lhsRecord = mRecordList.get(recordIndex);
                    EDIRecordTemplate rhsRecord = pRhsRecordList.mRecordList.get(recordIndex);

                    if (lhsRecord.getId() != rhsRecord.getId()) {
                        printRecordMismatch(lhsRecord, rhsRecord);
                    } else if (lhsRecord.getFieldCount() != rhsRecord.getFieldCount()) {
                        printRecordMismatch(lhsRecord, rhsRecord);
                    } else {
                        for (FieldTemplate lhsField : lhsRecord.getFieldList()) {
                            FieldTemplate rhsField = rhsRecord.getField(lhsField.getId());

                            if (rhsField == null) {
                                printRecordMismatch(lhsRecord, rhsRecord);
                                break; // we know the records don't match, so we're done
                            } else if (isFieldComparable(lhsRecord.getId(), lhsField.getId())) {
                                if ((lhsField.getValue() == null) && (rhsField.getValue() == null)) {
                                    continue; // both field values are null, so just move on
                                }

                                if ((lhsField.getValue() == null) || (rhsField.getValue() == null) ||
                                    !lhsField.getValue().equals(rhsField.getValue())) {
                                    printRecordMismatch(lhsRecord, rhsRecord);
                                    break; // we know the records don't match, so we're done
                                }
                            }
                        }
                    }
                }
            }

            if (mMismatchCount == 0) {
                logger.info("Files match.");
            } else {
                logger.warn(String.format("Files do not match (mismatch record count: %d)", mMismatchCount));
            }
        }

        return (mMismatchCount == 0);
    }

    private void printRecordMismatch(EDIRecordTemplate pLhsRecord, EDIRecordTemplate pRhsRecord) {
        ++mMismatchCount;

        StringBuffer sb = new StringBuffer();
        String ediType = (getEdiFileType() != null) ? getEdiFileType().name() : "edi type unknown";

        sb.append(String.format("EDI Record mismatch (%s):", ediType)).append(EftpsUtil.NEWLINE);
        sb.append("LHS Record: ").append(pLhsRecord.getRecordBuffer()).append(EftpsUtil.NEWLINE);
        sb.append("RHS Record: ").append(pRhsRecord.getRecordBuffer()).append(EftpsUtil.NEWLINE);

        logger.warn(sb.toString());
    }

    private boolean isFieldComparable(int pRecordId, int pFieldId) {
        switch (pRecordId) {
            // These record segments have no field exclusions (noted here so we know we didn't forget them)
            case RecordId.EDI_SEG_GENERIC:
            case RecordId.EDI_997_SEG_AK5:
            case RecordId.EDI_997_SEG_AK9:
            case RecordId.EDI_838_SEG_PER:
            case RecordId.EDI_838_SEG_N1:
            case RecordId.EDI_838_SEG_N3:
            case RecordId.EDI_838_SEG_N4:
            case RecordId.EDI_838_SEG_N9:
            case RecordId.EDI_813_SEG_OUTER_TIA:
            case RecordId.EDI_813_SEG_BPR:
            case RecordId.EDI_813_SEG_TFS:
            case RecordId.EDI_813_SEG_INNER_TIA:
            case RecordId.EDI_827_SEG_RIC:
            case RecordId.EDI_821_SEG_B2A:
            case RecordId.EDI_821_SEG_DTM:
            case RecordId.EDI_821_SEG_ACT:
            case RecordId.EDI_821_SEG_FIR:
                break;

            case RecordId.EDI_SEG_ISA:
                switch (pFieldId) {
                    case FieldId.EDI_SEG_ISA09:
                    case FieldId.EDI_SEG_ISA10:
                    case FieldId.EDI_SEG_ISA13:
                        return false;
                }
                break;
            case RecordId.EDI_SEG_IEA:
                switch (pFieldId) {
                    case FieldId.EDI_SEG_IEA02:
                        return false;
                }
                break;
            case RecordId.EDI_SEG_GS:
                switch (pFieldId) {
                    case FieldId.EDI_SEG_GS04:
                    case FieldId.EDI_SEG_GS05:
                    case FieldId.EDI_SEG_GS06:
                        return false;
                }
                break;
            case RecordId.EDI_SEG_GE:
                switch (pFieldId) {
                    case FieldId.EDI_SEG_GE02:
                        return false;
                }
                break;
            case RecordId.EDI_SEG_ST:
                switch (pFieldId) {
                    case FieldId.EDI_SEG_ST02:
                        return false;
                }
                break;
            case RecordId.EDI_SEG_SE:
                switch (pFieldId) {
                    case FieldId.EDI_SEG_SE02:
                        return false;
                }
                break;
            case RecordId.EDI_997_SEG_AK1:
                switch (pFieldId) {
                    case FieldId.EDI_997_SEG_AK102:
                        return false;
                }
                break;
            case RecordId.EDI_997_SEG_AK2:
                switch (pFieldId) {
                    case FieldId.EDI_997_SEG_AK202:
                        return false;
                }
                break;
            case RecordId.EDI_824_SEG_BGN:
                switch (pFieldId) {
                    case FieldId.EDI_824_SEG_BGN02:
                    case FieldId.EDI_824_SEG_BGN03:
                    case FieldId.EDI_824_SEG_BGN04:
                    case FieldId.EDI_824_SEG_BGN06:
                        return false;
                }
                break;
            case RecordId.EDI_151_SEG_BTA:
                switch (pFieldId) {
                    case FieldId.EDI_151_SEG_BTA02:
                        return false;
                }
                break;
            case RecordId.EDI_813_SEG_BTI:
                switch (pFieldId) {
                    case FieldId.EDI_813_SEG_BTI05:
                        return false;
                }
                break;
            case RecordId.EDI_151_SEG_BTI:
                switch (pFieldId) {
                    case FieldId.EDI_151_SEG_BTI02:
                    case FieldId.EDI_151_SEG_BTI05:
                    case FieldId.EDI_151_SEG_BTI08:
                        return false;
                }
                break;
            case RecordId.EDI_826_SEG_BTI:
                switch (pFieldId) {
                    case FieldId.EDI_826_SEG_BTI02:
                        return false;
                }
                break;
            case RecordId.EDI_838_SEG_BTP:
                switch (pFieldId) {
                    case FieldId.EDI_838_SEG_BTP03:
                    case FieldId.EDI_838_SEG_BTP04:
                        return false;
                }
                break;
            case RecordId.EDI_813_SEG_DTM:
                switch (pFieldId) {
                    case FieldId.EDI_813_SEG_DTM02:
                        return false;
                }
                break;
            case RecordId.EDI_813_SEG_FGS:
                switch (pFieldId) {
                    case FieldId.EDI_813_SEG_FGS01:
                        return false;
                }
                break;
            case RecordId.EDI_838_SEG_LX:
                switch (pFieldId) {
                    case FieldId.EDI_838_SEG_LX01:
                        return false;
                }
                break;
            case RecordId.EDI_824_SEG_OTI:
                switch (pFieldId) {
                    case FieldId.EDI_824_SEG_OTI03:
                        return false;
                }
                break;
            case RecordId.EDI_813_SEG_OUTER_REF:
                switch (pFieldId) {
                    case FieldId.EDI_813_SEG_OUTER_REF02:
                        return false;
                }
                break;
            case RecordId.EDI_813_SEG_INNER_REF:
                switch (pFieldId) {
                    case FieldId.EDI_813_SEG_INNER_REF02:
                        return false;
                }
                break;
            case RecordId.EDI_824_SEG_REF:
                switch (pFieldId) {
                    case FieldId.EDI_824_SEG_REF02:
                        return false;
                }
                break;
            case RecordId.EDI_827_SEG_REF:
                switch (pFieldId) {
                    case FieldId.EDI_827_SEG_REF02:
                        return false;
                }
                break;
            case RecordId.EDI_151_SEG_TFS:
                switch (pFieldId) {
                    case FieldId.EDI_151_SEG_TFS02:
                    case FieldId.EDI_151_SEG_TFS04:
                        return false;
                }
                break;
            case RecordId.EDI_826_SEG_TFS:
                switch (pFieldId) {
                    case FieldId.EDI_826_SEG_TFS02:
                    case FieldId.EDI_826_SEG_TFS04:
                        return false;
                }
                break;
            case RecordId.EDI_821_SEG_ENT:
                switch (pFieldId) {
                    case FieldId.EDI_821_SEG_ENT01:
                        return false;
                }
                break;
            case RecordId.EDI_821_SEG_TRN:
                switch (pFieldId) {
                    case FieldId.EDI_821_SEG_TRN02:
                        return false;
                }
                break;
        }

        return true;
    }

    public void write(Writer pWriter, boolean pWantCrlf) {
        try {
            for (EDIRecordTemplate template : mRecordList) {
                template.setIncludeCarriageReturn(pWantCrlf);
                template.write(pWriter);
                template.setIncludeCarriageReturn(false); // for neatness and consistency
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error writing EdiEftpsRecordList to writer. ", t);
        }
    }

    public String toString(boolean pFormatted) {
        Writer writer = pFormatted ? new StringWriter() : new EDIWrappedStringWriter();

        write(writer, pFormatted);

        return writer.toString();
    }

    public List<EDIRecordTemplate> getSTRecordList() {
        return getRecordListForId(RecordId.EDI_SEG_ST);
    }

    public List<EDIRecordTemplate> getRecordListForId(int pRecordId) {
        List<EDIRecordTemplate> recordList = new ArrayList<EDIRecordTemplate>();

        for (EDIRecordTemplate ediRecordTemplate : mRecordList) {
            if (ediRecordTemplate.getId() == pRecordId) {
                recordList.add(ediRecordTemplate);
            }
        }

        return recordList;
    }
}
