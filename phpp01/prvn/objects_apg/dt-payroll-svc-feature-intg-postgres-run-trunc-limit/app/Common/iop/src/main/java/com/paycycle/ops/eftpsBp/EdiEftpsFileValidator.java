package com.paycycle.ops.eftpsBp;

import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Feb 3, 2011
 * Time: 1:37:39 AM
 * This validator class does a reasonable validity check on the given EFTPS EDI file to ensure the expected segments
 * are present and in the correct order.  Primarily this class is used to simply ensure that the given EFTPS EDI file
 * is not junk and can be processed (at least at the segment level - we do not validate down to the field level.)
 */
public class EdiEftpsFileValidator extends EdiEftpsFileReader {
    private List<Integer> mNextExpectedSegment;
    private boolean mContinueValidation = true;
    private boolean mValid = false;
    private int mStSegmentCount = 0;
    private int mSeSegmentCount = 0;
    private int mValidatedRecordCount = 0;

    public EdiEftpsFileValidator(File pEdiFile) {
        super(pEdiFile);
    }

    public EdiEftpsFileValidator(String pFileName) {
        super(pFileName);
    }

    @Override
    public void reset() {
        super.reset();
        mValidatedRecordCount = 0;
        mSeSegmentCount = 0;
        mStSegmentCount = 0;
        mValid = false;
        mContinueValidation = true;
        setNextExpectedSegment(RecordId.EDI_SEG_ISA);
    }

    public static boolean isValid(File pEdiFile) {
        return new EdiEftpsFileValidator(pEdiFile).isValid();
    }

    public boolean isValid() {
        readFile(); // calls reset prior to reading file
        return mValid && (mValidatedRecordCount == getRecordCount());
    }

    public void recordCreated(final RecordTemplate template) {
        if (mContinueValidation) {
            validateRecord((EDIRecordTemplate) template);
        }
    }

    private void setNextExpectedSegment(Integer... pNextExpectedSegments) {
        mNextExpectedSegment = Arrays.asList(pNextExpectedSegments);
    }

