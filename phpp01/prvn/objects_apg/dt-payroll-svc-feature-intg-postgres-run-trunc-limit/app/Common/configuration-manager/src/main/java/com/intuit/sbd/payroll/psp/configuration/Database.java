package com.intuit.sbd.payroll.psp.configuration;

public enum Database {
    MONOLITH("monolith"),
    AUDIT("audit")
    ;

    private final String name;

    /**
     * @param name
     */
    Database(final String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
