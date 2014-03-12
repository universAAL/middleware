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
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.ConfigurationParameterTypePattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.EntityPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.IdPattern;
import org.universAAL.middleware.owl.Complement;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * Commands for universAAL configuration
 * 
 * @author amedrano
 */
@Command(scope = "universAAL", name = "configEdit", description = "Change the value of a given Parameter")
public class ConfigChangeCommand extends ConfigurationEditorAbstractCommand {

    @Argument(index = 0, name = "parameter", description = "Parameter to change", required = true, multiValued = false)
    String parameter = null;

    @Argument(index = 1, name = "value", description = "new Value of the parameter to be changed, if not set default value is set ", required = false, multiValued = false)
    String value = null;

    @Override
    protected Object doExecute() throws Exception {


	List<EntityPattern> pattern = new ArrayList<EntityPattern>();
	// only for Parameter configuration.
	pattern.add(new ConfigurationParameterTypePattern());

	if (parameter != null && !parameter.isEmpty()){
	    pattern.add(new IdPattern(parameter));
	}
	else {
	    System.out.println("no parameter selected");
	}
	String locale = "en";
	List<ConfigurableEntityEditor> ents = configurationEditor.getMatchingConfigurationEditors(pattern, new Locale(locale));

	if (ents.size() == 0){
	    System.out.println("No Entity found by the given Id");
	    return null;
	}

	ConfigurationParameterEditor selected = null;

	if (ents.size() > 1){
	    //TODO select menu
	}
	else {
	    selected = (ConfigurationParameterEditor) ents.get(0);
	}

	if (value == null){
	    boolean res = selected.setDefaultValue();
	    if (res){
		System.out.println("Set to default successful.");
	    } else{
		System.out.println("Could not Set Value!");
	    }
	} else {
	    // Cast String to the correct value
	    Object val = null;

	    if (isOfType(selected, String.class)){
		val = value;
	    }
	    else if (isOfType(selected, Long.class)){
		try {
		    val = Long.decode(value);
		} catch (Exception e) {
		    System.out.println("could not parse value string to appropiate Type (Long)");
		    return null;
		}
	    }
	    else if (isOfType(selected, Integer.class)){
		try {
		    val = Integer.decode(value);
		} catch (Exception e) {
		    System.out.println("could not parse value string to appropiate Type (Integer)");
		    return null;
		}
	    }
	    else if (isOfType(selected, Double.class)){
		try {
		    val = Double.parseDouble(value);
		} catch (Exception e) {
		    System.out.println("could not parse value string to appropiate Type (Double)");
		    return null;
		}
	    }
	    else if (isOfType(selected, Float.class)){
		try {
		    val = Float.parseFloat(value);
		} catch (Exception e) {
		    System.out.println("could not parse value string to appropiate Type (Float)");
		    return null;
		}
	    }
	    else if (isOfType(selected, Boolean.class)){
		try {
		    val = Boolean.parseBoolean(value);
		} catch (Exception e) {
		    System.out.println("could not parse value string to appropiate Type (Boolean)");
		    return null;
		}
	    }

	    //TODO set all the rest of possibilities


	    boolean res = selected.setValue(val);
	    if (res){
		System.out.println("Set new value successful.");
	    } else{
		System.out.println("Could not Set Value!");
	    }
	}
	return null;
    }

    /**
     * @param selected 
     * @param class1
     * @return
     */
    private static boolean isOfType(ConfigurationParameterEditor selected, Class<?> class1) {
	TypeExpression test = new TypeURI(TypeMapper.getDatatypeURI(class1), true);
	return selected.getType().isDisjointWith(new Complement(test));
    }
}
