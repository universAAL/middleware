/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council 

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
package org.universAAL.middleware.interfaces.aalspace.xml.model;

import static org.junit.Assert.*;

import org.junit.Assert.*;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

/**
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class AalspaceTest {

    @Test
    public void testUAPPParsing() {
	try {
	    JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
	    Unmarshaller unmarshaller = jc.createUnmarshaller();
	    Marshaller marshaller = jc.createMarshaller();
	    InputStream is = AalspaceTest.class
		    .getResourceAsStream("./Home.space");
	    Aalspace space = (Aalspace) unmarshaller.unmarshal(is);
	} catch (JAXBException e) {
	    e.printStackTrace(System.err);
	    fail(e.getMessage());
	}
    }

}
