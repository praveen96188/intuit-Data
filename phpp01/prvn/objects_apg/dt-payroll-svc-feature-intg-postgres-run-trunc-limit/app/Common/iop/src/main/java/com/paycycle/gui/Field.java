/**
 * Field.java
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

import com.paycycle.util.Identity;

/**
 * @author Kin-Hong Wong
 * @version initial
 */
public class Field extends Identity {
    protected int mSize = 10;
    protected int mMaxLength;

    public Field() {
    }

    protected Field(final Field pSource) {
        super(pSource);
        mSize = pSource.mSize;
        mMaxLength = pSource.mMaxLength;
    }

    @Override
    public Field clone() {
        return new Field(this);
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(String pSize) {
        mSize = Integer.parseInt(pSize);
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public void setMaxLength(String pMaxLength) {
        mMaxLength = Integer.parseInt(pMaxLength);
    }
}
