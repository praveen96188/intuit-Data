package com.intuit.ems.psp.adapters.dataadapter.dto;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by ajhawar on 10/20/2015.
 */
@XmlRootElement
public class AuthRole {
    private String roleId;
    private String roleName;
    private String roleDescription;
    private String authRoleSeq;
    private Date modifiedDate;

    public AuthRole() {
    }

    public AuthRole(String pAuthRoleSeq, String pRoleId, String pRoleName, String pRoleDescription, Date pModifiedDate) {
        roleId = pRoleId;
        roleName = pRoleName;
        roleDescription = pRoleDescription;
        authRoleSeq = pAuthRoleSeq;
        modifiedDate = pModifiedDate;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String pRoleId) {
        this.roleId = pRoleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String pRoleName) {
        this.roleName = pRoleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String pRoleDescription) {
        this.roleDescription = pRoleDescription;
    }

    public String getAuthRoleSeq() {
        return authRoleSeq;
    }

    public void setAuthRoleSeq(String pAuthRoleSeq) {
        this.authRoleSeq = pAuthRoleSeq;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        this.modifiedDate = pModifiedDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AuthRole) ){
            return false;
        }
        AuthRole authRole = (AuthRole) obj;
        return StringUtils.equals(this.authRoleSeq, authRole.getAuthRoleSeq());
    }

    @Override
    public int hashCode(){
        int hashCode = 0;
        if(StringUtils.isNotEmpty(authRoleSeq)){
            SpcfUniqueId spcfUniqueId = SpcfUniqueId.createInstance(authRoleSeq);
            hashCode = spcfUniqueId.hashCode();
        }
        return hashCode;
    }

}
