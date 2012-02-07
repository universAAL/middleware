package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.ResourceFactory;

public class SimpleOntology extends Ontology {

    /**
     * Create a simple ontology with only one class and without properties and
     * factory.
     * 
     * @param classURI
     * @param superClassURI
     */
    public SimpleOntology(String classURI, String superClassURI) {
	super(classURI.substring(0, classURI.indexOf('#') + 1));
	OntClassInfoSetup info = createNewAbstractOntClassInfo(classURI);
	info.addSuperClass(superClassURI);
    }

    public SimpleOntology(String classURI, String superClassURI,
	    ResourceFactory factory) {
	super(classURI.substring(0, classURI.indexOf('#') + 1));
	OntClassInfoSetup info = createNewOntClassInfo(classURI, factory);
	info.addSuperClass(superClassURI);
    }

    public void create() {
    }
}
