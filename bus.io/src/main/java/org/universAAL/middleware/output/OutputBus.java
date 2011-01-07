/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.output;

import org.universAAL.middleware.io.rdf.Submit;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari
 * 
 */
public interface OutputBus {
	public void adaptationParametersChanged(DialogManager dm, OutputEvent oe,
			String changedProp);

	public void addNewRegParams(String subscriberID,
			OutputEventPattern newSubscription);

	public void abortDialog(String publisherID, String dialogID);

	public void dialogFinished(String subscriberID, Submit submission,
			boolean poppedMessage);

	public void dialogSuspended(DialogManager dm, String dialogID);

	public String register(OutputPublisher publisher);

	public String register(OutputSubscriber subscriber,
			OutputEventPattern initialSubscription);

	public void removeMatchingRegParams(String subscriberID,
			OutputEventPattern oldSubscription);

	public void resumeDialog(String publisherID, String dialogID,
			Resource dialogData);

	public void sendMessage(String publisherID, OutputEvent event);

	public void unregister(String publisherID, OutputPublisher publisher);

	public void unregister(String subscriberID, OutputSubscriber subscriber);
}
