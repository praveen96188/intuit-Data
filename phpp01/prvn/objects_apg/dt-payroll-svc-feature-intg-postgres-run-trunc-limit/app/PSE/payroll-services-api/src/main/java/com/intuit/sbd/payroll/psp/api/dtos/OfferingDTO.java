package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jan 31, 2008
 * Time: 12:31:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class OfferingDTO {
    private String id;             // SPCF unique Id of the entity
    private String sku;            // SKU code
    private String name;           // Common name of the offering
    private String description;    // Long description of the offering
    private Boolean isApproved;    // Whether this Offering has been approved by POA and PSO
    private ServiceCode serviceCode;
    private String payrollSubType; //Payroll SubType

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getSKU() {
        return sku;
    }

    public void setSKU(String pSKU) {
        sku = pSKU;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean pIsApproved) {
        isApproved = pIsApproved;
    }

    public ServiceCode getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(ServiceCode pServiceCode) {
        serviceCode = pServiceCode;
    }

    public String getPayrollSubType() {
        return payrollSubType;
    }

    public void setPayrollSubType(String payrollSubType) {
        this.payrollSubType = payrollSubType;
    }

    /**
     * Performs all validations that are common to more than one Process (currently, the create and update processes).
     * @return
     */
    public ProcessResult validate()
    {
        ProcessResult result = new ProcessResult();
        return result;
    }
}
