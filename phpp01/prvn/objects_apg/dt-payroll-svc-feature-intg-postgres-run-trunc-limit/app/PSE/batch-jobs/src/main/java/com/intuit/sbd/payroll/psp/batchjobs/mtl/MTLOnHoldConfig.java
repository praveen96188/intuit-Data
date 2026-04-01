package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.batchjobs.entity.EntityPublisherConfig;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class MTLOnHoldConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityPublisherConfig.class);
    private final CommandLineParser commandLineParser = new PosixParser();

    private List<String> sourceCompanyPSIDs;
    private int chunkSize;

    public MTLOnHoldConfig(String[] args){
        try{
            CommandLine commandLine = getCommandLine(args);
            for (Argument argument : Argument.values()) {
                switch (argument){
                    case BATCH_SOURCE_COMPANY_PSIDS:
                        setSourceCompanyPSIDs(getSourceCompanyPSIDs(commandLine, argument));
                        break;
                    case MTL_CHUNK_SIZE:
                        setChunkSize(getChunkSize(commandLine, argument));
                        break;
                    default:
                        break;
                }
            }
        }catch (ParseException e) {
            LOGGER.error("Failed to parse the command line options", e);
            throw new RuntimeException("Failed to parse the command line options", e);
        }
    }

    private int getChunkSize(CommandLine commandLine, MTLOnHoldConfig.Argument argument){
        int chunkSize = 0;
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())){
            chunkSize = Integer.parseInt(commandLine.getOptionValue(argument.getName()));
        }
        return Integer.parseInt(argument.getDefaultValue().toString());
    }


    private List<String> getSourceCompanyPSIDs(CommandLine commandLine, MTLOnHoldConfig.Argument argument) {
        List<String> list = new ArrayList<>();
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            list = new ArrayList<>(Arrays.asList(commandLine.getOptionValue(argument.getName()).split(",")));
        }
        return list;
    }


    private CommandLine getCommandLine(String[] args) throws ParseException {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        return commandLineParser.parse(getOptions(), args);
    }


    public static Options getOptions() {
        Options options = new Options();
        for (EntityPublisherConfig.Argument argument : EntityPublisherConfig.Argument.values()) {
            options.addOption(argument.getName(), true, argument.getDescription());
        }
        return options;
    }

    public enum Argument {
        BATCH_SOURCE_COMPANY_PSIDS("psIds", null, "List of PSID to be put on MTLHOLD", ""),
        MTL_CHUNK_SIZE("chunkSize", null, "Chunk Size for number of companies to be put on hold at once", 20);

        private String name;
        private SystemParameter.Code code;
        private String description;
        private Object defaultValue;

        Argument(String name, SystemParameter.Code code, String description, Object defaultValue) {
            this.name = name;
            this.code = code;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public SystemParameter.Code getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}