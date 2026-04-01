/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/UserAdapter.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPJavaAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.authentication.Ldap;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.MessageException;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

/**
 * UserAdapter - Adapter class for retrieving User information in PSP core for SAP
 *
 * @author Joe Warmelink
 */
public class UserAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(UserAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private static final String DDUI_APPLICATION = "DDUI";


    @FlexMethod
    public void sapLogout(String pCorpId) throws Throwable {
        PayrollServices.beginUnitOfWork();
        try {
            ProcessResult result = PayrollServices.userManager.removeUserAuthorizationToken(pCorpId);
            if (!result.isSuccess()) {
                String message = "unable to login";
                if (result.getMessages().size() > 0)
                    message = result.getMessages().get(0).getMessage();
                throw new Exception(message);
            }
            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Unable to logout", "Corp", pCorpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AuthAccessApplication)
    public ArrayList<String> getAllRoles(String domainId) throws Throwable {

        ArrayList<String> returnRoleList = new ArrayList<String>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<AuthRole> authUserRoles = AuthRole.findRoles(domainId);

            for (AuthRole authUserRole : authUserRoles) {
                returnRoleList.add(UserTranslator.getSAPRoleDTO(authUserRole).getRoleId());
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding roles.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnRoleList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AuthAccessApplication)
    public ArrayList<SAPUserRole> getAllRoleObjects() throws Throwable {

        ArrayList<SAPUserRole> returnRoleList = new ArrayList<SAPUserRole>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<AuthRole> authUserRoles = AuthRole.findRoles(DDUI_APPLICATION);

            for (AuthRole authUserRole : authUserRoles) {
                returnRoleList.add(UserTranslator.getSAPRoleDTO(authUserRole));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding roles.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnRoleList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AuthAccessApplication)
    public ArrayList<SAPUserOperation> getAllOperations() throws Throwable {
        ArrayList<SAPUserOperation> returnOperationsList = new ArrayList<SAPUserOperation>();


        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DomainEntitySet<AuthOperation> authOperations = PayrollServices.entityFinder.findObjects(AuthOperation.class);

            for (AuthOperation authOperation : authOperations) {
                returnOperationsList.add(UserTranslator.getSAPUserOperationFromDomainEntity(authOperation));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding operations.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnOperationsList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AuthAccessApplication)
    public SAPSearchResults<SAPUser> searchUsersInDomain(String pDomainId, String pFirstName, String pLastName, String pCorpId, int pFirstIndex, int pMaxResults, String pSortColumn, boolean pSortDescending) throws Throwable {
        SAPSearchResults<SAPUser> searchResults = new SAPSearchResults<SAPUser>();
        ArrayList<SAPUser> sapUsers = new ArrayList<SAPUser>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            if (StringUtils.isEmpty(pDomainId)) {
                return null;
            }

            HqlBuilder hql = new HqlBuilder(true, "from com.intuit.sbd.payroll.psp.domain.AuthUser user");
            hql.append("join user.AuthRoleSet authRole");
            hql.append("where authRole.AuthDomain.DomainId = :authDomain").setParameter("authDomain",pDomainId);

            if (StringUtils.isNotEmpty(pLastName)) {
                hql.append("and user.LastName = :lastName").setParameter("lastName", pLastName);
            }

            if (StringUtils.isNotEmpty(pFirstName)) {
                hql.append("and user.FirstName = :firstName").setParameter("firstName", pFirstName);
            }

            if (StringUtils.isNotEmpty(pCorpId)) {
                hql.append("and user.CorpId = :corpId").setParameter("corpId", pCorpId);
            }

            searchResults.setTotalRecords((Long) hql.select("select count(*)").get(0));

            if(pSortColumn == null || pSortColumn.equals("lastName")) {
                hql.append("order by user.LastName");
            } else if (pSortColumn.equals("firstName")) {
                hql.append("order by user.FirstName");
            } else if (pSortColumn.equals("corpId")) {
                hql.append("order by user.CorpId");
            } else if (pSortColumn.equals("accountLockedUntil")) {
                hql.append("order by user.AccountLockedUntil");
            } else {
                hql.append("order by user.LastName");
            }

            if(pSortDescending) {
                hql.append("desc");
            } else {
                hql.append("asc");
            }

            hql.append(", user.Id");

            if (searchResults.getTotalRecords() > 0) {
                List<AuthUser> authUserList = hql.select(pFirstIndex, pMaxResults, "select distinct user");
                for (AuthUser authUser : authUserList) {
                    sapUsers.add(UserTranslator.getSAPUserDTOFromOnlyAuthUser(authUser));
                }
            }
            searchResults.setReturnsList(sapUsers);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding users", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return searchResults;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AuthAccessApplication)
    public ArrayList<SAPUser> getUsersInDomain(String domainId) throws Exception {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return getAllUsers(domainId);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private ArrayList<SAPUser> getAllUsers(String domainId) throws Exception {
        ArrayList<SAPUser> returnUserList = new ArrayList<SAPUser>();

        DomainEntitySet<AuthUser> authUsers = AuthUser.findUsers(domainId);
        for (AuthUser authUser : authUsers) {
            returnUserList.add(UserTranslator.getSAPUserDTOFromOnlyAuthUser(authUser));
        }

        return returnUserList;
    }

    @FlexMethod
    public ArrayList<SAPUser> getAllUsersByOperation(String operationId) throws Throwable {
        ArrayList<SAPUser> returnUserList = new ArrayList<SAPUser>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<AuthUser> authUsers = AuthUser.findUsersByOperation(operationId);

            for (AuthUser authUser : authUsers) {
                returnUserList.add(UserTranslator.getSAPUserDTOFromOnlyAuthUser(authUser));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding all users.", "Operation", operationId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnUserList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AuthAccessApplication)
    public void addNewUserData(
            String corpId,
            String firstName,
            String lastName,
            ArrayList<String> roleIds) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Ldap ldap = Ldap.createInstance();
            // Does user exist in directory
            boolean userExists = ldap.corpIdExists(corpId);
            if (!userExists) {
                aeFactory.throwGenericException("Error user does not exist in ldap.");
            } else {
                ProcessResult<AuthUser> processResult;
                processResult = PayrollServices.userManager.addUser(corpId, roleIds, firstName, lastName);
                if (processResult.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                } else {
                    PayrollServices.rollbackUnitOfWork();
                    aeFactory.throwGenericException("Error adding user.", processResult);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding user.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Operation(operationIds = {
            OperationId.AuthAddUpdateUsers,
            OperationId.AuthAddUpdateHelpDesk,
            OperationId.AuthAddUpdateDataCustodian
    })
    @FlexMethod
    public void updateUserData(String uniqueId, String corpId, String firstName, String lastName, ArrayList<String> roleIds)
            throws Throwable {
        PayrollServices.beginUnitOfWork();

        try {
            Ldap ldap = Ldap.createInstance();
            // Does user exist in directory
            boolean userExists = ldap.corpIdExists(corpId);
            if (!userExists) {
                aeFactory.throwGenericException("Error user does not exist in ldap.");
            } else {
                ProcessResult<AuthUser> processResult;
                processResult = PayrollServices.userManager.updateUser(uniqueId, corpId, roleIds, firstName, lastName);
                if (processResult.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                } else {
                    aeFactory.throwGenericException("Error updating user data.", processResult);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating user data.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Deprecated
    @FlexMethod
    private void addNewRole(String roleId, String name, String description, String domainId, ArrayList<OperationId> opId) throws Throwable {
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDTO = new UserRoleDTO();
        ProcessResult<AuthRole> processResult;
        try {
            roleDTO.setRoleId(roleId);
            roleDTO.setDescription(description);
            roleDTO.setDomainId(domainId);
            roleDTO.setOperationIds(opId);
            processResult = PayrollServices.userManager.addRole(roleDTO);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding new role.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.AuthRemoveUsers,
            OperationId.AuthRemoveDataCustodian,
            OperationId.AuthAddRemoveHelpDesk
    })
    public void removeUser(String corpId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<AuthUser> processResult;
            processResult = PayrollServices.userManager.deleteUser(corpId);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error deleting user.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public void updatePreference(String corpId, String key, String value) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<UserSetting> processResult = PayrollServices.userManager.updateUserSetting(corpId, key, value);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating user preference.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public void updatePreferences(String corpId, ArrayList<SAPUserSetting> settings) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            for (SAPUserSetting setting : settings) {
                ProcessResult<UserSetting> processResult = PayrollServices.userManager.updateUserSetting(corpId, setting.getKey(), setting.getValue());
                if (!processResult.isSuccess()) {
                    aeFactory.throwGenericException("Error updating preferences.", processResult);
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating preferences.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPUserSetting> getUserSettings(String corpId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            AuthUser authUser = getAuthUserDomainEntity(corpId);
            DomainEntitySet<UserPreference> prefs = PayrollServices.entityFinder.findObjects(UserPreference.class);
            return UserTranslator.getSettings(authUser, prefs);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding user settings.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public void resetSettings(String corpId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult pr = PayrollServices.userManager.resetSettingsToDefault(corpId);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error resetting settings.", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error resetting user settings.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public void unlockUser(String corpId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            AuthUser authUser = AuthUser.findUser(corpId);
            ProcessResult pr = PayrollServices.userManager.unlockUser(authUser);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error unlocking user.", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error unlocking user.", "Corp", corpId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private AuthUser getAuthUserDomainEntity(String corpId) throws Exception {
        AuthUser authUserEntity = AuthUser.findUser(corpId);
        if (authUserEntity == null) {
            aeFactory.throwGenericException("LDAP user data not found in the database. corp id:" + corpId);
        }
        return authUserEntity;
    }


}
