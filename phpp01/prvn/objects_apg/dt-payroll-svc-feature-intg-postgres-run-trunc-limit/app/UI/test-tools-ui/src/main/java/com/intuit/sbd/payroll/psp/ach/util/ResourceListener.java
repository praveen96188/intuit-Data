/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.util;

/**
 *
 * @author shivanandad069
 */
import java.util.EventListener;

/**
 * The resource listener interface.
 */
public interface ResourceListener extends EventListener
{
	public void handlerChanged(ResourceHandler handler);
	public void itemCreated(String id, Object c);
	public Object itemRequested(String id);
}