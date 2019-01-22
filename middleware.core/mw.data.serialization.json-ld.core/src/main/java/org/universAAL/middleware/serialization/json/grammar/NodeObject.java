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

import org.universAAL.middleware.rdf.Resource;

import com.google.gson.JsonObject;

/**
 * @author amedrano
 * @see <a
 *      href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#node-objects>https://www.w3.org/TR/2014/REC-json-ld-20140116/#node-objects</a>
 */
public class NodeObject implements JSONLDInterpreter<Resource> {

	private JsonObject obj;

	/**
	 * It does not contain the @value, @list, or @set keywords.
	 * A node object must be a JSON object. All keys which are not IRIs, compact
	 * IRIs, terms valid in the active context, or one of the following keywords
	 * must be ignored when processed:
	 *
	 * @context, @id, @graph, @type, @reverse, or @index
	 */
	public NodeObject(JsonObject obj) {
		this.obj = obj;
		if (!validate()) {
			//TODO use appropriate Exception
			//TODO log
			throw new InvalidParameterException();
		}
	}

	/* (non-Javadoc)
	 * @see org.universAAL.middleware.serialization.json.grammar.JSONLDInterpreter#interpret()
	 */
	public Resource interpret() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.universAAL.middleware.serialization.json.grammar.JSONLDValidator#validate()
	 */
	public boolean validate() {
		return false;
	}
}
