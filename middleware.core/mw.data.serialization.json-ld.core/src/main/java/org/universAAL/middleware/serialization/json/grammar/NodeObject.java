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
import org.universAAL.middleware.serialization.json.JsonLdKeyword;

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

	/**
	 * It does not contain the @value, @list, or @set keywords. A node object must
	 * be a JSON object. All keys which are not IRIs, compact IRIs, terms valid in
	 * the active context, or one of the following keywords must be ignored when
	 * processed:
	 *
	 * @context, @id, @graph, @type, @reverse, or @index
	 */
	public NodeObject(Object father, JsonObject obj) {
		if (father instanceof ContextDefinition) {
			throw new InvalidParameterException(
					"A JSON object is a node object if it exists outside of a JSON-LD context");
		}
		this.father = father;
		this.obj = obj;
	}

	/**
	 * Context's value must be null,
	 * an absolute IRI, a relative IRI, or a context definition.
	 */
	private boolean isValidContext(JsonElement context) {
		return context.isJsonNull()
		|| IRI.isAbsolute(context.getAsString())
		|| IRI.isRelative(null,context.getAsString())
		|| new ContextDefinition(context.getAsJsonObject()).validate();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.middleware.serialization.json.grammar.JSONLDValidator#validate
	 * ()
	 */
	public boolean validate() {
		// A node object must be a JSON object.
		if (!obj.isJsonObject()) {
			return false;
		}
		//it is not the top-most JSON object in the JSON-LD document consisting of no other members than @graph and @context.
		if (father instanceof JSONLDDocument
				&& obj.entrySet().size() == 2
				&& obj.entrySet().contains(JsonLdKeyword.GRAPH.toString())
				&& obj.entrySet().contains(JsonLdKeyword.CONTEXT.toString())) {
			return false;
		}

		/* If the node object contains the @context key, its value must be null,
		 * an absolute IRI, a relative IRI, a context definition,
		 * or an array composed of any of these.
		 */
		if (obj.entrySet().contains(JsonLdKeyword.CONTEXT.toString())) {
			JsonElement context = obj.get(JsonLdKeyword.CONTEXT.toString());
			if (context.isJsonArray()) {
				boolean allContextValid = true;
				for (JsonElement je : context.getAsJsonArray()) {
					allContextValid &= isValidContext(je);
					if (allContextValid) {
						if (activeContext == null) {
							activeContext = new ContextDefinition(je);
						}else {
							activeContext.merge(new ContextDefinition(je));
						}
					}
				}
				if (!allContextValid) {
					return false;
				}
			} else if (isValidContext(context)) {
				activeContext = new ContextDefinition(context);
			}
			else {
				return false;
			}
		}

		/* All keys which are not IRIs, compact IRIs, terms valid in the active context,
		 * or one of the following keywords must be ignored when
		 * processed: @context, @id, @graph, @type, @reverse, or @index
		 */

		//TODO ...

		return true;
	}
}
