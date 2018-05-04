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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurationParameterEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.EntityPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.IdPattern;
import org.universAAL.middleware.interfaces.configuration.scope.AppPartScope;
import org.universAAL.middleware.interfaces.configuration.scope.ApplicationScope;
import org.universAAL.middleware.interfaces.configuration.scope.InstanceScope;
import org.universAAL.middleware.interfaces.configuration.scope.ModuleScope;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;

/**
 * Commands for universAAL configuration
 * 
 * @author amedrano
 */
@Command(scope = "universAAL", name = "configList", description = "Discover the existing Configurable Entities")
public class ConfigListCommand extends ConfigurationEditorAbstractCommand {

    @Argument(index = 0, name = "regExp", description = "Filter to apply to the discovery", required = false, multiValued = false)
    String regExp = null;
    
    @Argument(index = 1, name = "locale", description = "Locale (two letter) to use for descriptions", required = false, multiValued = false)
    String locale = null;
    
    @Override
    protected Object doExecute() throws Exception {
	log.debug("Executing command...");
	if(locale == null || locale.isEmpty()){
	    locale = "en";
	}
	
	List<EntityPattern> pattern = new ArrayList<EntityPattern>();
	
	if (regExp != null && !regExp.isEmpty()){
	    if (!regExp.endsWith(".*")){
		regExp.concat(".*");
	    }
	    pattern.add(new IdPattern(regExp));
	}
	
	List<ConfigurableEntityEditor> ents = getConfigurationEditor().getMatchingConfigurationEditors(pattern, new Locale(locale));
	
	String leftAlignFormat = "| %-15s | %-15s | %-8s | %-30s |%n";

	System.out.format("+-----------------+-----------------+--------+--------------------------------+%n");
	System.out.printf("| Entity ID       | Scope           | Value  | Description                    |%n");
	System.out.format("+-----------------+-----------------+--------+--------------------------------+%n");
	for (ConfigurableEntityEditor cee : ents) {
	    Scope s = cee.getScope();
	    String val = "";
	    if (cee instanceof ConfigurationParameterEditor){
	    	Object value = ((ConfigurationParameterEditor)cee).getConfiguredValue();
	    	if (value != null) {
				val = value.toString();
			}else {
				val = "null";
			}
	    }else{
		val = "[FILE]";
	    }
	    
	    String scope = "AAL Space";
	    if (s instanceof InstanceScope){
		scope = "Inst:"+ ((InstanceScope)s).getPeerID();
	    }
	    else if (s instanceof ApplicationScope){
		scope = "App:"+((ApplicationScope)s).getAppID();
	    }
	    if (s instanceof ModuleScope){
		scope = "Mod:" + (((ModuleScope)s).getModuleID());
	    }
	    else if (s instanceof AppPartScope){
		scope = "Part:" + ((AppPartScope)s).getPartID();
	    }
	    System.out.format(leftAlignFormat, s.getId(), scope, val, 
		    cee.getDescription(new Locale(locale)));
	}

	System.out.format("+-----------------+-----------------+--------+--------------------------------+%n");
	return null;
    }
}
