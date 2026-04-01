package com.intuit.sbd.payroll.psp.junit;

import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.TestClassRunner;

/**
 * User: dweinberg
 * Date: 7/23/13
 * Time: 1:56 PM
 *
 * Execute the test once.
 * If the first execution passes, pass the test and quit.
 * If the first execution fails, run 4 more executions and fail if it fails 4/5 times
 *
 * Basically designed for integration tests that have unreliable external dependencies
 *
 * Code is a copy/paste job from TestClassMethodsRunner since many methods protected
 * @see LenientRunnerInternal
 */

public class LenientRunner extends TestClassRunner {
    public LenientRunner(Class<?> klass) throws InitializationError {
        super(klass, new LenientRunnerInternal(klass));
    }
}
