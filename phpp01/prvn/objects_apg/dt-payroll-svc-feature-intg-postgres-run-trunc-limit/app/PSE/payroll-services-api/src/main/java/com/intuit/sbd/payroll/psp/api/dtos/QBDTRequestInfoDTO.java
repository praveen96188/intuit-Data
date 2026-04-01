package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 3/4/12
 * Time: 10:07 AM
 */
public class QBDTRequestInfoDTO {
    private int mEmployeeAddCount;
    private SpcfCalendar mEmployeeAddStart;
    private SpcfCalendar mEmployeeAddEnd;
    private int mEmployeeUpdateCount;
    private SpcfCalendar mEmployeeUpdateStart;
    private SpcfCalendar mEmployeeUpdateEnd;

    private int mPayrollItemAddCount;
    private SpcfCalendar mPayrollItemAddStart;
    private SpcfCalendar mPayrollItemAddEnd;
    private int mPayrollItemUpdateCount;
    private SpcfCalendar mPayrollItemUpdateStart;
    private SpcfCalendar mPayrollItemUpdateEnd;

    private int mPaycheckAddCount;
    private int mPaycheckUpdateCount;
    private SpcfCalendar mPayrollProcessingStart;
    private SpcfCalendar mPayrollProcessingEnd;

    private int mPayrollTransactionAddCount;
    private SpcfCalendar mPayrollTransactionAddStart;
    private SpcfCalendar mPayrollTransactionAddEnd;
    private int mPayrollTransactionUpdateCount;
    private SpcfCalendar mPayrollTransactionUpdateStart;
    private SpcfCalendar mPayrollTransactionUpdateEnd;

    private int mEmployeeDeleteCount;
    private int mPayrollItemDeleteCount;
    private int mPaycheckDeleteCount;
    private int mPayrollTransactionDeleteCount;
    private SpcfCalendar mDeleteProcessingStart;
    private SpcfCalendar mDeleteProcessingEnd;

    public int getEmployeeAddCount() {
        return mEmployeeAddCount;
    }

    public void setEmployeeAddCount(int pEmployeeAddCount) {
        mEmployeeAddCount = pEmployeeAddCount;
    }

    public SpcfCalendar getEmployeeAddStart() {
        return mEmployeeAddStart;
    }

    public void setEmployeeAddStart(SpcfCalendar pEmployeeAddStart) {
        mEmployeeAddStart = pEmployeeAddStart;
    }

    public SpcfCalendar getEmployeeAddEnd() {
        return mEmployeeAddEnd;
    }

    public void setEmployeeAddEnd(SpcfCalendar pEmployeeAddEnd) {
        mEmployeeAddEnd = pEmployeeAddEnd;
    }

    public int getEmployeeUpdateCount() {
        return mEmployeeUpdateCount;
    }

    public void setEmployeeUpdateCount(int pEmployeeUpdateCount) {
        mEmployeeUpdateCount = pEmployeeUpdateCount;
    }

    public SpcfCalendar getEmployeeUpdateStart() {
        return mEmployeeUpdateStart;
    }

    public void setEmployeeUpdateStart(SpcfCalendar pEmployeeUpdateStart) {
        mEmployeeUpdateStart = pEmployeeUpdateStart;
    }

    public SpcfCalendar getEmployeeUpdateEnd() {
        return mEmployeeUpdateEnd;
    }

    public void setEmployeeUpdateEnd(SpcfCalendar pEmployeeUpdateEnd) {
        mEmployeeUpdateEnd = pEmployeeUpdateEnd;
    }

    public int getPayrollItemAddCount() {
        return mPayrollItemAddCount;
    }

    public void setPayrollItemAddCount(int pPayrollItemAddCount) {
        mPayrollItemAddCount = pPayrollItemAddCount;
    }

    public SpcfCalendar getPayrollItemAddStart() {
        return mPayrollItemAddStart;
    }

    public void setPayrollItemAddStart(SpcfCalendar pPayrollItemAddStart) {
        mPayrollItemAddStart = pPayrollItemAddStart;
    }

    public SpcfCalendar getPayrollItemAddEnd() {
        return mPayrollItemAddEnd;
    }

    public void setPayrollItemAddEnd(SpcfCalendar pPayrollItemAddEnd) {
        mPayrollItemAddEnd = pPayrollItemAddEnd;
    }

    public int getPayrollItemUpdateCount() {
        return mPayrollItemUpdateCount;
    }

    public void setPayrollItemUpdateCount(int pPayrollItemUpdateCount) {
        mPayrollItemUpdateCount = pPayrollItemUpdateCount;
    }

    public SpcfCalendar getPayrollItemUpdateStart() {
        return mPayrollItemUpdateStart;
    }

    public void setPayrollItemUpdateStart(SpcfCalendar pPayrollItemUpdateStart) {
        mPayrollItemUpdateStart = pPayrollItemUpdateStart;
    }

