/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.middleware.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * @author mtazari
 *
 */
public class Messages extends ConfFile {
	private Properties localizedMessages, defaultMessages;
	private String lang = null;
	
	public Messages(String id) throws IOException {
		super(id);
		defaultMessages = new Properties();
		InputStream fis = getConfFileAsStream("messages.properties");
		defaultMessages.load(fis);
		fis.close();
	}
	
	public String getString(String key) {
		String l = Locale.getDefault().getLanguage();
		if (!l.equals(lang)) {
			lang = l;
			localizedMessages = new Properties();
			try {
				InputStream fis = getConfFileAsStream("messages_"+l+".properties");
				localizedMessages.load(fis);
				fis.close();
			} catch (Exception e) {}
		}
		
		l = localizedMessages.getProperty(key);
		if (l == null)
			l = defaultMessages.getProperty(key);
		
		return (l == null)? key : l;
	}
}
