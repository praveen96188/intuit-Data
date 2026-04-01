/**
 * AppHelper.java
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

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class contains useful utility functions that can be used by application.
 * <b>Note:</b> This class implements the Singleton pattern
 */
public class AppHelper {
    /**
     * Determines if the passed string contains all digits
     */
    public static boolean isDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Breaks a string into a vector of strings
     *
     * @return A vector containing separated string items
     */
    public static Vector getStrings(String toParse, String separator) {
        if (toParse == null) {
            return new Vector(0);
        }

        Vector res = new Vector(20);
        StringTokenizer tokenizer = new StringTokenizer(toParse, separator);
        while (tokenizer.hasMoreTokens()) {
            res.add(tokenizer.nextToken());
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
        int c[] = new int[4];

        for (int i = 0; i < 4 && tokenizer.hasMoreTokens(); i++) {
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
        if (toParse == null || separator == null) {
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

    /**
     * Copy the given file
     */
    public static void copy(File from, File to) throws Exception {
        FileInputStream in = new FileInputStream(from);
        byte buf[] = new byte[(int) from.length()];
        in.read(buf, 0, buf.length);
        in.close();
        FileOutputStream out = new FileOutputStream(to);
        out.write(buf);
        out.close();
    }

    /**
     * Create a valid URL
     */
    protected static String getURL(String urlOrFile) throws MalformedURLException {
        try {
            if (urlOrFile.indexOf(":") >= 0 || urlOrFile.startsWith("/")) {
                if (urlOrFile.startsWith("/")) {
                    return "file://" + urlOrFile;
                } else {
                    return "file:///" + urlOrFile;
                }
            } else {
                URL baseURL;
                String currentDir = System.getProperty("user.dir");
                String file = currentDir.replace(File.separatorChar, '/') + '/';
                if (file.charAt(0) != '/') {
                    file = "/" + file;
                }
                baseURL = new URL("file", null, file);
                return new URL(baseURL, urlOrFile).toString();
            }
        } catch (SecurityException ex) {
            return urlOrFile.replace(File.separatorChar, '/');
        }
    }

    public static String getDirectory(String file) {
        while (!new File(file).isDirectory()) {
            if (file.indexOf(File.separator) >= 0) {
                file = file.substring(0, file.lastIndexOf(File.separator));
            } else {
                return "";
            }
        }
        return file;
    }

    public static String getFileName(String file) {
        if (file.indexOf(File.separator) >= 0) {
            return file.substring(file.lastIndexOf(File.separator) + 1, file.length());
        }
        return file;
    }

    /**
     * Given a class, extract its base class name. For example, the name
     * "SwingHelper" will be returned from "com.paycycle.util.SwingHelper"
     */
    public static final String getBaseClassName(Class clazz) {
        String fullName = clazz.getName();
        int idx = fullName.lastIndexOf("$");
        if (idx == -1) {
            if ((idx = fullName.lastIndexOf(".")) == -1) {
                return null;
            }
        }

        return fullName.substring(idx + 1);
    }

    public static final Class getClass(String className) {
        Class res = null;
        int i;
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
     * Returns the field value of the target object.
     *
     * @param    target    the target object
     * @param    name        the name of the field.
     * @return the value of the field.
     */
    public static final Object getFieldValue(Object target, String name) {
        Object result = null;

        try {
            Field fld = target.getClass().getField(name);
            result = fld.get(target);
        } catch (Exception ex) {
            System.err.println("Cannot obtain field value: " + ex);
        }
        return result;
    }
}
