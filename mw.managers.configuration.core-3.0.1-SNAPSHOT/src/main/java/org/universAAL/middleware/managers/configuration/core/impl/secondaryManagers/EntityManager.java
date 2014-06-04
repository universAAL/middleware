/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.managers.configuration.core.impl.factories.ScopeFactory;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.rdf.Resource;

/**
 * Collection of {@link Entity Entities} managed in a master file.
 * @author amedrano
 *
 */
public class EntityManager {

    private static final String PROP_ENTITIES = Resource.uAAL_VOCABULARY_NAMESPACE + "entries";
    private static final String UTF_8 = "utf-8";
    private static final String ROOT_URI = Resource.uAAL_VOCABULARY_NAMESPACE + "storedEntities";
    
    private File file;
    private ModuleContext mc;
    private SharedObjectConnector hub;
    
    /**
     * Create and link the manager.
     */
    public EntityManager(SharedObjectConnector connector, File file) {
	this.file = file;
	this.mc = connector.getContext();
	hub = connector;
	if (!file.exists()){
	    LogUtils.logWarn(mc, getClass(), "Constructor",
			    new Object[] { "File doesn't exist, creating it." }, null);
		    try {
				file.createNewFile();
			} catch (IOException e) {
			    LogUtils.logError(mc, getClass(), "Constructor",
					    new Object[] { "Could not create file." }, null);
			}
	}
    }

    /**
     * Read the master file.
     * @return
     * @throws FileNotFoundException
     */
    private List<Entity> load() throws FileNotFoundException{
	String serialized = "";

	try {
		serialized = new Scanner(file,UTF_8).useDelimiter("\\Z").next();
	} catch (Exception e){
		/*
		 *  either:
		 *  	- empty file
		 *  	- non existent file
		 *  	- Scanner failture...
		 *  Nothing to do here
		 */
	}
	
	Resource root;
	try {
		root = (Resource) hub.getMessageContentSerializer().deserialize(serialized);
	} catch (Exception e) {
		LogUtils.logWarn(mc, getClass(), "load", new String[]{"deserializer error. ", "interpreting empty list!"}, e);
		root = null;
	}
	if (serialized.length() > 5 && root !=null){
	    return (List<Entity>) root.getProperty(PROP_ENTITIES);
	} else {
	    return new ArrayList<Entity>();
	}
    }
    
    /**
     * Load the master file as a Map.
     * @return
     */
    private Map<String, Entity> loadAsMap(){
	Map<String, Entity> maped = new HashMap<String, Entity>();
	List<Entity> list;
	try {
	    list = load();
	} catch (FileNotFoundException e) {
	    list = Collections.emptyList();
	}
	for (Entity entity : list) {
	    maped.put(entity.getURI(), entity);
	}
	return maped;
    }
    
