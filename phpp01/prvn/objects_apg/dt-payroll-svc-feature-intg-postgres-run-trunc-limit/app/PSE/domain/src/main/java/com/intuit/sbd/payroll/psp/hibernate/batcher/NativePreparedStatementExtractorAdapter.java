package com.intuit.sbd.payroll.psp.hibernate.batcher;

import org.hibernate.HibernateException;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/**
 * Base implementation that uses reflection on the wrapper
 * <code>PreparedStatement</code> object to access the native
 * <code>PreparedStatement</code>. By using reflection we avoid compile-time
 * dependencies to the connection pool libraries. Subclasses are responsible for
 * returning the class and method names specific to the connection pool library.
 */
public abstract class NativePreparedStatementExtractorAdapter implements
		NativePreparedStatementExtractor {

	private Method nativePreparedStatementMethod;

	protected NativePreparedStatementExtractorAdapter() {
		try {
			final Class nativePreparedStatementClass = Class.forName(this
					.getNativePreparedStatementClassName());
			this.setNativePreparedStatementMethod(nativePreparedStatementClass
					.getMethod(this.getNativePreparedStatementMethodName()));
		} catch (final Exception e) {
			throw new HibernateException(e);
		}
	}

	/**
	 * Uses reflection to invoke the method on preparedStatement.
	 * 
	 * @param preparedStatement
	 *            PreparedStatement wrapper specific to the connection pool
	 *            library
	 * @return the native prepared statement wrapped in
	 *         <code>preparedStatement</code>
	 */
	public PreparedStatement getNativePreparedStatement(
			final PreparedStatement preparedStatement) {
		try {
			return (PreparedStatement) this.getNativePreparedStatementMethod()
					.invoke(preparedStatement);
		} catch (final Exception e) {
			throw new HibernateException(e);
		}
	}

	/**
	 * Subclasses must return the class name of the
	 * <code>PreparedStatement</code> wrapper specific to the connection pool
	 * library.
	 */
	public abstract String getNativePreparedStatementClassName();

	/**
	 * Subclasses must return the method name, belonging to the class returned
	 * in {@link #getNativePreparedStatementClassName()}; when invoked on the
	 * prepared statement wrapper, this method returns the native prepared
	 * statement.
	 */
	public abstract String getNativePreparedStatementMethodName();

	private Method getNativePreparedStatementMethod() {
		return nativePreparedStatementMethod;
	}

	private void setNativePreparedStatementMethod(
			final Method nativePreparedStatementMethod) {
		this.nativePreparedStatementMethod = nativePreparedStatementMethod;
	}
}
