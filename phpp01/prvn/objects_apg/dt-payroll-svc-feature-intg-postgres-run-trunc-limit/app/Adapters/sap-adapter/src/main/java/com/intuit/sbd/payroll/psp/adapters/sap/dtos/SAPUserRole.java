package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: cyoder
 * Date: May 18, 2008
 * Time: 5:23:22 PM
 */
public class SAPUserRole {
    private String description;
    private String name;
    private String roleId;
    private ArrayList<SAPUserOperation> operations; 

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public ArrayList<SAPUserOperation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<SAPUserOperation> operations) {
        this.operations = operations;
    }
}
