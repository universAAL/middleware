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

import java.util.Map.Entry;
import java.util.Set;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#lists-and-sets>https://www.w3.org/TR/2014/REC-json-ld-20140116/#lists-and-sets</a>
 */
public class ListObject implements JSONLDValidator{
	private JsonElement candidate=null;
	
	public ListObject(JsonElement candidate) {
		this.candidate=candidate;
	}

	public boolean validate() {
		//A list object MUST be a JSON object that contains no keys that expand to 
		//an absolute IRI or keyword other than @list, @context, and @index.
		
		if(this.candidate.isJsonObject() && this.candidate != null) {
			JsonObject obj = this.candidate.getAsJsonObject();
			
			for (Entry<String, JsonElement> element : obj.entrySet()) {
				if( !(element.getKey().equals(JsonLdKeyword.LIST.toString()) ||
						element.getKey().equals(JsonLdKeyword.CONTEXT.toString()) ||
						element.getKey().equals(JsonLdKeyword.INDEX.toString())))
					return false;
     
			}
			
		}else
			//TODO  
			return false;
		
		return true;
	}

}
