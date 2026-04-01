package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class ConfigFileParser {

    public ConfigFileModel[] getJsonConfig(String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(new File(filePath), ConfigFileModel[].class);
        } catch (Exception e) {
            log.error("job=SendCustomEmailsProcessor, Action=ConfigFileParser, Method=getJsonConfig, Status=Error");
            throw new RuntimeException(e);
        }
    }
}
