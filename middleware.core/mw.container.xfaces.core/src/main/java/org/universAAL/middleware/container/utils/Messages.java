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
package org.universAAL.middleware.container.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * The properties file consists of lines with key-value pairs (called
 * 'messages'). In addition to simplify the reading of configuration files, this
 * class also handles messages in different files representing the messages in
 * different languages. When a message is not available in the localized
 * language, the corresponding value for the default language is returned.<br>
 * If the default file name is <i>messages.properties</i>, the file name for a
 * different language include a language code as lowercase ISO 639 code, e.g.
 * for english, the file name would be <i>messages_en.properties</i>.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @author amedrano
 */
public class Messages {

	/**
	 * The file Extension to be used, it should always be ".properties"
	 */
	private static final String FILE_EXTENSION = ".properties";

	/** The set of messages in a localized language. */
	private Properties localizedMessages;

	/** The set of messages in a default language. */
	private Properties defaultMessages;

	/** The localized language. */
	private Locale lang = null;

	/**
	 * The default properties file used as default, and base for
	 * internationalization.
	 */
	private File defaultFile;

	/**
	 * Constructor: opens the file with the given descriptor and loads all
	 * messages. Initialises to default Locale.
	 * 
	 * @param propertiesFile
	 *            the properties file to use to load the messages.
	 * @throws IOException
	 */
	public Messages(File propertiesFile) throws IOException {
		this(propertiesFile, Locale.getDefault());
	}

	/**
	 * Constructor: opens the file with the given descriptor and loads all
	 * messages. Initialises a default Locale.
	 * 
	 * @param propertiesFile
	 *            the properties file to use to load the messages.
	 * @param initialLocale
	 *            the initialLocale to be used.
	 * @throws IOException
	 */
	public Messages(File propertiesFile, Locale initialLocale)
			throws IOException, IllegalArgumentException {
		if (!propertiesFile.getName().endsWith(FILE_EXTENSION)) {
			throw new IllegalArgumentException("File should be a \"" +FILE_EXTENSION +"\" file");
		}
		defaultFile = propertiesFile;
		defaultMessages = load(propertiesFile);
		setLocale(initialLocale);
	}

	/**
	 * Change the locale for messages.
	 * 
	 * @param loc
	 */
	public void setLocale(Locale loc) {
		if (lang.equals(loc)) {
			try {
				localizedMessages = load(internationalizedFile(loc));
				lang = loc;
			} catch (IOException e) {
				// log?
			}
		}
	}

	/**
	 * Gets the file descriptor for the international {@link Locale} set.
	 * 
	 * @param loc
	 * @return {defaultFileName}_{localeLanguaje}.properties
	 */
	private File internationalizedFile(Locale loc) {
		return new File(defaultFile.getAbsolutePath().replace(FILE_EXTENSION,
				"_" + loc.getLanguage() + FILE_EXTENSION));
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

	/**
	 * Loads the property file into {@link Properties}.
	 * @param f the file from which to load.
	 * @return the {@link Properties} conained in file f.
	 * @throws IOException if file not found, or could not read.
	 */
	private Properties load(File f) throws IOException {
		Properties props = new Properties();
		InputStream fis = new FileInputStream(f);
		props.load(fis);
		fis.close();
		return props;
	}
}