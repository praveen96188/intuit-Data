package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
  * User: ihannur
 * Date: Aug 26, 2011
 * Time: 12:07:31 PM
 */
public class SAPKeyValuePair {
    private String key;
    private String value;

    public SAPKeyValuePair(String pKey, String pValue) {
        key = pKey;
        value = pValue;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SAPKeyValuePair that = (SAPKeyValuePair) o;

        if (!key.equals(that.key)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
