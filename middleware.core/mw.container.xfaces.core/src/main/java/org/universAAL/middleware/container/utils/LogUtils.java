/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.container.utils;

import java.util.Iterator;

import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.container.ModuleContext;

/**
 * Logging utility class. Call one of the static methods of this class to add
 * new log entries to a logger. Log entries may also be forwarded to various
 * other {@link LogListener}s.
 * <p>
 * <b>Security considerations:</b><br>
 * Since the log entries are forwarded to <i>custom</i> log listeners, the
 * <code>msgPart</code> should contain only <i>unmodifiable</i> content.
 * </p>
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class LogUtils {

    /**
     * Internal method to create a single String from a list of objects.
     * 
     * @param cls
     *            The class which has called the logger.
     * @param method
     *            The method in which the logger was called.
     * @return The String.
     */
    private static String buildTag(String cls, String method) {
	StringBuffer sb = new StringBuffer(256);
	sb.append(cls).append("->").append(method).append("()");
	return sb.toString();
    }

    /**
     * Internal method to create a single String from a list of objects.
     * 
     * @param msgPart
     *            The message of this log entry. All elements of this array are
     *            converted to a string object and concatenated.
     * @return The String.
     */
    private static String buildMsg(Object[] msgPart) {
	StringBuffer sb = new StringBuffer(256);
	if (msgPart != null)
	    for (int i = 0; i < msgPart.length; i++)
		sb.append(msgPart[i]);
	return sb.toString();
    }

    private static void log(int level, ModuleContext mc, Class claz,
	    String method, Object[] msgPart, Throwable t) {

	String pkg, cls;
	if (claz == null)
	    pkg = cls = "null";
	else {
	    pkg = claz.getPackage().getName();
	    cls = claz.getName().substring(pkg.length() + 1);
	}

	String module;
	if (mc == null)
	    module = "null";
	else {
	    switch (level) {
	    case LogListener.LOG_LEVEL_TRACE:
		mc.logTrace(buildTag(cls, method), buildMsg(msgPart), t);
		break;
	    case LogListener.LOG_LEVEL_DEBUG:
		mc.logDebug(buildTag(cls, method), buildMsg(msgPart), t);
		break;
	    case LogListener.LOG_LEVEL_INFO:
		mc.logInfo(buildTag(cls, method), buildMsg(msgPart), t);
		break;
	    case LogListener.LOG_LEVEL_WARN:
		mc.logWarn(buildTag(cls, method), buildMsg(msgPart), t);
		break;
	    case LogListener.LOG_LEVEL_ERROR:
		mc.logError(buildTag(cls, method), buildMsg(msgPart), t);
		break;
	    }
	    module = mc.getID();

	    for (Iterator i = mc.getContainer().logListeners(); i.hasNext();)
		try {
		    ((LogListener) i.next()).log(level, module, pkg, cls,
			    method, msgPart, t);
		} catch (Exception e) {
		    mc.logDebug(buildTag("LogUtils", "log"),
			    "One of the LogListeners has thrown an exception.",
			    e);
		}
	}
    }

    /**
     * Provides a standard way for using container-specific loggers for logging
     * debug messages via
     * {@link ModuleContext#logDebug(String, String, Throwable)}. The advantage
     * compared to using
     * {@link ModuleContext#logDebug(String, String, Throwable)} directly is
     * twofold:
     * <ol>
     * <li>here registered instances of {@link LogListener} are notified
     * automatically, and</li>
     * <li>the message string needed by
     * {@link ModuleContext#logDebug(String, String, Throwable)} is built in a
     * structured way by concatenating several different info (see the
     * parameters as well as {@link #buildMsg(Object[])}).
     * 
     * @param mc
     *            the {@link ModuleContext} needed for accessing the
     *            container-specific logger for the corresponding module
     * @param claz
     *            The Java class that wants to generate the log message
     * @param method
     *            The name of the method in the above Java class that intends to
     *            generate the log message
     * @param msgPart
     *            An array of strings and other objects that should be
     *            concatenated using {@link java.lang.String#valueOf(Object)} in
     *            order to construct the log message
     * @param t
     *            An optional {@link java.lang.Throwable} object like an
     *            exception that might have caused the log request
     */
    public static void logDebug(ModuleContext mc, Class claz, String method,
	    Object[] msgPart, Throwable t) {
	log(LogListener.LOG_LEVEL_DEBUG, mc, claz, method, msgPart, t);
    }

    /**
     * Provides a standard way for using container-specific loggers for logging
     * error messages via
     * {@link ModuleContext#logError(String, String, Throwable)}. The advantage
     * compared to using
     * {@link ModuleContext#logError(String, String, Throwable)} directly is
     * twofold:
     * <ol>
     * <li>here registered instances of {@link LogListener} are notified
     * automatically, and</li>
     * <li>the message string needed by
     * {@link ModuleContext#logError(String, String, Throwable)} is built in a
     * structured way by concatenating several different info (see the
     * parameters as well as {@link #buildMsg(Object[])}).
     * 
     * @param mc
     *            the {@link ModuleContext} needed for accessing the
     *            container-specific logger for the corresponding module
     * @param claz
     *            The Java class that wants to generate the log message
     * @param method
     *            The name of the method in the above Java class that intends to
     *            generate the log message
     * @param msgPart
     *            An array of strings and other objects that should be
     *            concatenated using {@link java.lang.String#valueOf(Object)} in
     *            order to construct the log message
     * @param t
     *            An optional {@link java.lang.Throwable} object like an
     *            exception that might have caused the log request
     */
    public static void logError(ModuleContext mc, Class claz, String method,
	    Object[] msgPart, Throwable t) {
	log(LogListener.LOG_LEVEL_ERROR, mc, claz, method, msgPart, t);
    }

    /**
     * Provides a standard way for using container-specific loggers for logging
     * info messages via
     * {@link ModuleContext#logInfo(String, String, Throwable)}. The advantage
     * compared to using
     * {@link ModuleContext#logInfo(String, String, Throwable)} directly is
     * twofold:
     * <ol>
     * <li>here registered instances of {@link LogListener} are notified
     * automatically, and</li>
     * <li>the message string needed by
     * {@link ModuleContext#logInfo(String, String, Throwable)} is built in a
     * structured way by concatenating several different info (see the
     * parameters as well as {@link #buildMsg(Object[])}).
     * 
     * @param mc
     *            the {@link ModuleContext} needed for accessing the
     *            container-specific logger for the corresponding module
     * @param claz
     *            The Java class that wants to generate the log message
     * @param method
     *            The name of the method in the above Java class that intends to
     *            generate the log message
     * @param msgPart
     *            An array of strings and other objects that should be
     *            concatenated using {@link java.lang.String#valueOf(Object)} in
     *            order to construct the log message
     * @param t
     *            An optional {@link java.lang.Throwable} object like an
     *            exception that might have caused the log request
     */
    public static void logInfo(ModuleContext mc, Class claz, String method,
	    Object[] msgPart, Throwable t) {
	log(LogListener.LOG_LEVEL_INFO, mc, claz, method, msgPart, t);
    }

    /**
     * Provides a standard way for using container-specific loggers for logging
     * warn messages via
     * {@link ModuleContext#logWarn(String, String, Throwable)}. The advantage
     * compared to using
     * {@link ModuleContext#logWarn(String, String, Throwable)} directly is
     * twofold:
     * <ol>
     * <li>here registered instances of {@link LogListener} are notified
     * automatically, and</li>
     * <li>the message string needed by
     * {@link ModuleContext#logWarn(String, String, Throwable)} is built in a
     * structured way by concatenating several different info (see the
     * parameters as well as {@link #buildMsg(Object[])}).
     * 
     * @param mc
     *            the {@link ModuleContext} needed for accessing the
     *            container-specific logger for the corresponding module
     * @param claz
     *            The Java class that wants to generate the log message
     * @param method
     *            The name of the method in the above Java class that intends to
     *            generate the log message
     * @param msgPart
     *            An array of strings and other objects that should be
     *            concatenated using {@link java.lang.String#valueOf(Object)} in
     *            order to construct the log message
     * @param t
     *            An optional {@link java.lang.Throwable} object like an
     *            exception that might have caused the log request
     */
    public static void logWarn(ModuleContext mc, Class claz, String method,
	    Object[] msgPart, Throwable t) {
	log(LogListener.LOG_LEVEL_WARN, mc, claz, method, msgPart, t);
    }

    /**
     * Provides a standard way for using container-specific loggers for logging
     * trace messages via
     * {@link ModuleContext#logTrace(String, String, Throwable)}. The advantage
     * compared to using
     * {@link ModuleContext#logTrace(String, String, Throwable)} directly is
     * twofold:
     * <ol>
     * <li>here registered instances of {@link LogListener} are notified
     * automatically, and</li>
     * <li>the message string needed by
     * {@link ModuleContext#logTrace(String, String, Throwable)} is built in a
     * structured way by concatenating several different info (see the
     * parameters as well as {@link #buildMsg(Object[])}).
     * 
     * @param mc
     *            the {@link ModuleContext} needed for accessing the
     *            container-specific logger for the corresponding module
     * @param claz
     *            The Java class that wants to generate the log message
     * @param method
     *            The name of the method in the above Java class that intends to
     *            generate the log message
     * @param msgPart
     *            An array of strings and other objects that should be
     *            concatenated using {@link java.lang.String#valueOf(Object)} in
     *            order to construct the log message
     * @param t
     *            An optional {@link java.lang.Throwable} object like an
     *            exception that might have caused the log request
     */
    public static void logTrace(ModuleContext mc, Class claz, String method,
	    Object[] msgPart, Throwable t) {
	log(LogListener.LOG_LEVEL_TRACE, mc, claz, method, msgPart, t);
    }
}
