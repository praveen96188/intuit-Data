/**
 * FieldType.java
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

package com.paycycle.gui;

/**
 * @author Kin-Hong Wong
 * @version initial
 */
public interface FieldType {
    public static final int INPUT_NONE = 0;
    public static final int INPUT_TEXT = 1;
    public static final int INPUT_SELECT = 2;
    public static final int INPUT_CHECKBOX = 3;
    public static final int INPUT_RADIO = 4;
    public static final int INPUT_FILE = 5;
    public static final int INPUT_HIDDEN = 6;
}
