/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut f�r Graphische Datenverarbeitung 
	
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

import org.universAAL.middleware.owl.supply.Rating;

/**
 * Utility class to calculate values (like average, minimum) from a consecutive
 * appearance of {@link org.universAAL.middleware.owl.supply.Rating} values.
 * 
 * @see IntAggregator
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * @author Carsten Stockloew
 */
public class RatingAggregator {
	
	/** The average. */
	private Rating avg;
	
	/** The minimum. */
	private Rating min;
	
	/** The maximum. */
	private Rating max;
	
	/** The number of Ratings. */
	private int num = 0;
	
	/** The sum. */
	private int sum = 0;
	
	
	/** Create a new instance. */
	public RatingAggregator() {
	}
	
	/** Add a new Rating. */
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
	
	/** Get the average value. */
	public Rating getAverage() {
		return avg;
	}
	
	/** Get the maximum value. */
	public Rating getMax() {
		return max;
	}
	
	/** Get the minimum value. */
	public Rating getMin() {
		return min;
	}
	
	/** Get the number of Ratings. */
	public int getNumberOfRatings() {
		return num;
	}
}
