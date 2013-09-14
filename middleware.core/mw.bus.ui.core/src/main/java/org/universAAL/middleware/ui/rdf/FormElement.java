/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
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

package org.universAAL.middleware.ui.rdf;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.ui.owl.Recommendation;

/**
 * @author amedrano
 *
 */
public abstract class FormElement extends FinalizedResource {

    /**
     * FormElements may have Recommendations.
     */
    public static final String PROP_APPEARANCE = Form.uAAL_DIALOG_NAMESPACE
	    + "appearance";

	/**
	 * 
	 */
	public FormElement() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param isXMLLiteral
	 */
	public FormElement(boolean isXMLLiteral) {
		super(isXMLLiteral);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param uri
	 * @param isXMLLiteral
	 */
	public FormElement(String uri, boolean isXMLLiteral) {
		super(uri, isXMLLiteral);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param uriPrefix
	 * @param numProps
	 */
	public FormElement(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param uri
	 */
	public FormElement(String uri) {
		super(uri);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Convenience method to add an Appearance recommendation, 
	 * see ont.recommendation for the definition of possible recommendations.
	 * It is not granted that the recommendation will be followed when rendered.
	 * @param recommendation the {@link Recommendation} instance to add.
	 */
	public void addAppearanceRecommendation(Recommendation recommendation){
		if (recommendation == null){
			return;
		}
		
		List recommendations = (List) getProperty(PROP_APPEARANCE);
		if (recommendations == null){
			recommendations = new ArrayList();
		}
		recommendations.add(recommendation);
		changeProperty(PROP_APPEARANCE, recommendations);
	}
	
	/**
	 * Intended to be used by handlers, to iterate over the recommendations added to the FormElement.
	 * @return the List of {@link Recommendation}s, that is empty if none are present.
	 */
	public List getAppearanceRecommendations(){
		List recommendations = (List) getProperty(PROP_APPEARANCE);
		if (recommendations == null){
			recommendations = new ArrayList();
		}
		return recommendations;
	}
}
