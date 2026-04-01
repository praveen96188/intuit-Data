/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	1.0
 *
 * --------------------------------------------------------------------------
 */

package com.intuit.sbd.payroll.psp.domainsecondary.entitybase;

import com.intuit.sbd.payroll.psp.domainsecondary.QbdtRequestInfo;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.sbd.payroll.psp.domainsecondary.query.SourceSystemTransmissionExpression;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class BaseQbdtRequestInfo extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // EmployeeAddCount
    //
    private int mEmployeeAddCount = 0;

    public void setEmployeeAddCount(int pEmployeeAddCount)
    {
        mEmployeeAddCount = pEmployeeAddCount;
    }

    public int getEmployeeAddCount()
    {
    	return mEmployeeAddCount;
    }

    //
    // EmployeeUpdateCount
    //
    private int mEmployeeUpdateCount = 0;

    public void setEmployeeUpdateCount(int pEmployeeUpdateCount)
    {
        mEmployeeUpdateCount = pEmployeeUpdateCount;
    }

    public int getEmployeeUpdateCount()
    {
    	return mEmployeeUpdateCount;
    }

    //
    // EmployeeUpdateStart
    //
    private SpcfCalendar mEmployeeUpdateStart = null;

    public void setEmployeeUpdateStart(SpcfCalendar pEmployeeUpdateStart)
    {
        mEmployeeUpdateStart = pEmployeeUpdateStart;
    }

    public SpcfCalendar getEmployeeUpdateStart()
    {
    	return mEmployeeUpdateStart;
    }

    //
    // EmployeeAddStart
    //
    private SpcfCalendar mEmployeeAddStart = null;

    public void setEmployeeAddStart(SpcfCalendar pEmployeeAddStart)
    {
        mEmployeeAddStart = pEmployeeAddStart;
    }

    public SpcfCalendar getEmployeeAddStart()
    {
    	return mEmployeeAddStart;
    }

    //
    // PaycheckAddCount
    //
    private int mPaycheckAddCount = 0;

    public void setPaycheckAddCount(int pPaycheckAddCount)
    {
        mPaycheckAddCount = pPaycheckAddCount;
    }

    public int getPaycheckAddCount()
    {
    	return mPaycheckAddCount;
    }

    //
    // PaycheckUpdateCount
    //
    private int mPaycheckUpdateCount = 0;

    public void setPaycheckUpdateCount(int pPaycheckUpdateCount)
    {
        mPaycheckUpdateCount = pPaycheckUpdateCount;
    }

    public int getPaycheckUpdateCount()
    {
    	return mPaycheckUpdateCount;
    }

    //
    // PayrollProcessingStart
    //
    private SpcfCalendar mPayrollProcessingStart = null;

    public void setPayrollProcessingStart(SpcfCalendar pPayrollProcessingStart)
    {
        mPayrollProcessingStart = pPayrollProcessingStart;
    }

    public SpcfCalendar getPayrollProcessingStart()
    {
    	return mPayrollProcessingStart;
    }

    //
    // PayrollItemAddCount
    //
    private int mPayrollItemAddCount = 0;

    public void setPayrollItemAddCount(int pPayrollItemAddCount)
    {
        mPayrollItemAddCount = pPayrollItemAddCount;
    }

    public int getPayrollItemAddCount()
    {
    	return mPayrollItemAddCount;
    }

    //
    // PayrollItemUpdateCount
    //
    private int mPayrollItemUpdateCount = 0;

    public void setPayrollItemUpdateCount(int pPayrollItemUpdateCount)
    {
        mPayrollItemUpdateCount = pPayrollItemUpdateCount;
    }

    public int getPayrollItemUpdateCount()
    {
    	return mPayrollItemUpdateCount;
    }

    //
    // PayrollItemUpdateStart
    //
    private SpcfCalendar mPayrollItemUpdateStart = null;

    public void setPayrollItemUpdateStart(SpcfCalendar pPayrollItemUpdateStart)
    {
        mPayrollItemUpdateStart = pPayrollItemUpdateStart;
    }

    public SpcfCalendar getPayrollItemUpdateStart()
    {
    	return mPayrollItemUpdateStart;
    }

    //
    // PayrollItemDeleteCount
    //
    private int mPayrollItemDeleteCount = 0;

    public void setPayrollItemDeleteCount(int pPayrollItemDeleteCount)
    {
        mPayrollItemDeleteCount = pPayrollItemDeleteCount;
    }

    public int getPayrollItemDeleteCount()
    {
    	return mPayrollItemDeleteCount;
    }

    //
    // PayrollProcessingEnd
    //
    private SpcfCalendar mPayrollProcessingEnd = null;

    public void setPayrollProcessingEnd(SpcfCalendar pPayrollProcessingEnd)
    {
        mPayrollProcessingEnd = pPayrollProcessingEnd;
    }

    public SpcfCalendar getPayrollProcessingEnd()
    {
    	return mPayrollProcessingEnd;
    }

    //
    // PayrollItemAddEnd
    //
    private SpcfCalendar mPayrollItemAddEnd = null;

    public void setPayrollItemAddEnd(SpcfCalendar pPayrollItemAddEnd)
    {
        mPayrollItemAddEnd = pPayrollItemAddEnd;
    }

    public SpcfCalendar getPayrollItemAddEnd()
    {
    	return mPayrollItemAddEnd;
    }

    //
    // PayrollItemUpdateEnd
    //
    private SpcfCalendar mPayrollItemUpdateEnd = null;

    public void setPayrollItemUpdateEnd(SpcfCalendar pPayrollItemUpdateEnd)
    {
        mPayrollItemUpdateEnd = pPayrollItemUpdateEnd;
    }

    public SpcfCalendar getPayrollItemUpdateEnd()
    {
    	return mPayrollItemUpdateEnd;
    }

    //
    // PayrollTransactionAddEnd
    //
    private SpcfCalendar mPayrollTransactionAddEnd = null;

    public void setPayrollTransactionAddEnd(SpcfCalendar pPayrollTransactionAddEnd)
    {
        mPayrollTransactionAddEnd = pPayrollTransactionAddEnd;
    }

    public SpcfCalendar getPayrollTransactionAddEnd()
    {
    	return mPayrollTransactionAddEnd;
    }

    //
    // PayrollTransactionUpdateEnd
    //
    private SpcfCalendar mPayrollTransactionUpdateEnd = null;

    public void setPayrollTransactionUpdateEnd(SpcfCalendar pPayrollTransactionUpdateEnd)
    {
        mPayrollTransactionUpdateEnd = pPayrollTransactionUpdateEnd;
    }

    public SpcfCalendar getPayrollTransactionUpdateEnd()
    {
    	return mPayrollTransactionUpdateEnd;
    }

    //
    // EmployeeDeleteCount
    //
    private int mEmployeeDeleteCount = 0;

    public void setEmployeeDeleteCount(int pEmployeeDeleteCount)
    {
        mEmployeeDeleteCount = pEmployeeDeleteCount;
    }

    public int getEmployeeDeleteCount()
    {
    	return mEmployeeDeleteCount;
    }

    //
    // PayrollTransactionDeleteCount
    //
    private int mPayrollTransactionDeleteCount = 0;

    public void setPayrollTransactionDeleteCount(int pPayrollTransactionDeleteCount)
    {
        mPayrollTransactionDeleteCount = pPayrollTransactionDeleteCount;
    }

    public int getPayrollTransactionDeleteCount()
    {
    	return mPayrollTransactionDeleteCount;
    }

    //
    // DeleteProcessingStart
    //
    private SpcfCalendar mDeleteProcessingStart = null;

    public void setDeleteProcessingStart(SpcfCalendar pDeleteProcessingStart)
    {
        mDeleteProcessingStart = pDeleteProcessingStart;
    }

    public SpcfCalendar getDeleteProcessingStart()
    {
    	return mDeleteProcessingStart;
    }

    //
    // DeleteProcessingEnd
    //
    private SpcfCalendar mDeleteProcessingEnd = null;

    public void setDeleteProcessingEnd(SpcfCalendar pDeleteProcessingEnd)
    {
        mDeleteProcessingEnd = pDeleteProcessingEnd;
    }

    public SpcfCalendar getDeleteProcessingEnd()
    {
    	return mDeleteProcessingEnd;
    }

    //
    // PayrollItemAddStart
    //
    private SpcfCalendar mPayrollItemAddStart = null;

    public void setPayrollItemAddStart(SpcfCalendar pPayrollItemAddStart)
    {
        mPayrollItemAddStart = pPayrollItemAddStart;
    }

    public SpcfCalendar getPayrollItemAddStart()
    {
    	return mPayrollItemAddStart;
    }

    //
    // PayrollTransactionAddCount
    //
    private int mPayrollTransactionAddCount = 0;

    public void setPayrollTransactionAddCount(int pPayrollTransactionAddCount)
    {
        mPayrollTransactionAddCount = pPayrollTransactionAddCount;
    }

    public int getPayrollTransactionAddCount()
    {
    	return mPayrollTransactionAddCount;
    }

    //
    // PayrollTransactionUpdateCount
    //
    private int mPayrollTransactionUpdateCount = 0;

    public void setPayrollTransactionUpdateCount(int pPayrollTransactionUpdateCount)
    {
        mPayrollTransactionUpdateCount = pPayrollTransactionUpdateCount;
    }

    public int getPayrollTransactionUpdateCount()
    {
    	return mPayrollTransactionUpdateCount;
    }

    //
    // PayrollTransactionUpdateStart
    //
    private SpcfCalendar mPayrollTransactionUpdateStart = null;

    public void setPayrollTransactionUpdateStart(SpcfCalendar pPayrollTransactionUpdateStart)
    {
        mPayrollTransactionUpdateStart = pPayrollTransactionUpdateStart;
    }

    public SpcfCalendar getPayrollTransactionUpdateStart()
    {
    	return mPayrollTransactionUpdateStart;
    }

    //
    // PayrollTransactionAddStart
    //
    private SpcfCalendar mPayrollTransactionAddStart = null;

    public void setPayrollTransactionAddStart(SpcfCalendar pPayrollTransactionAddStart)
    {
        mPayrollTransactionAddStart = pPayrollTransactionAddStart;
    }

    public SpcfCalendar getPayrollTransactionAddStart()
    {
    	return mPayrollTransactionAddStart;
    }

    //
    // EmployeeAddEnd
    //
    private SpcfCalendar mEmployeeAddEnd = null;

    public void setEmployeeAddEnd(SpcfCalendar pEmployeeAddEnd)
    {
        mEmployeeAddEnd = pEmployeeAddEnd;
    }

    public SpcfCalendar getEmployeeAddEnd()
    {
    	return mEmployeeAddEnd;
    }

    //
    // EmployeeUpdateEnd
    //
    private SpcfCalendar mEmployeeUpdateEnd = null;

    public void setEmployeeUpdateEnd(SpcfCalendar pEmployeeUpdateEnd)
    {
        mEmployeeUpdateEnd = pEmployeeUpdateEnd;
    }

    public SpcfCalendar getEmployeeUpdateEnd()
    {
    	return mEmployeeUpdateEnd;
    }

    //
    // PaycheckDeleteCount
    //
    private int mPaycheckDeleteCount = 0;

    public void setPaycheckDeleteCount(int pPaycheckDeleteCount)
    {
        mPaycheckDeleteCount = pPaycheckDeleteCount;
    }

    public int getPaycheckDeleteCount()
    {
    	return mPaycheckDeleteCount;
    }
    //
    // SourceSystemTransmission
    //
    private SourceSystemTransmission mSourceSystemTransmission = null;

    public void setSourceSystemTransmission(SourceSystemTransmission pSourceSystemTransmission)
    {
    	mSourceSystemTransmission = pSourceSystemTransmission;
    }

    public SourceSystemTransmission getSourceSystemTransmission()
    {
    	return mSourceSystemTransmission;
    }



    // PSP query support
    public static final ScalarProperty<QbdtRequestInfo, Integer> EmployeeAddCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "EmployeeAddCount");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> EmployeeUpdateCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "EmployeeUpdateCount");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> EmployeeUpdateStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "EmployeeUpdateStart");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> EmployeeAddStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "EmployeeAddStart");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PaycheckAddCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PaycheckAddCount");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PaycheckUpdateCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PaycheckUpdateCount");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollProcessingStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollProcessingStart");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PayrollItemAddCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PayrollItemAddCount");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PayrollItemUpdateCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PayrollItemUpdateCount");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollItemUpdateStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollItemUpdateStart");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PayrollItemDeleteCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PayrollItemDeleteCount");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollProcessingEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollProcessingEnd");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollItemAddEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollItemAddEnd");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollItemUpdateEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollItemUpdateEnd");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollTransactionAddEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollTransactionAddEnd");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollTransactionUpdateEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollTransactionUpdateEnd");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> EmployeeDeleteCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "EmployeeDeleteCount");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PayrollTransactionDeleteCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PayrollTransactionDeleteCount");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> DeleteProcessingStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "DeleteProcessingStart");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> DeleteProcessingEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "DeleteProcessingEnd");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollItemAddStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollItemAddStart");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PayrollTransactionAddCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PayrollTransactionAddCount");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PayrollTransactionUpdateCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PayrollTransactionUpdateCount");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollTransactionUpdateStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollTransactionUpdateStart");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> PayrollTransactionAddStart() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "PayrollTransactionAddStart");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> EmployeeAddEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "EmployeeAddEnd");};
    public static final ScalarProperty<QbdtRequestInfo, SpcfCalendar> EmployeeUpdateEnd() {return new ScalarProperty<QbdtRequestInfo, SpcfCalendar>(null, "EmployeeUpdateEnd");};
    public static final ScalarProperty<QbdtRequestInfo, Integer> PaycheckDeleteCount() {return new ScalarProperty<QbdtRequestInfo, Integer>(null, "PaycheckDeleteCount");};
    public static final SourceSystemTransmissionExpression<QbdtRequestInfo> SourceSystemTransmission() {return new SourceSystemTransmissionExpression<QbdtRequestInfo>(null, "SourceSystemTransmission", false);};
}