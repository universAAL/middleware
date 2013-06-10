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
package org.universAAL.middleware.brokers.message.deploy;
/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class DeployMessageException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 2616912297180230008L;
    String description;

    /**
     *
     * @param msg the error message describing what is happened,
     * 		it will be stored also into the {@link #description} field
     * @param t	The {@link Throwable} exception causing this exception
     * @since 1.3.2
     */
    public DeployMessageException(String msg, Throwable t) {
        super(msg,t);
        this.description = msg;
    }

    public DeployMessageException(String description) {
        super(description);
        this.description = description;
    }

    /**
     *
     * @return the current value of the {@link #description} field
     */
    public String getDescription() {
        return this.description;
    }

}
