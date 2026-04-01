package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AuthAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.UserAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.authentication.Ldap;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.AuthenticationException;

import static org.junit.Assert.*;

/**
 * User: Greg Patterson
 * Date: 6/18/13
 * Time: 9:27 AM
 */
public class UserLockoutTests {
    private int mLockAccountDuration;
    private int mMaxNumberOfFailedLoginAttempts;
    private AuthAdapter mAuthAdapter;
    private Ldap mockLdap;
    private String mCorpId;
    private final String mUsername = "";
    private final String mUserDn= "";
    private final String mValidPass = "yes";
    private final String mInvalidPass = "no";
    private AuthUser mAuthUser;

    @Before
    public void runBeforeEachTest() throws Exception {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        FlexUnitDataLoaderService.AddUsers();
        getSystemParameters();

        // assign a user from the database as our test user
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AuthUser> authUsers = AuthUser.findUsers("AUTH");
        assert(authUsers.isNotEmpty());
        mCorpId = authUsers.getFirst().getCorpId();
        PayrollServices.rollbackUnitOfWork();

        mockLdap = EasyMock.createMock(Ldap.class);
        // Create a mock ldap adapter that will simulate LDAP authentication by:

        // returning a (fake) userDn for our (fake) username,
        EasyMock.expect(mockLdap.getUserDn(mUsername)).andReturn(mUserDn).anyTimes();

        // returning our test (real) corpId when given the (fake) username,
        EasyMock.expect(mockLdap.getCorpId(mUsername)).andReturn(mCorpId).anyTimes();

        // returning true when authenticating with our (fake) valid pass,
        EasyMock.expect(mockLdap.isAuthenticated(mUserDn, mUsername, mValidPass)).andReturn(true).anyTimes();

        // and throwing an exception when authenticating with our (fake) invalid pass
        EasyMock.expect(mockLdap.isAuthenticated("", mUsername, mInvalidPass));
        EasyMock.expectLastCall().andThrow(new AuthenticationException("Mock LDAP authentication failure.")).anyTimes();

        // ..we also don't care how many times it does each of these, thus the .anyTimes() suffix

        // replay the mock and assign it to a new AuthAdapter
        EasyMock.replay(mockLdap);
        mAuthAdapter = new AuthAdapter();
        mAuthAdapter.setLdap(mockLdap);

        // test user should exist and begin unlocked
        mAuthUser = AuthUser.findUser(mCorpId);
        assertNotNull(mAuthUser);
        assertUnlocked();
    }

    @After
    public void runAfterEachTest() {
        EasyMock.verify(mockLdap);
        PayrollServicesTest.afterEachTest();
    }

    /**
     * On a fresh user, test that invalid logins increment NOFLA and set ALU as expected,
     * while valid logins clear NOFLA, and that logins at and after the lock throw the
     * correct exception.
     */
    @Test
    public void testLoginsOnUnlockedUser() throws Throwable {

        // first test that a single invalid login increments NOFLA, but doesn't set ALU
        assertNull(mAuthAdapter.login(mUsername, mInvalidPass, true));
        assertLockValues(1, null);

        // verify that a valid login on unlocked user resets NOFLA
        assertNotNull(mAuthAdapter.login(mUsername, mValidPass, true));
        assertUnlocked();

        // send invalid logins up to allowed, verify NOFLA increment, lack of ALU on each
        for (int i = 1; i <= mMaxNumberOfFailedLoginAttempts; i++) {
            assertNull("Invalid login succeeded", mAuthAdapter.login(mUsername, mInvalidPass, true));
            assertLockValues(i, null);
        }

        // send locking invalid login, verify exception and lock values
        SpcfCalendar expectedLock = PSPDate.getPSPTime();
        expectedLock.addMinutes(mLockAccountDuration);
        try {
            mAuthAdapter.login(mUsername, mInvalidPass, true);
            fail("Login on locked account failed to trigger exception.");
        } catch (SAPException e) {
            assertExceptionIsLockMessage(e);
        }
        assertLockValues(mMaxNumberOfFailedLoginAttempts + 1, expectedLock);

        // wait a minute so that lock update will be obvious
        SpcfCalendar newTime = PSPDate.getPSPTime();
        newTime.addMinutes(1);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(newTime);
        PayrollServices.commitUnitOfWork();

        // send additional invalid login, verify updated NOFLA & ALU
        expectedLock = PSPDate.getPSPTime();
        expectedLock.addMinutes(mLockAccountDuration);
        try {
            mAuthAdapter.login(mUsername, mInvalidPass, true);
            fail("Login on locked account failed to trigger exception.");
        } catch (SAPException e) {
            assertExceptionIsLockMessage(e);
        }
        assertLockValues(mMaxNumberOfFailedLoginAttempts + 2, expectedLock);
    }

