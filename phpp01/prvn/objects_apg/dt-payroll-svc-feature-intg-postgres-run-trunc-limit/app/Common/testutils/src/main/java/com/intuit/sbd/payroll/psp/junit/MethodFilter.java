package com.intuit.sbd.payroll.psp.junit;

import org.junit.runner.manipulation.Filter;
import org.junit.runner.Description;
import java.util.Collection;

/**
 * This class is used in conjunction with <code>MethodSuite</code>.
 *
 * @author Wiktor Kozlik
 */
public class MethodFilter extends Filter {

    private Collection<Description> testMethodDescriptions;

    public MethodFilter(Collection<Description> pDescriptions) {
        testMethodDescriptions = pDescriptions;
    }

    @Override
    public boolean shouldRun(Description description) {
        if (description.isTest())
            return testMethodDescriptions.contains(description);
        for (Description each : description.getChildren())
            if (shouldRun(each))
                return true;
        return false;
    }

    @Override
    public String describe() {
        return String.format("Multiple methods");
    }
}
