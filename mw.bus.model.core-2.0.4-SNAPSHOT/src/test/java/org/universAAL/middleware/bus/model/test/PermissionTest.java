package org.universAAL.middleware.bus.model.test;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.bus.permission.Permission;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

import junit.framework.TestCase;

public class PermissionTest extends TestCase {

    ModuleContext mc;

    private class TestServiceRequest extends ManagedIndividual implements
	    Matchable {
	public boolean matches(Matchable subset) {
	    return true;
	}

	public int getPropSerializationType(String propURI) {
	    return 0;
	}
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	mc = new JUnitModuleContext();

	// init data representation
	SharedResources.moduleContext = mc;
	SharedResources.loadReasoningEngine();
	// OntologyManagement.getInstance().register(mc, new DataRepOntology());
	// mc.getContainer().shareObject(mc, new TurtleSerializer(),
	// new Object[] { MessageContentSerializer.class.getName() });
	TurtleUtil.moduleContext = mc;
	BusMessage.setMessageContentSerializer(new TurtleSerializer());

	// The required classes (i.e. ServiceRequest) is only available in the
	// buses. Therefore, we need to register here at least a minimal
	// ontology with just the one class of a ServiceRequest that implements
	// Matchable. Otherwise, parsing the manifest entry will fail because
	// the resource cannot be deserialized in a specialized class, it will
	// be just a Resource and then the casting to a Matchable will fail.
	OntologyManagement
		.getInstance()
		.register(
			mc,
			new SimpleOntology(
				"http://ontology.universAAL.org/uAAL.owl#ServiceRequest",
				ManagedIndividual.MY_URI,
				new ResourceFactory() {
				    public Resource createInstance(
					    String classURI,
					    String instanceURI, int factoryIndex) {
					return new TestServiceRequest();
				    }
				}));

	// permission
	AccessControl.INSTANCE.init(mc);
	Permission.init(mc);
    }

