package org.universAAL.middleware.brokers.message.aalspace;

public class AALSpaceMessageException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String description;

    public AALSpaceMessageException(String description) {
	super();
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

}
