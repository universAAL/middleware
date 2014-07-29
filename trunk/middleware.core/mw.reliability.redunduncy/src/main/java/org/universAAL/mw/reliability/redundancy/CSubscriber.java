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

 * TMR Event Handler subscriber. 
 * @author <a href="mailto:zaher.owda@uni-siegen.de">Zaher Owda</a>  
 *		©2012
 */

package org.universAAL.mw.faultTolerance.redundancy;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.owl.MergedRestriction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;


public class CSubscriber extends ContextSubscriber {
	
	protected static final int timeOut = 100;
	
	protected static final String myURI = "http://ontology.itaca.upv.es/Test.owl#tempsensor8tempsensor8";

	protected List<TMReventHandler>TMRevManager = new ArrayList<TMReventHandler>();
	
    protected CSubscriber(ModuleContext context,ContextEventPattern[] initialSubscriptions) {
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
	* In this function, the TMR events handler is implemented
	* @param	event
	*			recieved event from ContextBus 
	**/
    public void handleContextEvent(ContextEvent event) {

    	Date currentDate = new Date();
        long msec = currentDate.getTime(); 
        if(checkValues(event,TMRevManager) == false){
        	TMReventHandler newEv = new TMReventHandler();
	        newEv.setTimeOut(msec+timeOut);
	        newEv.addEvent(event);
	        TMRevManager.add(newEv);
        }
        ContextEvent evt = TMRVoter(msec,TMRevManager);	// call for voting
        if(evt !=null)
        	System.out.println("------Event object:" + evt.getRDFObject());
    }
    
	/**
	* In this function, the TMR voter is implemented
	* @param	msec
	*			timestamp tagged to each event when recieved 
	* @param	TMRevManager
	*			list of events recieved
	* @return	ContextEvent 
	*			voting result
	**/
	
    public ContextEvent TMRVoter(long msec, List<TMReventHandler> TMRevManager){

		if(TMRevManager.size()>0){
			//TODO
			Iterator<TMReventHandler> itr = TMRevManager.iterator(); 
			int i=0;
			while(itr.hasNext()) {
				TMReventHandler element = itr.next(); 
				if(element.getTimeOut()>=msec){	//within timeout
					if(element.getEvent(0)!=null && element.getEvent(1)!=null && element.getEvent(2)!=null){	// have 3 replicas within time out.
						//System.out.println("------have 3 replica within time out, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
							 if(element.getEvent(0).getRDFObject() == element.getEvent(1).getRDFObject() && element.getEvent(1).getRDFObject() == element.getEvent(2).getRDFObject()){
								 ContextEvent ret = element.getEvent(0);
								 TMRevManager.remove(i);
								 return  ret;
							 }
					}
					if(element.getEvent(0)!=null && element.getEvent(1)!=null && element.getEvent(2)==null){	// have 2 replicas within time out.
						//System.out.println("------have 2 replica within time out, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
							 if(element.getEvent(0).getRDFObject() == element.getEvent(1).getRDFObject()){ //similar replicas
								 ContextEvent ret = element.getEvent(0);
								 TMRevManager.remove(i);
								 return  ret;
							 }
							 else{	// have 2 different replicas and time out is not over, then wait for the third one
									try {
										Thread.sleep(1);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							    	Date currentDate = new Date();
							        long msec0 = currentDate.getTime();
							        return TMRVoter(msec0,TMRevManager);								 
							 }
					}
					if(element.getEvent(0)!=null && element.getEvent(1)==null && element.getEvent(2)==null){	//have 1 replica within time out. then wait
						//System.out.println("------have 1 replica within time out. then wait, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	Date currentDate = new Date();
				        long msec1 = currentDate.getTime();
				        return TMRVoter(msec1,TMRevManager);
					}
				}
				else if(element.getTimeOut()<msec){	//time out is over
					if(element.getEvent(0)!=null && element.getEvent(1)!=null && element.getEvent(2)!=null){	// have 3 replicas within time out.
							 if(element.getEvent(0).getRDFObject() == element.getEvent(1).getRDFObject() && element.getEvent(1).getRDFObject() == element.getEvent(2).getRDFObject()){
							//System.out.println("------have 3 replica, time out is over.");
								 ContextEvent ret = element.getEvent(0);
								 TMRevManager.remove(i);
								 return  ret;
							 }
					}
					else if(element.getEvent(0)!=null && element.getEvent(1)!=null && element.getEvent(2)==null){	// have 2 replicas within time out.
						//System.out.println("------have 2 replica , time out is over. ");
							 if(element.getEvent(0).getRDFObject() == element.getEvent(1).getRDFObject()){ //similar replicas
								 ContextEvent ret = element.getEvent(0);
								 TMRevManager.remove(i);
								 return  ret;
							 }
					}
					else if(element.getEvent(0)!=null && element.getEvent(1)==null && element.getEvent(2)==null){	// have 1 replica, time out is over.
						//System.out.println("------ have 1 replica, time out is over. time out: "+ element.getTimeOut()+ ", msec: "+ msec);
						ContextEvent ret = element.getEvent(0);
						TMRevManager.remove(i);
						return ret;
					}
					else{
						//System.out.println("------ replica are different, replica omission, time out: "+ element.getTimeOut()+ ", msec: "+ msec);
						TMRevManager.remove(i);
				    	return null;
					}
				}
				i++;
			}

		}
       	return null;
    }
    
	/**
	* In this function, compare the replicas 
	* @param	event
	*			recieved event from ContextBus
	* @param	evManger
	*			list of events recieved
	* @return	boolean 
	*			comparison result, fault: if it does not exist aleardy 
	**/
	public boolean checkValues(ContextEvent event, List<TMReventHandler> evManger){
		if(evManger.size()>0){
			Iterator<TMReventHandler> itr = evManger.iterator(); 
			int i=1;
			while(itr.hasNext() && i == 1) {
				TMReventHandler element = itr.next(); 
				if(element.getEvent(0)!=null && element.getEvent(1)==null){
					if(element.getEvent(0).getRDFObject() == event.getRDFObject() && element.getEvent(0).getProvider() != event.getProvider()){ // Second replica has same value from different providers
						element.addEvent(event);
						i = 0;
						return true;
					}
				}
				else if(element.getEvent(0)!=null && element.getEvent(1)!=null){
					if(element.getEvent(1).getRDFObject() == event.getRDFObject() && element.getEvent(1).getProvider() != event.getProvider()){ //third replica has same value from different providers
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
