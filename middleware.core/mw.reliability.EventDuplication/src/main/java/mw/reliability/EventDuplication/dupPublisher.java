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

 * Event Duplication Duplicator. 
 * @author <a href="mailto:zaher.owda@uni-siegen.de">Zaher Owda</a>  
 *		©2012
 */
 
package mw.faultTolerance.EventDuplication;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.owl.ContextProvider;

public class dupPublisher extends CPublisher{
	protected static int replicaCounter = 0;
	
	/**
	* In this function, we republich the events and dupliucate them
	* @param	context
	*			to fix the class inheritance parameters 
	* @param	providerInfo
	*			to fix the class inheritance parameters  
	* @param	context
	*			recieved event from ContextBus 
	**/	
	protected dupPublisher(ModuleContext context, ContextProvider providerInfo, ContextEvent event) {
		super(context, providerInfo);
		//publish TMR
		while(replicaCounter < 2){
			try{
				this.publish(event);
			}catch(Exception e){
				//TODO: handle exception
				System.err.print("Publishing events failed" + e.getMessage());
			}
			replicaCounter++;
		}
	}
}