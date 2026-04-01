package com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.WorkflowFinderConstants;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileReader {

    private static final SpcfLogger LOGGER = Application.getLogger(FileReader.class);

    public List<String> read(String filePath){
        LOGGER.info("Event=FileRead, Status=Start, FileName=" + filePath);
        List<String> lines = new ArrayList<>();
        Path path = Paths.get(filePath);
        try {
            Files.lines(path).forEach(line -> {
                if(StringUtils.isNotBlank(line)) {
                    lines.add(line.replace(WorkflowFinderConstants.DOUBLE_QUOTES, StringUtils.EMPTY));
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Event=FileRead, Status=Failed, FileName=" + filePath);
        }
        LOGGER.info("Event=FileRead, Status=Done, FileName=" + filePath + ", Total Workflows=" + lines.size());
        return lines;
    }
}
