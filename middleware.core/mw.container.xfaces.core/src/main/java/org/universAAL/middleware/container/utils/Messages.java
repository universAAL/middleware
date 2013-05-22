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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
	private URL defaultResource;

	/**
	 * Constructor: opens the file with the given descriptor and loads all
	 * messages. Initializes to default Locale.
	 * 
	 * @param propertiesFile
	 *            the properties file to use to load the messages.
	 * @throws IOException if the propertiesFile does not exist
	 * @throws IllegalArgumentException if propertiesFile has incorrect extension
	 */
	public Messages(File propertiesFile) throws IOException
	, IllegalArgumentException {
		this(propertiesFile, Locale.getDefault());
	}

	/**
	 * Constructor: opens the file with the given descriptor and loads all
	 * messages. Initializes a default Locale.
	 * 
	 * @param propertiesFile
	 *            the properties file to use to load the messages.
	 * @param initialLocale
	 *            the initialLocale to be used.
	 * @throws IOException if the propertiesFile does not exist
	 * 	(but not if the internationalized file does not exist)
	 * @throws IllegalArgumentException if propertiesFile has incorrect extension
	 */
	public Messages(File propertiesFile, Locale initialLocale)
			throws IOException, IllegalArgumentException {
		this(propertiesFile.toURI().toURL(), initialLocale);
	}
	
	/**
	 * Constructor: opens the file with the given descriptor and loads all
	 * messages. Initializes to default Locale.
	 * 
	 * @param propertiesURL
	 *            the properties file to use to load the messages.
	 * @throws IOException if the propertiesURL does not exist
	 * @throws IllegalArgumentException if propertiesURL has incorrect extension
	 */
	public Messages(URL propertiesURL)
			throws IOException, IllegalArgumentException {
		this(propertiesURL, Locale.getDefault());
	}
	
	/**
	 * Constructor: opens the file with the given descriptor and loads all
	 * messages. Initializes a default Locale.
	 * 
	 * @param propertiesURL
	 *            the properties file to use to load the messages.
	 * @param initialLocale
	 *            the initialLocale to be used.
	 *            
	 * @throws IOException if the propertiesURL does not exist 
	 * 	(but not if the internationalized file does not exist)
	 * @throws IllegalArgumentException if propertiesURL has incorrect extension
	 */
	public Messages(URL propertiesURL, Locale initialLocale)
			throws IOException, IllegalArgumentException {
		if (propertiesURL == null )
			throw new IllegalArgumentException("URL should not be null.");
		else if ( propertiesURL.getFile()== null )
			throw new IllegalArgumentException("Not accessible resource.");
		else if (!propertiesURL.getFile().endsWith(FILE_EXTENSION)) 
			throw new IllegalArgumentException("File should be a \"" +FILE_EXTENSION +"\" file");
		if (initialLocale == null)
			initialLocale = Locale.getDefault();
		
		defaultResource = propertiesURL;
		defaultMessages = load(propertiesURL);
		setLocale(initialLocale);
	}

	/**
	 * Try to change the locale for messages.
	 * 
	 * @param loc
	 */
	public void setLocale(Locale loc) {
		if (lang == null 
				|| !lang.equals(loc)) {
			try {
				localizedMessages = load(internationalizedFile(loc));
				lang = loc;
			} catch (IOException e) {
				// log?
			}
		}
	}

	/**
	 * Get the current Locale used for messages.
	 * @return the current Locale, null if default.
	 */
	public Locale getCurrentLocale(){
		return lang;
	}
	
	/**
	 * Gets the file descriptor for the international {@link Locale} set.
	 * 
	 * @param loc
	 * @return {defaultFileName}_{localeLanguaje}.properties
	 */
	private URL internationalizedFile(Locale loc) {
			try {
				return 
					new URL(defaultResource.getProtocol(), defaultResource.getHost(),
						defaultResource.getFile().replace(FILE_EXTENSION,
								"_" + loc.getLanguage() + FILE_EXTENSION));
			} catch (MalformedURLException e) {
				return null;
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
		String l = null;
		if (localizedMessages != null){
			l = localizedMessages.getProperty(key);
		}
		if (l == null && defaultMessages != null)
			l = defaultMessages.getProperty(key);

		return (l == null) ? key : l;
	}

	/**
	 * Loads the property file into {@link Properties}.
	 * @param propertiesURL the file from which to load.
	 * @return the {@link Properties} conained in file f.
	 * @throws IOException if file not found, or could not read.
	 */
	private Properties load(URL propertiesURL) throws IOException {
		Properties props = new Properties();
		InputStream fis = propertiesURL.openStream();
		props.load(fis);
		fis.close();
		return props;
	}
}