package org.universAAL.middleware.container;

public interface uAALModuleActivator {

    public void start(ModuleContext mc) throws Exception;

    public void stop(ModuleContext mc) throws Exception;

}
