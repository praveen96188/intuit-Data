package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:46:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813StSegment {
    private int mStControlNumber;
    private Edi813BtiSegment mBtiSegment;
    private Edi813DtmSegment mDtmSegment;
    private Edi813TiaSegmentOuter mTiaSegment;
    private Edi813RefSegmentOuter mRefSegment;
    private Edi813BprSegment mBprSegment;
    private Stack<Edi813TfsSegment> mTfsStack = new Stack<Edi813TfsSegment>();

    public Edi813StSegment(RecordTemplate pTemplate) {
        mStControlNumber = pTemplate.getFieldInt(FieldId.EDI_SEG_ST02);
    }

    public int getStControlNumber() {
        return mStControlNumber;
    }

    public void setStControlNumber(final int pStControlNumber) {
        mStControlNumber = pStControlNumber;
    }

    public Edi813BtiSegment getBtiSegment() {
        return mBtiSegment;
    }

    public void setBtiSegment(final Edi813BtiSegment pBtiSegment) {
        mBtiSegment = pBtiSegment;
    }

    public void setBtiSegment(RecordTemplate pTemplate) {
        mBtiSegment = new Edi813BtiSegment(pTemplate);
    }

    public Edi813DtmSegment getDtmSegment() {
        return mDtmSegment;
    }

    public void setDtmSegment(final Edi813DtmSegment pDtmSegment) {
        mDtmSegment = pDtmSegment;
    }

    public void setDtmSegment(RecordTemplate pTemplate) {
        mDtmSegment = new Edi813DtmSegment(pTemplate);
    }

    public Edi813TiaSegmentOuter getTiaSegment() {
        return mTiaSegment;
    }

    public void setTiaSegment(final Edi813TiaSegmentOuter pTiaSegment) {
        mTiaSegment = pTiaSegment;
    }

    public void setTiaSegment(RecordTemplate pTemplate) {
        mTiaSegment = new Edi813TiaSegmentOuter(pTemplate);
    }

    public Edi813RefSegmentOuter getRefSegment() {
        return mRefSegment;
    }

    public void setRefSegment(final Edi813RefSegmentOuter pRefSegment) {
        mRefSegment = pRefSegment;
    }

    public void setRefSegment(RecordTemplate pTemplate) {
        mRefSegment = new Edi813RefSegmentOuter(pTemplate);
    }

    public Edi813BprSegment getBprSegment() {
        return mBprSegment;
    }

    public void setBprSegment(final Edi813BprSegment pBprSegment) {
        mBprSegment = pBprSegment;
    }

    public void setBprSegment(RecordTemplate pTemplate) {
        mBprSegment = new Edi813BprSegment(pTemplate);
    }

    public Stack<Edi813TfsSegment> getTfsStack() {
        return mTfsStack;
    }

    public void addTfsSegment(Edi813TfsSegment pTfsSegment) {
        mTfsStack.push(pTfsSegment);
    }

    public void addTfsSegment(RecordTemplate pTemplate) {
        addTfsSegment(new Edi813TfsSegment(pTemplate));
    }

    public Edi813TfsSegment peekTfsSegment() {
        return mTfsStack.peek();
    }
}
