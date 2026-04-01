package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.intuit.sbd.payroll.psp.domain.OperationId;

/**
 * Hand-written business logic
 */
public class AuthUser extends BaseAuthUser {
    private static final SpcfLogger logger = Application.getLogger(AuthUser.class);

    private static final Set<String> sapAdminRoles = Arrays.stream(ConfigurationManager.getSettingValue(
            ConfigurationModule.SAPAdapter, "sap_admin_roles").split(","))
            .map(String :: trim).collect(Collectors.toSet());

    public boolean hasOperation(OperationId operation) {
        AuthOperation authOperation = Application.findById(AuthOperation.class, operation);
        for (AuthRole role : getAuthRoleCollection()) {
            if (role.getAuthOperationCollection().contains(authOperation)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyOperation(OperationId[] operations) {
        for(OperationId operation : operations){
            AuthOperation authOperation = Application.findById(AuthOperation.class, operation);
            for (AuthRole role : getAuthRoleCollection()) {
                if (role.getAuthOperationCollection().contains(authOperation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public DomainEntitySet<RoleSubStatus> getRoleSubStatuses() {
        DomainEntitySet<RoleSubStatus> roleSubStatuses = new DomainEntitySet<RoleSubStatus>();
        for (AuthRole authRole : getAuthRoleCollection()) {
            roleSubStatuses.addAll(authRole.getRoleSubStatusCollection());
        }
        return roleSubStatuses;
    }

    //this is heuristically primary, but there is no real "primary" role
    //assume role with most operations is the "primary" one
    //concept really only useful for error messaging, etc.
    public AuthRole getPrimaryRole() {
        AuthRole maxAuthRole = null;
        for (AuthRole authRole : getAuthRoleCollection()) {
            if (maxAuthRole == null || authRole.getAuthOperationCollection().size() > maxAuthRole.getAuthOperationCollection().size()) {
                maxAuthRole = authRole;
            }
        }
        return maxAuthRole;
    }

    public boolean hasRole(String roleId) {
        return getAuthRoleCollection().find(AuthRole.RoleId().equalTo(roleId)).isNotEmpty();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders & counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function to get the AuthUser for the given User Id
     *
     * @param pUserId String
     * @return AuthUser
     */
    public static AuthUser findUser(String pUserId) {
        AuthUser foundUser = null;
        DomainEntitySet<AuthUser> userList = Application.find(AuthUser.class, new Query<AuthUser>()
                .Where(AuthUser.CorpId().equalTo(pUserId))
                .EagerLoad(AuthUser.AuthRoleSet()));

        if (userList.size() > 1) {
            throw new RuntimeException("Duplicate users for user id: " + pUserId);
        }

        if (!userList.isEmpty()) {
            foundUser = userList.get(0);
        }

        return foundUser;
    }

    public static boolean isAuthorizedtoAccessDGDeletedCompanies() {

        String userId = Application.getCurrentPrincipal().getId();
        AuthUser user = AuthUser.findUser(userId);
        try {
            Set<AuthRole> userRoles = user.getAuthRoleSet();
            Set<String> userRolesList = new HashSet<>();
            for (AuthRole userRole : userRoles) {
                userRolesList.add(userRole.getRoleId());
            }

            userRolesList.retainAll(sapAdminRoles);

            if (userRolesList.size() > 0) {
                logger.info("This user " + userId + " is a SAP admin");
                return true;
            }
            return false;
        } finally {
            Application.evict(user);
        }
    }

    public static boolean hasSAPAdminAccess() {
        boolean isDGDiscoverabilityEnabled = Company.isDGDeleteFeatureEnabled();
        logger.debug("feature flag of DG_DISCOVERABILITY_FEATURE=" + isDGDiscoverabilityEnabled);

        if (!isDGDiscoverabilityEnabled) {
            // feature flag of DG discoverability is OFF
            return true;
        }

        // feature flag of DG discoverability is ON
        PspPrincipal pspPrincipal = Application.getCurrentPrincipal();

        if (Objects.isNull(pspPrincipal) || (!pspPrincipal.isAgent())) {
            //if PSP principal is null or not an Agent (Non SAP flow)
            return false;
        }

        // SAP flow
        boolean isAdmin = AuthUser.isAuthorizedtoAccessDGDeletedCompanies();

        if (isAdmin) {
            //Admin can view the deleted companies
            return true;
        }

        //Non Admin can't view the deleted companies
        return false;
    }

    /**
     * Function to get the list of users
     *
     * @param pDomainId String
     * @return DomainEntitySet<AuthUser>
     */
    public static DomainEntitySet<AuthUser> findUsers(String pDomainId) {
        return Application.findByNamedQueryUsingCache(AuthUser.class,
                                                      "findUsersByDomain",
                                                      new String[]{"authDomain"},
                                                      new Object[]{pDomainId});
    }

    public static DomainEntitySet<AuthUser> findUsersByOperation(String pOperationId) {
        return Application.findByNamedQueryUsingCache(AuthUser.class,
                                                      "findUsersByOperation",
                                                      new String[]{"operation"},
                                                      new Object[]{OperationId.valueOf(pOperationId)});
    }

    public static AuthUser getLoggedInUser() {
        return AuthUser.findUser(Application.getCurrentPrincipal().getId());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public AuthUser()
	{
		super();
	}

    public boolean isExpired() {
        long sapSessionTimeout = SystemParameter.findLongValue(SystemParameter.Code.SAP_SESSION_TIMEOUT) * 1000;
        long earliestSessionStartTimeMillis = PSPDate.getPSPTime().getTimeInMilliseconds() - sapSessionTimeout;
        SpcfCalendar earliestSessionStartTime = SpcfCalendar.createInstance(earliestSessionStartTimeMillis);
        return getLastRemoteCallTimestamp() == null || getLastRemoteCallTimestamp().before(earliestSessionStartTime);
    }

    public PspPrincipal createPrincipal() {
        return new PspPrincipal(getCorpId(), getFirstName() + " " + getLastName());
    }

}