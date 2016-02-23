package org.universAAL.middleware.owl.ont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.IntRestriction;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.generic.GenericManagedIndividual;
import org.universAAL.middleware.owl.generic.GenericOntology;
import org.universAAL.middleware.owl.generic.GenericResourceFactory;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;

import junit.framework.TestCase;

public class GenericTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
	OntologyManagement.getInstance().register(
		SharedResources.moduleContext, new DataRepOntology());
    }

    public void test() {
	// new GenericManagedIndividual("classURI", "instanceURI");

	SharedResources.moduleContext = new JUnitModuleContext();
	ModuleContext mc = SharedResources.moduleContext;

	final class LightingOntology extends Ontology {
	    public static final String ns = "http://ontology.universaal.org/Lighting.owl#";
	    private ResourceFactory factory = new GenericResourceFactory();

	    public LightingOntology() {
		super(ns);
	    }

	    public void create() {
		Resource r = getInfo();
		r.setResourceComment("The ontology defining the most general concepts dealing with light sources and their control.");
		r.setResourceLabel("Lighting");
		addImport(DataRepOntology.NAMESPACE);
		addImport("http://ontology.universAAL.org/ServiceBus.owl");
		addImport("http://ontology.universAAL.org/Location.owl");

		OntClassInfoSetup oci;

		// load LightType
		oci = createNewAbstractOntClassInfo(ns + "LightTypeMY_URI");
		oci.addSuperClass(ManagedIndividual.MY_URI);
		oci.setResourceComment("The type of a light source");
		oci.setResourceLabel("Light Type");

		// load NaturalLight
		oci = createNewAbstractOntClassInfo(ns + "NaturalLightMY_URI");
		oci.setResourceComment("The type of natural light sources");
		oci.setResourceLabel("Natural Light");
		oci.addSuperClass(ns + "LightTypeMY_URI");
		oci.toEnumeration(new ManagedIndividual[] {
			new GenericManagedIndividual(ns + "NaturalLightMY_URI",
				"moonShine"),
			new GenericManagedIndividual(ns + "NaturalLightMY_URI",
				"sunShine"), });

		// load LightSource
		oci = createNewOntClassInfo(ns + "LightSourceMY_URI", factory,
			0);
		oci.setResourceComment("The class of all light sources");
		oci.setResourceLabel("Light Source");
		oci.addSuperClass(ns + "DeviceMY_URI");
		oci.addObjectProperty("LightSource.PROP_HAS_TYPE")
			.setFunctional();
		oci.addDatatypeProperty("LightSource.PROP_SOURCE_BRIGHTNESS")
			.setFunctional();
		oci.addObjectProperty("LightSource.PROP_SOURCE_COLOR")
			.setFunctional();
		oci.addRestriction(MergedRestriction
			.getAllValuesRestrictionWithCardinality(
				"LightSource.PROP_HAS_TYPE", ns
					+ "LightTypeMY_URI", 1, 1));
		oci.addRestriction(MergedRestriction
			.getAllValuesRestrictionWithCardinality(
				"LightSource.PROP_SOURCE_BRIGHTNESS",
				new IntRestriction(new Integer(0), true,
					new Integer(100), true), 1, 1));
		oci.addRestriction(MergedRestriction.getCardinalityRestriction(
			"LightSource.PROP_SOURCE_COLOR", 0, 1));
		oci.addInstance(new GenericManagedIndividual(ns
			+ "LightSourceMY_URI", "myLightSource"));
	    }
	}

	// register the ontology, get the resources, and unregister
	LightingOntology ont1 = new LightingOntology();
	OntologyManagement.getInstance().register(mc, ont1);
	Resource[] arr1 = ont1.getResourceList();
	List lst1 = new ArrayList();
	Collections.addAll(lst1, arr1);
	Resource r1 = new Resource();
	r1.setProperty("p", lst1);
	// System.out.println(r1.toStringRecursive());
	OntologyManagement.getInstance().unregister(mc, ont1);

	// now register the generic ontology with the resources we got from the
	// ontology
	GenericOntology ont2 = new GenericOntology(LightingOntology.ns, lst1);
	OntologyManagement.getInstance().register(mc, ont2);
	Resource[] arr2 = ont2.getResourceList();
	List lst2 = new ArrayList();
	Collections.addAll(lst2, arr2);
	Resource r2 = new Resource();
	r2.setProperty("p", lst2);
	// System.out.println(r2.toStringRecursive());
	OntologyManagement.getInstance().unregister(mc, ont2);

	Iterator<GraphIteratorElement> it;

	// the detailed test: test all triple values
	HashSet<String> triples = new HashSet<String>();
	it = GraphIterator.getIterator(r2);
	while (it.hasNext()) {
	    GraphIteratorElement el = it.next();
	    String s = getTriple(el);
	    // System.out.println(s);
	    // assertFalse(triples.contains(s));
	    triples.add(s);
	    //System.out.println(s);
	}
	//System.out.println("\n\n");
	it = GraphIterator.getIterator(r1);
	while (it.hasNext()) {
	    GraphIteratorElement el = it.next();
	    String s = getTriple(el);
	    if (!triples.contains(s))
		System.out.println("ERROR: triple not available: " + s);
	    assertTrue(triples.contains(s));
	    // triples.remove(s);
	    //System.out.println(s);
	}
	// assertTrue(triples.size() == 0);

	// test that the number of triples is the same in both ontology
	// representations
	int i1 = 0;
	it = GraphIterator.getIterator(r1);
	while (it.next() != null) {
	    i1++;
	}
	int i2 = 0;
	it = GraphIterator.getIterator(r2);
	while (it.next() != null) {
	    i2++;
	}
	if (i1 != i2)
	    System.out.println("ERROR: number of triples do not match: " + i1 + " " + i2);
	assertTrue(i1 == i2);
    }

    private String getTriple(GraphIteratorElement el) {
	String s = "";

	if (el.getSubject().isAnon())
	    s += "Anon ";
	else
	    s += el.getSubject().getURI() + " ";

	s += el.getPredicate() + " ";

	if (el.getObject() instanceof Resource
		&& ((Resource) el.getObject()).isAnon())
	    s += "Anon";
	else
	    s += el.getObject().toString();
	return s;
    }
}
