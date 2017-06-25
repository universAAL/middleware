/*
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.owl.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.DatatypeProperty;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfo;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Property;
import org.universAAL.middleware.rdf.RDFClassInfo;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.rdf.TypeMapper;

public final class OntologyTest {

	private OntologyTest() {
	}

	/**
	 * Perform a sanity check of the given class that is defined in the given
	 * ontology. The check is done after registration of the ontology.
	 *
	 * @param ont
	 *            The ontology that defined the class.
	 * @param info
	 *            the ontology class.
	 */
	public static boolean postTestClass(Ontology ont, OntClassInfo info, HashMap<String, String> dbgClass) {
		ResourceFactory fact = info.getFactory();
		if (fact == null)
			// must be an abstract class
			return true;
		Resource testInstance = fact.createInstance(info.getURI(), Resource.NAMESPACE_PREFIX + "testInstance",
				info.getFactoryIndex());
		ManagedIndividual m = (ManagedIndividual) testInstance;

		// test the super class
		String[] superClasses = info.getNamedSuperClasses(true, true);
		if (superClasses.length == 0) {
			LogUtils.logError(SharedResources.moduleContext, OntologyTest.class,
					"postTestClass", new Object[] { "No super class: the ontology class ", info.getURI(),
							" of the ontology ", ont.getInfo().getURI(), " does not have a super class defined." },
					null);
			return false;
		}
		HashSet<String> supcls = new HashSet<String>();
		for (String tmp : superClasses)
			supcls.add(tmp);
		// if (!supcls.contains(ManagedIndividual.MY_URI)) {
		// LogUtils.logDebug(
		// SharedResources.moduleContext,
		// OntologyTest.class,
		// "testClass",
		// new Object[] {
		// "ManagedIndividual is not a super class: the ontology class ",
		// info.getURI(),
		// " of the ontology ",
		// ont.getInfo().getURI(),
		// " does not have ManagedIndividual defined as a super class.
		// ManagedIndividual, as super class of ontology classes "
		// +
		// "should either be a direct super class or a super class of one of the
		// super classes of this class."
		// },
		// null);
		// }
		// System.out.println("----------------- checking class: "
		// + m.getClass().getName());
		// for (Object o : superClasses) {
		// System.out.println(" -- registered super class: " + o);
		// }
		Class superClassJava = m.getClass().getSuperclass();
		while (Modifier.isAbstract(superClassJava.getModifiers())) {
			superClassJava = superClassJava.getSuperclass();
		}
		while (!(superClassJava.equals(Object.class)) && !(superClassJava.equals(ManagedIndividual.class))
				&& !(superClassJava.equals(FinalizedResource.class)) && !(superClassJava.equals(Resource.class))) {
			// System.out.println(" -- super: " + superClassJava.getName());
			String superClassURI = dbgClass.get(superClassJava.getName());

			if (superClassURI == null) {
				LogUtils.logWarn(SharedResources.moduleContext, OntologyTest.class, "postTestClass",
						new Object[] { "Unregistered super class: the ontology class ", info.getURI(),
								" of the ontology ", ont.getInfo().getURI(),
								" has a super class from Java inheritance (", superClassJava.getName(),
								") that is not registered as a class in Ontology Management (or the super class is a non-abstract class that is registered as an abstract ontology class)." },
						null);
			} else {
				if (!(supcls.contains(superClassURI))) {
					String s = "";
					for (String s2 : supcls) {
						s += s2 + "\n";
					}
					LogUtils.logWarn(SharedResources.moduleContext, OntologyTest.class, "postTestClass",
							new Object[] { "Undefined super class: the ontology class ", info.getURI(),
									" of the ontology ", ont.getInfo().getURI(),
									" has a super class from Java inheritance (", superClassJava.getName(),
									") that is registered in Ontology Management with the URI '", superClassURI,
									"', but this class is not defined as a super class of this class (neither directly nor indirectly). The defined super classes are:\n",
									s },
							null);
				}
			}
			do {
				superClassJava = superClassJava.getSuperclass();
			} while (Modifier.isAbstract(superClassJava.getModifiers()));
		}

		// test special case fixed for a service in service bus, so we need
		// to use concrete URIs here
		// Note, that it is not completely correct to do this here since DataRep
		// is a dependency of service bus and not the other way around,
		// therefore DataRep should be independent from the service bus.
		// However, this test may help to find some serious problems during
		// serialization!
		if (supcls.contains("http://www.daml.org/services/owl-s/1.1/Service.owl#Service")) {
			// we have a subclass of Service
			// test the serialization type for some of the properties

			String[] propURIs = { "http://www.daml.org/services/owl-s/1.1/Service.owl#presents",
					Resource.VOCABULARY_NAMESPACE + "instanceLevelRestrictions",
					Resource.VOCABULARY_NAMESPACE + "numberOfValueRestrictions" };
			for (String propURI : propURIs) {
				if (m.getPropSerializationType(propURI) != Resource.PROP_SERIALIZATION_FULL) {
					LogUtils.logWarn(SharedResources.moduleContext, OntologyTest.class, "postTestClass",
							new Object[] { "Wrong serialization type: the property ", propURI,
									" of the ontology class ", info.getURI(), " of the ontology ",
									ont.getInfo().getURI(),
									" does not return Resource.PROP_SERIALIZATION_FULL as serialization type for a property used by the service bus."
											+ " Please check the method getPropSerializationType(String propURI) of that class;"
											+ " this might cause an incomplete serialization result." },
							null);
				}
			}
		}

		return true;
	}

	/**
	 * Perform a sanity check of the given class that is defined in the given
	 * ontology. The check is done during registration of the ontology.
	 *
	 * @param ont
	 *            The ontology that defined the class.
	 * @param info
	 *            the ontology class.
	 */
	public static boolean testClass(Ontology ont, OntClassInfo info, HashMap<String, String> dbgClass) {
		try {
			if (!Resource.isQualifiedName(info.getURI())) {
				LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Unqualified URI: the ontology class ", info.getURI(), " of the ontology ",
								ont.getInfo().getURI(),
								" does not have a qualified URI. Please check the URI you give as parameter to creator methods like createNewOntClassInfo()." },
						null);
				return false;
			}

			String className = info.getLocalName();
			if (className != null) {
				if (!className.substring(0, 1).matches("[A-Z]")) {
					LogUtils.logInfo(SharedResources.moduleContext, OntologyTest.class, "testClass",
							new Object[] { "Class not starting with upper-case letter: the ontology class ",
									info.getURI(), " of the ontology ", ont.getInfo().getURI(),
									" starts with a lower-case letter. It is recommended that classes start with an upper-case letter." },
							null);
				}
			}

			if (!checkUniversAALLetterCase(info.getURI())) {
				LogUtils.logInfo(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Class URI contains \"universAAL\" with wrong letter case: the ontology class ",
								info.getURI(), " of the ontology ", ont.getInfo().getURI(),
								" contains the string \"universAAL\" with wrong letter case." },
						null);
			}

			if (!checkUniversAALSubdomain(info.getURI())) {
				LogUtils.logInfo(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Class URI contains \"universAAL\" but no \"ontology\": the ontology class ",
								info.getURI(), " of the ontology ", ont.getInfo().getURI(),
								" contains the string \"universAAL\" but not \"ontology\". universAAL URIs should start with \"http://ontology.universAAL.org\"" },
						null);
			}

			if (info.isAbstract())
				return true;

			ResourceFactory fact = info.getFactory();
			if (fact == null) {
				LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Missing factory: the ontology class ", info.getURI(), " of the ontology ",
								ont.getInfo().getURI(),
								" is not an abstract class but it does not define a factory to create instances of this class." },
						null);
				return false;
			}

			Resource testInstance = fact.createInstance(info.getURI(), Resource.NAMESPACE_PREFIX + "testInstance",
					info.getFactoryIndex());
			if (testInstance == null) {
				LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Missing factory result: the ontology class ", info.getURI(),
								" (factory index: " + info.getFactoryIndex() + ") of the ontology ",
								ont.getInfo().getURI(),
								" is not an abstract class and it defines a factory, but the factory does not create instances for this class (the factory returned null)." },
						null);
				return false;
			}

			if (!(testInstance instanceof ManagedIndividual)) {
				LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Factory returned non-ManagedIndividual: the ontology class ", info.getURI(),
								" of the ontology ", ont.getInfo().getURI(),
								" is registered as an ontology class (OWL), but the factory does not return a subclass of ManagedIndividual."
										+ " All OWL classes must be a subclass of ManagedIndividual." },
						null);
				return false;
			}

			ManagedIndividual m = (ManagedIndividual) testInstance;
			if (!info.getURI().equals(m.getClassURI())) {
				LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
						new Object[] { "Wrong class URI: the ontology class ", info.getURI(), " of the ontology ",
								ont.getInfo().getURI(),
								" does not return the URI that was used for registration."
										+ " Please check that the method \"getClassURI()\" is overwritten and matches the URI you specify as parameter to creator methods like createNewOntClassInfo()."
										+ " The factory could also be the source of this error: please check that the factoryIndex is correct." },
						null);
				return false;
			}

			// we store the Java .class for later testing
			dbgClass.put(m.getClass().getName(), m.getClassURI());

			// test properties
			String[] props = info.getDeclaredPropertyURIs();
			for (int i = 0; i < props.length; i++) {
				String propURI = props[i];

				// test that the URI starts with a lower-case letter
				String propURILocal = new Resource(propURI).getLocalName();
				if (propURILocal != null) {
					if (!propURILocal.substring(0, 1).matches("[a-z]")) {
						LogUtils.logInfo(SharedResources.moduleContext, OntologyTest.class, "testClass",
								new Object[] { "Property not starting with lower-case letter: the property ", propURI,
										" of the ontology ", ont.getInfo().getURI(),
										" does not start with a lower-case letter. It is recommended that properties start with a lower-case letter." },
								null);
					}
				}

				// test for universAAL URI letter case
				if (!checkUniversAALLetterCase(propURI)) {
					LogUtils.logInfo(SharedResources.moduleContext, OntologyTest.class, "testClass",
							new Object[] { "Property URI contains \"universAAL\" with wrong letter case: the property ",
									propURI, " of the ontology ", ont.getInfo().getURI(),
									" contains the string \"universAAL\" with wrong letter case." },
							null);
				}

				// test for "ontology" before "universAAL" in URI
				if (!checkUniversAALSubdomain(propURI)) {
					LogUtils.logInfo(SharedResources.moduleContext, OntologyTest.class, "testClass",
							new Object[] { "Property URI contains \"universAAL\" but no \"ontology\": the property ",
									propURI, " of the ontology ", ont.getInfo().getURI(),
									" contains the string \"universAAL\" but not \"ontology\". universAAL URIs should start with \"http://ontology.universAAL.org\"" },
							null);
				}

				// test that the property has a serialization type
				int serType = m.getPropSerializationType(propURI);
				if (serType == Resource.PROP_SERIALIZATION_UNDEFINED)
					LogUtils.logWarn(SharedResources.moduleContext, OntologyTest.class, "testClass",
							new Object[] { "Undefined serialization type: the property ", propURI,
									" of the ontology class ", info.getURI(), " of the ontology ",
									ont.getInfo().getURI(),
									" returns <undefined> as serialization type for a declared property."
											+ " Is this intended? If not,"
											+ " please check the method getPropSerializationType(String propURI);"
											+ " this might cause an incomplete serialization result." },
							null);

				// test that the restrictions match the type (i.e. a
				// DatatypeProperty has a literal as value, not a Resource)
				MergedRestriction res = info.getRestrictionsOnProp(propURI);
				if (res == null)
					continue;
				Property prop = info.getDeclaredProperty(propURI);
				boolean isDatatype = prop instanceof DatatypeProperty;
				Object constraint;
				constraint = res.getConstraint(MergedRestriction.allValuesFromID);
				if (constraint != null) {
					if (constraint instanceof TypeURI) {
						TypeURI t = (TypeURI) constraint;
						String err = null;
						if (TypeMapper.isRegisteredDatatypeURI(t.getURI())) {
							if (!isDatatype) {
								err = "ObjectProperty";
							}
						} else {
							if (isDatatype) {
								err = "DatatypeProperty";
							}
						}
						if (err != null) {
							LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
									new Object[] { "Wrong property definition: the property ", propURI,
											" of the ontology class ", info.getURI(), " of the ontology ",
											ont.getInfo().getURI(), " is registered as a " + err
													+ " but the defined restrictions indicate otherwise." },
									null);
						}
					}
				}
				// TODO: there is much more we can test!
			}

			return true;
		} catch (Exception e) {
			LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testClass",
					new Object[] { "An unknown exception occured while testing the ontology class ", info.getURI(),
							" during registration of the ontology ", ont.getInfo().getURI(), "." },
					e);
		}
		return false;
	}

	/**
	 * Tests if a URI that contains the string ".universAAL." is preceded with
	 * the subdomain "ontology".
	 *
	 * @param uri
	 *            the URI to test
	 * @return false, if ".universAAL." is not preceded with "ontology".
	 */
	private static boolean checkUniversAALSubdomain(String uri) {
		String low = uri.toLowerCase();
		int pos = low.indexOf(".universaal.");
		if (pos == -1)
			return true;
		int pos2 = uri.indexOf("ontology.");
		if (pos2 == -1)
			return false;
		if (pos2 > pos)
			return false;
		return true;
	}

	/**
	 * Tests if a URI that contains the string ".universAAL." has it with this
	 * letter case.
	 *
	 * @param uri
	 *            the URI to test
	 * @return false, if ".universAAL." is contained with a wrong letter case.
	 */
	private static boolean checkUniversAALLetterCase(String uri) {
		String low = uri.toLowerCase();
		if (!low.contains(".universaal."))
			return true;
		if (!uri.contains(".universAAL."))
			return false;
		return true;
	}

	/**
	 * Perform a sanity check of the given ontology. The check is done during
	 * registration of the ontology.
	 *
	 * @param ont
	 *            The ontology that defined the class.
	 */
	public static boolean testOntology(Ontology ont) {
		try {
			boolean retVal = true;

			Resource[] rsc = ont.getResourceList();
			Resource r;
			// maps factoryIndex to RDFClassInfo
			HashMap factories = new HashMap();

			for (int i = 0; i < rsc.length; i++) {
				r = rsc[i];
				if (r instanceof RDFClassInfo) {
					RDFClassInfo rci = (RDFClassInfo) r;
					if (rci.isAbstract())
						continue;

					Integer factoryIndex = Integer.valueOf(rci.getFactoryIndex());

					// test factory
					Object o = factories.get(factoryIndex);
					if (o != null) {
						LogUtils.logWarn(SharedResources.moduleContext, OntologyTest.class, "testOntology",
								new Object[] { "Duplicate factory index: the classes ", rci.getURI(), " and ",
										((RDFClassInfo) o).getURI(), " of the ontology ", ont.getInfo().getURI(),
										" have the same factory index. Is this intended?" },
								null);
					} else {
						factories.put(factoryIndex, rci);
					}
				}
			}

			return retVal;
		} catch (Exception e) {
			LogUtils.logError(SharedResources.moduleContext, OntologyTest.class, "testOntology",
					new Object[] { "An unknown exception occured while testing the ontology ", ont.getInfo().getURI(),
							" during registration." },
					e);
		}
		return false;
	}
}
