/**Copyright [2011-2014] [University of Siegen, Embedded System Instiute]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 * Event Duplication Publisher. 
 * @author <a href="mailto:zaher.owda@uni-siegen.de">Zaher Owda</a>  
 *		©2012
 */

package mw.faultTolerance.EventDuplication;


/* More on how to use this class at: 
 * http://forge.universaal.org/wiki/support:Developer_Handbook_6#Publishing_context_events */
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.ontology.phThing.Device;
import org.universAAL.ontology.weather.TempSensor;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.owl.supply.LevelRating;

public class CPublisher extends ContextPublisher {

	protected static final String URIROOT = "http://ontology.itaca.upv.es/Test.owl#";
	
    protected CPublisher(ModuleContext context, ContextProvider providerInfo) {
	super(context, providerInfo);
	// TODO Auto-generated constructor stub
    }

    protected CPublisher(ModuleContext context) {
		super(context, getProviderInfo());
		Device dev = new Device();
		//TempSensor sen = new TempSensor();
		TempSensor ts7 = new TempSensor(URIROOT + "tempsensor10");
		dev.setBatteryLevel(LevelRating.high);
		dev.setResourceLabel("102");
		ts7.setMeasuredValue(20);
		ContextEvent evt0 = new ContextEvent(dev, Device.PROP_RDFS_LABEL);
		ContextEvent evt10 = new ContextEvent(ts7, TempSensor.PROP_MEASURED_VALUE);
	
		this.publish(evt10);
	
		TempSensor ts6 = new TempSensor(URIROOT + "tempsensor8");
		dev.setBatteryLevel(LevelRating.high);
		dev.setResourceLabel("101");
		ts6.setMeasuredValue(25);
		ContextEvent evt = new ContextEvent(dev, Device.PROP_RDFS_LABEL);
		ContextEvent evt1 = new ContextEvent(ts6, TempSensor.PROP_MEASURED_VALUE);
		dupPublisher myRedPublisher =  new dupPublisher(context, getProviderInfo(), evt1);
	
    }

    private static ContextProvider getProviderInfo() {
    	ContextProvider cpinfo = new ContextProvider(URIROOT
    			+ "TestMassContextProvider");
    		cpinfo.setType(ContextProviderType.gauge);
    		cpinfo.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
    		return cpinfo;
	
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

}