    public SpcfCalendar getPayrollItemUpdateEnd() {
        return mPayrollItemUpdateEnd;
    }

    public void setPayrollItemUpdateEnd(SpcfCalendar pPayrollItemUpdateEnd) {
        mPayrollItemUpdateEnd = pPayrollItemUpdateEnd;
    }

    public int getPaycheckAddCount() {
        return mPaycheckAddCount;
    }

    public void setPaycheckAddCount(int pPaycheckAddCount) {
        mPaycheckAddCount = pPaycheckAddCount;
    }

    public int getPaycheckUpdateCount() {
        return mPaycheckUpdateCount;
    }

    public void setPaycheckUpdateCount(int pPaycheckUpdateCount) {
        mPaycheckUpdateCount = pPaycheckUpdateCount;
    }

    public SpcfCalendar getPayrollProcessingStart() {
        return mPayrollProcessingStart;
    }

    public void setPayrollProcessingStart(SpcfCalendar pPayrollProcessingStart) {
        mPayrollProcessingStart = pPayrollProcessingStart;
    }

    public SpcfCalendar getPayrollProcessingEnd() {
        return mPayrollProcessingEnd;
    }

    public void setPayrollProcessingEnd(SpcfCalendar pPayrollProcessingEnd) {
        mPayrollProcessingEnd = pPayrollProcessingEnd;
    }

    public int getPayrollTransactionAddCount() {
        return mPayrollTransactionAddCount;
    }

    public void setPayrollTransactionAddCount(int pPayrollTransactionAddCount) {
        mPayrollTransactionAddCount = pPayrollTransactionAddCount;
    }

    public SpcfCalendar getPayrollTransactionAddStart() {
        return mPayrollTransactionAddStart;
    }

    public void setPayrollTransactionAddStart(SpcfCalendar pPayrollTransactionAddStart) {
        mPayrollTransactionAddStart = pPayrollTransactionAddStart;
    }

    public SpcfCalendar getPayrollTransactionAddEnd() {
        return mPayrollTransactionAddEnd;
    }

    public void setPayrollTransactionAddEnd(SpcfCalendar pPayrollTransactionAddEnd) {
        mPayrollTransactionAddEnd = pPayrollTransactionAddEnd;
    }

    public int getPayrollTransactionUpdateCount() {
        return mPayrollTransactionUpdateCount;
    }

    public void setPayrollTransactionUpdateCount(int pPayrollTransactionUpdateCount) {
        mPayrollTransactionUpdateCount = pPayrollTransactionUpdateCount;
    }

    public SpcfCalendar getPayrollTransactionUpdateStart() {
        return mPayrollTransactionUpdateStart;
    }

    public void setPayrollTransactionUpdateStart(SpcfCalendar pPayrollTransactionUpdateStart) {
        mPayrollTransactionUpdateStart = pPayrollTransactionUpdateStart;
    }

    public SpcfCalendar getPayrollTransactionUpdateEnd() {
        return mPayrollTransactionUpdateEnd;
    }

    public void setPayrollTransactionUpdateEnd(SpcfCalendar pPayrollTransactionUpdateEnd) {
        mPayrollTransactionUpdateEnd = pPayrollTransactionUpdateEnd;
    }

    public int getEmployeeDeleteCount() {
        return mEmployeeDeleteCount;
    }

    public void setEmployeeDeleteCount(int pEmployeeDeleteCount) {
        mEmployeeDeleteCount = pEmployeeDeleteCount;
    }

    public int getPayrollItemDeleteCount() {
        return mPayrollItemDeleteCount;
    }

    public void setPayrollItemDeleteCount(int pPayrollItemDeleteCount) {
        mPayrollItemDeleteCount = pPayrollItemDeleteCount;
    }

    public int getPaycheckDeleteCount() {
        return mPaycheckDeleteCount;
    }

    public void setPaycheckDeleteCount(int pPaycheckDeleteCount) {
        mPaycheckDeleteCount = pPaycheckDeleteCount;
    }

    public int getPayrollTransactionDeleteCount() {
        return mPayrollTransactionDeleteCount;
    }

    public void setPayrollTransactionDeleteCount(int pPayrollTransactionDeleteCount) {
        mPayrollTransactionDeleteCount = pPayrollTransactionDeleteCount;
    }

    public SpcfCalendar getDeleteProcessingStart() {
        return mDeleteProcessingStart;
    }

    public void setDeleteProcessingStart(SpcfCalendar pDeleteProcessingStart) {
        mDeleteProcessingStart = pDeleteProcessingStart;
    }

    public SpcfCalendar getDeleteProcessingEnd() {
        return mDeleteProcessingEnd;
    }

    public void setDeleteProcessingEnd(SpcfCalendar pDeleteProcessingEnd) {
        mDeleteProcessingEnd = pDeleteProcessingEnd;
    }
}
