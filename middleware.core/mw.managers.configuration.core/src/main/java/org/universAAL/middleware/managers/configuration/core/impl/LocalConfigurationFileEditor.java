/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.managers.configuration.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurationFileEditor;
import org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;

/**
 * The Implementation for {@link ConfigurableEntityEditor} for local and for
 * configuration file type Entities.
 * 
 * @author amedrano
 * 
 */
public class LocalConfigurationFileEditor extends LocalConfigurationEntity
	implements ConfigurationFileEditor {

    /**
     * @param configurationManagerImpl
     * @param uri
     */
    public LocalConfigurationFileEditor(
	    ConfigurationManagerImpl configurationManagerImpl, String uri) {
	super(configurationManagerImpl, uri);
    }

    /** {@ inheritDoc} */
    public URL getDefaultFileRef() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    try {
		return new URL(((ConfigurationFile) e).getDefaultURL());
	    } catch (MalformedURLException e1) {
		e1.printStackTrace();
	    }
	}
	return null;
    }

    /** {@ inheritDoc} */
    public String getExtensionfilter() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    return ((ConfigurationFile) e).getExtensionFilter();
	}
	return null;
    }

    /** {@ inheritDoc} */
    public boolean isDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ConfigurationFile cf = (ConfigurationFile) e;
	    File defF;
	    File actual;
	    try {
		defF = confManager.fileM.cache(new URL(cf.getDefaultURL()));
		actual = new File(URLDecoder.decode(
			new URL(cf.getLocalURL()).getFile(), "UTF-8"));
		return actual.getAbsolutePath().equals(defF.getAbsolutePath());
	    } catch (Exception e1) {
		return false;
	    }
	}
	return false;
    }

    /** {@ inheritDoc} */
    public boolean setDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ((ConfigurationFile) e).setLocalURL(((ConfigurationFile) e)
		    .getDefaultURL());
	    try {
		((ConfigurationFile) e).loadContentFromDefaultURL();
	    } catch (IOException e1) {
		((ConfigurationFile) e).setContent(null);
	    }
	    e.incrementVersion();
	    return confManager.updateLocalAndPropagate(e);
	}
	return false;
    }

    /** {@ inheritDoc} */
    public File pullFile() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ConfigurationFile cf = (ConfigurationFile) e;
	    URL url = null;
	    try {
		url = new URL(cf.getLocalURL());
	    } catch (MalformedURLException e1) {
		// very complicated
	    }
	    File f;
	    try {
		f = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		if (f.exists()) {
		    return f;
		}
	    } catch (UnsupportedEncodingException e1) {
	    }
	}
	return null;
    }

    /** {@ inheritDoc} */
    public boolean pushFile(File file) {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ConfigurationFile cf = (ConfigurationFile) e;
	    try {
		cf.setLocalURL(file.toURI().toURL().toString());
		cf.loadContentFromLocalURL();
		cf.incrementVersion();
		return confManager.updateLocalAndPropagate(cf);
	    } catch (Exception e1) {
		return false;
	    }
	}
	return false;
    }

}
