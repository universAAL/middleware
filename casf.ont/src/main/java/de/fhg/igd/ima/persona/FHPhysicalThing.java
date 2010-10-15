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
package de.fhg.igd.ima.persona;

import org.persona.middleware.MiddlewareConstants;
import org.persona.ontology.Location;
import org.persona.ontology.ManagedIndividual;
import org.persona.ontology.expr.Restriction;
import org.persona.platform.casf.ontology.PhysicalThing;

import de.fhg.igd.ima.persona.location.Place;
import de.fhg.igd.ima.persona.shape.Box;
import de.fhg.igd.ima.persona.shape.Shape;

/**
 * Represents the class of physical things that are supposed to have a location.
 * 
 * @author mtazari
 *
 */
public class FHPhysicalThing extends PhysicalThing {
	
	public static final String MY_URI;

	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "FHPhysicalThing";
		register(FHPhysicalThing.class);
	}
	
	/**
	 * 
	 * @param propURI
	 * @return
	 */
	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		return PhysicalThing.getClassRestrictionsOnProperty(propURI);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String[] getStandardPropertyURIs() {
		String[] inherited = PhysicalThing.getStandardPropertyURIs();
		String[] toReturn = new String[inherited.length];
		int i = 0;
		while (i < inherited.length) {
			toReturn[i] = inherited[i];
			i++;
		}
		return toReturn;
	}

	/**
	 * Returns a human readable description on the essence of this ontology class.
	 */
	public static String getRDFSComment() {
		return "The root class for all physical things in the PERSONA ontology. Physical things have a location";
	}

	/**
	 * Returns a label with which this ontology class can be introduced to human users.
	 */
	public static String getRDFSLabel() {
		return "FPhysicalThing";
	}
	
	/**
	 * 
	 */
	protected FHPhysicalThing() {
		super();
	}
	
	/**
	 * 
	 * @param uri
	 */
	public FHPhysicalThing(String uri) {
		super(uri);
	}
	
	/**
	 * 
	 * @param uriPrefix
	 * @param numProps
	 */
	protected FHPhysicalThing(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}

	/**
	 * will return an anon place. for getting the real containing location, get
	 * the containing location of this anon place. The shape of the place is the shape
	 * of this physical Thing.
	 * 
	 * @return
	 */
	public Location getLocation() {
		return (Location) props.get(PROP_PHYSICAL_LOCATION);
	}
	
	/**
	 * 
	 */
	public Shape getShape() {
		return (Shape) props.get(PROP_HAS_SHAPE);
	}

	/**
	 * From the point of view of this top most class of things with a location, the location
	 * can be represented in its reduced form. As the class has no other property, for all
	 * other input, we return {@link ManagedIndividual#PROP_SERIALIZATION_OPTIONAL}. 
	 * 
	 * @see ManagedIndividual#getPropSerializationType(String).
	 */
	public int getPropSerializationType(String propURI) {
		return super.getPropSerializationType(propURI);
	}

	/**
	 * 
	 * Defines, that the PhysicalThing is located in location o at point x,y,z
	 * Warning: This does not set the PROP_PHYSICAL_LOCATION property to o, but
	 * to an new anon place, that is "contained in" the given location
	 * Note: the coordinates are defining the center of the PhysicalThing
	 * relative to the center of the containing location
	 * 
	 * @param o the location
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLocation(Place o,float x,float y,float z) {
		if(o != null) {
			Place place = new Place(this.getShape());
			if(o instanceof Place) ((Place)o).addContainedLocation(place,x,y,z);
			else place.setContainingLocation(place, x, y, z);
			place.setPhysicalThingofLocation(this);
			props.put(PROP_PHYSICAL_LOCATION, place);
		}
		else props.put(PROP_PHYSICAL_LOCATION, null);
	}
	
	/**
	 * 
	 */
	public void setLocation(Location loc) {
		if(loc == null)throw new IllegalArgumentException();
		props.put(PROP_PHYSICAL_LOCATION, null);
	}
	
	/**
	 * 
	 * Defines, that the PhysicalThing is located in location o at point x,y,z
	 * Warning: This does not set the PROP_PHYSICAL_LOCATION property to o, but
	 * to a new anon place, that is "contained in" the given location
	 * Note: the coordinates are defining center of the PhysicalThing
	 * relative to the lower, left corner of the containing location, assuming its a box
	 * 
	 * @param o the location
	 * @param x
	 * @param y
	 * @param z
	 */
	// TODO: what is the real meaning of the coordinates?
	public void setLocationRelativeToCorner(Location o,float x,float y,float z) {
		if(o != null) {
			Place place = new Place(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + this.getLocalName() + "_place", this.getShape());
			
			if(o instanceof Place) {
				// assumed, the shape of the place is a box. that needn't be the case
				Box box = (Box)((Place)o).getShape();
				((Place)o).addContainedLocation(place,(float)(x-box.getWidth()/2f),(float)(y-box.getDepth()/2f),(float)(z-box.getHeight()/2f));
			}
			else
				place.setContainingLocation(place, x, y, z);
			
			// associating the location of the furniture and the furniture
			place.setPhysicalThingofLocation(this);
			props.put(PROP_PHYSICAL_LOCATION, place);
		}
		else
			props.put(PROP_PHYSICAL_LOCATION, null);
	}
	
	/**
	 * 
	 * Defines, that the PhysicalThing is located in location o at point x,y,z
	 * Warning: This does not set the PROP_PHYSICAL_LOCATION property to o, but
	 * to an new anon place, that is "contained in" the given location
	 * Note: the coordinates are defining corner of the PhysicalThing
	 * relative to the lower, left corner of the containing location, assuming its a box
	 * 
	 * @param o the location
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLocationCornerRelativeToCorner(Location o,float x,float y,float z) {
		if(o != null) {
			Place place = new Place(this.getShape());
			if(o instanceof Place) {
				Box box = (Box)((Place)o).getShape();
				Box box2 = (Box)this.getShape().getBoundingVolume();
				((Place)o).addContainedLocation(place,(float)(x-box.getWidth()/2f+box2.getWidth()/2f),(float)(y-box.getDepth()/2f+box2.getDepth()/2f),(float)(z-box.getHeight()/2f+box2.getHeight()/2f));
			}
			else place.setContainingLocation(place, x, y, z);
			place.setPhysicalThingofLocation(this);
			props.put(PROP_PHYSICAL_LOCATION, place);
		}
		else props.put(PROP_PHYSICAL_LOCATION, null);
	}
	
	/**
	 * 
	 */
	public void setShape(Shape o) {
		if (o != null)
			props.put(PROP_HAS_SHAPE, o);
	}

}
