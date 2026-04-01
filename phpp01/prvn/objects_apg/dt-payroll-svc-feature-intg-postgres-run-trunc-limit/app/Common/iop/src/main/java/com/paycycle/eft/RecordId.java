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

package com.paycycle.eft;

/**
 * The Electronic Federal Tax Payment System Record IDs
 */
public interface RecordId {
    //
    // State Withholding
    //
    public static final int Txp_AL_WH = 11000; // ALABAMA
    public static final int Txp_AK_WH = 11100; // ALASKA
    public static final int Txp_AZ_WH = 11200; // ARIZONA
    public static final int Txp_AR_WH = 11300; // ARKANSAS
    public static final int Txp_CA_WH = 11400; // CALIFORNIA
    public static final int Txp_CO_WH = 11500; // COLORADO
    public static final int Txp_CT_WH = 11600; // CONNECTICUT
    public static final int Txp_DE_WH = 11700; // DELAWARE
    public static final int Txp_FL_WH = 11800; // FLORIDA
    public static final int Txp_GA_WH = 11900; // GEORGIA
    public static final int Txp_HI_WH = 12000; // HAWAII
    public static final int Txp_ID_WH = 12100; // IDAHO
    public static final int Txp_IL_WH = 12200; // ILLINOIS
    public static final int Txp_IN_WH = 12300; // INDIANA
    public static final int Txp_IA_WH = 12400; // IOWA
    public static final int Txp_KS_WH = 12500; // KANSAS
    public static final int Txp_KY_WH = 12600; // KENTUCKY
    public static final int Txp_LA_WH = 12700; // LOUISIANA
    public static final int Txp_ME_WH = 12800; // MAINE
    public static final int Txp_MD_WH = 12900; // MARYLAND
    public static final int Txp_MA_WH = 13000; // MASSACHUSETTS
    public static final int Txp_MI_WH = 13100; // MICHIGAN
    public static final int Txp_MN_WH = 13200; // MINNESOTA
    public static final int Txp_MS_WH = 13300; // MISSISSIPPI
    public static final int Txp_MO_WH = 13400; // MISSOURI
    public static final int Txp_MT_WH = 13500; // MONTANA
    public static final int Txp_NE_WH = 13600; // NEBRASKA
    public static final int Txp_NV_WH = 13700; // NEVADA
    public static final int Txp_NH_WH = 13800; // NEW HAMPSHIRE
    public static final int Txp_NJ_WH = 13900; // NEW JERSEY
    public static final int Txp_NM_WH = 14000; // NEW MEXICO
    public static final int Txp_NY_WH = 14100; // NEW YORK
    public static final int Txp_NC_WH = 14200; // NORTH CAROLINA
    public static final int Txp_ND_WH = 14300; // NORTH DAKOTA
    public static final int Txp_OH_WH = 14400; // OHIO
    public static final int Txp_OK_WH = 14500; // OKLAHOMA
    public static final int Txp_OR_WH = 14600; // OREGON
    public static final int Txp_PA_WH = 14700; // PENNSYLVANIA
    public static final int Txp_RI_WH = 14800; // RHODE ISLAND
    public static final int Txp_SC_WH = 14900; // SOUTH CAROLINA
    public static final int Txp_SD_WH = 15000; // SOUTH DAKOTA
    public static final int Txp_TN_WH = 15100; // TENNESSEE
    public static final int Txp_TX_WH = 15200; // TEXAS
    public static final int Txp_UT_WH = 15300; // UTAH
    public static final int Txp_VT_WH = 15400; // VERMONT
    public static final int Txp_VA_WH = 15500; // VIRGINIA
    public static final int Txp_WA_WH = 15600; // WASHINGTON
    public static final int Txp_WV_WH = 15700; // WEST VIRGINIA
    public static final int Txp_WI_WH = 15800; // WISCONSIN
    public static final int Txp_WY_WH = 15900; // WYOMING

    public static final int Txp_DC_WH = 16000; // DISTRICT OF COLUMBIA
    public static final int Txp_AS_WH = 16100; // AMERICAN SAMOA
    public static final int Txp_FM_WH = 16200; // FEDERATED STATES OF MICRONESIA
    public static final int Txp_GU_WH = 16300; // GUAM
    public static final int Txp_MH_WH = 16400; // MARSHALL ISLANDS
    public static final int Txp_MP_WH = 16500; // NORTHERN MARIANA ISLANDS
    public static final int Txp_PW_WH = 16600; // PALAU
    public static final int Txp_PR_WH = 16700; // PUERTO RICO
    public static final int Txp_VI_WH = 16800; // VIRGIN ISLANDS

