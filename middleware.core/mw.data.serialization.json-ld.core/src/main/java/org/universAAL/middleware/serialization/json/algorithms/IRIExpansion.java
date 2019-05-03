package org.universAAL.middleware.serialization.json.algorithms;

import java.util.Map;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class IRIExpansion {
	
	public static JsonElement expandIRI(ContextDefinition activeContext, Map.Entry<String, JsonElement> toExpand) {
		/**
		 * If active property has a type mapping in the active context set to @id or @vocab, and the value is a string,
		 *  a dictionary with a single member @id whose value is the result of using the IRI Expansion algorithm on value is returned.*/
		if( activeContext.hasTerm(toExpand.getKey())) {
			 if(activeContext.getTerm(toExpand.getKey()).getAsJsonObject().has(JsonLdKeyword.ID.toString()) ||
				activeContext.getTerm(toExpand.getKey()).getAsJsonObject().has(JsonLdKeyword.VOCAB.toString())) {
				//IRIExpansionAlgorithm

		}

	}
		return null;
}
	
}
