/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	
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
package org.universAAL.middleware.owl;

/**
 * Represents the class of individuals that can be compared to other individuals for finding out their
 * (partial) order. The conventions described for the class hierarchy rooted at {@link
 * org.universAAL.middleware.owl.ManagedIndividual}; in particular, the subclasses must override the following
 * non-final static methods: {@link #getMaximum()} and {@link #getMinimum()}.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * @author Carsten Stockloew
 */
public abstract class ComparableIndividual extends ManagedIndividual implements Comparable {
	/**
	 * To be used in the implementation of {@link java.lang.Comparable#compareTo(Object)},
	 * if the passed parameter is not comparable with the current individual.
	 */
	public static final int NON_COMPARABLE_INDIVIDUALS = Integer.MAX_VALUE;
	
	public static final String MY_URI;
	static {
		MY_URI = uAAL_VOCABULARY_NAMESPACE + "ComparableIndividual";
		register(ComparableIndividual.class);
	}
	
	protected ComparableIndividual() {
		super();
	}
	
	protected ComparableIndividual(String uri) {
		super(uri);
	}
	
	public static final ComparableIndividual getClassMaximum(Class claz) {
		// TODO: change to getMaxValue?
		try {
			return (ComparableIndividual) claz.getMethod("getMaximum", null).invoke(null, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static final ComparableIndividual getClassMinimum(Class claz) {
		// TODO: change to getMinValue?
		try {
			return (ComparableIndividual) claz.getMethod("getMinimum", null).invoke(null, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the "largest" instance in this class, if it exists.
	 */
	public static ComparableIndividual getMaximum() {
		return null;
	}
	
	/**
	 * Returns the "smallest" instance in this class, if it exists.
	 */
	public static ComparableIndividual getMinimum() {
		return null;
	}
	
	/**
	 * Returns a human readable description on the essence of this ontology class.
	 */
	public static String getRDFSComment() {
		return "The root class for all comparable individuals in the uAAL ontology.";
	}
	
	/**
	 * Returns a label with which this ontology class can be introduced to human users.
	 */
	public static String getRDFSLabel() {
		return "Comparable Individual";
	}
	
	/**
	 * Compare this object to the given object.
	 * 
	 * @return 	<b>zero </b>if current location and argument location are in the same Place.<br>
	 * 			<b> -1 </b> if current location is in a Place contained in argument location Place.<br>
	 * 			<b>1 </b>	if argument location is in a Place contained in current location Place.<br>
	 * 			{@link ComparableIndividual#NON_COMPARABLE_INDIVIDUALS} if two locations are not comparable.
	 */
	public abstract int compareTo(Object arg0);
	
	public final ComparableIndividual getClassMaximum() {
		try {
			return (ComparableIndividual) this.getClass().getMethod(
					"getMaximum", null).invoke(null, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	public final ComparableIndividual getClassMinimum() {
		try {
			return (ComparableIndividual) this.getClass().getMethod(
					"getMinimum", null).invoke(null, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	/** Get the next value according to the total order of this comparable individual. */
	public abstract ComparableIndividual getNext();
	
	/** Get the previous value according to the total order of this comparable individual. */
	public abstract ComparableIndividual getPrevious();
	
	
	/** Determines if this object equals the given object. */
	public final boolean equal(Object other) {
		try {
			return compareTo(other) == 0;
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Determines if this object is greater than the given object. */
	public final boolean greater(Object other) {
		try {
			return compareTo(other) == 1;
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Determines if this object is greater than or equals the given object. */
	public final boolean greaterEqual(Object other) {
		try {
			switch (compareTo(other)) {
			case 0:
			case 1:
				return true;
			default:
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Determines if this object is smaller than the given object. */
	public final boolean less(Object other) {
		try {
			return compareTo(other) == -1;
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Determines if this object is smaller than or equals the given object. */
	public final boolean lessEqual(Object other) {
		try {
			switch (compareTo(other)) {
			case 0:
			case -1:
				return true;
			default:
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * If there is a total strict order between the class members, then it must return the "serial number"
	 * of this instance, otherwise Integer.MIN_VALUE must be returned.
	 */
	public abstract int ord();

}
