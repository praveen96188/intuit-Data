/**
 * FieldId.java
 *
 * Copyright (c) 2007 PayCycle, Inc. All Rights Reserved.
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
 * The Electronic Federal Tax Payment System Bulk Provider Field IDs.
 */
public interface FieldId {
    public static final String EDI_FIELD_SEP = "~";
    public static final int SEGMENT_HEADER = 10;
    public static final int SEGMENT_SEPARATOR = 100;

    // EDI_SEG_ISA fields
    public static final int EDI_SEG_ISA_HEADER = 1000;
    public static final int EDI_SEG_ISA01 = 1001;
    public static final int EDI_SEG_ISA02 = 1002;
    public static final int EDI_SEG_ISA03 = 1003;
    public static final int EDI_SEG_ISA04 = 1004;
    public static final int EDI_SEG_ISA05 = 1005;
    public static final int EDI_SEG_ISA06 = 1006;
    public static final int EDI_SEG_ISA07 = 1007;
    public static final int EDI_SEG_ISA08 = 1008;
    public static final int EDI_SEG_ISA09 = 1009;
    public static final int EDI_SEG_ISA10 = 1010;
    public static final int EDI_SEG_ISA11 = 1011;
    public static final int EDI_SEG_ISA12 = 1012;
    public static final int EDI_SEG_ISA13 = 1013;
    public static final int EDI_SEG_ISA14 = 1014;
    public static final int EDI_SEG_ISA15 = 1015;
    public static final int EDI_SEG_ISA16 = 1016;

    // EDI_SEG_IEA fields
    public static final int EDI_SEG_IEA_HEADER = 1100;
    public static final int EDI_SEG_IEA01 = 1101;
    public static final int EDI_SEG_IEA02 = 1102;

    // EDI_SEG_GS fields
    public static final int EDI_SEG_GS_HEADER = 1200;
    public static final int EDI_SEG_GS01 = 1201;
    public static final int EDI_SEG_GS02 = 1202;
    public static final int EDI_SEG_GS03 = 1203;
    public static final int EDI_SEG_GS04 = 1204;
    public static final int EDI_SEG_GS05 = 1205;
    public static final int EDI_SEG_GS06 = 1206;
    public static final int EDI_SEG_GS07 = 1207;
    public static final int EDI_SEG_GS08 = 1208;

    // EDI_SEG_GE fields
    public static final int EDI_SEG_GE_HEADER = 1300;
    public static final int EDI_SEG_GE01 = 1301;
    public static final int EDI_SEG_GE02 = 1302;

    // EDI_SEG_ST fields
    public static final int EDI_SEG_ST_HEADER = 1400;
    public static final int EDI_SEG_ST01 = 1401;
    public static final int EDI_SEG_ST02 = 1402;

    // EDI_SEG_SE fields
    public static final int EDI_SEG_SE_HEADER = 1500;
    public static final int EDI_SEG_SE01 = 1501;
    public static final int EDI_SEG_SE02 = 1502;

    // EDI_997_SEG_AK1 fields
    public static final int EDI_997_SEG_AK1_HEADER = 1600;
    public static final int EDI_997_SEG_AK101 = 1601;
    public static final int EDI_997_SEG_AK102 = 1602;

    // EDI_997_SEG_AK2 fields
    public static final int EDI_997_SEG_AK2_HEADER = 1700;
    public static final int EDI_997_SEG_AK201 = 1701;
    public static final int EDI_997_SEG_AK202 = 1702;

    // EDI_997_SEG_AK5 fields
    public static final int EDI_997_SEG_AK5_HEADER = 1800;
    public static final int EDI_997_SEG_AK501 = 1801;
    public static final int EDI_997_SEG_AK502 = 1802; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK503 = 1803; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK504 = 1804; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK505 = 1805; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK506 = 1806; // not used by EFTPS spec

    // EDI_997_SEG_AK9 fields
    public static final int EDI_997_SEG_AK9_HEADER = 1900;
    public static final int EDI_997_SEG_AK901 = 1901;
    public static final int EDI_997_SEG_AK902 = 1902;
    public static final int EDI_997_SEG_AK903 = 1903;
    public static final int EDI_997_SEG_AK904 = 1904;
    public static final int EDI_997_SEG_AK905 = 1905; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK906 = 1906; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK907 = 1907; // not used by EFTPS spec
    public static final int EDI_997_SEG_AK908 = 1908; // not used by EFTPS spec

