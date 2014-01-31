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


import junit.framework.TestCase;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.owl.UIBusOntology;

/**
 * @author amedrano
 *
 */
public class SearchTest extends TestCase {

	private static final int DEPTH = 10;
	private static JUnitModuleContext mc;
	
	static {

		mc = new JUnitModuleContext();
		OntologyManagement.getInstance().register(mc, new DataRepOntology());
    	OntologyManagement.getInstance().register(mc, new UIBusOntology());
	}

	
	private PropertyPath getPath(String input){
		return new PropertyPath(
				null,
				false,
				new String[] { "http://org.universaal.ui.newGui/tests.owl#"+input });
	}
	
	public void test() {
		Form f = Form.newDialog("test", new Resource());
		
		InputField[] fields = new InputField[DEPTH];
		Group[] groups = new Group[DEPTH];
		
		groups[0] = new Group(f.getIOControls(), new Label(null, null), null, null, null);
		
		for (int i = 1; i < DEPTH; i++) {
			groups[i] = new Group(groups[i-1],new Label(null, null), null, null, null);
			String fid = "input" + Integer.toString(i);
			fields[i] = new InputField(groups[i],
					new Label(fid, null),
					getPath(fid),
					null,
					null);
		}
		
		Select s = new Select(f.getStandardButtons(), new Label("troll",null), getPath("select"), null, null);
		
		for (int i = 1; i < DEPTH; i++) {
			assertEquals(groups[i], f.searchFormControl(groups[i].getURI()));
			assertEquals(fields[i], f.searchFormControl(fields[i].getURI()));
		}
		assertNull(groups[0].searchFormControl(s.getURI()));
	}	

}
