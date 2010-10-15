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
package org.persona.middleware.util;

import org.persona.ontology.Rating;

/**
 * @author mtazari
 *
 */
public class RatingAggregator {
	
	private Rating avg, min, max;
	private int num = 0, sum = 0;
	
	public RatingAggregator() {
		
	}
	
	public RatingAggregator addRating(Rating r) {
		if (r != null) {
			sum += r.ord();
			num++;
			
			if (num == 1)
				max = min = r;
			else if (max.ord() < r.ord())
				max = r;
			else if (min.ord() > r.ord())
				min = r;

			int a = sum / num;
			if ((sum % num) << 1 >= num)
				a++;
			avg = Rating.getRatingByOrder(a);
		}
		
		return this;
	}
	
	public Rating getAverage() {
		return avg;
	}
	
	public Rating getMax() {
		return max;
	}
	
	public Rating getMin() {
		return min;
	}
	
	public int getNumberOfRatings() {
		return num;
	}
}
