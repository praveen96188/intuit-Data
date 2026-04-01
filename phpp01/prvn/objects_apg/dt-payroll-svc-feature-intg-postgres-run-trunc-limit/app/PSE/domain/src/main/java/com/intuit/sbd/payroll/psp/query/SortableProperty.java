package com.intuit.sbd.payroll.psp.query;

public interface SortableProperty<Q, V>  {
    SortableProperty<Q, V> Descending();
    boolean isDescending();
}