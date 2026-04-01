package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
public class SAPAuthHelper {
    private static SpcfLogger logger = PayrollServices.getLogger(SAPJavaAdapter.class);

    public static void validateLoginParameters(String corpId, String authorizationToken) throws Exception {
        if (corpId == null) {
            String msg = "corpId is null.  Please authenticate before calling any service methods.";
            logger.debug(msg);
            throw new Exception(msg);
        }
        if (authorizationToken == null) {
            String msg = "authorizationToken is null.  Please authenticate before calling any service methods.";
            logger.debug(msg);
            throw new Exception(msg);
        }
    }

    public static AuthUser getLoggedInUser(String corpId) throws Exception {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            AuthUser user = AuthUser.findUser(corpId);
            if (user == null) {
                String msg = "User is not configured to use PSP.";
                logger.debug("CorpId (" + corpId + ") is not found in PSP user table." + msg);
                throw new Exception(msg);
            }
            return user;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void validateUserHasAccessToMethod(AuthUser user, Class destinationClass, String destinationMethodName) throws Exception {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Application.refresh(user);
            for (Method method : destinationClass.getDeclaredMethods()) {
                if (method.getName().equals(destinationMethodName)) {
                    Operation operation = method.getAnnotation(Operation.class);
                    if (operation != null) {
                        if (!user.hasAnyOperation(operation.operationIds())) {
                            String msg = "Your role is not allowed to perform this operation.";
                            logger.warn("User with corpId '" + user.getCorpId()
                                    + "' tried to preform/postform/reform/conform/inform/claim form/free form/lukewarm/ice storm/life form/perform action " + Arrays.toString(operation.operationIds()) + " which is not allowed.");
                            throw new Exception(msg);
                        }
                    }
                    // no need to continue we do not override service methods
                    break;
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void validateAuthToken(AuthUser user, String authorizationToken) throws Exception {
        String dbAuthToken = user.getAuthorizationToken();
        if (dbAuthToken == null) {
            String msg = "Session terminated.  Please login again.";
            logger.debug("The authToken in the database was null for corpId '" + user.getCorpId()
                    + "'.  " + msg);
            throw new Exception(msg);
        }

        if (!dbAuthToken.equals(authorizationToken)) {
            String msg = "Session terminated due to alternate login.";
            logger.debug("AuthToken '" + authorizationToken
                    + "' from client doesn't match token '" + dbAuthToken + "' from database.  " + msg);

            throw new Exception(msg);
        }
    }

    public static void validateSessionTimeout(AuthUser user) throws Exception {
        if (user.isExpired()) {
            try {
                PayrollServices.beginUnitOfWork();
                ProcessResult prUnhandled = PayrollServices.userManager.removeUserAuthorizationToken(user.getCorpId());
                if (!prUnhandled.isSuccess()) {
                    logger.error("Unhandled ProcessResult failure from UserManager.removeUserAuthorizationToken(): " + prUnhandled.toString());
                }
                PayrollServices.commitUnitOfWork();

                throw new Exception("Session expired.  Please login again.");
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static void validateSSOSessionTimeout(AuthUser user) throws Exception {
        if (user.isExpired()) {
            try {
                logger.error("The user session is expired.Logging out of the application.");
                String msg ="Session expired.  Please login again.";
                throw new Exception(msg);
            } finally {

            }
        }
    }

    public static void updateLastRemoteCallTimestamp(AuthUser user) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult prUnhandled = PayrollServices.userManager.updateUserLastRemoteCallTimestamp(user.getCorpId());
            if (!prUnhandled.isSuccess()) {
                logger.error("Unhandled ProcessResult failure from UserManager.updateUserLastRemoteCallTimestamp(): " + prUnhandled.toString());
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable ex) {
            PayrollServices.rollbackUnitOfWork();
            throw new Exception("Contact system administrator.  Unexpected error occurred: " + ex.toString());
        }
    }

}