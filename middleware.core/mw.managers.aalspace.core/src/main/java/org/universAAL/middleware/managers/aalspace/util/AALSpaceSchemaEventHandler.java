/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council 

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
package org.universAAL.middleware.managers.aalspace.util;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * Event Handler for AALSpace Schema
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class AALSpaceSchemaEventHandler implements ValidationEventHandler {

    private ModuleContext context;

    public AALSpaceSchemaEventHandler(ModuleContext context) {
	this.context = context;
    }

    public boolean handleEvent(ValidationEvent event) {
	// TODO Auto-generated method stub

	LogUtils.logError(context, AALSpaceSchemaEventHandler.class,
		"AALSpaceSchemaEventHandler",
		new Object[] { "Error during AALSpace Schema validation" },
		null);

	LogUtils.logError(context, AALSpaceSchemaEventHandler.class,
		"AALSpaceSchemaEventHandler",
		new Object[] { "Severity: " + event.getSeverity()
			+ " Message: " + event.getMessage()
			+ " Position. Coloumn: "
			+ event.getLocator().getColumnNumber()
			+ "Line Number: " + event.getLocator().getLineNumber()
			+ " in the node: "
			+ event.getLocator().getNode().getNodeName() }, null);
	return true;
    }

}
