package org.universaal.testing;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


public class JXBMavenIntegration {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JAXBContext jc = JAXBContext.newInstance(org.universAAL.middleware.deploymaneger.uapp.model.ObjectFactory.class);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

}
