/*******************************************************************************
 * Copyright 2017 2011 Universidad Politécnica de Madrid
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
package org.universAAL.middleware.bus.junit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.reflections.Reflections;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * Class specifically designed to ease the creation of JUnit tests where
 * buses and available ontologies need to be preloaded.
 * 
 * @author amedrano
 *
 */
public class OntTestCase extends BusTestCase {
	
	private static final File owlDir = new File("target/ontologies/");
	private MessageContentSerializer contentSerializer;

	@Override
    protected void setUp() throws Exception {
		super.setUp();
		autoLoadOntologies();
	}
	
	/**
	 * Attempts to load all ontology modules, if fail it will attempt later,
	 * after, if fortunately, dependant ontologies are loaded.
	 */
	private void autoLoadOntologies(){
		List<ModuleActivator> toBeLoaded = getOntologyModules();
		int attempts = toBeLoaded.size() *2;
		while (!toBeLoaded.isEmpty() && attempts >0) {
			attempts--;
			ModuleActivator next = toBeLoaded.remove(0);
			try{
				//load Next
				next.start(mc);
				
			} catch (Exception e){
				//Close but no cigar, try later
				toBeLoaded.add(next);
				continue;
			}
	
		}
	}

	/**
	 * Recovers the serializer. 
	 * @return the serializer.
	 */
	private MessageContentSerializer getContentserializer() {
		if (contentSerializer == null) {
		    contentSerializer = (MessageContentSerializer) mc
			    .getContainer().fetchSharedObject(
				    mc,
				    new Object[] { MessageContentSerializer.class
					    .getName() });
		    if (contentSerializer == null) {
			System.out
				.println("ERROR: no serializer found for serializing the ontology");
		    }
		}
		return contentSerializer;
	}
	
    /**
     * Uses reflection to locate all {@link ModuleActivator}s in the package
     * org.universAAL.ontology .
     * @return all instances of module activators.
     */
    private List<ModuleActivator> getOntologyModules() {
    	List<ModuleActivator> onts = new ArrayList<ModuleActivator>();
		
		Reflections reflections = new Reflections("org.universAAL.ontology");

		try {
			Set<?> subTypes = reflections.getSubTypesOf(Class.forName(ModuleActivator.class.getName()));
			
			for (Object o : subTypes) {
				if (o instanceof Class<?>){
					try {
						onts.add((ModuleActivator) ((Class<?>)o).newInstance());
					} catch (Exception e) {
						LogUtils.logError(mc, getClass(), "getOntologyModules", new String[] {"could not instantiate: " + ((Class)o).getName()},e);
					} 
				}
			}
		} catch (ClassNotFoundException e) {
			LogUtils.logError(mc, getClass(), "getOntologyModules", new String[] {"Unexpected error"},e);
		}
    	
    	return onts;
    }

    /**
     * Write to target/ontologies the serializations (in TTL, and OWL) of a given ontology.
     * @param ont the ontology to be serialized.
     * @return true iif the operation was successful.
     */
    protected boolean generateOntFiles4Ont(Ontology ont){
    	if (!owlDir.exists() && !owlDir.mkdirs()){
    		return false;
    	}
    	String name = ontFileName(ont);
    	File ttlFile = new File(owlDir,name + ".ttl");
    	return writeTTL(ont, ttlFile) && transformTTL2OWL(ttlFile, new File(owlDir,name + ".owl"));
    }
    
    /**
     * Write to target/ontologies the serializations (in TTL, and OWL) of all loaded ontologies for 
     * the testcase.
     * @return true iif the operation was successful
     */
    protected boolean generateOntFiles4ALL(){
    	boolean out = true;
    	String[] onts = OntologyManagement.getInstance().getOntoloyURIs();
    	for (int i = 0; i < onts.length; i++) {
    		 Ontology ont = OntologyManagement.getInstance().getOntology(onts[i]);
    		 if (!generateOntFiles4Ont(ont)){
    			 LogUtils.logError(mc, getClass(), "generateOntFiles4All", "Unable to generate file for: "+onts[i]);
    			 out = false;
    		 }
		}
    	return out;
    }
    
