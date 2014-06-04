package org.universAAL.middleware.context.test;

import java.math.BigInteger;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.context.impl.osgi.Activator;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

public class SerializerIT extends IntegrationTest {

    public SerializerIT() {
	setRunArguments("net.slp.port", "7000");
	setRunArguments("net.slp.multicastTimeouts", "500,750");
	setRunArguments("java.net.preferIPv4Stack", "true");
    }

    public void testComposite() {
	logAllBundles();
    }

    public void testInteger() {
	// issue from Bug report #280 Incorrect deserialization of Integer
	// it is done here to also test the correct serialization of ontology
	// classes (e.g. ContextEvent is only defined here, so it cannot be
	// tested in mw.data.serialization)
	String serialized = "@prefix : <http://ontology.universAAL.org/Context.owl#> .\r\n"
		+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n"
		+ "@prefix ns : <ns#> .\r\n"
		+ "@prefix ns1 : <ns1#> .\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#_:7f00000167122367:975>\r\n"
		+ ":hasProvider ns1:TestMassContextProvider ;\r\n"
		+ "  a :ContextEvent ;\r\n"
		+ "  :hasConfidence \"39\"^^<http://www.w3.org/2001/XMLSchema#int> ;\r\n"
		+ "  rdf:subject ns1:blind4 ;\r\n"
		+ "  :hasTimestamp\r\n"
		+ "\"1364379795612\"^^<http://www.w3.org/2001/XMLSchema#long> ;\r\n"
		+ "  rdf:predicate ns:hasValue ;\r\n"
		+ "  rdf:object \"100\"^^<http://www.w3.org/2001/XMLSchema#int> .\r\n"
		+ ":gauge a :ContextProviderType .\r\n"
		+ "ns1:blind4 a ns:BlindController ,\r\n"
		+ "    <http://ontology.universAAL.org/Device.owl#Device> ,\r\n"
		+ "    <http://ontology.universAAL.org/uAAL.owl#PhysicalThing> ;\r\n"
		+ "  ns:hasValue \"90\"^^<http://www.w3.org/2001/XMLSchema#int> .\r\n"
		+ "ns1:TestMassContextProvider a :ContextProvider ;\r\n"
		+ "  :hasType :gauge .";
	MessageContentSerializer contentSerializer = (MessageContentSerializerEx) Activator
		.getModuleContext()
		.getContainer()
		.fetchSharedObject(
			Activator.getModuleContext(),
			new Object[] { MessageContentSerializerEx.class
				.getName() });
	Object o = contentSerializer.deserialize(serialized);
	// System.out.println("O: "+ o);

	assertTrue(o instanceof Resource);
	Resource r = (Resource) o;

	o = r.getProperty("http://ontology.universAAL.org/Context.owl#hasConfidence");
	assertTrue(o instanceof Integer);
	assertTrue(((Integer) o).intValue() == 39);

	o = r.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
	assertTrue(o instanceof Integer);
	assertTrue(((Integer) o).intValue() == 100);

	o = r.getProperty("http://ontology.universAAL.org/Context.owl#hasTimestamp");
	assertTrue(o instanceof Long);
	assertTrue(((Long) o).longValue() == 1364379795612L);

	o = r.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
	assertTrue(o instanceof Resource);
	o = ((Resource) o).getProperty("ns#hasValue");
	assertTrue(o instanceof Integer);
	assertTrue(((Integer) o).intValue() == 90);
    }

    public void testInteger2() {
	// issue from Bug report #280 Incorrect deserialization of Integer
	// it is done here to also test the correct serialization of ontology
	// classes (e.g. ContextEvent is only defined here, so it cannot be
	// tested in mw.data.serialization)
	String serialized = "@prefix : <http://ontology.universAAL.org/Context.owl#> .\r\n"
		+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n"
		+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#_:7f000001b5b42abf:99e>\r\n"
		+ "a :ContextEvent ;\r\n"
		+ "	rdf:object \"100\"^^xsd:integer ;\r\n"
		+ "	:hasConfidence \"55\"^^xsd:int .";
	MessageContentSerializer contentSerializer = (MessageContentSerializerEx) Activator
		.getModuleContext()
		.getContainer()
		.fetchSharedObject(
			Activator.getModuleContext(),
			new Object[] { MessageContentSerializerEx.class
				.getName() });
	Object o = contentSerializer.deserialize(serialized);
	System.out.println("O: " + o);

	assertTrue(o instanceof Resource);
	Resource r = (Resource) o;
	System.out.println(r.toStringRecursive());

	o = r.getProperty("http://ontology.universAAL.org/Context.owl#hasConfidence");
	assertTrue(o instanceof Integer);
	assertTrue(((Integer) o).intValue() == 55);

	o = r.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
	assertTrue(o instanceof BigInteger);
	assertTrue(((BigInteger) o).intValue() == 100);
    }
}
