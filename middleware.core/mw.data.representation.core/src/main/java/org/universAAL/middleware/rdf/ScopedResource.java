/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
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
package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author amedrano
 *
 */
public class ScopedResource extends FinalizedResource {

	public static String PROP_SCOPE = uAAL_VOCABULARY_NAMESPACE + "hasScopes";
	
	public static String ONLY_LOCAL_SCOPE = uAAL_VOCABULARY_NAMESPACE+ "only_local_scope";
	
	/** {@inheritDoc}	 */
	public ScopedResource() {
		super();
	}

	/** {@inheritDoc}	 */
	public ScopedResource(boolean isXMLLiteral) {
		super(isXMLLiteral);
	}

	/** {@inheritDoc}	 */
	public ScopedResource(String uri) {
		super(uri);
	}

	/** {@inheritDoc}	 */
	public ScopedResource(String uri, boolean isXMLLiteral) {
		super(uri, isXMLLiteral);
	}

	/** {@inheritDoc}	 */
	public ScopedResource(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}

	public boolean isScoped(){
		return props.contains(PROP_SCOPE);
	}
	
	public List getScopes(){
		Object s = getProperty(PROP_SCOPE);
		if (s instanceof String){
			List res = new ArrayList();
			res.add(s);
			return res;
		}else if (s instanceof List){
			return (List) s;
		}else {
			return null;
		}
	}
	
	public boolean addScope(String newScope){
		if (newScope == null){
			return false;
		}
		Object s = getProperty(PROP_SCOPE);
		if (s instanceof String){
			List res = new ArrayList();
			res.add(s);
			res.add(newScope);
			return changeProperty(PROP_SCOPE, res);
		}else if (s instanceof List){
			((List) s).add(newScope);
			return changeProperty(PROP_SCOPE, s);
		}else if (s == null){
			return setProperty(PROP_SCOPE, newScope);
		} else {
			return false;
		}
	}
	
	public boolean clearScopes(){
		return changeProperty(PROP_SCOPE, null);
	}
	
}
