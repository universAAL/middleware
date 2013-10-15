/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

import junit.framework.TestCase;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.owl.UIBusOntology;

/**
 * @author amedrano
 *
 */
public class RepeatVirtualFormExpansionTest extends TestCase {

	/**
	 * 
	 */
	private static final String SIMPLE_OUTPUT_CONTENT = "out";
	private static final String PREFIX = "http://example.com/Dable.owl#";
    private static final String PROP_TABLE = PREFIX + "table";
    private static final String PROP_COL = PREFIX + "column";
	private static JUnitModuleContext mc;
	
	static{
	    mc = new JUnitModuleContext();
		OntologyManagement.getInstance().register(mc,new DataRepOntology());
		OntologyManagement.getInstance().register(mc,new UIBusOntology());
	}
    
    public void testSimpleNull(){
    	Repeat r = getObjectRepeat(null,SIMPLE_OUTPUT_CONTENT);
    	List fs = r.virtualFormExpansion();
    	assertEquals(3, fs.size());
    	for (int i = 1; i < 4; i++) {
    		FormControl[] ch = ((Form) fs.get(i-1)).getIOControls().getChildren();
    		assertEquals(1, ch.length);
    		assertEquals(SIMPLE_OUTPUT_CONTENT, (String) ch[0].getValue());
    	}
    }

    public void testSimpleEmpty(){
    	Repeat r = getObjectRepeat(new PropertyPath(PROP_COL, true, new String[]{}),null);
    	List fs = r.virtualFormExpansion();
    	assertEquals(3, fs.size());
    	for (int i = 1; i < 4; i++) {
    		FormControl[] ch = ((Form) fs.get(i-1)).getIOControls().getChildren();
    		assertEquals(1, ch.length);
    		assertEquals(Integer.valueOf(i),ch[0].getValue());
    	}
    }
    
    public void testResource(){
    	Repeat r = getResourceRepeat();
    	List fs = r.virtualFormExpansion();
    	assertEquals(3, fs.size());
    	for (int i = 1; i < 4; i++){
    		FormControl[] ch = ((Form) fs.get(i-1)).getIOControls().getChildren();
    		assertEquals(3, ch.length);
    		assertEquals(Integer.valueOf(i),ch[0].getValue());
    		
    	}
    }
    
    private Repeat getObjectRepeat(PropertyPath pp, Object content){
    	List list = new ArrayList();
    	list.add(Integer.valueOf(1));
    	list.add(Integer.valueOf(2));
    	list.add(Integer.valueOf(3));
    	Resource r = new Resource();
    	r.setProperty(PROP_TABLE, list);
    	Form f = Form.newDialog("simple Repeat Test", r);
    	Repeat repeat = new Repeat(f.getIOControls(), new Label("table", null),
    			new PropertyPath(null, false, new String[] { PROP_TABLE }),
    			null, null);
    	new SimpleOutput(repeat, new Label("val", null), pp, content);
    	return repeat;
    }
    
	private Repeat getResourceRepeat(){
		List rows = new ArrayList();
		Resource cell = new Resource();
		cell.setProperty(PROP_COL + "1", new Integer(1));
		cell.setProperty(PROP_COL + "2", "two");
		cell.setProperty(PROP_COL + "3", new Float(3));
		rows.add(cell);
		// ...
		cell = new Resource();
		cell.setProperty(PROP_COL + "1", new Integer(2));
		cell.setProperty(PROP_COL + "2", "three");
		cell.setProperty(PROP_COL + "3", new Float(4));
		rows.add(cell);
		// ...
		cell = new Resource();
		cell.setProperty(PROP_COL + "1", new Integer(3));
		cell.setProperty(PROP_COL + "2", "four");
		cell.setProperty(PROP_COL + "3", new Float(5));
		rows.add(cell);
		Resource dataRoot = new Resource();
		dataRoot.setProperty(PROP_TABLE, rows);
		Form f = Form.newDialog("test", dataRoot);
		Repeat repeat = new Repeat(f.getIOControls(), new Label("table", null),
			new PropertyPath(null, false, new String[] { PROP_TABLE }),
			null, null);
		// new Repeat(g, new Label(userDM
		// .getString("UICaller.pendingDialogs"), null),
		// new PropertyPath(null, false,
		// new String[] { PROP_DLG_LIST_DIALOG_LIST }),
		// null, null);
		Group row = new Group(repeat, null, null, null, null);
		new SimpleOutput(row, new Label("col1", null), new PropertyPath(null,
			false, new String[] { PROP_COL + "1" }), null);
		new SimpleOutput(row, new Label("col2", null), new PropertyPath(null,
			false, new String[] { PROP_COL + "2" }), null);
		new SimpleOutput(row, new Label("col3", null), new PropertyPath(null,
			false, new String[] { PROP_COL + "3" }), null);
		return repeat;
	}

}
