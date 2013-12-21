/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.ui.impl.generic;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.ui.UIHandlerProfile;

/**
 * Strategy Stack that is in charge of managing registrations, generally a
 * registration is composed of {@link Matchable} information that is tied to an Id. <br>
 * Thus the main data representation is a {@link Map} of keys {@link Resource}s and
 * values {@link String id}s. This way Resources can be iterated to do match making, 
 * then retrieve their associated ID.
 *  <center> <img style="background-color:white;" src="doc-files/CoordinatedRegistrationStrategy.png"
 * alt="UIStrategy messages" width="70%"/> </center>
 * 
 * @author amedrano
 * 
 */
public class CoordinatedRegistrationManagement extends CoordinatedStrategy {
    
    /**
     * Property to Hold the RegistrationID
     */
    public static final String PROP_uAAL_REGISTRATION_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "registrationID";
    
    /**
     * Property to Hold the Registration
     */
    public static final String PROP_uAAL_REGISTRATION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "registration";

    
    private class RegistrationMessage extends Resource implements
    	EventMessage<CoordinatedRegistrationManagement> {

        public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
    	    + "NewRegistration";
        
        public RegistrationMessage(String regID, Matchable reg){
    	addType(MY_URI, true);
    	setProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION_ID, regID);
    	setProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION, reg);
        }

	public RegistrationMessage() {
	    super();
	}

	/** {@ inheritDoc}	 */
        public void onReceived(CoordinatedRegistrationManagement strategy, BusMessage m,
    	    String senderID) {
    	strategy.addRegistration(
    		(String) getProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION_ID), 
    		(UIHandlerProfile) getProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION));
    	
        }
    }

    private class UnRegistrationMessage extends Resource implements
    EventMessage<CoordinatedRegistrationManagement> {

	public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		+ "UnRegistration";

	public UnRegistrationMessage(String regID){
	    addType(MY_URI, true);
	    setProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION, regID);
	}

	public UnRegistrationMessage() {
	    super();
	}

	/** {@ inheritDoc}	 */
	public void onReceived(CoordinatedRegistrationManagement strategy, BusMessage m, String senderID) {
	    strategy.removeAllRegistries((String) getProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION));
	}
    }

    private class RemoveMatchingRegistrationMessage extends Resource implements
    	EventMessage<CoordinatedRegistrationManagement> {

        public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
    	    + "MatchingUnRegistration";
        
        public RemoveMatchingRegistrationMessage(String regID, Matchable filter){
    	addType(MY_URI, true);
    	setProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION_ID, regID);
    	setProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION, filter);
        }

	public RemoveMatchingRegistrationMessage() {
	    super();
	}

	/** {@ inheritDoc}	 */
        public void onReceived(CoordinatedRegistrationManagement strategy,
    	    BusMessage m, String senderID) {
    	strategy.removeMatchingRegistries(
    		(String) getProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION_ID),
    		(Matchable) getProperty(CoordinatedRegistrationManagement.PROP_uAAL_REGISTRATION));
        }
    }

    private class CoordinatedRegsMessageFactory implements ResourceFactory{

	/** {@ inheritDoc}	 */
	public Resource createInstance(String classURI, String instanceURI,
		int factoryIndex) {
	    switch (factoryIndex) {
	    case 0:
		return new RegistrationMessage();
	    case 1:
		return new UnRegistrationMessage();
	    case 2:
		return new RemoveMatchingRegistrationMessage();
	    default:
		break;
	    }
	    return null;
	}
	
    }
    
    private class CoordinatedRegMessageOnt extends Ontology{

	private CoordinatedRegsMessageFactory factory;

	/**
	 * @param ontURI
	 */
	public CoordinatedRegMessageOnt(String ontURI) {
	    super(ontURI);
	    factory = new CoordinatedRegsMessageFactory();
	}

	/** {@ inheritDoc}	 */
	@Override
	public void create() {
	    createNewRDFClassInfo(RegistrationMessage.MY_URI, factory, 0);
	    createNewRDFClassInfo(UnRegistrationMessage.MY_URI, factory, 1);
	    createNewRDFClassInfo(RemoveMatchingRegistrationMessage.MY_URI, factory, 2);   
	}
	
    }
    
    private Map<Matchable, String> registrationMap;

    private CoordinatedRegMessageOnt ontology;

    /**
     * @param commModule
     * @param name
     */
    public CoordinatedRegistrationManagement(CommunicationModule commModule,
	    String name) {
	super(commModule, name);
    }

    /** {@ inheritDoc}	 */
    public synchronized void start() {
	super.start();
	ontology = new CoordinatedRegMessageOnt(
		Resource.uAAL_NAMESPACE_PREFIX + "CoordinatedRegistrationMessageOntology");
	OntologyManagement.getInstance().register(busModule, ontology);
    }

    private Map<Matchable, String> getRegistrationMap(){
	if(registrationMap == null){
		registrationMap = new Hashtable<Matchable, String>();
	}
	return registrationMap;
    }
    
    /**
     * @param commModule
     */
    public CoordinatedRegistrationManagement(CommunicationModule commModule) {
	super(commModule);
    }

    public final void addRegistration(String id, Matchable registration) {
	if (id == null || registration == null)
	    return;
	
	if (iAmCoordinator()){
	    getRegistrationMap().put(registration, id);
	}else{
	    //Send Message
	    sendEventToRemoteBusMember(getCoordinator(), new RegistrationMessage(id, registration));
	}
    }

    public final void removeAllRegistries(String id) {
	if (id == null)
	    return;
	if (iAmCoordinator()){
	    Set<Matchable> remove = new HashSet<Matchable>();
	    for (Entry<Matchable, String> ent : getRegistrationMap().entrySet()) {
		if (ent.getValue().equals(id)){
		    remove.add(ent.getKey());
		}
	    }
	    for (Matchable resource : remove) {
		getRegistrationMap().remove(resource);
	    }
	}else{
	    //Send Message
	    sendEventToRemoteBusMember(getCoordinator(), new UnRegistrationMessage(id));
	}
    }

    public final void removeMatchingRegistries(String id, Matchable filter) {
	if (id == null || filter == null) {
	    return;
	}
	if (iAmCoordinator()){
	    Set<Matchable> remove = new HashSet<Matchable>();
	    for (Entry<Matchable, String> ent : getRegistrationMap().entrySet()) {
		if (ent.getValue().equals(id)
			&& filter.matches(ent.getKey())){
		    remove.add(ent.getKey());
		}
	    }
	    for (Matchable resource : remove) {
		getRegistrationMap().remove(resource);
	    }
	}else{
	    //Send Message
	    sendEventToRemoteBusMember(getCoordinator(),new RemoveMatchingRegistrationMessage(id, filter));
	}
    }

    protected final Iterator<Matchable> registryIterator() {
	return getRegistrationMap().keySet().iterator();
    }

    protected final String getRegistryID(Resource res) {
	return getRegistrationMap().get(res);
    }
    
    protected final boolean isIdRegistered(String id){
	return getRegistrationMap().containsValue(id);
    }

    protected final Iterator<String> registryIdIterator() {
	return new HashSet<String>(getRegistrationMap().values()).iterator();
    }
    
    /** {@ inheritDoc}	 */
    public void close() {
	super.close();
	if (registrationMap != null) {
	    registrationMap.clear();
	}
	OntologyManagement.getInstance().unregister(busModule, ontology);
    }
}
