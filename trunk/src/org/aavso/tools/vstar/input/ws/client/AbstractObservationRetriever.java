
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for abstractObservationRetriever complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractObservationRetriever">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="maxMag" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="minMag" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractObservationRetriever", propOrder = {
    "maxMag",
    "minMag"
})
public abstract class AbstractObservationRetriever {

    protected double maxMag;
    protected double minMag;

    /**
     * Gets the value of the maxMag property.
     * 
     */
    public double getMaxMag() {
        return maxMag;
    }

    /**
     * Sets the value of the maxMag property.
     * 
     */
    public void setMaxMag(double value) {
        this.maxMag = value;
    }

    /**
     * Gets the value of the minMag property.
     * 
     */
    public double getMinMag() {
        return minMag;
    }

    /**
     * Sets the value of the minMag property.
     * 
     */
    public void setMinMag(double value) {
        this.minMag = value;
    }

}
