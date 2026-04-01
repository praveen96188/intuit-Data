package com.intuit.sbd.payroll.psp.hibernate.batcher;

import java.sql.PreparedStatement;

/**
 * Defines an interface for extracting the native <code>PreparedStatement</code>
 * wrapped in another <code>PreparedStatement</code>, usually backed by a
 * connection pool.
 */
public interface NativePreparedStatementExtractor {

	/**
	 * @param preparedStatement
	 *            <code>PreparedStatement</code> wrapping the native
	 *            <code>PreparedStatement</code>.
	 * @return the native <code>PreparedStatement/code>
	 */
	public PreparedStatement getNativePreparedStatement(
			PreparedStatement preparedStatement);

}
