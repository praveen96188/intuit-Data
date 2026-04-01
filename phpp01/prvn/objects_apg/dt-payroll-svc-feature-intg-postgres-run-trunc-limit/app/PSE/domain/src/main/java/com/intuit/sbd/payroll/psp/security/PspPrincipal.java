/*
 * $Id: //psp/dev/PSE/Domain/src/com/intuit/sbd/payroll/psp/security/PspPrincipal.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.security;

import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * PSP specific implementation of SpcfPrincipal.
 *
 * @author Wiktor Kozlik
 */
public class PspPrincipal {

    /**
     * User Id
     */
    private String userId;

    /**
     * Name
     */
    private String name;

    /**
     * SystemPrincipal
     */
    private SystemPrincipal systemPrincipal;

    /**
     * IsAgent
     */
    private Boolean isAgent;

    /**
     * Default constructor.
     *
     * @param id    User Id
     * @param name  Name of the user
     */
    public PspPrincipal(String id, String name) {
        systemPrincipal = SystemPrincipal.SAPAdapter;
        userId = id;
        this.name = name;
        this.isAgent = true;
    }

    public PspPrincipal(SystemPrincipal principal) {
        systemPrincipal = principal;
        userId = principal.getId();
        name = principal.getId();
        this.isAgent = false;
    }


    public PspPrincipal(SystemPrincipal principal, String userName) {
        systemPrincipal = principal;
        userId = userName;
        name = principal.getId();
        this.isAgent = false;
    }

    public String getId() {
        return userId;
    }

    public String getName() {
        return name;
    }


    public Boolean isAgent() {
        return isAgent;
    }


    public boolean isAuthenticated() {
        // the identity is authenticated, if identity's id is valid
        return userId != null && userId.length() > 0;
    }

    /**
     *
     */
    public String getLoggerApplicationName() {
        return systemPrincipal.getId();
    }

    public SystemPrincipal getSystemPrincipal() {
        return systemPrincipal;
    }

    public boolean isCustomer() {
        return systemPrincipal != null && systemPrincipal.isCustomer();
    }

    /**
     * Override equal method to determine the equality of two principals.
     *
     * @param o object
     * @return true if equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (o instanceof PspPrincipal) {
            PspPrincipal oPrincipal = (PspPrincipal) o;

            boolean idCheck = this.getId() != null && oPrincipal.getId() != null &&
                    this.getId().equals(oPrincipal.getId());
            boolean nameCheck = this.getName() != null && oPrincipal.getName() != null &&
                    this.getName().equals(oPrincipal.getName());
            // don't need to check isAuthenticated value since it is based on id
            return idCheck && nameCheck;
        }
        return false;
    }

    /**
     * Overridden since the equals operator is also overridden.
     *
     * @return Returns the hashcode for this instance.
     */
    @Override
    public int hashCode() {
        //Seed to start with.
        int result = 17;

        //Computing hash code as given in "Effective Java" book in java.sun.code
        result = 37 * result + this.getId().hashCode();

        result = 37 * result + this.getName().hashCode();

        return result;
    }

}
