package org.universAAL.middleware.brokers.message.deploy;

public class DeployMessageException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 2616912297180230008L;
    String description;

    public DeployMessageException(String description) {
	this.description = description;
    }

    public String getDescription() {
	return this.description;
    }

}