    /**
     * save the list of entities in the master file.
     * @param entities
     */
    private void store(Collection<Entity> entities){
	Resource root = new Resource(ROOT_URI);
	root.setProperty(PROP_ENTITIES, new ArrayList<Entity>(entities));
	String serialized = hub.getMessageContentSerializer().serialize(root);
	//writing
	OutputStreamWriter osw;
	try {
		osw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(UTF_8));
		osw.write(serialized);
		osw.close();
	} catch (FileNotFoundException e) {
		// Highly improbable.
		LogUtils.logError(mc, getClass(), "store", new String[]{"File not Found ??!!"}, e);
	} catch (IOException e) {
		LogUtils.logError(mc, getClass(), "store", new String[]{"unable to store"}, e);
	}
    }
    
    /**
     * Add a single Entity to the configuration file.
     * @param ent the entity to be added.
     * @return true if it was added, false if it couldn't be added because there is a newer version stored.
     */
    public synchronized boolean addEntity(Entity ent){
	if (ent == null){
	    return false;
	}
	boolean toBeAdded = false;
	
	ent.unliteral();
	
	Map<String, Entity> emap = loadAsMap();
	
	toBeAdded = checkAdd(ent, emap);
	
	if (toBeAdded) {
	    emap.remove(ent.getURI());
	    emap.put(ent.getURI(),ent);
	    store(emap.values());
	}
	return toBeAdded;
    }
    
    /**
     * Check that it is possible to add an entity in the file.
     * @param newEnt
     * @param emap
     * @return
     */
    private boolean checkAdd(Entity newEnt, Map<String, Entity> emap){
	
	Entity existing = emap.get(newEnt.getURI());
	
	return 	existing == null 
		|| newEnt.isNewerThan(existing);
//		|| newEnt.getVersion() ==  existing.getVersion();
	    //FIXME, when they are not equal but the version is the same, there is a conflict!
	    
    }
    
    /**
     * Try to merge all new entities.
     * For those which there is a newer version stored, the newer version will be 
     * included in the return list. 
     * @param news list of {@link Entity Entities} to attempt to "add".
     * @return the list of newer {@link Entity Entities} found.
     */
    public synchronized List<Entity> mergeProbe(List<Entity> news){
	Map<String, Entity> emap = loadAsMap();
	return mergeProbe(news, emap);
    }
    
    /**
     * same as {@link EntityManager#mergeAdd(List)} but does not actually add to the master file, instead it updates the emap.
     * @param news
     * @param emap
     * @return
     */
    private List<Entity> mergeProbe(List<Entity> news, Map<String, Entity> emap){
    	List<Entity> rejected = new ArrayList<Entity>();
    	for (Entity ent : news) {
    		if (ent == null){
    			continue;
    		}
    		ent.unliteral();
    		Entity existing = emap.get(ent.getURI());

    		if(!checkAdd(ent, emap)) {
    			rejected.add(existing);
    		} else {
    			emap.put(ent.getURI(), ent);
    		}
    	}
    	return rejected;
    }
    
    /**
     * Try to add all new entities.
     * For those that are not be added (because there is a newer version stored), the newer version will be 
     * included in the return list. 
     * @param news list of {@link Entity Entities} to attempt to add.
     * @return the list of newer {@link Entity Entities} found.
     */
    public synchronized List<Entity> mergeAdd(List<Entity> news){
	Map<String, Entity> emap = loadAsMap();
	List<Entity> rejected = mergeProbe(news, emap);
	List<Entity> newset = new ArrayList<Entity>(emap.values());
	store(newset);
	return rejected;
    }
    
    /**
     * Look in the file for an {@link Entity} with matching URI (from {@link Scope} use {@link ScopeFactory#getScopeURN(Scope)}).
     * @param uri the uri(scope) to be matched.
     * @return the entity if found, null otherwise.
     */
    public synchronized Entity find(String uri){
	if (uri == null 
		|| uri.isEmpty()
		|| ScopeFactory.getScope(uri) == null){
	    return null;
	}
	Map<String, Entity> emap = loadAsMap();
	return emap.get(uri);
    }
    
    /**
     * Look in the file for an {@link Entity} with matching Restrictions.
     * @param filters all the restrictions that the Entity should comply (anded, empty list returns all).
     * @return the matching entities found.
     */
    public synchronized List<Entity> find(List<TypeExpression> filters){
	List<Entity> list;
	try {
	    list = load();
	} catch (FileNotFoundException e) {
	    return null;
	}
	return EntityManager.filter(list, filters);
    }
    
    /**
     * Utility method, Filter a list of entities and return only those that match all {@link TypeExpression} filters.
     * @param list
     * @param filters
     * @return
     */
    public static List<Entity> filter(List<Entity> list, List<TypeExpression> filters){
	List<Entity> result = new ArrayList<Entity>();
	for (Entity entity : list) {
	    boolean forAll = true;
	    for (TypeExpression r : filters) {
		forAll &= r.hasMember(entity);
	    }
	    if (forAll){
		result.add(entity);
	    }
	}
	return result;
    }
}