    private void validateRecord(EDIRecordTemplate pRecordTemplate) {
        mContinueValidation = mNextExpectedSegment.contains(pRecordTemplate.getId());

        if (mContinueValidation) {
            ++mValidatedRecordCount;

            switch (pRecordTemplate.getId()) {
                case RecordId.EDI_SEG_GENERIC:
                    //
                    // Should never see generic segment here. If so, file is invalid.
                    // It means the record should not be identified as an EFTPS record type.
                    //
                    mValid = false;
                    mContinueValidation = false;
                    break;

                case RecordId.EDI_SEG_ISA: // ISA should always be first record in file
                    mContinueValidation = (getRecordCount() == 1);
                    setNextExpectedSegment(RecordId.EDI_SEG_GS);
                    break;

                case RecordId.EDI_SEG_GS:
                    setNextExpectedSegment(RecordId.EDI_SEG_ST);
                    break;

                case RecordId.EDI_SEG_GE:
                    setNextExpectedSegment(RecordId.EDI_SEG_IEA);
                    break;

                case RecordId.EDI_SEG_IEA: // IEA should always be last record in file
                    mValid = ((mStSegmentCount > 0) && (mStSegmentCount == mSeSegmentCount));
                    mContinueValidation = false;
                    break;

                case RecordId.EDI_SEG_SE:
                    ++mSeSegmentCount;
                    setNextExpectedSegment(RecordId.EDI_SEG_ST, RecordId.EDI_SEG_GE);
                    break;

                case RecordId.EDI_SEG_ST:
                    ++mStSegmentCount;

                    // fall-thru intentional

                default:
                    if (getEdiFileType() == null) { // edi file type should never be null at this point
                        mValid = false;
                        mContinueValidation = false;
                        break;
                    }

                    switch (getEdiFileType()) {
                        case EDI151:
                            if(mFileVersion != null && mFileVersion.equals("004010")){
                                switch (pRecordTemplate.getId()) {
                                    case RecordId.EDI_SEG_ST:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_BTA_4010);
                                        break;

                                    case RecordId.EDI_151_SEG_BTA_4010:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_BTI_4010);
                                        break;

                                    case RecordId.EDI_151_SEG_BTI_4010:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_TFS_4010);
                                        break;

                                    case RecordId.EDI_151_SEG_TFS_4010:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_OUTER_REF_4010);
                                        break;
                                    case RecordId.EDI_151_SEG_OUTER_REF_4010: // multiples allowed
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_OUTER_REF_4010, RecordId.EDI_151_SEG_INNER_REF_4010);
                                        break;
                                    case RecordId.EDI_151_SEG_INNER_REF_4010: // multiples allowed
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_INNER_REF_4010, RecordId.EDI_151_SEG_PBI_4010);
                                        break;
                                    case RecordId.EDI_151_SEG_PBI_4010: // multiples allowed
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_TFS_4010, RecordId.EDI_SEG_SE);
                                        break;
                                    default:
                                        mContinueValidation = false;
                                        break;
                                }
                            } else {
                                switch (pRecordTemplate.getId()) {
                                    case RecordId.EDI_SEG_ST:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_BTA);
                                        break;

                                    case RecordId.EDI_151_SEG_BTA:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_BTI);
                                        break;

                                    case RecordId.EDI_151_SEG_BTI:
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_TFS);
                                        break;

                                    case RecordId.EDI_151_SEG_TFS: // multiples allowed
                                        setNextExpectedSegment(RecordId.EDI_151_SEG_TFS, RecordId.EDI_SEG_SE);
                                        break;

                                    default:
                                        mContinueValidation = false;
                                        break;
                                }
                            }
                            break;
                        case EDI813:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_BTI);
                                    break;

                                case RecordId.EDI_813_SEG_BTI:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_DTM);
                                    break;

                                case RecordId.EDI_813_SEG_DTM:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_OUTER_TIA);
                                    break;

                                case RecordId.EDI_813_SEG_OUTER_TIA:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_OUTER_REF);
                                    break;

                                case RecordId.EDI_813_SEG_OUTER_REF: // multiples allowed
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_OUTER_REF, RecordId.EDI_813_SEG_BPR);
                                    break;

                                case RecordId.EDI_813_SEG_BPR:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_TFS);
                                    break;

                                case RecordId.EDI_813_SEG_TFS:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_INNER_REF);
                                    break;

                                case RecordId.EDI_813_SEG_INNER_REF:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_FGS);
                                    break;

                                case RecordId.EDI_813_SEG_FGS:
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_INNER_TIA);
                                    break;

                                case RecordId.EDI_813_SEG_INNER_TIA: // multiples allowed
                                    setNextExpectedSegment(RecordId.EDI_813_SEG_INNER_TIA, RecordId.EDI_813_SEG_TFS, RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;
                        case EDI821:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_B2A);
                                    break;

                                case RecordId.EDI_821_SEG_B2A:
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_DTM);
                                    break;

                                case RecordId.EDI_821_SEG_DTM:
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_TRN);
                                    break;

                                case RecordId.EDI_821_SEG_TRN:
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_ENT);
                                    break;

                                case RecordId.EDI_821_SEG_ENT:
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_ACT);
                                    break;

                                case RecordId.EDI_821_SEG_ACT:
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_FIR);
                                    break;

                                case RecordId.EDI_821_SEG_FIR: // multiples allowed
                                    setNextExpectedSegment(RecordId.EDI_821_SEG_FIR, RecordId.EDI_821_SEG_ACT, RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;
                        case EDI824:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_824_SEG_BGN);
                                    break;

                                case RecordId.EDI_824_SEG_BGN:
                                    setNextExpectedSegment(RecordId.EDI_824_SEG_OTI);
                                    break;

                                case RecordId.EDI_824_SEG_OTI:
                                    setNextExpectedSegment(RecordId.EDI_824_SEG_REF);
                                    break;

                                case RecordId.EDI_824_SEG_REF:
                                    setNextExpectedSegment(RecordId.EDI_824_SEG_OTI, RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;
                        case EDI826:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_826_SEG_BTI);
                                    break;

                                case RecordId.EDI_826_SEG_BTI:
                                    setNextExpectedSegment(RecordId.EDI_826_SEG_TFS);
                                    break;

                                case RecordId.EDI_826_SEG_TFS: // multiples allowed
                                    setNextExpectedSegment(RecordId.EDI_826_SEG_TFS, RecordId.EDI_826_SEG_BTI, RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;
                        case EDI827:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_827_SEG_RIC);
                                    break;

                                case RecordId.EDI_827_SEG_RIC:
                                    setNextExpectedSegment(RecordId.EDI_827_SEG_REF);
                                    break;

                                case RecordId.EDI_827_SEG_REF: // multiples allowed
                                    setNextExpectedSegment(RecordId.EDI_827_SEG_REF, RecordId.EDI_827_SEG_RIC, RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;
                        case EDI838:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_BTP);
                                    break;

                                case RecordId.EDI_838_SEG_BTP:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_PER);
                                    break;

                                case RecordId.EDI_838_SEG_PER:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_LX);
                                    break;

                                case RecordId.EDI_838_SEG_LX:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_N1);
                                    break;

                                case RecordId.EDI_838_SEG_N1:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_N3, RecordId.EDI_838_SEG_N4);
                                    break;

                                case RecordId.EDI_838_SEG_N3: // N3 segment is optional
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_N4);
                                    break;

                                case RecordId.EDI_838_SEG_N4:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_N9);
                                    break;

                                case RecordId.EDI_838_SEG_N9:
                                    setNextExpectedSegment(RecordId.EDI_838_SEG_LX, RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;
                        case EDI997:
                            switch (pRecordTemplate.getId()) {
                                case RecordId.EDI_SEG_ST:
                                    setNextExpectedSegment(RecordId.EDI_997_SEG_AK1);
                                    break;

                                case RecordId.EDI_997_SEG_AK1:
                                    setNextExpectedSegment(RecordId.EDI_997_SEG_AK2);
                                    break;

                                case RecordId.EDI_997_SEG_AK2:
                                    if(mFileVersion != null && mFileVersion.equals("004010")){
                                        setNextExpectedSegment(RecordId.EDI_997_SEG_AK5_4010, RecordId.EDI_997_SEG_AK3_4010);
                                    } else {
                                        setNextExpectedSegment(RecordId.EDI_997_SEG_AK5);
                                    }
                                    break;

                                case RecordId.EDI_997_SEG_AK3_4010:
                                    setNextExpectedSegment(RecordId.EDI_997_SEG_AK5_4010, RecordId.EDI_997_SEG_AK4_4010);
                                    break;

                                case RecordId.EDI_997_SEG_AK4_4010:
                                    setNextExpectedSegment(RecordId.EDI_997_SEG_AK5_4010, RecordId.EDI_997_SEG_AK3_4010);
                                    break;

                                case RecordId.EDI_997_SEG_AK5:
                                case RecordId.EDI_997_SEG_AK5_4010:
                                    setNextExpectedSegment(RecordId.EDI_997_SEG_AK2, RecordId.EDI_997_SEG_AK9);
                                    break;

                                case RecordId.EDI_997_SEG_AK9:
                                    setNextExpectedSegment(RecordId.EDI_SEG_SE);
                                    break;

                                default:
                                    mContinueValidation = false;
                                    break;
                            }
                            break;

                        default:
                            mContinueValidation = false;
                            break;
                    }
                    break;
            }
        }
    }
}
