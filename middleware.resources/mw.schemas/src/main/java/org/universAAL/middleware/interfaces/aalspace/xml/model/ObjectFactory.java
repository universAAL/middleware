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

package org.universAAL.middleware.interfaces.aalspace.xml.model;

import javax.xml.bind.annotation.XmlRegistry;

import org.universAAL.middleware.interfaces.aalspace.xml.model.Aalspace.CommunicationChannels;
import org.universAAL.middleware.interfaces.aalspace.xml.model.Aalspace.PeeringChannel;
import org.universAAL.middleware.interfaces.aalspace.xml.model.Aalspace.SpaceDescriptor;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.universAAL.middleware.interfaces.aalspace.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.universAAL.middleware.interfaces.aalspace.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Aalspace }
     * 
     */
    public Aalspace createAalspace() {
        return new Aalspace();
    }

    /**
     * Create an instance of {@link ChannelDescriptor }
     * 
     */
    public ChannelDescriptor createChannelDescriptor() {
        return new ChannelDescriptor();
    }

    /**
     * Create an instance of {@link Aalspace.SpaceDescriptor }
     * 
     */
    public SpaceDescriptor createAalspaceSpaceDescriptor() {
        return new Aalspace.SpaceDescriptor();
    }

    /**
     * Create an instance of {@link Aalspace.PeeringChannel }
     * 
     */
    public PeeringChannel createAalspacePeeringChannel() {
        return new Aalspace.PeeringChannel();
    }

    /**
     * Create an instance of {@link Aalspace.CommunicationChannels }
     * 
     */
    public CommunicationChannels createAalspaceCommunicationChannels() {
        return new Aalspace.CommunicationChannels();
    }

}