    /**
     * Test that valid logins reset NOFLA and fail on existing lock, while
     * succeeding on expired locks (and clearing them).
     */
    @Test
    public void testLoginsOnLockedUser() throws Throwable {

        lockTestAccount();

        // save ALU to compare against
        SpcfCalendar lockTime = mAuthUser.getAccountLockedUntil();

        // send valid login, check for NOFLA reset & unchanged ALU
        try {
            mAuthAdapter.login(mUsername, mValidPass, true);
            fail("Login on locked account failed to trigger exception.");
        } catch (SAPException e) {
            assertExceptionIsLockMessage(e);
        }
        assertLockValues(0, lockTime);

        // 'wait' for lock to expire, send valid login, check for success, check for reset NOFLA & ALU
        PayrollServices.beginUnitOfWork();
        lockTime.addMinutes(1);
        PSPDate.setPSPTime(lockTime);
        PayrollServices.commitUnitOfWork();
        assertNotNull(mAuthAdapter.login(mUsername, mValidPass, true));
        assertUnlocked();
    }

    /**
     * Test that valid an administrator's unlock request really does unlock.
     */
    @Test
    public void testUnlockOfLockedUser() throws Throwable {

        lockTestAccount();

        // call unlock function to simulate Unlock button from admin UI
        UserAdapter userAdapter = new UserAdapter();
        userAdapter.unlockUser(mCorpId);

        // send valid login, check for success and check for reset NOFLA & ALU
        assertNotNull("Valid login with removed lockout failed", mAuthAdapter.login(mUsername, mValidPass, true));
        assertUnlocked();
    }

    private void getSystemParameters() {

        mLockAccountDuration =
                SystemParameter.findIntValue(SystemParameter.Code.LOCK_ACCOUNT_DURATION);

        mMaxNumberOfFailedLoginAttempts =
                SystemParameter.findIntValue(SystemParameter.Code.MAX_NUMBER_OF_FAILED_LOGIN_ATTEMPTS);
    }

    private void assertLockValues(int expectedNumFailed, SpcfCalendar expectedLockedUntil) {
        PayrollServices.beginUnitOfWork();
        mAuthUser = AuthUser.findUser(mCorpId);
        assertEquals(expectedNumFailed, mAuthUser.getNumberOfFailedLoginAttempts());
        if (expectedLockedUntil == null){
            assertNull(mAuthUser.getAccountLockedUntil());
            PayrollServices.rollbackUnitOfWork();
            return;
        }

        assertNotNull(expectedLockedUntil);
        assertNotNull(mAuthUser.getAccountLockedUntil());

        // time are considered the same if they're within one second
        long timeDiff = Math.abs(expectedLockedUntil.getTimeInMilliseconds() -
                                         mAuthUser.getAccountLockedUntil().getTimeInMilliseconds());
        assert(timeDiff < 1000);
        PayrollServices.rollbackUnitOfWork();
    }

    private void assertUnlocked() {
        assertLockValues(0, null);
    }

    private void assertExceptionIsLockMessage(SAPException e) {
        assertEquals("Account locked out for " + mLockAccountDuration + " minutes due to excessive failed logins.",
                     e.getMessage());
    }

    private void lockTestAccount() throws Throwable {

        for (int i = 1; i <= mMaxNumberOfFailedLoginAttempts; i++) {
            mAuthAdapter.login(mUsername, mInvalidPass, true);
        }

        SpcfCalendar expectedLock = PSPDate.getPSPTime();
        expectedLock.addMinutes(mLockAccountDuration);

        try {
            mAuthAdapter.login(mUsername, mInvalidPass, true);
            fail("Login on locked account failed to trigger exception.");
        } catch (SAPException e) {
            assertExceptionIsLockMessage(e);
        }
        assertLockValues(mMaxNumberOfFailedLoginAttempts + 1, expectedLock);
    }
}
