package org.universAAL.middleware.owl;

public interface OntClassInfoSetup {

    public void addSuperClass(ClassExpression superClass);

    public void addSuperClass(String namedSuperClass);

    public void addRestriction(MergedRestriction r);

    public ObjectPropertySetup addObjectProperty(String propURI);

    public DatatypePropertySetup addDatatypeProperty(String propURI);

    public void addInstance(ManagedIndividual instance);

    public void toEnumeration(ManagedIndividual[] individuals);

    public OntClassInfo getInfo();
    
    public void setResourceComment(String comment);
    
    public void setResourceLabel(String label);
    
    public void addEquivalentClass(ClassExpression eq);
    
    public void addDisjointClass(ClassExpression dj);
    
    public void setComplementClass(ClassExpression complement);
}
