package com.intuit.sbd.payroll.psp.configuration;

import java.util.stream.Stream;

public enum DatabaseType {
    ORACLE("oracle"),
    POSTGRES("postgres")
    ;

    private final String name;

    /**
     * @param name
     */
    DatabaseType(final String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    public static DatabaseType fromValue(String givenName) {
        return Stream.of(values())
                .filter(databaseType -> databaseType.name.equalsIgnoreCase(givenName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}