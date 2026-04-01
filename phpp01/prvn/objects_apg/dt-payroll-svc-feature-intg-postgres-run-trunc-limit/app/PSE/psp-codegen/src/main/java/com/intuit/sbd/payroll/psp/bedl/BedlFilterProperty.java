package com.intuit.sbd.payroll.psp.bedl;

import java.util.List;

/**
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:42:30 PM
  */
public class BedlFilterProperty {

    private String filterName;
    private String condition;

    public BedlFilterProperty(String filterName, String condition) {
        this.filterName = filterName;
        this.condition = condition;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
