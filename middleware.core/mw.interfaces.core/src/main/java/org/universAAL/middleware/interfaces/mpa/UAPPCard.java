package org.universAAL.middleware.interfaces.mpa;

import java.io.Serializable;

/**
 * Compact representation of an uApp
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class UAPPCard implements Serializable {

    private static final long serialVersionUID = -3217977547051129449L;
    private String name;
    private String id;
    private String description;

    public UAPPCard(String name, String id, String description) {
	this.name = name;
	this.id = id;
	this.description = description;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String toString() {
	return name + " - " + id + " - " + description;
    }

}
