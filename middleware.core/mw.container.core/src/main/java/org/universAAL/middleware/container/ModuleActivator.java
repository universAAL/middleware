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
package org.universAAL.middleware.container;

/**
 * Customizes the starting and stopping of a module.
 *
 * The specified ModuleActivator class must have a public constructor that takes
 * no parameters so that a ModuleActivator object can be created by
 * Class.newInstance().
 */
public interface ModuleActivator {

	/**
	 * Called when this module is started so the Framework can perform the
	 * module-specific activities necessary to start this module. This method
	 * can be used to register shared objects or to allocate any resources that
	 * this module needs.
	 *
	 * This method must complete and return to its caller in a timely manner.
	 *
	 * @param mc
	 *            The execution context of the module being started.
	 * @throws Exception
	 *             An exception indicating a problem.
	 */
	public void start(ModuleContext mc) throws Exception;

	/**
	 * Called when this module is stopped so the Framework can perform the
	 * module-specific activities necessary to stop the module. In general, this
	 * method should undo the work that the ModuleActivator.start method
	 * started. There should be no active threads that were started by this
	 * module when this module returns. A stopped module must not call any
	 * Framework objects.
	 *
	 * This method must complete and return to its caller in a timely manner.
	 *
	 * @param mc
	 *            The execution context of the module being stopped.
	 * @throws Exception
	 *             An exception indicating a problem.
	 */
	public void stop(ModuleContext mc) throws Exception;
}
