package com.intuit.sbd.payroll.psp.util;

/**
 * Created by arajendradeshpande on 4/24/2017.
 */
public class VMPEmployeePaginationDetails {
    private int currentPage;
    private int pageSize;
    private String sortBy;
    private boolean sortDesc;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isSortDesc() {
        return sortDesc;
    }

    public void setSortDesc(boolean sortDesc) {
        this.sortDesc = sortDesc;
    }

    public VMPEmployeePaginationDetails(int currentPage, int pageSize, String sortBy, boolean sortDesc) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.sortDesc = sortDesc;
    }
}