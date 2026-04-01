package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceCharge;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 23, 2008
 * Time: 4:59:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserRoleDTO {
    private String roleId; //role id
    private String name; //role name
    private String description; // description
    private String domainId; // domain id
    private List<OperationId> operationIds = new ArrayList<OperationId>(); // List of operation Ids

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public List<OperationId> getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(List<OperationId> operationIds) {
        this.operationIds = operationIds;
    }

    public ProcessResult validate(){
        ProcessResult validationResult = new ProcessResult();
        if (domainId == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.AuthUser, domainId, "DomainId");
        }

        if (roleId  == null) {
            validationResult.getMessages()
                    .RoleIdNotSpecified(EntityName.AuthUser, null);
        }

        if(operationIds == null || operationIds.size() == 0){
            validationResult.getMessages().OperationIdNotSpecified(EntityName.AuthUser, null);
        }

        return validationResult;
    }
}
