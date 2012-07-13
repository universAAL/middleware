/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui.impl;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.owl.AccessImpairment;
import org.universAAL.middleware.ui.rdf.ChoiceItem;
import org.universAAL.middleware.ui.rdf.ChoiceList;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.Group;
import org.universAAL.middleware.ui.rdf.InputField;
import org.universAAL.middleware.ui.rdf.Label;
import org.universAAL.middleware.ui.rdf.MediaObject;
import org.universAAL.middleware.ui.rdf.Range;
import org.universAAL.middleware.ui.rdf.Repeat;
import org.universAAL.middleware.ui.rdf.Select;
import org.universAAL.middleware.ui.rdf.Select1;
import org.universAAL.middleware.ui.rdf.SimpleOutput;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;
import org.universAAL.middleware.ui.rdf.TextArea;

/**
 * A factory for creating UIBus objects.
 * 
 * @author Carsten Stockloew
 */
public class UIBusFactory extends ResourceFactoryImpl {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.rdf.impl.ResourceFactoryImpl#createInstance
     * (java.lang.String, java.lang.String, int)
     */
    public Resource createInstance(String classURI, String instanceURI,
	    int factoryIndex) {

	switch (factoryIndex) {
	case 0:
	    return new UIHandlerProfile();
	case 1:
	    return new UIRequest();
	case 2:
	    return new UIResponse();
	case 3:
	    return new AccessImpairment();
	case 4:
	    return new Label();
	case 5:
	    return new ChoiceItem();
	case 6:
	    return new ChoiceList();
	case 7:
	    return new Form(instanceURI);
	case 8:
	    return new Group();
	case 9:
	    return new Select();
	case 10:
	    return new Select1();
	case 11:
	    return new InputField();
	case 12:
	    return new SimpleOutput();
	case 13:
	    return new TextArea();
	case 14:
	    return new Submit();
	case 15:
	    return new SubdialogTrigger();
	case 16:
	    return new Repeat();
	case 17:
	    return new MediaObject();
	case 18:
	    return new Range();
	}

	return null;
    }
}
