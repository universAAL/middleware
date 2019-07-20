/*
	Copyright 2009-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.serialization.turtle.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.osgi.OSGiContainer;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

/**
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 *
 */
public final class Activator implements BundleActivator {

	private JSONLDSerialization serializer = new JSONLDSerialization();

	public void start(BundleContext context) throws Exception {
		JSONLDSerialization.owner = OSGiContainer.THE_CONTAINER.registerModule(new Object[] { context });


        Dictionary< String, Object> dic = new Hashtable<String, Object>();
        
        dic.put(MessageContentSerializer.CONTENT_TYPE, serializer.getContentType());
       
        OSGiContainer.THE_CONTAINER.shareObject(JSONLDSerialization.moduleContext, serializer,
                new Object[] {MessageContentSerializer.class.getName(), MessageContentSerializerEx.class.getName(),  dic});
	}

	public void stop(BundleContext arg0) throws Exception {

		 Dictionary< String, Object> dic = new Hashtable<String, Object>();
	        
	        dic.put(MessageContentSerializer.CONTENT_TYPE, serializer.getContentType());
		OSGiContainer.THE_CONTAINER.removeSharedObject(JSONLDSerialization.moduleContext, serializer,
               new Object[] {MessageContentSerializer.class.getName(), MessageContentSerializerEx.class.getName(),  dic});
	}
}