    public void testFromManifest1() {
	String entry = "Get all light sources---</\r\n"
		+ " title>---Get a list of all light sources.---</description>---@prefix \r\n"
		+ " ns: <http://ontology.igd.fhg.de/LightingConsumer.owl#> . @prefix pvn:\r\n"
		+ "  <http://ontology.universAAL.org/uAAL.owl#> . @prefix : <http://www.d\r\n"
		+ " aml.org/services/owl-s/1.1/Process.owl#> . _:BN000000 a pvn:ServiceRe\r\n"
		+ " quest ; pvn:requiredResult [ :withOutput [ a :OutputBinding ; :toPara\r\n"
		+ " m ns:controlledLamps ; :valueForm \"\"\" @prefix : <http://ontology.univ\r\n"
		+ " ersAAL.org/Service.owl#> . _:BN000000 a :PropertyPath ; :thePath ( <h\r\n"
		+ " ttp://ontology.universaal.org/Lighting.owl#controls> ) . \"\"\"^^<http:/\r\n"
		+ " /www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral> ] ; a :Result ] ; pv\r\n"
		+ " n:requestedService [ a <http://ontology.universaal.org/Lighting.owl#L\r\n"
		+ " ighting> ] . ns:controlledLamps a :Output .---</serialization>---Turn\r\n"
		+ "  light source on---</title>---Turn on a specific light source.---</de\r\n"
		+ " scription>---@prefix owl: <http://www.w3.org/2002/07/owl#> . @prefix \r\n"
		+ " ns: <http://ontology.universaal.org/Lighting.owl#> . @prefix pvn: <ht\r\n"
		+ " tp://ontology.universAAL.org/uAAL.owl#> . @prefix xsd: <http://www.w3\r\n"
		+ " .org/2001/XMLSchema#> . @prefix ns1: <http://www.daml.org/services/ow\r\n"
		+ " l-s/1.1/Process.owl#> . @prefix : <http://ontology.universAAL.org/Ser\r\n"
		+ " vice.owl#> . _:BN000000 a pvn:ServiceRequest ; pvn:requiredResult [ a\r\n"
		+ "  ns1:Result ; ns1:hasEffect [ :affectedProperty [ a :PropertyPath ; :\r\n"
		+ " thePath ( ns:controls ns:srcBrightness ) ] ; a :ChangeEffect ; :prope\r\n"
		+ " rtyValue \"100\"^^xsd:int ] ] ; pvn:requestedService [ a ns:Lighting ; \r\n"
		+ " pvn:instanceLevelRestrictions [ owl:hasValue [ :parameterCardinality \r\n"
		+ " \"1\"^^xsd:int ; a ns1:Parameter ; ns1:parameterType \"http://ontology.u\r\n"
		+ " niversaal.org/Lighting.owl#LightSource\"^^xsd:anyURI ] ; a owl:Restric\r\n"
		+ " tion ; owl:onProperty ns:controls ] ; pvn:numberOfValueRestrictions \"\r\n"
		+ " 1\"^^xsd:int ] .---</serialization>---Turn light source off---</title>\r\n"
		+ " ---Turn off a specific light source.---</description>---@prefix owl: \r\n"
		+ " <http://www.w3.org/2002/07/owl#> . @prefix ns: <http://ontology.unive\r\n"
		+ " rsaal.org/Lighting.owl#> . @prefix pvn: <http://ontology.universAAL.o\r\n"
		+ " rg/uAAL.owl#> . @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . @p\r\n"
		+ " refix ns1: <http://www.daml.org/services/owl-s/1.1/Process.owl#> . @p\r\n"
		+ " refix : <http://ontology.universAAL.org/Service.owl#> . _:BN000000 a \r\n"
		+ " pvn:ServiceRequest ; pvn:requiredResult [ a ns1:Result ; ns1:hasEffec\r\n"
		+ " t [ :affectedProperty [ a :PropertyPath ; :thePath ( ns:controls ns:s\r\n"
		+ " rcBrightness ) ] ; a :ChangeEffect ; :propertyValue \"0\"^^xsd:int ] ] \r\n"
		+ " ; pvn:requestedService [ a ns:Lighting ; pvn:instanceLevelRestriction\r\n"
		+ " s [ owl:hasValue [ :parameterCardinality \"1\"^^xsd:int ; a ns1:Paramet\r\n"
		+ " er ; ns1:parameterType \"http://ontology.universaal.org/Lighting.owl#L\r\n"
		+ " ightSource\"^^xsd:anyURI ] ; a owl:Restriction ; owl:onProperty ns:con\r\n"
		+ " trols ] ; pvn:numberOfValueRestrictions \"1\"^^xsd:int ] .---</serializ\r\n"
		+ " ation>---Dim light source---</title>---Dim a specific light source to\r\n"
		+ "  a given value.---</description>---@prefix owl: <http://www.w3.org/20\r\n"
		+ " 02/07/owl#> . @prefix ns: <http://ontology.universaal.org/Lighting.ow\r\n"
		+ " l#> . @prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> . @pref\r\n"
		+ " ix xsd: <http://www.w3.org/2001/XMLSchema#> . @prefix ns1: <http://ww\r\n"
		+ " w.daml.org/services/owl-s/1.1/Process.owl#> . @prefix : <http://ontol\r\n"
		+ " ogy.universAAL.org/Service.owl#> . _:BN000000 a pvn:ServiceRequest ; \r\n"
		+ " pvn:requiredResult [ a ns1:Result ; ns1:hasEffect [ :affectedProperty\r\n"
		+ "  [ a :PropertyPath ; :thePath ( ns:controls ns:srcBrightness ) ] ; a \r\n"
		+ " :ChangeEffect ; :propertyValue [ :parameterCardinality \"1\"^^xsd:int ;\r\n"
		+ "  a ns1:Parameter ; ns1:parameterType \"http://www.w3.org/2001/XMLSchem\r\n"
		+ " a#int\"^^xsd:anyURI ] ] ] ; pvn:requestedService [ a ns:Lighting ; pvn\r\n"
		+ " :instanceLevelRestrictions [ owl:hasValue [ :parameterCardinality \"1\"\r\n"
		+ " ^^xsd:int ; a ns1:Parameter ; ns1:parameterType \"http://ontology.univ\r\n"
		+ " ersaal.org/Lighting.owl#LightSource\"^^xsd:anyURI ] ; a owl:Restrictio\r\n"
		+ " n ; owl:onProperty ns:controls ] ; pvn:numberOfValueRestrictions \"1\"^\r\n"
		+ " ^xsd:int ] .---</serialization>---";
	entry = entry.replace("\r\n ", "");

	Permission[] perms = Permission.parsePermission(entry);
	assertTrue(perms.length == 4);
    }
}
