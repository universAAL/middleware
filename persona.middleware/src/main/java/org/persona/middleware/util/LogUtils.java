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
package org.persona.middleware.util;

import org.slf4j.Logger;

/**
 * @author mtazari
 *
 */
public class LogUtils {
	
	public static String buildMsg(String cls, String method, Object[] msgPart) {
		StringBuffer sb = new StringBuffer(256);
		sb.append(cls).append("->").append(method).append("(): ");
		if (msgPart != null)
			for (int i=0; i<msgPart.length; i++)
				sb.append(msgPart[i]);
		return sb.toString();
	}
	
	public static void logDebug(Logger logger, String cls, String method, Object[] msgPart, Throwable t) {
		logger.debug(buildMsg(cls, method, msgPart), t);
	}
	
	public static void logError(Logger logger, String cls, String method, Object[] msgPart, Throwable t) {
		logger.error(buildMsg(cls, method, msgPart), t);
	}
	
	public static void logInfo(Logger logger, String cls, String method, Object[] msgPart, Throwable t) {
		logger.info(buildMsg(cls, method, msgPart), t);
	}
	
	public static void logWarning(Logger logger, String cls, String method, Object[] msgPart, Throwable t) {
		logger.warn(buildMsg(cls, method, msgPart), t);
	}
}
