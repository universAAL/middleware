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
package org.universAAL.middleware.service.owls.profile;


import org.universAAL.middleware.owl.supply.Rating;

/**
 * The superclass for profile parameters that deal with rating the quality of services,
 * hence the restriction for the property 'valueData' is not supported by this class but 
 * 'sParameter' will be mandatory accepting a single {@link Rating}.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 */
public class QoSRating extends ProfileParameter {
	public static final String MY_URI = uAAL_SERVICE_NAMESPACE + "QoSRating";

	public QoSRating(Rating r) {
		super();
		addType(MY_URI, true);
		if (r != null)
			props.put(PROP_OWLS_PROFILE_S_PARAMETER, r);
	}

	public QoSRating(String uri, Rating r) {
		super(uri);
		addType(MY_URI, true);
		if (r != null)
			props.put(PROP_OWLS_PROFILE_S_PARAMETER, r);
	}
	
	/**
	 * Returns the rating of the property with URI <PROP_OWLS_PROFILE_S_PARAMETER>.
	 * @return Rating
	 */
	public Rating getRating()  {
		return (Rating) props.get(PROP_OWLS_PROFILE_S_PARAMETER);
	}
	
	public void setProperty(String propURI, Object value) {
		if (propURI != null  &&  value != null  && !props.containsKey(propURI))
			if (propURI.equals(PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME)) {
				if (value instanceof String)
					props.put(propURI, value);
			} else if (propURI.equals(PROP_OWLS_PROFILE_S_PARAMETER)) {
				if (value instanceof Rating)
					props.put(propURI, value);
			}
	}
}
