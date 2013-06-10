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

 * Event Duplication Subscriber. 
 * @author <a href="mailto:zaher.owda@uni-siegen.de">Zaher Owda</a>  
 *		©2012
 */

package mw.faultTolerance.EventDuplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.owl.MergedRestriction;

public class CSubscriber extends ContextSubscriber {

	protected static final String myURI = "http://ontology.itaca.upv.es/Test.owl#tempsensor8tempsensor8";
	
	protected List<EventsManager>evManger = new ArrayList<EventsManager>();
	
    protected CSubscriber(ModuleContext context,
	    ContextEventPattern[] initialSubscriptions) {
	super(context, initialSubscriptions);
	// TODO Auto-generated constructor stub
    }

    protected CSubscriber(ModuleContext context) {
	super(context, getPermanentSubscriptions());
	// TODO Auto-generated constructor stub
    }

    private static ContextEventPattern[] getPermanentSubscriptions() {
        ContextEventPattern myContextEventPattern = new ContextEventPattern();
        myContextEventPattern.addRestriction(MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT, myURI));
        return new ContextEventPattern[] {myContextEventPattern};
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

	
	/**
	* In this function, the duplcated events handler is implemented
	* @param	event
	*			recieved event from ContextBus 
	**/
    public void handleContextEvent(ContextEvent event) {

    	Date currentDate = new Date();
        long msec = currentDate.getTime(); 
        if(checkEventTimeStamp(event,evManger) == false){
	        EventsManager newEv = new EventsManager();
	        newEv.setTimeOut(msec+100);
	        newEv.addEvent(event);
	        evManger.add(newEv);
        }

        ContextEvent evt = duplVoter(msec,evManger);	// call for voting
        if(evt !=null)
        	System.out.println("------Event object:" + evt.getRDFObject());

    }

	/**
	* In this function, the duplcated events voter is implemented
	* @param	msec
	*			timestamp tagged to each event when recieved 
	* @param	TMRevManager
	*			list of events recieved
	* @return	ContextEvent 
	*			voting result
	**/
    public ContextEvent duplVoter(long msec, List<EventsManager> evManger){

		if(evManger.size()>0){
			//TODO
			Iterator<EventsManager> itr = evManger.iterator(); 
			int i=0;
			while(itr.hasNext()) {
				EventsManager element = itr.next(); 
				if(element.getTimeOut()>=msec){	//within timeout
					if(element.getEvent(0)!=null && element.getEvent(1)!=null){	// have 2 events within time out.
						//System.out.println("------have 2 events within time out, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
							 if(element.getEvent(0).getRDFObject() == element.getEvent(1).getRDFObject()){
								 ContextEvent ret = element.getEvent(0);
								 evManger.remove(i);
								 return  ret;
							 }
					}
					if(element.getEvent(0)!=null && element.getEvent(1)==null){	//have 1 event within time out. then wait
						//System.out.println("------have 1 event within time out. then wait, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	Date currentDate = new Date();
				        long msec1 = currentDate.getTime();
				        return duplVoter(msec1,evManger);
					}
				}
				else if(element.getTimeOut()<msec){	//time out is over
					if(element.getEvent(0)!=null && element.getEvent(1)!=null){	// have 2 events, time out is over.
						//System.out.println("------have 2 events, time out is over.");
							 if(element.getEvent(0).getRDFObject() == element.getEvent(1).getRDFObject()){	//check for value faults
								 ContextEvent ret = element.getEvent(0);
								 evManger.remove(i);
								 return  ret;
							 }
					}
					else if(element.getEvent(0)!=null && element.getEvent(1)==null){	// have 1 event, time out is over.
						//System.out.println("------ have 1 event, time out is over. time out: "+ element.getTimeOut()+ ", msec: "+ msec);
						ContextEvent ret = element.getEvent(0);
						evManger.remove(i);
						return ret;
					}
					else{
						//System.out.println("------ Events are different, event omission, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
						evManger.remove(i);
				    	return null;
					}
				}
				i++;
			}

		}
       	return null;
    }
    
	/**
	* In this function, check the duplicated events timestamp 
	* @param	event
	*			recieved event from ContextBus
	* @param	evManger
	*			list of events recieved
	* @return	boolean 
	*			comparison result, fault: if it does not exist aleardy and we need to add it
	**/
	public boolean checkEventTimeStamp(ContextEvent event, List<EventsManager> evManger){
		if(evManger.size()>0){
			Iterator<EventsManager> itr = evManger.iterator(); 
			int i=1;
			while(itr.hasNext() && i == 1) {
				EventsManager element = itr.next(); 
				if(element.getEvent(0)!=null){
					if(element.getEvent(0).getTimestamp() == event.getTimestamp()){ //has same TS, should be added to the same object otherwise create new one
						element.addEvent(event);
						i = 0;
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}

}
