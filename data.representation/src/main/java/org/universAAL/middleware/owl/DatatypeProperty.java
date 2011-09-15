package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.Property;

public final class DatatypeProperty extends Property {

    public static final String MY_URI = ClassExpression.OWL_NAMESPACE + "DatatypeProperty";

    
    private class PrivateDataTypePropertySetup extends PrivatePropertySetup implements DatatypePropertySetup {
	public PrivateDataTypePropertySetup(Property prop) {
	    super(prop);
	}
    }
    
    protected DatatypeProperty(String uri, OntClassInfo info) {
	super(uri, info);
	setup = new PrivateDataTypePropertySetup(this);
	super.setup = setup;
	addType(MY_URI, true);
    }
    
    public static DatatypePropertySetup create(String propURI, OntClassInfo info) {
	DatatypeProperty prop = new DatatypeProperty(propURI, info);
	return (DatatypePropertySetup) prop.setup;
    }
}
