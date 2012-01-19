
package org.aavso.tools.vstar.input.ws.endpoint.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getAllObservations", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAllObservations", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/", propOrder = {
    "arg0",
    "arg1"
})
public class GetAllObservations {

    @XmlElement(name = "arg0", namespace = "")
    private org.aavso.tools.vstar.ui.mediator.StarInfo arg0;
    @XmlElement(name = "arg1", namespace = "")
    private int arg1;

    /**
     * 
     * @return
     *     returns StarInfo
     */
    public org.aavso.tools.vstar.ui.mediator.StarInfo getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(org.aavso.tools.vstar.ui.mediator.StarInfo arg0) {
        this.arg0 = arg0;
    }

    /**
     * 
     * @return
     *     returns int
     */
    public int getArg1() {
        return this.arg1;
    }

    /**
     * 
     * @param arg1
     *     the value for the arg1 property
     */
    public void setArg1(int arg1) {
        this.arg1 = arg1;
    }

}
