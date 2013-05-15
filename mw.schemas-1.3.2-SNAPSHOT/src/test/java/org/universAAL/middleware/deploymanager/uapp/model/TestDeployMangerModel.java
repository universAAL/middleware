package org.universAAL.middleware.deploymanager.uapp.model;

import static org.junit.Assert.fail;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class TestDeployMangerModel {

    @Test
    public void testUAPPParsing() {
        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Marshaller marshaller = jc.createMarshaller();
            InputStream is = TestDeployMangerModel.class.getResourceAsStream("./hwo.uapp.xml");
            AalUapp uAAP = (AalUapp)unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            e.printStackTrace(System.err);
            fail(e.getMessage());
        }
    }

}
