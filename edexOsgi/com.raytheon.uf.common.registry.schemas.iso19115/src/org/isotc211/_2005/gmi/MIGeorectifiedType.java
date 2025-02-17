//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.01.10 at 10:31:13 AM CST 
//


package org.isotc211._2005.gmi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.isotc211._2005.gmd.MDGeorectifiedType;


/**
 * Description: extends georectified grid description to include associated checkpoints - shortName: IGeorect
 * 
 * <p>Java class for MI_Georectified_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_Georectified_Type">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.isotc211.org/2005/gmd}MD_Georectified_Type">
 *       &lt;sequence>
 *         &lt;element name="checkPoint" type="{http://www.isotc211.org/2005/gmi}MI_GCP_PropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_Georectified_Type", propOrder = {
    "checkPoint"
})
public class MIGeorectifiedType
    extends MDGeorectifiedType
{

    protected List<MIGCPPropertyType> checkPoint;

    /**
     * Gets the value of the checkPoint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the checkPoint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCheckPoint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MIGCPPropertyType }
     * 
     * 
     */
    public List<MIGCPPropertyType> getCheckPoint() {
        if (checkPoint == null) {
            checkPoint = new ArrayList<MIGCPPropertyType>();
        }
        return this.checkPoint;
    }

}
