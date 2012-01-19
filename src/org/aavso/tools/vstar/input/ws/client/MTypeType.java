
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="mTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="STD"/>
 *     &lt;enumeration value="DIFF"/>
 *     &lt;enumeration value="STEP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "mTypeType")
@XmlEnum
public enum MTypeType {

    STD,
    DIFF,
    STEP;

    public String value() {
        return name();
    }

    public static MTypeType fromValue(String v) {
        return valueOf(v);
    }

}
