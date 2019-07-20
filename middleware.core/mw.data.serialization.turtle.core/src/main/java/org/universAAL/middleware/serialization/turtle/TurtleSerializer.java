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
package org.universAAL.middleware.serialization.turtle;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

public class TurtleSerializer implements MessageContentSerializerEx {

	private static String MIME_TURTLE = "text/turtle";
	
	public TurtleSerializer() {
	}

	/** @see org.universAAL.middleware.serialization.MessageContentSerializer#deserialize(String) */
	public synchronized Object deserialize(String serialized) {
		return deserialize(serialized, null);
	}

	/**
	 * @see org.universAAL.middleware.serialization.MessageContentSerializerEx#deserialize(String,
	 *      String)
	 */
	public synchronized Object deserialize(String serialized, String resourceURI) {
		try {
			TurtleParser parser = new TurtleParser();
			return parser.deserialize(serialized, resourceURI);
		} catch (Exception ex) {
			LogUtils.logError(TurtleUtil.moduleContext, TurtleSerializer.class, "deserialize", null, ex);
			return null;
		}
	}

	/** @see org.universAAL.middleware.serialization.MessageContentSerializer#serialize(Object) */
	public String serialize(Object messageContent) {
		return TurtleWriter.serialize(messageContent, 0);
	}

	public String getContentType() {
		return MIME_TURTLE;
	}
}
