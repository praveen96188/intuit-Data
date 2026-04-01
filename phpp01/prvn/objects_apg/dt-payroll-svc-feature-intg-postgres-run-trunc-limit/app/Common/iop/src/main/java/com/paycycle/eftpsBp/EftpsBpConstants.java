/**
 * EftpsBpConstants.java
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
 * The Electronic Federal Tax Payment System Bulk Provider Constants
 */
public interface EftpsBpConstants {
    //
    // Acknowledgement (997) file func code
    //
    public static final String EDI_GS_FUNC_CODE_997 = "FA"; // General purpose acknowledgement file

    //
    // These func codes are for EFTPS files we create (and the TFA will ack/997 us back)
    //
    public static final String EDI_GS_FUNC_CODE_813 = "TF"; // Payment file
    public static final String EDI_GS_FUNC_CODE_821 = "FR"; // Payment Forecast file
    public static final String EDI_GS_FUNC_CODE_838 = "TD"; // Enrollment file

    //
    // These func codes are for EFTPS files we receive from the TFA (and we need to create an ack/997 file back)
    //
    public static final String EDI_GS_FUNC_CODE_151 = "TA"; // Payment Acknowledgement file
    public static final String EDI_GS_FUNC_CODE_824 = "AG"; // Enrollment Accept/Reject file
    public static final String EDI_GS_FUNC_CODE_826 = "TI"; // Same-Day Payment Confirmation file
    public static final String EDI_GS_FUNC_CODE_827 = "FR"; // Payment Return file

    //
    // Error codes for rejected enrollments (sent by TFA within the 824 file)
    //
    public static final int ENROLLMENT_REJECTED_TIN_MISMATCH = 5;
    public static final int ENROLLMENT_REJECTED_NAME_MISMATCH = 6;
    public static final int ENROLLMENT_REJECTED_TIN_BLANK_OR_INVALID = 13;
}
