/*	
	Copyright 2007-2016 Fraunhofer IGD, http://www.igd.fraunhofer.de
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

import java.util.List;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.Manager;

/**
 * Manager for {@link DistributedLogListener}s. The manager registers as a
 * shared object. Registered listeners will be notified if a new log message is
 * issued in one of the nodes.
 * 
 * @author Carsten Stockloew
 * 
 */
public interface DistributedLogManager extends Manager {

	void addListener(DistributedLogListener listener, List<PeerCard> nodes);

	void removeListener(DistributedLogListener listener, List<PeerCard> nodes);
}
