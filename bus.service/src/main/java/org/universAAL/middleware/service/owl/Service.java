/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.service.owl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.service.PropertyPath;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * The root of the hierarchy of service classes in universAAL.
 * <p>All subclasses must follow the conventions declared by {@link ManagedIndividual}.
 * <p>The main characteristic of services is that they specify their view on their domain by restricting
 * some of the relevant properties from the domain ontology that are reachable from the service class using
 * a sequence of properties. The universAAL middleware calls such sequences a {@link
 * org.universAAL.middleware.service.PropertyPath}. Subclasses must define a static repository for such
 * restrictions as an empty instance of {@link java.util.Hashtable} and add their class-level restrictions to this
 * repository in the same static code segment, where they call {@link #register(Class)}, using the help
 * method {@link #addRestriction(Restriction, String[], java.util.Hashtable)}.
 * <p>In addition to class-level restrictions, concrete instances can add instance-level restrictions using
 * references to their input parameters (see {@link
 * org.universAAL.middleware.service.owls.process.ProcessInput#asVariableReference()}). The help method {@link
 * #addInstanceLevelRestriction(Restriction, String[])} facilitates the addition of such instance-level restrictions.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public abstract class Service extends ManagedIndividual {
	
	public static final String OWLS_NAMESPACE_PREFIX = "http://www.daml.org/services/owl-s/1.1/";
	
	public static final String OWLS_SERVICE_NAMESPACE = OWLS_NAMESPACE_PREFIX + "Service.owl#";
	public static final String PROP_INSTANCE_LEVEL_RESTRICTIONS =
		uAAL_VOCABULARY_NAMESPACE+"instanceLevelRestrictions";
	public static final String PROP_NUMBER_OF_VALUE_RESTRICTIONS =
		uAAL_VOCABULARY_NAMESPACE+"numberOfValueRestrictions";
	
	/**
	 * The OWL-S property http://www.daml.org/services/owl-s/1.1/Service.owl#presents
	 */
	public static final String PROP_OWLS_PRESENTS;
	
	/**
	 * The OWL-S property http://www.daml.org/services/owl-s/1.1/Service.owl#presentedBy
	 */
	public static final String PROP_OWLS_PRESENTED_BY;
	
	public static final String MY_URI;
	static {
		PROP_OWLS_PRESENTS = OWLS_SERVICE_NAMESPACE + "presents";
		PROP_OWLS_PRESENTED_BY = OWLS_SERVICE_NAMESPACE + "presentedBy";
		MY_URI = OWLS_SERVICE_NAMESPACE + "Service";
		register(Service.class);
	}
	
	/**
	 * A help method for subclasses to manage their restrictions on properties (from the domain
	 * ontology) that are reachable from the subclass, provided that they have a static Hashtable
	 * for gathering them.
	 * @param r the restriction to be added on the last element of the path given by 'toPath'.
	 * @param toPath the path to which the given restriction must be bound. It must start with a
	 *               property from the service class and address a reachable property from
	 *               the domain ontology; the last element of the path must be equal to
	 *               <code>r.getOnProperty()</code>. 
	 * @param restrictions a class-level static hash-table for managing restrictions
	 * @return true, if all constraints held and the restriction could be added; otherwise, false.
	 */
	protected static final boolean addRestriction(Restriction r, String[] toPath, Hashtable restrictions) {
		if (toPath == null  ||  toPath.length == 0  ||  restrictions == null  ||  r == null)
			return false;
		Restriction root = (Restriction) restrictions.get(toPath[0]),
		            tmp = r.appendTo(root, toPath);
		if (tmp == null)
			return false;
		if (root == null)
			restrictions.put(toPath[0], tmp);
		return true;
	}
	
	/**
	 * A restriction previously added by {@link #addRestriction(Restriction, String[], Hashtable)} to
	 * the given <code>propPath</code> using the same hash-table of <code>restrictions</code> will be
	 * returned by this method.
	 */
	protected static final Restriction getRestrictionOnPropPath(Hashtable restrictions, String[] propPath) {
		if (propPath == null  ||  propPath.length == 0  ||  restrictions == null)
			return null;
		return Restriction.getRestrictionOnPath((Restriction) restrictions.get(propPath[0]), propPath);
	}

	/**
	 * Returns the value of the property <code>rdfs:comment</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSComment() {
		return "The root of the hierarchy of service classes in universAAL.";
	}

	/**
	 * Returns the value of the property <code>rdfs:label</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSLabel() {
		return "universAAL Service";
	}
	
	protected Service() {
		super();
	}
	
	protected Service(String uri) {
		super(uri);
		if (uri != null) {
			myProfile = new ServiceProfile(this, uri+"Process");
			props.put(PROP_OWLS_PRESENTS, myProfile);
		}
	}
	
	/**
	 * The instance-level repository of defined restrictions on property paths. For adding instance-level
	 * restrictions to this repository, the method {@link #addRestriction(Restriction, String[], java.util.Hashtable)} must be used.
	 */
	protected final Hashtable instanceLevelRestrictions = new Hashtable();
	protected int numberOfValueRestrictions = 0;
	protected ServiceProfile myProfile;
	
	/**
	 * A method for adding instance-level restrictions.
	 * 
	 * @see #instanceLevelRestrictions
	 * @see #addRestriction(Restriction, String[], Hashtable)
	 */
	public final boolean addInstanceLevelRestriction(Restriction r, String[] toPath) {
		if (addRestriction(r, toPath, instanceLevelRestrictions)) {
			if (r.getProperty(Restriction.PROP_OWL_HAS_VALUE) != null)
				props.put(PROP_NUMBER_OF_VALUE_RESTRICTIONS, new Integer(++numberOfValueRestrictions));
			props.put(PROP_INSTANCE_LEVEL_RESTRICTIONS, new ArrayList(instanceLevelRestrictions.values()));
			return true;
		}
		return false;
	}
	
	/**
	 * Must return a hash-table defined statically by the subclass. Each subclass must have its
	 * own static repository of restrictions as hash-table. 
	 * 
	 * @see {@link #instanceLevelRestrictions}, which is a similar repository but at instance level
	 */
	protected abstract Hashtable getClassLevelRestrictions();

	public final Restriction getClassLevelRestrictionOnProp(String propURI) {
		return (Restriction) getClassLevelRestrictions().get(propURI);
	}
	
	public final Object getInstanceLevelFixedValueOnProp(String propURI) {
		if (propURI == null)
			return null;
		Restriction r = (Restriction) instanceLevelRestrictions.get(propURI);
		return (r == null)? null
				: r.getProperty(Restriction.PROP_OWL_HAS_VALUE);
	}
	
	/**
	 * Returns the restriction on the given <code>propPeth</code>, if it was previously added
	 * to {@link #instanceLevelRestrictions} using {@link #addRestriction(Restriction, String[], java.util.Hashtable)}.
	 */
	public final Restriction getInstanceLevelRestrictionOnProp(String propURI) {
		return (Restriction) instanceLevelRestrictions.get(propURI);
	}
	
	public final int getNumberOfValueRestrictions() {
		return numberOfValueRestrictions;
	}
	
	public final ServiceProfile getProfile() {
		return myProfile;
	}
	
	protected ProcessInput createInput(String inParamURI, String typeURI, int minCardinality, int maxCardinality) {
		ProcessInput in = new ProcessInput(inParamURI);
		in.setParameterType(typeURI);
		in.setCardinality(maxCardinality, minCardinality);
		myProfile.addInput(in);
		return in;
	}
	
	protected void addFilteringInput(String inParamURI, String typeURI, int minCardinality, int maxCardinality, String[] propPath) {
		ProcessInput in = createInput(inParamURI, typeURI, minCardinality, maxCardinality);
		addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(
						propPath[propPath.length-1],
						in.asVariableReference()),
				propPath);
	}
	
	protected void addInputWithAddEffect(String inParamURI, String typeURI, int minCardinality, int maxCardinality, String[] propPath) {
		ProcessInput in = createInput(inParamURI, typeURI, minCardinality, maxCardinality);
		myProfile.addAddEffect(
				new PropertyPath(null, true, propPath), 
				in.asVariableReference());
	}
	
	protected void addInputWithChangeEffect(String inParamURI, String typeURI, int minCardinality, int maxCardinality, String[] propPath) {
		ProcessInput in = createInput(inParamURI, typeURI, minCardinality, maxCardinality);
		myProfile.addChangeEffect(
				new PropertyPath(null, true, propPath), 
				in.asVariableReference());
	}
	
	protected void addInputWithRemoveEffect(String inParamURI, String typeURI, int minCardinality, int maxCardinality, String[] propPath) {
		addFilteringInput(inParamURI, typeURI, minCardinality, maxCardinality, propPath);
		myProfile.addRemoveEffect(new PropertyPath(null, true, propPath));
	}
	
	protected void addOutput(String outParamURI, String typeURI, int minCardinality, int maxCardinality, String[] propPath) {
		ProcessOutput out = new ProcessOutput(outParamURI);
		out.setParameterType(typeURI);
		out.setCardinality(maxCardinality, minCardinality);
		myProfile.addOutput(out);
		myProfile.addSimpleOutputBinding(out,
				new PropertyPath(null, true, propPath));
	}
	
	/**
	 * Using the repository of class-level restrictions returned by {@link
	 * #getClassLevelRestrictions()}, it returns the set of properties that are restricted
	 * at class level.
	 */
	public final String[] getRestrictedPropsOnClassLevel() {
		Hashtable ht = getClassLevelRestrictions();
		return (String[]) ht.keySet().toArray(new String[ht.size()]);
	}
	
	/**
	 * @see ManagedIndividual#getPropSerializationType(java.lang.String)
	 */
	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_FULL;
	}
	
	/**
	 * Returns the set of properties restricted at instance level.
	 */
	public final String[] getRestrictedPropsOnInstanceLevel() {
		return (String[]) instanceLevelRestrictions.keySet().toArray(
				new String[instanceLevelRestrictions.size()]);
	}

	public void setProperty(String propURI, Object value) {
		if (PROP_OWLS_PRESENTS.equals(propURI)
				&&  value instanceof ServiceProfile
				&&  (myProfile == null
						||  myProfile.isEmpty())) {
			myProfile = (ServiceProfile) value;
			props.put(PROP_OWLS_PRESENTS, myProfile);
		} else if (PROP_NUMBER_OF_VALUE_RESTRICTIONS.equals(propURI)
				&&  value instanceof Integer
				&&  numberOfValueRestrictions == 0) {
			numberOfValueRestrictions = ((Integer) value).intValue();
			props.put(PROP_NUMBER_OF_VALUE_RESTRICTIONS, new Integer(numberOfValueRestrictions));
		} else if (PROP_INSTANCE_LEVEL_RESTRICTIONS.equals(propURI)
				&&  value != null
				&&  !props.containsKey(PROP_INSTANCE_LEVEL_RESTRICTIONS)) {
			if (value instanceof List)
				for (Iterator i = ((List) value).iterator(); i.hasNext();) {
					Object o = i.next();
					if (o instanceof Restriction)
						instanceLevelRestrictions.put(((Restriction) o).getOnProperty(), o);
					else
						return;
				}
			else if (value instanceof Restriction) {
				instanceLevelRestrictions.put(((Restriction) value).getOnProperty(), value);
				List aux = new ArrayList(1);
				aux.add(value);
				value = aux;
			} else
				return;
			props.put(propURI, value);
		} else
			super.setProperty(propURI, value);
	}
}
