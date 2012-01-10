package org.universAAL.middleware.context.test;

import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIntegrationTest extends IntegrationTest {

    /**
     * Helper method for logging.
     * 
     * @param msg
     */
    protected void logInfo(String format, Object args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logInfo(ContextBusImpl.moduleContext, getClass(), callingMethod
		.getMethodName(), new Object[] { formatMsg(format, new Object[]{args}) },
		null);
    }

    /**
     * Helper method for logging.
     * 
     * @param msg
     */
    protected void logError(Throwable t, String format, Object args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logError(ContextBusImpl.moduleContext, getClass(), callingMethod
		.getMethodName(), new Object[] { formatMsg(format, new Object[]{args}) }, t);
    }    
    
    public void testComposite() {
	logAllBundles();
    }
    
    public void testPublisher(){
	ContextProvider info=new ContextProvider();
	ContextProvider info2=new ContextProvider();
	info.setType(ContextProviderType.gauge);
	info2.setType(ContextProviderType.controller);
	TestContextPublisher pub=new TestContextPublisher(ContextBusImpl.moduleContext,info);
	ContextEvent event=new ContextEvent(info2,ContextProvider.PROP_CONTEXT_PROVIDER_TYPE);
	pub.publish(event);
	logInfo("PUBLISHED EVENT: %s",event);
    }
    
    protected class TestContextPublisher extends ContextPublisher{

	protected TestContextPublisher(ModuleContext context,
		ContextProvider providerInfo) {
	    super(context, providerInfo);
	}

	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	}
    }

}
