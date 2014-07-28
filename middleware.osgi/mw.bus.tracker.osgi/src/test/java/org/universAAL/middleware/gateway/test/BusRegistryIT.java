package org.universAAL.middleware.gateway.test;

import java.util.Arrays;

import org.osgi.framework.Constants;
import org.springframework.util.Assert;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.osgi.Activator;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;

/**
 * Integration test for BusRegistry module.
 * 
 * @author dzmuda
 *
 */
public class BusRegistryIT extends IntegrationTest {

    /**
     * Constructor of each integration TestCase has to call constructor of upper
     * class providing path to the launch configuration and path to the
     * configuration directory of the uAAL runtime. Launch configuration will be
     * used to setup uAAL runtime for the purpose of TestCase. All bundles
     * needed for the TestCase have to be included in the launch configuration.
     */
    public BusRegistryIT() {
    }

    /**
     * Helper method for logging.
     * 
     * @param msg
     */
    protected void logInfo(String format, Object... args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logInfo(Activator.mc, getClass(),
		callingMethod.getMethodName(),
		new Object[] { formatMsg(format, args) }, null);
    }

    /**
     * Helper method for logging.
     * 
     * @param msg
     */
    protected void logError(Throwable t, String format, Object... args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logError(Activator.mc, getClass(),
		callingMethod.getMethodName(),
		new Object[] { formatMsg(format, args) }, t);
    }

    public void testComposite() {
	logAllBundles();
    }

    /**
     * Verifies that runtime platform has correctly started. It prints basic
     * information about framework (vendor, version) and lists installed
     * bundles.
     * 
     * @throws Exception
     */
    public void testOsgiPlatformStarts() throws Exception {
	logInfo("FRAMEWORK_VENDOR %s",
		bundleContext.getProperty(Constants.FRAMEWORK_VENDOR));
	logInfo("FRAMEWORK_VERSION %s",
		bundleContext.getProperty(Constants.FRAMEWORK_VERSION));
	logInfo("FRAMEWORK_EXECUTIONENVIRONMENT %s",
		bundleContext
			.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT));

	logInfo("!!!!!!! Listing bundles in integration test !!!!!!!");
	for (int i = 0; i < bundleContext.getBundles().length; i++) {
	    logInfo("name: " + bundleContext.getBundles()[i].getSymbolicName());
	}
	logInfo("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    /**
     * Verifies the lighting sample with the use of LightingConsumer. Following
     * operations are tested: getControlledLamps, turnOn, turnOff, dimToValue.
     * 
     * @throws Exception
     */
    /**
     * @throws Exception
     */
    public void testBusRegistry() throws Exception {
	logInfo("!!!!!!! Testing Bus Registry !!!!!!!");
	logInfo("!!!!!!! Getting Bus Registry !!!!!!!");

	IBusMemberRegistry registry = (IBusMemberRegistry) Activator.mc
		.getContainer().fetchSharedObject(Activator.mc,
			IBusMemberRegistry.busRegistryShareParams);

	MockBusRegistryListener listener = new MockBusRegistryListener();

	registry.addListener(listener, true);
	
	int[] emptyArray = new int[]{0,0,0,0,0,0};
	int[] singleServiceCallee = new int[]{1,0,0,0,0,0};
	int[] singleServiceCaller = new int[]{0,1,0,0,0,0};
	int[] singleContextPublisher = new int[]{0,0,1,0,0,0};
	int[] singleContextSubscriber = new int[]{0,0,0,1,0,0};
	int[] singleUICaller = new int[]{0,0,0,0,1,0};
	int[] singleUIHandler = new int[]{0,0,0,0,0,1};

	
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	ServiceCaller caller = new DefaultServiceCaller(Activator.mc);
	Assert.isTrue(Arrays.equals(singleServiceCaller,listener.getCounts()), "Listener does not contain single service caller count");
	caller.close();
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	ServiceCallee callee = new MockServiceCallee(Activator.mc, new ServiceProfile[]{});
	Assert.isTrue(Arrays.equals(singleServiceCallee,listener.getCounts()), "Listener does not contain single service callee count");
	callee.close();
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	ContextSubscriber subscriber = new MockContextSubscriber(Activator.mc, new ContextEventPattern[]{});
	Assert.isTrue(Arrays.equals(singleContextSubscriber,listener.getCounts()), "Listener does not contain single context subcription count");
	subscriber.close();
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
	ContextProvider info = new ContextProvider();
	info.setType(ContextProviderType.gauge);
	ContextEventPattern cep = new ContextEventPattern();
	info.setProvidedEvents(new ContextEventPattern[] { cep });
	
	ContextPublisher publisher = new DefaultContextPublisher(Activator.mc, info);
	Assert.isTrue(Arrays.equals(singleContextPublisher,listener.getCounts()), "Listener does not contain single context publisher count");
	publisher.close();
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	// commented because it hangs the test
	//UIHandler uiHandler = new MockUIHandler(Activator.mc, new UIHandlerProfile());
	//Assert.isTrue(Arrays.equals(singleUIHandler,listener.getCounts()), "Listener does not contain single ui handler subcription count");
	//uiHandler.close();
	//Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	UICaller uiCaller = new MockUiCaller(Activator.mc);
	Assert.isTrue(Arrays.equals(singleUICaller,listener.getCounts()), "Listener does not contain single ui caller count");
	uiCaller.close();
	Assert.isTrue(Arrays.equals(emptyArray,listener.getCounts()), "Listener does not contain empty array");
	
    }
    
    class MockUiCaller extends UICaller{

	protected MockUiCaller(ModuleContext context) {
	    super(context);
	    // TODO Auto-generated constructor stub
	}

	@Override
	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	}

	@Override
	public void dialogAborted(String dialogID, Resource data) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void handleUIResponse(UIResponse input) {
	    // TODO Auto-generated method stub
	}
    }
    
