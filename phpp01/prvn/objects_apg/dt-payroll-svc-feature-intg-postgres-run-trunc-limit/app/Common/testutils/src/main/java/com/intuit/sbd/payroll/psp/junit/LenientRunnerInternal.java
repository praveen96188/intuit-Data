package com.intuit.sbd.payroll.psp.junit;

import org.junit.Test;
import org.junit.internal.runners.TestIntrospector;
import org.junit.internal.runners.TestMethodRunner;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class LenientRunnerInternal extends Runner {

    private final List<Method> fTestMethods;
    private final Class<?> fTestClass;

    // This assumes that some containing runner will perform validation of the test methods
    public LenientRunnerInternal(Class<?> klass) {
        fTestClass= klass;
        fTestMethods= new TestIntrospector(getTestClass()).getTestMethods(Test.class);
    }

    @Override
    public void run(RunNotifier notifier) {
        if (fTestMethods.isEmpty()) {
            notifier.testAborted(getDescription(), new Exception("No runnable methods"));
        }

        for (Method method : fTestMethods) {
            runSingleTest(method, notifier);
        }

    }

    private void runSingleTest(Method method, RunNotifier notifier) {
        LenientRunNotifier myNotifier = new LenientRunNotifier(notifier);
        for (int i = 0; i < 5; i++) {
            invokeTestMethod(method, myNotifier);

            if (myNotifier.isIgnored()) {
                break;
            }

            if (myNotifier.getFailures() == 0) {
                break;
            }
        }

        if (myNotifier.getFailures() >= 4) {
            notifier.fireTestFailure(myNotifier.getFailure());
        }

        if (!myNotifier.isIgnored()) {
            myNotifier.testsDone();
        }
    }

    @Override
    public Description getDescription() {
        Description spec= Description.createSuiteDescription(getName());
        List<Method> testMethods= fTestMethods;
        for (Method method : testMethods)
            spec.addChild(methodDescription(method));
        return spec;
    }

    protected String getName() {
        return getTestClass().getName();
    }

    protected Object createTest() throws Exception {
        return getTestClass().getConstructor().newInstance();
    }

    protected void invokeTestMethod(Method method, RunNotifier notifier) {
        Object test;
        try {
            test= createTest();
        } catch (InvocationTargetException e) {
            notifier.testAborted(methodDescription(method), e.getCause());
            return;
        } catch (Exception e) {
            notifier.testAborted(methodDescription(method), e);
            return;
        }
        createMethodRunner(test, method, notifier).run();
    }

    protected TestMethodRunner createMethodRunner(Object test, Method method, RunNotifier notifier) {
        return new TestMethodRunner(test, method, notifier, methodDescription(method));
    }

    protected String testName(Method method) {
        return method.getName();
    }

    protected Description methodDescription(Method method) {
        return Description.createTestDescription(getTestClass(), testName(method));
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        for (Iterator<Method> iter= fTestMethods.iterator(); iter.hasNext();) {
            Method method= iter.next();
            if (!filter.shouldRun(methodDescription(method)))
                iter.remove();
        }
        if (fTestMethods.isEmpty())
            throw new NoTestsRemainException();
    }

    public void sort(final Sorter sorter) {
        Collections.sort(fTestMethods, new Comparator<Method>() {
            public int compare(Method o1, Method o2) {
                return sorter.compare(methodDescription(o1), methodDescription(o2));
            }
        });
    }

    protected Class<?> getTestClass() {
        return fTestClass;
    }

    public class LenientRunNotifier extends RunNotifier {

        private RunNotifier fEnclosedRunner;

        public LenientRunNotifier(RunNotifier pFEnclosedRunner) {
            fEnclosedRunner = pFEnclosedRunner;
        }

        private Description description;

        private int failures = 0;

        private Failure failure;

        private boolean isIgnored;

        public boolean isIgnored() {
            return isIgnored;
        }

        public Failure getFailure() {
            return failure;
        }

        public int getFailures() {
            return failures;
        }

        @Override
        public void fireTestFailure(Failure failure) {
            super.fireTestFailure(failure);
            this.failure = failure;
            failures++;
        }

        @Override
        public void fireTestRunStarted(Description description) {
            fEnclosedRunner.fireTestRunStarted(description);
        }

        @Override
        public void fireTestRunFinished(Result result) {
            fEnclosedRunner.fireTestRunFinished(result);
        }

        @Override
        public void fireTestStarted(Description description) throws StoppedByUserException {
            if (this.description == null) {
                this.description = description;
                fEnclosedRunner.fireTestStarted(description);
            }
        }

        @Override
        public void fireTestIgnored(Description description) {
            fEnclosedRunner.fireTestIgnored(description);
            isIgnored = true;
        }

        @Override
        public void fireTestFinished(Description description) {
            //do nothing per execution
        }

        public void testsDone() {
            fEnclosedRunner.fireTestFinished(description);
        }

    }
}