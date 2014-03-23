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

package org.universAAL.middleware.managers.configuration.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.universAAL.middleware.brokers.message.configuration.ConfigurationMessage;
import org.universAAL.middleware.brokers.message.configuration.ConfigurationMessage.ConfigurationMessageType;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationDefinedElsewhere;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DynamicDescribedEntity;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DynamicDescribedEntityListener;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.EntityPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.InstancePattern;
import org.universAAL.middleware.managers.api.ConfigurationEditor;
import org.universAAL.middleware.managers.api.ConfigurationManager;
import org.universAAL.middleware.managers.api.ConfigurationManagerConnector;
import org.universAAL.middleware.managers.configuration.core.impl.factories.EntityFactory;
import org.universAAL.middleware.managers.configuration.core.impl.factories.ScopeFactory;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.ConfigurationEditorPool;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.EntityManager;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.FileManagement;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.PendingRequestsManager;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.SharedObjectConnector;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.SynchronousConfEntityManager;
import org.universAAL.middleware.managers.configuration.core.owl.AALConfigurationOntology;
import org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile;
import org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.Complement;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.URIRestriction;
import org.universAAL.middleware.owl.Union;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.xsd.Base64Binary;

/**
 * @author amedrano
 *
 */
public class ConfigurationManagerImpl implements 
ConfigurationManager,
ConfigurationManagerConnector,
ConfigurationEditor,
DynamicDescribedEntityListener{
    
    static final String PROP_PARAM = AALConfigurationOntology.NAMESPACE + "messageParameter";
    static final String PROP_LOCALE = AALConfigurationOntology.NAMESPACE + "preferredLocale";
    
    private ModuleContext context;
    
    /*
     * Secondary Managers 
     */
    SharedObjectConnector shared;
    FileManagement fileM;
    EntityManager manager;
    private ConfigurationEditorPool editorsPool;
    private PendingRequestsManager pendingRequests;
    /*
     * Internal structures
     */
    Map<String,WeakReference<ConfigurableModule>> moduleRegistry;
    //FIXME module registry must be a map of URNs to a set of configurablemodules!
    HashMap<String, DescribedEntity> entitiesSources;
    private AALConfigurationOntology ont;
    
    /**
     * 
     */
    public ConfigurationManagerImpl(ModuleContext mc, FileManagement fm) {
	this.context = mc;
	//register ontology
	ont = new AALConfigurationOntology();
	OntologyManagement.getInstance().register(context, ont);
	moduleRegistry = new HashMap<String, WeakReference<ConfigurableModule>>();
	entitiesSources = new HashMap<String,DescribedEntity>();
	fileM= fm;
	shared = new SharedObjectConnector(mc);
	manager = new EntityManager(shared, fileM.getMasterFile());
	editorsPool = new ConfigurationEditorPool(this);
	pendingRequests = new PendingRequestsManager(editorsPool);
    }
    
    public ModuleContext getContext(){
	return context;
    }

    public void finish(){
	moduleRegistry.clear();
	for (DescribedEntity de : entitiesSources.values()) {
	    if (de instanceof DynamicDescribedEntity){
		((DynamicDescribedEntity)de).unRegisterListener(this);
	    }
	}
	entitiesSources.clear();
	manager = null;
	OntologyManagement.getInstance().unregister(context, ont);
    }
    
    /** {@ inheritDoc}	 */
    public void register(List<DescribedEntity> confPattern,
	    ConfigurableModule listener) {
	WeakReference<ConfigurableModule> ref = new WeakReference<ConfigurableModule>(listener);
	List<Entity> registered = new ArrayList<Entity>();
	for (DescribedEntity de : confPattern) {
	    if (de instanceof ConfigurationDefinedElsewhere){
		moduleRegistry.put(ScopeFactory.getScopeURN(de.getScope()),ref );
	    }else {
		Entity e = EntityFactory.getEntity(de, Locale.ENGLISH);
		if (e != null) {
		    moduleRegistry.put(e.getURI(), ref);
		    entitiesSources.put(e.getURI(), de);
		    registered.add(e);
		}
	    }
	    if (de instanceof DynamicDescribedEntity){
		((DynamicDescribedEntity)de).registerListener(this);
	    }
	}
	List<Entity> alreadyStored = manager.mergeAdd(registered);
	for (Entity e : alreadyStored) {
		//update the modules with the stored entities
	    updateLocalValue(e);
	}
        List<Entity> news = new ArrayList<Entity>(registered);
        news.removeAll(alreadyStored);
        for (Entity e : news) {
    		//update the modules with the default entities
    	    updateLocalValue(e);
		}
        propagate(news);
    }

    /** {@ inheritDoc}	 */
    public void register(DescribedEntity[] confPattern,
	    ConfigurableModule listener) {
	register(Arrays.asList(confPattern), listener);
    }

    /** {@ inheritDoc}	 */
    public void unregister(ConfigurableModule listener) {
	List<String> tbr = new ArrayList<String>();
	for(Entry<String, WeakReference<ConfigurableModule>> ent: moduleRegistry.entrySet()){
	    if (ent.getValue().get().equals(listener)
		    ||ent.getValue().get() == null){
		tbr.add(ent.getKey());
	    }
	}
	for (String k : tbr) {
	    moduleRegistry.remove(k);
	}
    }

    /**
     * @param list
     */
    private void propagated(List<Entity> list) {
	// update ConfigurationEditors
	for (Entity e : list) {
	    editorsPool.entityUpdated(e);
	}
	/*
	 *  filter out remotes (locals for other nodes) so they are not stored locally.
	 *  IE: after filtering, the remainder must be not(instanceScope)|localInstance
	 */
	Union u = new Union();
	u.addType(new Complement(new InstancePattern().getRestriction()));
	u.addType(localOnlyExpression());
	List<TypeExpression> filter = new ArrayList<TypeExpression>();
	list = EntityManager.filter(list, filter);
	// filtering complete
	
	List<Entity> toBeRepropagated = manager.mergeProbe(list);
	List<Entity> toBeAddedLocally = new ArrayList<Entity>(list);
	List<Entity> rejected = new ArrayList<Entity>();
        toBeAddedLocally.removeAll(toBeRepropagated);
        for (Entity e : toBeAddedLocally) {
            e.unliteral();
	    if (!updateLocalValue(e)){
		//To be removed from merge add
		rejected.add(e);
		// get the current value
		Entity actual = manager.find(e.getURI());
		// declare it as the most recent version
		actual.setVersion(e.getVersion() +1);
		// and propagate.
		toBeRepropagated.add(actual);
	    }
        }
        toBeAddedLocally.removeAll(rejected);
        manager.mergeAdd(toBeAddedLocally);
        propagate(toBeRepropagated);
    }

    /**
     * @param e
     */
    boolean updateLocalValue(Entity e) {
	ConfigurableModule cm = moduleRegistry.get(e.getURI()).get();
	if(cm == null){
	    moduleRegistry.remove(e.getURI());
	    return false;
	}
	Object value = null;
	if (e instanceof ConfigurationFile){
		ConfigurationFile cf = ((ConfigurationFile)e);
	    Base64Binary content = cf.getContent();
	    URL url = null;
	    try {
		url = new URL(cf.getLocalURL());
	    } catch (MalformedURLException e1) {
		// very unlikely
	    }
	    if (url != null){
		/*
		 * 	check it is local, it exists ;
		 * 	if not make it local by caching it
		 */
		if ("file".equalsIgnoreCase(url.getProtocol())
			|| url.getProtocol() == null
			|| url.getProtocol().isEmpty()) {
		    // it is a local file
		    try {
			File f = new File( URLDecoder.decode( url.getFile(), "UTF-8" ) );
			if (f.exists()) {
			    value = f;
			}
		    } catch (UnsupportedEncodingException e1) {  }
		    
		} else {
		    //cache the file and set value = file
		    value = fileM.cache(url);
		}
	    }
	    
	    if (content != null){
		File f = null; 
		if (value instanceof File){
		    //the content is updated
		    f = (File) value;
		}else {
		    //generate a file in the cache 
		    f = fileM.getLocalFile(e.getURI());
		    //and set localURL (and save Entity?)
		    try {
			((ConfigurationFile) e).setLocalURL(f.toURI().toURL().toString());
			e.incrementVersion();
			manager.addEntity(e);
			//and set as return value
			value = f;
		    } catch (MalformedURLException e1) {
			return false;
		    }
		}
		//rewrite file
		if (! ConfigurationFile.writeContentToFile(content, f)){
		    LogUtils.logError(context, getClass(), "updateLocalValue", "unable to write content in file: " + f);
		    return false;
		}
	    } 
	    else {
		//file was updated, content needs to be reloaded.
		try {
		    cf.loadContentFromLocalURL();
		    e.incrementVersion();
		    manager.addEntity(e);
		} catch (IOException e1) {
		    LogUtils.logWarn(context, getClass(), "updateLocalValue", "unable lo load content from URL: " +
			    cf.getLocalURL() +"\n Trying to load Default.");
		    try {
			cf.loadContentFromDefaultURL();
			e.incrementVersion();
			manager.addEntity(e);
		    } catch (IOException e2) {
			    LogUtils.logError(context, getClass(), "updateLocalValue", "unable lo load content from defaultURL: " +
				    cf.getDefaultURL());
			    return false;
		    }
		}
	    //value is assigned in previous "if".
	    }
	}
	if (e instanceof ConfigurationParameter){
	    ConfigurationParameter cp = (ConfigurationParameter) e;
	    value = cp.getValue();
//	    if (value == null){
//		value = cp.getDefaultValue();
//	    }
	}
	
	if (value != null)
	    return cm.configurationChanged(ScopeFactory.getScope(e.getURI()), value);
	return false;
    }
    
    boolean updateLocalAndPropagate(Entity e){
	if (updateLocalValue(e)){
	    propagate(e);
	    return true;
	}else {
	    return false;
	}
    }

    /** {@ inheritDoc}	 */
    public void updatedDescription(DescribedEntity dentity) {
	Entity old = manager.find(ScopeFactory.getScopeURN(dentity.getScope()));
	Entity ne = EntityFactory.updateEntity(old, dentity, Locale.ENGLISH);
	if (ne.isNewerThan(old)){
	    manager.addEntity(ne);
	    List<Entity> l = new ArrayList<Entity>();
	    l.add(ne);
	    propagate(l);
	}
    }

    /** {@ inheritDoc}	 */
    public void updatedValue(DescribedEntity dentity, Object value) {
	Entity old = manager.find(ScopeFactory.getScopeURN(dentity.getScope()));
	Entity ne = EntityFactory.updateEntity(old, dentity, Locale.ENGLISH);
	if (old instanceof ConfigurationParameter ) {
	    if(((ConfigurationParameter)ne).setValue(value) 
		    && !((ConfigurationParameter)old).getValue().equals(value)){
		ne.incrementVersion();
	    }else{
		ne = old;
	    }
	}
	if (ne instanceof ConfigurationFile){
	    // update the contents with file object
	    Base64Binary content = null;
	    if (value instanceof File){
		try {
		    ((ConfigurationFile) ne).loadContentFormURL(((File)value).toURI().toURL());
		} catch (MalformedURLException e) {
		    LogUtils.logError(context, getClass(), "udpdatedValue", 
			    "wrong URL.");
		} catch (IOException e) {
		    LogUtils.logError(context, getClass(), "udpdatedValue", 
			    "can't access file: "+ value);
		}
	    }
	    else if (value instanceof URL){
		try {
		    ((ConfigurationFile)ne).setLocalURL(value.toString());
		    ((ConfigurationFile)ne).loadContentFromLocalURL();
		} catch (IOException e) {
		    LogUtils.logError(context, getClass(), "updatedValue", new String[]{"unable to load new url"}, e);
		    return;
		}
	    }
	    else if (value instanceof String){
		content = new Base64Binary(((String)value).getBytes());
		((ConfigurationFile)ne).setLocalURL(null);
	    }
	    else if (value instanceof Base64Binary){
		content = (Base64Binary) value;
		((ConfigurationFile)ne).setLocalURL(null);
	    }
	    else if (value instanceof byte[]){
		content = new Base64Binary((byte[])value);
		((ConfigurationFile)ne).setLocalURL(null);
	    }
	    if (value == null){
		try {
		    ((ConfigurationFile)ne).loadContentFromDefaultURL();
		} catch (IOException e) {
		    LogUtils.logError(context, getClass(), "udpdatedValue", 
			    "Give me a break... value set to null, tried to load default url, and that failed too!");
		}
	    }
	    ((ConfigurationFile)ne).setContent(content);
	    ne.incrementVersion();
	}
	if (ne.isNewerThan(old)){
	    manager.addEntity(ne);
	    updateLocalValue(ne);
	    propagate(ne);
	}
    }
    
    /** {@ inheritDoc}	 */
    public List<ConfigurableEntityEditor> getMatchingConfigurationEditors(
            List<EntityPattern> configPattern, Locale locale) {
	SynchronousConfEntityManager scem = new SynchronousConfEntityManager();
        registerConfigurableEntityManager(scem, configPattern, locale);
        List<ConfigurableEntityEditor> res = scem.getList();
        unRegisterConfigurableEntityManager(scem);
        return res;
    }

    /** {@ inheritDoc}	 */
    public void registerConfigurableEntityManager(
            ConfigurableEntityManager cmanager,
            List<EntityPattern> configPattern, Locale locale) {
	List<TypeExpression> filters = new ArrayList<TypeExpression>();
	for (EntityPattern entityPattern : configPattern) {
	    filters.add(entityPattern.getRestriction());
	}
	
        List<Entity> stored = manager.find(filters);
        for (Entity e : stored) {
            try {
		Entity ne = EntityFactory.updateEntity(e, entitiesSources.get(e.getURI()), locale);
		manager.addEntity(ne);
		// use ConfigurationEditorPool
	    } catch (Exception e1) {
		/*
		 * described entity is not local.
		 * create a remoteEditor and let it manage it (will be done by editorsPool.get(Entity)).
		 */
	    }
            cmanager.addConfigurableEntity(editorsPool.get(e));
	}
        /*
         * add filter to the request, 
         * remove :
         * 	- global configs (they are stored and maintained on all nodes.
         * require:
         * 	- local (to recipient) config only that match the filters.
         */
        URIRestriction ur = new URIRestriction();
        ur.setPattern(".*inst\\:.*");
        filters.add(ur);
        
        pendingRequests.add(cmanager, filters);
        request(filters, locale);
    }

    /** {@ inheritDoc}	 */
    public void unRegisterConfigurableEntityManager(
            ConfigurableEntityManager manager) {
       pendingRequests.remove(manager);
    }

    void propagate(Entity singleEntity){
	List<Entity> l = new ArrayList<Entity>();
	l.add(singleEntity);
	propagate(l);
    }
   
    public  TypeExpression localOnlyExpression(){
	String localPeerID = shared.getAalSpaceManager().getMyPeerCard().getPeerID();
	URIRestriction localOnly = new URIRestriction();
	localOnly.setPattern("urn\\:configscope\\:.*\\:inst\\:" + localPeerID + ".*");
	return localOnly;
    }
    
    private static List<TypeExpression> asList(TypeExpression expr){
	List<TypeExpression> list = new ArrayList<TypeExpression>();
	list.add(expr);
	return list;
    }
    
    /*
     * NETWORKING 
     */
    
    private void request(List<TypeExpression> filters, Locale locale){
	Resource root = new Resource();
	root.changeProperty(PROP_PARAM, filters);
	root.changeProperty(PROP_LOCALE, locale);
	ConfigurationMessage cm = new ConfigurationMessage(
		ConfigurationMessageType.QUERY, 
		shared.getAalSpaceManager().getMyPeerCard(),
		shared.getMessageContentSerializer().serialize(root));
	// send
	shared.getControlBroker().sendConfigurationMessage(cm);
    }
    
    private void propagate(List<Entity> list){
	// update ConfigurationEditors
	for (Entity e : list) {
	    editorsPool.entityUpdated(e);
	}
	Resource root = new Resource();
	root.changeProperty(PROP_PARAM, list);
	String serialized = shared.getMessageContentSerializer().serialize(root);
	ConfigurationMessage cm = new ConfigurationMessage(ConfigurationMessageType.PROPAGATE, 
		shared.getAalSpaceManager().getMyPeerCard(),
		serialized);
	//send the list of Entities to all nodes.
	shared.getControlBroker().sendConfigurationMessage(cm);
    }
    
    /** {@ inheritDoc}	 */
    public void processPropagation(ConfigurationMessage message) {
	//ignore my own propagations
	if (message.isSentFrom(shared.getAalSpaceManager().getMyPeerCard())){
	    return;
	}
	Object r = shared.getMessageContentSerializer().deserialize(message.getPayload());
	if (r instanceof Resource
		&& ((Resource)r).hasProperty(PROP_PARAM)){
	    // a Resource that may contain Entities
	    Object cand = ((Resource)r).getProperty(PROP_PARAM);
	    if (cand instanceof List 
		    && !((List)cand).isEmpty()
		    && ((List)cand).get(0) instanceof Entity){
		// it contains at least one entity.
		propagated((List)cand);
	    }
	}
    }

    /** {@ inheritDoc}	 */
    public void processRequest(ConfigurationMessage message) {
	Object r = shared.getMessageContentSerializer().deserialize(message.getPayload());
	if (r instanceof Resource
		&& ((Resource)r).hasProperty(PROP_PARAM)){
	    // a Resource that may contain TypeExpressions
	    Object cand = ((Resource)r).getProperty(PROP_PARAM);
	    if (cand instanceof List){
		List response = manager.find((List)cand);
		if (response != null
			&& !response.isEmpty()){
		    // respond local-only entities
		    List<Entity> filtered = EntityManager.filter(response, asList(localOnlyExpression()));
		    List<Entity> processed = new ArrayList<Entity>();
		    for (Entity e : filtered) {
			try {
			    e = EntityFactory.updateEntity(e, entitiesSources.get(e.getURI()), (Locale) ((Resource)r).getProperty(PROP_LOCALE));
			    processed.add(e);
			} catch (Exception e1) {
			    // described entity not found locally;
			}
		    }
		    Resource res = new Resource();
		    res.changeProperty(PROP_PARAM, processed);
		    ConfigurationMessage cm = message.createResoponse(
			    shared.getMessageContentSerializer().serialize(res));
		    // send
		    shared.getControlBroker().sendConfigurationMessage(cm);
		}
	    }
	}
    }
    
    public void processResponse(ConfigurationMessage message){
	Object r = shared.getMessageContentSerializer().deserialize(message.getPayload());
	if (r instanceof Resource
		&& ((Resource)r).hasProperty(PROP_PARAM)){
	    // a Resource that may contain Entities
	    Object cand = ((Resource)r).getProperty(PROP_PARAM);
	    if (cand instanceof List 
		    && !((List)cand).isEmpty()
		    && ((List)cand).get(0) instanceof Entity){
		// it contains at least one entity.
		List<Entity> elist = ((List)cand);
		for (Entity e : elist) {
			editorsPool.get(e).updated(e);
		}
		pendingRequests.processResponse(elist);
	    }
	}
    }
}
