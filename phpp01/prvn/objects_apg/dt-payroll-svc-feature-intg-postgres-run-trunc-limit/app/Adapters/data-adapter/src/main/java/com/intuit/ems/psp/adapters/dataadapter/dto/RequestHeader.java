package com.intuit.ems.psp.adapters.dataadapter.dto;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 * Date: 7/21/15
 */
@XmlRootElement(name = "RatableRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestHeader {
    public static final String VALID_QBLICENSE_PATTERN = "\\d{4}-\\d{4}-\\d{4}-\\d{3}";
    private String QBLicense;
    private String SKU;
    private String Source;
    private String MajorVersion;
    private String MinorVersion;


    @XmlElement(name = "QBLicense", required = true)
    @NotNull(message = "may not be null or empty")
    @Size(min = 1, message = "may not be null or empty")
    public String getQBLicense() {
        return QBLicense;
    }

    public void setQBLicense(String pQBLicense) throws ValidationException {
        QBLicense = pQBLicense;
    }

    @XmlElement(name = "SKU")
    public String getSKU() {
        return SKU;
    }

    public void setSKU(String pSKU) {
        SKU = pSKU;
    }


    @XmlAttribute(name = "MinorVersion")
    public String getMinorVersion() {
        return MinorVersion;
    }

    public void setMinorVersion(String pMinorVersion) {
        MinorVersion = pMinorVersion;
    }

    @XmlAttribute(name = "Source")
    public String getSource() {
        return Source;
    }

    public void setSource(String pSource) {
        Source = pSource;
    }

    @XmlElement(name = "MajorVersion")
    public String getMajorVersion() {
        return MajorVersion;
    }

    public void setMajorVersion(String pMajorVersion) {
        MajorVersion = pMajorVersion;
    }
}
