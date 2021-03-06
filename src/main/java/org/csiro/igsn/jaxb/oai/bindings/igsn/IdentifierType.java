//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.21 at 12:26:25 PM AWST 
//


package org.csiro.igsn.jaxb.oai.bindings.igsn;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for identifierType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="identifierType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ARK"/>
 *     &lt;enumeration value="DOI"/>
 *     &lt;enumeration value="Handle"/>
 *     &lt;enumeration value="IGSN"/>
 *     &lt;enumeration value="ISBN"/>
 *     &lt;enumeration value="ISNI"/>
 *     &lt;enumeration value="ISSN"/>
 *     &lt;enumeration value="LSID"/>
 *     &lt;enumeration value="ORCID"/>
 *     &lt;enumeration value="PURL"/>
 *     &lt;enumeration value="URI"/>
 *     &lt;enumeration value="VIAF"/>
 *     &lt;enumeration value="arXiv"/>
 *     &lt;enumeration value="bibcode"/>
 *     &lt;enumeration value="EAN13"/>
 *     &lt;enumeration value="EISSN"/>
 *     &lt;enumeration value="ISTC"/>
 *     &lt;enumeration value="PMID"/>
 *     &lt;enumeration value="LISSN"/>
 *     &lt;enumeration value="UPC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "identifierType")
@XmlEnum
public enum IdentifierType {

    ARK("ARK"),
    DOI("DOI"),
    @XmlEnumValue("Handle")
    HANDLE("Handle"),
    IGSN("IGSN"),
    ISBN("ISBN"),
    ISNI("ISNI"),
    ISSN("ISSN"),
    LSID("LSID"),
    ORCID("ORCID"),
    PURL("PURL"),
    URI("URI"),
    VIAF("VIAF"),
    @XmlEnumValue("arXiv")
    AR_XIV("arXiv"),
    @XmlEnumValue("bibcode")
    BIBCODE("bibcode"),
    @XmlEnumValue("EAN13")
    EAN_13("EAN13"),
    EISSN("EISSN"),
    ISTC("ISTC"),
    PMID("PMID"),
    LISSN("LISSN"),
    UPC("UPC");
    private final String value;

    IdentifierType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IdentifierType fromValue(String v) {
        for (IdentifierType c: IdentifierType.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
