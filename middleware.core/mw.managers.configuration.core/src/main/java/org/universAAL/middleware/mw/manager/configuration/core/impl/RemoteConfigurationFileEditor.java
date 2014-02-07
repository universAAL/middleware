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

package org.universAAL.middleware.mw.manager.configuration.core.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationEditionTypes.ConfigurationFileEditor;
import org.universAAL.middleware.mw.manager.configuration.core.owl.ConfigurationFile;
import org.universAAL.middleware.mw.manager.configuration.core.owl.Entity;

/**
 * Implementation of {@link ConfigurableEntityEditor} for remote, file type entities.
 * @author amedrano
 */
public class RemoteConfigurationFileEditor extends RemoteConfigurationEntity
	implements ConfigurationFileEditor {

    /**
     * @param configurationManagerImpl
     * @param uri
     */
    public RemoteConfigurationFileEditor(
	    ConfigurationManagerImpl configurationManagerImpl, Entity entity) {
	super(configurationManagerImpl, entity);
    }

    /** {@ inheritDoc}	 */
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

    /** {@ inheritDoc}	 */
    public String getExtensionfilter() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	   return ((ConfigurationFile)e).getExtensionFilter();
	}
	return null;
    }

    /** {@ inheritDoc}	 */
    public boolean isDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {	
	    ConfigurationFile cf = (ConfigurationFile) e;
	    //oversimplifying:
	    return cf.getLocalURL().equals(cf.getDefaultURL());
	}
	return false;
    }

    /** {@ inheritDoc}	 */
    public boolean setDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ((ConfigurationFile)e).setLocalURL(((ConfigurationFile)e).getDefaultURL());
	    ((ConfigurationFile) e).setContent(null);
	    e.incrementVersion();
	    confManager.propagate(e);
	    return true;
	}	
	return false;
    }

    /** {@ inheritDoc}	 */
    public File pullFile() {
	Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ConfigurationFile cf = (ConfigurationFile) e;
	    // write content in a temp file and return it. 
	    File f = confManager.fileM.getTemporalFile();
	    ConfigurationFile.writeContentToFile(cf.getContent(), f);
	    return f;
	}
	return null;
    }

    /** {@ inheritDoc}	 */
    public boolean pushFile(File file) {Entity e = getEntity();
	if (e instanceof ConfigurationFile) {
	    ConfigurationFile cf = (ConfigurationFile) e;
	    try {
		cf.setLocalURL(file.toURI().toURL().toString());
		cf.loadContentFromLocalURL();
		cf.setLocalURL(null);
		cf.incrementVersion();
		confManager.propagate(cf);
		return true;
	    } catch (Exception e1) {
		return false;
	    }
	}
	return false;
    }

}
