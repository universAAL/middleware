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

import com.google.gson.JsonElement;

/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#lists-and-sets>https://www.w3.org/TR/2014/REC-json-ld-20140116/#lists-and-sets</a>
 */
public class SetObject implements JSONLDValidator{
	private JsonElement candidate=null;

	
	/*
	 *A set object MUST be a JSON object that contains no keys that expand to an absolute IRI or keyword other than @list, @context, and @index. 
	 *Please note that the @index key will be ignored when being processed.
	 */
	
	/**
	 * 
	 * @param candidate
	 */
	public SetObject(JsonElement candidate) {
		this.candidate = candidate;
	}


	public boolean validate() {
		
			if(this.candidate != null && this.candidate.isJsonObject()) {
				
			}
			
		return true;
	}

}
