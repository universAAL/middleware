/*******************************************************************************
 * Copyright 2018 Universidad Politécnica de Madrid UPM
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
package org.universAAL.middleware.serialization.json.grammar;

import java.security.InvalidParameterException;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.midi.Soundbank;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * @author amedrano
 * @see <a
 *      href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#node-objects>https://www.w3.org/TR/2014/REC-json-ld-20140116/#node-objects</a>
 */
public class NodeObject implements JSONLDValidator {
	private JsonObject obj;
	private Object father;
	private ContextDefinition activeContext;
	private boolean state;

	/**
	 *
	 * It does not contain the @value, @list, or @set keywords. A node object must
	 * be a JSON object. All keys which are not IRIs, compact IRIs, terms valid in
	 * the active context, or one of the following keywords must be ignored when
	 * processed:
	 *
	 * @context, @id, @graph, @type, @reverse, or @index
	 */
	
	
	public NodeObject(ContextDefinition father/*Object father*/, JsonObject obj) {
//		if (father instanceof ContextDefinition) {
//			throw new InvalidParameterException("A JSON object is a node object if it exists outside of a JSON-LD context");
//			}
		this.father = father;
		this.obj = obj;
	}

	/**
	 * Context's value must be null,
	 * an absolute IRI, a relative IRI, or a context definition.
	 */
//	private boolean isValidContext(JsonElement context) {
//		return context.isJsonNull()
//		|| IRI.isAbsolute(context.getAsString())
//		|| IRI.isRelative(null,context.getAsString())
//		|| new ContextDefinition(context.getAsJsonObject()).validate();
//	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.middleware.serialization.json.grammar.JSONLDValidator#validate
	 * ()
	 */
	public boolean validate() {
		
		
		//If the node object contains the @context key, its value MUST be null,
		//an absolute IRI, a relative IRI, a context definition, or an array composed of any of these.
		
//		LogUtils.logDebug(JSONLDSerialization.owner, this.getClass(), "validate","the given Json document has not any context to process");
		
		//si active context esta null significa que ya se encontro un contexto antes y este no debera analizarse...
		//si aqui tambien hay context entonces el JSONLD esta mal
		if(this.activeContext!=null) return false;
		//it does not contain the @value, @list, or @set keywords,
		if(this.obj.entrySet().contains(JsonLdKeyword.VALUE) ||
				this.obj.entrySet().contains(JsonLdKeyword.LIST) ||
				this.obj.entrySet().contains(JsonLdKeyword.SET))
			return false;
		
		
		for (Entry<String, JsonElement> element : this.obj.entrySet()) {
			
			//If the node object contains the @context key,
			if(element.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
				//an array composed of any of these
				if(element.getValue().isJsonArray()) {
					JsonArray jsa =element.getValue().getAsJsonArray();
					for (JsonElement item : jsa) {
							//control each member of array if is satisfactible context definition
							if(item.isJsonObject()) 
								if (! (new ContextDefinition(item).validate()) )
									return false;
							
							if(item.isJsonPrimitive()) {
								//TODO if the context its a IRI need to be controlled, in the example on documentation the 
								//json into array are IRI refecenre				
							}
						
							
					}
				}else {
					//if it is not an array...
					if(element.getValue().isJsonPrimitive()) {//control if it is a string to be processed as IRI
						//value MUST be null,an absolute IRI, a relative IRI, a context definition
						if ( 	!(element.getValue().isJsonNull() ||
								IRI.isAbsolute(element.getValue().getAsString()) ||
								IRI.isRelative(null, element.getValue().getAsString()))
							)
							return false;
					}else {
						//return false;
						//if it isn't primitive the node was incorrect structure
						//TODO throw error
					}
					
				}
				
				
			}
			/*
			 * If the node object contains the @id key, its value MUST be an absolute IRI, a relative IRI, or a compact IRI (including blank node identifiers).
			 *  See section 5.3 Node Identifiers, section 6.3 Compact IRIs, and section 6.14 Identifying Blank Nodes for further discussion on @id values.
			 * */
			if(element.getKey().equals(JsonLdKeyword.ID.toString())) {
						if ( ! (IRI.isAbsolute(element.getValue().getAsString()) ||
									IRI.isRelative("", element.getValue().getAsString()) ||
									IRI.isCompact(this.activeContext, element.getValue().getAsString()) || 
									element.getValue().getAsString().equals(JsonLdKeyword.BLANK_NODE))
									)
							return false;
			}
			/*
			 * 
			 * If the node object contains the @graph key, its value MUST be a node object or an array of zero or more node objects. 
			 * If the node object contains an @id keyword, its value is used as the label of a named graph.
			 *  See section 6.13 Named Graphs for further discussion on @graph values. As a special case, 
			 *  if a JSON object contains no keys other than @graph and @context, and the JSON object is the root of the JSON-LD document
			 *  , the JSON object is not treated as a node object; this is used as a way of defining node definitions that may not form a connected graph. 
			 * This allows a context to be defined which is shared by all of the constituent node objects.*/
			if(element.getKey().equals(JsonLdKeyword.GRAPH.toString())) {
				
				if(element.getValue().isJsonObject()) {
					//node object control. Take care of infinite loop
				}
				//cero or more node 
				if(element.getValue().isJsonArray()) {
					//array of node objects control.Take care of infinite loop
				}
				
				
			}
			/*
			 * If the node object contains the @type key, its value MUST be either an absolute IRI, a relative IRI, a compact IRI (including blank node identifiers),
			 *  a term defined in the active context expanding into an absolute IRI, or an array of any of these.
			 *   See section 5.4 Specifying the Type for further discussion on @type values.
			 * */
			if(element.getKey().equals(JsonLdKeyword.TYPE.toString())) {
				if(element.getValue().isJsonPrimitive()) {
					if( !(IRI.isAbsolute(element.getValue().getAsString()) ||
							/*IRI.isRelative(baseIRI, candidate) ||*/ 
							IRI.isCompact(activeContext, element.getValue().getAsString()) ||
							element.getValue().equals(JsonLdKeyword.BLANK_NODE.toString())
							//TODO see:  a term defined in the active context expanding into an absolute IRI, or an array of any of these
							) ) return false;					
				}

						
			}
			
			/*3
			 * .If the node object contains the @reverse key, its value MUST be a JSON object containing members representing reverse properties. 
			 * Each value of such a reverse property MUST be an absolute IRI,
			 *  a relative IRI, a compact IRI, a blank node identifier, a node object or an array containing a combination of these.*/
			
			if( element.getKey().equals(JsonLdKeyword.REVERSE)) {
				if(element.getValue().isJsonObject()) {
					JsonObject jso = element.getValue().getAsJsonObject(); 
					
				}else {
					//TODO throw error
					return false;
				}
			}
			
			/*
			 * If the node object contains the @index key, its value MUST be a string.
			 *  See section 6.16 Data Indexing for further discussion on @index values.
			 * */
			if(element.getKey().equals(JsonLdKeyword.INDEX)){
				if(!element.getValue().isJsonPrimitive()) return false;
			}
		
			//TODO :Keys in a node object that are not keywords MAY expand to an absolute IRI using the active context. The values associated with keys that expand to an absolute IRI MUST be one of the following:
		}


		return true;
	}
}
