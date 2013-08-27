/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.middleware.api;

import org.universAAL.middleware.api.exception.SimplifiedRegistrationException;
import org.universAAL.middleware.api.impl.DynamicServiceProxy;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.DefaultServiceCaller;

public class SimpleServiceLocator {
    public ModuleContext mc;

    public SimpleServiceLocator(ModuleContext mc) {
	this.mc = mc;
    }

    public Object lookupService(Class<?> interfaceClazz)
	    throws IllegalArgumentException, SimplifiedRegistrationException,
	    InstantiationException, IllegalAccessException {
	return DynamicServiceProxy.newInstance(interfaceClazz,
		new DefaultServiceCaller(mc));
    }
}
