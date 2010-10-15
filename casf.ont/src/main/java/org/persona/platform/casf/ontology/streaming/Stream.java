/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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

package org.persona.platform.casf.ontology.streaming;

import org.persona.ontology.ManagedIndividual;
import org.persona.ontology.expr.Restriction;

/**
 * 
 * @author climberg
 *
 */
public abstract class Stream extends ManagedIndividual{
	
	public static final String STREAM_NAMESPACE = "http://ontology.persona.ima.igd.fhg.de/Stream.owl#";
	public static final String MY_URI;
	public static final String PROP_HAS_FORMAT;
	public static final String PROP_HAS_ENDPOINT;
	
	static{
		MY_URI = Stream.STREAM_NAMESPACE + "Stream";
		PROP_HAS_FORMAT = Stream.STREAM_NAMESPACE + "hasFormat";
		PROP_HAS_ENDPOINT = Stream.STREAM_NAMESPACE + "hasEndPoint";
		register(Stream.class);
	}
	
	public static Restriction getClassRestrictionsOnProperty(String propURI){
		if (PROP_HAS_FORMAT.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(propURI, Format.MY_URI, 1, 1);
		if (PROP_HAS_ENDPOINT.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(propURI,
							EndPoint.MY_URI, 1, 1);
		return ManagedIndividual.getClassRestrictionsOnProperty(propURI);
	}
	
	public static String[] getStandardPropertyURIs() {
		String[] inherited = ManagedIndividual.getStandardPropertyURIs();
		String[] toReturn = new String[inherited.length+2];
		int i = 0;
		while (i < inherited.length) {
			toReturn[i] = inherited[i];
			i++;
		}
	
		toReturn[i++] = PROP_HAS_FORMAT;
		toReturn[i]   = PROP_HAS_ENDPOINT;
		return toReturn;
	}
	
	public static String getRDFSComment() {
		return "The class of Streams.";
	}
	
	public static String getRDFSLabel() {
		return "Stream";
	}
	
	/**
	 * default constructor
	 */
	public Stream() {
		super();
	}
	
	public Stream(String uri) {
		super(uri);
	}
	
	public int getPropSerializationType(String propURI){
			return PROP_SERIALIZATION_FULL;
	}
	
	//removed soon
	public boolean isWellFormed() {
		return true;
	}
	
}
