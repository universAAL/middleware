package org.universAAL.middleware.owl;

public abstract class PrivateResource extends ManagedIndividual {
	public static final String MY_URI = VOCABULARY_NAMESPACE + "PrivateResource";

	protected PrivateResource() {
		super();
	}

	@Override
	public String getClassURI() {
		return MY_URI;
	}

	@Override
	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_FULL;
	}

	@Override
	public boolean isWellFormed() {
		return true;
	}
}
