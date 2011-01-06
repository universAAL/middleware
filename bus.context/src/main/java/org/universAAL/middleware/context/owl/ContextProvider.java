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
package org.universAAL.middleware.context.owl;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.Restriction;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ContextProvider extends ManagedIndividual {
	public static final String MY_URI;
	
	/**
	 * The type of a context provider. The range is {@link ContextProviderType}.
	 */
	public static final String PROP_CONTEXT_PROVIDER_TYPE;
	
	/**
	 * The underlying device(s) used by a context provider to determine the state of the provided
	 * context elements. The range is {@link org.universAAL.middleware.context.owl.ManagedIndividual}.
	 */
	public static final String PROP_CONTEXT_SOURCE;
	
	static {
		MY_URI = ContextEvent.uAAL_CONTEXT_NAMESPACE + "ContextProvider";
		PROP_CONTEXT_PROVIDER_TYPE = ContextEvent.uAAL_CONTEXT_NAMESPACE + "hasType";
		PROP_CONTEXT_SOURCE = ContextEvent.uAAL_CONTEXT_NAMESPACE + "hasSource";
		register(ContextProvider.class);
	}
	
	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		if (PROP_CONTEXT_PROVIDER_TYPE.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(propURI,
					ContextProviderType.MY_URI, 1, 1);
		if (PROP_CONTEXT_SOURCE.equals(propURI))
			return Restriction.getAllValuesRestriction(propURI,
					ManagedIndividual.MY_URI);
		return ManagedIndividual.getClassRestrictionsOnProperty(propURI);
	}
	
	public static String[] getStandardPropertyURIs() {
		return new String[] {
				PROP_CONTEXT_PROVIDER_TYPE,
				PROP_CONTEXT_SOURCE
		};
	}

	public static String getRDFSComment() {
		return "Represents the set of components that may publish context events.";
	}

	public static String getRDFSLabel() {
		return "Context Provider";
	}
	
	public ContextProvider() {
		super();
	}
	
	public ContextProvider(String uri) {
		super(uri);
	}
	
	protected ContextProvider(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}
	
	/**
	 * Returns the list of devices used by this context provider.
	 */
	public ManagedIndividual[] getContextSources() {
		List l = (List) getProperty(PROP_CONTEXT_SOURCE);
		return (l == null)? null : (ManagedIndividual[]) l.toArray(new ManagedIndividual[l.size()]);
	}

	/**
	 * The type of a context provider should be of minor interest, whereas the source could be
	 * important; but even the latter can be represented in its reduced form.
	 * 
	 * @see ManagedIndividual#getPropSerializationType(String)
	 */
	public int getPropSerializationType(String propURI) {
		if (PROP_CONTEXT_SOURCE.equals(propURI)
				|| PROP_CONTEXT_PROVIDER_TYPE.equals(propURI))
			return PROP_SERIALIZATION_REDUCED;
		return PROP_SERIALIZATION_OPTIONAL;
	}
	
	public ContextProviderType getProviderType() {
		return (ContextProviderType) props.get(PROP_CONTEXT_PROVIDER_TYPE);
	}

	/**
	 * @see ManagedIndividual#isWellFormed()
	 */
	public boolean isWellFormed() {
		return true;
	}
	
	public void setContextSources(ManagedIndividual[] devices) {
		if (devices != null  &&  devices.length > 0
				&&  !props.containsKey(PROP_CONTEXT_SOURCE)) {
			List l = new ArrayList(devices.length);
			for (int i = 0;  i < devices.length;  i++)
				l.add(devices[i]);
			props.put(PROP_CONTEXT_SOURCE, l);
		}
	}

	/**
	 * @see org.universAAL.middleware.PResource#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String propURI, Object value) {
		if (PROP_CONTEXT_SOURCE.equals(propURI)  &&  value instanceof ManagedIndividual[])
			setContextSources((ManagedIndividual[]) value);
		else if (PROP_CONTEXT_PROVIDER_TYPE.equals(propURI)
				&&  value instanceof ContextProviderType)
			setType((ContextProviderType) value);
	}

	public void setType(ContextProviderType type) {
		if (type != null  &&  !props.containsKey(PROP_CONTEXT_PROVIDER_TYPE))
			props.put(PROP_CONTEXT_PROVIDER_TYPE, type);
	}
}
