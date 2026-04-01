/**
 * RecordId.java
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

package com.paycycle.eftpsBp;

/**
 * The Electronic Federal Tax Payment System Record IDs
 */
public interface RecordId {
    public static final String EDI_SEGMENT_SEP = "\\";

    // Generic Segment (just record, no data fields)
    public static final int EDI_SEG_GENERIC = 1;

    // Common segments for all EDI files
    public static final int EDI_SEG_ISA = 1001;
    public static final int EDI_SEG_IEA = 1002;
    public static final int EDI_SEG_GS  = 1003;
    public static final int EDI_SEG_GE  = 1004;
    public static final int EDI_SEG_ST  = 1005;
    public static final int EDI_SEG_SE  = 1006;

    // EDI Version 004010 ISA segment
    public static final int EDI_SEG_ISA_4010 = 1007;    

    // 997 file segments
    public static final int EDI_997_SEG_AK1 = 99701;
    public static final int EDI_997_SEG_AK2 = 99702;
    public static final int EDI_997_SEG_AK5 = 99703;
    public static final int EDI_997_SEG_AK9 = 99704;

    // 997 file segments - EDI Version 004010
    public static final int EDI_997_SEG_AK3_4010 = 997401005;
    public static final int EDI_997_SEG_AK4_4010 = 997401006;
    public static final int EDI_997_SEG_AK5_4010 = 997401007;

    // 838 file segments
    public static final int EDI_838_SEG_BTP = 83801;
    public static final int EDI_838_SEG_PER = 83802;
    public static final int EDI_838_SEG_LX  = 83803;
    public static final int EDI_838_SEG_N1  = 83804;
    public static final int EDI_838_SEG_N3  = 83805;
    public static final int EDI_838_SEG_N4  = 83806;
    public static final int EDI_838_SEG_N9  = 83807;

    // 824 file segments
    public static final int EDI_824_SEG_BGN = 82401;
    public static final int EDI_824_SEG_OTI = 82402;
    public static final int EDI_824_SEG_REF = 82403;

    // 813 file segments  version -- 003050
    public static final int EDI_813_SEG_BTI       = 81301;
    public static final int EDI_813_SEG_DTM       = 81302;
    public static final int EDI_813_SEG_OUTER_TIA = 81303;
    public static final int EDI_813_SEG_OUTER_REF = 81304;
    public static final int EDI_813_SEG_BPR       = 81305;
    public static final int EDI_813_SEG_TFS       = 81306;
    public static final int EDI_813_SEG_INNER_REF = 81307;
    public static final int EDI_813_SEG_FGS       = 81308;
    public static final int EDI_813_SEG_INNER_TIA = 81309;

    // 151 file segments
    public static final int EDI_151_SEG_BTA = 15101;
    public static final int EDI_151_SEG_BTI = 15102;
    public static final int EDI_151_SEG_TFS = 15103;

    // 826 file segments
    public static final int EDI_826_SEG_BTI = 82601;
    public static final int EDI_826_SEG_TFS = 82602;

    // 827 file segments
    public static final int EDI_827_SEG_RIC = 82701;
    public static final int EDI_827_SEG_REF = 82702;

    // 821 file segments
    public static final int EDI_821_SEG_B2A = 82101;
    public static final int EDI_821_SEG_DTM = 82102;
    public static final int EDI_821_SEG_TRN = 82103;
    public static final int EDI_821_SEG_ENT = 82104;
    public static final int EDI_821_SEG_ACT = 82105;
    public static final int EDI_821_SEG_FIR = 82106;

    // 813 file segments Version - 004010
    public static final int EDI_813_SEG_BTI_4010 = 81340101;
    public static final int EDI_813_SEG_DTM_4010 = 81340102;
    public static final int EDI_813_SEG_OUTER_TIA_4010 = 81340103;
    public static final int EDI_813_SEG_REF_4010 = 81340104;
    public static final int EDI_813_SEG_BPR_4010 = 81340105;
    public static final int EDI_813_SEG_TFS_4010 = 81340106;
    public static final int EDI_813_SEG_N1_4010  = 81340107;
    public static final int EDI_813_SEG_N2_4010  = 81340108;
    public static final int EDI_813_SEG_N3_4010  = 81340109;
    public static final int EDI_813_SEG_N4_4010  = 81340110;
    public static final int EDI_813_SEG_INNER_TIA_4010 = 81340111;

    // 151 file segments Version - 004010
    public static final int EDI_151_SEG_BTA_4010 = 151401001;
    public static final int EDI_151_SEG_BTI_4010 = 151401002;
    public static final int EDI_151_SEG_TFS_4010 = 151401003;
    public static final int EDI_151_SEG_OUTER_REF_4010 = 151401004;
    public static final int EDI_151_SEG_INNER_REF_4010 = 151401005;
    public static final int EDI_151_SEG_PBI_4010 = 151401006;    

}
