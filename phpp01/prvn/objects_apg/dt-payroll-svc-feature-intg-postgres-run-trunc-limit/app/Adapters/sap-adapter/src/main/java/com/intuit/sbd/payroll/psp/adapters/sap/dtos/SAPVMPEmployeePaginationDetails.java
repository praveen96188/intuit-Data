package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by arajendradeshpande on 4/24/2017.
 */
public class SAPVMPEmployeePaginationDetails {
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

    private int currentPage;
    private int pageSize;
    private String sortBy;
    private boolean sortDesc;
}
