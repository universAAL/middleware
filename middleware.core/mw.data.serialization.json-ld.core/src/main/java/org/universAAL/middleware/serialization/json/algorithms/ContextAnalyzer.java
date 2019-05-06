package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.Scanner;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.JSONLDValidator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ContextAnalyzer implements JSONLDValidator{
		
		private JsonElement result=null;
		private JsonObject activeContext=null;
		private JsonObject localContext=null;
		private String jsonString=null;
		private JsonParser parser = new JsonParser();
		
		public ContextAnalyzer(JsonObject localContext) {
			this.localContext = (JsonObject)localContext;
		}
		
		public ContextAnalyzer(String localContext) {
			this.jsonString= localContext;
			if( this.parser.parse(localContext) instanceof JsonObject)
				this.localContext = (JsonObject)this.parser.parse(localContext);
		}
		
		public ContextAnalyzer(InputStream localContext) {
			String jsonString = "";
			Scanner s = new Scanner(localContext);
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			if( this.parser.parse(jsonString) instanceof JsonObject)
				this.localContext = (JsonObject)this.parser.parse(jsonString);
		}

		@Override
		public boolean validate() {
			JsonArray arrayOfContext = new JsonArray();
			this.result = this.activeContext;
			
			if(this.localContext ==null) {
				//TODO throw error
				return false;
			}
			
			if(!this.localContext.isJsonArray()) {//local context is an object
				arrayOfContext.add(this.localContext);
			}
			
			for (int t =0;t<arrayOfContext.size();t++) {
				JsonObject aux =((JsonObject)arrayOfContext.get(t)).get(JsonLdKeyword.CONTEXT.toString()).getAsJsonObject();
				//TODO check the null return behavior
				if(aux.isJsonNull()) {
					this.activeContext = new JsonObject();
				}
				
				if(aux.isJsonPrimitive()) {
					//if the context is a string, remote context will be processed
					//TODO check the documentation
					//https://www.w3.org/2018/jsonld-cg-reports/json-ld-api/#context-processing-algorithm 
					this.activeContext = new JsonObject();
				}
				
				if(aux.isJsonObject()) {
					if( aux.getAsJsonObject().has(JsonLdKeyword.BASE.toString())) {
						
					}
					
					if( aux.getAsJsonObject().has("@version")) {
						
					}
					
					if( aux.getAsJsonObject().has(JsonLdKeyword.VOCAB.toString())) {
						
					} 
					
					if( aux.getAsJsonObject().has(JsonLdKeyword.LANG.toString())) {
						
					} 
						
						
				}else{
					//TODO throw invalid context definition error
				}
				
				
			}
			
			return false;
		}
		

	}
