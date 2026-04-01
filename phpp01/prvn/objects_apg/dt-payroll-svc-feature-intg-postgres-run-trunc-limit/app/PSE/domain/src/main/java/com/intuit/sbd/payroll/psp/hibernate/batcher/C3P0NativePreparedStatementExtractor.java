package com.intuit.sbd.payroll.psp.hibernate.batcher;

import com.mchange.v2.c3p0.C3P0ProxyStatement;
import org.hibernate.HibernateException;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;

public class C3P0NativePreparedStatementExtractor implements
		NativePreparedStatementExtractor {

	public PreparedStatement getNativePreparedStatement(
			final PreparedStatement preparedStatement) {
		try {
			final Method getRawStatementMethod = this.getClass().getMethod(
					"getRawStatement", new Class[] { PreparedStatement.class });
			return (PreparedStatement) ((C3P0ProxyStatement) preparedStatement)
					.rawStatementOperation(getRawStatementMethod, null,
							new Object[] { C3P0ProxyStatement.RAW_STATEMENT });
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

	public static PreparedStatement getRawStatement(
			final PreparedStatement preparedStatement) {
		return preparedStatement;
	}
}