    // EDI_838_SEG_BTP fields
    public static final int EDI_838_SEG_BTP_HEADER = 2000;
    public static final int EDI_838_SEG_BTP01 = 2001;
    public static final int EDI_838_SEG_BTP02 = 2002;
    public static final int EDI_838_SEG_BTP03 = 2003;
    public static final int EDI_838_SEG_BTP04 = 2004;
    public static final int EDI_838_SEG_BTP05 = 2005;
    public static final int EDI_838_SEG_BTP06 = 2006; // not used by EFTPS spec
    public static final int EDI_838_SEG_BTP07 = 2007; // not used by EFTPS spec
    public static final int EDI_838_SEG_BTP08 = 2008; // not used by EFTPS spec
    public static final int EDI_838_SEG_BTP09 = 2009; // not used by EFTPS spec
    public static final int EDI_838_SEG_BTP10 = 2010; // not used by EFTPS spec

    // EDI_838_SEG_PER fields
    public static final int EDI_838_SEG_PER_HEADER = 2100;
    public static final int EDI_838_SEG_PER01 = 2101;
    public static final int EDI_838_SEG_PER02 = 2102;
    public static final int EDI_838_SEG_PER03 = 2103;
    public static final int EDI_838_SEG_PER04 = 2104;

    // EDI_838_SEG_LX fields
    public static final int EDI_838_SEG_LX_HEADER = 2200;
    public static final int EDI_838_SEG_LX01 = 2201;

    // EDI_838_SEG_N1 fields
    public static final int EDI_838_SEG_N1_HEADER = 2300;
    public static final int EDI_838_SEG_N101 = 2301;
    public static final int EDI_838_SEG_N102 = 2302;
    public static final int EDI_838_SEG_N103 = 2303;
    public static final int EDI_838_SEG_N104 = 2304;
    public static final int EDI_838_SEG_N105 = 2305; // not used by EFTPS spec
    public static final int EDI_838_SEG_N106 = 2306; // not used by EFTPS spec

    // EDI_838_SEG_N3 fields
    public static final int EDI_838_SEG_N3_HEADER = 2400;
    public static final int EDI_838_SEG_N301 = 2401;
    public static final int EDI_838_SEG_N302 = 2402; // not used by EFTPS spec

    // EDI_838_SEG_N4 fields
    public static final int EDI_838_SEG_N4_HEADER = 2500;
    public static final int EDI_838_SEG_N401 = 2501;
    public static final int EDI_838_SEG_N402 = 2502;
    public static final int EDI_838_SEG_N403 = 2503;
    public static final int EDI_838_SEG_N404 = 2504;
    public static final int EDI_838_SEG_N405 = 2505; // not used by EFTPS spec
    public static final int EDI_838_SEG_N406 = 2506; // not used by EFTPS spec

    // EDI_838_SEG_N9 fields
    public static final int EDI_838_SEG_N9_HEADER = 2600;
    public static final int EDI_838_SEG_N901 = 2601;
    public static final int EDI_838_SEG_N902 = 2602;
    public static final int EDI_838_SEG_N903 = 2603; // not used by EFTPS spec
    public static final int EDI_838_SEG_N904 = 2604; // not used by EFTPS spec
    public static final int EDI_838_SEG_N905 = 2605; // not used by EFTPS spec
    public static final int EDI_838_SEG_N906 = 2606; // not used by EFTPS spec

    // EDI_824_SEG_BGN fields
    public static final int EDI_824_SEG_BGN_HEADER = 2700;
    public static final int EDI_824_SEG_BGN01 = 2701;
    public static final int EDI_824_SEG_BGN02 = 2702;
    public static final int EDI_824_SEG_BGN03 = 2703;
    public static final int EDI_824_SEG_BGN04 = 2704; // not used by EFTPS spec
    public static final int EDI_824_SEG_BGN05 = 2705; // not used by EFTPS spec
    public static final int EDI_824_SEG_BGN06 = 2706;
    public static final int EDI_824_SEG_BGN07 = 2707; // not used by EFTPS spec
    public static final int EDI_824_SEG_BGN08 = 2708;

