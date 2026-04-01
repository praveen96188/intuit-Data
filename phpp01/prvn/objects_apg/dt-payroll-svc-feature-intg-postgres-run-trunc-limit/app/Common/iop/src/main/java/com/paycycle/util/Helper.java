/**
 * Helper.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */
package com.paycycle.util;


//import com.maxmind.geoip.Location;
//import com.maxmind.geoip.LookupService;
//import com.paycycle.biz.Company;
//import com.paycycle.biz.CompanyStatus;
//import com.paycycle.biz.Partner;
//import com.paycycle.biz.PartnerMgr;
//import com.paycycle.biz.Provider;
//import com.paycycle.data.DbUtil;
//import com.paycycle.email.MailManager;
//import com.paycycle.tx.CompanyTx;
//import com.paycycle.tx.UserTx;
//import com.paycycle.user.IPAddressInfo;
//import com.paycycle.user.Login;
//import com.paycycle.user.User;

import com.paycycle.user.UserException;

import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


//import java.util.Vector;

/**
 * Collection of helper functions
 */
public class Helper {
    //	static public class SQLParameterList<T> extends Vector<T>
    //	{
    //	}
    //
    //    public static Logger _log = AppMgr.getLogger(Helper.class);
    //
    //	public static Object refreshObject(Object o)
    //	{
    //		ReadObjectQuery query = new ReadObjectQuery();
    //		query.setSelectionObject(o);
    //		query.refreshIdentityMapResult();
    //        query.cascadePrivateParts();
    //		query.dontCheckCache();
    //		Object refreshed = executeQuery(query);
    //
    //		// Hacky solution for PD-9641.  Reset contractor sorting flag in companies
    //		// upon reloading because EclipseLink can't handle sorting.
    //		// Only do this on public for performance reasons.
    //		if (refreshed.getClass() == Company.class || refreshed.getClass() == Provider.class
    //				&& !AppMgr.isOperations()) {
    //			((Company)refreshed).resetContractorIndex(false);
    //		}
    //
    //		return refreshed;
    //	}
    //
    //	public static Object executeQuery(String queryName, Object[] args)
    //	{
    //		Vector vectorArgs = new Vector();
    //		for (int i = 0; i < args.length; i += 1)
    //		{
    //			vectorArgs.add(args[i]);
    //		}
    //		return executeQuery(queryName, vectorArgs);
    //	}
    //
    //	public static Object executeQuery(String queryName, Vector args)
    //	{
    //		return executeSavedQuery(AppMgr.getQueryDatabase(), queryName, args);
    //
    //	}
    //
    //	/**
    //	 * Execute query, stored in the public query database.
    //	 * @param session
    //	 * @param queryName
    //	 * @param args
    //	 * @return
    //	 */
    //	private static Object executeQuery(Session session, String queryName, Vector args)
    //	{
    //			return executeSavedQuery(session, AppMgr.getQueryDatabase().getQuery(queryName), args);
    //	}
    //
    //	/**
    //	 * Execute query, without further binding as the query parameters.
    //	 */
    //	private static Object executeSavedQuery(Session session, DatabaseQuery theQuery, Vector args)
    //	{
    //		long before = System.currentTimeMillis();
    //		Object result = null;
    //		try
    //		{
    //			result = session.executeQuery(theQuery, args);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		long after = System.currentTimeMillis();
    //		AppMgr.updateDbTime(after - before);
    //		return result;
    //	}
    //
    //
    //	/**
    //	 * Execute query, without further binding as the query parameters.
    //	 */
    //	private static Object executeSavedQuery(DatabaseQuery theQuery, Vector args)
    //	{
    //			return executeSavedQuery(AppMgr.getDbSession(), theQuery, args);
    //	}
    //
    //
    //	/**
    //	 * Execute query, without further binding as the query parameters.
    //	 */
    //	public static Object executeSavedQuery(QueryDatabase queryDb, String theQuery, Vector args)
    //	{
    //		Object result = null;
    //		Session session = null;
    //		try
    //		{
    //			session = AppMgr.getDbSession();
    //			result = executeSavedQuery(session, queryDb.getQuery(theQuery), args);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		finally
    //		{
    //			if (session != null) {
    //				session.release();
    //			}
    //		}
    //		return result;
    //
    //	}
    //
    //	/**
    //	 * Compares 2 objects for equality in a null-safe way.  They are equal if
    //	 * they are both empty (null or 0 length) or both are not empty and
    //	 * their .equals returns true.
    //	 *
    //	 * @param o1	The first object to compare.
    //	 * @param o2	The 2nd object to compare.
    //	 *
    //	 * @return	true if equal, false if not.
    //	 */
    //	public static boolean isEqual (Object o1, Object o2)
    //	{
    //		if (isEmpty(o1))
    //		{
    //			if (isEmpty(o2))
    //				return true;			// Both empty (equal).
    //			else
    //				return false;			// o1 empty, o2 not empty (not equal).
    //		}
    //		else
    //		{
    //			if (isEmpty(o2))
    //				return false;			// o1 not empty, o2 is empty (not equal).
    //			else
    //				return o1.equals(o2);	// Both not empty (compare them).
    //		}
    //	}
    //

    /**
     * Determine if an object is empty.
     */
    public static boolean isEmpty(Object o) {
        return (o == null) ? true : ((String.valueOf(o) == null) ? true : ((String.valueOf(o).length() == 0) ? true : false));
    }

