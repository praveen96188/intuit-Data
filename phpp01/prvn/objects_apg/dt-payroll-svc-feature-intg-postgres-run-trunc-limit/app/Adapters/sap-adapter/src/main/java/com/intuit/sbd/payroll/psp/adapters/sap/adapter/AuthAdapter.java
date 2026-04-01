package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPException;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPJavaAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.authentication.Ldap;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.UserPreference;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.MessageException;
import org.apache.commons.lang.StringUtils;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.net.ssl.SSLException;
import java.io.File;
import java.security.InvalidAlgorithmParameterException;

/**
 * User: dweinberg
 * Date: Jun 2, 2010
 * Time: 3:43:46 PM
 */
public class AuthAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(AuthAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static Ldap mLdap = null;


    /**
     * Authenticate the user and return all user information.
     *
     * @return SAPUser
     *         User information.  This will be null is username does not exist or password does not match.
     * @throws Exception Throws exception upon failure to communicate with LDAP server
     */
    @FlexMethod
    public SAPUser login(String username, String password, boolean alwaysCreateNewToken) throws Throwable {
        AuthUser authUser = getAuthenticatedUser(username, password);
        if (authUser == null) {
            return null;
        }

        return processUserSession(authUser, username, alwaysCreateNewToken);
    }

    private AuthUser getAuthenticatedUser(String username, String password) throws Throwable {
        AuthUser authUser = null;

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.SAPAdapter));
            PayrollServices.beginUnitOfWork();

            boolean isAuthenticated = true;
            if (username.equals("AutoLogin")) {
                authUser = getAuthUserDomainEntity("AL_" + password.toLowerCase());
            } else {
                isAuthenticated = authenticateUser(username, password);
                try{
                    authUser = getAuthUserDomainEntity(getCorpIdFromLdap(username));
                }
                catch (SAPException e) {
                    // only throw "LDAP user not in db" if pass is valid
                    if (isAuthenticated) {
                        throw e;
                    }
                    // otherwise return null for standard login failure message
                }
            }

            if (authUser == null) {
                return null;
            }

            // set the principal before updating lockout and committing
            PayrollServices.setCurrentPrincipal(authUser.createPrincipal());

            // update lockout information
            ProcessResult prUnhandled = PayrollServices.userManager.updateUserLockoutValues(authUser, isAuthenticated);
            if (!prUnhandled.isSuccess()) {
                logger.error("Unhandled ProcessResult failure from UserManager.updateUserLockoutValues(): " + prUnhandled.toString());
                return null;
            }

            PayrollServices.commitUnitOfWork();

            // reject with lockout exception if lockout is active
            if (authUser.getAccountLockedUntil() != null) {
                aeFactory.throwGenericException("Account locked out for " +
                        SystemParameter.findIntValue(SystemParameter.Code.LOCK_ACCOUNT_DURATION) +
                        " minutes due to excessive failed logins.");
            }

            // standard login failure for unlocked accounts
            if (!isAuthenticated) {
                return null;
            }

        } catch (AuthenticationException e) {
            return null;
        } catch (SSLException ssle) {
            // test whether this is a 'trust anchor' problem
            if (isSSLTrustAnchorException(ssle)) {
                debugCACERTSFile();
            }
            throw ssle;
        } catch (CommunicationException e) {
            aeFactory.throwGenericException("Error communicating with LDAP server.", e);
        } catch (NamingException e) {
            aeFactory.throwGenericException("Error communicating with LDAP server.", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return authUser;
    }

    private SAPUser processUserSession(AuthUser pAuthUser, String pUsername, boolean alwaysCreateNewToken) throws Throwable {
        SAPUser sapUserReturnValue = null;

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(pAuthUser);

            DomainEntitySet<UserPreference> prefs = PayrollServices.entityFinder.findObjects(UserPreference.class);
            sapUserReturnValue = UserTranslator.getSAPUserDTOFromDomainEntity(
                    pAuthUser,
                    pUsername,
                    prefs);

            if (alwaysCreateNewToken || pAuthUser.isExpired()) {
                ProcessResult<String> newAuthTokenPR = PayrollServices.userManager.updateUserAuthorizationToken(
                        sapUserReturnValue.getCorpId());

                if (!newAuthTokenPR.isSuccess()) {
                    throw new MessageException(newAuthTokenPR.getMessages().get(0).getMessage());
                }
                sapUserReturnValue.setAuthorizationToken(newAuthTokenPR.getResult());
                logger.info("processUserSession: creating new authToken and updating in db.Value is: "+newAuthTokenPR.getResult() );
            } else {
                sapUserReturnValue.setAuthorizationToken(pAuthUser.getAuthorizationToken());
            }
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ProcessResult<AuthUser> updateTimestampPR =
                    PayrollServices.userManager.updateUserLastRemoteCallTimestamp(sapUserReturnValue.getCorpId());
            if (!updateTimestampPR.isSuccess()) {
                throw new MessageException(updateTimestampPR.getMessages().get(0).getMessage());
            }

            logger.info("user " + pAuthUser.getCorpId() + " logged in.");
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapUserReturnValue;
    }

    /*
    Not a strictly necessary method from a authentication standpoint, but useful for keeping the same application flow
     */
    @FlexMethod
    public SAPUser loginSSO(SAPUser unAuthenticatedUser) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            if (StringUtils.equals(unAuthenticatedUser.getCorpId(), "notFound")) { //see SSOServlet
                return null;
            }
            AuthUser user = getAuthUserDomainEntity(unAuthenticatedUser.getCorpId());

            //If the user authToken doesn't match input authtoken then terminate the session
            if(!user.getAuthorizationToken().equals(unAuthenticatedUser.getAuthorizationToken())){
                String msg = "Session terminated.  Please login again.";
                throw new Exception(msg);
            }

            DomainEntitySet<UserPreference> prefs = PayrollServices.entityFinder.findObjects(UserPreference.class);

            SAPUser sapUser = UserTranslator.getSAPUserDTOFromDomainEntity(
                    user,
                    unAuthenticatedUser.getCorpId(),
                    prefs);
            sapUser.setAuthorizationToken(unAuthenticatedUser.getAuthorizationToken());
            // Set emailAddress of the logged-in user
            sapUser.setEmailAddress(unAuthenticatedUser.getEmailAddress());

            copyIAMAttributes(unAuthenticatedUser, sapUser);

            return sapUser;
        }catch (Exception e) {
            MessageException me = new MessageException(e.getMessage());
            me.setCode(SAPJavaAdapter.SAP_SESSION_TERMINATED);
            throw me;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    private void copyIAMAttributes(SAPUser unAuthenticatedUser, SAPUser authenticatedUser) {
        authenticatedUser.setAuthId(unAuthenticatedUser.getAuthId());
        authenticatedUser.setTicket(unAuthenticatedUser.getTicket());
        authenticatedUser.setRealmId(unAuthenticatedUser.getRealmId());
    }

    private String getCorpIdFromLdap(String username) throws Exception {
        Ldap ldap = getLdap();

        // Does user exist in directory, and get distinguished name
        String userDn = ldap.getUserDn(username);
        if (userDn == null) {
            return null;
        }

        return ldap.getCorpId(username);
    }

    private boolean authenticateUser(String username, String password) throws Exception {
        Ldap ldap = getLdap();

        // Does user exist in directory, and get distinguished name
        String userDn = ldap.getUserDn(username);
        if (userDn == null) {
            return false;
        }

        try {
            if (ldap.isAuthenticated(userDn, username, password)) {
                return true;
            }
        } catch (AuthenticationException e) {
            return false;
        }
        return false;
    }

    private boolean isSSLTrustAnchorException(SSLException ssle) {
        Throwable rootCause = ssle.getCause();
        if (rootCause != null) {
            if (rootCause instanceof InvalidAlgorithmParameterException) {
                return true;
            }
        }
        return false;
    }

    private void debugCACERTSFile() {
        String cacertsFilePath = System.getProperty("javax.net.ssl.trustStore");
        String message = "CACERTS File Path: " + cacertsFilePath;
        if (cacertsFilePath != null) {
            File cacertsFile = new File(cacertsFilePath);
            if (cacertsFile.exists()) {
                message = message + "\t - file exists";
            }
        }
        logger.error(message);
    }

    private AuthUser getAuthUserDomainEntity(String corpId) throws Exception {
        AuthUser authUserEntity = AuthUser.findUser(corpId);
        if (authUserEntity == null) {
            //squelch redundant error if SSO problem (see SSOServlet)
            if (!StringUtils.equals(corpId, "notFound")) {
                aeFactory.throwGenericException("LDAP user data not found in the database. corp id:" + corpId);
            }
        }
        return authUserEntity;
    }

    /**
     * Overrides standard ldap adapter (used only for unit testing with mock ldap)
     */
    public void setLdap(Ldap pLdap) {
        mLdap = pLdap;
    }

    public Ldap getLdap() throws Exception {
        if (mLdap == null) {
            return Ldap.createInstance();
        } else {
            return mLdap;
        }
    }
}