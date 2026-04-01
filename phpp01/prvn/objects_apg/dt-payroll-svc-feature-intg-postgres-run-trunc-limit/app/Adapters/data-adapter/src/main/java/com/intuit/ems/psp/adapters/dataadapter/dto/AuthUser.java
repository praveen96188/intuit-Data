package com.intuit.ems.psp.adapters.dataadapter.dto;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Created by ajhawar on 10/19/2015.
 */
@XmlRootElement
public class AuthUser {
    private String authUserSeq;
    private String corpId;
    private Date modifiedDate;
    private String firstName;
    private String lastName;
    private List<AuthRole> authRoles;

    public AuthUser() {
    }

    public AuthUser(String pAuthUserSeq, String pCorpId, Date pModifiedDate, String pFirstName, String pLastName, List<AuthRole> pauthRoles) {
        authUserSeq = pAuthUserSeq;
        corpId = pCorpId;
        modifiedDate = pModifiedDate;
        firstName = pFirstName;
        lastName = pLastName;
        authRoles = pauthRoles;

    }

    public String getAuthUserSeq() {
        return authUserSeq;
    }

    public void setAuthUserSeq(String pAuthUserSeq) {
        this.authUserSeq = pAuthUserSeq;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String pCorpId) {
        this.corpId = pCorpId;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        this.modifiedDate = pModifiedDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        this.firstName = pFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        this.lastName = pLastName;
    }

    public List<AuthRole> getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(List<AuthRole> pAuthRoles) {
        authRoles = pAuthRoles;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AuthUser) ){
            return false;
        }
        AuthUser authUser = (AuthUser) obj;
        return StringUtils.equals(authUserSeq,authUser.getAuthUserSeq());
    }

    @Override
    public int hashCode(){
        int hashCode = 0;
        if(StringUtils.isNotEmpty(authUserSeq)){
            SpcfUniqueId spcfUniqueId = SpcfUniqueId.createInstance(authUserSeq);
            hashCode = spcfUniqueId.hashCode();
        }
        return hashCode;
    }

}
