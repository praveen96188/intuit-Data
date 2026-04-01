package com.intuit.sbd.payroll.psp.bedl;

import java.util.ArrayList;
import java.util.List;

/**
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:42:30 PM
  */
public class BedlFilterDefintionProperty {

    private String filterDefName;
    private List<FilterParam> filterParams;

    public BedlFilterDefintionProperty(String filterDefName, List<FilterParam> filterParams) {
        this.filterDefName = filterDefName;
        this.filterParams = filterParams;
    }

    public String getFilterDefName() {
        return filterDefName;
    }

    public void setFilterDefName(String filterDefName) {
        this.filterDefName = filterDefName;
    }

    public List<FilterParam> getFilterParams() {
        return filterParams;
    }

    public void setFilterParams(List<FilterParam> filterParams) {
        this.filterParams = filterParams;
    }

    public static class FilterParam {
        private String name;
        private String type;

        public FilterParam(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
