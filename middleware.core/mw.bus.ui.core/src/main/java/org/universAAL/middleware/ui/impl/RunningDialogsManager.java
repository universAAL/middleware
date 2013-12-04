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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.ui.UIHandler;

/**
 * Helper class to keep track of the Dialogs that a {@link UIHandler} is managing at each moment, 
 * and where a given Dialog is being handled.
 * @author amedrano
 *
 */
public class RunningDialogsManager {

    private Map<String, Set<String>> handlersToDialogs;
    private Map<String, String> dialogsToHandlers;
    
    /**
     * 
     */
    public RunningDialogsManager() {
	handlersToDialogs = new Hashtable<String, Set<String>>();
	dialogsToHandlers = new Hashtable<String, String>();
    }

    public void add(String handlerId, String dialogId){
	Set<String> setDialogs = handlersToDialogs.get(handlerId);
	if (setDialogs == null){
	    setDialogs = new HashSet<String>();
	}
	setDialogs.add(dialogId);
	handlersToDialogs.put(handlerId, setDialogs);
	dialogsToHandlers.put(dialogId, handlerId);
    }
    
    public void removeDialogId(String dialogId){
	String handlerID = dialogsToHandlers.remove(dialogId);
	Set<String> dialogs = handlersToDialogs.get(handlerID);
	dialogs.remove(dialogId);
	if (dialogs.isEmpty()){
	    handlersToDialogs.remove(handlerID);
	}else {
	    handlersToDialogs.put(handlerID, dialogs);
	}
    }
    
    public void removeHandlerId(String handlerId){
	Set<String> dialogs = handlersToDialogs.remove(handlerId);
	for (String dID : dialogs) {
	    dialogsToHandlers.remove(dID);
	}
    }
    
    public String getHandler(String dialogID){
	return dialogsToHandlers.get(dialogID);
    }
    
    public Set<String> getDialogs(String handlerID){
	return handlersToDialogs.get(handlerID);
    }
    
    public Set<String> usedHandlers(){
	return handlersToDialogs.keySet();
    }
    
    public Set<String> pendingDialogs(){
	return dialogsToHandlers.keySet();
    }
    
    public boolean isDialogHandled(String dialogId){
	return dialogsToHandlers.containsKey(dialogId);
    }
}