    /**
     * Write to target/ontologies the serializations (in TTL, and OWL) of all ontologies for 
     * the {@link TestCase}, that correspond to ontologies of the tested project. 
     * Test ontologies (in test-classes) are not considered. 
     * @return true iif the operation was successful
     */
    protected boolean generateOntFiles4MyProy(){
    	boolean out = true;
    	String[] onts = OntologyManagement.getInstance().getOntoloyURIs();
    	for (int i = 0; i < onts.length; i++) {
    		 Ontology ont = OntologyManagement.getInstance().getOntology(onts[i]);
    		 if (isInMyProy(ont) && !generateOntFiles4Ont(ont)){
    			 LogUtils.logError(mc, getClass(), "generateOntFiles4All", "Unable to generate file for: "+onts[i]);
    			 out = false;
    		 }
		}
    	return out;
    }
    
    /**
     * Check if an {@link Ontology} belongs to the testing project.
	 * @param ont the ontology to be checked
	 * @return true if the classloader of the ontology reports that the class
	 * is being loaded from the local folder target/classes/.
	 */
	private boolean isInMyProy(Ontology ont) {
		URL ontLoadURL = ont.getClass().getResource(ont.getClass().getSimpleName() + ".class");
		if (ontLoadURL == null ) {
			return false;
		}
		String ontLoad = ontLoadURL.toString();
		if (ontLoad != null && ontLoad.contains("/target/classes/")) {
			//System.out.println(ontLoad);
			return true;
		}
		return false;
	}

	/**
	 * Construct an appropriate filename for a given ontology.
	 * @param ont the ontology to generate the filename
	 * @return the file name corresponding to the filename stated in the URI, 
	 * if not possible to determine a random name is given.
	 */
	private String ontFileName(Ontology ont){
    	String name = ont.getInfo().getFilename();
    	if (name.endsWith(".owl")) // remove ".owl" at the end
    	    name = name.substring(0, name.length() - 4);
    	if (name == null) {
    		LogUtils.logWarn(mc, getClass(), "ontFileName", "unable to get Name for: " + ont.getInfo().getURI() 
    				+ " , generating random name.");
    	    name = Integer.toHexString(new Random(System.currentTimeMillis())
    		    .nextInt());
    	}
    	return name;
    }
    
    /**
     * Serializes and writes a TTL for a given {@link Ontology}.
     * @param ont The ontology to be serialized.
     * @param ttlFile The target file.
     * @return true iif the operation was successful.
     */
    private boolean writeTTL(Ontology ont, File ttlFile){
    	LogUtils.logInfo(mc, getClass(), "writeTTL", "Writing turtle serialization of ontology: "
    			+ ont.getInfo().getURI() + "\n\t to: " + ttlFile.getAbsolutePath());
		String serializedOntology = getContentserializer().serialize(ont);
    	try {
    	    BufferedWriter out = new BufferedWriter(new FileWriter(ttlFile,
    		    false));
    	    out.write(serializedOntology);
    	    out.flush();
    	    out.close();
    	    return true;
    	} catch (IOException e) {
    	    LogUtils.logError(mc, getClass(), "writeTTL", new String[] {"Unexpected Error"}, e);
    	    return false;
    	}
    }
    
    /**
     * Transform a given TTL file into an OWL file.
     * @param ttlFile the source file.
     * @param owlFile the target file
     * @return true iif the operation was successful.
     */
    private boolean transformTTL2OWL(File ttlFile, File owlFile){
    	LogUtils.logInfo(mc, getClass(), "transformTTL2OWL", "Transforming file: "
    			+ ttlFile.getAbsolutePath() + "\n\t to: " + owlFile.getAbsolutePath());
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    	IRI documentIRI = IRI.create(ttlFile);
    	OWLOntology owlOntology;
    	try {
    	    owlOntology = manager.loadOntologyFromOntologyDocument(documentIRI);
    	    LogUtils.logInfo(mc, getClass(), "transformTTL2OWL", "   Loaded ontology: " + owlOntology);

    	    OWLOntologyFormat format = manager.getOntologyFormat(owlOntology);

    	    RDFXMLOntologyFormat rdfxmlFormat = new RDFXMLOntologyFormat();
    	    if (format.isPrefixOWLOntologyFormat()) {
    		rdfxmlFormat.copyPrefixesFrom(format
    			.asPrefixOWLOntologyFormat());
    	    }
    	    manager.saveOntology(owlOntology, rdfxmlFormat, IRI.create(owlFile));
    	    LogUtils.logInfo(mc, getClass(), "transformTTL2OWL", "   Saved ontology " + owlOntology + " in file "
    		    + owlFile.getAbsolutePath());
    	} catch (OWLOntologyCreationException e1) {
    	    e1.printStackTrace();
    	    return false;
    	} catch (OWLOntologyStorageException e) {
    	    e.printStackTrace();
    	    return false;
    	}

    	return true;
    }
}
