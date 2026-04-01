package com.intuit.sbd.payroll.psp.bedl;

/**
 * Internal specifications embedded within notes of a DataEntity.  These specificatoins
 * augment the model meta data.
 * <p/>
 * Specifications take the form:
 * <spec>(<values>), i.e. AuditProperties(Attribute1,Attribute2)
 * <p/>
 * Multiple specifications may be present.  They are separated by ';'
 */
public class BedlInternalNoteSpec {
    public static final String AUDIT_ASSOCIATIONS = "AuditAssociations";
    public static final String AUDIT_PROPERTIES = "AuditProperties";
    public static final String VERSIONING = "Versioning";
    public static final String FOREIGNKEYORDER = "ForeignKeyOrder";
    public static final String NULL_ENUMS = "NullEnums";
    public static final String DYNAMIC_UPDATE = "DynamicUpdate";

    private String noteText;
    private String spec;
    private String[] values;

    BedlInternalNoteSpec(String noteText) {
        this.noteText = noteText.trim();

        int iValuesBegin = noteText.indexOf("(");
        if (iValuesBegin == -1)
            spec = noteText;
        else {
            spec = noteText.substring(0, iValuesBegin);
            String valuesList = noteText.substring(iValuesBegin + 1, noteText.indexOf(")"));
            valuesList = valuesList.replaceAll(" ", "");
            values = valuesList.split(",");
        }
    }

    public String getSpec() {
        return spec;
    }

    public String getValue() {
        if (values == null || values.length == 0)
            return null;

        return values[0];
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] pValues) {
        values = pValues;
    }

    public Integer getValueOrdinal(String pValue) {
        int i = 1;
        for (String value : getValues()) {
            if (value.equals(pValue)) {
                return i;
            }
            i++;
        }
        
        return null;
    }
}
