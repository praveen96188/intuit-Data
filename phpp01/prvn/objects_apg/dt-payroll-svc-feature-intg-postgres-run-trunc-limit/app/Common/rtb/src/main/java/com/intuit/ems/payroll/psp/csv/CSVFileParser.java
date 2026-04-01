package com.intuit.ems.payroll.psp.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intuit.ems.payroll.psp.csv.model.FileRecord;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess.FileReader;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import edu.emory.mathcs.backport.java.util.Arrays;

//File's First line must be header
@Component
public class CSVFileParser {

    private static final String COMMA = ",";

    private FileReader fileReader;

    private static final SpcfLogger LOGGER = Application.getLogger(CSVFileParser.class);

    @Autowired
	public CSVFileParser(FileReader fileReader) {
		this.fileReader = fileReader;
	}

    public FileRecord parse(String filePath) throws IOException {
        LOGGER.info("Event=ParseFile,Status=Start,FilePath=" + filePath);

        List<String> lines = fileReader.read(filePath);

        if (lines.size() < 2) {
            throw new RuntimeException("File has no records.FilePath=" + filePath);
        }

        List<String> header = parseHeader(lines.get(0));
        List<List<String>> data = parseData(lines);

        FileRecord fileRecord = new FileRecord(header, data);

        LOGGER.info("Event=ParseFile,Status=Done,FilePath=" + filePath);
        return fileRecord;
    }

    private List<List<String>> parseData(List<String> lines) {
        List<List<String>> rowList = new ArrayList<List<String>>();

        lines.stream().skip(1).forEach(line -> {
            List<String> row = parseRow(line);
            rowList.add(row);
        });

        return rowList;
    }

    private List<String> parseHeader(String header) {
        return parseRow(header);
    }

    private List<String> parseRow(String line) {
        String[] columns = line.split(COMMA);
        List<String> row = Arrays.asList(columns);
        return row;
    }

}
