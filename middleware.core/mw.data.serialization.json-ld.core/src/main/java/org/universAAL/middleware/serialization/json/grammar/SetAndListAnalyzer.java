/**
 * 
 */
package org.universAAL.middleware.serialization.json.grammar;

import java.util.Map.Entry;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import com.google.gson.JsonElement;

/**
 * @author Buhid Eduardo
 *
 */
public class SetAndListAnalyzer implements JSONLDValidator{
	private JsonElement candidate=null;
	
	/**
	 * 
	 * @param candidate {@link JsonElement} to be validated
	 */
	public SetAndListAnalyzer(JsonElement candidate) {
		this.candidate = candidate;
	}



	public boolean validate() {
		
		if(this.candidate != null && this.candidate.isJsonObject()) {
			
			for (Entry<String, JsonElement> element : this.candidate.getAsJsonObject().entrySet()) {
				if( !(element.getKey().equals(JsonLdKeyword.LIST.toString()) ||
						element.getKey().equals(JsonLdKeyword.CONTEXT.toString()) ||
						element.getKey().equals(JsonLdKeyword.INDEX.toString())))
					return false;
     
			}
			
		}else
			return false;
		return true;
	}
}
