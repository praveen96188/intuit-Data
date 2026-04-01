/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/UserTranslator.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUserOperation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUserRole;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUserSetting;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.*;

/**
 * UserTranslator - Translator class for retrieving SAP DTOs from PSP core domain entities for user information.
 *
 * @author Joe Warmelink
 */
public class UserTranslator {
    public static SAPUser getSAPUserDTOFromOnlyAuthUser(AuthUser authEntity) {
        if (authEntity == null) return null;
        SAPUser sapUser = new SAPUser();
        sapUser.setCorpId(authEntity.getCorpId());
        sapUser.setFirstName(authEntity.getFirstName());
        sapUser.setLastName(authEntity.getLastName());
        sapUser.setAccountLockedUntil(SAPTranslator.getDateFromSpcfCalendar(authEntity.getAccountLockedUntil()));
        sapUser.setNumberOfFailedLoginAttempts(authEntity.getNumberOfFailedLoginAttempts());
        sapUser.setRoleIds(new ArrayList<String>());
        for (AuthRole role : authEntity.getAuthRoleCollection()) {
            sapUser.getRoleIds().add(role.getRoleId());
        }
        sapUser.setUniqueId(authEntity.getId().toString());
        return sapUser;
    }

    public static SAPUser getSAPUserDTOFromDomainEntity(
            AuthUser authEntity,
            String userName,
            DomainEntitySet<UserPreference> prefs) {

        SAPUser sapUser = getSAPUserDTOFromOnlyAuthUser(authEntity);
        sapUser.setUserName(userName);

        // now add on roles
        AuthDomain roleDomain = authEntity.getAuthRoleCollection().get(0).getAuthDomain();

        for (AuthRole role : authEntity.getAuthRoleCollection()) {
            for (AuthOperation authOper : role.getAuthOperationCollection()) {
                SAPUserOperation sapUserOperation = new SAPUserOperation();
                sapUserOperation.setDescription(roleDomain.getDescription());
                sapUserOperation.setName(roleDomain.getName());
                sapUserOperation.setOperationId(authOper.getOperationId());
                sapUserOperation.setDomainId(roleDomain.getDomainId());
                sapUser.getGrantedOperations().add(sapUserOperation);
            }
        }

        sapUser.setSettings(getSettings(authEntity, prefs));

        return sapUser;
    }

    public static ArrayList<SAPUserSetting> getSettings(AuthUser authEntity, DomainEntitySet<UserPreference> prefs) {
        ArrayList<SAPUserSetting> settings = new ArrayList<SAPUserSetting>();
        Set<UserPreference> foundPreferences = new TreeSet<UserPreference>();
        for (UserSetting authSetting : authEntity.getUserSettingCollection()) {
            SAPUserSetting setting = new SAPUserSetting();
            setting.setKey(authSetting.getUserPreference().getKey());
            setting.setValue(authSetting.getValue());
            setting.setIsDefault(false);
            settings.add(setting);
            foundPreferences.add(authSetting.getUserPreference());
        }

        // add defaults
        for (UserPreference pref : prefs) {
            if (!foundPreferences.contains(pref)) {
                SAPUserSetting setting = new SAPUserSetting();
                setting.setKey(pref.getKey());
                setting.setValue(pref.getDefaultValue());
                setting.setIsDefault(true);
                settings.add(setting);
            }
        }
        return settings;
    }

    public static SAPUserRole getSAPRoleDTO(AuthRole authRole) {
        SAPUserRole sapUserRole = new SAPUserRole();
        sapUserRole.setRoleId(authRole.getRoleId());
        sapUserRole.setName(authRole.getName());

        ArrayList<SAPUserOperation> sapOperations = new ArrayList<SAPUserOperation>();
        SAPUserOperation sapOperation;
        for (AuthOperation authOp : authRole.getAuthOperationCollection()) {
            sapOperation = getSAPUserOperationFromDomainEntity(authOp);
            sapOperations.add(sapOperation);
        }
        sapUserRole.setOperations(sapOperations);
        sapUserRole.setDescription(authRole.getDescription());
        return sapUserRole;
    }

    public static SAPUserOperation getSAPUserOperationFromDomainEntity(AuthOperation operation) {
        SAPUserOperation sapUserOperation = new SAPUserOperation();
        sapUserOperation.setDescription(operation.getDescription());
        sapUserOperation.setName(operation.getName());
        sapUserOperation.setOperationId(operation.getOperationId());
        // sapUserOperation.setSystemId(systemId);
        return sapUserOperation;

    }

   /* public static SAPUser getSAPUserDTOFromOnlyAuthUser(AuthUser authEntity) {
        SAPUser sapUser = new SAPUser();
        sapUser.setCorpId(authEntity.getCorpId());
        sapUser.setFirstName(authEntity.getFirstName());
        sapUser.setLastName(authEntity.getLastName());
        sapUser.setRoleId(authEntity.getAuthRole().getRoleId());
        return sapUser;
    }  */
}
