/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.universAAL.middleware.util;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.universAAL.middleware.Activator;

/**
 * @author mtazari
 * @author cstockloew
 */
public class LogUtils {
	
	/**
	 * Internal method to create a single String from a list of objects.
	 * 
	 * @param cls
	 *            The class which has called the logger.
	 * @param method
	 *            The method in which the logger was called.
	 * @param msgPart
	 *            The message of this log entry. All elements of this array are
	 *            converted to a string object and concatenated.
	 * @return The String.
	 */
	private static String buildMsg(String cls, String method, Object[] msgPart) {
		StringBuffer sb = new StringBuffer(256);
		sb.append(cls).append("->").append(method).append("(): ");
		if (msgPart != null)
			for (int i=0; i<msgPart.length; i++)
				sb.append(msgPart[i]);
		return sb.toString();
	}
	
	/**
	 * Log a message at the DEBUG level.
	 * 
	 * @param logger
	 *            The logger.
	 * @param cls
	 *            The class which has called the logger.
	 * @param method
	 *            The method in which the logger was called.
	 * @param msgPart
	 *            The message of this log entry. All elements of this array are
	 *            converted to a string object and concatenated.
	 * @param t
	 *            The exception (Throwable) to log. Can be null.
	 */
	public static void logDebug(Logger logger, String cls, String method,
			Object[] msgPart, Throwable t) {
		
		logger.debug(buildMsg(cls, method, msgPart), t);

		// forward to all log listeners
		// TODO: do this only when a certain flag is set
		try {
			ServiceReference sr[] = Activator.context.getServiceReferences(
					LogListener.class.getName(), null);
			if (sr == null)
				return;
			for (int i = 0; i < sr.length; i++) {
				LogListener l = (LogListener) Activator.context
						.getService(sr[i]);
				if (l != null)
					l.logDebug(cls, method, msgPart, t);
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Log a message at the ERROR level.
	 * 
	 * @param logger
	 *            The logger.
	 * @param cls
	 *            The class which has called the logger.
	 * @param method
	 *            The method in which the logger was called.
	 * @param msgPart
	 *            The message of this log entry. All elements of this array are
	 *            converted to a string object and concatenated.
	 * @param t
	 *            The exception (Throwable) to log. Can be null.
	 */
	public static void logError(Logger logger, String cls, String method,
			Object[] msgPart, Throwable t) {
		logger.error(buildMsg(cls, method, msgPart), t);
	}
	
	/**
	 * Log a message at the INFO level.
	 * 
	 * @param logger
	 *            The logger.
	 * @param cls
	 *            The class which has called the logger.
	 * @param method
	 *            The method in which the logger was called.
	 * @param msgPart
	 *            The message of this log entry. All elements of this array are
	 *            converted to a string object and concatenated.
	 * @param t
	 *            The exception (Throwable) to log. Can be null.
	 */
	public static void logInfo(Logger logger, String cls, String method,
			Object[] msgPart, Throwable t) {
		logger.info(buildMsg(cls, method, msgPart), t);
	}
	
	/**
	 * Log a message at the WARNING level.
	 * 
	 * @param logger
	 *            The logger.
	 * @param cls
	 *            The class which has called the logger.
	 * @param method
	 *            The method in which the logger was called.
	 * @param msgPart
	 *            The message of this log entry. All elements of this array are
	 *            converted to a string object and concatenated.
	 * @param t
	 *            The exception (Throwable) to log. Can be null.
	 */
	public static void logWarning(Logger logger, String cls, String method,
			Object[] msgPart, Throwable t) {
		logger.warn(buildMsg(cls, method, msgPart), t);
	}
}
