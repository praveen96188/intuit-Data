/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPUser.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * SAPUser - SAP DTO for user information and privileges
 *
 * @author Joe Warmelink
 */
public class SAPUser {
    private String corpId;
    //private long globalUserId;
    private String firstName;
    private String lastName;
    private String userName;
    private ArrayList<String> roleIds;
    private String uniqueId;
    private String emailAddress;

    private String authorizationToken;
    private Date lastRemoteCallTimestamp;
    private Date accountLockedUntil;
    private int numberOfFailedLoginAttempts;

    private ArrayList<SAPUserSetting> settings;

    private ArrayList<SAPUserOperation> grantedOperations = new ArrayList<SAPUserOperation>();

    // IAM Attributes
    private String ticket;
    private String authId;
    private String realmId;

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    /*public long getGlobalUserId() {
        return globalUserId;
    }

    public void setGlobalUserId(long globalUserId) {
        this.globalUserId = globalUserId;
    }              */

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ArrayList<SAPUserOperation> getGrantedOperations() {
        return grantedOperations;
    }

    public void setGrantedOperations(ArrayList<SAPUserOperation> grantedOperations) {
        this.grantedOperations = grantedOperations;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(ArrayList<String> roleIds) {
        this.roleIds = roleIds;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public Date getLastRemoteCallTimestamp() {
        return lastRemoteCallTimestamp;
    }

    public void setLastRemoteCallTimestamp(Date lastRemoteCallTimestamp) {
        this.lastRemoteCallTimestamp = lastRemoteCallTimestamp;
    }

    public Date getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(Date pAccountLockedUntil) {
        this.accountLockedUntil = pAccountLockedUntil;
    }

    public int getNumberOfFailedLoginAttempts() {
        return numberOfFailedLoginAttempts;
    }

    public void setNumberOfFailedLoginAttempts(int pNumberOfFailedLoginAttempts) {
        this.numberOfFailedLoginAttempts = pNumberOfFailedLoginAttempts;
    }

    public ArrayList<SAPUserSetting> getSettings() {
        return settings;
    }

    public void setSettings(ArrayList<SAPUserSetting> settings) {
        this.settings = settings;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
}
