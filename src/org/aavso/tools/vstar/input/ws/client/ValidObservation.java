
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for validObservation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="validObservation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://endpoint.ws.input.vstar.tools.aavso.org/}observation">
 *       &lt;sequence>
 *         &lt;element name="airmass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="band" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}seriesType" minOccurs="0"/>
 *         &lt;element name="CMag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="charts" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="compStar1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="compStar2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dateInfo" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}dateInfo" minOccurs="0"/>
 *         &lt;element name="discrepant" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="excluded" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="HJD" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}dateInfo" minOccurs="0"/>
 *         &lt;element name="hqUncertainty" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="KMag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MType" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}mTypeType" minOccurs="0"/>
 *         &lt;element name="magnitude" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}magnitude" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="obsCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="previousCyclePhase" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="standardPhase" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="transformed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="validationType" type="{http://endpoint.ws.input.vstar.tools.aavso.org/}validationType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "validObservation", propOrder = {
    "airmass",
    "band",
    "cMag",
    "charts",
    "comments",
    "compStar1",
    "compStar2",
    "dateInfo",
    "discrepant",
    "excluded",
    "hjd",
    "hqUncertainty",
    "kMag",
    "mType",
    "magnitude",
    "name",
    "obsCode",
    "previousCyclePhase",
    "standardPhase",
    "transformed",
    "validationType"
})
public class ValidObservation
    extends Observation
{

    protected String airmass;
    protected SeriesType band;
    @XmlElement(name = "CMag")
    protected String cMag;
    protected String charts;
    protected String comments;
    protected String compStar1;
    protected String compStar2;
    protected DateInfo dateInfo;
    protected boolean discrepant;
    protected boolean excluded;
    @XmlElement(name = "HJD")
    protected DateInfo hjd;
    protected Double hqUncertainty;
    @XmlElement(name = "KMag")
    protected String kMag;
    @XmlElement(name = "MType")
    protected MTypeType mType;
    protected Magnitude magnitude;
    protected String name;
    protected String obsCode;
    protected Double previousCyclePhase;
    protected Double standardPhase;
    protected boolean transformed;
    protected ValidationType validationType;

    /**
     * Gets the value of the airmass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAirmass() {
        return airmass;
    }

    /**
     * Sets the value of the airmass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAirmass(String value) {
        this.airmass = value;
    }

    /**
     * Gets the value of the band property.
     * 
     * @return
     *     possible object is
     *     {@link SeriesType }
     *     
     */
    public SeriesType getBand() {
        return band;
    }

    /**
     * Sets the value of the band property.
     * 
     * @param value
     *     allowed object is
     *     {@link SeriesType }
     *     
     */
    public void setBand(SeriesType value) {
        this.band = value;
    }

    /**
     * Gets the value of the cMag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCMag() {
        return cMag;
    }

    /**
     * Sets the value of the cMag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCMag(String value) {
        this.cMag = value;
    }

    /**
     * Gets the value of the charts property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharts() {
        return charts;
    }

    /**
     * Sets the value of the charts property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharts(String value) {
        this.charts = value;
    }

    /**
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComments(String value) {
        this.comments = value;
    }

    /**
     * Gets the value of the compStar1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompStar1() {
        return compStar1;
    }

    /**
     * Sets the value of the compStar1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompStar1(String value) {
        this.compStar1 = value;
    }

    /**
     * Gets the value of the compStar2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompStar2() {
        return compStar2;
    }

    /**
     * Sets the value of the compStar2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompStar2(String value) {
        this.compStar2 = value;
    }

    /**
     * Gets the value of the dateInfo property.
     * 
     * @return
     *     possible object is
     *     {@link DateInfo }
     *     
     */
    public DateInfo getDateInfo() {
        return dateInfo;
    }

    /**
     * Sets the value of the dateInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateInfo }
     *     
     */
    public void setDateInfo(DateInfo value) {
        this.dateInfo = value;
    }

    /**
     * Gets the value of the discrepant property.
     * 
     */
    public boolean isDiscrepant() {
        return discrepant;
    }

    /**
     * Sets the value of the discrepant property.
     * 
     */
    public void setDiscrepant(boolean value) {
        this.discrepant = value;
    }

    /**
     * Gets the value of the excluded property.
     * 
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * Sets the value of the excluded property.
     * 
     */
    public void setExcluded(boolean value) {
        this.excluded = value;
    }

    /**
     * Gets the value of the hjd property.
     * 
     * @return
     *     possible object is
     *     {@link DateInfo }
     *     
     */
    public DateInfo getHJD() {
        return hjd;
    }

    /**
     * Sets the value of the hjd property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateInfo }
     *     
     */
    public void setHJD(DateInfo value) {
        this.hjd = value;
    }

    /**
     * Gets the value of the hqUncertainty property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHqUncertainty() {
        return hqUncertainty;
    }

    /**
     * Sets the value of the hqUncertainty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setHqUncertainty(Double value) {
        this.hqUncertainty = value;
    }

    /**
     * Gets the value of the kMag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKMag() {
        return kMag;
    }

    /**
     * Sets the value of the kMag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKMag(String value) {
        this.kMag = value;
    }

    /**
     * Gets the value of the mType property.
     * 
     * @return
     *     possible object is
     *     {@link MTypeType }
     *     
     */
    public MTypeType getMType() {
        return mType;
    }

    /**
     * Sets the value of the mType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MTypeType }
     *     
     */
    public void setMType(MTypeType value) {
        this.mType = value;
    }

    /**
     * Gets the value of the magnitude property.
     * 
     * @return
     *     possible object is
     *     {@link Magnitude }
     *     
     */
    public Magnitude getMagnitude() {
        return magnitude;
    }

    /**
     * Sets the value of the magnitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Magnitude }
     *     
     */
    public void setMagnitude(Magnitude value) {
        this.magnitude = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the obsCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObsCode() {
        return obsCode;
    }

    /**
     * Sets the value of the obsCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObsCode(String value) {
        this.obsCode = value;
    }

    /**
     * Gets the value of the previousCyclePhase property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPreviousCyclePhase() {
        return previousCyclePhase;
    }

    /**
     * Sets the value of the previousCyclePhase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPreviousCyclePhase(Double value) {
        this.previousCyclePhase = value;
    }

    /**
     * Gets the value of the standardPhase property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getStandardPhase() {
        return standardPhase;
    }

    /**
     * Sets the value of the standardPhase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setStandardPhase(Double value) {
        this.standardPhase = value;
    }

    /**
     * Gets the value of the transformed property.
     * 
     */
    public boolean isTransformed() {
        return transformed;
    }

    /**
     * Sets the value of the transformed property.
     * 
     */
    public void setTransformed(boolean value) {
        this.transformed = value;
    }

    /**
     * Gets the value of the validationType property.
     * 
     * @return
     *     possible object is
     *     {@link ValidationType }
     *     
     */
    public ValidationType getValidationType() {
        return validationType;
    }

    /**
     * Sets the value of the validationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValidationType }
     *     
     */
    public void setValidationType(ValidationType value) {
        this.validationType = value;
    }

}
