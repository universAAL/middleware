/*******************************************************************************
 * Copyright 2017 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.container.pojo.layers;

import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

/**
 * @author amedrano
 *
 */
public class TurtleSerialization implements ModuleActivator {

	private MessageContentSerializer mcs;

	/** {@ inheritDoc} */
	public void start(ModuleContext mc) throws Exception {
		TurtleSerializer turtleS = new TurtleSerializer();
		mc.getContainer().shareObject(mc, turtleS,
				new Object[] { MessageContentSerializer.class.getName() });
		mc.getContainer().shareObject(mc, turtleS,
				new Object[] { MessageContentSerializerEx.class.getName() });
		mcs = (MessageContentSerializer) mc.getContainer().fetchSharedObject(
				mc, new Object[] { MessageContentSerializer.class.getName() });
		TurtleUtil.moduleContext = mc;
	}

	/** {@ inheritDoc} */
	public void stop(ModuleContext mc) throws Exception {

	}

}
