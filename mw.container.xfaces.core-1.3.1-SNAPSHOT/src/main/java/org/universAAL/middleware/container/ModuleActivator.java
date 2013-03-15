package org.universAAL.middleware.container;

public interface ModuleActivator {

    public void start(ModuleContext mc) throws Exception;

    public void stop(ModuleContext mc) throws Exception;

}
