
package org.aavso.tools.vstar.input.ws.client;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for seriesType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="seriesType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Visual"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Johnson_R"/>
 *     &lt;enumeration value="Johnson_I"/>
 *     &lt;enumeration value="Johnson_V"/>
 *     &lt;enumeration value="Blue"/>
 *     &lt;enumeration value="Green"/>
 *     &lt;enumeration value="Red"/>
 *     &lt;enumeration value="Yellow"/>
 *     &lt;enumeration value="K_NIR_2pt2micron"/>
 *     &lt;enumeration value="H_NIR_1pt6micron"/>
 *     &lt;enumeration value="J_NIR_1pt2micron"/>
 *     &lt;enumeration value="Sloan_z"/>
 *     &lt;enumeration value="Johnson_B"/>
 *     &lt;enumeration value="Stromgren_u"/>
 *     &lt;enumeration value="Stromgren_v"/>
 *     &lt;enumeration value="Stromgren_b"/>
 *     &lt;enumeration value="Stromgren_y"/>
 *     &lt;enumeration value="Stromgren_Hbw"/>
 *     &lt;enumeration value="Stromgren_Hbn"/>
 *     &lt;enumeration value="Cousins_R"/>
 *     &lt;enumeration value="Sloan_u"/>
 *     &lt;enumeration value="Sloan_g"/>
 *     &lt;enumeration value="Sloan_r"/>
 *     &lt;enumeration value="Sloan_i"/>
 *     &lt;enumeration value="Cousins_I"/>
 *     &lt;enumeration value="Tri_Color_Blue"/>
 *     &lt;enumeration value="Tri_Color_Green"/>
 *     &lt;enumeration value="Tri_Color_Red"/>
 *     &lt;enumeration value="Optec_Wing_A"/>
 *     &lt;enumeration value="Optec_Wing_B"/>
 *     &lt;enumeration value="Optec_Wing_C"/>
 *     &lt;enumeration value="Orange_Liller"/>
 *     &lt;enumeration value="Johnson_U"/>
 *     &lt;enumeration value="Unfiltered_with_V_Zeropoint"/>
 *     &lt;enumeration value="Unfiltered_with_Red_Zeropoint"/>
 *     &lt;enumeration value="FAINTER_THAN"/>
 *     &lt;enumeration value="MEANS"/>
 *     &lt;enumeration value="DISCREPANT"/>
 *     &lt;enumeration value="Unspecified"/>
 *     &lt;enumeration value="Filtered"/>
 *     &lt;enumeration value="Model"/>
 *     &lt;enumeration value="Residuals"/>
 *     &lt;enumeration value="Excluded"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "seriesType")
@XmlEnum
public enum SeriesType {

    @XmlEnumValue("Visual")
    VISUAL("Visual"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Johnson_R")
    JOHNSON_R("Johnson_R"),
    @XmlEnumValue("Johnson_I")
    JOHNSON_I("Johnson_I"),
    @XmlEnumValue("Johnson_V")
    JOHNSON_V("Johnson_V"),
    @XmlEnumValue("Blue")
    BLUE("Blue"),
    @XmlEnumValue("Green")
    GREEN("Green"),
    @XmlEnumValue("Red")
    RED("Red"),
    @XmlEnumValue("Yellow")
    YELLOW("Yellow"),
    @XmlEnumValue("K_NIR_2pt2micron")
    K_NIR_2_PT_2_MICRON("K_NIR_2pt2micron"),
    @XmlEnumValue("H_NIR_1pt6micron")
    H_NIR_1_PT_6_MICRON("H_NIR_1pt6micron"),
    @XmlEnumValue("J_NIR_1pt2micron")
    J_NIR_1_PT_2_MICRON("J_NIR_1pt2micron"),
    @XmlEnumValue("Sloan_z")
    SLOAN_Z("Sloan_z"),
    @XmlEnumValue("Johnson_B")
    JOHNSON_B("Johnson_B"),
    @XmlEnumValue("Stromgren_u")
    STROMGREN_U("Stromgren_u"),
    @XmlEnumValue("Stromgren_v")
    STROMGREN_V("Stromgren_v"),
    @XmlEnumValue("Stromgren_b")
    STROMGREN_B("Stromgren_b"),
    @XmlEnumValue("Stromgren_y")
    STROMGREN_Y("Stromgren_y"),
    @XmlEnumValue("Stromgren_Hbw")
    STROMGREN_HBW("Stromgren_Hbw"),
    @XmlEnumValue("Stromgren_Hbn")
    STROMGREN_HBN("Stromgren_Hbn"),
    @XmlEnumValue("Cousins_R")
    COUSINS_R("Cousins_R"),
    @XmlEnumValue("Sloan_u")
    SLOAN_U("Sloan_u"),
    @XmlEnumValue("Sloan_g")
    SLOAN_G("Sloan_g"),
    @XmlEnumValue("Sloan_r")
    SLOAN_R("Sloan_r"),
    @XmlEnumValue("Sloan_i")
    SLOAN_I("Sloan_i"),
    @XmlEnumValue("Cousins_I")
    COUSINS_I("Cousins_I"),
    @XmlEnumValue("Tri_Color_Blue")
    TRI_COLOR_BLUE("Tri_Color_Blue"),
    @XmlEnumValue("Tri_Color_Green")
    TRI_COLOR_GREEN("Tri_Color_Green"),
    @XmlEnumValue("Tri_Color_Red")
    TRI_COLOR_RED("Tri_Color_Red"),
    @XmlEnumValue("Optec_Wing_A")
    OPTEC_WING_A("Optec_Wing_A"),
    @XmlEnumValue("Optec_Wing_B")
    OPTEC_WING_B("Optec_Wing_B"),
    @XmlEnumValue("Optec_Wing_C")
    OPTEC_WING_C("Optec_Wing_C"),
    @XmlEnumValue("Orange_Liller")
    ORANGE_LILLER("Orange_Liller"),
    @XmlEnumValue("Johnson_U")
    JOHNSON_U("Johnson_U"),
    @XmlEnumValue("Unfiltered_with_V_Zeropoint")
    UNFILTERED_WITH_V_ZEROPOINT("Unfiltered_with_V_Zeropoint"),
    @XmlEnumValue("Unfiltered_with_Red_Zeropoint")
    UNFILTERED_WITH_RED_ZEROPOINT("Unfiltered_with_Red_Zeropoint"),
    FAINTER_THAN("FAINTER_THAN"),
    MEANS("MEANS"),
    DISCREPANT("DISCREPANT"),
    @XmlEnumValue("Unspecified")
    UNSPECIFIED("Unspecified"),
    @XmlEnumValue("Filtered")
    FILTERED("Filtered"),
    @XmlEnumValue("Model")
    MODEL("Model"),
    @XmlEnumValue("Residuals")
    RESIDUALS("Residuals"),
    @XmlEnumValue("Excluded")
    EXCLUDED("Excluded");
    private final String value;

    SeriesType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SeriesType fromValue(String v) {
        for (SeriesType c: SeriesType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
