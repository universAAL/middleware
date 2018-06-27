/*******************************************************************************
 * Copyright 2018 2011 Universidad Politécnica de Madrid
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
package org.universAAL.middleware.serialization.json;

import java.util.Enumeration;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.serialization.json.analyzers.BlankNodeAnalyzer;
import org.universAAL.middleware.serialization.json.analyzers.GraphAnalyzer;
import org.universAAL.middleware.serialization.json.analyzers.PrefixAnalyzer;
import org.universAAL.middleware.serialization.json.analyzers.SerializationTypeAnalysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author amedrano
 * 
 */
public class JSONLDWriter {

	private Context context;
	private SerializationTypeAnalysis serTypeAn;
	private URICompactor compactor;
	private BlankNodeAnalyzer bnAn;

	String serialize(Resource o) {
		GraphAnalyzer firstPass = new GraphAnalyzer();
		serTypeAn = new SerializationTypeAnalysis();
		bnAn = new BlankNodeAnalyzer();
		firstPass.addAnalyzer(serTypeAn);
		firstPass.addAnalyzer(bnAn);
		firstPass.analyze(o);

		GraphAnalyzer secondPass = new GraphAnalyzer();
		compactor = new URICompactor();
		secondPass.addAnalyzer(new PrefixAnalyzer(serTypeAn, compactor));
		secondPass.analyze(o);
		// create context
		context = new Context(compactor);
		// create JsonObect
		JsonObject root = new JsonObject();
		root.add(JsonLdKeyword.CONTEXT.toString(), context.getContext());
		// Check if needs to be flat or not
		if (serTypeAn.isTree()) {
			// can be written in full tree, with the root as object
		} else {
			// must be flattened as a graph
		}
		return root.getAsString();
	}

	JsonObject getObject(Resource r, JsonObject usethis) {
		// TODO: process Resources marked as literals

		JsonObject jo;
		if (usethis != null) {
			jo = usethis;
		} else {
			jo = new JsonObject();
		}
		// add URI
		// check if it is anonymous
		String id = r.isAnon()? bnAn.getSerializedURI(r): compactor.compact(r.getURI()).getCompacted();
		jo.addProperty(JsonLdKeyword.ID.toString(),
				id);

		// add type(s)
		String[] types = r.getTypes();
		if (types.length == 1) {
			jo.addProperty(JsonLdKeyword.TYPE.toString(),
					compactor.compact(types[0]).getCompacted());
		} else if (types.length > 1) {
			JsonArray ta = new JsonArray();
			for (int i = 0; i < types.length; i++) {
				ta.add(new JsonPrimitive(compactor.compact(types[i])
						.getCompacted()));
			}
			jo.add(JsonLdKeyword.TYPE.toString(), ta);
		}

		// iterate over properties
		Enumeration props = r.getPropertyURIs();
		while (props.hasMoreElements()) {
			String p = (String) props.nextElement();
			if (!serTypeAn.isSerialized(r, p)){
				continue;
			}
			String cp = compactor.compact(p).getCompacted();
			Object O = r.getProperty(p);
			JsonElement je = getElement(O, r.isClosedCollection(p));
			if (je != null) {
				jo.add(cp, je);
			}
			// XXX: else warn!
			if (O instanceof Resource && !isEmbedded(r)) {
				// it has been serialized as not embedded, need to serialize in
				// flatmode.
			}
		}

		return jo;
	}

	private JsonElement getElement(Object o, boolean isClosedCollection) {

		if (o instanceof Resource && isEmbedded((Resource) o)) {
			return getObject((Resource) o, null);
		} else if (o instanceof Resource) {
			JsonObject ejo = new JsonObject();
			// check anonymous
			Resource r = (Resource)o;
			String id = r.isAnon()? bnAn.getSerializedURI(r): compactor.compact(r.getURI()).getCompacted();
			ejo.addProperty(JsonLdKeyword.ID.toString(),id);
			return ejo;
		}
		if (o instanceof List) {
			JsonArray ja = new JsonArray();
			for (Object lo : (List) o) {
				ja.add(getElement(lo, isClosedCollection));
			}
			if (isClosedCollection) {
				// as list
				JsonObject ljo = new JsonObject();
				ljo.add(JsonLdKeyword.LIST.toString(), ja);
				return ljo;
			} else {
				// as array
				return ja;
			}
		}
		if (TypeMapper.getDatatypeURI(o) != null) {
			// is data type
			String[] lit = TypeMapper.getXMLInstance(o);
			return new JsonPrimitive(lit[0]);
			// TODO add xsd type to context?
		}
		return null;
	}

	/**
	 * Check if a {@link Resource} should be embedded. Conditions: <li>it is not
	 * a resource with only URI <li>it is a resource with just 1 reference.
	 * 
	 * @param o
	 * @return
	 */
	private boolean isEmbedded(Resource r) {
		serTypeAn.countRefs(r, SerializationTypeAnalysis.REF_TYPE_ALL);
		return !r.representsQualifiedURI()
				|| serTypeAn.countRefs(r,
						SerializationTypeAnalysis.REF_TYPE_ALL) == 1;
	}

}