    // EDI_824_SEG_OTI fields
    public static final int EDI_824_SEG_OTI_HEADER = 2800;
    public static final int EDI_824_SEG_OTI01 = 2801;
    public static final int EDI_824_SEG_OTI02 = 2802;
    public static final int EDI_824_SEG_OTI03 = 2803;
    public static final int EDI_824_SEG_OTI04 = 2804; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI05 = 2805; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI06 = 2806; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI07 = 2807; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI08 = 2808; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI09 = 2809; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI10 = 2810; // not used by EFTPS spec
    public static final int EDI_824_SEG_OTI11 = 2811; // not used by EFTPS spec

    // EDI_824_SEG_REF fields
    public static final int EDI_824_SEG_REF_HEADER = 2900;
    public static final int EDI_824_SEG_REF01 = 2901;
    public static final int EDI_824_SEG_REF02 = 2902;
    public static final int EDI_824_SEG_REF03 = 2903;

    // EDI_813_SEG_BTI fields
    public static final int EDI_813_SEG_BTI_HEADER = 3000;
    public static final int EDI_813_SEG_BTI01 = 3001;
    public static final int EDI_813_SEG_BTI02 = 3002;
    public static final int EDI_813_SEG_BTI03 = 3003;
    public static final int EDI_813_SEG_BTI04 = 3004;
    public static final int EDI_813_SEG_BTI05 = 3005;
    public static final int EDI_813_SEG_BTI06 = 3006; // not used by EFTPS spec
    public static final int EDI_813_SEG_BTI07 = 3007;
    public static final int EDI_813_SEG_BTI08 = 3008;
    public static final int EDI_813_SEG_BTI09 = 3009; // not used by EFTPS spec
    public static final int EDI_813_SEG_BTI10 = 3010; // not used by EFTPS spec
    public static final int EDI_813_SEG_BTI11 = 3011; // not used by EFTPS spec
    public static final int EDI_813_SEG_BTI12 = 3012; // not used by EFTPS spec

    // EDI_813_SEG_DTM fields
    public static final int EDI_813_SEG_DTM_HEADER = 3100;
    public static final int EDI_813_SEG_DTM01 = 3101;
    public static final int EDI_813_SEG_DTM02 = 3102;

    // EDI_813_SEG_OUTER_TIA fields
    public static final int EDI_813_SEG_OUTER_TIA_HEADER = 3200;
    public static final int EDI_813_SEG_OUTER_TIA01 = 3201;
    public static final int EDI_813_SEG_OUTER_TIA02 = 3202;
    public static final int EDI_813_SEG_OUTER_TIA03 = 3203; // not used by EFTPS spec
    public static final int EDI_813_SEG_OUTER_TIA04 = 3204; // not used by EFTPS spec
    public static final int EDI_813_SEG_OUTER_TIA05 = 3205; // not used by EFTPS spec
    public static final int EDI_813_SEG_OUTER_TIA06 = 3206; // not used by EFTPS spec
    public static final int EDI_813_SEG_OUTER_TIA07 = 3207; // not used by EFTPS spec

    // EDI_813_SEG_OUTER_REF fields
    public static final int EDI_813_SEG_OUTER_REF_HEADER = 3300;
    public static final int EDI_813_SEG_OUTER_REF01 = 3301;
    public static final int EDI_813_SEG_OUTER_REF02 = 3302;

    // EDI_813_SEG_BPR fields
    public static final int EDI_813_SEG_BPR_HEADER = 3400;
    public static final int EDI_813_SEG_BPR01 = 3401;
    public static final int EDI_813_SEG_BPR02 = 3402;
    public static final int EDI_813_SEG_BPR03 = 3403;
    public static final int EDI_813_SEG_BPR04 = 3404;
    public static final int EDI_813_SEG_BPR05 = 3405; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR06 = 3406; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR07 = 3407; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR08 = 3408; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR09 = 3409; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR10 = 3410; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR11 = 3411; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR12 = 3412;
    public static final int EDI_813_SEG_BPR13 = 3413;
    public static final int EDI_813_SEG_BPR14 = 3414;
    public static final int EDI_813_SEG_BPR15 = 3415;
    public static final int EDI_813_SEG_BPR16 = 3416;
    public static final int EDI_813_SEG_BPR17 = 3417; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR18 = 3418; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR19 = 3419; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR20 = 3420; // not used by EFTPS spec
    public static final int EDI_813_SEG_BPR21 = 3421; // not used by EFTPS spec

