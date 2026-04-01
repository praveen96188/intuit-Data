package com.intuit.ems.payroll.psp.vmpRealmUpdate;

import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class FileParser {

    private static final String COMMA = ",";

    private FileReader fileReader;

    @Autowired
    public FileParser(FileReader fileReader) {
        this.fileReader = fileReader;
    }

    public List<String> parse(String filePath) throws IOException {
        log.info("Event=ParseFile,Status=Start,FilePath=" + filePath);

        List<String> lines = fileReader.read(filePath);

        if (lines.size() < 1) {
            throw new RuntimeException("File has no records.FilePath=" + filePath);
        }
        return lines;
    }
}
