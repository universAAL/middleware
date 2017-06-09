/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

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

package org.universAAL.middleware.deploymanager.uapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bundleId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bundleVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="partCapabilities" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="capability" type="{http://www.universaal.org/aal-uapp/v1.0.2}capabilityType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="partRequirements" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="requirement" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.universaal.org/aal-uapp/v1.0.2}deploymentUnit" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.universaal.org/aal-uapp/v1.0.2}executionUnit" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="partId" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "bundleId", "bundleVersion", "partCapabilities", "partRequirements", "deploymentUnit",
		"executionUnit" })
@XmlRootElement(name = "part")
public class Part implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(required = true)
	protected String bundleId;
	@XmlElement(required = true)
	protected String bundleVersion;
	protected Part.PartCapabilities partCapabilities;
	protected Part.PartRequirements partRequirements;
	@XmlElement(required = true)
	protected List<DeploymentUnit> deploymentUnit;
	protected List<ExecutionUnit> executionUnit;
	@XmlAttribute(name = "partId")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String partId;

	/**
	 * Gets the value of the bundleId property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getBundleId() {
		return bundleId;
	}

	/**
	 * Sets the value of the bundleId property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setBundleId(String value) {
		this.bundleId = value;
	}

	public boolean isSetBundleId() {
		return (this.bundleId != null);
	}

	/**
	 * Gets the value of the bundleVersion property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getBundleVersion() {
		return bundleVersion;
	}

	/**
	 * Sets the value of the bundleVersion property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setBundleVersion(String value) {
		this.bundleVersion = value;
	}

	public boolean isSetBundleVersion() {
		return (this.bundleVersion != null);
	}

	/**
	 * Gets the value of the partCapabilities property.
	 *
	 * @return possible object is {@link Part.PartCapabilities }
	 *
	 */
	public Part.PartCapabilities getPartCapabilities() {
		return partCapabilities;
	}

	/**
	 * Sets the value of the partCapabilities property.
	 *
	 * @param value
	 *            allowed object is {@link Part.PartCapabilities }
	 *
	 */
	public void setPartCapabilities(Part.PartCapabilities value) {
		this.partCapabilities = value;
	}

	public boolean isSetPartCapabilities() {
		return (this.partCapabilities != null);
	}

	/**
	 * Gets the value of the partRequirements property.
	 *
	 * @return possible object is {@link Part.PartRequirements }
	 *
	 */
	public Part.PartRequirements getPartRequirements() {
		return partRequirements;
	}

	/**
	 * Sets the value of the partRequirements property.
	 *
	 * @param value
	 *            allowed object is {@link Part.PartRequirements }
	 *
	 */
	public void setPartRequirements(Part.PartRequirements value) {
		this.partRequirements = value;
	}

	public boolean isSetPartRequirements() {
		return (this.partRequirements != null);
	}

	/**
	 * Gets the value of the deploymentUnit property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the deploymentUnit property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDeploymentUnit().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DeploymentUnit }
	 *
	 *
	 */
	public List<DeploymentUnit> getDeploymentUnit() {
		if (deploymentUnit == null) {
			deploymentUnit = new ArrayList<DeploymentUnit>();
		}
		return this.deploymentUnit;
	}

	public boolean isSetDeploymentUnit() {
		return ((this.deploymentUnit != null) && (!this.deploymentUnit.isEmpty()));
	}

	public void unsetDeploymentUnit() {
		this.deploymentUnit = null;
	}

	/**
	 * Gets the value of the executionUnit property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the executionUnit property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExecutionUnit().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ExecutionUnit }
	 *
	 *
	 */
	public List<ExecutionUnit> getExecutionUnit() {
		if (executionUnit == null) {
			executionUnit = new ArrayList<ExecutionUnit>();
		}
		return this.executionUnit;
	}

	public boolean isSetExecutionUnit() {
		return ((this.executionUnit != null) && (!this.executionUnit.isEmpty()));
	}

	public void unsetExecutionUnit() {
		this.executionUnit = null;
	}

	/**
	 * Gets the value of the partId property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getPartId() {
		return partId;
	}

	/**
	 * Sets the value of the partId property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setPartId(String value) {
		this.partId = value;
	}

	public boolean isSetPartId() {
		return (this.partId != null);
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 *
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 *
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="capability" type="{http://www.universaal.org/aal-uapp/v1.0.2}capabilityType" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "capability" })
	public static class PartCapabilities implements Serializable {

		private final static long serialVersionUID = 12343L;
		@XmlElement(required = true)
		protected List<CapabilityType> capability;

		/**
		 * Gets the value of the capability property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the capability property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getCapability().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link CapabilityType }
		 *
		 *
		 */
		public List<CapabilityType> getCapability() {
			if (capability == null) {
				capability = new ArrayList<CapabilityType>();
			}
			return this.capability;
		}

		public boolean isSetCapability() {
			return ((this.capability != null) && (!this.capability.isEmpty()));
		}

		public void unsetCapability() {
			this.capability = null;
		}

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 *
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 *
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="requirement" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqType" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "requirement" })
	public static class PartRequirements implements Serializable {

		private final static long serialVersionUID = 12343L;
		@XmlElement(required = true)
		protected List<ReqType> requirement;

		/**
		 * Gets the value of the requirement property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the requirement property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getRequirement().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ReqType }
		 *
		 *
		 */
		public List<ReqType> getRequirement() {
			if (requirement == null) {
				requirement = new ArrayList<ReqType>();
			}
			return this.requirement;
		}

		public boolean isSetRequirement() {
			return ((this.requirement != null) && (!this.requirement.isEmpty()));
		}

		public void unsetRequirement() {
			this.requirement = null;
		}

	}

}
