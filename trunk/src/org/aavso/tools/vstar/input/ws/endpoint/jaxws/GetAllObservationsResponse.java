
package org.aavso.tools.vstar.input.ws.endpoint.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getAllObservationsResponse", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAllObservationsResponse", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
public class GetAllObservationsResponse {

    @XmlElement(name = "return", namespace = "", nillable = true)
    private org.aavso.tools.vstar.data.ValidObservation[] _return;

    /**
     * 
     * @return
     *     returns ValidObservation[]
     */
    public org.aavso.tools.vstar.data.ValidObservation[] getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(org.aavso.tools.vstar.data.ValidObservation[] _return) {
        this._return = _return;
    }

}
