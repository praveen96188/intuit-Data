package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.OperationId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * User: dweinberg
 * Date: Apr 8, 2009
 * Time: 7:28:47 AM
 *
 * Helper class for servlets outside of Flash for testing if
 * a user is valid and what operations are available.
 */
public class UserOperationVerifier {

    private boolean isValid=false;
    private AuthUser loggedInUser;

    public UserOperationVerifier(HttpServletRequest request) {
        String token = request.getParameter("token");
        if(token != null){
            try {
                PayrollServices.beginUnitOfWork();
                DomainEntitySet<AuthUser> users = Application.find(AuthUser.class, AuthUser.AuthorizationToken().equalTo(token));

                // there can be only one (user)
                if (users.size() > 0 && users.size() < 2) {
                    isValid = true;
                    loggedInUser = users.getFirst();
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean canPerformOperation(OperationId operation) {
        boolean manageTransaction = !Application.hasActiveTransaction();
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
                Application.refresh(loggedInUser);
            }
            return loggedInUser.hasOperation(operation);
        } finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    public void requireValidUser() throws ServletException {
        if (! isValid()) {
            throw new ServletException("Invalid user.");
        }
    }

    public void requireOperation(OperationId operation) throws ServletException {
        if (! canPerformOperation(operation)) {
            throw new ServletException("User must be able to perform " + operation.toString());
        }
    }




}
