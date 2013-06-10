/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council 

	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	  http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.connectors.exception;

/**
 * Discovery Connector Exception
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class DiscoveryConnectorException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7231774119150878104L;
    /**
	 * 
	 */
    private String description;
    private short errorCode;

    public DiscoveryConnectorException(short errorCode, String description) {
	super();
	this.description = description;
	this.errorCode = errorCode;
    }

    public String toString() {
	return "cause: " + this.description + "error code: " + this.errorCode;
    }

}