    class MockUIHandler extends UIHandler {

	protected MockUIHandler(ModuleContext context,
		UIHandlerProfile initialSubscription) {
	    super(context, initialSubscription);
	    // TODO Auto-generated constructor stub
	}

	@Override
	public void adaptationParametersChanged(String dialogID,
		String changedProp, Object newVal) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	}

	@Override
	public Resource cutDialog(String dialogID) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public void handleUICall(UIRequest uicall) {
	    // TODO Auto-generated method stub
	}
    }
    
    class MockContextSubscriber extends ContextSubscriber{

	protected MockContextSubscriber(ModuleContext context,
		ContextEventPattern[] initialSubscriptions) {
	    super(context, initialSubscriptions);
	    // TODO Auto-generated constructor stub
	}

	@Override
	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	    
	}

	@Override
	public void handleContextEvent(ContextEvent event) {
	    // TODO Auto-generated method stub
	    
	}
    }
    
    class MockServiceCallee extends ServiceCallee {

	protected MockServiceCallee(ModuleContext context,
		ServiceProfile[] realizedServices) {
	    super(context, realizedServices);
	    // TODO Auto-generated constructor stub
	}

	@Override
	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	}

	@Override
	public ServiceResponse handleCall(ServiceCall call) {
	    // TODO Auto-generated method stub
	    return null;
	}
	
    }
    
    class MockBusRegistryListener implements IBusMemberRegistryListener {

	private int serviceCalleesCount = 0;
	private int serviceCallersCount = 0;
	private int contextPubilshersCount = 0;
	private int contextSubscribersCount = 0;
	private int uiHandlersCount = 0;
	private int uiCallersCount = 0;

	public void busMemberAdded(BusMember member, BusType type) {
	    if (member instanceof ServiceCallee) {
		serviceCalleesCount++;
	    } else if (member instanceof ServiceCaller) {
		serviceCallersCount++;
	    } else if (member instanceof ContextPublisher) {
		contextPubilshersCount++;
	    } else if (member instanceof ContextSubscriber) {
		contextSubscribersCount++;
	    } else if (member instanceof UICaller) {
		uiCallersCount++;
	    } else if (member instanceof UIHandler) {
		uiHandlersCount++;
	    }
	}

	public void busMemberRemoved(BusMember member, BusType type) {
	    if (member instanceof ServiceCallee) {
		serviceCalleesCount--;
	    } else if (member instanceof ServiceCaller) {
		serviceCallersCount--;
	    } else if (member instanceof ContextPublisher) {
		contextPubilshersCount--;
	    } else if (member instanceof ContextSubscriber) {
		contextSubscribersCount--;
	    } else if (member instanceof UICaller) {
		uiCallersCount--;
	    } else if (member instanceof UIHandler) {
		uiHandlersCount--;
	    }
	}

	public int[] getCounts(){
	    return new int[] { serviceCalleesCount,
		    serviceCallersCount, contextPubilshersCount,
		    contextSubscribersCount, uiCallersCount, uiHandlersCount };
	}
	
	public void print() {
	    int[] values = new int[] { serviceCalleesCount,
		    serviceCallersCount, contextPubilshersCount,
		    contextSubscribersCount, uiCallersCount, uiHandlersCount };
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < values.length; i++) {
		sb.append(values[i]);
		if (i != values.length - 1){
		    sb.append(",");
		}
	    }
	    logInfo(sb.toString());
	}

	public int getContextPubilshersCount() {
	    return contextPubilshersCount;
	}

	public void setContextPubilshersCount(int contextPubilshersCount) {
	    this.contextPubilshersCount = contextPubilshersCount;
	}

	public int getContextSubscribersCount() {
	    return contextSubscribersCount;
	}

	public void setContextSubscribersCount(int contextSubscribersCount) {
	    this.contextSubscribersCount = contextSubscribersCount;
	}

	public int getServiceCalleesCount() {
	    return serviceCalleesCount;
	}

	public void setServiceCalleesCount(int serviceCalleesCount) {
	    this.serviceCalleesCount = serviceCalleesCount;
	}

	public int getServiceCallersCount() {
	    return serviceCallersCount;
	}

	public void setServiceCallersCount(int serviceCallersCount) {
	    this.serviceCallersCount = serviceCallersCount;
	}

	public int getUiHandlersCount() {
	    return uiHandlersCount;
	}

	public void setUiHandlersCount(int uiHandlersCount) {
	    this.uiHandlersCount = uiHandlersCount;
	}

	public int getUiCallersCount() {
	    return uiCallersCount;
	}

	public void setUiCallersCount(int uiCallersCount) {
	    this.uiCallersCount = uiCallersCount;
	}

	public void regParamsAdded(String busMemberID, Resource[] params) {
	    // TODO Auto-generated method stub
	}

	public void regParamsRemoved(String busMemberID, Resource[] params) {
	    // TODO Auto-generated method stub
	}
    }
}
