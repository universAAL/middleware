/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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

import java.util.Hashtable;

public interface TypeExpression {

    /**
     * Create a copy of this object, i.e. create a new object of this class and
     * copy the necessary properties.
     * 
     * @return The newly created copy.
     */
    public ClassExpression copy();

    /**
     * Get the set of class URIs for all super classes of the individuals of
     * this class expression.
     */
    public String[] getNamedSuperclasses();

    /**
     * Each class expression can contain multiple objects; this method returns
     * this set of objects.
     */
    public Object[] getUpperEnumeration();

    /**
     * Returns true if the given object is a member of the class represented by
     * this class expression, otherwise false. The <code>context</code> table
     * maps the URIs of certain variables onto values currently assigned to
     * them. The variables are either standard variables managed by the
     * universAAL middleware or parameters of a specific service in whose
     * context this method is called. Both the object whose membership is going
     * to be checked and this class expression may contain references to such
     * variables. If there is already a value assigned to such a referenced
     * variable, it must be replaced by the associated value, otherwise this
     * method may expand the <code>context</code> table by deriving a value for
     * such unassigned but referenced variables with which the membership can be
     * asserted. In case of returning true, the caller must check the size of
     * the <code>context</code> table to see if new conditions are added in
     * order for the membership to be asserted. If the <code>context</code>
     * table is null, the method does a global and unconditional check.
     * 
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
     */
    public boolean hasMember(Object member, Hashtable context);

    /**
     * Returns true if the given class expression has no member in common with
     * the class represented by this class expression, otherwise false. The
     * <code>context</code> table maps the URIs of certain variables onto values
     * currently assigned to them. The variables are either standard variables
     * managed by the universAAL middleware or parameters of a specific service
     * in whose context this method is called. Both of the class expressions may
     * contain references to such variables. If there is already a value
     * assigned to such a referenced variable, it must be replaced by the
     * associated value, otherwise this method may expand the
     * <code>context</code> table by deriving a value for such unassigned but
     * referenced variables with which the disjointness of the two classes can
     * be asserted. In case of returning true, the caller must check the size of
     * the <code>context</code> table to see if new conditions are added in
     * order for the disjointness to be asserted. If the <code>context</code>
     * table is null, the method does a global and unconditional check.
     * 
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
     */
    public boolean isDisjointWith(ClassExpression other, Hashtable context);

    /**
     * Returns true, if the state of the resource is valid, otherwise false.
     * Redefined in this class as abstract to force subclasses to override it.
     * 
     * @see org.universAAL.middleware.rdf.Resource#isWellFormed()
     */
    public boolean isWellFormed();

    /**
     * Returns true if the given class expression is a subset of the class
     * represented by this class expression, otherwise false. The
     * <code>context</code> table maps the URIs of certain variables onto values
     * currently assigned to them. The variables are either standard variables
     * managed by the universAAL middleware or parameters of a specific service
     * in whose context this method is called. Both of the class expressions may
     * contain references to such variables. If there is already a value
     * assigned to such a referenced variable, it must be replaced by the
     * associated value, otherwise this method may expand the
     * <code>context</code> table by deriving a value for such unassigned but
     * referenced variables with which the compatibility of the two classes can
     * be asserted. In case of returning true, the caller must check the size of
     * the <code>context</code> table to see if new conditions are added in
     * order for the compatibility to be asserted. If the <code>context</code>
     * table is null, the method does a global and unconditional check.
     * 
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
     */
    public boolean matches(ClassExpression subset, Hashtable context);

}