    // EDI_813_SEG_TFS fields
    public static final int EDI_813_SEG_TFS_HEADER = 3500;
    public static final int EDI_813_SEG_TFS01 = 3501;
    public static final int EDI_813_SEG_TFS02 = 3502;
    public static final int EDI_813_SEG_TFS03 = 3503; // not used by EFTPS spec
    public static final int EDI_813_SEG_TFS04 = 3504; // not used by EFTPS spec
    public static final int EDI_813_SEG_TFS05 = 3505;
    public static final int EDI_813_SEG_TFS06 = 3506;
    public static final int EDI_813_SEG_TFS07 = 3507;
    public static final int EDI_813_SEG_TFS08 = 3508;

    // EDI_813_SEG_INNER_REF fields
    public static final int EDI_813_SEG_INNER_REF_HEADER = 3600;
    public static final int EDI_813_SEG_INNER_REF01 = 3601;
    public static final int EDI_813_SEG_INNER_REF02 = 3602;
    public static final int EDI_813_SEG_INNER_REF03 = 3603; // not used by EFTPS spec

    // EDI_813_SEG_FGS fields
    public static final int EDI_813_SEG_FGS_HEADER = 3700;
    public static final int EDI_813_SEG_FGS01 = 3701;

    // EDI_813_SEG_INNER_TIA fields
    public static final int EDI_813_SEG_INNER_TIA_HEADER = 3800;
    public static final int EDI_813_SEG_INNER_TIA01 = 3801;
    public static final int EDI_813_SEG_INNER_TIA02 = 3802;
    public static final int EDI_813_SEG_INNER_TIA03 = 3803; // not used by EFTPS spec
    public static final int EDI_813_SEG_INNER_TIA04 = 3804; // not used by EFTPS spec
    public static final int EDI_813_SEG_INNER_TIA05 = 3805; // not used by EFTPS spec
    public static final int EDI_813_SEG_INNER_TIA06 = 3806; // not used by EFTPS spec
    public static final int EDI_813_SEG_INNER_TIA07 = 3807; // not used by EFTPS spec

    // EDI_151_SEG_BTA fields
    public static final int EDI_151_SEG_BTA_HEADER = 3900;
    public static final int EDI_151_SEG_BTA01 = 3901;
    public static final int EDI_151_SEG_BTA02 = 3902;
    public static final int EDI_151_SEG_BTA03 = 3903;
    public static final int EDI_151_SEG_BTA04 = 3904;

    // EDI_151_SEG_BTI fields
    public static final int EDI_151_SEG_BTI_HEADER = 4000;
    public static final int EDI_151_SEG_BTI01 = 4001;
    public static final int EDI_151_SEG_BTI02 = 4002;
    public static final int EDI_151_SEG_BTI03 = 4003;
    public static final int EDI_151_SEG_BTI04 = 4004;
    public static final int EDI_151_SEG_BTI05 = 4005; // not used by EFTPS spec
    public static final int EDI_151_SEG_BTI06 = 4006; // not used by EFTPS spec
    public static final int EDI_151_SEG_BTI07 = 4007;
    public static final int EDI_151_SEG_BTI08 = 4008;
    public static final int EDI_151_SEG_BTI09 = 4009; // not used by EFTPS spec
    public static final int EDI_151_SEG_BTI10 = 4010; // not used by EFTPS spec
    public static final int EDI_151_SEG_BTI11 = 4011; // not used by EFTPS spec
    public static final int EDI_151_SEG_BTI12 = 4012; // not used by EFTPS spec

    // EDI_151_SEG_TFS fields
    public static final int EDI_151_SEG_TFS_HEADER = 4100;
    public static final int EDI_151_SEG_TFS01 = 4101;
    public static final int EDI_151_SEG_TFS02 = 4102;
    public static final int EDI_151_SEG_TFS03 = 4103;
    public static final int EDI_151_SEG_TFS04 = 4104;
    public static final int EDI_151_SEG_TFS05 = 4105; // not used by EFTPS spec
    public static final int EDI_151_SEG_TFS06 = 4106; // not used by EFTPS spec
    public static final int EDI_151_SEG_TFS07 = 4107; // not used by EFTPS spec
    public static final int EDI_151_SEG_TFS08 = 4108; // not used by EFTPS spec

