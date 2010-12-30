/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.universAAL.middleware.util;

/**
 * @author mtazari
 *
 */
public class IntAggregator {
	
	private int avg, min, max;
	private int num = 0, sum = 0;
	
	public IntAggregator() {
		
	}
	
	public IntAggregator addVote(int vote) {
		sum += vote;
		num++;
			
		if (num == 1)
			max = min = vote;
		else if (max < vote)
			max = vote;
		else if (min > vote)
			min = vote;

		avg = sum / num;
		
		return this;
	}
	
	public int getAverage() {
		return avg;
	}
	
	public int getMax() {
		return max;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getNumberOfVotes() {
		return num;
	}
}
