package org.universAAL.middleware.context.test;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.context.test.ont.Location;
import org.universAAL.middleware.context.test.ont.TestOntology;
import org.universAAL.middleware.context.test.ont.User;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIntegrationTest extends IntegrationTest {

    public static String NAMESPACE = "http://ontology.universAAL.org/Test.owl#";
    public static String USER = NAMESPACE + "User";
    public static String DUMMYUSER = NAMESPACE + "dummyUser";
    public static String HAS_LOCATION = NAMESPACE + "hasLocation";
    public static String LOCATION = NAMESPACE + "dummyLocation";
    private BlockingQueue queue = new SynchronousQueue();

    /**
     * Helper method for logging.
     * 
     * @param msg
     */
    protected void logInfo(String format, Object args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logInfo(ContextBusImpl.moduleContext, getClass(),
		callingMethod.getMethodName(), new Object[] { formatMsg(format,
			new Object[] { args }) }, null);
    }

    /**
     * Helper method for logging.
     * 
     * @param msg
     */
    protected void logError(Throwable t, String format, Object args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logError(ContextBusImpl.moduleContext, getClass(),
		callingMethod.getMethodName(), new Object[] { formatMsg(format,
			new Object[] { args }) }, t);
    }

    /**
     * Test which verifies if classes which are not explicitly imported by
     * tested bundle, can be used in integration test.
     */
    public void testDynamicImport() {
	ContextEvent cev1 = ContextEvent.constructSimpleEvent(DUMMYUSER, USER,
		HAS_LOCATION, LevelRating.high);
    }

    /**
     * Test which verifies if JUnit API can be used in integration tests.
     */
    public void testJunitAssert() {
	junit.framework.Assert.assertTrue(true);
    }

    /**
     * Test 1: Check all artifacts in the log
     */
    public void testComposite() {
	logAllBundles();
    }

    /**
     * Test 2: Create context publishers (& fail) <- Integration (use module
     * context)
     */
    public void testCreateContextPublisher() {
	logInfo("-Test 2-", null);
	ContextPublisher pub1, pub2, pub3, pub4, pub5;
	// Correctly create a CP with full info
	ContextProvider info = new ContextProvider();
	info.setType(ContextProviderType.gauge);
	ContextEventPattern cep = new ContextEventPattern();
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));
	info.setProvidedEvents(new ContextEventPattern[] { cep });
	// info.setContextSources(new ManagedIndividual[]{new
	// ManagedIndividual(SOURCE)}); //Until we can use outer classes...
	pub1 = new DummyContextPublisher(ContextBusImpl.moduleContext, info);
	logInfo("Created COntext Publisher with full Provider Info", null);

	// Correctly create a DefCP with full info
	pub2 = new DefaultContextPublisher(ContextBusImpl.moduleContext, info);
	logInfo("Created Default Context Publisher with full Provider Info",
		null);

	// Incorrectly create a CP without info
	try {
	    pub5 = new DummyContextPublisher(ContextBusImpl.moduleContext, null);
	    // Assert.notNull(null,"Allowed creation of a Context Publisher with null provider info");
	} catch (Exception e) {
	    Assert.notNull(e);
	    logInfo("Properly launched exception creating bad publisher %s", e
		    .toString());
	}

	// Incorrectly create a CP without info
	try {
	    info = new ContextProvider();
	    pub3 = new DummyContextPublisher(ContextBusImpl.moduleContext, info);
	    // Assert.notNull(null,"Allowed creation of a Context Publisher with null provider info");
	} catch (Exception e) {
	    Assert.notNull(e);
	    logInfo("Created COntext Publisher without full Provider Info",
		    null);
	}

	// Incorrectly create a CP without info
	try {
	    info = new ContextProvider();
	    pub4 = new DefaultContextPublisher(ContextBusImpl.moduleContext,
		    info);
	    // Assert.notNull(null,"Allowed creation of a Context Publisher with null provider info");
	} catch (Exception e) {
	    Assert.notNull(e);
	    logInfo(
		    "Created Default Context Publisher without full Provider Info",
		    null);
	}

	// Try closes of subscriber
	pub1.communicationChannelBroken();
	pub1.close();
	pub2.close();
    }

    /**
     * Test 3: Create context subscribers (& fail) <- Integration (module
     * context)
     */
    public void testCreateContextSubscriber() {
	logInfo("-Test 3-", null);
	// Correctly create a context subscriber
	ContextSubscriber sub1, sub2;
	ContextEventPattern cep = new ContextEventPattern();
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));
	sub1 = new DummyContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep });
	logInfo("Created Context Subscriber", null);

	// Incorrectly create a context subscriber with null cep
	try {
	    sub2 = new DummyContextSubscriber(ContextBusImpl.moduleContext,
		    null);
	    Assert
		    .notNull(
			    null,
			    "Allowed creation of a Context Subscriber with null context event pattern subscription");
	} catch (Exception e) {
	    Assert.notNull(e);
	    logInfo("Properly launched exception creating bad subscriber %s", e
		    .toString());
	}

	// Try closes of subscriber
	sub1.communicationChannelBroken();
	sub1.close();
    }

    /**
     * Test 4: Send Context Events (& fail) <- Integration (bus)
     */
    public void testSendContextEvent() {
	logInfo("-Test 4-", null);
	// Correctly create a DefCP with full info
	ContextProvider info = new ContextProvider();
	info.setType(ContextProviderType.gauge);
	ContextEventPattern cep = new ContextEventPattern();
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));
	info.setProvidedEvents(new ContextEventPattern[] { cep });
	// info.setContextSources(new ManagedIndividual[]{new
	// ManagedIndividual(SOURCE)}); //Until we can use outer classes...
	ContextPublisher pub = new DefaultContextPublisher(
		ContextBusImpl.moduleContext, info);
	logInfo("Created Default Context Publisher with full Provider Info",
		null);

	// Create and send first event
	ContextEvent cev1 = ContextEvent.constructSimpleEvent(DUMMYUSER, USER,
		HAS_LOCATION, LOCATION);
	pub.publish(cev1);
	logInfo("Published event 1: %s", cev1);

	// Create and send second event
	Resource s = new Resource(DUMMYUSER);
	s.setProperty(Resource.PROP_RDF_TYPE, USER);
	s.setProperty(HAS_LOCATION, new Resource(LOCATION));
	ContextEvent cev2 = new ContextEvent(s, HAS_LOCATION);
	pub.publish(cev2);
	logInfo("Published event 2: %s", cev2);

	// Create and send third event
	ContextProvider info2 = new ContextProvider();
	info2.setType(ContextProviderType.controller);
	ContextEvent cev3 = new ContextEvent(info2,
		ContextProvider.PROP_CONTEXT_PROVIDER_TYPE);
	pub.publish(cev3);
	logInfo("Published event 3: %s", cev3);

	// Incorrectly send a null event
	try {
	    pub.publish(null);
	    // Assert.notNull(null,"Allowed sending a null event");
	} catch (Exception e) {
	    Assert.notNull(e);
	    logInfo("Properly launched exception sending null event %s", e
		    .toString());
	}
    }

    /**
     * Test 5: Receive Context Events (& fail) <- Integration (bus)
     */
    public void testReceiveContextEvent() {
	logInfo("-Test 5-", null);
	// Correctly create a context subscriber
	ContextSubscriber sub;
	ContextEventPattern cepA = new ContextEventPattern();
	cepA.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT,
		new Resource(DUMMYUSER + "right")));
	cepA.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));

	cepA.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextProvider.PROP_CONTEXT_PROVIDER_TYPE,
		ContextProviderType.gauge).appendTo(
		MergedRestriction.getAllValuesRestriction(
			ContextEvent.PROP_CONTEXT_PROVIDER,
			ContextProvider.MY_URI),
		new String[] { ContextEvent.PROP_CONTEXT_PROVIDER,
			ContextProvider.PROP_CONTEXT_PROVIDER_TYPE }));
	// TODO: Test "OR" patterns (using several different CEPs)
	sub = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cepA });
	logInfo("Created Context Subscriber", null);

	// Correctly create a DefCP with full info
	ContextProvider info = new ContextProvider();
	info.setType(ContextProviderType.gauge);
	ContextEventPattern cep2 = new ContextEventPattern();
	cep2.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT,
		new Resource(DUMMYUSER + "right")));
	cep2.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));
	info.setProvidedEvents(new ContextEventPattern[] { cep2 });
	// info.setContextSources(new ManagedIndividual[]{new
	// ManagedIndividual(SOURCE)}); //Until we can use outer classes...
	ContextPublisher pub = new DefaultContextPublisher(
		ContextBusImpl.moduleContext, info);
	logInfo("Created Default Context Publisher with full Provider Info",
		null);

	// Create and send first event
	Integer retrieved = null;
	ContextEvent cev1 = ContextEvent.constructSimpleEvent(DUMMYUSER
		+ "right", USER, HAS_LOCATION, LOCATION);
	pub.publish(cev1);
	try {
	    retrieved = (Integer) queue.poll(1000, TimeUnit.MILLISECONDS);
	    Assert.notNull(retrieved, "Not received the expected good event");
	} catch (InterruptedException e) {
	    logError(e, "Something bad happened %s", e.toString());
	    Assert.notNull(null);
	}

	// Create and send second wrong event
	retrieved = null;
	ContextEvent cev2 = ContextEvent.constructSimpleEvent(DUMMYUSER
		+ "wrong", USER, HAS_LOCATION, LOCATION);
	pub.publish(cev2);
	try {
	    retrieved = (Integer) queue.poll(1000, TimeUnit.MILLISECONDS);
	    Assert.isNull(retrieved, "Wrongly received a bad context event");
	} catch (InterruptedException e) {
	    logError(e, "Something bad happened %s", e.toString());
	    Assert.notNull(null);
	}

	// Close subscriber
	sub.close();
    }

    public void testSubscriptions() {
	logInfo("-Test 6-", null);
	OntologyManagement.getInstance().register(new TestOntology());

	SyncContextSubscriber c1, c2, c3, c4, c5, c6, c7;
	ContextPublisher cpublisher = null;

	ContextEventPattern cep1 = new ContextEventPattern();
	cep1.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_SUBJECT, User.MY_URI));
	c1 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep1 }, 1);

	ContextEventPattern cep2 = new ContextEventPattern();
	cep2.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_SUBJECT, User.MY_URI));
	cep2.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, User.PROP_PHYSICAL_LOCATION));
	c2 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep2 }, 2);

	ContextEventPattern cep3 = new ContextEventPattern();
	cep3.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_SUBJECT, User.MY_URI));
	cep3.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, User.PROP_PHYSICAL_LOCATION));
	cep3.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_OBJECT, Location.MY_URI));
	c3 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep3 }, 3);

	ContextEventPattern cep4 = new ContextEventPattern();
	cep4.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, User.PROP_PHYSICAL_LOCATION));
	cep4.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_OBJECT, Location.MY_URI));
	c4 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep4 }, 4);

	ContextEventPattern cep5 = new ContextEventPattern();
	cep5.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_SUBJECT, User.MY_URI));
	cep5.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_OBJECT, Location.MY_URI));
	c5 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep5 }, 5);

	ContextEventPattern cep6 = new ContextEventPattern();
	cep6.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, User.PROP_PHYSICAL_LOCATION));
	c6 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep6 }, 6);

	ContextEventPattern cep7 = new ContextEventPattern();
	cep7.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_OBJECT, Location.MY_URI));
	c7 = new SyncContextSubscriber(ContextBusImpl.moduleContext,
		new ContextEventPattern[] { cep7 }, 7);

	User usr = new User(DUMMYUSER);
	usr.setLocation(new Location(LOCATION));
	ContextEvent e = new ContextEvent(usr, User.PROP_PHYSICAL_LOCATION);

	ContextProvider p = new ContextProvider();
	p.setType(ContextProviderType.gauge);
	p.setProvidedEvents(new ContextEventPattern[] { cep3 });
	cpublisher = new DefaultContextPublisher(ContextBusImpl.moduleContext,
		p);

	Integer retrieved = null;
	ArrayList good = new ArrayList(7);
	cpublisher.publish(e);
	try {
	    for (int i = 0; i < 7; i++) {
		retrieved = (Integer) queue.poll(1000, TimeUnit.MILLISECONDS);
		if (retrieved != null) {
		    good.add(retrieved);
		}
		retrieved = null;
	    }
	    Assert.isTrue(!good.isEmpty(),
		    "Not received an expected subscribed event. Received subscriptions "
			    + good);
	} catch (InterruptedException ex) {
	    logError(ex, "Something bad happened %s", ex.toString());
	    Assert.notNull(null);
	}
    }

    protected class DummyContextPublisher extends ContextPublisher {

	protected DummyContextPublisher(ModuleContext context,
		ContextProvider providerInfo) {
	    super(context, providerInfo);
	}

	public void communicationChannelBroken() {
	    logInfo("Publisher: Communication channel broken", null);
	}
    }

    protected class DummyContextSubscriber extends ContextSubscriber {

	protected DummyContextSubscriber(ModuleContext context,
		ContextEventPattern[] initialSubscriptions) {
	    super(context, initialSubscriptions);
	}

	public void communicationChannelBroken() {
	    logInfo("Subscriber: Communication channel broken", null);
	}

	public void handleContextEvent(ContextEvent event) {
	    logInfo("Received an event in subscriber SUBJECT: %s", event);
	}

    }

    protected class SyncContextSubscriber extends ContextSubscriber {
	private int ind = 0;

	protected SyncContextSubscriber(ModuleContext context,
		ContextEventPattern[] initialSubscriptions) {
	    super(context, initialSubscriptions);
	}

	protected SyncContextSubscriber(ModuleContext context,
		ContextEventPattern[] initialSubscriptions, int index) {
	    super(context, initialSubscriptions);
	    ind = index;
	}

	public void communicationChannelBroken() {
	    logInfo("Subscriber: Communication channel broken", null);
	}

	public void handleContextEvent(ContextEvent event) {
	    try {
		boolean sent = queue.offer(new Integer(ind), 1000,
			TimeUnit.MILLISECONDS);
		if (!sent) {
		    logError(null, "Received event but was not expected", event);
		}
	    } catch (InterruptedException e) {
		logError(e, "Something bad happened %s", e.toString());
	    }
	    logInfo("Received an event in subscriber SUBJECT: %s", event
		    .getRDFSubject());
	    logInfo("Received an event in subscriber PREDICATE: %s", event
		    .getRDFPredicate());
	    logInfo("Received an event in subscriber OBJECT: %s", event
		    .getRDFObject());
	}

    }

}
