/**
 * RecordManager.java
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
package com.paycycle.fixedlen;

import java.util.List;
import java.util.Vector;

/**
 * The base record manager class.
 */
public class RecordManager {
    protected List<RecordListener> mReadListeners = new Vector<RecordListener>();
    protected List<RecordListener> mWriteListeners = new Vector<RecordListener>();

    public RecordManager() {
        setDefaultListeners();
    }

    public void clearListeners() {
        mReadListeners.clear();
        mWriteListeners.clear();
    }

    public void resetListeners() {
        clearListeners();
        setDefaultListeners();
    }

    /**
     * Override to add default listeners
     */
    protected void setDefaultListeners() {
    }

    public void addReadRecordListener(RecordListener pListener) {
        if (!mReadListeners.contains(pListener)) {
            mReadListeners.add(pListener);
        }
    }

    public void addWriteRecordListener(RecordListener pListener) {
        if (!mWriteListeners.contains(pListener)) {
            mWriteListeners.add(pListener);
        }
    }

    public RecordListener removeReadRecordListener(RecordListener pListener) {
        if ((pListener != null) && mReadListeners.contains(pListener)) {
            mReadListeners.remove(pListener);
        }

        return pListener;
    }

    public RecordListener removeWriteRecordListener(RecordListener pListener) {
        if ((pListener != null) && mWriteListeners.contains(pListener)) {
            mWriteListeners.remove(pListener);
        }

        return pListener;
    }

    public void notifyReadRecordCreated(RecordTemplate pRecordTemplate) {
        for (RecordListener listener : mReadListeners) {
            listener.recordCreated(pRecordTemplate);
        }
    }

    public void notifyWriteRecordCreated(RecordTemplate pRecordTemplate) {
        for (RecordListener listener : mWriteListeners) {
            listener.recordCreated(pRecordTemplate);
        }
    }
}
