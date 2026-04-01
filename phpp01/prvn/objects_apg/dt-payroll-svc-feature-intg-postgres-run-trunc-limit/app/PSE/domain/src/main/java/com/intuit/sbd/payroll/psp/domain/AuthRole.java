package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class AuthRole extends BaseAuthRole {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders & counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Function to return the list of AuthRoles for the given domain id
     *
     * @param pDomainId String
     * @return DomainEntitySet<AuthRole>
     */
    public static DomainEntitySet<AuthRole> findRoles(String pDomainId) {
        AuthDomain domain = Application.findById(AuthDomain.class, pDomainId);

        DomainEntitySet<AuthRole> roleList = Application.find(AuthRole.class, AuthDomain().equalTo(domain));

        return roleList;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public AuthRole()
	{
		super();
	}

    /**
     * Function to get the AuthRole for the given role id
     *
     * @param pRoleId String
     * @return AuthRole
     */
    public static AuthRole findRole(String pRoleId) {
        AuthRole foundRole = null;
        DomainEntitySet<AuthRole> roleList = Application.find(AuthRole.class, RoleId().equalTo(pRoleId));

        if (roleList.size() > 1) {
            throw new RuntimeException("Duplicate Roles for Role id: " + pRoleId);
        }

        if (!roleList.isEmpty()) {
            foundRole = roleList.get(0);
        }

        return foundRole;
    }
}