    // EDI_826_SEG_BTI fields
    public static final int EDI_826_SEG_BTI_HEADER = 4200;
    public static final int EDI_826_SEG_BTI01 = 4201;
    public static final int EDI_826_SEG_BTI02 = 4202;
    public static final int EDI_826_SEG_BTI03 = 4203;
    public static final int EDI_826_SEG_BTI04 = 4204;
    public static final int EDI_826_SEG_BTI05 = 4205; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI06 = 4206; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI07 = 4207; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI08 = 4208; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI09 = 4209; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI10 = 4210; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI11 = 4211; // not used by EFTPS spec
    public static final int EDI_826_SEG_BTI12 = 4212; // not used by EFTPS spec

    // EDI_826_SEG_TFS fields
    public static final int EDI_826_SEG_TFS_HEADER = 4300;
    public static final int EDI_826_SEG_TFS01 = 4301;
    public static final int EDI_826_SEG_TFS02 = 4302;
    public static final int EDI_826_SEG_TFS03 = 4303;
    public static final int EDI_826_SEG_TFS04 = 4304;
    public static final int EDI_826_SEG_TFS05 = 4305; // not used by EFTPS spec
    public static final int EDI_826_SEG_TFS06 = 4306; // not used by EFTPS spec
    public static final int EDI_826_SEG_TFS07 = 4307; // not used by EFTPS spec
    public static final int EDI_826_SEG_TFS08 = 4308; // not used by EFTPS spec

    // EDI_827_SEG_RIC fields
    public static final int EDI_827_SEG_RIC_HEADER = 4400;
    public static final int EDI_827_SEG_RIC01 = 4401;
    public static final int EDI_827_SEG_RIC02 = 4402;
    public static final int EDI_827_SEG_RIC03 = 4403;
    public static final int EDI_827_SEG_RIC04 = 4404; // not used by EFTPS spec
    public static final int EDI_827_SEG_RIC05 = 4405; // not used by EFTPS spec
    public static final int EDI_827_SEG_RIC06 = 4406; // not used by EFTPS spec
    public static final int EDI_827_SEG_RIC07 = 4407; // not used by EFTPS spec

    // EDI_827_SEG_REF fields
    public static final int EDI_827_SEG_REF_HEADER = 4500;
    public static final int EDI_827_SEG_REF01 = 4501;
    public static final int EDI_827_SEG_REF02 = 4502;

    // EDI_821_SEG_B2A fields
    public static final int EDI_821_SEG_B2A_HEADER = 4600;
    public static final int EDI_821_SEG_B2A01 = 4601;
    public static final int EDI_821_SEG_B2A02 = 4602; // not used by EFTPS spec

    // EDI_821_SEG_DTM fields
    public static final int EDI_821_SEG_DTM_HEADER = 4700;
    public static final int EDI_821_SEG_DTM01 = 4701;
    public static final int EDI_821_SEG_DTM02 = 4702;
    public static final int EDI_821_SEG_DTM03 = 4703; // not used by EFTPS spec
    public static final int EDI_821_SEG_DTM04 = 4704; // not used by EFTPS spec
    public static final int EDI_821_SEG_DTM05 = 4705; // not used by EFTPS spec
    public static final int EDI_821_SEG_DTM06 = 4706; // not used by EFTPS spec
    public static final int EDI_821_SEG_DTM07 = 4707; // not used by EFTPS spec

    // EDI_821_SEG_TRN fields
    public static final int EDI_821_SEG_TRN_HEADER = 4800;
    public static final int EDI_821_SEG_TRN01 = 4801;
    public static final int EDI_821_SEG_TRN02 = 4802;
    public static final int EDI_821_SEG_TRN03 = 4803; // not used by EFTPS spec
    public static final int EDI_821_SEG_TRN04 = 4804; // not used by EFTPS spec

    // EDI_821_SEG_ENT fields
    public static final int EDI_821_SEG_ENT_HEADER = 4900;
    public static final int EDI_821_SEG_ENT01 = 4901;
    public static final int EDI_821_SEG_ENT02 = 4902; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT03 = 4903; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT04 = 4904; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT05 = 4905; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT06 = 4906; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT07 = 4907; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT08 = 4908; // not used by EFTPS spec
    public static final int EDI_821_SEG_ENT09 = 4909; // not used by EFTPS spec

