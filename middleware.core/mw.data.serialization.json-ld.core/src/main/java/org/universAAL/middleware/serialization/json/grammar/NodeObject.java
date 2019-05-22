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
	private JsonElement obj;
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
	
	/**
	 * 
	 * @param parentContext Parent context
	 * @param obj {@link JsonElement} to be analyzed as NodeObject
	 */
	public NodeObject(ContextDefinition activeContext, JsonElement obj) {

		this.activeContext = activeContext;
		this.obj = obj;
	}

	public boolean validate() {
		
		
		//If the node object contains the @context key, its value MUST be null,
		//an absolute IRI, a relative IRI, a context definition, or an array composed of any of these.
		
		//LogUtils.logDebug(JSONLDSerialization.owner, this.getClass(), "validate","the given Json document has not any context to process");

		//if active context is null...exists another context and this context will not be analyzed
		//if exist a context into this object...the jsonLD structure is bad
		
		if(this.obj==null || !this.obj.isJsonObject()) return false;
		//it does not contain the @value, @list, or @set keywords,
		if(this.obj.getAsJsonObject().has(JsonLdKeyword.VALUE.toString()) ||
				this.obj.getAsJsonObject().has(JsonLdKeyword.LIST.toString()) ||
				this.obj.getAsJsonObject().has(JsonLdKeyword.SET.toString()))
			return false;
		
		
		for (Entry<String, JsonElement> element : this.obj.getAsJsonObject().entrySet()) {
			
			//If the node object contains the @context key,its value MUST be null, an absolute IRI, a relative IRI, a context definition, or an array composed of any of these.
			if(element.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
				//an array composed of any of these
				if(element.getValue().isJsonArray()) {
					JsonArray jsa =element.getValue().getAsJsonArray();
					for (JsonElement item : jsa) {
							//control each member of array if is satisfactible context definition
						//its value MUST be null, an absolute IRI, a relative IRI, a context definition
							if(item.isJsonObject()) {
								//context definition case
								//if (! (new ContextDefinition(this.activeContext,item).validate()) )
								if (! (new ContextDefinition(item).validate()) )
									//TODO if exists a main context, this should be merged with it?
									return false;
							}
							if(item.isJsonPrimitive()) {
								//TODO if the context its a IRI need to be controlled, in the example on documentation the json into array are IRI refecenre				
								if (!(element.getValue().isJsonNull() ||
										IRI.isAbsolute(element.getValue().getAsString()) ||
										IRI.isRelative(this.activeContext.getBaseIRI(), element.getValue().getAsString()))
									)
									return false;
							}
						
							
					}
				}else if(element.getValue().isJsonPrimitive()) {//control if it is a string to be processed as IRI
						//value MUST be null,an absolute IRI, a relative IRI, a context definition
						if ( 	!(element.getValue().isJsonNull() ||
								IRI.isAbsolute(element.getValue().getAsString()) ||
								IRI.isRelative(this.activeContext.getBaseIRI(), element.getValue().getAsString()))
							)
							return false;
					}else if(element.getValue().isJsonObject()){
						//TODO object given...check if it is a valid context definition
						if ( !new ContextDefinition(element.getValue()).validate());
							return false;
						}
					
				}

			/*
			 * If the node object contains the @id key, its value MUST be an absolute IRI, a relative IRI, or a compact IRI (including blank node identifiers).
			 *  See section 5.3 Node Identifiers, section 6.3 Compact IRIs, and section 6.14 Identifying Blank Nodes for further discussion on @id values.
			 * */
			if(element.getKey().equals(JsonLdKeyword.ID.toString())) {
						if (!(IRI.isAbsolute(element.getValue().getAsString()) ||
									IRI.isRelative(this.activeContext.getBaseIRI(), element.getValue().getAsString()) ||
									IRI.isCompact(this.activeContext, element.getValue().getAsString()) || 
									element.getValue().getAsString().equals(JsonLdKeyword.BLANK_NODE.toString()))
									)
							return false;
			}
			/*
			 * TODO interpret annd implement correctly
			 * If the node object contains the @graph key, its value MUST be a node object or an array of zero or more node objects. 
			 * If the node object contains an @id keyword, its value is used as the label of a named graph.
			 *  See section 6.13 Named Graphs for further discussion on @graph values. As a special case, 
			 *  if a JSON object contains no keys other than @graph and @context, and the JSON object is the root of the JSON-LD document
			 *  , the JSON object is not treated as a node object; this is used as a way of defining node definitions that may not form a connected graph. 
			 * This allows a context to be defined which is shared by all of the constituent node objects.*/
			if(element.getKey().equals(JsonLdKeyword.GRAPH.toString())) {
				
				if(element.getValue().isJsonObject()) {
					if (! new NodeObject(activeContext, element.getValue().getAsJsonObject()).validate())
						return false;
					//node object control. Take care of infinite loop
				}
				//cero or more node 
				else if(element.getValue().isJsonArray()) {
					for (int i = 0; i < element.getValue().getAsJsonArray().size(); i++) {
						if(element.getValue().getAsJsonArray().get(i).isJsonObject()) {
							if (! new NodeObject(activeContext, element.getValue().getAsJsonArray().get(i).getAsJsonObject()).validate())
								return false;
						}
			
					}
					
				}
				else 
					//jsonPrimitive given...
					return false;
				
			}
			/*
			 * If the node object contains the @type key, its value MUST be either an absolute IRI, a relative IRI, a compact IRI (including blank node identifiers),
			 *  a term defined in the active context expanding into an absolute IRI, or an array of any of these.
			 *   See section 5.4 Specifying the Type for further discussion on @type values.
			 * */
			if(element.getKey().equals(JsonLdKeyword.TYPE.toString())) {
				if(element.getValue().isJsonPrimitive()) {
					if( !(IRI.isAbsolute(element.getValue().getAsString()) ||
							IRI.isRelative(this.activeContext.getBaseIRI(), element.getValue().toString()) || 
							IRI.isCompact(activeContext, element.getValue().getAsString()) ||
							element.getValue().equals(JsonLdKeyword.BLANK_NODE.toString()) ||
							this.activeContext.hasTerm(element.getValue().toString())
							) ) return false;
				}
				else if(element.getValue().isJsonArray()) {
					for (JsonElement item : element.getValue().getAsJsonArray()) {
						if(item.isJsonPrimitive()) {
							
							if( !(IRI.isAbsolute(element.getValue().getAsString()) ||
									IRI.isRelative(this.activeContext.getBaseIRI(), element.getValue().toString()) || 
									IRI.isCompact(activeContext, element.getValue().getAsString()) ||
									element.getValue().equals(JsonLdKeyword.BLANK_NODE.toString()) ||
									this.activeContext.hasTerm(element.getValue().toString())
									) ) return false;
						}else 
							return false;
					}
				}
					return false;

						
			}
			
			/*
			 * If the node object contains the @reverse key, its value MUST be a JSON object containing members representing reverse properties. 
			 * Each value of such a reverse property MUST be an absolute IRI,
			 *  a relative IRI, a compact IRI, a blank node identifier, a node object or an array containing a combination of these.*/
			
			if( element.getKey().equals(JsonLdKeyword.REVERSE)) {
				
				if(element.getValue().isJsonObject()) {
					
					for (Entry<String, JsonElement> item : element.getValue().getAsJsonObject().entrySet()) {
						
						if(item.getValue().isJsonObject()) {
							//validate NodeObject
							if( ! new NodeObject(activeContext, item.getValue()).validate()) 
								return false;
						}else if(item.getValue().isJsonArray()) {
							for (JsonElement value : item.getValue().getAsJsonArray()) {
								if(value.isJsonPrimitive()) {
									
									if( !(IRI.isAbsolute(value.toString()) ||
											IRI.isRelative(this.activeContext.getBaseIRI(), value.toString()) || 
											IRI.isCompact(this.activeContext, value.toString()) ||
											value.toString().equals(JsonLdKeyword.BLANK_NODE.toString()))
											 ) return false;
								}else if( value.isJsonObject()) {
									if( ! new NodeObject(activeContext, value).validate()) 
										return false;
								}
									
							} 
						}else if(item.getValue().isJsonPrimitive()) {
							/*absolute IRI,
							 *  a relative IRI, a compact IRI, a blank node identifier*/
							if( !(IRI.isAbsolute(element.getValue().getAsString()) ||
									IRI.isRelative(this.activeContext.getBaseIRI(), element.getValue().toString()) || 
									IRI.isCompact(this.activeContext, element.getValue().getAsString()) ||
									element.getValue().equals(JsonLdKeyword.BLANK_NODE.toString()))
									 ) return false;
						}else {
							return false;
						}
						
						if(
						  IRI.isAbsolute(item) || 
						  IRI.isRelative(null, item.getKey()) ||
						  IRI.isCompact(this.activeContext, item.getKey()) ||
						  item.getKey().equals(JsonLdKeyword.BLANK_NODE.toString())
					      ) return false;
					}
					//TODO see https://json-ld.org/spec/latest/json-ld/#reverse-properties
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
				if(!element.getValue().isJsonPrimitive())
					return false;
			}
			
			

			
		/*
		 * Keys in a node object that are not keywords MAY expand to an absolute IRI using the active context
		 * The values associated with keys that expand to an absolute IRI MUST be one of the following:*/
		if(!JsonLdKeyword.isKeyword(element.getKey())) {
			//TODO complete this step
			/*
		    string,
		    number,
		    true,
		    false,
		    null,
		    node object,
		    value object, --> A value object is used to explicitly associate a type or a language with a value to create a typed value or a language-tagged string.
		    list object,
		    set object,
		    an array of zero or more of the possibilities above,
		    a language map, or
		    an index map
		*/
			if(!this.activeContext.hasTerm(element.getKey())) {
				return false;
			}else {
				if( !IRI.isAbsolute(this.activeContext.getTermValue(element.getKey()).getAsString())  )
					return false;
				
				if(element.getValue().isJsonPrimitive()) {
					//TODO null will be interpreted as primitive in this case
			  
				}else  if(element.getValue().isJsonObject()) {
						//puede ser node object, value object,list,set
						if( !(new NodeObject(activeContext, element.getValue()).validate() ||
							  new ValueObject(activeContext, element.getValue()).validate()) ||
							  new SetAndListAnalyzer(element.getValue()).validate()	)
							return false;
					}
					if(element.getValue().isJsonArray()) {
			 		
					}
			}
			

	 
			}
			
			

			
		}

		


		return true;
	}
	
	public boolean mergeNodeObjects() {
		return false;
	}
}
