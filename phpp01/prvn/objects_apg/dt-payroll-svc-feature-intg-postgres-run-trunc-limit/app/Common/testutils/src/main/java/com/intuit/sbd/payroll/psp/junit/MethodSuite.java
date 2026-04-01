package com.intuit.sbd.payroll.psp.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.MethodValidator;
import org.junit.internal.runners.TestClassRunner;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Request;
import org.junit.runner.Description;

/**
 * Using <code>MethodSuite</code> as a runner allows you to manually
 * build a suite containing tests from many methods in many classes. To use it, annotate a class
 * with <code>@RunWith(MethodSuite.class)</code> and <code>@SuiteMethods("TestClass1.testMethod", ...)</code>.
 * When you run this class, it will run the selected tests.
 *
 * @author Wiktor Kozlik
 */
public class MethodSuite extends TestClassRunner {
	/**
	 * The <code>SuiteMethods</code> annotation specifies the methods to be run when a class
	 * annotated with <code>@RunWith(MethodSuite.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SuiteMethods {
		public String[] value();
	}

	private static Set<Class<?>> parents = new HashSet<Class<?>>();

	private static Class<?> addParent(Class<?> parent) throws InitializationError {
		if (!parents.add(parent))
			throw new InitializationError(String.format("class '%s' (possibly indirectly) contains itself as a SuiteClass", parent.getName()));
		return parent;
	}

	public MethodSuite(Class<?> klass) throws InitializationError {
        super(addParent(klass), new FilterRequest(Request.classes(klass.getName(), getAnnotatedClasses(klass)), new MethodFilter(getAnnotatedMethods(klass))).getRunner());
        parents.remove(klass);
	}

	private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
		SuiteMethods annotation= klass.getAnnotation(SuiteMethods.class);
		if (annotation == null)
			throw new InitializationError(String.format("class '%s' must have a SuiteMethods annotation", klass.getName()));
        TreeSet<String> annotatedClassNames = new TreeSet<String>();
        ArrayList<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
        for (String s : annotation.value()) {
            Class<?> clazz = getClassFromLine(s);
            String className = clazz.getName();
            if (!annotatedClassNames.contains(className)) {
                annotatedClassNames.add(className);
                annotatedClasses.add(clazz);
            }

        }
        return annotatedClasses.toArray(new Class<?>[0]);
	}

    //get the class from the line.
    //line could either be fully.qualified.class.method_name
    //or fully.qualified.class
    private static Class<?> getClassFromLine(String s) throws InitializationError {
        Class<?> clazz=null;

        try {
            //first try the form class.method
            String className = getClassName(s);
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            //do nothing
        }
        if (clazz != null) {
            return clazz;
        }

        //if that doesn't work, see if that line was actually the name of the class itself
        try {
            clazz = Class.forName(s);
        } catch (ClassNotFoundException e) {
            throw new InitializationError(String.format("class '%s' not found", s));
        }
        return clazz;
    }

    private static Collection<Description> getAnnotatedMethods(Class<?> klass) throws InitializationError {
        SuiteMethods annotation= klass.getAnnotation(SuiteMethods.class);
		if (annotation == null)
			throw new InitializationError(String.format("class '%s' must have a SuiteMethods annotation", klass.getName()));
        Collection<Description> annotatedMethods = new ArrayList<Description>();

        for (String s : annotation.value()) {
            annotatedMethods.add(Description.createTestDescription(getClassFromLine(s), getMethodName(s)));
        }

        return annotatedMethods;
    }

    private static String getClassName(String annotation) {
        return annotation.substring(0, annotation.lastIndexOf("."));
    }

    private static String getMethodName(String annotation) {
        return annotation.substring(annotation.lastIndexOf(".") + 1);
    }

    @Override
	protected void validate(MethodValidator methodValidator) {
		methodValidator.validateStaticMethods();
		methodValidator.validateInstanceMethods();
	}
}
