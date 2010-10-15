package org.persona.serialization.turtle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.persona.middleware.TypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.igd.ima.sodapop.msg.MessageContentSerializer;

/**
 * The Jena ontology factory is an implementation of {@link MessageContentSerializer}
 * using JENA as the underlying tool.
 * 
 * @author mtazari
 * 
 */
public class Activator implements BundleActivator, ServiceListener {
	
	static BundleContext context = null;
	static final Logger logger = LoggerFactory.getLogger(Activator.class);
	TurtleParser ser;
	
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		
		ser = new TurtleParser();
		
		context.registerService(
				new String[] {MessageContentSerializer.class.getName()},
				ser, null);
		
		String filter = "(objectclass=" + org.persona.middleware.TypeMapper.class.getName() + ")";
		context.addServiceListener(this, filter);
		ServiceReference references[] = context.getServiceReferences(null, filter);
		for (int i = 0; references != null && i < references.length; i++)
			this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, references[i]));
				
	}

	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub	
	}

	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
			TurtleUtil.typeMapper = (TypeMapper) context.getService(event.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			TurtleUtil.typeMapper = null;
			break;
		}		
	}
}
