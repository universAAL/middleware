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

 * TMR Event Handler Class. 
 * @author <a href="mailto:zaher.owda@uni-siegen.de">Zaher Owda</a>  
 *		©2012
 */

package org.universAAL.mw.faultTolerance.redundancy;

import org.universAAL.middleware.context.ContextEvent;

public class TMReventHandler {
	
	/**
	* This class handles sets of raplicas and return results needed accordingly 
	**/
	private static long evTimeOut;
	private ContextEvent [] retEvent = new ContextEvent[3];
	
	/**
	* In this function, set the voting time out for each set of replicas
	* @param	inTiOut
	*			time out calculated fot the set of replicas
	**/
	public void setTimeOut(long inTiOut){
		if(retEvent[0] == null)
			this.evTimeOut = inTiOut;
	}
	
	/**
	* In this function, return the time-out for each set of replicas
	* @return 
	*		return the time-out
	**/
	public long getTimeOut(){
		return evTimeOut;
	}
	
	/**
	* In this function, add new even to the a set of replicas
	* @param event
	*		event to be added
	**/
	public void addEvent(ContextEvent event){
		if(retEvent[0] == null)
			this.retEvent[0] = event;
		else if(retEvent[0].getRDFObject() == event.getRDFObject() && retEvent[1] == null && retEvent[0] !=null)
			this.retEvent[1] = event;
		else if(retEvent[0] !=null && retEvent[1] !=null && retEvent[1].getRDFObject() == event.getRDFObject())
			this.retEvent[2] = event;
	}
	
	/**
	* In this function, return a specific event
	* @return 
	*		return the event
	**/
	public ContextEvent getEvent(int index){
			return retEvent[index];
	}
}