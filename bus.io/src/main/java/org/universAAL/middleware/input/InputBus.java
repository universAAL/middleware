/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.input;

/**
 * The Input Bus is an event based bus. It is responsible for transferring user
 * input from appropriate I/O Handler to the application. It accepts
 * registration parameters from their subscribers (when they register to the
 * bus) and allows removing and/or updating of these parameters
 * 
 * @author mtazari
 * 
 */
public interface InputBus {

    /**
     * Adds profile of registered subscriber.
     * 
     * @param subscriberID
     *            ID of an input bus subscriber
     * @param dialogID
     *            ID of the dialog
     */
    public void addNewRegParams(String subscriberID, String dialogID);

    /**
     * Removes old subscription of the subscriber.
     * 
     * @param subscriberID
     *            ID of subscriber
     * @param dialogID
     *            ID of the dialog
     */
    public void removeMatchingRegParams(String subscriberID, String dialogID);

    /**
     * Registers input publisher on the input bus.
     * 
     * @param publisher
     *            input publisher (I/O Handler)
     * @return id (local mw name prefix + publisher id)
     */
    public String register(InputPublisher publisher);

    /**
     * Registers input subscriber on the input bus.
     * 
     * @param subscriber
     *            input subscriber
     * @return id (local mw name prefix + subscriber id)
     */
    public String register(InputSubscriber subscriber);

    /**
     * Sends an Input Event.
     * 
     * @param publisherID
     *            ID of input publisher
     * @param event
     *            input event
     */
    public void sendMessage(String publisherID, InputEvent event);

    /**
     * Unregisters Input Publisher.
     * 
     * @param publisherID
     *            ID of input publisher
     * @param publisher
     *            input publisher
     */
    public void unregister(String publisherID, InputPublisher publisher);

    /**
     * Unregisters Input Subscriber.
     * 
     * @param subscriberID
     *            ID of input subscriber
     * @param subscriber
     *            input subscriber
     */
    public void unregister(String subscriberID, InputSubscriber subscriber);
}