    // EDI_821_SEG_ACT fields
    public static final int EDI_821_SEG_ACT_HEADER = 5000;
    public static final int EDI_821_SEG_ACT01 = 5001;
    public static final int EDI_821_SEG_ACT02 = 5002;
    public static final int EDI_821_SEG_ACT03 = 5003;
    public static final int EDI_821_SEG_ACT04 = 5004;
    public static final int EDI_821_SEG_ACT05 = 5005; // not used by EFTPS spec
    public static final int EDI_821_SEG_ACT06 = 5006; // not used by EFTPS spec
    public static final int EDI_821_SEG_ACT07 = 5007; // not used by EFTPS spec
    public static final int EDI_821_SEG_ACT08 = 5008; // not used by EFTPS spec

    // EDI_821_SEG_FIR fields
    public static final int EDI_821_SEG_FIR_HEADER = 5100;
    public static final int EDI_821_SEG_FIR01 = 5101;
    public static final int EDI_821_SEG_FIR02 = 5102;
    public static final int EDI_821_SEG_FIR03 = 5103;
    public static final int EDI_821_SEG_FIR04 = 5104; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR05 = 5105; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR06 = 5106; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR07 = 5107; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR08 = 5108; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR09 = 5109; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR10 = 5110; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR11 = 5111; // not used by EFTPS spec
    public static final int EDI_821_SEG_FIR12 = 5112; // not used by EFTPS spec


    // EDI_813_SEG_N1 fields
    public static final int EDI_813_SEG_N1_HEADER = 6000;
    public static final int EDI_813_SEG_N101 = 6001;
    public static final int EDI_813_SEG_N102 = 6002;
    public static final int EDI_813_SEG_N103 = 6003;

    // EDI_813_SEG_N2 fields
    public static final int EDI_813_SEG_N2_HEADER = 6004;
    public static final int EDI_813_SEG_N201 = 6005;

    // EDI_813_SEG_N3 fields
    public static final int EDI_813_SEG_N3_HEADER = 6006;
    public static final int EDI_813_SEG_N301 = 6007;
    public static final int EDI_813_SEG_N302 = 6008;

    // EDI_813_SEG_N4 fields
    public static final int EDI_813_SEG_N4_HEADER = 6009;
    public static final int EDI_813_SEG_N401 = 6010;
    public static final int EDI_813_SEG_N402 = 6011;
    public static final int EDI_813_SEG_N403 = 6012;

    //EDI_151_SEG_OUTER_REF_4010 fields
    public static final int EDI_151_SEG_OUTER_REF_HEADER = 7000;
    public static final int EDI_151_SEG_OUTER_REF01 = 7001;
    public static final int EDI_151_SEG_OUTER_REF02 = 7002; 

    //EDI_151_SEG_OUTER_REF_4010 fields
    public static final int EDI_151_SEG_INNER_REF_HEADER = 7003;
    public static final int EDI_151_SEG_INNER_REF01 = 7004;
    public static final int EDI_151_SEG_INNER_REF02 = 7005;
    public static final int EDI_151_SEG_INNER_REF03 = 7006;

    //EDI_151_SEG_PBI_4010 fields
    public static final int EDI_151_SEG_PBI_HEADER = 7007;
    public static final int EDI_151_SEG_PBI01 = 7008;
    public static final int EDI_151_SEG_PBI02 = 7009;
    public static final int EDI_151_SEG_PBI03 = 7010;
    public static final int EDI_151_SEG_PBI04 = 7011;

    // EDI_997_SEG_AK3 fields
    public static final int EDI_997_SEG_AK3_HEADER = 7100;
    public static final int EDI_997_SEG_AK301 = 7101;
    public static final int EDI_997_SEG_AK302 = 7102;
    public static final int EDI_997_SEG_AK303 = 7103;
    public static final int EDI_997_SEG_AK304 = 7104;

    // EDI_997_SEG_AK4 fields
    public static final int EDI_997_SEG_AK4_HEADER = 7200;
    public static final int EDI_997_SEG_AK401 = 7201;
    public static final int EDI_997_SEG_AK402 = 7302;
    public static final int EDI_997_SEG_AK403 = 7303;
    public static final int EDI_997_SEG_AK404 = 7304;

}
