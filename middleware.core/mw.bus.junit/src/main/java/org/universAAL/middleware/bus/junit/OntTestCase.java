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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
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
	
	private class LogEntry{
		int logLevel;
		String module; String pkg; String cls;
		String method; Object[] msgPart; Throwable t;

		public LogEntry(int logLevel, String module, String pkg, String cls,
				String method, Object[] msgPart, Throwable t) {
			super();
			this.logLevel = logLevel;
			this.module = module;
			this.pkg = pkg;
			this.cls = cls;
			this.method = method;
			this.msgPart = msgPart;
			this.t = t;
		}

	    /**
	     * Internal method to create a single String from a list of objects.
	     * 
	     * @param msgPart
	     *            The message of this log entry. All elements of this array are
	     *            converted to a string object and concatenated.
	     * @return The String.
	     */
	    private String buildMsg(Object[] msgPart) {
		StringBuffer sb = new StringBuffer(256);
		if (msgPart != null)
		    for (int i = 0; i < msgPart.length; i++)
			sb.append(msgPart[i]);
		return sb.toString();
	    }
		
		public String toString(){
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			switch (logLevel) {
			case LogListener.LOG_LEVEL_TRACE:
				sb.append("TRACE");
				break;
			case LogListener.LOG_LEVEL_DEBUG:
				sb.append("DEBUG");
				break;
			case LogListener.LOG_LEVEL_INFO:
				sb.append("INFO");
				break;
			case LogListener.LOG_LEVEL_WARN:
				sb.append("WARN");
				break;
			case LogListener.LOG_LEVEL_ERROR:
				sb.append("ERROR");
				break;
			}
			
			sb.append("] -> ");
			sb.append(buildMsg(msgPart));
			return sb.toString();
		}
	}
	
	private class OntologyLoaderTask implements LogListener{
		Ontology ont;
		int attempts = 0;
		int warnings = 0;
		int errors = 0;
		List<LogEntry> logEntries = new ArrayList<OntTestCase.LogEntry>();
		
		
		public OntologyLoaderTask(Ontology ont) {
			super();
			this.ont = ont;
		}


		String report(){
			if (warnings == 0 && errors == 0){
				return "";
			}
			String ret = "(";
			if (warnings < 0){
				ret += "warnings: " + Integer.toString(warnings) + " ";
			}
			if (errors > 0 ){
				ret += "errors: " + Integer.toString(errors) + " ";
			}
			ret += ")";
			return ret;
		}


		void attempt(){
			if (ont.getInfo() != null && OntologyManagement.getInstance().isRegisteredClass(ont.getInfo().getURI(), true))
				return;
			attempts++;
			
			mc.getContainer().shareObject(mc, OntologyLoaderTask.this, new String[] { LogListener.class.getName() });
			
			OntologyManagement.getInstance().register(mc, ont);
			
			mc.getContainer().removeSharedObject(mc, OntologyLoaderTask.this, new String[] { LogListener.class.getName() });
		}
		
		void unregister(){
			//reset statistics
			warnings = 0;
			errors = 0;
			logEntries.clear();
			OntologyManagement.getInstance().unregister(mc, ont);
		}
		
		public void log(int logLevel, String module, String pkg, String cls,
				String method, Object[] msgPart, Throwable t) {
			LogEntry le = new LogEntry(logLevel, module, pkg, cls, method, msgPart, t);
			logEntries.add(le);
			if (logLevel == LOG_LEVEL_ERROR)
				errors++;
			if (logLevel == LOG_LEVEL_WARN)
				warnings++;
		}


		public boolean allImportsRegistered() {
			Object imports = ont.getInfo().getProperty(Ontology.PROP_OWL_IMPORT);
			if (imports == null){
				return true;
			}
			if (! (imports instanceof List)){
				List a = new ArrayList();
				a.add(imports);
				imports = a;
			}
			String[] registeredA = OntologyManagement.getInstance().getOntoloyURIs();
			List registered = new ArrayList();
			for (int i = 0; i < registeredA.length; i++) {
				registered.add(new Resource(registeredA[i]));
			}
			((List)imports).removeAll(registered);
			return ((List)imports).isEmpty();
		}
	}
	

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
		List<Ontology> toBeLoaded = getOntologies();
		int totalOntologiesFound = toBeLoaded.size();
		Map<Ontology, OntologyLoaderTask> pendingOnts = new HashMap<Ontology, OntTestCase.OntologyLoaderTask>();
		List<OntologyLoaderTask> loadingOrder = new ArrayList<OntTestCase.OntologyLoaderTask>();
		for (Ontology ont : toBeLoaded) {
			pendingOnts.put(ont, new OntologyLoaderTask(ont));
		}
		
		while (!toBeLoaded.isEmpty() ) {
			Ontology next = toBeLoaded.remove(0);
			OntologyLoaderTask otl = pendingOnts.get(next);
			try{
				
				otl.attempt();
				if ((!otl.allImportsRegistered()
						|| otl.errors > 0)
						&& otl.attempts <= totalOntologiesFound*2){
					otl.unregister();
					toBeLoaded.add(next);
					continue;
				}
				loadingOrder.add(otl);
			} catch (Exception e){
				if(otl.attempts <= totalOntologiesFound*2) {
					otl.unregister();
					toBeLoaded.add(next);
					continue;
				}else {
					otl.log(LogListener.LOG_LEVEL_ERROR, mc.getID(), getClass().getPackage().toString(), getClass().toString(),
							"autoloadOntolgoies", 
							new String[] {"Recurrent Error, could not register ontology: " + next.getInfo().getURI()}, e);
					LogUtils.logError(mc, getClass(), "autoLoadOntologies", 
							new String[] {"Recurrent Error, could not register ontology: " + next.getInfo().getURI()}, e);
				}
			}
		}
		//Print Summary
		System.out.println("---------------------------------");
		System.out.println("AUTO LOAD RESULT");
		System.out.println("\t Load Order:");
		StringBuffer sb = new StringBuffer();
		sb.append("\t Problems found in this project:\n");
		boolean problems = false;
		for (OntologyLoaderTask olt : loadingOrder) {
			System.out.println("\t\t" + olt.ont.getInfo().getURI() + " " + olt.report());
			if (isInMyProy(olt.ont)){
				sb.append("\t\t"+ olt.ont.getInfo().getURI() + "\n" );
				if (olt.errors > 0 || olt.warnings > 0){
					problems = true;
					for (LogEntry le : olt.logEntries) {
						sb.append("\t\t\t" + le.toString() + "\n");
					}
				}
			}
		}
		if (problems){
			System.err.println(sb.toString());
		}
		System.out.println("---------------------------------");
	}

	/**
	 * Recovers the serializer. 
	 * @return the serializer.
	 */
	protected MessageContentSerializer getContentserializer() {
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
    private List<Ontology> getOntologies() {
    	List<Ontology> onts = new ArrayList<Ontology>();
		
		Reflections reflections = new Reflections("org.universAAL.ontology");

		try {
			Set<?> subTypes = reflections.getSubTypesOf(Class.forName(Ontology.class.getName()));
			
			for (Object o : subTypes) {
				if (o instanceof Class<?>){
					try {
						onts.add((Ontology) ((Class<?>)o).newInstance());
					} catch (Exception e) {
						LogUtils.logError(mc, getClass(), "getOntologies", new String[] {"could not instantiate: " + ((Class)o).getName()},e);
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
