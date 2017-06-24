/*
Copyright 2007-2015 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.managers.distributedmw.api;

import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.interfaces.PeerCard;

/**
 * Listener interface for new log entries. The log listeners are called
 * automatically when adding a log entry to
 * {@link org.universAAL.middleware.container.utils.LogUtils}.
 *
 * @author Carsten Stockloew
 */
public interface DistributedLogListener {

	public static final int LOG_LEVEL_TRACE = LogListener.LOG_LEVEL_TRACE;
	public static final int LOG_LEVEL_DEBUG = LogListener.LOG_LEVEL_DEBUG;
	public static final int LOG_LEVEL_INFO = LogListener.LOG_LEVEL_INFO;
	public static final int LOG_LEVEL_WARN = LogListener.LOG_LEVEL_WARN;
	public static final int LOG_LEVEL_ERROR = LogListener.LOG_LEVEL_ERROR;

	/**
	 * Log a new message.
	 *
	 * @param origin
	 *            the peer on which the event occurred.
	 * @param logLevel
	 *            the log level (trace, debug, info, warn, or error)
	 * @param module
	 *            the name of the module that contains the class that intends to
	 *            generate the log message
	 * @param pkg
	 *            the name of the package that contains the class that intends
	 *            to generate the log message
	 * @param cls
	 *            the name of the class that intends to generate the log message
	 * @param method
	 *            The name of the method in the above Java class that intends to
	 *            generate the log message
	 * @param msgPart
	 *            An array of strings and other objects that should be
	 *            concatenated using {@link java.lang.String#valueOf(Object)} in
	 *            order to construct the log message
	 * @param t
	 *            An optional {@link java.lang.Throwable} object, serialized as
	 *            String, like an exception that might have caused the log
	 *            request
	 */
	public void log(PeerCard origin, int logLevel, String module, String pkg, String cls, String method,
			Object[] msgPart, String t);
}
