package com.intuit.ems.psp.adapters.dataadapter.mapper;

import com.intuit.ems.psp.adapters.dataadapter.dto.AuthRole;

import java.util.Date;

/**
 * Created by ajhawar on 10/20/2015.
 */
public class AuthRoleToDTOMapper {

    public static AuthRole mapToDTO(com.intuit.sbd.payroll.psp.domain.AuthRole pAuthRole) {
        AuthRole authRoles = new AuthRole();
        authRoles.setRoleId(pAuthRole.getRoleId());
        authRoles.setModifiedDate(new Date(pAuthRole.getModifiedDate().getTimeInMilliseconds()));
        authRoles.setRoleName(pAuthRole.getName());
        authRoles.setRoleDescription(pAuthRole.getDescription());
        authRoles.setAuthRoleSeq(pAuthRole.getId().toString());
        return authRoles;
    }
}
