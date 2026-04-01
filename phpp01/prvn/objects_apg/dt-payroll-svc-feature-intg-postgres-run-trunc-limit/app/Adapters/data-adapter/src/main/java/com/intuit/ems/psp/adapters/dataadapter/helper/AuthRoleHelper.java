package com.intuit.ems.psp.adapters.dataadapter.helper;

import com.intuit.ems.psp.adapters.dataadapter.dto.AuthRole;
import com.intuit.ems.psp.adapters.dataadapter.mapper.AuthRoleToDTOMapper;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Auth Role api
 */
public class AuthRoleHelper {
    /**
     *
     * @return list of auth roles
     */
    public static List<AuthRole> getAuthRoles() {
        DomainEntitySet<com.intuit.sbd.payroll.psp.domain.AuthRole> authRoleSet = Application.find(com.intuit.sbd.payroll.psp.domain.AuthRole.class);
        List<AuthRole> authRoleList = new ArrayList<AuthRole>();
        for (com.intuit.sbd.payroll.psp.domain.AuthRole authRole : authRoleSet) {
            authRoleList.add(AuthRoleToDTOMapper.mapToDTO(authRole));
        }
        return authRoleList;
    }

}
