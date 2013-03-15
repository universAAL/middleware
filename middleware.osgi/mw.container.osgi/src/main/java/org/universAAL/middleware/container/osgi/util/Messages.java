/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
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
package org.universAAL.middleware.container.osgi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * An extension of configuration file handling. The configuration file consists
 * of lines with key-value pairs (called 'messages'). In addition to simplify
 * the reading of configuration files, this class also handles messages in
 * different files representing the messages in different languages. When a
 * message is not available in the localized language, the corresponding value
 * for the default language is returned.<br>
 * The default file name is <i>messages.properties</i>, the file name for a
 * different language include a language code as lowercase ISO 639 code, e.g.
 * for english, the file name would be <i>messages_en.properties</i>.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @author amedrano
 */
public class Messages extends BundleConfigHome {

    /** The set of messages in a localized language. */
    private Properties localizedMessages;

    /** The set of messages in a default language. */
    private Properties defaultMessages;

    /** The localized language. */
    private String lang = "";

    /**
     * Constructor: opens the file with the given ID and loads all messages.
     * 
     * @param id
     *            The ID for the configuration file, see
     *            {@link org.universAAL.middleware.BundleConfigHome.ConfFile#ConfFile(String)}
     * @throws IOException
     */
    public Messages(String id) throws IOException {
	super(id);
	defaultMessages = load("messages.properties");
	setLocale(Locale.getDefault());
    }

    /**
     * Change the locale for messages.
     * 
     * @param loc
     */
    public void setLocale(Locale loc) {
	if (!lang.equals(loc.getLanguage())) {
	    lang = loc.getLanguage();
	    localizedMessages = load("messages_" + loc.getLanguage()
		    + ".properties");
	}
    }

    /**
     * Get the value for a given key.
     * 
     * @param key
     *            The key.
     * @return The value.
     */
    public String getString(String key) {
	String l = localizedMessages.getProperty(key);
	if (l == null)
	    l = defaultMessages.getProperty(key);

	return (l == null) ? key : l;
    }

    private Properties load(String filename) {
	Properties props = new Properties();
	try {
	    InputStream fis = getConfFileAsStream(filename);
	    props.load(fis);
	    fis.close();
	} catch (Exception e) {
	}
	return props;
    }
}