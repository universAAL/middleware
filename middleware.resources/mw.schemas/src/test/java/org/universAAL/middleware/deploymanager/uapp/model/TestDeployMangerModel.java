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
package org.universAAL.middleware.deploymanager.uapp.model;

import static org.junit.Assert.*;

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
public class TestDeployMangerModel {

    @Test
    public void testUAPPParsing() {
	try {
	    JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
	    Unmarshaller unmarshaller = jc.createUnmarshaller();
	    Marshaller marshaller = jc.createMarshaller();
	    InputStream is = TestDeployMangerModel.class
		    .getResourceAsStream("./hwo.uapp.xml");
	    AalUapp uAAP = (AalUapp) unmarshaller.unmarshal(is);
	    Part part = uAAP.getApplicationPart().getPart().get(0);
	    System.out.println(part.getBundleId());
	    DeploymentUnit unit = part.getDeploymentUnit().get(0);
	    FeaturesRoot f = unit.getContainerUnit().getKaraf().getFeatures();
	    assertNotNull("Features file must not be null", f);
	} catch (JAXBException e) {
	    e.printStackTrace(System.err);
	    fail(e.getMessage());
	}
    }

}
