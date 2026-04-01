package com.intuit.ems.psp.adapters.dataadapter.mapper;

import com.intuit.ems.psp.adapters.dataadapter.dto.AuthRole;
import com.intuit.ems.psp.adapters.dataadapter.dto.AuthUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AuthUserToDTOMapper {

    public static AuthUser mapToDTO(com.intuit.sbd.payroll.psp.domain.AuthUser pAuthUser) {
        AuthUser authUser = new AuthUser();
        authUser.setAuthUserSeq(pAuthUser.getId().toString());
        authUser.setCorpId(pAuthUser.getCorpId());
        authUser.setFirstName(pAuthUser.getFirstName());
        authUser.setLastName(pAuthUser.getLastName());
        authUser.setModifiedDate(new Date(pAuthUser.getModifiedDate().getTimeInMilliseconds()));
        List<AuthRole> authRoleList = new ArrayList<AuthRole>();
        for (com.intuit.sbd.payroll.psp.domain.AuthRole domainAuthRole : pAuthUser.getAuthRoleCollection()) {
            authRoleList.add(AuthRoleToDTOMapper.mapToDTO(domainAuthRole));
        }
        authUser.setAuthRoles(authRoleList);
        return authUser;
    }
}
