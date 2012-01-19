
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for starInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="starInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="auid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="designation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="discoverer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="epoch" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="period" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="retriever" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}abstractObservationRetriever" minOccurs="0"/>
 *         &lt;element name="spectralType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="varType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "starInfo", propOrder = {
    "auid",
    "designation",
    "discoverer",
    "epoch",
    "period",
    "retriever",
    "spectralType",
    "varType"
})
public class StarInfo {

    protected String auid;
    protected String designation;
    protected String discoverer;
    protected Double epoch;
    protected Double period;
    protected AbstractObservationRetriever retriever;
    protected String spectralType;
    protected String varType;

    /**
     * Gets the value of the auid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuid() {
        return auid;
    }

    /**
     * Sets the value of the auid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuid(String value) {
        this.auid = value;
    }

    /**
     * Gets the value of the designation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesignation() {
        return designation;
    }

    /**
     * Sets the value of the designation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesignation(String value) {
        this.designation = value;
    }

    /**
     * Gets the value of the discoverer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDiscoverer() {
        return discoverer;
    }

    /**
     * Sets the value of the discoverer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDiscoverer(String value) {
        this.discoverer = value;
    }

    /**
     * Gets the value of the epoch property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEpoch() {
        return epoch;
    }

    /**
     * Sets the value of the epoch property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEpoch(Double value) {
        this.epoch = value;
    }

    /**
     * Gets the value of the period property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPeriod(Double value) {
        this.period = value;
    }

    /**
     * Gets the value of the retriever property.
     * 
     * @return
     *     possible object is
     *     {@link AbstractObservationRetriever }
     *     
     */
    public AbstractObservationRetriever getRetriever() {
        return retriever;
    }

    /**
     * Sets the value of the retriever property.
     * 
     * @param value
     *     allowed object is
     *     {@link AbstractObservationRetriever }
     *     
     */
    public void setRetriever(AbstractObservationRetriever value) {
        this.retriever = value;
    }

    /**
     * Gets the value of the spectralType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpectralType() {
        return spectralType;
    }

    /**
     * Sets the value of the spectralType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpectralType(String value) {
        this.spectralType = value;
    }

    /**
     * Gets the value of the varType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarType() {
        return varType;
    }

    /**
     * Sets the value of the varType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarType(String value) {
        this.varType = value;
    }

}
