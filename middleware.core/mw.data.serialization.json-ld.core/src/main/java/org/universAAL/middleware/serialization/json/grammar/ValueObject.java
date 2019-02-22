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

import java.awt.peer.KeyboardFocusManagerPeer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#value-objects>https://www.w3.org/TR/2014/REC-json-ld-20140116/#value-objects</a>
 */
public class ValueObject implements KeyControl, JSONLDInterpreter<JsonObject>{
	private JsonObject jsonObjectOrReference; 
	private JsonObject mainJSON = null;
	private JsonParser jp = new JsonParser();
	private String jsonToControl=null;
	
	/*A value object MUST be a JSON object containing the @value key. It MAY also contain an @type, an @language, an @index, or an @context key 
	 * but MUST NOT contain both an @type and an @language key at the same time.
	 * A value object MUST NOT contain any other keys that expand to an absolute IRI or keyword.
	 * */
	/**
	 * 
	 * @param jsonObjectOrReference
	 */
	public ValueObject(String jsonObjectOrReference) {
		// TODO Auto-generated constructor stub
//		this.jp = new JsonParser();
//		this.mainJSON = (JsonObject) jp.parse(jsonObjectOrReference);
		this.jsonToControl=jsonObjectOrReference;
	
	}

	public boolean validate() {
		
		if (jp.parse(this.jsonToControl) instanceof JsonObject ) {
			this.mainJSON = (JsonObject)jp.parse(this.jsonToControl);
			// It MAY also contain an @type, an @language, an @index, or an @context key 
//			this.keyControl();
//			return true;
			return this.keyControl();
		}
		return false;
	}

	
	public JsonObject interpret() {
		
		if(this.validate()) {
			
		}
		
		return null;
	}
	
	public boolean keyControl() {
		//MUST NOT contain both an @type and an @language key at the same time
		//TODO  A value object MUST NOT contain any other keys that expand to an absolute IRI or keyword.
		if ( this.mainJSON.has(JsonLdKeyword.LANG.toString()) &&  this.mainJSON.has(JsonLdKeyword.TYPE.toString() )){
			return false;
		}else {
			 JsonElement var = this.mainJSON.get(JsonLdKeyword.LANG.toString()); 
			 System.out.println(var);
		}
		return true;
	}
	


}
