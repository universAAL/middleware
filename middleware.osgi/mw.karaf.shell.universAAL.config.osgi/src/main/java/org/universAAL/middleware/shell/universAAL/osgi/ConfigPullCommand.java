/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
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
package org.universAAL.middleware.shell.universAAL.osgi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurationFileEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.ConfigurationFileTypePattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.EntityPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.IdPattern;

/**
 * Commands for universAAL configuration
 * 
 * @author amedrano
 */
@Command(scope = "universAAL", name = "configPull", description = "Get the current Configuration file")
public class ConfigPullCommand extends ConfigurationEditorAbstractCommand {

    @Argument(index = 0, name = "parameter", description = "Parameter referring to the file to pull", required = true, multiValued = false)
    String parameter = null;
    
    @Argument(index = 1, name = "path", description = "Local Path to copy the file to", required = true, multiValued = false)
    String path = null;
    
    @Override
    protected Object doExecute() throws Exception {

	List<EntityPattern> pattern = new ArrayList<EntityPattern>();
	// only for File configuration.
	pattern.add(new ConfigurationFileTypePattern());

	if (parameter != null && !parameter.isEmpty()){
	    pattern.add(new IdPattern(parameter));
	}
	else {
	    System.out.println("no parameter selected");
	}
	String locale = "en";
	List<ConfigurableEntityEditor> ents = getConfigurationEditor().getMatchingConfigurationEditors(pattern, new Locale(locale));

	if (ents.size() == 0){
	    System.out.println("No Entity found by the given Id");
	    return null;
	}

	ConfigurationFileEditor selected = null;

	if (ents.size() > 1){
	    //TODO select menu
	}
	else {
	    selected = (ConfigurationFileEditor) ents.get(0);
	}
	
	File dest = new File(path);
	File org = selected.pullFile();
	
	if (!org.renameTo(dest)){
		System.err.println("unable to rename temp File.");
	}
	
	
	return null;
    }
}
