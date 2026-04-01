package com.intuit.sbd.payroll.psp.filter.config;

import com.intuit.sbd.payroll.psp.filter.constants.PartitionedTablesDetails;
import com.intuit.sbg.psp.filtervalidator.configuration.FilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class PartitionedTableConfiguration {
    private PartitionedTablesDetails partitionedTablesDetails;

    @Autowired
    public PartitionedTableConfiguration(PartitionedTablesDetails partitionedTablesDetails) {
        this.partitionedTablesDetails = partitionedTablesDetails;
    }

    @Bean
    public FilterConfiguration filterConfiguration(){
        Set<String> excludedTables = new HashSet<>();
        excludedTables.add("PSP_SYSTEM_PARAMETER");
        excludedTables.add("PSP_SOURCE_SYSTEM_TRANSMISSION");
        return FilterConfiguration.builder()
                .childTables(partitionedTablesDetails.getChildClassTableNameMap().values().stream().collect(Collectors.toSet()))
                .partitionedTables(partitionedTablesDetails.getClassTableNameMap().values().stream().collect(Collectors.toSet()))
                .excludedTables(excludedTables)
                .build();
    }

}
