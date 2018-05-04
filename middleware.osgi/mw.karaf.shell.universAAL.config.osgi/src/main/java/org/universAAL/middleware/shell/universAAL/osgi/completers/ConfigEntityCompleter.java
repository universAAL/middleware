/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
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

package org.universAAL.middleware.shell.universAAL.osgi.completers;

import java.util.List;

import org.apache.karaf.shell.console.Completer;

/**
 * TBD.
 * @author amedrano
 *
 */
public class ConfigEntityCompleter implements Completer {

    /**
     * 
     */
    public ConfigEntityCompleter() {
//	ServiceReference ref = bundleContext
//		.getServiceReference(ConfigurationEditor.class.getName());
//	if (ref != null) {
//	    configurationEditor = (ConfigurationEditor) bundleContext.getService(ref);
//	} else {
//	    throw new IllegalArgumentException("unable to locate the Configuration Editor...");
//	}
    }

    /** {@ inheritDoc}	 */
    public int complete(String buffer, int cursor, List<String> candidates) {
	// TODO Auto-generated method stub
	return 0;
    }

}
