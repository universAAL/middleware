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

import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurationParameterEditor;
import org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.MergedRestriction;

/**
 * Implementation of {@link ConfigurableEntityEditor} for remote, parameter type entities.
 * @author amedrano
 *
 */
public class RemoteConfigurationParamaterEditor extends
	RemoteConfigurationEntity implements ConfigurationParameterEditor {

    /**
     * @param configurationManagerImpl
     * @param remote
     */
    public RemoteConfigurationParamaterEditor(
	    ConfigurationManagerImpl configurationManagerImpl, Entity remote) {
	super(configurationManagerImpl, remote);
    }

    /** {@ inheritDoc} */
    public Object getDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationParameter) {
	    return ((ConfigurationParameter) e).getDefaultValue();
	}
	return null;
    }

    /** {@ inheritDoc} */
    public MergedRestriction getType() {
	Entity e = getEntity();
	if (e instanceof ConfigurationParameter) {
	    return ((ConfigurationParameter) e).getParameterRestriction();
	}
	return null;
    }

    /** {@ inheritDoc} */
    public boolean isDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationParameter) {
	    Object dVal = ((ConfigurationParameter) e).getDefaultValue();
	    return ((ConfigurationParameter) e).getValue().equals(dVal);
	}
	return false;
    }

    /** {@ inheritDoc} */
    public boolean setDefaultValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationParameter) {
	    Object dVal = ((ConfigurationParameter) e).getDefaultValue();
	    return setValue((ConfigurationParameter) e, dVal);
	}
	return false;
    }

    /**
     * @param e
     * @param dVal
     * @return
     */
    boolean setValue(ConfigurationParameter e, Object val) {
	if (e.setValue(val)) {
	    e.incrementVersion();
	    confManager.propagate(e);
	    return true;
	}
	return false;
    }

    /** {@ inheritDoc} */
    public boolean setValue(Object value) {
	Entity e = getEntity();
	if (e instanceof ConfigurationParameter) {
	    return setValue((ConfigurationParameter) e, value);
	}
	return false;
    }

    /** {@ inheritDoc} */
    public Object getConfiguredValue() {
	Entity e = getEntity();
	if (e instanceof ConfigurationParameter) {
	    return ((ConfigurationParameter) e).getValue();
	}
	return null;
    }

}
