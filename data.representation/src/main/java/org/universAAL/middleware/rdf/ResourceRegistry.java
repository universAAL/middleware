/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.rdf;

import java.util.HashMap;
import java.util.Hashtable;


public final class ResourceRegistry {
    
    // Singleton instance
    private static ResourceRegistry instance = new ResourceRegistry();
    
    // maps URI (of Resource class) to FactoryEntry
    // unsynchronized: we assume that there will be much more read access than
    // modifications (except at the startup) -> modifications will copy the
    // complete map
    private volatile HashMap resourceMap = new HashMap();
    
    private class FactoryEntry {
	public ResourceFactory factory;
	public int factoryIndex;
	FactoryEntry(ResourceFactory factory, int factoryIndex) {
	    this.factory = factory;
	    this.factoryIndex = factoryIndex;
	}
    }
    
    /**
     * Registration of named objects. When getting a Resource (e.g. by a
     * serializer), either the named Resource is retrieved according to
     * the URI of the specific instance, or a new object is created which is
     * derived from Resource according to the class URI. Register an object by
     * calling {@link #addNamedResource(Resource r)}. The registered instance
     * - given its instance URI - can then be retrieved by calling
     * {@link #getResource(String classURI, String instanceURI)}
     */
    // maps URI (of instance) to Resource
    private Hashtable namedResources = new Hashtable();
    
//    private Hashtable dataTypeProperties = new Hashtable();
//    private Hashtable objectProperties = new Hashtable();
//    
//    private static class ConcreteDataTypeProperty extends DataTypeProperty {
//	public ConcreteDataTypeProperty(Object password, String uri) {
//	    super(password, uri);
//	}
//    }
//    private static class ConcreteObjectProperty extends ObjectProperty {
//	public ConcreteObjectProperty(Object password, String uri) {
//	    super(password, uri);
//	}
//    }
    
    
    
    
    private ResourceRegistry() {}
    
    public static ResourceRegistry getInstance() {
	return instance;
    }
    
    
    
    public synchronized void registerResourceFactory(String classURI,
	    ResourceFactory fac, int factoryIndex) {
	HashMap newResourceMap = new HashMap(resourceMap.size()+1);
	newResourceMap.putAll(resourceMap);
	newResourceMap.put(classURI, new FactoryEntry(fac, factoryIndex));
	resourceMap = newResourceMap;
    }

    /**
     * Register a new named Resource instance (instead of a Resource
     * class). The instance can be retrieved by calling
     * {@link #getResource(String classURI, String instanceURI)}
     * 
     * @param r
     *            The Resource to register.
     */
    public void registerNamedResource(Resource r) {
	if (r != null && !r.isAnon())
	    namedResources.put(r.uri, r);
    }
    
    
    
    public boolean isRegisteredClass(String classURI) {
	return resourceMap.containsKey(classURI);
    }
    
    
    
    /**
     * Get a Resource with the given class and instance URI.
     * 
     * @param classURI
     *            The URI of the class.
     * @param instanceURI
     *            The URI of the instance.
     * @return The Resource object with the given 'instanceURI', or a new
     *         Resource, if it does not exist.
     * @see #registerNamedResource(Resource)
     * @see #getNamedResource(String)
     */
    public Resource getResource(String classURI, String instanceURI) {
	if (classURI == null)
	    return null;

	FactoryEntry entry = (FactoryEntry) resourceMap.get(classURI);
	if (entry == null) {
	    System.out.println("getResource: no factory entry for " + classURI + "  " + instanceURI);
	    return null;
	}
	ResourceFactory fac = entry.factory;
	if (fac == null) {
	    System.out.println("getResource: no factory for " + classURI + "  " + instanceURI);
	    return null;
	}

	try {
	    if (!Resource.isAnonymousURI(instanceURI)) {
		Resource r = getNamedResource(classURI, instanceURI);
		if (r != null)
		    return r;
	    }

	    return (Resource) fac.createInstance(classURI, instanceURI, entry.factoryIndex);
	} catch (Exception e) {
	    System.out.println("getResource: catch for " + classURI + "  " + instanceURI);
	    return null;
	}
    }

    public Resource getNamedResource(String classURI, String instanceURI) {
	// TODO: test if the resource is really of type classURI
//	Resource r = getNamedResource(instanceURI);
//	if (r instanceof ManagedIndividual)
	return getNamedResource(instanceURI);
    }
    
    public Resource getNamedResource(String instanceURI) {
	if (instanceURI == null)
	    return null;
	return (Resource) namedResources.get(instanceURI);
    }
        
    
//    public DataTypeProperty getDataTypeProperty(String propURI) {
//	return (DataTypeProperty) dataTypeProperties.get(propURI);
//    }
//    
//    public ObjectProperty getObjectProperty(String propURI) {
//	return (ObjectProperty) objectProperties.get(propURI);
//    }
//    
//    
//    public synchronized DataTypeProperty createDataTypeProperty(Object password, String propURI) {
//	if (objectProperties.containsKey(propURI)) {
//	    LogUtils
//		    .logDebug(
//			    SharedResources.moduleContext,
//			    ResourceRegistry.class,
//			    "createDataTypeProperty",
//			    new Object[] {
//				    "ERROR: creating data type property with URI that already exists as object property:",
//				    propURI }, null);
//	    return null;
//	}
//	if (dataTypeProperties.containsKey(propURI))
//	    return getDataTypeProperty(propURI);
//	DataTypeProperty prop = new ConcreteDataTypeProperty(password, propURI);
//	dataTypeProperties.put(propURI, prop);
//	return prop;
//    }
//    public synchronized ObjectProperty createObjectProperty(Object password, String propURI) {
//	if (dataTypeProperties.containsKey(propURI)) {
//	    LogUtils
//		    .logDebug(
//			    SharedResources.moduleContext,
//			    ResourceRegistry.class,
//			    "createObjectProperty",
//			    new Object[] {
//				    "ERROR: creating object property with URI that already exists as data type property:",
//				    propURI }, null);
//	    return null;
//	}
//	if (objectProperties.containsKey(propURI))
//	    return getObjectProperty(propURI);
//	ObjectProperty prop = new ConcreteObjectProperty(password, propURI);
//	objectProperties.put(propURI, prop);
//	return prop;
//    }
}
