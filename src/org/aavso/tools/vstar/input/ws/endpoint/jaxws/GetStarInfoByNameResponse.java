
package org.aavso.tools.vstar.input.ws.endpoint.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getStarInfoByNameResponse", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getStarInfoByNameResponse", namespace = "http://endpoint.ws.input.vstar.tools.aavso.org/")
public class GetStarInfoByNameResponse {

    @XmlElement(name = "return", namespace = "")
    private org.aavso.tools.vstar.ui.mediator.StarInfo _return;

    /**
     * 
     * @return
     *     returns StarInfo
     */
    public org.aavso.tools.vstar.ui.mediator.StarInfo getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(org.aavso.tools.vstar.ui.mediator.StarInfo _return) {
        this._return = _return;
    }

}
