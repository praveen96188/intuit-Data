package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:27:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Like<Q, V> extends PropertyOperator<Q, V> {
    public Like(Property<Q, V> pProperty, String pLikeString, boolean pCaseInsensitive) {
        super(pProperty);
        likeString = pLikeString;
        caseInsensitive = pCaseInsensitive;
    }

    @Override
    public <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitLikeExpression(this);
    }

    public String getLikeString() {
        return likeString;
    }

    public void setLikeString(String likeString) {
        this.likeString = likeString;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    private String likeString;
    private boolean caseInsensitive;
}
