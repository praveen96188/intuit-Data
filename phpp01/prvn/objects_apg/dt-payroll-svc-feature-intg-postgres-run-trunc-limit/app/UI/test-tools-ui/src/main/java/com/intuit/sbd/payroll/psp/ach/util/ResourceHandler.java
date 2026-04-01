/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.util;

/**
 *
 * @author shivanandad069
 */
import org.xml.sax.helpers.*;

/**
 * This class implements a resource bundle backed by XML files.
 */
public abstract class ResourceHandler extends DefaultHandler
{
	public abstract void addResourceListener (ResourceListener l);
	public abstract void removeResourceListener (ResourceListener l);
}
