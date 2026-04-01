/**
 * CollectionUtil
 *
 * Copyright (c) 2003 PayCycle, Inc. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CollectionUtil {
    public static interface FilterFunction {
        public boolean keep(Object object, List params);
    }

    /**
     * iterate over collection and keep elements where function is true
     *
     * @param collection
     * @param function
     * @return new collection of same class as collection parameter
     */
    public static Collection filter(Collection collection, FilterFunction function, List params) throws IllegalAccessException, InstantiationException {
        Collection result = collection.getClass().newInstance();
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            Object object = iter.next();
            if (function.keep(object, params)) {
                result.add(object);
            }
        }
        return result;
    }

    public static boolean areAllElementsNull(Collection collection) {
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            if (iter.next() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     */
    public static final class EmptyEnumeration implements Enumeration {
        public EmptyEnumeration() {
        }

        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException();
        }
    }

    public static interface OneArgumentFunction {
        public Object call(Object object);
    }

    public static Collection map(OneArgumentFunction function, Collection collection) {
        try {
            Collection result = collection.getClass().newInstance();
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                result.add(function.call(iterator.next()));
            }
            return result;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void flatten(Object object, Collection target) {
        if (object instanceof Collection) {
            Iterator iterator = ((Collection) object).iterator();
            while (iterator.hasNext()) {
                flatten(iterator.next(), target);
            }
        } else {
            target.add(object);
        }

    }

    public static Collection flatten(Collection collection) throws IllegalAccessException, InstantiationException {
        Collection result = collection.getClass().newInstance();
        flatten(collection, result);
        return result;
    }

    /**
     * Group the collection elements using the key function provided by the caller
     *
     * @param keyFunction - function which extracts key from collection element
     * @return Map of Objects to Lists
     */
    public static Map groupBy(Collection collection, OneArgumentFunction keyFunction) {
        Map map = new HashMap();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            Object key = keyFunction.call(element);
            if (map.containsKey(key)) {
                ((List) map.get(key)).add(element);
            } else {
                List value = new ArrayList();
                value.add(element);
                map.put(key, value);
            }
        }
        return map;
    }

    public static List getColumn(String columnName, List<Map> table) {
        List result = new ArrayList(table.size());
        for (Map row : table) {
            result.add(row.get(columnName));
        }
        return result;
    }

    public static List getColumnFromListOfMapOfStringToObject(String columnName, List<Map<String, Object>> table) {
        List result = new ArrayList(table.size());
        for (Map row : table) {
            result.add(row.get(columnName));
        }
        return result;
    }
}
