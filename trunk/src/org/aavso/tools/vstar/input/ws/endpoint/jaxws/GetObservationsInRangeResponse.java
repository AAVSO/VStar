
package org.aavso.tools.vstar.input.ws.endpoint.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getObservationsInRangeResponse", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getObservationsInRangeResponse", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
public class GetObservationsInRangeResponse {

    @XmlElement(name = "return", namespace = "", nillable = true)
    private org.aavso.tools.vstar.input.ws.endpoint.Ob[] _return;

    /**
     * 
     * @return
     *     returns Ob[]
     */
    public org.aavso.tools.vstar.input.ws.endpoint.Ob[] getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(org.aavso.tools.vstar.input.ws.endpoint.Ob[] _return) {
        this._return = _return;
    }

}
