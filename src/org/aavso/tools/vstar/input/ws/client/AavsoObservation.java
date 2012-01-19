
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for aavsoObservation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="aavsoObservation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="jd" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="magnitude" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="uncertainty" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "aavsoObservation", propOrder = {
    "jd",
    "magnitude",
    "uncertainty"
})
public class AavsoObservation {

    protected double jd;
    protected double magnitude;
    protected double uncertainty;

    /**
     * Gets the value of the jd property.
     * 
     */
    public double getJd() {
        return jd;
    }

    /**
     * Sets the value of the jd property.
     * 
     */
    public void setJd(double value) {
        this.jd = value;
    }

    /**
     * Gets the value of the magnitude property.
     * 
     */
    public double getMagnitude() {
        return magnitude;
    }

    /**
     * Sets the value of the magnitude property.
     * 
     */
    public void setMagnitude(double value) {
        this.magnitude = value;
    }

    /**
     * Gets the value of the uncertainty property.
     * 
     */
    public double getUncertainty() {
        return uncertainty;
    }

    /**
     * Sets the value of the uncertainty property.
     * 
     */
    public void setUncertainty(double value) {
        this.uncertainty = value;
    }

}