    //
    // State UI
    //
    public static final int Txp_AL_UI = 21000; // ALABAMA
    public static final int Txp_AK_UI = 21100; // ALASKA
    public static final int Txp_AZ_UI = 21200; // ARIZONA
    public static final int Txp_AR_UI = 21300; // ARKANSAS
    public static final int Txp_CA_UI = 21400; // CALIFORNIA
    public static final int Txp_CO_UI = 21500; // COLORADO
    public static final int Txp_CT_UI = 21600; // CONNECTICUT
    public static final int Txp_DE_UI = 21700; // DELAWARE
    public static final int Txp_FL_UI = 21800; // FLORIDA
    public static final int Txp_GA_UI = 21900; // GEORGIA
    public static final int Txp_HI_UI = 22000; // HAWAII
    public static final int Txp_ID_UI = 22100; // IDAHO
    public static final int Txp_IL_UI = 22200; // ILLINOIS
    public static final int Txp_IN_UI = 22300; // INDIANA
    public static final int Txp_IA_UI = 22400; // IOWA
    public static final int Txp_KS_UI = 22500; // KANSAS
    public static final int Txp_KY_UI = 22600; // KENTUCKY
    public static final int Txp_LA_UI = 22700; // LOUISIANA
    public static final int Txp_ME_UI = 22800; // MAINE
    public static final int Txp_MD_UI = 22900; // MARYLAND
    public static final int Txp_MA_UI = 23000; // MASSACHUSETTS
    public static final int Txp_MI_UI = 23100; // MICHIGAN
    public static final int Txp_MN_UI = 23200; // MINNESOTA
    public static final int Txp_MS_UI = 23300; // MISSISSIPPI
    public static final int Txp_MO_UI = 23400; // MISSOURI
    public static final int Txp_MT_UI = 23500; // MONTANA
    public static final int Txp_NE_UI = 23600; // NEBRASKA
    public static final int Txp_NV_UI = 23700; // NEVADA
    public static final int Txp_NH_UI = 23800; // NEW HAMPSHIRE
    public static final int Txp_NJ_UI = 23900; // NEW JERSEY
    public static final int Txp_NM_UI = 24000; // NEW MEXICO
    public static final int Txp_NY_UI = 24100; // NEW YORK
    public static final int Txp_NC_UI = 24200; // NORTH CAROLINA
    public static final int Txp_ND_UI = 24300; // NORTH DAKOTA
    public static final int Txp_OH_UI = 24400; // OHIO
    public static final int Txp_OK_UI = 24500; // OKLAHOMA
    public static final int Txp_OR_UI = 24600; // OREGON
    public static final int Txp_PA_UI = 24700; // PENNSYLVANIA
    public static final int Txp_RI_UI = 24800; // RHODE ISLAND
    public static final int Txp_SC_UI = 24900; // SOUTH CAROLINA
    public static final int Txp_SD_UI = 25000; // SOUTH DAKOTA
    public static final int Txp_TN_UI = 25100; // TENNESSEE
    public static final int Txp_TX_UI = 25200; // TEXAS
    public static final int Txp_UT_UI = 25300; // UTAH
    public static final int Txp_VT_UI = 25400; // VERMONT
    public static final int Txp_VA_UI = 25500; // VIRGINIA
    public static final int Txp_WA_UI = 25600; // WASHINGTON
    public static final int Txp_WV_UI = 25700; // WEST VIRGINIA
    public static final int Txp_WI_UI = 25800; // WISCONSIN
    public static final int Txp_WY_UI = 25900; // WYOMING

    public static final int Txp_DC_UI = 26000; // DISTRICT OF COLUMBIA
    public static final int Txp_AS_UI = 26100; // AMERICAN SAMOA
    public static final int Txp_FM_UI = 26200; // FEDERATED STATES OF MICRONESIA
    public static final int Txp_GU_UI = 26300; // GUAM
    public static final int Txp_MH_UI = 26400; // MARSHALL ISLANDS
    public static final int Txp_MP_UI = 26500; // NORTHERN MARIANA ISLANDS
    public static final int Txp_PW_UI = 26600; // PALAU
    public static final int Txp_PR_UI = 26700; // PUERTO RICO
    public static final int Txp_VI_UI = 26800; // VIRGIN ISLANDS

    //
    // Other state taxes
    //
    public static final int Txp_NY_MCT = 34100; // NEW YORK MCT (Metropolitan Commuter Transportation Mobility Tax)
    public static final int Txp_OR_LOCAL = 34600; // OREGON (local taxes - TriMet Transit Tax, Lane County Transit Tax)
    public static final int Txp_MA_PFML = 13000; // NEW YORK MCT (Metropolitan Commuter Transportation Mobility Tax)
    public static final int Txp_CT_PFML = 21600; // CONNECTICUT (Paid Family and Medical Leave Act)
}
