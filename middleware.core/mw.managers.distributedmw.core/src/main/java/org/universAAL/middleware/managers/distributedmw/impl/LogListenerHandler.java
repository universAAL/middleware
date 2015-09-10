/*	
	Copyright 2007-2016 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.managers.distributedmw.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.distributedmw.api.DistributedLogListener;
import org.universAAL.middleware.managers.distributedmw.impl.DistributedMWManagerImpl.Handler;
import org.universAAL.middleware.rdf.Resource;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public class LogListenerHandler extends ListenerHandler<DistributedLogListener> {
    public static final String TYPE_ADD_LOGLISTENER = DistributedMWManagerImpl.NAMESPACE
	    + "addLogListener";
    public static final String TYPE_REMOVE_LOGLISTENER = DistributedMWManagerImpl.NAMESPACE
	    + "removeLogListener";
    public static final String TYPE_LOGLISTENER_MESSAGE = DistributedMWManagerImpl.NAMESPACE
	    + "LogListenerMessage";

    public static final String PROP_LEVEL = DistributedMWManagerImpl.NAMESPACE
	    + "logLevel";
    public static final String PROP_MODULE = DistributedMWManagerImpl.NAMESPACE
	    + "module";
    public static final String PROP_PKG = DistributedMWManagerImpl.NAMESPACE
	    + "pkg";
    public static final String PROP_CLS = DistributedMWManagerImpl.NAMESPACE
	    + "cls";
    public static final String PROP_METH = DistributedMWManagerImpl.NAMESPACE
	    + "method";
    public static final String PROP_MSG = DistributedMWManagerImpl.NAMESPACE
	    + "msgPart";
    public static final String PROP_T = DistributedMWManagerImpl.NAMESPACE
	    + "t";

    private Object[] sharingParams;
    private LocalLogListener localListener = null;
    

    public class LogListenerMessageHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer, to which we subscribed, sent us a message
	    // -> notify all listeners

	    int logLevel = (Integer) r.getProperty(PROP_LEVEL);
	    String module = (String) r.getProperty(PROP_MODULE);
	    String pkg = (String) r.getProperty(PROP_PKG);
	    String cls = (String) r.getProperty(PROP_CLS);
	    String method = (String) r.getProperty(PROP_METH);
	    Object[] msgPart = ((List<?>) r.getProperty(PROP_MSG)).toArray();
	    String t = (String) r.getProperty(PROP_T);

	    Set<DistributedLogListener> st = null;
	    synchronized (listeners) {
		st = listeners.get(sender);
		if (st == null || st.size() == 0) {
		    // TODO: log message?
		    // we received a message from a node to which we did not
		    // subscribe. This can also happen in the short time after
		    // removing the listener until the remote node stops sending
		    // messages
		    return;
		}
		// dispatch message
		for (DistributedLogListener l : st) {
		    l.log(sender, logLevel, module, pkg, cls, method, msgPart,
			    t);
		}
	    }
	}
    }

    public class LocalLogListener implements LogListener {
	public void log(int logLevel, String module, String pkg, String cls,
		String method, Object[] msgPart, Throwable t) {

	    // get throwable as string
	    Writer result = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(result);
	    t.printStackTrace(printWriter);
	    String s = result.toString();

	    // dispatch message
	    synchronized (listeners) {
		// local subscriptions
		for (DistributedLogListener l : localListeners) {
		    l.log(DistributedMWManagerImpl.myPeer, logLevel, module,
			    pkg, cls, method, msgPart, s);
		}

		// remote subscriptions
		if (subscribers != null) {
		    Resource r = new Resource();
		    r.addType(TYPE_LOGLISTENER_MESSAGE, true);
		    r.setProperty(PROP_LEVEL, Integer.valueOf(logLevel));
		    r.setProperty(PROP_MODULE, module);
		    r.setProperty(PROP_PKG, pkg);
		    r.setProperty(PROP_CLS, cls);
		    r.setProperty(PROP_METH, method);
		    r.setProperty(PROP_MSG,
			    new ArrayList<Object>(Arrays.asList(msgPart)));
		    r.setProperty(PROP_T, s);

		    for (PeerCard peer : subscribers) {
			DistributedMWManagerImpl.sendMessage(r, peer);
		    }
		}
	    }
	}
    }

    public LogListenerHandler() {
	super(TYPE_ADD_LOGLISTENER, TYPE_REMOVE_LOGLISTENER);
    }

    public void setSharingParams(Object[] sharingParams) {
	this.sharingParams = sharingParams;
    }

    public void shareObject(Object objToShare) {
	DistributedMWManagerImpl.context.getContainer().shareObject(
		DistributedMWManagerImpl.context, objToShare, sharingParams);
    }

    public void removeSharedObject(Object objToRemove) {
	DistributedMWManagerImpl.context.getContainer().removeSharedObject(
		DistributedMWManagerImpl.context, objToRemove, sharingParams);
    }

    @Override
    protected void addListenerLocally() {
	synchronized (this) {
	    if (localListener == null) {
		localListener = new LocalLogListener();
		shareObject(localListener);
	    }
	}
    }
}
