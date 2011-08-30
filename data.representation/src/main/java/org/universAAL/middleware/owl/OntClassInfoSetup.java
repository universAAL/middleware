package org.universAAL.middleware.owl;

public interface OntClassInfoSetup {

    public void addSuperClass(ClassExpression superClass);

    public void addSuperClass(String namedSuperClass);

    public void addRestriction(MergedRestriction r);

    public ObjectProperty addObjectProperty(String propURI,
	    boolean isFunctional, boolean isInverseFunctional,
	    boolean isSymmetric, boolean isTransitive);

    public DataTypeProperty addDatatypeProperty(String propURI,
	    boolean isFunctional);

    public void addInstance(ManagedIndividual instance);

    public void toEnumeration(ManagedIndividual[] individuals);

    public OntClassInfo getInfo();
    
    public void setResourceComment(String comment);
    
    public void setResourceLabel(String label);
}
