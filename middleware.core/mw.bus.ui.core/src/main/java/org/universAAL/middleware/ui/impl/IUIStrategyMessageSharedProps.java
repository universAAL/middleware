/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.ui.impl;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.UIRequest;

/**
 * @author amedrano
 *
 */
public interface IUIStrategyMessageSharedProps {
    
    /**
     * The DialogID over which the message applies.
     */
    public static final String PROP_uAAL_DIALOG_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "dialogID";

    /**
     * References changed (updated) dialog data to be passed to coordinator
     */
    public static final String PROP_uAAL_UI_UPDATED_DATA = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "updatedData";
    
    /**
     * Property to hold the Handler ID.
     */
    public static final String PROP_uAAL_UI_HANDLER_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theUIHandler";
    
    /**
     * Property to hold the Caller ID.
     */
    public static final String PROP_uAAL_UI_CALLER_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theUICaller";
    
    /**
     * Property to hold the Dialog ID.
     */
    public static final String PROP_uAAL_UI_DIALOG_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "dialogID";

    /**
     * Property to hold the input that comes from the user.
     */
    public static final String PROP_uAAL_UI_USER_INPUT = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "userInput";

    /**
     * Property to hold the {@link UIRequest}.
     */
    public static final String PROP_uAAL_UI_CALL = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theUICall";

    /**
     * Property to hold the Changed parameter.
     */
    public static final String PROP_uAAL_CHANGED_PROPERTY = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "changedProperty";
}
