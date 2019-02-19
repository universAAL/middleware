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
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions>https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions</a>
 */
public class ContextDefinition implements JSONLDValidator	 {


	public ContextDefinition(JsonElement jsonObjectOrReference) {
		if (jsonObjectOrReference.isJsonObject()) {
			//TODO
		}
		if (jsonObjectOrReference.isJsonPrimitive()) {
			jsonObjectOrReference.getAsString();
			// TODO read Context from reference.openStream()
		}
	}


	public void merge(ContextDefinition cd) {
		//TODO
	}

	/**
	 * Method to start json validation.
	 * @return {@link Boolean} value indicating the status of the process
	 */
	public boolean validate() {
		//TODO

		return false;
	}


}
