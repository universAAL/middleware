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
package org.universAAL.middleware.ui.owl;

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Defines types of dialog that can be System menu, Message, Subdialog and
 * Standard Dialog. For their explanation check
 * {@link org.universAAL.middleware.ui.rdf.Form}
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ManagedIndividual
 */
public class DialogType extends ManagedIndividual {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "DialogType";

    public static final int SYS_MENU = 0;
    public static final int MESSAGE = 1;
    public static final int SUBDIALOG = 2;
    public static final int STD_DIALOG = 3;

    private static final String[] names = { "system_menu", "message",
	    "subdialog", "std_dialog" };

    public static final DialogType sysMenu = new DialogType(SYS_MENU);
    public static final DialogType message = new DialogType(MESSAGE);
    public static final DialogType subdialog = new DialogType(SUBDIALOG);
    public static final DialogType stdDialog = new DialogType(STD_DIALOG);

    private int order;

    public static DialogType getLevelByOrder(int order) {
	switch (order) {
	case SYS_MENU:
	    return sysMenu;
	case MESSAGE:
	    return message;
	case SUBDIALOG:
	    return subdialog;
	case STD_DIALOG:
	    return stdDialog;
	default:
	    return null;
	}
    }

    public static final DialogType valueOf(String name) {
	for (int i = SYS_MENU; i <= STD_DIALOG; i++)
	    if (names[i].equals(name))
		return getLevelByOrder(i);
	return null;
    }

    /** Default constructor is prevented */
    private DialogType() {
	// prevented usage of the default constructor
    }

    /**
     * Constructor with order (it is private so that instantiation by other
     * callers is prevented?)
     * 
     * @param order
     *            order of dialog
     */
    private DialogType(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
	this.order = order;
    }

    /** @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI() */
    public String getClassURI() {
	return MY_URI;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#getPropSerializationType(String)
     */
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#isWellFormed()
     */
    public boolean isWellFormed() {
	return true;
    }

    /**
     * @return name of the DialogType with the order defined at the time of
     *         construction
     */
    public String name() {
	return names[order];
    }

    /**
     * @return order defined at the time of construction
     */
    public int ord() {
	return order;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#setProperty(String,
     *      Object)
     */
    public boolean setProperty(String propURI, Object o) {
	// do nothing
	return false;
    }
}