    /**
     * Determine if an object is not empty.
     */
    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }

    public static <T> T isEmpty(T originalObject, T replacementObject) {
        return (Helper.isEmpty(originalObject) ? replacementObject : originalObject);
    }

    //
    //	/**
    //	 * Reads an object of class specified by className and identified by id.
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> T executeQuery (DatabaseQuery qry, Object...arguments)
    //	{
    //		return (T) executeQuery(qry, true, arguments);
    //	}
    //	public static <T> T executeQueryThrowError (DatabaseQuery qry, Object...arguments)
    //	{
    //		return (T) executeQueryThrowError(qry, true, arguments);
    //	}
    //
    //
    //	private static <T> T executeQuery(DatabaseQuery qry, boolean logError, Object...arguments)
    //	{
    //		return (T) executeQuery(AppMgr.getDbSession(), qry, false, logError, arguments);
    //	}
    //	private static <T> T executeQueryThrowError(DatabaseQuery qry, boolean logError, Object...arguments)
    //	{
    //		return (T) executeQuery(AppMgr.getDbSession(), qry, true, logError, arguments);
    //	}
    //
    //
    //	private static <T> T executeQuery (Session session, DatabaseQuery qry, boolean throwerror, boolean logError, Object...arguments)
    //	{
    //		Object result = null;
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			result = session.executeQuery(qry, setUpSQLQueryAndArguments(qry, qry.getSQLString(), arguments));
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			if (logError)
    //			{
    //				AppMgr.getLogger().error(ex);
    //			}
    //			if (throwerror && ex.toString().indexOf("No result sets were produced by") < 0) {
    //				throw new RuntimeException(ex.toString());
    //			}
    //		}
    //		finally
    //		{
    //			if (session != null) {
    //				session.release();
    //			}
    //		}
    //		return (T) result;
    //	}
    //	/**
    //	 * Execute a selecting raw SQL string. This returns a Collection of the Map representing the result set
    //	 * @param query
    //	 * @return
    //	 */
    //	public static Vector<Map> executeQuery (String query)
    //	{
    //		return (Vector<Map>)executeQuery(new DataReadQuery(query));
    //	}
    //
    //	public static Vector<Map> executeQueryThrowError (String query)
    //	{
    //		return (Vector<Map>)executeQueryThrowError(new DataReadQuery(query));
    //	}
    //
    //	/**
    //	 * This can be used to read a single data value (i.e. one field).
    //	 * A single data value is returned, or null if no rows are returned.
    //	 * @param query
    //	 * @return
    //	 */
    //	public static <T> T executeValueReadQuery(String query, Object...arguments)
    //	{
    //		return (T)executeQuery(new ValueReadQuery(query), arguments);
    //	}
    //
    //	public static <T> T executeValueReadQuery(String query, Vector arguments)
    //	{
    //		return (T)executeQuery(new ValueReadQuery(query), arguments.toArray());
    //	}
    //
    //	public static <T> T executeValueReadQuery(AppMgr.Catalog alternateCatalog, String query, Object...arguments)
    //	{
    //		return (T)executeQuery(AppMgr.getDbSession(alternateCatalog), new ValueReadQuery(query), false, true, arguments);
    //	}
    //
    //	public static Vector<Map<String,Object>> executeDataReadQuery(String query, Vector arguments)
    //	{
    //		return (Vector<Map<String,Object>>) executeQuery(new DataReadQuery(query), arguments.toArray());
    //	}
    //
    //	public static Vector<Map<String,Object>> executeDataReadQuery(String query, Object...arguments)
    //	{
    //		return (Vector<Map<String,Object>>) executeQuery(new DataReadQuery(query), arguments);
    //	}
    //
    //	public static Vector<Map<String,Object>> executeDataReadQuery(AppMgr.Catalog alternateCatalog, String query, Object...arguments)
    //	{
    //		return (Vector<Map<String,Object>>) executeQuery(AppMgr.getDbSession(alternateCatalog), new DataReadQuery(query), false, false, arguments);
    //	}
    //	/**
    //	 * Can be used to modify the database
    //	 *
    //	 * @param <T>
    //	 * @param query
    //	 * @return
    //	 */
    //	public static <T> T executeModifyQuery(String query,Object...arguments)
    //	{
    //		return (T) executeQuery(new DataModifyQuery(query), true, arguments);
    //	}
    //
    //	public static <T> T executeModifyQuery(String query, boolean logError, Object...arguments)
    //	{
    //		return (T) executeQuery(new DataModifyQuery(query), logError, arguments);
    //	}
    //
    //	public static <T> T executeModifyQuery(AppMgr.Catalog alternateCatalog, String query,Object...arguments)
    //	{
    //		return (T) executeQuery(AppMgr.getDbSession(alternateCatalog), new DataModifyQuery(query), false, true, arguments);
    //	}
    //
    //	/**
    //	 * This can be used to read a single column of data i.e. it returns list of data values.
    //	 * @param <T>
    //	 * @param query
    //	 * @return
    //	 */
    //	public static <T> List<T> executeSingleColumnQuery(AppMgr.Catalog alternateCatalog, String query, Object...arguments)
    //	{
    //		return (List<T>)executeQuery(AppMgr.getDbSession(alternateCatalog), new DirectReadQuery(query), false, true, arguments);
    //	}
    //
    //	public static <T> List<T> executeSingleColumnQuery(String query, Object...arguments)
    //	{
    //		return (List<T>)executeQuery(new DirectReadQuery(query), arguments);
    //	}
    //
    //	public static <T> Vector<T> readAllObjects(Class klass, String sql, boolean refresh, Object...arguments)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		Vector<T> result = readAllObjects(session, klass, sql, refresh, arguments);
    //		session.release();
    //		return result;
    //	}
    //
    //	private static <T> Vector<T> readAllObjects(Session session, Class klass, String sql, boolean refresh, Object...arguments)
    //	{
    //		Vector<T> result = null;
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			ReadAllQuery qry = new ReadAllQuery();
    //			qry.setReferenceClass(klass);
    //
    //			if (AppMgr.isOperations()) {
    //                // The intention with passing in SQL is for it to always
    //                // go to the DB.  Along with that, cascade to all parts.
    //				refreshCache(qry, true, ObjectLevelReadQuery.DoNotCheckCache, klass);
    //			} else if(refresh)
    //			{
    //				qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //				qry.refreshIdentityMapResult();
    //			}
    //			result = (Vector<T>) session.executeQuery(qry,setUpSQLQueryAndArguments(qry, sql, arguments));
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		return result;
    //	}
    //
    //	/**
    //	 * Read all of the instances of the class from the database.
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> Vector<T> readAllObjects(Class klass, String sql, Object... arguments)
    //	{
    //		Vector<T> result = null;
    //		Session session = null;
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			ReadAllQuery qry = new ReadAllQuery();
    //			qry.setReferenceClass(klass);
    //			qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //
    //			if(AppMgr.isOperations()) {
    //                // This is typically used by EFile and EPayment processing, etc.
    //                // It needs to bypass the cache and cascade through all parts,
    //                // and the intention with passing in SQL is for it to always
    //                // go to the DB.
    //				refreshCache(qry, true, ObjectLevelReadQuery.DoNotCheckCache, klass);
    //			}
    //
    //			session = AppMgr.getDbSession();
    //			result = (Vector<T>)session.executeQuery(qry, setUpSQLQueryAndArguments(qry, sql, arguments));
    //			long after = System.currentTimeMillis();
    //
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		finally
    //		{
    //			if (session != null) {
    //				session.release();
    //			}
    //		}
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads all objects specified by className and expression,
    //	 * using the specified session.
    //	 */
    //	private static <T> Vector<T> readAllObjects (Session session, Class klass, Expression exp, boolean refresh, String[] ascendingOrdering)
    //	{
    //		Vector<T> result = null;
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			ReadAllQuery qry = new ReadAllQuery();
    //			qry.setReferenceClass(klass);
    //			if (exp != null) {
    //				qry.setSelectionCriteria(exp);
    //			}
    //
    //			if (ascendingOrdering != null)
    //			{
    //				for (String ordering : ascendingOrdering)
    //				{
    //					qry.addAscendingOrdering(ordering);
    //				}
    //			}
    //
    //			/* TODO: Fix later. For now, do not use in-memory query for Paychecks */
    //			if (klass != com.paycycle.payroll.Paycheck.class)
    //			{
    //				qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //			}
    //
    //			if (refresh)
    //			{
    //				qry.refreshIdentityMapResult();
    //				qry.cascadeAllParts();
    //			}
    //
    //			if (AppMgr.isOperations()) {
    //				refreshCache(qry, false, ObjectLevelReadQuery.CheckCacheThenDatabase, klass);
    //			}
    //
    //			result = (Vector<T>) session.executeQuery(qry);
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads all objects specified by className and expression,
    //	 * using the specified session.
    //	 */
    //	public static <T> Vector<T> readAllObjects (Session session, Class klass, Expression exp)
    //	{
    //		return readAllObjects(session, klass, exp, false, null);
    //	}
    //
    //	/**
    //	 * Read all of the instances of the class from the database
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> Vector<T> readAllObjects(Class klass, Expression exp)
    //	{
    //		return readAllObjects(klass, exp, false, null);
    //	}
    //
    //	public static <T> Vector<T> readAllObjects(Class klass, Expression exp, String[] ascendingOrdering)
    //	{
    //		return readAllObjects(klass, exp, false, ascendingOrdering);
    //	}
    //
    //	/**
    //	 * Read all of the instances of the class from the database
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> Vector<T> readAllObjects(Class klass, Expression exp, boolean refresh)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		Vector<T> result = readAllObjects(session, klass, exp, refresh, null);
    //		session.release();
    //		return result;
    //	}
    //
    //	public static <T> Vector<T> readAllObjects(Class klass, Expression exp, boolean refresh, String[] ascendingOrdering)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		Vector<T> result = readAllObjects(session, klass, exp, refresh, ascendingOrdering);
    //		session.release();
    //		return result;
    //	}
    //
    //	/**
    //	 * Read all of the instances of the class from the database
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> Vector<T> readAllObjects(Class klass)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		Vector result = readAllObjects(session, klass, null);
    //		session.release();
    //		return result;
    //	}
    //
    //	/**
    //	 * Read all of the instances of the class from the database
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> T readObject (Class klass, Expression exp)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		T result = (T) readObject(session, klass, exp);
    //		session.release();
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads an object of class specified by className and identified by id.
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> T readObject (Class klass, long id)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		T result = (T) readObject(session, klass, id);
    //		session.release();
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads an object of class specified by className and full SQL string.
    //	 * Creates a session and release it after the read.
    //	 */
    //	public static <T> T readObject(Class klass, String sql, Object... arguments)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		T result = (T) readObject(session, klass, sql, arguments);
    //		session.release();
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads an object of class specified by className and identified by id,
    //	 * using the specified session.
    //	 */
    //	public static <T> T readObject(Session session, Class klass, long id)
    //	{
    //		T result = null;
    //		AppMgr.getLogger().debug("Start readObject(), " + klass + ", " + id);
    //		try
    //		{
    //			Vector k = new Vector(1);
    //			k.add(new Long(id));
    //			long before = System.currentTimeMillis();
    //			ReadObjectQuery qry = new ReadObjectQuery();
    //			qry.setReferenceClass(klass);
    //			qry.setSelectionKey(k);
    //
    //			if(AppMgr.isOperations()) {
    //				refreshCache(qry, false, ObjectLevelReadQuery.CheckCacheThenDatabase, klass);
    //			} else
    //			{
    //				qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //			}
    //			result = (T) session.executeQuery(qry);
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		AppMgr.getLogger().debug("End readObject(), " + klass + ", " + id);
    //
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads an object of class specified by className and expression,
    //	 * using the specified session.
    //	 */
    //	public static <T> T readObject (Session session, Class klass, Expression exp)
    //	{
    //		return (T) Helper.readObject(session, klass, exp, true);
    //	}
    //
    //	public static <T> T readObject (Class klass, Expression exp, boolean checkCache)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		T returnValue = (T) Helper.readObject(session, klass, exp, checkCache);
    //		session.release();
    //		return returnValue;
    //	}
    //
    //		private static <T> T readObject (Session session, Class klass, Expression exp, boolean checkCache)
    //	{
    //		T result = null;
    //		AppMgr.getLogger().debug("Start readObject(), " + klass);
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			ReadObjectQuery qry = new ReadObjectQuery();
    //			qry.setReferenceClass(klass);
    //			qry.setSelectionCriteria(exp);
    //
    //			if (checkCache)
    //			{
    //				/* TODO: Fix later. For now, do not use in-memory query for Paychecks */
    //				if (klass != com.paycycle.payroll.Paycheck.class) {
    //					qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //				}
    //
    //				if(AppMgr.isOperations()) {
    //					refreshCache(qry, false, ObjectLevelReadQuery.CheckCacheThenDatabase, klass);
    //				}
    //			}
    //			else
    //			{
    //				qry.setCacheUsage(ObjectLevelReadQuery.DoNotCheckCache);
    //			}
    //
    //			result = (T) session.executeQuery(qry);
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //
    //		AppMgr.getLogger().debug("End readObject(), " + klass);
    //		return result;
    //	}
    //
    //	/**
    //	 * Reads an object of class specified by className based on the full SQL string,
    //	 * using the specified session.
    //	 */
    //	private static <T> T readObject (Session session, Class klass, String sql, Object... arguments)
    //	{
    //		T result = null;
    //		AppMgr.getLogger().debug("Start readObject(), " + klass);
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			ReadObjectQuery qry = new ReadObjectQuery();
    //			qry.setReferenceClass(klass);
    //
    //			if(AppMgr.isOperations()) {
    //                // This is typically used by EFile processing, Billing, etc.
    //                // It needs to bypass the cache and cascade through all parts,
    //                // and the intention with passing in SQL is for it to always
    //                // go to the DB.
    //				refreshCache(qry, true, ObjectLevelReadQuery.DoNotCheckCache, klass);
    //			} else {
    //				qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //			}
    //
    //			result = (T) session.executeQuery(qry, setUpSQLQueryAndArguments(qry, sql, arguments));
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //
    //		AppMgr.getLogger().debug("End readObject(), " + klass);
    //		return result;
    //	}
    //
    //	public static Integer deleteAllObjects(Expression exp, Class klass)
    //	{
    //		Session session = AppMgr.getDbSession();
    //		Integer result = deleteAllObjects(session, exp, klass);
    //		session.release();
    //		return result;
    //	}
    //
    //	public static Integer deleteAllObjects(Session session, Expression exp, Class klass)
    //	{
    //		Integer result = null;
    //		try
    //		{
    //			long before = System.currentTimeMillis();
    //			DeleteAllQuery qry = new DeleteAllQuery();
    //			qry.setReferenceClass(klass);
    //			if (exp != null) {
    //				qry.setSelectionCriteria(exp);
    //			}
    //			qry.cascadeAllParts();
    //			result = (Integer) session.executeQuery(qry);
    //			long after = System.currentTimeMillis();
    //			AppMgr.updateDbTime(after - before);
    //		}
    //		catch (Exception ex)
    //		{
    //			AppMgr.getLogger().error(ex);
    //		}
    //		return result;
    //	}
    //
    //	private static Vector setUpSQLQueryAndArguments(DatabaseQuery qry, String sql, Object[] arguments)
    //	{
    //		Vector result = new Vector();
    //		String newSQL = sql;
    //		List<Object> argumentList = new Vector<Object>();
    //
    //		if (arguments.length == 1 && arguments[0] instanceof SQLParameterList)
    //		{
    //			argumentList = (List<Object>) arguments[0];
    //		}
    //		else
    //		{
    //			for (Object argument : arguments)
    //			{
    //				argumentList.add(argument);
    //			}
    //		}
    //
    //		if (arguments.length > 0)
    //		{
    //			int index = 1;
    //			for (Object argument : argumentList)
    //			{
    //				if (argument instanceof List)
    //				{
    //					if (((List) argument).size() > 100) {
    //						Object[] argumentArray = new Object[((List) argument).size()];
    //						for (int argumentIndex = 0; argumentIndex < ((List) argument).size(); argumentIndex++) {
    //							argumentArray[argumentIndex] = ((List) argument).get(argumentIndex);
    //						}
    //						newSQL = newSQL.replaceFirst("#P\\d*", DbUtil.SQLListFromObjects(argumentArray));
    //					} else {
    //						int subIndex = 1;
    //						List<String> argumentNames = new Vector<String>();
    //						for (Object subArgument : (List) argument)
    //						{
    //							String argumentName = "RP"+index+"_"+subIndex;
    //							argumentNames.add("#"+argumentName);
    //							qry.addArgument(argumentName);
    //							result.add(subArgument);
    //							subIndex++;
    //						}
    //						newSQL = newSQL.replaceFirst("#P\\d*", StringUtil.joinListIntoString(argumentNames, ","));
    //					}
    //				}
    //				else
    //				{
    //					String argumentName = "RP"+index;
    //					qry.addArgument(argumentName);
    //					result.add(argument);
    //					newSQL = newSQL.replaceFirst("#P\\d*", "#"+argumentName);
    //				}
    //				index++;
    //			}
    //			qry.bindAllParameters();
    //		}
    //
    //		qry.setSQLString(newSQL);
    //
    //		return result;
    //	}
    //
    //
    //	/*
    //	 * Gets the Field object for a field in a class.  Supports dotted and array notation for the
    //	 * field name.  This method does not look at actual objects.  Rather, it looks only at the
    //	 * Class meta data to get the Field object for a dotted name.
    //	 *
    //	 * @param className The fully qualified name of the class (e.g. "com.paycycle.util.Helper").
    //	 * @param fieldName The name of the property (can be dotted: employeeModel.workAddress.state)
    //	 *
    //	 * @returns Field object if found or null if not found (or class doesn't exist).
    //	 */
    //	public static Field getField (String className, String fieldName)
    //	{
    //		try
    //		{
    //			// Get the Class object for this name - throws an exception if it doesn't exist.
    //			Class klass = Class.forName(className);
    //
    //			int breakAt = StringUtil.indexOf(fieldName, "[.", 0);
    //			if (breakAt >= 0)
    //			{
    //				boolean isArray = (fieldName.charAt(breakAt) == '[');
    //
    //				// Name is dotted, break into parts.
    //				String partOne = fieldName.substring(0, breakAt);
    //				String partTwo = fieldName.substring(isArray ? fieldName.indexOf(']') + 2 : breakAt + 1, fieldName.length());
    //
    //				// Get the field for part one (using recursion).
    //				Field partOneField = getField (className, partOne);
    //
    //				// If we didn;t find it, we're done.
    //				if (partOne == null) {
    //					return null;
    //				}
    //
    //				// We did find it, so get its class name and recurse.
    //				String partOneClassName = isArray ? partOneField.getType().getComponentType().getName() : partOneField.getType().getName();
    //
    //				// Call ourself again to get the field of the next part.
    //				return getField (partOneClassName, partTwo);
    //			}
    //			else
    //			{
    //				// Not dotted, so just get the field for the class (and it's super classes).
    //				do
    //				{
    //					Field fieldList[] = klass.getDeclaredFields();
    //		            for (Field field : fieldList)
    //		            {
    //		            	if (field.getName().equals(fieldName))
    //		            		return field;
    //		            }
    //
    //		            // Not found in that class - go to the next super.
    //		            klass = klass.getSuperclass();
    //				} while (klass != null);
    //
    //	            return null;
    //			}
    //		}
    //		catch (ClassNotFoundException ex)
    //		{
    //			// Field or class not found.
    //			return null;
    //		}
    //	}
    //
    //	/**
    //	 * Determine if a class has a named field.  Supports dotted notation for the field name.
    //	 *
    //	 * @param className The fully qualified name of the class (e.g. "com.paycycle.util.Helper").
    //	 * @param fieldName The name of the property (can be dotted: employeeModel.workAddress.state)
    //	 *
    //	 * @return True if the field exists, false if it doesn't.
    //	 */
    //	public static boolean fieldExists(String className, String fieldName)
    //	{
    //		// Get the field.
    //		Field field = getField (className, fieldName);
    //
    //		// If we got it, it exists.
    //		return (field != null);
    //	}
    //
    //	/*
    //	 * Gets the value of an object in a class or super class.  Supports dotted and array
    //	 * notation for the field name.  Supports accessing private and protected fields.  Supports
    //	 * calling the getter method when the field itself can not be found. Supports traversing
    //	 * through ValueHolder objects (if a field is a ValueHolder, then this method will call
    //	 * its "getValue()" method to get the object the ValueHolder points to in the field chain).
    //	 * BASICALLY SUPPORTS ALL REFLECTION FEATURES.
    //	 *
    //	 * @param obj		The object to get the field value from.
    //	 * @param fieldName The name of the property (can be dotted: employeeModel.workAddress.state)
    //	 *
    //	 * @returns The value of the field or null if not found.
    //	 */
    //	public static Object getFieldValue (Object obj, String fieldName)
    //	{
    //		try
    //		{
    //			// Initialize.
    //			Class klass = obj.getClass();
    //
    //			int breakAt = StringUtil.indexOf(fieldName, "[.", 0);
    //			if (breakAt >= 0)
    //			{
    //				boolean isArray = (fieldName.charAt(breakAt) == '[');
    //
    //				// Name is dotted, break into parts.
    //				String partOne = fieldName.substring(0, breakAt);
    //				String partTwo = fieldName.substring(isArray ? fieldName.indexOf(']') + 2 : breakAt + 1, fieldName.length());
    //
    //				// Get the object for part one (using recursion).
    //				Object partOneObj = getFieldValue (obj, partOne);
    //
    //				// If we didn't find it, we're done.
    //				if (partOneObj == null)
    //					return null;
    //
    //				// If this is an array, get the right element.
    //				if (isArray)
    //				{
    //					Object[] theArray = ((Object []) partOneObj);
    //					int index = Integer.parseInt(fieldName.substring (breakAt + 1, fieldName.indexOf(']')));
    //
    //					// Make sure we don't go out of bounds.
    //					if (index < theArray.length)
    //					{
    //						// Get the object at that position in the array.
    //						partOneObj = ((Object []) partOneObj)[index];
    //
    //						// If nothing at that position, we're done.
    //						if (partOneObj == null)
    //							return null;
    //					}
    //					else
    //					{
    //						// Index is beyond end of array.
    //						return null;
    //					}
    //				}
    //
    //				// Call ourself again to get the field of the next part.
    //				return getFieldValue (partOneObj, partTwo);
    //			}
    //			else
    //			{
    //				// If the object is a ValueHolder, then skip over it by
    //				// getting the object that it is a holder of.
    //				if (implementsInterface (obj, ValueHolderInterface.class) ||
    //					implementsInterface (obj, WeavedAttributeValueHolderInterface.class))
    //				{
    //					obj = klass.getMethod("getValue").invoke(obj);
    //					if (obj == null)
    //						return null;
    //					klass = obj.getClass();
    //				}
    //
    //				// Loop through our cache of
    //				FieldOrMethod fieldOrMethod = FieldOrMethod.getFieldOrMethod(klass, fieldName);
    //				if (fieldOrMethod == null)
    //					return null;
    //
    //				// Get the field value (may call getter or access public member).
    //				Object result = fieldOrMethod.get(obj);
    //
    //				// The field itself might be a ValueHolder.  If so, call getValue to get the actual object.
    //				if (result != null && (implementsInterface (result, ValueHolderInterface.class) || implementsInterface (result, WeavedAttributeValueHolderInterface.class)))
    //					result = result.getClass().getMethod("getValue").invoke(result);
    //
    //				return result;
    //			}
    //		}
    //		catch (InvocationTargetException ex)
    //		{
    //			// Should not happen since we check that it is a ValueHolderInterface before calling the getMethod().invoke().
    //			return null;
    //		}
    //		catch (NoSuchMethodException ex)
    //		{
    //			// Should not happen since we check that it is a ValueHolderInterface before calling getMethod.
    //			return null;
    //		}
    //		catch (IllegalAccessException ex)
    //		{
    //			// Should not happen since we call setAccessible(true).
    //			return null;
    //		}
    //	}
    //
    //	/**
    //	 * Calls the "getter" method on an object with the parameters passed.
    //	 * For example, getField(obj, "xyz", new Long(7)) will essentially call
    //	 * getXyz(new Long(7)) to get the value.  Does NOT support dotted
    //	 * notation, array notation or traversal through ValueHolder objects.
    //	 *
    //	 * @param obj 		The source object.
    //	 * @param name 		The member variable or property name.
    //	 * @param args 		The argument list.
    //	 *
    //	 * @return The value returned from the getter method.
    //	 */
    //	public static Object getFieldValue(Object obj, String name, Object[] args) throws Exception
    //	{
    //		Object result = null;
    //
    //		// First try to get the method with the get<Method> pattern
    //		try
    //		{
    //			// Capitalize first letter of name
    //			String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    //			result = invoke(obj, methodName, args);
    //		}
    //		catch (NoSuchMethodException ex)
    //		{
    //			// Next try the method name provided as-is
    //			result = invoke(obj, name, args);
    //		}
    //		return result;
    //	}
    //
    //	/*
    //	 * Determines if the object passed in implements the interface passed in.
    //	 * Checks the interfaces on all levels of the object's class and then
    //	 * checks all levels of the interface's supers.
    //	 *
    //	 * @param obj	The object to check the to see if an interface is implemented.
    //	 * @param iFace	The interface to check for.
    //	 *
    //	 * @returns true if the interface is implemented, false if not
    //	 */
    //	static boolean implementsInterface (Object obj, Class iFace)
    //	{
    //		// Loop through this object's class and super classes.
    //		Class superClass = obj.getClass();
    //		do
    //		{
    //			// For each level of class in obj, check its interfaces and super interfaces.
    //			Class[] superClassInterfaces = superClass.getInterfaces();
    //			for (Class superClassInterface : superClassInterfaces)
    //			{
    //				Class superFace = superClassInterface;
    //				do
    //				{
    //					if (superFace.equals(iFace))
    //						return true;
    //					superFace = superFace.getSuperclass();
    //				} while (superFace != null);
    //			}
    //			superClass = superClass.getSuperclass();
    //		} while (superClass != null);
    //
    //		return false;
    //	}
    //
    //	/*
    //	 * Gets all Fields objects in obj where obj have the annotationClass
    //	 * annotation.
    //	 *
    //	 * @param obj			The object to get the field value from.
    //	 * @param annotation	The annotaion to find on fields.
    //	 *
    //	 * @returns Array of Field objects that have the annotation or null if none.
    //	 */
    //	public static <T extends Annotation> Field[] getFieldsWithAnnotation (Object obj, Class<T> annotationClass)
    //	{
    //		Vector<Field> foundFields = new Vector<Field>();
    //		try
    //		{
    //			// Loop through all fields in the object.
    //			Class klass = obj.getClass();
    //			do
    //			{
    //				Field fieldList[] = klass.getDeclaredFields();
    //				for (Field field : fieldList)
    //				{
    //					// Get the annotations from the field.
    //					Annotation annotation = field.getAnnotation(annotationClass);
    //					if (annotation != null)
    //					{
    //						foundFields.add(field);
    //					}
    //				}
    //				// Move onto the next super.
    //				klass = klass.getSuperclass();
    //			} while (klass != null);
    //		}
    //		catch (Exception ex)
    //		{
    //			return null;
    //		}
    //
    //		if (foundFields.size() > 0)
    //			return (Field[]) foundFields.toArray();
    //		else
    //			return null;
    //	}
    //

    /**
     * Invoke a single argument method on the target object using reflection.
     */
    public static final Object invoke(Object target, String methodName, Object[] args) throws Exception {
        Method m = getMethod(target.getClass(), methodName, args);

        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException ex) {
            /* There is a known bug in the JDK where reflection does not work on some objects
             * because the object's interface does not return the most accessible method; here's
             * a workaround so we don't unintentionally break forms
             */
            return invokeInnerMethodOrInterface(m, target, methodName, args);
        }
    }

    //
    //	/**
    //	 * Invoke a method and does not throw if there's an erro.  Returns null if the method does
    //	 * not exist.  Returns what the method returns if it did exist.  You cannot tell the difference
    //	 * between the 2.
    //	 */
    //	public static final Object invokeNoThrow (Object target, String methodName, Object...args)
    //	{
    //		Method m = null;
    //
    //		try
    //		{
    //			m = getMethod (target.getClass(), methodName, args);
    //			return m.invoke (target, args);
    //		}
    //		catch (IllegalAccessException ex)
    //		{
    //			/* There is a known bug in the JDK where reflection does not work on some objects
    //			 * because the object's interface does not return the most accessible method; here's
    //			 * a workaround so we don't unintentionally break forms
    //			 */
    //			try
    //			{
    //				return invokeInnerMethodOrInterface(m, target, methodName, args);
    //			}
    //			catch (Exception ex1)
    //			{
    //				return null;
    //			}
    //		}
    //		catch (Exception ex)
    //		{
    //			return null;
    //		}
    //	}
    //

    /**
     * Invokes a inner method or interface.  Used when regular invoke fails.
     */
    public static final Object invokeInnerMethodOrInterface(Method m, Object target, String methodName, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        /* There is a known bug in the JDK where reflection does not work on some objects
         * because the object's interface does not return the most accessible method; here's
         * a workaround so we don't unintentionally break forms
         */
        Class currentClass = target.getClass();

        while (currentClass != null) {
            Method innerMethod = currentClass.getMethod(m.getName(), m.getParameterTypes());

            if (!m.equals(innerMethod)) {
                return innerMethod.invoke(target, args);
            }

            // try invoking the method for this class's interfaces
            Class[] intfaces = currentClass.getInterfaces();

            for (int i = 0; i < intfaces.length; i++) {
                Method interfaceMethod = intfaces[i].getMethod(m.getName(), m.getParameterTypes());

                return interfaceMethod.invoke(target, args);
            }

            // we haven't found an accessible method yet, so now try any super classes
            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    //
    //	/**
    //	 * Using reflection get the variable value, when variable is created runtime.
    //	 */
    //	public static final Object getVariable(String clazz, String var, Object instance)
    //								throws Exception
    //	{
    //		Field field = (Class.forName(clazz)).getField(var);
    //		return field.get(instance);
    //	}
    //
    //	/**
    //	 * Invoke a single argument method on the target object using reflection.
    //	 */
    //	public static final Object invokeStatic (String className,
    //									String methodName, Object[] args)
    //							throws Exception
    //	{
    //		Method m = getMethod (Class.forName (className), methodName, args);
    //		return m.invoke (null, args);
    //	}
    //

    public static final Method getMethod(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        // Do short cut for methods with no arguments
        if (args == null) {
            return clazz.getMethod(name, (Class[]) null);
        }

        ArrayList filter = new ArrayList();
        Method[] methods = clazz.getMethods();

        // Filter out methods that don't match name
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];

            if (m.getName().equals(name)) {
                filter.add(m);
            }
        }

        // Filter out methods that don't match the parameters
        for (Iterator it = filter.iterator(); it.hasNext();) {
            Method m = (Method) it.next();
            Class[] params = m.getParameterTypes();

            // Check for param count
            if (params.length != args.length) {
                it.remove();
            } else {
                // Check for param type match
                for (int i = 0; i < args.length; i++) {
                    boolean match = false;
                    Object arg = args[i];

                    if (arg == null) {
                        continue;
                    }

                    Class c = arg.getClass();
                    Class p = params[i];

                    // Check for class hierarchy on argument
                    do {
                        if (p.isAssignableFrom(c)) {
                            match = true;
                        }
                    } while (((c = c.getSuperclass()) != null) && !match);

                    if (!match && !checkPrimitive(args[i].getClass()).equals(p)) {
                        it.remove();

                        break;
                    }
                }
            }
        }

        if (filter.size() == 0) {
            String list = "";

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];

                if (i > 0) {
                    list += ", ";
                }

                list += ((arg == null) ? "null" : arg.getClass().getName());
            }

            throw new NoSuchMethodException("Cannot find " + name + "(" + list + ")");
        }

        return (Method) filter.get(0);
    }

    /**
     * Check if the class representing the passed argument is assignable to
     * primitive types.  If it is true, than the primitive class type is
     * returned, else the original class is returned.
     */
    public static Class checkPrimitive(Class clazz) {
        Class[][] primitives = {{Integer.class, Integer.TYPE}, {Boolean.class, Boolean.TYPE}, {Byte.class, Byte.TYPE}, {Short.class, Short.TYPE}, {Character.class, Character.TYPE}, {Long.class, Long.TYPE}, {Float.class, Float.TYPE}, {Double.class, Double.TYPE}};

        for (int i = 0; i < primitives.length; i++) {
            if (clazz.isAssignableFrom(primitives[i][0])) {
                return primitives[i][1];
            }
        }

        return clazz;
    }

    //	/**
    //	 * Determine the number of groups needed to contain the specified items.
    //	 */
    //	public static int getGroups (int numItems, int maxItemsPerGroup)
    //	{
    //		int result = numItems / maxItemsPerGroup;
    //		return ((numItems % maxItemsPerGroup) > 0) ? result + 1 : result;
    //	}
    //
    //	private static boolean gDateStamp=true;
    //
    //	/**
    //	 *  Prints a string to the debug file, ending with a carriage return.
    //	 */
    //	public static void debugln(String s) {
    //		debugln("pchistory",s);		// Default version.  For backwards compatability.
    //	}
    //
    //	public static void debugln(String logger,String s) {
    //		AppMgr.getLogger("com.paycycle."+logger).debug(s);
    //	}
    //
    //	public static void error(String logger,String s) {
    //		AppMgr.getLogger("com.paycycle."+logger).error(s);
    //	}
    //
    //	/**
    //	 * Dumps an exception to the debug file.
    //	 * @param e   Exception to be dumped.
    //	 */
    //	public static void debugDumpException( Exception e ) {
    //		Helper.error("pchistory","Exception ("+new Date()+") -----------------------");
    //		Helper.error("pchistory",StringUtil.getStackTrace(e));
    //	}
    //
    //	/**
    //	 * Dumps the parameters from an HttpServletRequest object to the debug file.
    //	 * @param  request    The request object contains the stuff to be dumped.
    //	 */
    //	public static void debugDumpRequest( HttpServletRequest request ) {
    //		Enumeration e = request.getParameterNames();
    //
    //		Helper.debugln(" controller param dump ("+new Date()+")");
    //		while (e.hasMoreElements()) {
    //			String name = (String)e.nextElement();
    //			String value = request.getParameter(name);
    //			Helper.debugln("   " + name + " = [" + value + "]");
    //		}
    //    }
    //
    //    /**
    //	 * Gets a human readable message that describes who the current
    //	 * method caller is and how it was called.
    //	 * @return the message.
    //     */
    //	public static String getCallerMessage(int callerDepth) {
    //		return getCallerMessageString(null, callerDepth++);
    //	}
    //	/**
    //	 * Gets a human readable message that describes who the current
    //	 * method caller is and how it was called.
    //	 * @param callInfo Some call specific information to include
    //	 * in the message.
    //	 * @return the message.
    //	 */
    //	public static String getCallerMessage(String callInfo, int callerDepth) {
    //		return getCallerMessageString(callInfo, callerDepth++);
    //	}
    //
    //	private static String getCallerMessageString(String callInfo, int callerDepth) {
    //    	StringBuilder logMsg = new StringBuilder();
    //    	logMsg.append(StringUtil.getCaller(callerDepth++, false));
    //    	logMsg.append(" called");
    //    	if (callInfo != null && callInfo.length() != 0) {
    //	    	logMsg.append(" (");
    //	    	logMsg.append(callInfo);
    //	    	logMsg.append(")");
    //    	}
    //    	String callStack = StringUtil.getMiniStackTrace(callerDepth);
    //    	if (callStack.length() > 0) {
    //    		logMsg.append(" by ");
    //    		logMsg.append(callStack);
    //    	}
    //    	return logMsg.toString();
    //	}
    //
    //	/**
    //	 *  Converts all the parameters in a request object into a string that is usable in a url.
    //	 *  @param  request      The request object
    //	 *  @param  excludeHash  Hash of values not be included (can be null)
    //	 */
    //	public static String makeUrlParameters(HttpServletRequest request, Hashtable excludeHash) {
    //		Enumeration e = request.getParameterNames();
    //		String ret = "";
    //
    //		Helper.debugln(" controller param dump ("+new Date()+")");
    //		while (e.hasMoreElements()) {
    //			String name = (String)e.nextElement();
    //			if( excludeHash!=null && excludeHash.get(name)!=null ) {
    //				continue;
    //			}
    //			String value = request.getParameter(name);
    //			try {
    //				ret = ret + "&" + name + "=" + URLEncoder.encode(value, "UTF-8");
    //			} catch (java.io.UnsupportedEncodingException ex) {
    //				ret = ret + "&" + name + "=" + value;
    //			}
    //		}
    //		if( ret.length()==0 ) {
    //			return "";
    //		}
    //		return ret.substring(1);
    //	}
    //
    //
    //	/**
    //	 *  Converts all the parameters in a request object into a string that is usable in a url.
    //	 *  @param  request      The request object
    //	 *  @param  excludeHash  Hash of values not be included (can be null)
    //	 */
    //	public static String makeUrlHiddens(HttpServletRequest request, Hashtable excludeHash) {
    //		Enumeration e = request.getParameterNames();
    //		String ret = "";
    //
    //		Helper.debugln(" controller param dump ("+new Date()+")");
    //		while (e.hasMoreElements()) {
    //			String name = (String)e.nextElement();
    //			if( excludeHash!=null && excludeHash.get(name)!=null ) {
    //				continue;
    //			}
    //			String value = request.getParameter(name);
    //			ret = ret + "<input type='hidden' name='" +name+ "' value='" +StringUtil.escapeSpecialChars(value,false)+ "'>\n";
    //		}
    //		return ret;
    //	}
    //
    //
    //
    //	/**
    //	 *
    //	 */
    //	public static String objectDump(Object o)
    //	{
    //		return objectDumpLow(o,0);
    //	}
    //
    //	private static String objectDumpLow(Object o, int level)
    //	{
    //		String ret;
    //
    //		if( o==null ) {
    //			return "(null)\n";
    //		}
    //		String className = o.getClass().getName();
    //		String subName = className.substring(className.lastIndexOf(".")+1);
    //		ret = "("+subName+") ";
    //		if( className.equals("java.util.Hashtable") ) {
    //			Hashtable hash = (Hashtable)o;
    //			ret = ret + "\n";
    //			for( Enumeration en=hash.keys(); en.hasMoreElements(); ) {
    //				String key = (String)en.nextElement();
    //				for(int ix=0;ix<level;ix++) {
    //					ret = ret + "  ";
    //				}
    //				ret = ret+key;
    //				for(int ix=key.length();ix<12;ix++) {
    //					ret = ret+" ";
    //				}
    //				ret = ret+objectDumpLow(hash.get(key),level+1);
    //			}
    //		} else if( className.equals("java.util.Vector") ) {
    //			Vector vect = (Vector)o;
    //			ret = ret + "\n";
    //			for( int ix=0; ix<vect.size(); ix++ ) {
    //				Object o2 = vect.get(ix);
    //				for(int iy=0;iy<level;iy++) {
    //					ret = ret + "  ";
    //				}
    //				ret = ret+"["+ix+"]  ";
    //				ret = ret+objectDumpLow(o2,level+1);
    //			}
    //		} else {
    //			ret = ret+o.toString()+"\n";
    //		}
    //		return ret;
    //	}
    //
    //	/**
    //	 * Calling store procedure with one input parameter
    //	 * @param	procName	name of store procedure
    //	 * @param	inputName	name of input param to store proc
    //	 * @param	inputValue	the value of input param as String
    //	 * @return	a result set (Vector of DatabaseRecord)
    //	 */
    //	public static Vector callStoredProcedure(String procName, String inputName, String inputValue)
    //	{
    //		HashMap map = new HashMap();
    //		map.put(inputName, inputValue);
    //		return (callStoredProcedure(procName, map));
    //	}
    //
    //	/**
    //	 * Calling store procedure with mutiple input parameters or none
    //	 * @param	procName	name of store procedure
    //	 * @param	map			contain name and value pair of each input parameter
    //	 * 						or NULL if there is no input parameter
    //	 * @return	a result set (Vector of Databaserecord)
    //	 */
    //	public static Vector callStoredProcedure(String procName, HashMap map)
    //	{
    //		StoredProcedureCall call = new StoredProcedureCall();
    //		call.setProcedureName(procName);
    //		DataReadQuery query = new DataReadQuery();
    //		query.setCall(call);
    //		Vector param = new Vector();
    //		if (map != null)
    //		{
    //			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
    //			{
    //				String inputName = (String)it.next();
    //				String inputValue = (String)map.get(inputName);
    //				call.addNamedArgument(inputName);
    //				query.addArgument(inputName);
    //				param.addElement(inputValue);
    //			}
    //		}
    //
    //		query.dontMaintainCache();
    //		Session session = AppMgr.getDbSession();
    //		Vector result = null;
    //		long before = System.currentTimeMillis();
    //		if (param.size() > 0) {
    //			result = (Vector)session.executeQuery(query, param);
    //		} else {
    //			result = (Vector)session.executeQuery(query);
    //		}
    //		long after = System.currentTimeMillis();
    //		AppMgr.updateDbTime(after - before);
    //
    //		session.release();
    //		return result;
    //	}
    //

    /**
     * Create a new instance of the class specified by the className
     * parameter.  The class must supply a default no argument public
     * constructor.
     */
    public static final Object createInstance(String className) {
        Object result = null;

        try {
            result = Class.forName(className).newInstance();
        } catch (Exception ex) {
            AppMgr.getLogger().error("Unable to create new instance of the class (" + className + "): ", ex);
        }

        return result;
    }

    //
    //	/**
    //	 * Recreates the navigation array, without the indicated menu item.
    //	 */
    //	public static String[][] removeMenuItem(String [][] navs, String removeItemName)
    //	{
    //		String newNavs[][] = new String[navs.length-1][];
    //
    //		int index = 0;
    //		for (int i = 0; i < navs.length; i++) {
    //			if (!navs[i][0].equals(removeItemName)) {
    //				newNavs[index] = navs[i];
    //				index++;
    //			}
    //		}
    //
    //		return newNavs;
    //	}
    //
    //	/**
    //	 * Given the request returns the role of the user that is logged in.
    //	 */
    //	public static User getRequestUser(HttpServletRequest request)
    //	{
    //		String userIdStr = (String) request.getAttribute("userId");
    //		if (userIdStr != null) {
    //			long userId = Long.parseLong(userIdStr);
    //			User user = (User) Helper.readObject(com.paycycle.user.User.class, userId);
    //			return user;
    //		}
    //		return null;
    //	}
    //
    //	protected static Hashtable m_jurisdictionFromGeocode = new Hashtable();
    //	public static String jurisdictionFromGeocode(String geocode)
    //	{
    //		if (!m_jurisdictionFromGeocode.containsKey(geocode))
    //		{
    //			if ((geocode.length() == 1 && geocode.charAt(0) == '0') || geocode.startsWith("00"))
    //			{
    //				m_jurisdictionFromGeocode.put(geocode, "FD");
    //			}
    //			else
    //			{
    //				String sqlQuery = "SELECT Jurisdiction FROM GTaxGeocodes " +
    //						"WHERE TaxGeocode LIKE '" + geocode.substring(0,2) + "%'";
    //
    //				ValueReadQuery query = new ValueReadQuery();
    //				query.setSQLString(sqlQuery);
    //				String result = (String) Helper.executeQuery(query);
    //
    //				m_jurisdictionFromGeocode.put(geocode, Helper.isEmpty(result, ""));
    //			}
    //		}
    //
    //		return (String) m_jurisdictionFromGeocode.get(geocode);
    //	}
    //
    //	protected static Hashtable m_geocodeFromJurisdiction = new Hashtable();
    //	public static String getGeocodeFromJurisdiction(String jurisdiction)
    //	{
    //		if (!m_geocodeFromJurisdiction.containsKey(jurisdiction))
    //		{
    //			String sqlQuery =
    //				"SELECT TaxGeocode FROM GTaxGeocodes "
    //					+ "WHERE Jurisdiction = ''{0}''";
    //
    //			MessageFormat formatter = new MessageFormat(sqlQuery);
    //			Object[] messageArgs = { jurisdiction };
    //
    //			ValueReadQuery query = new ValueReadQuery();
    //			query.setSQLString(formatter.format(messageArgs));
    //			m_geocodeFromJurisdiction.put(jurisdiction, Helper.isEmpty(Helper.executeQuery(query), null));
    //		}
    //
    //		return (String) m_geocodeFromJurisdiction.get(jurisdiction);
    //	}
    //
    ///****************************************************************************
    //The following methods are copies of methods in AppHelper (ops), but since it doesn't inherit from
    //the public Helper class, I'm copying them here - both places would need to be changed if changes
    //are made.  (ykb)
    //*****************************************************************************/
    //

    public static final Class getClass(String className) {
        Class res = null;

        try {
            res = Class.forName(className);
        } catch (Exception ex) {
            System.out.println("Class " + className + " not found");
        }

        return res;
    }

    public static final Object getConstant(String constantPath) {
        Object res = null;
        int i;

        /** Split the path into class path and field name */
        if ((i = constantPath.lastIndexOf('.')) != -1) {
            try {
                Class clazz = Class.forName(constantPath.substring(0, i));

                return clazz.getField(constantPath.substring(i + 1)).get(null);
            } catch (Exception ex) {
            }
        }

        if (res == null) {
            System.out.println("Constant " + constantPath + " not found");
        }

        return res;
    }

    /**
     * Breaks a string and parse into a rectangle representing coordinates. The
     * string should be in the form of {left, top, width, height}
     *
     * @return A rectangle containing the encoded coordinates
     */
    public static Rectangle parseRectangle(String toParse, String separator) {
        Rectangle res = new Rectangle();

        if (toParse == null) {
            return res;
        }

        StringTokenizer tokenizer = new StringTokenizer(toParse, separator);
        int[] c = new int[4];

        for (int i = 0; (i < 4) && tokenizer.hasMoreTokens(); i++) {
            c[i] = Integer.parseInt(tokenizer.nextToken());
        }

        res.setBounds(c[0], c[1], c[2], c[3]);

        return res;
    }

    /**
     * Breaks a string and parse into a Dimension representing width & height.
     *
     * @return A Dimension object
     */
    public static Dimension parseDimension(String toParse, String separator) {
        if ((toParse == null) || (separator == null)) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(toParse, separator);

        if (tokenizer.countTokens() != 2) {
            return null;
        }

        Dimension res = new Dimension();
        res.width = Integer.parseInt(tokenizer.nextToken());
        res.height = Integer.parseInt(tokenizer.nextToken());

        return res;
    }

    //
    //	/**
    //	 * Refreshes the read, by querying the database without checking the cache. Cache is updated
    //	 * NOTE: if metadata needs to refreshed, AppMgr.refresh function should be invoked.
    //	 * with read results.
    //	 * @param readQuery
    //	 * @param cascadeAllParts
    //	 * @param cacheUsage Value can be ObjectLevelReadQuery.CheckCacheOnly, ObjectLevelReadQuery.CheckCacheThenDatabase
    //	 * @param clazz
    //	 */
    //	public static void refreshCache(ObjectLevelReadQuery readQuery, boolean cascadeAllParts, int cacheUsage, Class clazz)
    //	{
    //
    //	    //For metadata just check the cache, otherwise get from db. No need to cascade.
    //	    if(DbUtil.METADATA.contains(clazz))
    //	    {
    //	        readQuery.setCacheUsage(ObjectLevelReadQuery.CheckCacheThenDatabase);
    //	        return;
    //	    }
    //	    readQuery.setCacheUsage(cacheUsage);
    //        readQuery.refreshIdentityMapResult();
    //
    //        if(cascadeAllParts) {
    //			readQuery.cascadeAllParts();
    //		} else {
    //			readQuery.cascadePrivateParts();
    //		}
    //
    //	}
    //
    //	public static String getStringFrom(InputStream stream)
    //	{
    //		try
    //		{
    //			StringBuffer result = new StringBuffer();
    //			int oneChar = stream.read();
    //			while (oneChar > -1)
    //			{
    //				result.append((char) oneChar);
    //				oneChar = stream.read();
    //			}
    //
    //			return result.toString();
    //		}
    //		catch (IOException ex)
    //		{
    //			return "";
    //		}
    //	}
    //
    //	public static String getCallCustomerServiceText(Company company, boolean capitalizeFirstWord)
    //	{
    //		Partner partner = PartnerMgr.getPartner(company);
    //		return getCallCustomerServiceText(partner, capitalizeFirstWord);
    //	}
    //
    //	public static String getCallCustomerServiceText(Partner partner, boolean capitalizeFirstWord)
    //	{
    //		if (partner.getId() == Partner.MANAGEPAYROLL)
    //		{
    //			return (capitalizeFirstWord ? "P":"p")+"lease contact your accountant.";
    //		}
    //		else
    //		{
    //	    	String supportPhone = partner.getTextAttribute(Partner.ATTR_PHONE_NUMBER_SUPPORT);
    //	    	String supportHours = partner.getTextAttribute(Partner.ATTR_PHONE_SUPPORT_HOURS);
    //
    //	    	return (capitalizeFirstWord ? "P":"p")+"lease call customer service at "+supportPhone+", "+supportHours+".";
    //		}
    //	}
    //
    //	/*
    //	 *  FUNCTIONS TO TELL THE COUNTRY OR AN IP ADDRESS
    //	 *
    //	 *  Uses code an a binary DB from www.MaxMind.com
    //	 *  It was a one-time $50 site license for a commercial "Country DB"
    //	 *  plus a 12/mo subscription fee for an updated data file.
    //	 *  This file is in the public/Db/MaxMind directory.
    //	 */
    //
    //	static LookupService ipAddressLookupService = null;
    //	private static void initIpAddressCountryLookup() {
    //		if (ipAddressLookupService == null) {
    //			try {
    //				String dbfile = AppMgr.getMaxMindPath();
    //				ipAddressLookupService = new LookupService(dbfile,LookupService.GEOIP_MEMORY_CACHE);
    //			} catch (Exception e) {
    //				AppMgr.getLogger().error(e);
    //				throw new RuntimeException(e);
    //			}
    //		}
    //	}
    //
    //	public static String getIpAddressCountryName(String ipAddress) {
    //		initIpAddressCountryLookup();
    //		return ipAddressLookupService.getCountry(ipAddress).getName();
    //	}
    //
    //	public static String getIpAddressCountryCode(String ipAddress)
    //	{
    //		initIpAddressCountryLookup();
    //		return ipAddressLookupService.getCountry(ipAddress).getCode();
    //	}
    //
    //	public static boolean isUSAIpAddress(String ipAddress) {
    //		String code = getIpAddressCountryCode(ipAddress);
    //		// private adddresses have country code "--" ... we will treat these as US ip addresses because we shouldn't ever get requests from them
    //		// other than when people with product access are testing accounts post-deployment
    //		return "US".equalsIgnoreCase(code) || "--".equalsIgnoreCase(code);
    //	}
    //
    //        /**
    //         * Retrieves an IPAddressInfo record from MaxMind.
    //         *
    //         * @param ipAddress
    //         * @return Returns the geographic information for the given ip address
    //         */
    //        public static IPAddressInfo getIpAddressInfoFromMaxMind(String ipAddress) {
    //            initIpAddressCountryLookup();
    //            Location location = ipAddressLookupService.getLocation(ipAddress);
    //
    //            if( location==null ) {
    //                return null;
    //            }
    //            IPAddressInfo info = new IPAddressInfo();
    //            info.setCountry(location.countryName);
    //            info.setCity(location.city);
    //            info.setDmaCode(""+location.dma_code);
    //            info.setRegion(location.region);
    //            info.setPostalCode(location.postalCode);
    //            info.setLastUpdated(DateUtil.now());
    //            info.setIpAddress(ipAddress);
    //           return info;
    //        }
    //
    //	public static void testGetIpAddressCountryName () {
    //		String name = getIpAddressCountryName("151.38.39.114");	// Italy
    //		name = getIpAddressCountryName("151.38.39.114");		// Italy
    //		name = getIpAddressCountryName("12.25.205.51");			// United States
    //		name = getIpAddressCountryName("64.81.104.131");		// United States
    //		name = getIpAddressCountryName("200.21.225.82");		// Anonymous Proxy
    //		name = getIpAddressCountryName("62.85.112.100"); 		// Latvia
    //		name = getIpAddressCountryName("62.149.23.243");		// Ukraine
    //	}
    //
    //    public static void logIPAddressLogin(HttpServletRequest request, User user, boolean success ) {
    //        try {
    //            Login login = user.getLogin();
    //            String ipAddress = ServletUtil.getBrowserIP(request);
    //            ipAddress = ipAddress.length() > 16 ? ipAddress.substring(0, 16) : ipAddress;
    //            if( login!=null && isNotEmpty(ipAddress) ) {
    //                if( !UserTx.hasIPAddressLogins(ipAddress,login.getId()) ) {
    //                    // This means this is the first time we've seen this ipAddress
    //
    //                    IPAddressInfo info = UserTx.getIpAddressInfo(ipAddress);
    //                    if(info==null ) {
    //                        // Store it in the data baese for future reference
    //                        info = getIpAddressInfoFromMaxMind(ipAddress);
    //                        if( info==null ) {
    //                            info = new IPAddressInfo();
    //                            info.setIpAddress(ipAddress);
    //                            info.setCity("(no entry)");
    //                            info.setLastUpdated(DateUtil.now());
    //                        }
    //                        UserTx tx = new UserTx();
    //                        tx.addIpAddressInfo(info);
    //                    }
    //                }
    //                UserTx tx = new UserTx();
    //                tx.addIpAddressLogin(ipAddress, login, success);
    //            }
    //        } catch( Exception e) {
    //            // For now, if something goes wrong, we're just going to go Ostrich and stick our heads in the sand.
    //        }
    //    }
    //
    //    /**
    //     * IP Address logging feature:
    //     *     - if currently in process of login, check if login attempt (request) is from foreign ip address
    //     *     - if it is from outside US, set the correct company attribute and send out internal email to ops.
    //     *     - Regardless of where it's from save it in the company's last login attr.
    //     * @param request
    //     * @param co
    //     */
    //    public static void logLoginIPAddr(HttpServletRequest request, Company co)
    //    {
    //        String ipAddress = "unknown";
    //        String countryName = "UNKNOWN_ERROR";
    //
    //        try {
    //            if (isNotEmpty(request.getSession().getAttribute("logLoginIPAddr"))) {
    //                ipAddress = ServletUtil.getBrowserIP(request);
    //                if (isNotEmpty(ipAddress)) {
    //                    countryName = getIpAddressCountryName(ipAddress);
    //                    String countryCode = getIpAddressCountryCode(ipAddress);
    //
    //                    // If it's a foreign IP address, set that attr and send mail.
    //                    if (!isUSAIpAddress(ipAddress)) {
    //                        new CompanyTx().setStatus(co, CompanyStatus.FOREIGN_IP, countryCode + "_" + ipAddress);
    //                        sendEmailOnForeignIPAddressLogin(co, null, ipAddress, countryName);
    //                    }
    //
    //                    // Always save the IP addr in this attr.
    //                    new CompanyTx().setStatus(co, CompanyStatus.LAST_LOGIN_IP, countryCode + "_" + ipAddress);
    //
    //                    // This session attr is used by the FakeRequest class.
    //                    request.getSession().setAttribute("lastLoginIP", ipAddress);
    //                } else {
    //                    new CompanyTx().setStatus(co, CompanyStatus.FOREIGN_IP, "IP_ADDR_UNDEFINED");
    //                    new CompanyTx().setStatus(co, CompanyStatus.LAST_LOGIN_IP, "IP_ADDR_UNDEFINED");
    //                }
    //
    //                // Remove this trigger so that we only do it once.
    //                // The trigger was added in AppMgr.validateLogin().
    //                request.getSession().removeAttribute("logLoginIPAddr");
    //            }
    //        } catch (Exception ex) {
    //            AppMgr.getLogger().error("Company " + co.getId() + ": caught exception while checking for foreign IP address: " + ipAddress + ".  Exception: " + ex.getMessage());
    //        }
    //    }
    //
    //	private static void sendEmailOnForeignIPAddressLogin (Company co, Exception ex, String ipAddress, String countryName)
    //		throws Exception
    //	{
    //		String [] toList = new String[] {"fraud@paycycle.com"};
    //		String body = "\n Foreign IP access detected with the following parameters:\n\n";
    //		body += "Company ID: " + co.getId() + "\n";
    //		body += "Business Name: " + co.getBusinessName() + "\n";
    //		body += "IP Address: " + ipAddress + "\n";
    //		body += "Country: " + countryName + "\n";
    //		body += "Time of Access: " + DateUtil.dateTimeFormat(DateUtil.now()) + "\n";
    //		if (!isEmpty(ex))
    //		{
    //			body += "Exception: " + ex.getMessage() + "\n";
    //			body += "Stack Trace: " + StringUtil.getStackTrace(ex);
    //		}
    //		String subject= "Foreign IP: Company ID " + co.getId() + " (" + co.getBusinessName() + ") was accessed from IP " + ipAddress + " in " + countryName;
    //		if (!isEmpty(ex))
    //			subject = "(ERROR)!  " + subject;
    //		MailManager.sendMail(toList,null,null,"Foreign IP Address Access","noreply@paycycle.com", "noreply@paycycle.com", subject, null, body, co.getId());
    //	}
    //
    //	private static void sendExceptionEmailOnForeignIPAddressLogin (Company co, Exception ex, String ipAddress, String countryName)
    //	{
    //		try {
    //			sendEmailOnForeignIPAddressLogin(co, ex, ipAddress, countryName);
    //		} catch (Exception e)
    //		{}
    //	}
    //
    //	/**
    //	 * Prevent forged submission of form by checking for a per-form salt that can only be used once.
    //	 */
    //	public static void checkFormSalt(ParamMap parameters)
    //	{
    ////		if (!isDevelopment()) {
    //		String parameterFormSalt = parameters.getString("formSalt");
    //		if (Helper.isEmpty(parameterFormSalt)) {
    //			throw new UserException("Please try again. (Error Code 1)");
    //		}
    //		Hashtable sessionFormSalts = (Hashtable) parameters.getSession().getAttribute("formSalts");
    //		if (sessionFormSalts == null) {
    ////			String queryString = request.getQueryString();
    ////			String visitorID = ServletUtil.getCookieValue(request, "visitorid");
    ////			visitorID = visitorID == null ? "" : visitorID;
    ////			getLogger("com.paycycle.saltMismatches").error(
    ////					"nosessionsalts: " + request.getRequestURL() + (queryString == null ? "" : "?" + queryString) + " sessionid "
    ////							+ session.getId() + " requestSalt " + parameterLoginSalt + " visitorid " + visitorID);
    //			throw new UserException("Please try again. (Error Code 2)");
    //		}
    //		if (sessionFormSalts.containsKey(parameterFormSalt)) {
    //			parameters.getSession().removeAttribute("formSalts");
    //		} else {
    ////			String queryString = request.getQueryString();
    ////			String visitorID = ServletUtil.getCookieValue(request, "visitorid");
    ////			visitorID = visitorID == null ? "" : visitorID;
    ////			getLogger("com.paycycle.saltMismatches").error(
    ////					"salt mismatch: " + request.getRequestURL() + (queryString == null ? "" : "?" + queryString) + " sessionid "
    ////							+ session.getId() + " requestSalt " + parameterLoginSalt + " sessionSalt(s) " + sessionSalts + " visitorid "
    ////							+ visitorID);
    //			throw new UserException("Please try again. (Error Code 3)");
    //		}
    ////	}
    //	}
    //

    /**
     * This method creates directory if it already doesn't exist and returns the directory name.
     * It expects directoryName to have forward slashes as file separator
     *
     * @param directoryName
     * @return directory name
     */
    public static String makeDirectory(String directoryName) {
        if (Helper.isEmpty(directoryName)) {
            throw new UserException("Directory provided is either empty or null");
        }

        File directory = new File(directoryName);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new UserException("Unable to create directory:" + directoryName);
            }
        }

        return directoryName;
    }
}
