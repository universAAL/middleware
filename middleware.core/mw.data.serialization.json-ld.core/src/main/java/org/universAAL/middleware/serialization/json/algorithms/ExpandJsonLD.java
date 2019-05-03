package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
/**
 * Class to expand JsonLD.
 * @author Eduardo
 *
 */
public class ExpandJsonLD {
	private ContextDefinition activeContext=null;
	private JsonArray expandedJsonLD=null;
	private Object jsonToExpand=null;
	private JsonObject mainJson;
	private JsonParser parser = new JsonParser();
	
	public ExpandJsonLD(Object jsonToExpand) {
		this.jsonToExpand=jsonToExpand;
		this.expandedJsonLD = new JsonArray();
	}
	
	public JsonArray expand() {
		this.initJSONLDDoc();
		if(this.mainJson==null)
			return null;
		JsonObject context= this.mainJson.get(JsonLdKeyword.CONTEXT.toString()).getAsJsonObject();
		this.activeContext = new ContextDefinition(context);
		if(this.activeContext.validate()) {
			//TODO add logging system
			System.out.println("conext invalid...");
			return null;
		}
		
		for (Entry<String, JsonElement> jsonElement : this.mainJson.entrySet()) {
			if (termExpansion(this.activeContext,jsonElement)) return null;
		}
		
		return this.expandedJsonLD;
	}
	
	/**
	 * this method add the expanded term to resultant json (expanded)
	 * @param context
	 * @param toExpand
	 */
	private Boolean termExpansion(ContextDefinition context,Map.Entry<String, JsonElement> toExpand ) {

		return true;
	}
	
	
	private void initJSONLDDoc() {
		
		if(this.jsonToExpand instanceof String) {
			if(parser.parse((String)jsonToExpand).isJsonObject()) {
				this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
			}
		}

		if(this.jsonToExpand instanceof JsonObject) {
			this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
		}

		if(this.jsonToExpand instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner( (InputStream)this.jsonToExpand );
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			if(parser.parse((String)jsonToExpand).isJsonObject()) {
				this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
			}
		}
		
	}
}
