/**
 * 
 */
package org.universAAL.middleware.container;

/**
 * @author mtazari
 * 
 */
public interface SharedObjectListener {
    public void sharedObjectAdded(Object sharedObj, Object removeHook);

    public void sharedObjectRemoved(Object removeHook);
}
