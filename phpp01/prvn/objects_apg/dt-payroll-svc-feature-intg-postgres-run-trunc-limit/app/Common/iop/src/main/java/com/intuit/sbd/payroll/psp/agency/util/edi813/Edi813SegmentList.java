package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.paycycle.fixedlen.RecordTemplate;

import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:43:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813SegmentList {
    private Stack<Edi813StSegment> mStStack = new Stack<Edi813StSegment>();

    public boolean isEmpty() {
        return mStStack.isEmpty();
    }

    public Stack<Edi813StSegment> getStStack() {
        return mStStack;
    }

    public void addStSegment(Edi813StSegment pStSegment) {
        mStStack.push(pStSegment);
    }

    public void addStSegment(RecordTemplate pTemplate) {
        addStSegment(new Edi813StSegment(pTemplate));
    }

    public void setBtiSegment(RecordTemplate pTemplate) {
        mStStack.peek().setBtiSegment(pTemplate);
    }

    public void setDtmSegment(RecordTemplate pTemplate) {
        mStStack.peek().setDtmSegment(pTemplate);
    }

    public void setTiaSegment(RecordTemplate pTemplate) {
        mStStack.peek().setTiaSegment(pTemplate);
    }

    public void setRefSegment(RecordTemplate pTemplate) {
        mStStack.peek().setRefSegment(pTemplate);
    }

    public void setBprSegment(RecordTemplate pTemplate) {
        mStStack.peek().setBprSegment(pTemplate);
    }

    public void addTfsSegment(RecordTemplate pTemplate) {
        mStStack.peek().addTfsSegment(pTemplate);
    }

    public void setTfsRefSegment(RecordTemplate pTemplate) {
        mStStack.peek().peekTfsSegment().setRefSegment(pTemplate);
    }

    public void addTfsTiaSegment(RecordTemplate pTemplate) {
        mStStack.peek().peekTfsSegment().addTiaSegment(pTemplate);
    }
}
