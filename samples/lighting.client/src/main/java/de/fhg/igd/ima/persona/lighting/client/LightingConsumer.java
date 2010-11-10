/**
 * 
 */
package de.fhg.igd.ima.persona.lighting.client;

import org.persona.platform.casf.ontology.device.Device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.persona.middleware.context.ContextEvent;
import org.persona.middleware.context.ContextEventPattern;
import org.persona.middleware.context.ContextSubscriber;
import org.persona.middleware.service.CallStatus;
import org.persona.middleware.service.DefaultServiceCaller;
import org.persona.middleware.service.PropertyPath;
import org.persona.middleware.service.ServiceCaller;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.service.ServiceResponse;
import org.persona.middleware.service.process.ProcessOutput;
import org.persona.ontology.expr.Restriction;
import de.fhg.igd.ima.persona.location.FHLocation;
import org.persona.platform.casf.ontology.device.lighting.LightSource;
import org.persona.platform.casf.ontology.device.lighting.Lighting;


/**
 * @author amarinc
 *
 */
class LightingConsumer extends ContextSubscriber {
	
	private static ServiceCaller caller;
	
	private static final String LIGHTING_CONSUMER_NAMESPACE = "http://ontology.igd.fhg.de/LightingConsumer.owl#";
	
	private static final String OUTPUT_LIST_OF_LAMPS = LIGHTING_CONSUMER_NAMESPACE + "controlledLamps";

	
	private static ContextEventPattern[] getContextSubscriptionParams() {
		// I am interested in all events with a light source as subject
		ContextEventPattern cep = new ContextEventPattern();
		cep.addRestriction(Restriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT,
				LightSource.MY_URI));
		return new ContextEventPattern[] {cep};
	}
	
	static {

		// force the JVM to load the class location classes
		FHLocation.getClassRestrictionsOnProperty(null);
		FHLocation.getClassRestrictionsOnProperty(null);
	}
	
	LightingConsumer(BundleContext context){
		
		super(context,getContextSubscriptionParams());


		caller = new DefaultServiceCaller(context);
		
		Device[] d = getControlledLamps();
		LightClient c = new LightClient();
	}
	
	
	// *****************************************************************
	// Services Requests
	// *****************************************************************
	
	private static ServiceRequest turnOffRequest(String lampURI){
		ServiceRequest turnOff = new ServiceRequest( new Lighting(), null);
		
		turnOff.getRequestedService()
			.addInstanceLevelRestriction(
					Restriction.getFixedValueRestriction(								
					Lighting.PROP_CONTROLS, new LightSource(
							lampURI)),
							new String[] { Lighting.PROP_CONTROLS });
		turnOff.addChangeEffect(
				new PropertyPath(null, true, new String[] {
						Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_BRIGHTNESS}),
				new Integer(0));
			return turnOff;
	}
	
	private static ServiceRequest turnOnRequest(String lampURI){
		ServiceRequest turnOn = new ServiceRequest( new Lighting(), null);
		
		turnOn.getRequestedService()
			.addInstanceLevelRestriction(
					Restriction.getFixedValueRestriction(								
					Lighting.PROP_CONTROLS, new LightSource(
							lampURI)),
							new String[] { Lighting.PROP_CONTROLS });
		turnOn.addChangeEffect(
				new PropertyPath(null, true, new String[] {
						Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_BRIGHTNESS}),
				new Integer(100));
			return turnOn;
	}
	
	private static ServiceRequest dimRequest(String lampURI, Integer percent){
		ServiceRequest dim = new ServiceRequest( new Lighting(), null);
		
		dim.getRequestedService()
			.addInstanceLevelRestriction(
					Restriction.getFixedValueRestriction(								
					Lighting.PROP_CONTROLS, new LightSource(
							lampURI)),
							new String[] { Lighting.PROP_CONTROLS });		
		dim.addChangeEffect(
				new PropertyPath(null, true, new String[] {
						Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_BRIGHTNESS}),
				percent);
		
			return dim;
	}
	
	public  static  ServiceRequest getAllLampsRequest() {
		ServiceRequest getAllLamps = new ServiceRequest(
				new Lighting(), null);
	
		getAllLamps.addSimpleOutputBinding(new ProcessOutput(
				OUTPUT_LIST_OF_LAMPS), new PropertyPath(null, true,
				new String[] { Lighting.PROP_CONTROLS }));
	
		return getAllLamps;
	}
	
	// *****************************************************************
	// Controller Methods
	// *****************************************************************
	
	public static Device[] getControlledLamps(){
		
		ServiceResponse sr = caller.call(getAllLampsRequest());
		
		if (sr.getCallStatus() == CallStatus.succeeded){
			try {
				List lampList = new ArrayList();

				List outputs = sr.getOutputs();

				if (outputs == null || outputs.size() == 0) {
//					Activator.log.log(LogService.LOG_ERROR,"LocalVideoStreamMultiplexer:   No outputs available");
					System.out.println("LightingConsumer:   outputs are null in getControlledLamps()");
					return null;
				}

				for (Iterator iter1 = outputs.iterator(); iter1.hasNext();) {
					Object obj = iter1.next();
					if(obj instanceof List){
						List outputLists = (List)obj;
						for(Iterator iter2 = outputLists.iterator(); iter2.hasNext();){
							
							ProcessOutput output = (ProcessOutput) iter2.next();
							if (output.getURI().equals(OUTPUT_LIST_OF_LAMPS)) {
								Object ob = output.getParameterValue();
								if(!(ob instanceof List))
									break;
								List lamps = (List) ob;
								lampList.addAll(lamps);
							}
						}
					}else{
						ProcessOutput output = (ProcessOutput) obj;
						
						if (output.getURI().equals(OUTPUT_LIST_OF_LAMPS)) {
							Object ob = output.getParameterValue();
							if(!(ob instanceof List))
								break;
							List lamps = (List)ob;
							lampList.addAll(lamps);
						}
					}
					
				}

				if (lampList.size() == 0) {
					System.out.println("LightingConsumer:   there are no results for lamps in getControlledLamps()");
					return null;
				}

				LightSource[] lamps = (LightSource[]) lampList.toArray(new LightSource[lampList.size()]);

				return lamps;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			System.out.println("LightingConsumer:   callstatus is not succeeded in getControlledLamps()");
			return null;
		}
	}
	
	public static boolean turnOff(String lampURI){
		
		if((lampURI == null) || !(lampURI instanceof String)) {
			System.out.println("LightingConsumer: wrong lampURI in turnOff(String lampURI)");
			return false;
		}
		
		ServiceResponse sr = caller.call(turnOffRequest(lampURI));
		System.out.println(sr.getCallStatus());
		
		if (sr.getCallStatus() == CallStatus.succeeded) return true;
		else{
			System.out.println("LightingConsumer: the lamp couldn't turned off in turnOff(String lampURI)");
			return false;
		}
	}
	
	public static boolean turnOn(String lampURI){
		
		if((lampURI == null) || !(lampURI instanceof String)) {
			System.out.println("LightingConsumer: wrong lampURI in turnOn(String lampURI)");
			return false;
		}
		
		ServiceResponse sr = caller.call(turnOnRequest(lampURI));
		
		if (sr.getCallStatus() == CallStatus.succeeded) return true;
		else{
			System.out.println("LightingConsumer: the lamp couldn't turned on in turnOn(String lampURI)");
			return false;
		}
	}
	
	public static boolean dimToValue(String lampURI, Integer percent){
		
		if((lampURI == null) || (percent == null) || !(lampURI instanceof String) || !(percent instanceof Integer)) {
			System.out.println("LightingConsumer: wrong inputs in dimToValue(String lampURI, Integer percent)");
			return false;
		}
		
		ServiceResponse sr = caller.call(dimRequest(lampURI, percent));
		
		if (sr.getCallStatus() == CallStatus.succeeded) return true;
		else{
			System.out.println("LightingConsumer: the lamp couldn't dimmed to wanted value in dimToValue(String lampURI, integer percent)");
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.persona.middleware.context.ContextSubscriber#handleContextEvent(org.persona.middleware.context.ContextEvent)
	 */
	public void handleContextEvent(ContextEvent event) {
		Activator.log.log(LogService.LOG_INFO,
				"Received context event:\n" +
				"    Subject     ="+event.getSubjectURI()+"\n" +
				"    Subject type="+event.getSubjectTypeURI()+"\n" +
				"    Predicate   ="+event.getRDFPredicate()+"\n" +
				"    Object      ="+event.getRDFObject());
	}
	

	/* (non-Javadoc)
	 * @see org.persona.middleware.context.ContextSubscriber#communicationChannelBroken()
	 */
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub

	}

}
